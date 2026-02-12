package ca.bc.gov.educ.api.gradstudent.service.event;

import ca.bc.gov.educ.api.gradstudent.constant.GradRequirementYearCodes;
import ca.bc.gov.educ.api.gradstudent.constant.OptionalProgramCodes;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.CourseStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.CourseStudentDetail;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.DemographicStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.CareerProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.GraduationProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.OptionalProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.student.v1.StudentUpdate;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import ca.bc.gov.educ.api.gradstudent.service.CourseCacheService;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.service.HistoryService;
import ca.bc.gov.educ.api.gradstudent.util.DateUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ca.bc.gov.educ.api.gradstudent.constant.HistoryActivityCodes.ASSESSUPD;
import static ca.bc.gov.educ.api.gradstudent.constant.HistoryActivityCodes.PENALERT;

@Service
@Slf4j
@RequiredArgsConstructor
public class GraduationStudentRecordService {
    private final RestUtils restUtils;
    private final CourseCacheService courseCacheService;
    private final GraduationStudentRecordRepository graduationStudentRecordRepository;
    private final GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository;
    private final StudentOptionalProgramRepository studentOptionalProgramRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final FineArtsAppliedSkillsCodeRepository fineArtsAppliedSkillsCodeRepository;
    private final EquivalentOrChallengeCodeRepository equivalentOrChallengeCodeRepository;
    private final StudentCareerProgramRepository studentCareerProgramRepository;
    private static final String GDC_ADD = "GDCADD";// confirm,
    private static final String GDC_UPDATE = "GDCUPATE";
    private static final String GDC_DELETE = "GDCDELETE";
    private final HistoryService historyService;
    private final GraduationStatusService graduationStatusService;
    public static final String CURRENT = "CUR";
    public static final String TERMINATED = "TER";
    public static final String DECEASED = "DEC";
    public static final String ARC = "ARC";
    public static final String MERGED = "MER";
    public static final Set<String> gradActiveStatuses = Set.of(CURRENT, TERMINATED, ARC);
    public static final Set<String> gradNonActiveStatuses = Set.of(MERGED, DECEASED);
    public static final String CREATE_USER = "createUser";
    public static final String CREATE_DATE = "createDate";
    public static final String YYYY_MM_DD = "uuuuMMdd";
    public static final String EN_1996_CODE = "1996-EN";
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd";
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
    public void handleStudentUpdated(StudentUpdate studentUpdate, GraduationStudentRecordEntity existingStudentRecordEntity, final GradStatusEvent event){
        String dob = studentUpdate.getDob();
        if(StringUtils.isNotBlank(dob)){
            try {
                existingStudentRecordEntity.setDob(
                        DateUtils.stringToLocalDateTime(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT), dob)
                );
            } catch(Exception e) {
                logger.error("Error replicating DOB from upstream: {}", e.getMessage());
                existingStudentRecordEntity.setDob(null);
            }
        }
        updateGradStatusFromStudentApi(studentUpdate, existingStudentRecordEntity);
        existingStudentRecordEntity.setPen(studentUpdate.getPen());
        existingStudentRecordEntity.setGenderCode(studentUpdate.getGenderCode());
        existingStudentRecordEntity.setLegalFirstName(studentUpdate.getLegalFirstName());
        existingStudentRecordEntity.setLegalLastName(studentUpdate.getLegalLastName());
        existingStudentRecordEntity.setLegalMiddleNames(studentUpdate.getLegalMiddleNames());
        existingStudentRecordEntity.setUpdateUser(event.getUpdateUser());
        existingStudentRecordEntity.setUpdateDate(LocalDateTime.now());
        if(!studentUpdate.getStatusCode().equals("M")){
            existingStudentRecordEntity.setRecalculateProjectedGrad("Y");
            existingStudentRecordEntity.setRecalculateGradStatus("Y");
        }
        var savedStudentRecord = graduationStudentRecordRepository.save(existingStudentRecordEntity);
        historyService.createStudentHistory(savedStudentRecord, PENALERT.getCode());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void handleSetFlagsForGradStudent(GraduationStudentRecordEntity existingStudentRecordEntity, final GradStatusEvent event) {
        existingStudentRecordEntity.setUpdateUser(event.getUpdateUser());
        existingStudentRecordEntity.setUpdateDate(LocalDateTime.now());
        existingStudentRecordEntity.setRecalculateProjectedGrad("Y");
        existingStudentRecordEntity.setRecalculateGradStatus("Y");
        var saved = graduationStudentRecordRepository.save(existingStudentRecordEntity);
        historyService.createStudentHistory(saved, ASSESSUPD.getCode());
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Pair<GraduationStudentRecord, GradStatusEvent> handleAssessmentAdoptEvent(String studentID, final String updateUser) throws JsonProcessingException {
        return graduationStatusService.adoptStudent(UUID.fromString(studentID), updateUser);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public GraduationStudentRecordEntity createNewStudentRecord(DemographicStudent demStudent, Student studentFromApi) {
        List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();
        List<CareerProgramCode> careerProgramCodes = restUtils.getCareerProgramCodeList();
        List<StudentOptionalProgramEntity> optionalProgramEntities = new ArrayList<>();
        GraduationStudentRecordEntity entity = createGraduationStudentRecordEntity(demStudent, studentFromApi);
        if(StringUtils.isNotBlank(demStudent.getGradRequirementYear()) && demStudent.getIsSummerCollection().equalsIgnoreCase("N")) {
            entity.setProgram(mapGradProgramCode(demStudent.getGradRequirementYear(), demStudent.getSchoolReportingRequirementCode()));
        } else {
            entity.setProgram(createProgram(demStudent.getSchoolReportingRequirementCode()));
        }

        entity.setCreateUser(demStudent.getCreateUser());
        entity.setUpdateUser(demStudent.getUpdateUser());
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        var savedStudentRecord = graduationStudentRecordRepository.save(entity);
        historyService.createStudentHistory(savedStudentRecord, GDC_ADD);

        //List of all optional programs
        List<String> allProgramCodes = Stream.of(demStudent.getProgramCode1(), demStudent.getProgramCode2(), demStudent.getProgramCode3(), demStudent.getProgramCode4(), demStudent.getProgramCode5())
                .filter(Objects::nonNull)
                .toList();

        List<UUID> incomingProgramIDs = new ArrayList<>();
        List<String> careerProgramIDs = new ArrayList<>();

        allProgramCodes.forEach(code -> {
            getOptionalProgramCode(optionalProgramCodes, code, savedStudentRecord.getProgram()).ifPresent(optEntity -> incomingProgramIDs.add(optEntity.getOptionalProgramID()));
            getOptionalCareerCode(careerProgramCodes, code).ifPresent(careerEntity -> careerProgramIDs.add(careerEntity.getCode()));
        });

        //CP optional program ID
        var optionalCPProgramCode = getOptionalProgramCode(optionalProgramCodes, "CP", savedStudentRecord.getProgram());

        if(!careerProgramIDs.isEmpty() && optionalCPProgramCode.isPresent()) {
            incomingProgramIDs.add(optionalCPProgramCode.get().getOptionalProgramID());
        }

        //if student is not grad. and program code is -PF - add DD if not present
        if (StringUtils.endsWithIgnoreCase(savedStudentRecord.getProgram(), "-PF")) {
            getOptionalProgramCode(optionalProgramCodes, OptionalProgramCodes.DD.getCode(), savedStudentRecord.getProgram())
                    .map(OptionalProgramCode::getOptionalProgramID)
                    .filter(ddId -> !incomingProgramIDs.contains(ddId))
                    .ifPresent(incomingProgramIDs::add);
        }

        incomingProgramIDs.forEach(programID -> optionalProgramEntities.add(createStudentOptionalProgramEntity(programID, savedStudentRecord.getStudentID(), demStudent.getCreateUser(), demStudent.getUpdateUser())));

        if(StringUtils.isNotBlank(savedStudentRecord.getProgram()) && savedStudentRecord.getProgram().equalsIgnoreCase("SCCP") && savedStudentRecord.getProgramCompletionDate() != null && demStudent.getSchoolReportingRequirementCode().equalsIgnoreCase("CSF")) {
            var frProgram = getOptionalProgramCode(optionalProgramCodes, "FI",  entity.getProgram());
            frProgram.ifPresent(optionalProgramCode -> optionalProgramEntities.add(createStudentOptionalProgramEntity(optionalProgramCode.getOptionalProgramID(), savedStudentRecord.getStudentID(), demStudent.getCreateUser(), demStudent.getUpdateUser())));
        }

        if(!careerProgramIDs.isEmpty()) {
            List<String> careerCodesToAdd = getCareerProgramToAdd(UUID.fromString(studentFromApi.getStudentID()), careerProgramIDs);
            // this will save career codes to the career repository
            List<StudentCareerProgramEntity> careerProgramEntities = new ArrayList<>();
            careerCodesToAdd.forEach(programCode -> careerProgramEntities.add(createStudentCareerProgramEntity(programCode, savedStudentRecord.getStudentID(), demStudent.getCreateUser(), demStudent.getUpdateUser())));
            studentCareerProgramRepository.saveAll(careerProgramEntities);
        }
        var savedEntities = studentOptionalProgramRepository.saveAll(optionalProgramEntities);
        savedEntities.forEach(optEntity -> historyService.createStudentOptionalProgramHistory(optEntity, GDC_ADD));
        return savedStudentRecord;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public Pair<GradStudentUpdateResult, GraduationStudentRecordEntity> updateStudentRecord(DemographicStudent demStudent, Student studentFromApi, GraduationStudentRecordEntity existingStudentRecordEntity) {
        var newStudentRecordEntity = new GraduationStudentRecordEntity();
        var gradStudentUpdateResult = new GradStudentUpdateResult();
        gradStudentUpdateResult.setSchoolOfRecordUpdated(checkIfSchoolOfRecordIsUpdated(demStudent, existingStudentRecordEntity));
        BeanUtils.copyProperties(existingStudentRecordEntity, newStudentRecordEntity, CREATE_USER, CREATE_DATE);
        Pair<Boolean,GraduationStudentRecordEntity> studentUpdate = compareAndUpdateGraduationStudentRecordEntity(demStudent, newStudentRecordEntity, gradStudentUpdateResult);
        
        var studentWasUpdated = studentUpdate.getLeft();
        var updatedEntity = studentUpdate.getRight();
        updatedEntity.setUpdateUser(demStudent.getUpdateUser());
        updatedEntity.setUpdateDate(LocalDateTime.now());
        // ensure latest demographics from student api
        if (studentFromApi != null) {
            updatedEntity.setPen(studentFromApi.getPen());
            updatedEntity.setLegalFirstName(studentFromApi.getLegalFirstName());
            updatedEntity.setLegalMiddleNames(studentFromApi.getLegalMiddleNames());
            updatedEntity.setLegalLastName(studentFromApi.getLegalLastName());
            updatedEntity.setGenderCode(studentFromApi.getGenderCode());
            if (StringUtils.isNotBlank(studentFromApi.getDob())) {
                try {
                    updatedEntity.setDob(DateUtils.stringToLocalDateTime(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT), studentFromApi.getDob()));
                } catch (Exception e) {
                    logger.warn("Invalid DOB from Student API for student {}: {}", studentFromApi.getStudentID(), studentFromApi.getDob());
                }
            }
        }
        var savedStudentRecord = graduationStudentRecordRepository.save(updatedEntity);
        if(Boolean.TRUE.equals(studentWasUpdated)) {
            historyService.createStudentHistory(savedStudentRecord, GDC_UPDATE);
        }

        handleOptionalPrograms(demStudent, studentFromApi, updatedEntity);
        return Pair.of(gradStudentUpdateResult, savedStudentRecord);
    }

    private void handleOptionalPrograms(DemographicStudent demStudent, Student studentFromApi, GraduationStudentRecordEntity savedStudentRecord) {
        boolean isGraduated = deriveIfGraduated(savedStudentRecord);

        List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();
        List<CareerProgramCode> careerProgramCodes = restUtils.getCareerProgramCodeList();

        //List of all optional programs
        List<String> allProgramCodes = Stream.of(demStudent.getProgramCode1(), demStudent.getProgramCode2(), demStudent.getProgramCode3(), demStudent.getProgramCode4(), demStudent.getProgramCode5())
                .filter(Objects::nonNull)
                .toList();

        List<UUID> incomingProgramIDs = new ArrayList<>();
        List<String> careerProgramIDs = new ArrayList<>();

        allProgramCodes.forEach(code -> {
            getOptionalProgramCode(optionalProgramCodes, code, savedStudentRecord.getProgram()).ifPresent(entity -> incomingProgramIDs.add(entity.getOptionalProgramID()));
            getOptionalCareerCode(careerProgramCodes, code).ifPresent(entity -> careerProgramIDs.add(entity.getCode()));
        });

        //CP optional program ID
        var optionalCPProgramCode = getOptionalProgramCode(optionalProgramCodes, "CP", savedStudentRecord.getProgram());

        //if student is not grad. and program code is -PF - add DD if not present
        if (!isGraduated && StringUtils.endsWithIgnoreCase(savedStudentRecord.getProgram(), "-PF")) {
            getOptionalProgramCode(optionalProgramCodes, OptionalProgramCodes.DD.getCode(), savedStudentRecord.getProgram())
                    .map(OptionalProgramCode::getOptionalProgramID)
                    .filter(ddId -> !incomingProgramIDs.contains(ddId))
                    .ifPresent(incomingProgramIDs::add);
        }

        var careerCodesForRemoval = getCareerCodesForRemoval(UUID.fromString(studentFromApi.getStudentID()), careerProgramIDs, isGraduated);
        if(!careerCodesForRemoval.isEmpty()) {
            studentCareerProgramRepository.deleteAll(careerCodesForRemoval);
        }

        //remove optional prog. - it will remove optional program code and CP if condition is met
        var optionalProgramsToRemove = getOptionalProgramForRemoval(UUID.fromString(studentFromApi.getStudentID()), incomingProgramIDs, optionalProgramCodes, isGraduated, savedStudentRecord.getProgram());
        log.debug("Found optional program IDs to remove :: {}", optionalProgramsToRemove);
        if(!optionalProgramsToRemove.isEmpty()) {
            optionalProgramsToRemove.forEach(optEntity -> historyService.createStudentOptionalProgramHistory(optEntity, GDC_DELETE));
            studentOptionalProgramRepository.deleteAll(optionalProgramsToRemove);
        }

        //add optional prog. - this will add CP if condition is met
        List<UUID> programIDsToAdd = getOptionalProgramToAdd(UUID.fromString(studentFromApi.getStudentID()), incomingProgramIDs);
        if(!careerProgramIDs.isEmpty() && optionalCPProgramCode.isPresent()) {
            programIDsToAdd.add(optionalCPProgramCode.get().getOptionalProgramID());
        }
        log.debug("Found optional program IDs to add :: {}", programIDsToAdd);

        List<StudentOptionalProgramEntity> optionalProgramEntities = new ArrayList<>();
        programIDsToAdd.forEach(programID -> optionalProgramEntities.add(createStudentOptionalProgramEntity(programID, savedStudentRecord.getStudentID(), demStudent.getCreateUser(), demStudent.getUpdateUser())));
        var savedOptionalEntities = studentOptionalProgramRepository.saveAll(optionalProgramEntities);

        if(!careerProgramIDs.isEmpty()) {
            List<String> careerCodesToAdd = getCareerProgramToAdd(UUID.fromString(studentFromApi.getStudentID()), careerProgramIDs);
            // this will save career codes to the career repository
            log.debug("Found career codes to add :: {}", careerCodesToAdd);
            List<StudentCareerProgramEntity> careerProgramEntities = new ArrayList<>();
            careerCodesToAdd.forEach(programCode -> careerProgramEntities.add(createStudentCareerProgramEntity(programCode, savedStudentRecord.getStudentID(), demStudent.getCreateUser(), demStudent.getUpdateUser())));
            studentCareerProgramRepository.saveAll(careerProgramEntities);
        }
        savedOptionalEntities.forEach(optEntity -> historyService.createStudentOptionalProgramHistory(optEntity, GDC_UPDATE));

        if (!optionalProgramsToRemove.isEmpty() || !programIDsToAdd.isEmpty()) {
            boolean projAlreadyY = "Y".equalsIgnoreCase(savedStudentRecord.getRecalculateProjectedGrad());
            boolean statusAlreadyY = "Y".equalsIgnoreCase(savedStudentRecord.getRecalculateGradStatus());
            if (!projAlreadyY || !statusAlreadyY) {
                savedStudentRecord.setRecalculateProjectedGrad("Y");
                savedStudentRecord.setRecalculateGradStatus("Y");
                graduationStudentRecordRepository.save(savedStudentRecord);
                historyService.createStudentHistory(savedStudentRecord, GDC_UPDATE);
            }
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void handleStudentCourseRecord(GraduationStudentRecordEntity existingStudentRecordEntity, CourseStudent courseStudent, Student studentFromApi) {
        boolean hasGraduated = existingStudentRecordEntity.getProgramCompletionDate() != null;
        log.debug("Handling student course record for studentID: {}, submissionModeCode: {}, isSummerCollection: {}, hasGraduated: {}",
                studentFromApi.getStudentID(), courseStudent.getSubmissionModeCode(), courseStudent.getIsSummerCollection(), hasGraduated);
        if(courseStudent.getSubmissionModeCode().equalsIgnoreCase("APPEND") || courseStudent.getIsSummerCollection().equalsIgnoreCase("Y") || hasGraduated) {
            courseStudent.getStudentDetails().forEach(student -> handleAppendCourseRecord(existingStudentRecordEntity, student, studentFromApi.getStudentID()));
        } else {
            List<StudentCourseEntity> existingStudentCourses =  studentCourseRepository.findByStudentIDAndCourseExamIsNull(UUID.fromString(studentFromApi.getStudentID()));
            log.debug("Existing student courses to be replaced: {}", existingStudentCourses);
            if(!existingStudentCourses.isEmpty()) {
                studentCourseRepository.deleteAllInBatch(existingStudentCourses);
            }
            courseStudent.getStudentDetails().forEach(student -> {
                if(!student.getCourseStatus().equalsIgnoreCase("W")) {
                    handleReplaceCourseRecord(existingStudentRecordEntity, student, studentFromApi.getStudentID());
                }
            });
        }

        existingStudentRecordEntity.setRecalculateProjectedGrad("Y");
        existingStudentRecordEntity.setRecalculateGradStatus("Y");
        var savedStudentRecord = graduationStudentRecordRepository.save(existingStudentRecordEntity);
        historyService.createStudentHistory(savedStudentRecord, GDC_UPDATE);
    }

    private void handleReplaceCourseRecord(GraduationStudentRecordEntity existingStudentRecordEntity, CourseStudentDetail courseStudent, String studentID) {
        var coursesRecord = getCoregCoursesRecord(courseStudent.getCourseCode(), courseStudent.getCourseLevel());
        log.debug("Creating new student course record for course: {}, level: {}, courseID: {}",
                courseStudent.getCourseCode(), courseStudent.getCourseLevel(), coursesRecord != null ? coursesRecord.getCourseID() : "N/A");
        createNewStudentCourseEntity(courseStudent, studentID, coursesRecord, existingStudentRecordEntity);
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
        } else if(!courseStudent.getCourseStatus().equalsIgnoreCase("W")) {
            createNewStudentCourseEntity(courseStudent, studentID, coursesRecord, existingStudentRecordEntity);
        }
    }

    private void createNewStudentCourseEntity(CourseStudentDetail courseStudent, String studentID, CoregCoursesRecord coursesRecord, GraduationStudentRecordEntity existingStudentRecordEntity) {
        StudentCourseEntity studentCourseEntity = createStudentCourseEntity(courseStudent, studentID, coursesRecord, existingStudentRecordEntity);
        studentCourseEntity.setCreateUser(courseStudent.getCreateUser());
        studentCourseEntity.setUpdateUser(courseStudent.getUpdateUser());
        studentCourseEntity.setCreateDate(LocalDateTime.now());
        studentCourseEntity.setUpdateDate(LocalDateTime.now());
        studentCourseRepository.save(studentCourseEntity);
        log.debug("Created new student course entity for studentID: {}, courseID: {}, courseSession: {}",
                studentID, studentCourseEntity.getCourseID(), studentCourseEntity.getCourseSession());
        updateOptionalPrograms(existingStudentRecordEntity, courseStudent);
    }

    private void updateOptionalPrograms(GraduationStudentRecordEntity existingStudentRecordEntity, CourseStudentDetail courseStudent) {
        String course = StringUtils.isEmpty(courseStudent.getCourseLevel()) ? courseStudent.getCourseCode() : String.format("%-5s", courseStudent.getCourseCode()) + courseStudent.getCourseLevel();
        log.debug("Checking optional programs for course: {}", course);
        boolean isFRAL10 = (course.equalsIgnoreCase("FRAL 10") || course.equalsIgnoreCase("FRALP 10")) && StringUtils.isNotBlank(existingStudentRecordEntity.getProgram()) && fral10Programs.contains(existingStudentRecordEntity.getProgram());
        boolean isFRAL11 = course.equalsIgnoreCase("FRAL 11") && StringUtils.isNotBlank(existingStudentRecordEntity.getProgram()) && fral11Programs.contains(existingStudentRecordEntity.getProgram());
        boolean otherFICourses = (course.equalsIgnoreCase("LAEOF10") || course.equalsIgnoreCase("LANMF10") || course.equalsIgnoreCase("LACWF10")) && fral10Programs.contains(existingStudentRecordEntity.getProgram());
        log.debug("isFRAL10: {}, isFRAL11: {}", isFRAL10, isFRAL11);
        if(isFRAL10 || isFRAL11 || otherFICourses || course.equalsIgnoreCase("FRALP 11") && StringUtils.isNotBlank(existingStudentRecordEntity.getProgram()) && existingStudentRecordEntity.getProgram().equalsIgnoreCase(EN_1996_CODE)) {
            List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();
            var frProgram = getOptionalProgramCode(optionalProgramCodes, "FI", existingStudentRecordEntity.getProgram());
            log.debug("Resolved French optional program: {}", frProgram);
            if(frProgram.isPresent() && !hasFrenchProgram(existingStudentRecordEntity.getStudentID(), frProgram.get().getOptionalProgramID())) {
                var entity = createStudentOptionalProgramEntity(frProgram.get().getOptionalProgramID(), existingStudentRecordEntity.getStudentID(), courseStudent.getCreateUser(), courseStudent.getUpdateUser());
                var savedEntity = studentOptionalProgramRepository.save(entity);
                log.debug("Added French optional program for studentID: {}, optionalProgramID: {}", existingStudentRecordEntity.getStudentID(), frProgram.get().getOptionalProgramID());
                historyService.createStudentOptionalProgramHistory(savedEntity, GDC_UPDATE);
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

        if(StringUtils.isNotBlank(newStudentCourseEntity.getFinalLetterGrade())
                && StringUtils.isNotBlank(newStudentCourseEntity.getFinalLetterGrade())
                && !newStudentCourseEntity.getFinalLetterGrade().equalsIgnoreCase(courseStudent.getFinalLetterGrade())) {
            newStudentCourseEntity.setFinalLetterGrade(courseStudent.getFinalLetterGrade());
        } else if(StringUtils.isBlank(newStudentCourseEntity.getFinalLetterGrade())) {
            newStudentCourseEntity.setFinalLetterGrade(mapLetterGrade(courseStudent.getFinalLetterGrade(), courseStudent.getFinalPercentage()));
        }

        if(relatedCourseRecord != null && newStudentCourseEntity.getRelatedCourseId() != null
                && !Objects.equals(newStudentCourseEntity.getRelatedCourseId(), new BigInteger(relatedCourseRecord.getCourseID()))) {
            newStudentCourseEntity.setRelatedCourseId(new BigInteger(relatedCourseRecord.getCourseID()));
        }

        if(StringUtils.isNotBlank(coregCoursesRecord.getGenericCourseType()) && coregCoursesRecord.getGenericCourseType().equalsIgnoreCase("G")) {
            newStudentCourseEntity.setCustomizedCourseName(courseStudent.getCourseDescription());
        }


        newStudentCourseEntity.setInterimPercent(mapPercentage(courseStudent.getInterimLetterGrade(), courseStudent.getInterimPercentage()));
        newStudentCourseEntity.setFinalPercent(mapPercentage(courseStudent.getFinalLetterGrade(), courseStudent.getFinalPercentage()));
        newStudentCourseEntity.setCredits(StringUtils.isNotBlank(courseStudent.getNumberOfCredits()) ? Integer.parseInt(courseStudent.getNumberOfCredits()) : 0);
        newStudentCourseEntity.setFineArtsAppliedSkills(fineArtsSkillsCode);
        newStudentCourseEntity.setEquivOrChallenge(equivalentOrChallengeCode);

        return newStudentCourseEntity;
    }

    private StudentCourseEntity createStudentCourseEntity(CourseStudentDetail courseStudent, String studentID, CoregCoursesRecord coregCoursesRecord, GraduationStudentRecordEntity existingStudentRecordEntity) {
        var relatedCourseRecord = StringUtils.isNotBlank(courseStudent.getRelatedCourse()) && StringUtils.isNotBlank(courseStudent.getRelatedLevel()) ?
                getCoregCoursesRecord(courseStudent.getRelatedCourse(), courseStudent.getRelatedLevel()) : null;

        log.debug("Creating student course entity for course: {}, level: {}, courseID: {}",
                courseStudent.getCourseCode(), courseStudent.getCourseLevel(), coregCoursesRecord != null ? coregCoursesRecord.getCourseID() : "N/A");

        var fineArtsSkillsCode = resolveFineArtsAppliedSkillsCode(courseStudent, coregCoursesRecord, existingStudentRecordEntity);

        log.debug("Resolved fine arts applied skills code: {}", fineArtsSkillsCode);

        var equivalentOrChallengeCode = StringUtils.isNotBlank(courseStudent.getCourseType()) ?
                equivalentOrChallengeCodeRepository.findById(courseStudent.getCourseType()).map(EquivalentOrChallengeCodeEntity::getEquivalentOrChallengeCode).orElse(null)
                : null;

        log.debug("Resolved equivalent or challenge code: {}", equivalentOrChallengeCode);
        return StudentCourseEntity
                .builder()
                .studentID(UUID.fromString(studentID))
                .courseID(new BigInteger(coregCoursesRecord.getCourseID()))
                .courseSession(courseStudent.getCourseYear() + courseStudent.getCourseMonth())
                .interimLetterGrade(mapLetterGrade(courseStudent.getInterimLetterGrade(), courseStudent.getInterimPercentage()))
                .interimPercent(mapPercentage(courseStudent.getInterimLetterGrade(), courseStudent.getInterimPercentage()))
                .finalLetterGrade(mapLetterGrade(courseStudent.getFinalLetterGrade(), courseStudent.getFinalPercentage()))
                .finalPercent(mapPercentage(courseStudent.getFinalLetterGrade(), courseStudent.getFinalPercentage()))
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
        var isGrade11 = StringUtils.startsWithIgnoreCase(courseStudent.getCourseLevel(), "11"); //some levels include letter like '11A'
        var is1996 = GradRequirementYearCodes.YEAR_1996.getCode().equalsIgnoreCase(gradRequirementYear);
        var is2004_2018_2023 = GradRequirementYearCodes.get2004_2018_2023Codes().stream().anyMatch(reqYear -> reqYear.equalsIgnoreCase(gradRequirementYear));
        var isBA = "BA".equalsIgnoreCase(catCode);
        var isLD = courseStudent.getCourseCode() != null && courseStudent.getCourseCode().startsWith("X");

        log.debug("Resolving fine arts applied skills code with gradRequirementYear: {}, catCode: {}, isGrade11: {}, is1996: {}, is2004_2018_2023: {}, isBA: {}, isLD: {}",
                gradRequirementYear, catCode, isGrade11, is1996, is2004_2018_2023, isBA, isLD);

        var shouldConsume = StringUtils.isNotBlank(courseStudent.getCourseGraduationRequirement())
                && isGrade11
                && ( (is1996 && (isBA || isLD)) || (is2004_2018_2023 && isBA) );

        log.debug("Should consume fine arts applied skills code: {}", shouldConsume);
        return shouldConsume
                ? fineArtsAppliedSkillsCodeRepository.findById(courseStudent.getCourseGraduationRequirement())
                .map(FineArtsAppliedSkillsCodeEntity::getFineArtsAppliedSkillsCode)
                .orElse(null)
                : null;
    }

    private String mapLetterGrade(String letterGrade, String percent) {
        List<LetterGrade> letterGradeList = courseCacheService.getLetterGradesFromCache();
        var doublePercent = percent != null ? Double.parseDouble(percent) : null;
        var percentageScrub = getPercentage(letterGrade, doublePercent);
        if(StringUtils.isBlank(letterGrade) && percentageScrub != null) {
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
            return letterEntity.isPresent() ? getPercentage(letterGrade, doublePercent) : null;
        } else {
            return getPercentage(letterGrade, doublePercent);
        }
    }
    
    private Double getPercentage(String letterGrade, Double percent) {
        if(percent != null && percent.compareTo(0.0) == 0 && (StringUtils.isBlank(letterGrade) || !letterGrade.equalsIgnoreCase("F"))) {
            return null;
        }
        return percent;
    }

    private CoregCoursesRecord getCoregCoursesRecord(String courseCode, String courseLevel) {
        String externalID = StringUtils.isEmpty(courseLevel) ? courseCode : String.format("%-5s", courseCode) + courseLevel;
        log.debug("getCoregCoursesRecord: externalID={}", externalID);
        return restUtils.getCoursesByExternalID(UUID.randomUUID(), externalID);
    }

    private List<StudentCareerProgramEntity> getCareerCodesForRemoval(UUID studentID, List<String> incomingCareerCodes, boolean isGraduated) {
        if (isGraduated) {
            return Collections.emptyList();
        }
        List<StudentCareerProgramEntity> existingCareerEntities = studentCareerProgramRepository.findByStudentID(studentID);

        List<StudentCareerProgramEntity> careerCodesToRemove = new ArrayList<>();
        for (StudentCareerProgramEntity existing : existingCareerEntities) {
            if (!incomingCareerCodes.contains(existing.getCareerProgramCode())) {
                careerCodesToRemove.add(existing);
            }
        }
        return careerCodesToRemove;
    }

    private List<StudentOptionalProgramEntity> getOptionalProgramForRemoval(UUID studentID, List<UUID> incomingProgramIDs, List<OptionalProgramCode> optionalProgramCodes, boolean isGraduated, String gradProgram) {
        if (isGraduated) {
            return Collections.emptyList();
        }
        List<StudentOptionalProgramEntity> existingPrograms = studentOptionalProgramRepository.findByStudentID(studentID);

        Set<UUID> protectedIds = OptionalProgramCodes.getProtectedCodes().stream()
                .map(code -> getOptionalProgramCode(optionalProgramCodes, code, gradProgram))
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

    private List<String> getCareerProgramToAdd(UUID studentID, List<String> careerCodes) {
        List<StudentCareerProgramEntity> careerEntities = studentCareerProgramRepository.findByStudentID(studentID);

        List<String> optionalCareerCodesToAdd = new ArrayList<>();
        careerCodes.forEach(code -> {
            if(careerEntities.stream().noneMatch(existingProgram -> existingProgram.getCareerProgramCode().equalsIgnoreCase(code))) {
                optionalCareerCodesToAdd.add(code);
            }
        });
        return optionalCareerCodesToAdd;
    }

    private Pair<Boolean, GraduationStudentRecordEntity> compareAndUpdateGraduationStudentRecordEntity(DemographicStudent demStudent, GraduationStudentRecordEntity newStudentRecordEntity, GradStudentUpdateResult gradStudentUpdateResult) {
        int projectedChangeCount = 0;
        int statusChangeCount = 0;
        boolean hasAdultChange = false;
        if(demStudent.getIsSummerCollection().equalsIgnoreCase("N")) {
            UUID originalSchoolOfRecordId = newStudentRecordEntity.getSchoolOfRecordId();
            if(newStudentRecordEntity.getSchoolOfRecordId() != null && !Objects.equals(newStudentRecordEntity.getSchoolOfRecordId(), UUID.fromString(demStudent.getSchoolID()))) {
                newStudentRecordEntity.setSchoolOfRecordId(UUID.fromString(demStudent.getSchoolID()));
                if(demStudent.getStudentStatus().equalsIgnoreCase("A") || demStudent.getStudentStatus().equalsIgnoreCase("T")) {
                    projectedChangeCount++;
                    statusChangeCount++;
                }
            }
            
            if(StringUtils.isNotBlank(newStudentRecordEntity.getStudentGrade())) {
                if (!newStudentRecordEntity.getStudentGrade().equalsIgnoreCase(demStudent.getGrade()) && demStudent.getStudentStatus().equalsIgnoreCase("A") && newStudentRecordEntity.getProgramCompletionDate() == null) {
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
            }

            var mappedStudentStatus = mapStudentStatusForUpdate(demStudent, newStudentRecordEntity, originalSchoolOfRecordId);
            if(!newStudentRecordEntity.getStudentStatus().equalsIgnoreCase(mappedStudentStatus)) {
                newStudentRecordEntity.setStudentStatus(mappedStudentStatus);
                projectedChangeCount++;
                statusChangeCount++;
            }

            if((StringUtils.isBlank(newStudentRecordEntity.getStudentCitizenship()) && StringUtils.isNotBlank(demStudent.getCitizenship()))  
                    ||  (StringUtils.isNotBlank(newStudentRecordEntity.getStudentCitizenship()) && StringUtils.isNotBlank(demStudent.getCitizenship()) && !newStudentRecordEntity.getStudentCitizenship().equalsIgnoreCase(demStudent.getCitizenship()))) {
                newStudentRecordEntity.setStudentCitizenship(demStudent.getCitizenship());
                projectedChangeCount++;
                statusChangeCount++;
                gradStudentUpdateResult.setCitizenshipUpdated(true);
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
            hasAdultChange = true;
        }

        if(projectedChangeCount > 0) {
            newStudentRecordEntity.setRecalculateProjectedGrad("Y");
        }

        if(statusChangeCount > 0) {
            newStudentRecordEntity.setRecalculateGradStatus("Y");
            newStudentRecordEntity.setRecalculateProjectedGrad("Y");
        }
        
        var hasUpdates = projectedChangeCount > 0 || statusChangeCount > 0 || hasAdultChange;
        return Pair.of(hasUpdates, newStudentRecordEntity);
    }

    private Optional<OptionalProgramCode> getOptionalProgramCode(List<OptionalProgramCode> optionalProgramCodes, String incomingProgramCode, String gradProgram) {
        var extractedProgramCode = extractProgramCode(incomingProgramCode);
        return  optionalProgramCodes
                .stream()
                .filter(program -> program.getOptProgramCode().equalsIgnoreCase(extractedProgramCode)
                        && StringUtils.isNotBlank(gradProgram)
                        && StringUtils.isNotBlank(program.getGraduationProgramCode())
                        && program.getGraduationProgramCode().equalsIgnoreCase(gradProgram)).findFirst();
    }

    private Optional<CareerProgramCode> getOptionalCareerCode(List<CareerProgramCode> optionalCareerCodes, String incomingProgramCode) {
        return  optionalCareerCodes
                .stream()
                .filter(program -> program.getCode().equalsIgnoreCase(incomingProgramCode)).findFirst();
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

    private StudentCareerProgramEntity createStudentCareerProgramEntity(String careerCode, UUID studentID, String createUser, String updateUser) {
        var entity =  StudentCareerProgramEntity
                .builder()
                .careerProgramCode(careerCode)
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
                .dob((StringUtils.isNotBlank(studentFromApi.getDob()) ? DateUtils.stringToLocalDateTime(DateTimeFormatter.ofPattern(DATE_TIME_FORMAT), studentFromApi.getDob()) : null))
                .genderCode(studentFromApi.getGenderCode())
                .legalFirstName(studentFromApi.getLegalFirstName())
                .legalMiddleNames(studentFromApi.getLegalMiddleNames())
                .legalLastName(studentFromApi.getLegalLastName())
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

    private void updateGradStatusFromStudentApi(StudentUpdate studentUpdate, GraduationStudentRecordEntity existingStudentRecordEntity) {
        var existingStatus = existingStudentRecordEntity.getStudentStatus();
        var incomingStatus = studentUpdate.getStatusCode();

        if("A".equals(incomingStatus) && gradActiveStatuses.contains(existingStatus.toUpperCase())){
            return;
        }
        if("A".equals(incomingStatus) && gradNonActiveStatuses.contains(existingStatus.toUpperCase())){
            String previousStatus = findPreviousActiveStatus(existingStudentRecordEntity.getStudentID(), existingStatus);
            existingStudentRecordEntity.setStudentStatus(previousStatus);
        } else {
            existingStudentRecordEntity.setStudentStatus(mapStudentStatus(incomingStatus));
        }
    }

    public String findPreviousActiveStatus(UUID studentID, String currentStatus) {
        List<GraduationStudentRecordHistoryEntity> allRecords = graduationStudentRecordHistoryRepository.findAllHistoryDescByStudentId(studentID);

        for (int i = 0; i < allRecords.size(); i++) {
            if (currentStatus.equalsIgnoreCase(allRecords.get(i).getStudentStatus())) {
                // Look through remaining history records for first active status
                for (int j = i + 1; j < allRecords.size(); j++) {
                    String status = allRecords.get(j).getStudentStatus();
                    if (gradActiveStatuses.contains(status.toUpperCase())) {
                        return status;
                    }
                }
                break;
            }
        }
        return CURRENT;
    }

    private String mapStudentStatus(String demStudentStatus) {
        if(demStudentStatus.equalsIgnoreCase("A")) {
            return CURRENT;
        } else if(demStudentStatus.equalsIgnoreCase("T")) {
            return TERMINATED;
        } else if(demStudentStatus.equalsIgnoreCase("D")) {
            return DECEASED;
        } else if(demStudentStatus.equalsIgnoreCase("M")) {
            return MERGED;
        } else {
            log.error("Invalid student status: {}", demStudentStatus);
            throw new IllegalArgumentException("Invalid student status: " + demStudentStatus);
        }
    }

    private String mapStudentStatusForUpdate(DemographicStudent demStudent, GraduationStudentRecordEntity graduationStudentRecordEntity, UUID originalSchoolOfRecordId) {
        String demStudentStatus = demStudent.getStudentStatus();
        if(demStudentStatus.equalsIgnoreCase("A")) {
            return CURRENT;
        } else if(demStudentStatus.equalsIgnoreCase("D")) {
            return DECEASED;
        } else if(demStudentStatus.equalsIgnoreCase("T")) {
            String currentGradStatus = graduationStudentRecordEntity.getStudentStatus();

            if (currentGradStatus.equalsIgnoreCase(TERMINATED)) {
                return TERMINATED;
            }
            if (currentGradStatus.equalsIgnoreCase(ARC)) {
                return ARC;
            }
            if (currentGradStatus.equalsIgnoreCase(CURRENT)) {
                boolean isSchoolOfRecord = Objects.equals(UUID.fromString(demStudent.getSchoolID()), originalSchoolOfRecordId);
                if (isSchoolOfRecord) {
                    return TERMINATED;
                } else {
                    return CURRENT;
                }
            }
            log.error("Unable to map student status for update. Reported status: {}, Current GRAD status: {}", demStudentStatus, currentGradStatus);
            throw new IllegalArgumentException("Unable to map student status for update. Reported status: " + demStudentStatus + ", Current GRAD status: " + currentGradStatus);
        } else {
            log.error("Invalid student status: {}", demStudentStatus);
            throw new IllegalArgumentException("Invalid student status: " + demStudentStatus);
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

    private String createProgram(String schoolReportingRequirementCode) {
        List<GraduationProgramCode> codes =  restUtils.getGraduationProgramCodeList(true);
        var filteredCodes = codes.stream().filter(code -> !code.getProgramCode().equalsIgnoreCase("1950") && !code.getProgramCode().equalsIgnoreCase("SCCP") && !code.getProgramCode().equalsIgnoreCase("NOPROG")).findFirst();
        var code = filteredCodes.orElseThrow(() ->new EntityNotFoundException(GraduationProgramCode.class, "Program Code", "Program Code not found"));
        var splitProgramCode = code.getProgramCode().split("-");
        var appendCode = schoolReportingRequirementCode.equalsIgnoreCase("CSF") ? "PF" : "EN";
        return splitProgramCode.length == 2 ? splitProgramCode[0] + "-" + appendCode : code.getProgramCode();
    }

    private boolean checkIfSchoolOfRecordIsUpdated(DemographicStudent demStudent, GraduationStudentRecordEntity existingStudentRecordEntity) {
        return existingStudentRecordEntity.getSchoolOfRecordId() != null
                && !Objects.equals(existingStudentRecordEntity.getSchoolOfRecordId(), UUID.fromString(demStudent.getSchoolID()));
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
