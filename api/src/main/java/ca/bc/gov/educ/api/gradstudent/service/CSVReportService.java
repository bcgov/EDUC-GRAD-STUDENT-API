package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.OptionalProgramCodes;
import ca.bc.gov.educ.api.gradstudent.constant.v1.StudentCourseSearchReportHeader;
import ca.bc.gov.educ.api.gradstudent.constant.v1.StudentOptionalProgramSearchReportHeader;
import ca.bc.gov.educ.api.gradstudent.constant.v1.StudentProgramSearchReportHeader;
import ca.bc.gov.educ.api.gradstudent.constant.v1.StudentSearchReportHeader;
import ca.bc.gov.educ.api.gradstudent.constant.v1.YukonReportHeader;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.exception.GradStudentAPIRuntimeException;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CourseCodeRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.OptionalProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.District;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class CSVReportService {

    // Stream Constants
    private static final int CSV_BUFFER_SIZE = 1024;
    private static final int CSV_FLUSH_INTERVAL = 100;

    // CSV Report Constants
    private static final String CONTENT_TYPE_CSV = "text/csv";
    private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";
    private static final String DATE_FORMAT_YYYYMMDD = "yyyyMMdd";
    private static final String CSV_FILE_EXTENSION = ".csv\"";
    private static final String ATTACHMENT_FILENAME_PREFIX = "attachment; filename=\"";

    private final RestUtils restUtils;
    private final GraduationStudentRecordRepository graduationStudentRecordRepository;
    private final StudentCoursePaginationService studentCoursePaginationService;
    private final StudentCoursePaginationRepository studentCoursePaginationRepository;
    private final StudentOptionalProgramPaginationService studentOptionalProgramPaginationService;
    private final StudentOptionalProgramPaginationRepository studentOptionalProgramPaginationRepository;
    private final StudentOptionalProgramPaginationLeanRepository studentOptionalProgramPaginationLeanRepository;
    private final GradStudentSearchService gradStudentSearchService;
    private final GradStudentSearchRepository gradStudentSearchRepository;

    public DownloadableReportResponse generateYukonReport(UUID districtID, String fromDate, String toDate) {
        var district = restUtils.getDistrictByDistrictID(districtID.toString()).orElseThrow(() -> new EntityNotFoundException(District.class, "districtID", districtID.toString()));
        List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();

        List<UUID> schoolsInDistrict = restUtils.getSchoolList()
                .stream()
                .filter(school -> Objects.equals(school.getDistrictId(), district.getDistrictId()))
                .map(School::getSchoolId)
                .map(UUID::fromString)
                .toList();

        Date from = Date.valueOf(LocalDate.parse(fromDate));
        Date to = Date.valueOf(LocalDate.parse(toDate));

        List<GraduationStudentRecordEntity> results = graduationStudentRecordRepository.findByProgramCompletionDateIsGreaterThanEqualAndProgramCompletionDateIsLessThanEqualAndSchoolAtGradIdIn(from, to, schoolsInDistrict);
        //Get all optional programs for the same student set
        var gradStudentIDs = results.stream().map(GraduationStudentRecordEntity::getStudentID).toList();
        Map<UUID, List<StudentOptionalProgramPaginationLeanEntity>> studentOptionalProgramMap = new HashMap<>();

        for (int i = 0; i < gradStudentIDs.size(); i += 1000) {
            List<UUID> chunk = gradStudentIDs.subList(i, Math.min(i + 1000, gradStudentIDs.size()));
            var returnedOptionalProgs = studentOptionalProgramPaginationLeanRepository
                    .findAllByGraduationStudentRecordIDIn(chunk)
                    .stream()
                    .collect(Collectors.groupingBy(StudentOptionalProgramPaginationLeanEntity::getGraduationStudentRecordID));

            studentOptionalProgramMap.putAll(returnedOptionalProgs);
        }
        
        List<String> headers = Arrays.stream(YukonReportHeader.values()).map(YukonReportHeader::getCode).toList();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .build();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream), CSV_BUFFER_SIZE);
            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

            csvPrinter.printRecord(headers);
            for (GraduationStudentRecordEntity result : results) {
                var studentOptionalProgram = studentOptionalProgramMap.get(result.getStudentID()); 
                var school = restUtils.getSchoolBySchoolID(result.getSchoolAtGradId().toString()).orElseThrow(() -> new EntityNotFoundException(School.class, "schoolAtGradID", result.getSchoolAtGradId().toString()));
                List<String> csvRowData = prepareDataForCsv(result, school, optionalProgramCodes, studentOptionalProgram);
                csvPrinter.printRecord(csvRowData);
            }
            csvPrinter.flush();

            var downloadableReport = new DownloadableReportResponse();
            downloadableReport.setReportType("yukon-report");
            downloadableReport.setDocumentData(Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));

            return downloadableReport;
        } catch (IOException e) {
            throw new GradStudentAPIRuntimeException(e);
        }
    }

    private List<String> prepareDataForCsv(GraduationStudentRecordEntity student, School school, List<OptionalProgramCode> optionalProgramCodes, List<StudentOptionalProgramPaginationLeanEntity> studentOptionalPrograms) {
        var studentData = StringUtils.isNotBlank(student.getStudentGradData()) ? deriveStudentData(student) : null;
        return new ArrayList<>(Arrays.asList(
                school.getMincode(),
                school.getDisplayName(),
                studentData != null &&  studentData.getGradStudent() != null ? studentData.getGradStudent().getPen() : "",
                studentData != null &&  studentData.getGradStudent() != null ? studentData.getGradStudent().getLegalLastName() : "",
                studentData != null &&  studentData.getGradStudent() != null ? studentData.getGradStudent().getLegalFirstName() : "",
                studentData != null &&  studentData.getGradStudent() != null ? studentData.getGradStudent().getLegalMiddleNames() : "",
                student.getProgram(),
                getOptionalProgram(studentOptionalPrograms, student.getProgram(), optionalProgramCodes),
                student.getProgramCompletionDate() != null ? EducGradStudentApiUtils.formatDate(student.getProgramCompletionDate(), EducGradStudentApiConstants.YUKON_DATE_FORMAT) : ""
        ));
    }

    private String getOptionalProgram(List<StudentOptionalProgramPaginationLeanEntity> studentOptionalPrograms, String gradProgram, List<OptionalProgramCode> optionalProgramCodes) {
        if(studentOptionalPrograms == null){
            studentOptionalPrograms = new ArrayList<>();
        }
        var fiProgram = optionalProgramCalc(studentOptionalPrograms, gradProgram, optionalProgramCodes, OptionalProgramCodes.FI.getCode());
        var ddProgram = optionalProgramCalc(studentOptionalPrograms, gradProgram, optionalProgramCodes, OptionalProgramCodes.DD.getCode());
        var prog = StringUtils.isBlank(fiProgram) ? ddProgram : fiProgram;
        return  StringUtils.isNotBlank(fiProgram) && StringUtils.isNotBlank(ddProgram) ? fiProgram + "," + ddProgram : prog;
    }

    private GraduationData deriveStudentData(GraduationStudentRecordEntity studentRecord) {
        GraduationData graduationData;
        try {
            graduationData = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(studentRecord.getStudentGradData(), GraduationData.class);
        } catch (Exception e) {
            throw new GradStudentAPIRuntimeException(e);
        }
        return graduationData;
    }

    private String optionalProgramCalc(List<StudentOptionalProgramPaginationLeanEntity> studentOptionalPrograms, String gradProgram, List<OptionalProgramCode> optionalProgramCodes, String programCode) {
        var optProgram = getOptionalProgramCode(optionalProgramCodes, programCode,  gradProgram);

        if(optProgram.isPresent()) {
            var hasProgram = studentOptionalPrograms.stream()
                    .anyMatch(optProg -> Objects.equals(optProg.getOptionalProgramID(), optProgram.get().getOptionalProgramID()));

            return hasProgram ? optProgram.get().getOptionalProgramName() : "";
        }
       return "";
    }

    private Optional<OptionalProgramCode> getOptionalProgramCode(List<OptionalProgramCode> optionalProgramCodes, String incomingProgramCode, String gradProgram) {
        return  optionalProgramCodes
                .stream()
                .filter(program -> program.getOptProgramCode().equalsIgnoreCase(incomingProgramCode)
                        && StringUtils.isNotBlank(gradProgram)
                        && StringUtils.isNotBlank(program.getGraduationProgramCode())
                        && program.getGraduationProgramCode().equalsIgnoreCase(gradProgram)).findFirst();
    }

    /**
     * Generate course student search report and stream directly to HTTP response.
     *
     * @param searchCriteriaListJson JSON string with search criteria
     * @param response HTTP response to stream CSV to
     * @throws IOException if writing to response fails
     */
    public void generateCourseStudentSearchReportStream(String searchCriteriaListJson, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String whereClause = buildWhereClauseForReport(searchCriteriaListJson, objectMapper);

        List<String> headers = Arrays.stream(StudentCourseSearchReportHeader.values())
                .map(StudentCourseSearchReportHeader::getCode)
                .toList();

        response.setContentType(CONTENT_TYPE_CSV);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(CONTENT_DISPOSITION_HEADER, ATTACHMENT_FILENAME_PREFIX + "StudentCourseSearch-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD)) + CSV_FILE_EXTENSION);
        response.setBufferSize(CSV_BUFFER_SIZE);

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8), CSV_BUFFER_SIZE);
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
             Stream<CourseReport> courseReportStream = studentCoursePaginationRepository.streamForCourseReport(whereClause)) {

            csvPrinter.printRecord(headers);
            csvPrinter.flush();

            AtomicInteger rowCount = new AtomicInteger(0);
            AtomicBoolean clientDisconnected = new AtomicBoolean(false);

            log.debug("Starting course student search report stream processing");

            courseReportStream
                    .takeWhile(dto -> !clientDisconnected.get())
                    .forEach(courseDTO -> {
                        try {
                            List<String> csvRowData = prepareCourseReportDataForCsv(courseDTO);
                            csvPrinter.printRecord(csvRowData);
                            int count = rowCount.incrementAndGet();
                            if (count % CSV_FLUSH_INTERVAL == 0) {
                                csvPrinter.flush();
                            }
                        } catch (IOException e) {
                            log.debug("Client disconnected during course student search report at record {}. Stopping stream.", rowCount.get());
                            clientDisconnected.set(true);
                        }
                    });

            if (!clientDisconnected.get()) {
                csvPrinter.flush();
                log.debug("Successfully generated course student search report with {} rows", rowCount.get());
            } else {
                log.debug("Course student search report generation stopped at {} rows due to client disconnect", rowCount.get());
            }
        } catch (IOException e) {
            log.warn("Failed to start or complete course student search report generation: {}", e.getMessage());
        }
    }

    /**
     * Build WHERE clause from search criteria JSON for report queries.
     * Handles UUID columns with HEXTORAW conversion for Oracle.
     *
     * @param searchCriteriaListJson JSON string containing search criteria
     * @param objectMapper Jackson ObjectMapper for JSON parsing
     * @return WHERE clause string (without "WHERE" keyword) or empty string if no criteria
     */
    private String buildWhereClauseForReport(String searchCriteriaListJson, ObjectMapper objectMapper) {
        if (StringUtils.isBlank(searchCriteriaListJson)) {
            return "";
        }

        try {
            List<Search> searches = objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {});
            if (searches == null || searches.isEmpty()) {
                return "";
            }

            StringBuilder whereClause = new StringBuilder();
            boolean first = true;

            for (Search search : searches) {
                if (search.getSearchCriteriaList() == null || search.getSearchCriteriaList().isEmpty()) {
                    continue;
                }

                if (!first) {
                    whereClause.append(" ").append(search.getCondition() != null ? search.getCondition().toString() : "AND").append(" ");
                }

                whereClause.append("(");
                boolean firstCriteria = true;

                for (SearchCriteria criteria : search.getSearchCriteriaList()) {
                    if (!firstCriteria) {
                        whereClause.append(" ").append(criteria.getCondition() != null ? criteria.getCondition().toString() : "AND").append(" ");
                    }

                    String column = mapFieldToColumn(criteria.getKey());
                    String value = criteria.getValue();
                    String valueType = criteria.getValueType() != null ? criteria.getValueType().toString() : "STRING";

                    switch (criteria.getOperation()) {
                        case EQUAL:
                            if (value == null || value.isEmpty()) {
                                whereClause.append(column).append(" IS NULL");
                            } else if ("UUID".equals(valueType)) {
                                whereClause.append(column).append(" = HEXTORAW('").append(escapeSql(value.replace("-", ""))).append("')");
                            } else {
                                whereClause.append(column).append(" = '").append(escapeSql(value)).append("'");
                            }
                            break;
                        case NOT_EQUAL:
                            if (value == null || value.isEmpty()) {
                                whereClause.append(column).append(" IS NOT NULL");
                            } else if ("UUID".equals(valueType)) {
                                whereClause.append(column).append(" != HEXTORAW('").append(escapeSql(value.replace("-", ""))).append("')");
                            } else {
                                whereClause.append(column).append(" != '").append(escapeSql(value)).append("'");
                            }
                            break;
                        case CONTAINS:
                            whereClause.append(column).append(" LIKE '%").append(escapeSql(value)).append("%'");
                            break;
                        case STARTS_WITH:
                            whereClause.append(column).append(" LIKE '").append(escapeSql(value)).append("%'");
                            break;
                        case IN:
                            if ("UUID".equals(valueType)) {
                                String[] uuids = value.split(",");
                                whereClause.append(column).append(" IN (");
                                for (int i = 0; i < uuids.length; i++) {
                                    if (i > 0) whereClause.append(", ");
                                    whereClause.append("HEXTORAW('").append(escapeSql(uuids[i].trim().replace("-", ""))).append("')");
                                }
                                whereClause.append(")");
                            } else {
                                whereClause.append(column).append(" IN (").append(value).append(")");
                            }
                            break;
                        case GREATER_THAN:
                            whereClause.append(column).append(" > '").append(escapeSql(value)).append("'");
                            break;
                        case GREATER_THAN_OR_EQUAL_TO:
                            whereClause.append(column).append(" >= '").append(escapeSql(value)).append("'");
                            break;
                        case LESS_THAN:
                            whereClause.append(column).append(" < '").append(escapeSql(value)).append("'");
                            break;
                        case LESS_THAN_OR_EQUAL_TO:
                            whereClause.append(column).append(" <= '").append(escapeSql(value)).append("'");
                            break;
                        case DATE_RANGE:
                            if (value != null && value.contains(",")) {
                                String[] dates = value.split(",");
                                if (dates.length == 2) {
                                    whereClause.append(column)
                                            .append(" BETWEEN TO_TIMESTAMP('")
                                            .append(escapeSql(dates[0].trim()))
                                            .append("', 'YYYY-MM-DD\"T\"HH24:MI:SS') AND TO_TIMESTAMP('")
                                            .append(escapeSql(dates[1].trim()))
                                            .append("', 'YYYY-MM-DD\"T\"HH24:MI:SS')");
                                }
                            }
                            break;
                        default:
                            if (value == null || value.isEmpty()) {
                                whereClause.append(column).append(" IS NULL");
                            } else {
                                whereClause.append(column).append(" = '").append(escapeSql(value)).append("'");
                            }
                    }

                    firstCriteria = false;
                }

                whereClause.append(")");
                first = false;
            }

            return whereClause.toString();
        } catch (Exception e) {
            log.warn("Failed to parse search criteria for course report: {}", e.getMessage());
            return "";
        }
    }

    private String mapFieldToColumn(String fieldName) {
        if (fieldName.startsWith("graduationStudentRecordEntity.")) {
            fieldName = fieldName.substring("graduationStudentRecordEntity.".length());
        }

        // Map field names from SearchCriteria to database columns
        return switch (fieldName) {
            // Graduation Student Record fields (gsr)
            case "pen" -> "gsr.PEN";
            case "studentStatus", "studentStatusCode" -> "gsr.STUDENT_STATUS_CODE";
            case "legalLastName", "surname" -> "gsr.LEGAL_LAST_NAME";
            case "dob", "birthdate" -> "gsr.DOB";
            case "studentGrade", "grade" -> "gsr.STUDENT_GRADE";
            case "program", "graduationProgramCode" -> "gsr.GRADUATION_PROGRAM_CODE";
            case "programCompletionDate" -> "gsr.PROGRAM_COMPLETION_DATE";
            case "schoolOfRecord", "schoolOfRecordCode" -> "gsr.SCHOOL_OF_RECORD";
            case "schoolOfRecordId" -> "gsr.SCHOOL_OF_RECORD_ID";
            case "schoolAtGrad", "schoolAtGraduation", "schoolOfGraduationCode" -> "gsr.SCHOOL_AT_GRADUATION";
            case "schoolAtGradId", "schoolAtGraduationId" -> "gsr.SCHOOL_AT_GRADUATION_ID";

            // Student Course fields (sc)
            case "courseID", "courseId" -> "sc.COURSE_ID";
            case "courseSession" -> "sc.COURSE_SESSION";
            case "interimPercent" -> "sc.INTERIM_PERCENT";
            case "interimLetterGrade" -> "sc.INTERIM_LETTER_GRADE";
            case "finalPercent" -> "sc.FINAL_PERCENT";
            case "finalLetterGrade" -> "sc.FINAL_LETTER_GRADE";
            case "credits", "numberCredits" -> "sc.NUMBER_CREDITS";
            case "equivOrChallenge", "equivalentOrChallengeCode" -> "sc.EQUIVALENT_OR_CHALLENGE_CODE";
            case "fineArtsAppliedSkillsCode" -> "sc.FINE_ARTS_APPLIED_SKILLS_CODE";
            case "studentExamId", "studentCourseExamId" -> "sc.STUDENT_COURSE_EXAM_ID";

            // Student Optional Program fields (sop)
            case "optionalProgramID", "optionalProgramId" -> "sop.OPTIONAL_PROGRAM_ID";
            case "completionDate", "optionalProgramCompletionDate" -> "sop.COMPLETION_DATE";

            default -> fieldName; // Fallback to original field name
        };
    }

    private String escapeSql(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("'", "''");
    }

    private List<String> prepareCourseReportDataForCsv(CourseReport courseDTO) {
        String schoolOfRecordName = "";
        if (courseDTO.getSchoolOfRecordId() != null) {
            Optional<School> school = restUtils.getSchoolBySchoolID(courseDTO.getSchoolOfRecordId().toString());
            schoolOfRecordName = school.map(School::getDisplayName).orElse("");
        }

        String schoolOfGraduationName = "";
        if (courseDTO.getSchoolAtGraduationId() != null) {
            Optional<School> school = restUtils.getSchoolBySchoolID(courseDTO.getSchoolAtGraduationId().toString());
            schoolOfGraduationName = school.map(School::getDisplayName).orElse("");
        }

        String courseCode = "";
        String courseLevel = "";
        if (courseDTO.getCourseID() != null) {
            Optional<CourseCodeRecord> courseRecord = restUtils.getCoreg39CourseByID(courseDTO.getCourseID().toString());
            if (courseRecord.isPresent() && StringUtils.isNotBlank(courseRecord.get().getExternalCode())) {
                String externalCode = courseRecord.get().getExternalCode();
                courseCode = externalCode.substring(0, Math.min(5, externalCode.length())).trim();
                if (externalCode.length() > 5) {
                    courseLevel = externalCode.substring(5).trim();
                }
            }
        }

        String birthdate = courseDTO.getDob() != null
                ? courseDTO.getDob().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
        String completionDate = courseDTO.getProgramCompletionDate() != null
                ? courseDTO.getProgramCompletionDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";

        String equivChall = "";
        if (StringUtils.isNotBlank(courseDTO.getEquivOrChallenge())) {
            if (courseDTO.getEquivOrChallenge().startsWith("E")) {
                equivChall = "E";
            } else if (courseDTO.getEquivOrChallenge().startsWith("C")) {
                equivChall = "C";
            }
        }

        String fineArtsAppSkill = StringUtils.isNotBlank(courseDTO.getFineArtsAppliedSkillsCode())
                ? courseDTO.getFineArtsAppliedSkillsCode() : "";

        String hasExam = courseDTO.getStudentExamId() != null ? "Yes" : "No";

        return Arrays.asList(
                courseDTO.getPen() != null ? courseDTO.getPen() : "",
                courseDTO.getStudentStatus() != null ? getHumanReadableStudentStatus(courseDTO.getStudentStatus()) : "",
                courseDTO.getLegalLastName() != null ? courseDTO.getLegalLastName() : "",
                birthdate,
                courseDTO.getStudentGrade() != null ? courseDTO.getStudentGrade() : "",
                courseDTO.getProgram() != null ? courseDTO.getProgram() : "",
                completionDate,
                courseDTO.getSchoolOfRecord() != null ? courseDTO.getSchoolOfRecord() : "",
                schoolOfRecordName,
                courseDTO.getSchoolAtGraduation() != null ? courseDTO.getSchoolAtGraduation() : "",
                schoolOfGraduationName,
                courseCode,
                courseLevel,
                courseDTO.getCourseSession() != null ? courseDTO.getCourseSession() : "",
                courseDTO.getInterimPercent() != null ? courseDTO.getInterimPercent().toString() : "",
                courseDTO.getInterimLetterGrade() != null ? courseDTO.getInterimLetterGrade() : "",
                courseDTO.getFinalPercent() != null ? courseDTO.getFinalPercent().toString() : "",
                courseDTO.getFinalLetterGrade() != null ? courseDTO.getFinalLetterGrade() : "",
                courseDTO.getCredits() != null ? courseDTO.getCredits().toString() : "",
                equivChall,
                fineArtsAppSkill,
                hasExam
        );
    }

    private List<String> prepareCourseStudentSearchDataForCsv(StudentCoursePaginationEntity studentCourse) {
        var gradStudentRecord = studentCourse.getGraduationStudentRecordEntity();

        String schoolOfRecordCode = gradStudentRecord != null && gradStudentRecord.getSchoolOfRecord() != null
                ? gradStudentRecord.getSchoolOfRecord() : "";
        String schoolOfGraduationCode = gradStudentRecord != null && gradStudentRecord.getSchoolAtGraduation() != null
                ? gradStudentRecord.getSchoolAtGraduation() : "";

        String schoolOfRecordName = "";
        if (gradStudentRecord != null && gradStudentRecord.getSchoolOfRecordId() != null) {
            Optional<School> school = restUtils.getSchoolBySchoolID(gradStudentRecord.getSchoolOfRecordId().toString());
            schoolOfRecordName = school.map(School::getDisplayName).orElse("");
        }

        String schoolOfGraduationName = "";
        if (gradStudentRecord != null && gradStudentRecord.getSchoolAtGraduationId() != null) {
            Optional<School> school = restUtils.getSchoolBySchoolID(gradStudentRecord.getSchoolAtGraduationId().toString());
            schoolOfGraduationName = school.map(School::getDisplayName).orElse("");
        }

        // Get course info from coreg39 course cache
        String courseCode = "";
        String courseLevel = "";
        if (studentCourse.getCourseID() != null) {
            Optional<CourseCodeRecord> courseRecord = restUtils.getCoreg39CourseByID(studentCourse.getCourseID().toString());
            if (courseRecord.isPresent() && StringUtils.isNotBlank(courseRecord.get().getExternalCode())) {
                String externalCode = courseRecord.get().getExternalCode();
                courseCode = externalCode.substring(0, Math.min(5, externalCode.length())).trim();
                if (externalCode.length() > 5) {
                    courseLevel = externalCode.substring(5).trim();
                }
            }
        }

        // Format birthdate as yyyy-MM-dd
        String birthdate = "";
        if (gradStudentRecord != null && gradStudentRecord.getDob() != null) {
            birthdate = EducGradStudentApiUtils.formatDate(gradStudentRecord.getDob(), EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        }

        // Format completion date as yyyy-MM-dd
        String completionDate = "";
        if (gradStudentRecord != null && gradStudentRecord.getProgramCompletionDate() != null) {
            completionDate = EducGradStudentApiUtils.formatDate(gradStudentRecord.getProgramCompletionDate(), EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        }

        // Equiv. Chall. - E for Equivalency, C for Challenge
        String equivChall = "";
        if (StringUtils.isNotBlank(studentCourse.getEquivOrChallenge())) {
            if (studentCourse.getEquivOrChallenge().startsWith("E")) {
                equivChall = "E";
            } else if (studentCourse.getEquivOrChallenge().startsWith("C")) {
                equivChall = "C";
            }
        }

        String fineArtsAppSkill = StringUtils.isNotBlank(studentCourse.getFineArtsAppliedSkillsCode())
                ? studentCourse.getFineArtsAppliedSkillsCode()
                : "";

        // Has Exam? - Yes if there's a student exam ID, No otherwise
        String hasExam = studentCourse.getStudentExamId() != null ? "Yes" : "No";

        return Arrays.asList(
                gradStudentRecord != null && gradStudentRecord.getPen() != null ? gradStudentRecord.getPen() : "",
                gradStudentRecord != null && gradStudentRecord.getStudentStatus() != null ? getHumanReadableStudentStatus(gradStudentRecord.getStudentStatus()) : "",
                gradStudentRecord != null && gradStudentRecord.getLegalLastName() != null ? gradStudentRecord.getLegalLastName() : "",
                birthdate,
                gradStudentRecord != null && gradStudentRecord.getStudentGrade() != null ? gradStudentRecord.getStudentGrade() : "",
                gradStudentRecord != null && gradStudentRecord.getProgram() != null ? gradStudentRecord.getProgram() : "",
                completionDate,
                schoolOfRecordCode,
                schoolOfRecordName,
                schoolOfGraduationCode,
                schoolOfGraduationName,
                courseCode,
                courseLevel,
                studentCourse.getCourseSession() != null ? studentCourse.getCourseSession() : "",
                studentCourse.getInterimPercent() != null ? studentCourse.getInterimPercent().toString() : "",
                studentCourse.getInterimLetterGrade() != null ? studentCourse.getInterimLetterGrade() : "",
                studentCourse.getFinalPercent() != null ? studentCourse.getFinalPercent().toString() : "",
                studentCourse.getFinalLetterGrade() != null ? studentCourse.getFinalLetterGrade() : "",
                studentCourse.getCredits() != null ? studentCourse.getCredits().toString() : "",
                equivChall,
                fineArtsAppSkill,
                hasExam
        );
    }

    public void generateProgramStudentSearchReportStream(String searchCriteriaListJson, HttpServletResponse response) throws IOException {
        List<Sort.Order> sorts = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Specification<GradStudentSearchDataEntity> specs =
                gradStudentSearchService.setSpecificationAndSortCriteria("", searchCriteriaListJson, objectMapper, sorts);

        List<String> headers = Arrays.stream(StudentProgramSearchReportHeader.values())
                .map(StudentProgramSearchReportHeader::getCode)
                .toList();

        response.setContentType(CONTENT_TYPE_CSV);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(CONTENT_DISPOSITION_HEADER, ATTACHMENT_FILENAME_PREFIX + "StudentProgramSearch-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD)) + CSV_FILE_EXTENSION);
        response.setBufferSize(CSV_BUFFER_SIZE);

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8), CSV_BUFFER_SIZE);
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
             Stream<GradStudentSearchDataEntity> gradStudentStream = gradStudentSearchRepository.streamAll(specs)) {

            csvPrinter.printRecord(headers);
            csvPrinter.flush();

            AtomicInteger rowCount = new AtomicInteger(0);
            AtomicBoolean clientDisconnected = new AtomicBoolean(false);

            log.debug("Starting program student search report stream processing");

            gradStudentStream
                    .takeWhile(gs -> !clientDisconnected.get())
                    .forEach(gradStudent -> {
                        try {
                            List<String> csvRowData = prepareProgramStudentSearchDataForCsv(gradStudent);
                            csvPrinter.printRecord(csvRowData);
                            int count = rowCount.incrementAndGet();
                            if (count % CSV_FLUSH_INTERVAL == 0) {
                                csvPrinter.flush();
                            }
                        } catch (IOException e) {
                            log.debug("Client disconnected during program student search report at record {}. Stopping stream.", rowCount.get());
                            clientDisconnected.set(true);
                        }
                    });

            if (!clientDisconnected.get()) {
                csvPrinter.flush();
                log.debug("Successfully generated program student search report with {} rows", rowCount.get());
            } else {
                log.debug("Program student search report generation stopped at {} rows due to client disconnect", rowCount.get());
            }
        } catch (IOException e) {
            log.warn("Failed to start or complete program student search report generation: {}", e.getMessage());
        }
    }

    private List<String> prepareProgramStudentSearchDataForCsv(GradStudentSearchDataEntity gradStudent) {
        String schoolOfRecordCode = "";
        String schoolOfRecordName = "";
        if (gradStudent.getSchoolOfRecordId() != null) {
            Optional<School> school = restUtils.getSchoolBySchoolID(gradStudent.getSchoolOfRecordId().toString());
            if (school.isPresent()) {
                schoolOfRecordCode = school.get().getMincode() != null ? school.get().getMincode() : "";
                schoolOfRecordName = school.get().getDisplayName() != null ? school.get().getDisplayName() : "";
            }
        }

        String schoolOfGraduationCode = "";
        String schoolOfGraduationName = "";
        if (gradStudent.getSchoolAtGraduationId() != null) {
            Optional<School> school = restUtils.getSchoolBySchoolID(gradStudent.getSchoolAtGraduationId().toString());
            if (school.isPresent()) {
                schoolOfGraduationCode = school.get().getMincode() != null ? school.get().getMincode() : "";
                schoolOfGraduationName = school.get().getDisplayName() != null ? school.get().getDisplayName() : "";
            }
        }

        String birthdate = "";
        if (gradStudent.getDob() != null) {
            birthdate = gradStudent.getDob().format(DateTimeFormatter.ofPattern(EducGradStudentApiConstants.DEFAULT_DATE_FORMAT));
        }

        String completionDate = "";
        if (gradStudent.getProgramCompletionDate() != null) {
            completionDate = EducGradStudentApiUtils.formatDate(gradStudent.getProgramCompletionDate(), EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        }

        String adultStartDate = "";
        if (gradStudent.getAdultStartDate() != null) {
            adultStartDate = EducGradStudentApiUtils.formatDate(gradStudent.getAdultStartDate(), EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        }

        return Arrays.asList(
                gradStudent.getPen() != null ? gradStudent.getPen() : "",
                gradStudent.getStudentStatus() != null ? getHumanReadableStudentStatus(gradStudent.getStudentStatus()) : "",
                gradStudent.getLegalLastName() != null ? gradStudent.getLegalLastName() : "",
                gradStudent.getLegalFirstName() != null ? gradStudent.getLegalFirstName() : "",
                gradStudent.getLegalMiddleNames() != null ? gradStudent.getLegalMiddleNames() : "",
                birthdate,
                gradStudent.getStudentGrade() != null ? gradStudent.getStudentGrade() : "",
                gradStudent.getProgram() != null ? gradStudent.getProgram() : "",
                completionDate,
                schoolOfRecordCode,
                schoolOfRecordName,
                schoolOfGraduationCode,
                schoolOfGraduationName,
                adultStartDate
        );
    }

    /**
     * Generate CSV report for student optional program search using native SQL streaming.
     *
     * @param searchCriteriaListJson search criteria in JSON format
     * @param response HTTP response to write CSV to
     * @throws IOException if writing to response fails
     */
    public void generateOptionalProgramStudentSearchReportStream(String searchCriteriaListJson, HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String whereClause = buildWhereClauseForReport(searchCriteriaListJson, objectMapper);

        List<String> headers = Arrays.stream(StudentOptionalProgramSearchReportHeader.values())
                .map(StudentOptionalProgramSearchReportHeader::getCode)
                .toList();

        response.setContentType(CONTENT_TYPE_CSV);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(CONTENT_DISPOSITION_HEADER, ATTACHMENT_FILENAME_PREFIX + "StudentOptionalProgramSearch-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD)) + CSV_FILE_EXTENSION);
        response.setBufferSize(CSV_BUFFER_SIZE);

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8), CSV_BUFFER_SIZE);
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
             Stream<OptionalProgramReport> optionalProgramReportStream = studentOptionalProgramPaginationRepository.streamForOptionalProgramReport(whereClause)) {

            csvPrinter.printRecord(headers);
            csvPrinter.flush();

            List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();

            AtomicInteger rowCount = new AtomicInteger(0);
            AtomicBoolean clientDisconnected = new AtomicBoolean(false);

            log.debug("Starting optional program student search report stream processing");

            optionalProgramReportStream
                    .takeWhile(op -> !clientDisconnected.get())
                    .forEach(optionalProgramDTO -> {
                        try {
                            List<String> csvRowData = prepareOptionalProgramReportDataForCsv(optionalProgramDTO, optionalProgramCodes);
                            csvPrinter.printRecord(csvRowData);
                            int count = rowCount.incrementAndGet();

                            if (count % CSV_FLUSH_INTERVAL == 0) {
                                csvPrinter.flush();
                            }
                        } catch (IOException e) {
                            log.debug("Client disconnected during optional program student search report at record {}. Stopping stream.", rowCount.get());
                            clientDisconnected.set(true);
                        }
                    });

            if (!clientDisconnected.get()) {
                csvPrinter.flush();
                log.debug("Successfully generated optional program student search report with {} rows", rowCount.get());
            } else {
                log.debug("Optional program student search report generation stopped at {} rows due to client disconnect", rowCount.get());
            }
        } catch (IOException e) {
            log.warn("Failed to start or complete optional program student search report generation: {}", e.getMessage());
        }
    }

    private List<String> prepareOptionalProgramReportDataForCsv(
            OptionalProgramReport optionalProgramDTO,
            List<OptionalProgramCode> optionalProgramCodes) {

        String schoolOfRecordName = "";
        if (optionalProgramDTO.getSchoolOfRecordId() != null) {
            Optional<School> school = restUtils.getSchoolBySchoolID(optionalProgramDTO.getSchoolOfRecordId().toString());
            schoolOfRecordName = school.map(School::getDisplayName).orElse("");
        }

        String schoolOfGraduationName = "";
        if (optionalProgramDTO.getSchoolAtGraduationId() != null) {
            Optional<School> school = restUtils.getSchoolBySchoolID(optionalProgramDTO.getSchoolAtGraduationId().toString());
            schoolOfGraduationName = school.map(School::getDisplayName).orElse("");
        }

        String optionalProgramName = "";
        if (optionalProgramDTO.getOptionalProgramId() != null) {
            optionalProgramName = optionalProgramCodes.stream()
                    .filter(op -> op.getOptionalProgramID().equals(optionalProgramDTO.getOptionalProgramId()))
                    .findFirst()
                    .map(OptionalProgramCode::getOptionalProgramName)
                    .orElse("");
        }

        String birthdate = optionalProgramDTO.getDob() != null
                ? optionalProgramDTO.getDob().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
        String completionDate = optionalProgramDTO.getProgramCompletionDate() != null
                ? optionalProgramDTO.getProgramCompletionDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";
        String optionalProgramCompletionDate = optionalProgramDTO.getOptionalProgramCompletionDate() != null
                ? optionalProgramDTO.getOptionalProgramCompletionDate().format(DateTimeFormatter.ISO_LOCAL_DATE) : "";

        return Arrays.asList(
                optionalProgramDTO.getPen() != null ? optionalProgramDTO.getPen() : "",
                getHumanReadableStudentStatus(optionalProgramDTO.getStudentStatus()),
                optionalProgramDTO.getLegalLastName() != null ? optionalProgramDTO.getLegalLastName() : "",
                optionalProgramDTO.getLegalFirstName() != null ? optionalProgramDTO.getLegalFirstName() : "",
                optionalProgramDTO.getLegalMiddleNames() != null ? optionalProgramDTO.getLegalMiddleNames() : "",
                birthdate,
                optionalProgramDTO.getStudentGrade() != null ? optionalProgramDTO.getStudentGrade() : "",
                optionalProgramDTO.getProgram() != null ? optionalProgramDTO.getProgram() : "",
                completionDate,
                optionalProgramDTO.getSchoolOfRecord() != null ? optionalProgramDTO.getSchoolOfRecord() : "",
                schoolOfRecordName,
                optionalProgramDTO.getSchoolAtGraduation() != null ? optionalProgramDTO.getSchoolAtGraduation() : "",
                schoolOfGraduationName,
                optionalProgramName,
                optionalProgramCompletionDate
        );
    }

    private List<String> prepareOptionalProgramStudentSearchDataForCsv(StudentOptionalProgramPaginationEntity studentOptionalProgram, List<OptionalProgramCode> optionalProgramCodes) {
        var gradStudentRecord = studentOptionalProgram.getGraduationStudentRecordEntity();

        String pen = gradStudentRecord != null && gradStudentRecord.getPen() != null
                ? gradStudentRecord.getPen() : "";

        String studentStatus = gradStudentRecord != null
                ? getHumanReadableStudentStatus(gradStudentRecord.getStudentStatus()) : "";

        String surname = gradStudentRecord != null && gradStudentRecord.getLegalLastName() != null
                ? gradStudentRecord.getLegalLastName() : "";

        String givenName = gradStudentRecord != null && gradStudentRecord.getLegalFirstName() != null
                ? gradStudentRecord.getLegalFirstName() : "";

        String middleName = gradStudentRecord != null && gradStudentRecord.getLegalMiddleNames() != null
                ? gradStudentRecord.getLegalMiddleNames() : "";

        String birthdate = "";
        if (gradStudentRecord != null && gradStudentRecord.getDob() != null) {
            birthdate = EducGradStudentApiUtils.formatDate(gradStudentRecord.getDob(), EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        }

        String grade = gradStudentRecord != null && gradStudentRecord.getStudentGrade() != null
                ? gradStudentRecord.getStudentGrade() : "";

        String program = gradStudentRecord != null && gradStudentRecord.getProgram() != null
                ? gradStudentRecord.getProgram() : "";

        String completionDate = "";
        if (gradStudentRecord != null && gradStudentRecord.getProgramCompletionDate() != null) {
            completionDate = EducGradStudentApiUtils.formatDate(gradStudentRecord.getProgramCompletionDate(), EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        }

        String schoolOfRecordCode = gradStudentRecord != null && gradStudentRecord.getSchoolOfRecord() != null
                ? gradStudentRecord.getSchoolOfRecord() : "";

        String schoolOfGraduationCode = gradStudentRecord != null && gradStudentRecord.getSchoolAtGraduation() != null
                ? gradStudentRecord.getSchoolAtGraduation() : "";

        String schoolOfRecordName = "";
        if (gradStudentRecord != null && gradStudentRecord.getSchoolOfRecordId() != null) {
            Optional<School> school = restUtils.getSchoolBySchoolID(gradStudentRecord.getSchoolOfRecordId().toString());
            schoolOfRecordName = school.map(School::getDisplayName).orElse("");
        }

        String schoolOfGraduationName = "";
        if (gradStudentRecord != null && gradStudentRecord.getSchoolAtGraduationId() != null) {
            Optional<School> school = restUtils.getSchoolBySchoolID(gradStudentRecord.getSchoolAtGraduationId().toString());
            schoolOfGraduationName = school.map(School::getDisplayName).orElse("");
        }

        String optionalProgramName = "";
        if (studentOptionalProgram.getOptionalProgramID() != null) {
            optionalProgramName = optionalProgramCodes.stream()
                    .filter(op -> op.getOptionalProgramID().equals(studentOptionalProgram.getOptionalProgramID()))
                    .findFirst()
                    .map(OptionalProgramCode::getOptionalProgramName)
                    .orElse("");
        }

        String optionalProgramCompletionDate = "";
        if (studentOptionalProgram.getCompletionDate() != null) {
            optionalProgramCompletionDate = EducGradStudentApiUtils.formatDate(studentOptionalProgram.getCompletionDate(), EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        }

        return Arrays.asList(
                pen,
                studentStatus,
                surname,
                givenName,
                middleName,
                birthdate,
                grade,
                program,
                completionDate,
                schoolOfRecordCode,
                schoolOfRecordName,
                schoolOfGraduationCode,
                schoolOfGraduationName,
                optionalProgramName,
                optionalProgramCompletionDate
        );
    }

    /**
     * Generate CSV report for student search results
     *
     * @param searchCriteriaListJson search criteria in JSON format
     * @param response HTTP response to write CSV to
     * @throws IOException if writing to response fails
     */
    public void generateStudentSearchReportStream(String searchCriteriaListJson, HttpServletResponse response) throws IOException {
        List<Sort.Order> sorts = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Specification<GradStudentSearchDataEntity> specs =
                gradStudentSearchService.setSpecificationAndSortCriteria("", searchCriteriaListJson, objectMapper, sorts);

        List<String> headers = Arrays.stream(StudentSearchReportHeader.values())
                .map(StudentSearchReportHeader::getCode)
                .toList();

        response.setContentType(CONTENT_TYPE_CSV);
        response.setCharacterEncoding("UTF-8");
        response.setHeader(CONTENT_DISPOSITION_HEADER, ATTACHMENT_FILENAME_PREFIX + "StudentSearch-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT_YYYYMMDD)) + CSV_FILE_EXTENSION);
        response.setBufferSize(CSV_BUFFER_SIZE);

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream(), StandardCharsets.UTF_8), CSV_BUFFER_SIZE);
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
             Stream<GradStudentSearchDataEntity> gradStudentStream = gradStudentSearchRepository.streamAll(specs)) {

            csvPrinter.printRecord(headers);
            csvPrinter.flush();

            AtomicInteger rowCount = new AtomicInteger(0);
            AtomicBoolean clientDisconnected = new AtomicBoolean(false);

            log.debug("Starting student search report stream processing");

            gradStudentStream
                    .takeWhile(gs -> !clientDisconnected.get())
                    .forEach(gradStudent -> {
                        try {
                            List<String> csvRowData = prepareStudentSearchDataForCsv(gradStudent);
                            csvPrinter.printRecord(csvRowData);
                            int count = rowCount.incrementAndGet();
                            if (count % CSV_FLUSH_INTERVAL == 0) {
                                csvPrinter.flush();
                            }
                        } catch (IOException e) {
                            log.debug("Client disconnected during student search report at record {}. Stopping stream.", rowCount.get());
                            clientDisconnected.set(true);
                        }
                    });

            if (!clientDisconnected.get()) {
                csvPrinter.flush();
                log.debug("Successfully generated student search report with {} rows", rowCount.get());
            } else {
                log.debug("Student search report generation stopped at {} rows due to client disconnect", rowCount.get());
            }
        } catch (IOException e) {
            log.warn("Failed to start or complete student search report generation: {}", e.getMessage());
        }
    }

    private List<String> prepareStudentSearchDataForCsv(GradStudentSearchDataEntity gradStudent) {
        String pen = StringUtils.defaultString(gradStudent.getPen());
        String studentStatus = getHumanReadableStudentStatus(gradStudent.getStudentStatus());
        String surname = StringUtils.defaultString(gradStudent.getLegalLastName());
        String givenName = StringUtils.defaultString(gradStudent.getLegalFirstName());
        String middleName = StringUtils.defaultString(gradStudent.getLegalMiddleNames());
        String birthdate = gradStudent.getDob() != null ? gradStudent.getDob().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "";
        String gender = StringUtils.defaultString(gradStudent.getGenderCode());
        String grade = StringUtils.defaultString(gradStudent.getStudentGrade());
        String program = StringUtils.defaultString(gradStudent.getProgram());
        String completionDate = gradStudent.getProgramCompletionDate() != null ?
                new SimpleDateFormat("yyyy/MM").format(gradStudent.getProgramCompletionDate()) : "";

        String schoolOfRecordCode = "";
        String schoolOfRecordName = "";
        if (gradStudent.getSchoolOfRecordId() != null) {
            Optional<School> school = restUtils.getSchoolBySchoolID(gradStudent.getSchoolOfRecordId().toString());
            if (school.isPresent()) {
                schoolOfRecordCode = StringUtils.defaultString(school.get().getMincode());
                schoolOfRecordName = StringUtils.defaultString(school.get().getDisplayName());
            }
        }

        String recalculateGradStatus = StringUtils.defaultString(gradStudent.getRecalculateGradStatus());
        String recalculateProjectedGrad = StringUtils.defaultString(gradStudent.getRecalculateProjectedGrad());

        return List.of(
                pen,
                studentStatus,
                surname,
                givenName,
                middleName,
                birthdate,
                gender,
                grade,
                program,
                completionDate,
                schoolOfRecordCode,
                schoolOfRecordName,
                recalculateGradStatus,
                recalculateProjectedGrad
        );
    }

    private String getHumanReadableStudentStatus(String statusCode) {
        if (statusCode == null) {
            return "";
        }
        return switch (statusCode.toUpperCase()) {
            case "CUR" -> "Current";
            case "ARC" -> "Archived";
            case "DEC" -> "Deceased";
            case "MER" -> "Merged";
            case "TER" -> "Terminated";
            default -> statusCode;
        };
    }
}
