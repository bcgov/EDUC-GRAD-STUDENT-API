package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.OptionalProgramCodes;
import ca.bc.gov.educ.api.gradstudent.constant.v1.StudentCourseSearchReportHeader;
import ca.bc.gov.educ.api.gradstudent.constant.v1.StudentOptionalProgramSearchReportHeader;
import ca.bc.gov.educ.api.gradstudent.constant.v1.StudentProgramSearchReportHeader;
import ca.bc.gov.educ.api.gradstudent.constant.v1.YukonReportHeader;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.exception.GradStudentAPIRuntimeException;
import ca.bc.gov.educ.api.gradstudent.model.dto.DownloadableReportResponse;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationData;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CourseCodeRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.OptionalProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.District;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
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
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class CSVReportService {

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
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
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
        List<Sort.Order> sorts = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Specification<StudentCoursePaginationEntity> specs =
                studentCoursePaginationService.setSpecificationAndSortCriteria("", searchCriteriaListJson, objectMapper, sorts);

        List<String> headers = Arrays.stream(StudentCourseSearchReportHeader.values())
                .map(StudentCourseSearchReportHeader::getCode)
                .toList();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"StudentCourseSearch-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv\"");

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
             Stream<StudentCoursePaginationEntity> studentCourseStream = studentCoursePaginationRepository.streamAll(specs)) {

            csvPrinter.printRecord(headers);

            studentCourseStream
                    .map(this::prepareCourseStudentSearchDataForCsv)
                    .forEach(csvRowData -> {
                        try {
                            csvPrinter.printRecord(csvRowData);
                            csvPrinter.flush();
                        } catch (IOException e) {
                            throw new GradStudentAPIRuntimeException(e);
                        }
                    });

            csvPrinter.flush();
        }
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
                int codeLength = Math.min(5, externalCode.length());
                courseCode = externalCode.substring(0, codeLength).trim();
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

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"StudentProgramSearch-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv\"");

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
             Stream<GradStudentSearchDataEntity> gradStudentStream = gradStudentSearchRepository.streamAll(specs)) {

            csvPrinter.printRecord(headers);

            gradStudentStream
                    .map(this::prepareProgramStudentSearchDataForCsv)
                    .forEach(csvRowData -> {
                        try {
                            csvPrinter.printRecord(csvRowData);
                            csvPrinter.flush();
                        } catch (IOException e) {
                            throw new GradStudentAPIRuntimeException(e);
                        }
                    });

            csvPrinter.flush();
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

        String recalculateGradStatus = "Y".equalsIgnoreCase(gradStudent.getRecalculateGradStatus()) ? "Yes" : "No";
        String recalculateProjectedGrad = "Y".equalsIgnoreCase(gradStudent.getRecalculateProjectedGrad()) ? "Yes" : "No";

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
                adultStartDate,
                recalculateGradStatus,
                recalculateProjectedGrad
        );
    }

    /**
     * Generate CSV report for student optional program search
     *
     * @param searchCriteriaListJson search criteria in JSON format
     * @param response HTTP response to write CSV to
     * @throws IOException if writing to response fails
     */
    public void generateOptionalProgramStudentSearchReportStream(String searchCriteriaListJson, HttpServletResponse response) throws IOException {
        List<Sort.Order> sorts = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Specification<StudentOptionalProgramPaginationEntity> specs =
                studentOptionalProgramPaginationService.setSpecificationAndSortCriteria("", searchCriteriaListJson, objectMapper, sorts);

        List<String> headers = Arrays.stream(StudentOptionalProgramSearchReportHeader.values())
                .map(StudentOptionalProgramSearchReportHeader::getCode)
                .toList();

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"StudentOptionalProgramSearch-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv\"");

        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().build();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream()));
             CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);
             Stream<StudentOptionalProgramPaginationEntity> studentOptionalProgramStream = studentOptionalProgramPaginationRepository.streamAll(specs)) {

            csvPrinter.printRecord(headers);

            studentOptionalProgramStream
                    .map(this::prepareOptionalProgramStudentSearchDataForCsv)
                    .forEach(csvRowData -> {
                        try {
                            csvPrinter.printRecord(csvRowData);
                            csvPrinter.flush();
                        } catch (IOException e) {
                            throw new GradStudentAPIRuntimeException(e);
                        }
                    });

            csvPrinter.flush();
        }
    }

    private List<String> prepareOptionalProgramStudentSearchDataForCsv(StudentOptionalProgramPaginationEntity studentOptionalProgram) {
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

        // Get optional program name
        String optionalProgramName = "";
        if (studentOptionalProgram.getOptionalProgramID() != null) {
            List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();
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
