package ca.bc.gov.educ.api.gradstudent.service.event;

import ca.bc.gov.educ.api.gradstudent.constant.GradRequirementYearCodes;
import ca.bc.gov.educ.api.gradstudent.constant.OptionalProgramCodes;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationData;
import ca.bc.gov.educ.api.gradstudent.model.dto.LetterGrade;
import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.CourseStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.CourseStudentDetail;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.DemographicStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.GraduationProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.OptionalProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import ca.bc.gov.educ.api.gradstudent.service.CourseCacheService;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.service.HistoryService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GraduationStudentRecordService {
    private final RestUtils restUtils;
    private final CourseCacheService courseCacheService;
    private final GraduationStudentRecordRepository graduationStudentRecordRepository;
    private final StudentOptionalProgramRepository studentOptionalProgramRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final FineArtsAppliedSkillsCodeRepository fineArtsAppliedSkillsCodeRepository;
    private final EquivalentOrChallengeCodeRepository equivalentOrChallengeCodeRepository;
    private static final String DATA_CONVERSION_HISTORY_ACTIVITY_CODE = "DATACONVERT"; // confirm,
    private static final String ADD_ONGOING_HISTORY_ACTIVITY_CODE = "TRAXADD";// confirm,
    private final HistoryService historyService;
    private final GraduationStatusService graduationStatusService;
    public static final String CURRENT = "CUR";
    public static final String TERMINATED = "TER";
    public static final String DECEASED = "DEC";
    public static final String CREATE_USER = "createUser";
    public static final String CREATE_DATE = "createDate";
    public static final String YYYY_MM_DD = "uuuuMMdd";
    public static final String EN_1996_CODE = "1996-EN";
    public final List<String> fral10Programs = Arrays.asList("2023-EN", "2018-EN", "2004-EN");
    public final List<String> fral11Programs = Arrays.asList(EN_1996_CODE, "1986-EN");
    private static final Logger logger = LoggerFactory.getLogger(GraduationStudentRecordService.class);

    @Transactional
    public Student getStudentByPenFromStudentAPI(String pen) {
        return restUtils.getStudentByPEN(UUID.randomUUID(), pen);
    }

    @Transactional
    public Optional<GraduationStudentRecordEntity> getStudentByStudentID(String studentID) {
        return graduationStudentRecordRepository.findOptionalByStudentID(UUID.fromString(studentID));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void handleAssessmentUpdateEvent(GraduationStudentRecordEntity existingStudentRecordEntity, final GradStatusEvent event) {
        var newStudentRecordEntity = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(existingStudentRecordEntity, newStudentRecordEntity, CREATE_USER, CREATE_DATE);

        newStudentRecordEntity.setUpdateUser(event.getUpdateUser());
        newStudentRecordEntity.setUpdateDate(LocalDateTime.now());
        newStudentRecordEntity.setRecalculateProjectedGrad("Y");
        newStudentRecordEntity.setRecalculateGradStatus("Y");
        graduationStudentRecordRepository.save(newStudentRecordEntity);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void handleAssessmentAdoptEvent(String studentID, final GradStatusEvent event) {
        graduationStatusService.adoptStudent(UUID.fromString(studentID), event.getUpdateUser());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void createNewStudentRecord(DemographicStudent demStudent, Student studentFromApi) {
        List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();
        List<StudentOptionalProgramEntity> optionalProgramEntities = new ArrayList<>();
        GraduationStudentRecordEntity entity = createGraduationStudentRecordEntity(demStudent, studentFromApi);
        if(StringUtils.isNotBlank(demStudent.getGradRequirementYear()) && demStudent.getIsSummerCollection().equalsIgnoreCase("N")) {
            entity.setProgram(mapGradProgramCode(demStudent.getGradRequirementYear(), demStudent.getSchoolReportingRequirementCode()));
        } else {
            entity.setProgram(createProgram());
        }

        entity.setCreateUser(demStudent.getCreateUser());
        entity.setUpdateUser(demStudent.getUpdateUser());
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        var savedStudentRecord = graduationStudentRecordRepository.save(entity);
        historyService.createStudentHistory(savedStudentRecord, ADD_ONGOING_HISTORY_ACTIVITY_CODE);

        List<UUID> incomingProgramIDs = getOptionalProgramIDForIncomingPrograms(demStudent, optionalProgramCodes);
        incomingProgramIDs.forEach(programID -> optionalProgramEntities.add(createStudentOptionalProgramEntity(programID, savedStudentRecord.getStudentID(), demStudent.getCreateUser(), demStudent.getUpdateUser())));

        if(StringUtils.isNotBlank(savedStudentRecord.getProgram()) && savedStudentRecord.getProgram().equalsIgnoreCase("SCCP") && savedStudentRecord.getProgramCompletionDate() != null && demStudent.getSchoolReportingRequirementCode().equalsIgnoreCase("CSF")) {
            var frProgram = getOptionalProgramCode(optionalProgramCodes, "FR");
            frProgram.ifPresent(optionalProgramCode -> optionalProgramEntities.add(createStudentOptionalProgramEntity(optionalProgramCode.getOptionalProgramID(), savedStudentRecord.getStudentID(), demStudent.getCreateUser(), demStudent.getUpdateUser())));
        }
        var savedEntities = studentOptionalProgramRepository.saveAll(optionalProgramEntities);
        savedEntities.forEach(optEntity -> historyService.createStudentOptionalProgramHistory(optEntity, DATA_CONVERSION_HISTORY_ACTIVITY_CODE));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean updateStudentRecord(DemographicStudent demStudent, Student studentFromApi, GraduationStudentRecordEntity existingStudentRecordEntity) {
        var newStudentRecordEntity = new GraduationStudentRecordEntity();
        boolean isSchoolOfRecordUpdated = checkIfSchoolOfRecordIsUpdated(demStudent, existingStudentRecordEntity);
        BeanUtils.copyProperties(existingStudentRecordEntity, newStudentRecordEntity, CREATE_USER, CREATE_DATE);
        GraduationStudentRecordEntity updatedEntity = compareAndUpdateGraduationStudentRecordEntity(demStudent, newStudentRecordEntity);
        updatedEntity.setUpdateUser(demStudent.getUpdateUser());
        updatedEntity.setUpdateDate(LocalDateTime.now());
        var savedStudentRecord = graduationStudentRecordRepository.save(updatedEntity);

        List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();
        List<UUID> incomingProgramIDs = getOptionalProgramIDForIncomingPrograms(demStudent, optionalProgramCodes);
        if (StringUtils.endsWithIgnoreCase(savedStudentRecord.getProgram(), "-PF")) {
            getOptionalProgramCode(optionalProgramCodes, OptionalProgramCodes.DD.getCode())
                    .map(OptionalProgramCode::getOptionalProgramID)
                    .filter(ddId -> !incomingProgramIDs.contains(ddId))
                    .ifPresent(incomingProgramIDs::add);
        }
        boolean isGraduated = deriveIfGraduated(savedStudentRecord);
        var optionalProgramsToRemove = getOptionalProgramForRemoval(UUID.fromString(studentFromApi.getStudentID()), incomingProgramIDs, optionalProgramCodes, isGraduated);
        if(!optionalProgramsToRemove.isEmpty()) {
            studentOptionalProgramRepository.deleteAll(optionalProgramsToRemove);
        }

        List<UUID> programIDsToAdd = getOptionalProgramToAdd(UUID.fromString(studentFromApi.getStudentID()), incomingProgramIDs);

        List<StudentOptionalProgramEntity> optionalProgramEntities = new ArrayList<>();
        programIDsToAdd.forEach(programID -> optionalProgramEntities.add(createStudentOptionalProgramEntity(programID, savedStudentRecord.getStudentID(), demStudent.getCreateUser(), demStudent.getUpdateUser())));
        var savedEntities = studentOptionalProgramRepository.saveAll(optionalProgramEntities);
        savedEntities.forEach(optEntity -> historyService.createStudentOptionalProgramHistory(optEntity, DATA_CONVERSION_HISTORY_ACTIVITY_CODE));

        if (!optionalProgramsToRemove.isEmpty() || !programIDsToAdd.isEmpty()) {
            boolean projAlreadyY = "Y".equalsIgnoreCase(savedStudentRecord.getRecalculateProjectedGrad());
            boolean statusAlreadyY = "Y".equalsIgnoreCase(savedStudentRecord.getRecalculateGradStatus());
            if (!projAlreadyY || !statusAlreadyY) {
                savedStudentRecord.setRecalculateProjectedGrad("Y");
                savedStudentRecord.setRecalculateGradStatus("Y");
                graduationStudentRecordRepository.save(savedStudentRecord);
            }
        }

        return isSchoolOfRecordUpdated;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void handleStudentCourseRecord(GraduationStudentRecordEntity existingStudentRecordEntity, CourseStudent courseStudent, Student studentFromApi) {
        boolean hasGraduated = existingStudentRecordEntity.getProgramCompletionDate() != null;
        if(courseStudent.getSubmissionModeCode().equalsIgnoreCase("APPEND") || courseStudent.getIsSummerCollection().equalsIgnoreCase("Y") || hasGraduated) {
            courseStudent.getStudentDetails().forEach(student -> handleAppendCourseRecord(existingStudentRecordEntity, student, studentFromApi.getStudentID()));
        } else {
            List<StudentCourseEntity> existingStudentCourses =  studentCourseRepository.findByStudentIDAndCourseExamIsNull(UUID.fromString(studentFromApi.getStudentID()));
            if(!existingStudentCourses.isEmpty()) {
                studentCourseRepository.deleteAllInBatch(existingStudentCourses);
            }
            courseStudent.getStudentDetails().forEach(student -> handleReplaceCourseRecord(existingStudentRecordEntity, student,  studentFromApi.getStudentID()));
        }
    }

    private void handleReplaceCourseRecord(GraduationStudentRecordEntity existingStudentRecordEntity, CourseStudentDetail courseStudent, String studentID) {
        var coursesRecord = getCoregCoursesRecord(courseStudent.getCourseCode(), courseStudent.getCourseLevel());
        createNewStudentCourseEntity(courseStudent, studentID, coursesRecord, existingStudentRecordEntity);
        existingStudentRecordEntity.setRecalculateProjectedGrad("Y");
        existingStudentRecordEntity.setRecalculateGradStatus("Y");
        graduationStudentRecordRepository.save(existingStudentRecordEntity);
    }

    private void handleAppendCourseRecord(GraduationStudentRecordEntity existingStudentRecordEntity, CourseStudentDetail courseStudent, String studentID) {
        List<StudentCourseEntity> existingStudentCourses =  studentCourseRepository.findByStudentID(UUID.fromString(studentID));
        var coursesRecord = getCoregCoursesRecord(courseStudent.getCourseCode(), courseStudent.getCourseLevel());
        var matchingCourseRecord = existingStudentCourses.stream().filter(course -> Objects.equals(course.getCourseID(), new BigInteger(coursesRecord.getCourseID())) && course.getCourseSession().equalsIgnoreCase(courseStudent.getCourseYear() + courseStudent.getCourseMonth())).findFirst();
        if(matchingCourseRecord.isPresent() && courseStudent.getCourseStatus().equalsIgnoreCase("W")) {
            studentCourseRepository.delete(matchingCourseRecord.get());
        } else if(matchingCourseRecord.isPresent() && courseStudent.getCourseStatus().equalsIgnoreCase("A")) {
            var newStudentCourseEntity = new StudentCourseEntity();
            BeanUtils.copyProperties(matchingCourseRecord.get(), newStudentCourseEntity, CREATE_USER, CREATE_DATE);
            StudentCourseEntity updatedEntity = compareAndUpdateStudentCourseEntity(newStudentCourseEntity, courseStudent, coursesRecord, existingStudentRecordEntity);
            updatedEntity.setCreateUser(courseStudent.getCreateUser());
            updatedEntity.setUpdateUser(courseStudent.getUpdateUser());
            updatedEntity.setCreateDate(LocalDateTime.now());
            updatedEntity.setUpdateDate(LocalDateTime.now());
            studentCourseRepository.save(updatedEntity);
        } else {
            createNewStudentCourseEntity(courseStudent, studentID, coursesRecord, existingStudentRecordEntity);
        }

        existingStudentRecordEntity.setRecalculateProjectedGrad("Y");
        existingStudentRecordEntity.setRecalculateGradStatus("Y");
        graduationStudentRecordRepository.save(existingStudentRecordEntity);
    }

    private void createNewStudentCourseEntity(CourseStudentDetail courseStudent, String studentID, CoregCoursesRecord coursesRecord, GraduationStudentRecordEntity existingStudentRecordEntity) {
        StudentCourseEntity studentCourseEntity = createStudentCourseEntity(courseStudent, studentID, coursesRecord, existingStudentRecordEntity);
        studentCourseEntity.setCreateUser(courseStudent.getCreateUser());
        studentCourseEntity.setUpdateUser(courseStudent.getUpdateUser());
        studentCourseEntity.setCreateDate(LocalDateTime.now());
        studentCourseEntity.setUpdateDate(LocalDateTime.now());
        studentCourseRepository.save(studentCourseEntity);
        updateOptionalPrograms(existingStudentRecordEntity, courseStudent);
    }

    private void updateOptionalPrograms(GraduationStudentRecordEntity existingStudentRecordEntity, CourseStudentDetail courseStudent) {
        String course = StringUtils.isEmpty(courseStudent.getCourseLevel()) ? courseStudent.getCourseCode() : String.format("%-5s", courseStudent.getCourseCode()) + courseStudent.getCourseLevel();
        boolean isFRAL10 = (course.equalsIgnoreCase("FRAL 10") || course.equalsIgnoreCase("FRALP 10")) && StringUtils.isNotBlank(existingStudentRecordEntity.getProgram()) && fral10Programs.contains(existingStudentRecordEntity.getProgram());
        boolean isFRAL11 = course.equalsIgnoreCase("FRAL 11") && StringUtils.isNotBlank(existingStudentRecordEntity.getProgram()) && fral11Programs.contains(existingStudentRecordEntity.getProgram());
        
        if(isFRAL10 || isFRAL11 || course.equalsIgnoreCase("FRALP 11") && StringUtils.isNotBlank(existingStudentRecordEntity.getProgram()) && existingStudentRecordEntity.getProgram().equalsIgnoreCase(EN_1996_CODE)) {
            List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();
            var frProgram = getOptionalProgramCode(optionalProgramCodes, "FR");
            if(frProgram.isPresent() && !hasFrenchProgram(existingStudentRecordEntity.getStudentID(), frProgram.get().getOptionalProgramID())) {
                var entity = createStudentOptionalProgramEntity(frProgram.get().getOptionalProgramID(), existingStudentRecordEntity.getStudentID(), courseStudent.getCreateUser(), courseStudent.getUpdateUser());
                var savedEntity = studentOptionalProgramRepository.save(entity);
                historyService.createStudentOptionalProgramHistory(savedEntity, DATA_CONVERSION_HISTORY_ACTIVITY_CODE);
            }
        }
    }
    
    private boolean hasFrenchProgram(UUID studentID, UUID optionalProgramID) {
        var programs = studentOptionalProgramRepository.findByStudentID(studentID);
        return programs.stream().anyMatch(program -> program.getOptionalProgramID().equals(optionalProgramID));
    }

    private StudentCourseEntity compareAndUpdateStudentCourseEntity(StudentCourseEntity newStudentCourseEntity, CourseStudentDetail courseStudent, CoregCoursesRecord coregCoursesRecord, GraduationStudentRecordEntity existingStudentRecordEntity) {
        var relatedCourseRecord = StringUtils.isNotBlank(courseStudent.getRelatedCourse()) && StringUtils.isNotBlank(courseStudent.getRelatedLevel()) ? getCoregCoursesRecord(courseStudent.getRelatedCourse(), courseStudent.getRelatedLevel()) : null;

        var fineArtsSkillsCode = resolveFineArtsAppliedSkillsCode(courseStudent, coregCoursesRecord, existingStudentRecordEntity);

        var equivalentOrChallengeCode = StringUtils.isNotBlank(courseStudent.getCourseType()) ?
                equivalentOrChallengeCodeRepository.findById(courseStudent.getCourseType()).map(EquivalentOrChallengeCodeEntity::getEquivalentOrChallengeCode).orElse(null)
                : null;
        if(StringUtils.isNotBlank(newStudentCourseEntity.getInterimLetterGrade())
                && StringUtils.isNotBlank(courseStudent.getInterimLetterGrade())
                && !newStudentCourseEntity.getInterimLetterGrade().equalsIgnoreCase(courseStudent.getInterimLetterGrade())) {
            newStudentCourseEntity.setInterimLetterGrade(courseStudent.getInterimLetterGrade());
        } else if(StringUtils.isBlank(newStudentCourseEntity.getInterimLetterGrade())) {
            newStudentCourseEntity.setInterimLetterGrade(mapLetterGrade(courseStudent.getInterimLetterGrade(), courseStudent.getInterimPercentage()));
        }

        if(StringUtils.isNotBlank(newStudentCourseEntity.getCompletedCourseLetterGrade())
                && StringUtils.isNotBlank(newStudentCourseEntity.getCompletedCourseLetterGrade())
                && !newStudentCourseEntity.getCompletedCourseLetterGrade().equalsIgnoreCase(courseStudent.getFinalLetterGrade())) {
            newStudentCourseEntity.setCompletedCourseLetterGrade(courseStudent.getFinalLetterGrade());
        } else if(StringUtils.isBlank(newStudentCourseEntity.getCompletedCourseLetterGrade())) {
            newStudentCourseEntity.setCompletedCourseLetterGrade(mapLetterGrade(courseStudent.getFinalLetterGrade(), courseStudent.getFinalPercentage()));
        }

        if(relatedCourseRecord != null && newStudentCourseEntity.getRelatedCourseId() != null
                && !Objects.equals(newStudentCourseEntity.getRelatedCourseId(), new BigInteger(relatedCourseRecord.getCourseID()))) {
            newStudentCourseEntity.setRelatedCourseId(new BigInteger(relatedCourseRecord.getCourseID()));
        }

        if(StringUtils.isNotBlank(coregCoursesRecord.getGenericCourseType()) && coregCoursesRecord.getGenericCourseType().equalsIgnoreCase("G")) {
            newStudentCourseEntity.setCustomizedCourseName(courseStudent.getCourseDescription());
        }


        newStudentCourseEntity.setInterimPercent(mapPercentage(courseStudent.getInterimLetterGrade(), courseStudent.getInterimPercentage()));
        newStudentCourseEntity.setCompletedCoursePercentage(mapPercentage(courseStudent.getFinalLetterGrade(), courseStudent.getFinalPercentage()));
        newStudentCourseEntity.setCredits(StringUtils.isNotBlank(courseStudent.getNumberOfCredits()) ? Integer.parseInt(courseStudent.getNumberOfCredits()) : 0);
        newStudentCourseEntity.setFineArtsAppliedSkills(fineArtsSkillsCode);
        newStudentCourseEntity.setEquivOrChallenge(equivalentOrChallengeCode);

        return newStudentCourseEntity;
    }

    private StudentCourseEntity createStudentCourseEntity(CourseStudentDetail courseStudent, String studentID, CoregCoursesRecord coregCoursesRecord, GraduationStudentRecordEntity existingStudentRecordEntity) {
        var relatedCourseRecord = StringUtils.isNotBlank(courseStudent.getRelatedCourse()) && StringUtils.isNotBlank(courseStudent.getRelatedLevel()) ?
                getCoregCoursesRecord(courseStudent.getRelatedCourse(), courseStudent.getRelatedLevel()) : null;

        var fineArtsSkillsCode = resolveFineArtsAppliedSkillsCode(courseStudent, coregCoursesRecord, existingStudentRecordEntity);

        var equivalentOrChallengeCode = StringUtils.isNotBlank(courseStudent.getCourseType()) ?
                equivalentOrChallengeCodeRepository.findById(courseStudent.getCourseType()).map(EquivalentOrChallengeCodeEntity::getEquivalentOrChallengeCode).orElse(null)
                : null;
        return StudentCourseEntity
                .builder()
                .studentID(UUID.fromString(studentID))
                .courseID(new BigInteger(coregCoursesRecord.getCourseID()))
                .courseSession(courseStudent.getCourseYear() + courseStudent.getCourseMonth())
                .interimLetterGrade(mapLetterGrade(courseStudent.getInterimLetterGrade(), courseStudent.getInterimPercentage()))
                .interimPercent(mapPercentage(courseStudent.getInterimLetterGrade(), courseStudent.getInterimPercentage()))
                .completedCourseLetterGrade(mapLetterGrade(courseStudent.getFinalLetterGrade(), courseStudent.getFinalPercentage()))
                .completedCoursePercentage(mapPercentage(courseStudent.getFinalLetterGrade(), courseStudent.getFinalPercentage()))
                .relatedCourseId(relatedCourseRecord != null ? new BigInteger(relatedCourseRecord.getCourseID()) : null)
                .customizedCourseName(StringUtils.isNotBlank(coregCoursesRecord.getGenericCourseType()) && coregCoursesRecord.getGenericCourseType().equalsIgnoreCase("G") ? courseStudent.getCourseDescription() : null)
                .fineArtsAppliedSkills(fineArtsSkillsCode)
                .equivOrChallenge(equivalentOrChallengeCode)
                .credits(StringUtils.isNotBlank(courseStudent.getNumberOfCredits()) ? Integer.parseInt(courseStudent.getNumberOfCredits()) : 0)
                .build();
    }

    private String resolveFineArtsAppliedSkillsCode(CourseStudentDetail courseStudent, CoregCoursesRecord coregCoursesRecord, GraduationStudentRecordEntity existingStudentRecordEntity) {
        var gradRequirementYear = existingStudentRecordEntity.getProgram().replace("-EN","").replace("-PF","");
        var catCode = coregCoursesRecord.getCourseCategory() != null && coregCoursesRecord.getCourseCategory().getCode() != null ? coregCoursesRecord.getCourseCategory().getCode() : "";
        var isGrade11 = "11".equalsIgnoreCase(courseStudent.getCourseLevel());
        var is1996 = GradRequirementYearCodes.YEAR_1996.getCode().equalsIgnoreCase(gradRequirementYear);
        var is2004_2018_2023 = GradRequirementYearCodes.get2004_2018_2023Codes().stream().anyMatch(reqYear -> reqYear.equalsIgnoreCase(gradRequirementYear));
        var isBA = "BA".equalsIgnoreCase(catCode);
        var isLD = "LD".equalsIgnoreCase(catCode);

        var shouldConsume = StringUtils.isNotBlank(courseStudent.getCourseGraduationRequirement())
                && isGrade11
                && ( (is1996 && (isBA || isLD)) || (is2004_2018_2023 && isBA) );

        return shouldConsume
                ? fineArtsAppliedSkillsCodeRepository.findById(courseStudent.getCourseGraduationRequirement())
                .map(FineArtsAppliedSkillsCodeEntity::getFineArtsAppliedSkillsCode)
                .orElse(null)
                : null;
    }

    private String mapLetterGrade(String letterGrade, String percent) {
        List<LetterGrade> letterGradeList = courseCacheService.getLetterGradesFromCache();
        if(StringUtils.isBlank(letterGrade) && StringUtils.isNotBlank(percent)) {
            var letterEntity =  letterGradeList.stream().filter(grade -> grade.getPercentRangeHigh() != null &&
                    grade.getPercentRangeHigh() >= Integer.parseInt(percent)
                    && grade.getPercentRangeLow() != null && grade.getPercentRangeLow() <= Integer.parseInt(percent)).findFirst();
            return letterEntity.map(LetterGrade::getGrade).orElse(null);
        } else {
            return letterGrade;
        }
    }

    private Double mapPercentage(String letterGrade, String percent) {
        List<LetterGrade> letterGradeList = courseCacheService.getLetterGradesFromCache();
        var doublePercent = percent != null ? Double.parseDouble(percent) : null;
        if(StringUtils.isNotBlank(letterGrade)) {
            var letterEntity =  letterGradeList.stream()
                    .filter(grade -> grade.getGrade().equalsIgnoreCase(letterGrade)
                            && grade.getPercentRangeHigh() != null && grade.getPercentRangeLow() != null)
                    .findFirst();
            return letterEntity.isPresent() ? doublePercent : null;
        } else {
            return doublePercent;
        }
    }

    private CoregCoursesRecord getCoregCoursesRecord(String courseCode, String courseLevel) {
        String externalID = StringUtils.isEmpty(courseLevel) ? courseCode : String.format("%-5s", courseCode) + courseLevel;
        return restUtils.getCoursesByExternalID(UUID.randomUUID(), externalID);
    }

    private List<StudentOptionalProgramEntity> getOptionalProgramForRemoval(UUID studentID, List<UUID> incomingProgramIDs, List<OptionalProgramCode> optionalProgramCodes, boolean isGraduated) {
        if (isGraduated) {
            return Collections.emptyList();
        }
        List<StudentOptionalProgramEntity> existingPrograms = studentOptionalProgramRepository.findByStudentID(studentID);

        Set<UUID> protectedIds = OptionalProgramCodes.getProtectedCodes().stream()
                .map(code -> getOptionalProgramCode(optionalProgramCodes, code))
                .flatMap(Optional::stream)
                .map(OptionalProgramCode::getOptionalProgramID)
                .collect(Collectors.toSet());

        List<StudentOptionalProgramEntity> optionalProgramsToRemove = new ArrayList<>();
        for (StudentOptionalProgramEntity existing : existingPrograms) {
            UUID opId = existing.getOptionalProgramID();
            if (!incomingProgramIDs.contains(opId) && !protectedIds.contains(opId)) {
                optionalProgramsToRemove.add(existing);
            }
        }
        return optionalProgramsToRemove;
    }

    private List<UUID> getOptionalProgramToAdd(UUID studentID, List<UUID> incomingProgramIDs) {
        List<StudentOptionalProgramEntity> existingPrograms = studentOptionalProgramRepository.findByStudentID(studentID);

        List<UUID> optionalProgramsToAdd = new ArrayList<>();
        incomingProgramIDs.forEach(programID -> {
            if(existingPrograms.stream().noneMatch(existingProgram -> existingProgram.getOptionalProgramID().equals(programID))) {
                optionalProgramsToAdd.add(programID);
            }
        });
        return optionalProgramsToAdd;
    }

    private List<UUID> getOptionalProgramIDForIncomingPrograms(DemographicStudent demStudent, List<OptionalProgramCode> optionalProgramCodes) {
        List<UUID> optionalProgramIDs = new ArrayList<>();
        if(StringUtils.isNotBlank(demStudent.getProgramCode1())) {
            var programCode1Entity = getOptionalProgramCode(optionalProgramCodes, extractProgramCode(demStudent.getProgramCode1()));
            programCode1Entity.ifPresent(entity -> optionalProgramIDs.add(entity.getOptionalProgramID()));
        }

        if(StringUtils.isNotBlank(demStudent.getProgramCode2())) {
            var programCode2Entity = getOptionalProgramCode(optionalProgramCodes, extractProgramCode(demStudent.getProgramCode2()));
            programCode2Entity.ifPresent(entity -> optionalProgramIDs.add(entity.getOptionalProgramID()));
        }

        if(StringUtils.isNotBlank(demStudent.getProgramCode3())) {
            var programCode3Entity = getOptionalProgramCode(optionalProgramCodes, extractProgramCode(demStudent.getProgramCode3()));
            programCode3Entity.ifPresent(entity -> optionalProgramIDs.add(entity.getOptionalProgramID()));
        }

        if(StringUtils.isNotBlank(demStudent.getProgramCode4())) {
            var programCode4Entity = getOptionalProgramCode(optionalProgramCodes, extractProgramCode(demStudent.getProgramCode4()));
            programCode4Entity.ifPresent(entity -> optionalProgramIDs.add(entity.getOptionalProgramID()));
        }

        if(StringUtils.isNotBlank(demStudent.getProgramCode5())) {
            var programCode5Entity = getOptionalProgramCode(optionalProgramCodes, extractProgramCode(demStudent.getProgramCode5()));
            programCode5Entity.ifPresent(entity -> optionalProgramIDs.add(entity.getOptionalProgramID()));
        }
        return optionalProgramIDs;
    }

    private GraduationStudentRecordEntity compareAndUpdateGraduationStudentRecordEntity(DemographicStudent demStudent, GraduationStudentRecordEntity newStudentRecordEntity) {
        int projectedChangeCount = 0;
        int statusChangeCount = 0;
        if(demStudent.getIsSummerCollection().equalsIgnoreCase("N")) {
            if(newStudentRecordEntity.getSchoolOfRecordId() != null && newStudentRecordEntity.getSchoolOfRecordId() != UUID.fromString(demStudent.getSchoolID()) && (demStudent.getStudentStatus().equalsIgnoreCase("A") || demStudent.getStudentStatus().equalsIgnoreCase("T"))) {
                newStudentRecordEntity.setSchoolOfRecordId(UUID.fromString(demStudent.getSchoolID()));
                projectedChangeCount++;
                statusChangeCount++;
            }
            if(!newStudentRecordEntity.getStudentGrade().equalsIgnoreCase(demStudent.getGrade()) && demStudent.getStudentStatus().equalsIgnoreCase("A") && newStudentRecordEntity.getProgramCompletionDate() == null) {
                newStudentRecordEntity.setStudentGrade(demStudent.getGrade());
                projectedChangeCount++;
                statusChangeCount++;
            } else if (!newStudentRecordEntity.getStudentGrade().equalsIgnoreCase(demStudent.getGrade()) && demStudent.getStudentStatus().equalsIgnoreCase("A") && newStudentRecordEntity.getProgramCompletionDate() != null) {
                newStudentRecordEntity.setStudentGrade(demStudent.getGrade());
                projectedChangeCount++;
            } else if (!newStudentRecordEntity.getStudentGrade().equalsIgnoreCase(demStudent.getGrade()) && demStudent.getStudentStatus().equalsIgnoreCase("T")) {
                newStudentRecordEntity.setStudentGrade(demStudent.getGrade());
                statusChangeCount++;
            }

            var mappedStudentStatus = mapStudentStatus(demStudent.getStudentStatus());
            if(!newStudentRecordEntity.getStudentStatus().equalsIgnoreCase(mappedStudentStatus)) {
                newStudentRecordEntity.setStudentStatus(mappedStudentStatus);
                projectedChangeCount++;
                statusChangeCount++;
            }

            if(StringUtils.isNotBlank(newStudentRecordEntity.getStudentCitizenship()) && StringUtils.isNotBlank(demStudent.getCitizenship()) && !newStudentRecordEntity.getStudentCitizenship().equalsIgnoreCase(demStudent.getCitizenship())) {
                newStudentRecordEntity.setStudentCitizenship(demStudent.getCitizenship());
                projectedChangeCount++;
                statusChangeCount++;
            }
        }

        if (StringUtils.isNotBlank(demStudent.getGradRequirementYear())) {
            String mappedProgram = mapGradProgramCode(demStudent.getGradRequirementYear(), demStudent.getSchoolReportingRequirementCode());
            boolean isGraduated = deriveIfGraduated(newStudentRecordEntity);
            boolean hasProgramCompletionDate = newStudentRecordEntity.getProgramCompletionDate() != null;
            boolean completedSCCP = hasProgramCompletionDate && "SCCP".equalsIgnoreCase(newStudentRecordEntity.getProgram());
            if (!isGraduated || completedSCCP) {
                newStudentRecordEntity.setProgram(mappedProgram);
                projectedChangeCount++;
                statusChangeCount++;
            }
        }
        
        if(StringUtils.isNotBlank(demStudent.getGradRequirementYear()) && demStudent.getGradRequirementYear().equalsIgnoreCase("SCCP")) {
            var parsedSSCPDate = StringUtils.isNotBlank(demStudent.getSchoolCertificateCompletionDate()) ?
                    Date.valueOf(LocalDate.parse(demStudent.getSchoolCertificateCompletionDate(), DateTimeFormatter.ofPattern(YYYY_MM_DD))) : null;
            newStudentRecordEntity.setProgramCompletionDate(parsedSSCPDate);
            projectedChangeCount++;
            statusChangeCount++;
        }

        if(newStudentRecordEntity.getAdultStartDate() == null) {
            newStudentRecordEntity.setAdultStartDate(mapAdultStartDate(demStudent.getBirthdate(), demStudent.getGradRequirementYear()));
        }

        if(projectedChangeCount > 0) {
            newStudentRecordEntity.setRecalculateProjectedGrad("Y");
        }

        if(statusChangeCount > 0) {
            newStudentRecordEntity.setRecalculateGradStatus("Y");
        }
        return newStudentRecordEntity;
    }

    private Optional<OptionalProgramCode> getOptionalProgramCode(List<OptionalProgramCode> optionalProgramCodes, String incomingProgramCode) {
        return  optionalProgramCodes.stream().filter(program -> program.getOptProgramCode().equalsIgnoreCase(incomingProgramCode)).findFirst();
    }

    private StudentOptionalProgramEntity createStudentOptionalProgramEntity(UUID programID, UUID studentID, String createUser, String updateUser) {
        var entity =  StudentOptionalProgramEntity
               .builder()
               .optionalProgramID(programID)
               .studentID(studentID)
               .build();
        entity.setCreateUser(createUser);
        entity.setUpdateUser(updateUser);
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        return entity;
    }

    private String extractProgramCode(String incomingProgramCode) {
        if(incomingProgramCode.length() == 3) {
            return incomingProgramCode.substring(1);
        } else if(incomingProgramCode.length() == 4) {
            return incomingProgramCode.substring(2);
        }
        return incomingProgramCode;
    }

    private GraduationStudentRecordEntity createGraduationStudentRecordEntity(DemographicStudent demStudent, Student studentFromApi) {
        var parsedSSCPDate = StringUtils.isNotBlank(demStudent.getSchoolCertificateCompletionDate()) ?
                Date.valueOf(LocalDate.parse(demStudent.getSchoolCertificateCompletionDate(), DateTimeFormatter.ofPattern(YYYY_MM_DD))) : null;
        return GraduationStudentRecordEntity
                .builder()
                .pen(demStudent.getPen())
                .programCompletionDate(demStudent.getIsSummerCollection().equalsIgnoreCase("N") ? parsedSSCPDate : null)
                .studentGrade(demStudent.getIsSummerCollection().equalsIgnoreCase("N") ? demStudent.getGrade() : studentFromApi.getGradeCode())
                .studentStatus(demStudent.getIsSummerCollection().equalsIgnoreCase("N") ? mapStudentStatus(demStudent.getStudentStatus()) : CURRENT)
                .studentCitizenship(demStudent.getIsSummerCollection().equalsIgnoreCase("N") ? demStudent.getCitizenship() : null)
                .schoolOfRecordId(UUID.fromString(demStudent.getSchoolID()))
                .studentID(UUID.fromString(studentFromApi.getStudentID()))
                .recalculateGradStatus("Y")
                .recalculateProjectedGrad("Y")
                .adultStartDate(demStudent.getIsSummerCollection().equalsIgnoreCase("N") ? mapAdultStartDate(demStudent.getBirthdate(), demStudent.getGradRequirementYear()): null)
                .build();
    }

    private Date mapAdultStartDate(String birthdate, String gradYear) {
        if(StringUtils.isNotBlank(gradYear) && gradYear.equalsIgnoreCase("1950")) {
            var parsedBirthdate = LocalDate.parse(birthdate, DateTimeFormatter.ofPattern(YYYY_MM_DD));
            if(parsedBirthdate.getYear() >= 1994) {
                return Date.valueOf(parsedBirthdate.plusYears(18).plusMonths(1));
            } else {
                return Date.valueOf(parsedBirthdate.plusYears(19));
            }
        }
        return null;
    }

    private String mapStudentStatus(String demStudentStatus) {
        if(demStudentStatus.equalsIgnoreCase("A")) {
            return CURRENT;
        } else if(demStudentStatus.equalsIgnoreCase("T")) {
            return TERMINATED;
        } else if(demStudentStatus.equalsIgnoreCase("D")) {
            return DECEASED;
        } else {
            return null;
        }
    }

    private String mapGradProgramCode(String demGradProgramCode, String schoolReportingRequirementCode) {
        if(demGradProgramCode.equalsIgnoreCase("2018")
        || demGradProgramCode.equalsIgnoreCase("2004")
        || demGradProgramCode.equalsIgnoreCase("1996")
        || demGradProgramCode.equalsIgnoreCase("1986")
        || demGradProgramCode.equalsIgnoreCase("2023")) {
            return schoolReportingRequirementCode.equalsIgnoreCase("CSF") ? demGradProgramCode + "-PF" : demGradProgramCode + "-EN";
        } else {
            return demGradProgramCode;
        }
    }

    private String createProgram() {
        List<GraduationProgramCode> codes =  restUtils.getGraduationProgramCodeList(true);
        var filteredCodes = codes.stream().filter(code -> !code.getProgramCode().equalsIgnoreCase("1950") && !code.getProgramCode().equalsIgnoreCase("SCCP") && !code.getProgramCode().equalsIgnoreCase("NOPROG")).findFirst();
        var code = filteredCodes.orElseThrow(() ->new EntityNotFoundException(GraduationProgramCode.class, "Program Code", "Program Code not found"));
        var splitProgramCode = code.getProgramCode().split("-");
        return splitProgramCode.length == 2 ? splitProgramCode[0] + "-" + "EN" : code.getProgramCode();
    }

    private boolean checkIfSchoolOfRecordIsUpdated(DemographicStudent demStudent, GraduationStudentRecordEntity existingStudentRecordEntity) {
        return existingStudentRecordEntity.getSchoolOfRecordId() != null && existingStudentRecordEntity.getSchoolOfRecordId() != UUID.fromString(demStudent.getSchoolID()) && (demStudent.getStudentStatus().equalsIgnoreCase("A") || demStudent.getStudentStatus().equalsIgnoreCase("T"));
    }

    private boolean deriveIfGraduated(GraduationStudentRecordEntity studentRecord) {
        GraduationData graduationData = null;
        try {
            graduationData = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(studentRecord.getStudentGradData(), GraduationData.class);
        } catch (Exception e) {
            logger.debug("Parsing Graduation Data Error {}", e.getMessage());
        }
        return graduationData != null && graduationData.isGraduated();
    }
}
