package ca.bc.gov.educ.api.gradstudent.service.event;

import ca.bc.gov.educ.api.gradstudent.model.dto.LetterGrade;
import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.CourseStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.DemographicStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.GraduationProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.OptionalProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import ca.bc.gov.educ.api.gradstudent.service.HistoryService;
import org.apache.commons.lang3.StringUtils;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class GraduationStudentRecordService {
    private final RestUtils restUtils;
    private final GraduationStudentRecordRepository graduationStudentRecordRepository;
    private final StudentOptionalProgramRepository studentOptionalProgramRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final FineArtsAppliedSkillsCodeRepository fineArtsAppliedSkillsCodeRepository;
    private final EquivalentOrChallengeCodeRepository equivalentOrChallengeCodeRepository;
    private static final String DATA_CONVERSION_HISTORY_ACTIVITY_CODE = "DATACONVERT"; // confirm,
    private static final String ADD_ONGOING_HISTORY_ACTIVITY_CODE = "TRAXADD";// confirm,
    private final HistoryService historyService;
    public static final String CURRENT = "CUR";
    public static final String TERMINATED = "TER";
    public static final String DECEASED = "DEC";
    public static final String CREATE_USER = "createUser";
    public static final String CREATE_DATE = "createDate";
    public static final String YYYY_MM_DD = "uuuuMMdd";
    public final List<String> FRAL10_PROGRAMS = Arrays.asList("2023-EN", "2018-EN", "2004-EN");
    public final List<String> FRAL11_PROGRAMS = Arrays.asList("1996-EN", "1986-EN");


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
    public void createNewStudentRecord(DemographicStudent demStudent, Student studentFromApi) {
        List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();
        List<StudentOptionalProgramEntity> optionalProgramEntities = new ArrayList<>();
        GraduationStudentRecordEntity entity = createGraduationStudentRecordEntity(demStudent, studentFromApi);
        entity.setCreateUser(demStudent.getCreateUser());
        entity.setUpdateUser(demStudent.getUpdateUser());
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateDate(LocalDateTime.now());
        var savedStudentRecord = graduationStudentRecordRepository.save(entity);
        historyService.createStudentHistory(savedStudentRecord, ADD_ONGOING_HISTORY_ACTIVITY_CODE);

        List<UUID> incomingProgramIDs = getOptionalProgramIDForIncomingPrograms(demStudent, optionalProgramCodes);
        incomingProgramIDs.forEach(programID -> optionalProgramEntities.add(createStudentOptionalProgramEntity(programID, savedStudentRecord.getStudentID(), demStudent.getCreateUser(), demStudent.getUpdateUser())));

        if(StringUtils.isNotBlank(savedStudentRecord.getProgram()) && savedStudentRecord.getProgram().equalsIgnoreCase("SSCP") && savedStudentRecord.getProgramCompletionDate() != null && demStudent.getSchoolReportingRequirementCode().equalsIgnoreCase("CSF")) {
            var frProgram = getOptionalProgramCode(optionalProgramCodes, "FR");
            frProgram.ifPresent(optionalProgramCode -> optionalProgramEntities.add(createStudentOptionalProgramEntity(optionalProgramCode.getOptionalProgramID(), savedStudentRecord.getStudentID(), demStudent.getCreateUser(), demStudent.getUpdateUser())));
        }
        var savedEntities = studentOptionalProgramRepository.saveAll(optionalProgramEntities);
        savedEntities.forEach(optEntity -> historyService.createStudentOptionalProgramHistory(optEntity, DATA_CONVERSION_HISTORY_ACTIVITY_CODE));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void updateStudentRecord(DemographicStudent demStudent, Student studentFromApi, GraduationStudentRecordEntity existingStudentRecordEntity) {
        var newStudentRecordEntity = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(existingStudentRecordEntity, newStudentRecordEntity, CREATE_USER, CREATE_DATE);
        GraduationStudentRecordEntity updatedEntity = compareAndUpdateGraduationStudentRecordEntity(demStudent, newStudentRecordEntity);
        updatedEntity.setUpdateUser(demStudent.getUpdateUser());
        updatedEntity.setUpdateDate(LocalDateTime.now());
        var savedStudentRecord = graduationStudentRecordRepository.save(updatedEntity);

        List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();
        List<UUID> incomingProgramIDs = getOptionalProgramIDForIncomingPrograms(demStudent, optionalProgramCodes);
        var optionalProgramsToRemove = getOptionalProgramForRemoval(UUID.fromString(studentFromApi.getStudentID()), incomingProgramIDs, optionalProgramCodes);
        if(!optionalProgramsToRemove.isEmpty()) {
            studentOptionalProgramRepository.deleteAll(optionalProgramsToRemove);
        }

        List<UUID> programIDsToAdd = getOptionalProgramToAdd(UUID.fromString(studentFromApi.getStudentID()), incomingProgramIDs);

        List<StudentOptionalProgramEntity> optionalProgramEntities = new ArrayList<>();
        programIDsToAdd.forEach(programID -> optionalProgramEntities.add(createStudentOptionalProgramEntity(programID, savedStudentRecord.getStudentID(), demStudent.getCreateUser(), demStudent.getUpdateUser())));
        var savedEntities = studentOptionalProgramRepository.saveAll(optionalProgramEntities);
        savedEntities.forEach(optEntity -> historyService.createStudentOptionalProgramHistory(optEntity, DATA_CONVERSION_HISTORY_ACTIVITY_CODE));
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void handleStudentCourseRecord(GraduationStudentRecordEntity existingStudentRecordEntity, CourseStudent courseStudent, Student studentFromApi) {
        boolean hasGraduated = existingStudentRecordEntity.getProgramCompletionDate() != null;
        if(courseStudent.getSubmissionModeCode().equalsIgnoreCase("APPEND") || courseStudent.getIsSummerCollection().equalsIgnoreCase("Y") || hasGraduated) {
            handleAppendCourseRecord(existingStudentRecordEntity, courseStudent, studentFromApi.getStudentID());
        } else {
            //TODO: replace
        }
    }

    private void handleAppendCourseRecord(GraduationStudentRecordEntity existingStudentRecordEntity, CourseStudent courseStudent, String studentID) {
        List<StudentCourseEntity> existingStudentCourses =  studentCourseRepository.findByStudentID(UUID.fromString(studentID));
        var coursesRecord = getCoregCoursesRecord(courseStudent.getCourseCode(), courseStudent.getCourseLevel());
        var matchingCourseRecord = existingStudentCourses.stream().filter(course -> Objects.equals(course.getCourseID(), new BigInteger(coursesRecord.getCourseID())) && course.getCourseSession().equalsIgnoreCase(courseStudent.getCourseYear() + courseStudent.getCourseMonth())).findFirst();
        if(matchingCourseRecord.isPresent() && courseStudent.getCourseStatus().equalsIgnoreCase("W")) {
            studentCourseRepository.delete(matchingCourseRecord.get());
        } else if(matchingCourseRecord.isPresent() && courseStudent.getCourseStatus().equalsIgnoreCase("A")) {
            var newStudentCourseEntity = new StudentCourseEntity();
            BeanUtils.copyProperties(matchingCourseRecord.get(), newStudentCourseEntity, CREATE_USER, CREATE_DATE);
            StudentCourseEntity updatedEntity = compareAndupdateStudentCourseEntity(newStudentCourseEntity, courseStudent, coursesRecord);
            updatedEntity.setCreateUser(courseStudent.getCreateUser());
            updatedEntity.setUpdateUser(courseStudent.getUpdateUser());
            updatedEntity.setCreateDate(LocalDateTime.now());
            updatedEntity.setUpdateDate(LocalDateTime.now());
            studentCourseRepository.save(updatedEntity);
        } else {
            StudentCourseEntity studentCourseEntity = createStudentCourseEntity(courseStudent, studentID, coursesRecord);
            studentCourseEntity.setCreateUser(courseStudent.getCreateUser());
            studentCourseEntity.setUpdateUser(courseStudent.getUpdateUser());
            studentCourseEntity.setCreateDate(LocalDateTime.now());
            studentCourseEntity.setUpdateDate(LocalDateTime.now());
            studentCourseRepository.save(studentCourseEntity);

            String course = StringUtils.isEmpty(courseStudent.getCourseLevel()) ? courseStudent.getCourseCode() : String.format("%-5s", courseStudent.getCourseCode()) + courseStudent.getCourseLevel();
            boolean isFRAL10 = (course.equalsIgnoreCase("FRAL 10") || course.equalsIgnoreCase("FRALP 10")) && FRAL10_PROGRAMS.contains(existingStudentRecordEntity.getProgram());
            boolean isFRAL11 = course.equalsIgnoreCase("FRAL 11") && FRAL11_PROGRAMS.contains(existingStudentRecordEntity.getProgram());

            if(isFRAL10 || isFRAL11 || course.equalsIgnoreCase("FRALP 11") && existingStudentRecordEntity.getProgram().equalsIgnoreCase("1996-EN")) {
                List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();
                var frProgram = getOptionalProgramCode(optionalProgramCodes, "FR");
                if(frProgram.isPresent()) {
                    var entity = createStudentOptionalProgramEntity(frProgram.get().getOptionalProgramID(), existingStudentRecordEntity.getStudentID(), courseStudent.getCreateUser(), courseStudent.getUpdateUser());
                    var savedEntity = studentOptionalProgramRepository.save(entity);
                    historyService.createStudentOptionalProgramHistory(savedEntity, DATA_CONVERSION_HISTORY_ACTIVITY_CODE);
                }
            }
        }

        existingStudentRecordEntity.setRecalculateProjectedGrad("Y");
        existingStudentRecordEntity.setRecalculateGradStatus("Y");
        graduationStudentRecordRepository.save(existingStudentRecordEntity);
    }

    private StudentCourseEntity compareAndupdateStudentCourseEntity(StudentCourseEntity newStudentCourseEntity, CourseStudent courseStudent, CoregCoursesRecord coregCoursesRecord) {
        var relatedCourseRecord = StringUtils.isNotBlank(courseStudent.getRelatedCourse()) && StringUtils.isNotBlank(courseStudent.getRelatedLevel()) ?
                getCoregCoursesRecord(courseStudent.getRelatedCourse(), courseStudent.getRelatedLevel()) : null;
        var fineArtsSkillsCode = StringUtils.isNotBlank(courseStudent.getCourseType()) ?
                fineArtsAppliedSkillsCodeRepository.findById(courseStudent.getCourseType()).map(FineArtsAppliedSkillsCodeEntity::getFineArtsAppliedSkillsCode).orElse(null)
                : null;
        var equivalentOrChallengeCode = StringUtils.isNotBlank(courseStudent.getCourseGraduationRequirement()) ?
                equivalentOrChallengeCodeRepository.findById(courseStudent.getCourseGraduationRequirement()).map(EquivalentOrChallengeCodeEntity::getEquivalentOrChallengeCode).orElse(null)
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

        newStudentCourseEntity.setFineArtsAppliedSkills(fineArtsSkillsCode);
        newStudentCourseEntity.setEquivOrChallenge(equivalentOrChallengeCode);

        return newStudentCourseEntity;
    }

    private StudentCourseEntity createStudentCourseEntity(CourseStudent courseStudent, String studentID, CoregCoursesRecord coregCoursesRecord) {
        var relatedCourseRecord = StringUtils.isNotBlank(courseStudent.getRelatedCourse()) && StringUtils.isNotBlank(courseStudent.getRelatedLevel()) ?
                getCoregCoursesRecord(courseStudent.getRelatedCourse(), courseStudent.getRelatedLevel()) : null;
        var fineArtsSkillsCode = StringUtils.isNotBlank(courseStudent.getCourseType()) ?
                fineArtsAppliedSkillsCodeRepository.findById(courseStudent.getCourseType()).map(FineArtsAppliedSkillsCodeEntity::getFineArtsAppliedSkillsCode).orElse(null)
                : null;
        var equivalentOrChallengeCode = StringUtils.isNotBlank(courseStudent.getCourseGraduationRequirement()) ?
                equivalentOrChallengeCodeRepository.findById(courseStudent.getCourseGraduationRequirement()).map(EquivalentOrChallengeCodeEntity::getEquivalentOrChallengeCode).orElse(null)
                : null;
        return StudentCourseEntity
                .builder()
                .studentID(UUID.fromString(studentID))
                .courseID(new BigInteger(coregCoursesRecord.getCourseID()))
                .courseSession(courseStudent.getCourseYear() + courseStudent.getCourseMonth())
                .interimLetterGrade(mapLetterGrade(courseStudent.getInterimLetterGrade(), courseStudent.getInterimPercentage()))
                .completedCourseLetterGrade(mapLetterGrade(courseStudent.getFinalLetterGrade(), courseStudent.getFinalPercentage()))
                .relatedCourseId(relatedCourseRecord != null ? new BigInteger(relatedCourseRecord.getCourseID()) : null)
                .customizedCourseName(StringUtils.isNotBlank(coregCoursesRecord.getGenericCourseType()) && coregCoursesRecord.getGenericCourseType().equalsIgnoreCase("G") ? courseStudent.getCourseDescription() : null)
                .fineArtsAppliedSkills(fineArtsSkillsCode)
                .equivOrChallenge(equivalentOrChallengeCode)
                .build();
    }

    private String mapLetterGrade(String letterGrade, String percent) {
        List<LetterGrade> letterGradeList = restUtils.getLetterGradeList();
        if(StringUtils.isBlank(letterGrade) && StringUtils.isNotBlank(percent)) {
            var letterEntity =  letterGradeList.stream().filter(grade -> grade.getPercentRangeHigh() >= Integer.parseInt(percent)
                    && grade.getPercentRangeLow() <= Integer.parseInt(percent)).findFirst();
            return letterEntity.map(LetterGrade::getGrade).orElse(null);
        } else {
            return letterGrade;
        }
    }

    private CoregCoursesRecord getCoregCoursesRecord(String courseCode, String courseLevel) {
        String externalID = StringUtils.isEmpty(courseLevel) ? courseCode : String.format("%-5s", courseCode) + courseLevel;
        return restUtils.getCoursesByExternalID(UUID.randomUUID(), externalID);
    }

    private List<StudentOptionalProgramEntity> getOptionalProgramForRemoval(UUID studentID, List<UUID> incomingProgramIDs, List<OptionalProgramCode> optionalProgramCodes) {
        List<StudentOptionalProgramEntity> existingPrograms = studentOptionalProgramRepository.findByStudentID(studentID);

        var programCodeFREntity = getOptionalProgramCode(optionalProgramCodes, "FR");
        var programCodeFIEntity = getOptionalProgramCode(optionalProgramCodes, "FI");

        List<StudentOptionalProgramEntity> optionalProgramsToRemove = new ArrayList<>();
        existingPrograms.forEach(existingProgram -> {
            if(!incomingProgramIDs.contains(existingProgram.getOptionalProgramID()) &&
                    (existingProgram.getOptionalProgramID() != programCodeFREntity.get().getOptionalProgramID()
                    || existingProgram.getOptionalProgramID() != programCodeFIEntity.get().getOptionalProgramID())) {
                optionalProgramsToRemove.add(existingProgram);
            }
        });
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

        var mappedProgram = mapGradProgramCode(demStudent.getGradRequirementYear(), demStudent.getSchoolReportingRequirementCode());
        if(!newStudentRecordEntity.getProgram().equalsIgnoreCase(mappedProgram)) {
            newStudentRecordEntity.setProgram(mappedProgram);
            projectedChangeCount++;
            statusChangeCount++;
        }

        if(demStudent.getGradRequirementYear().equalsIgnoreCase("SSCP")) {
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
                .program(demStudent.getIsSummerCollection().equalsIgnoreCase("N") ? mapGradProgramCode(demStudent.getGradRequirementYear(), demStudent.getSchoolReportingRequirementCode()) : createProgram())
                .adultStartDate(demStudent.getIsSummerCollection().equalsIgnoreCase("N") ? mapAdultStartDate(demStudent.getBirthdate(), demStudent.getGradRequirementYear()): null)
                .build();
    }

    private Date mapAdultStartDate(String birthdate, String gradYear) {
        if(gradYear.equalsIgnoreCase("1950")) {
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
        var filteredCodes = codes.stream().filter(code -> code.getProgramCode().equalsIgnoreCase("1950") || code.getProgramCode().equalsIgnoreCase("SSCP") || code.getProgramCode().equalsIgnoreCase("NOPROG")).findFirst();
        if(filteredCodes.isPresent()) {
            var code = filteredCodes.get().getProgramCode().split("-");
            return code.length == 2 ? code[0] + "-" + "EN" : null;
        }
        return null;
    }
}
