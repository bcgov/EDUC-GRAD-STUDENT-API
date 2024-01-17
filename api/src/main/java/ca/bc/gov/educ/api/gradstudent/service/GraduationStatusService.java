package ca.bc.gov.educ.api.gradstudent.service;


import ca.bc.gov.educ.api.gradstudent.constant.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.constant.Generated;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentCareerProgramTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentOptionalProgramTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.StudentNonGradReasonTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.gradstudent.constant.EventStatus.DB_COMMITTED;

@Service
public class GraduationStatusService {

    public static final int PAGE_SIZE = 500;

    private static final Logger logger = LoggerFactory.getLogger(GraduationStatusService.class);

    private static final String CREATE_USER = "createUser";
    private static final String CREATE_DATE = "createDate";
    private static final String UPDATE_USER = "updateUser";
    private static final String UPDATE_DATE = "updateDate";
    private static final String GRAD_ALG = "GRADALG";
    private static final String USER_EDIT = "USEREDIT";
    private static final String USER_CREATE = "USEREDIT";
    private static final String USER_DELETE = "USERDELETE";
    private static final String USER_UNDO_CMPL = "USERUNDOCMPL";

    final
    WebClient webClient;

    final GraduationStudentRecordRepository graduationStatusRepository;
    final StudentStatusRepository studentStatusRepository;
    final GradStatusEventRepository gradStatusEventRepository;
    final GraduationStatusTransformer graduationStatusTransformer;
    final StudentOptionalProgramRepository gradStudentOptionalProgramRepository;
    final GradStudentOptionalProgramTransformer gradStudentOptionalProgramTransformer;
    final StudentCareerProgramRepository gradStudentCareerProgramRepository;
    final GradStudentCareerProgramTransformer gradStudentCareerProgramTransformer;
    final StudentNonGradReasonRepository studentNonGradReasonRepository;
    final StudentNonGradReasonTransformer studentNonGradReasonTransformer;

    final GradStudentService gradStudentService;
    final HistoryService historyService;
    final GradValidation validation;
    final EducGradStudentApiConstants constants;

    @Autowired
    GraduationStudentRecordSearchRepository graduationStudentRecordSearchRepository;

    @Autowired
    public GraduationStatusService(WebClient webClient, GraduationStudentRecordRepository graduationStatusRepository, StudentStatusRepository studentStatusRepository, GradStatusEventRepository gradStatusEventRepository, StudentNonGradReasonRepository studentNonGradReasonRepository, GraduationStatusTransformer graduationStatusTransformer, StudentOptionalProgramRepository gradStudentOptionalProgramRepository, GradStudentOptionalProgramTransformer gradStudentOptionalProgramTransformer, StudentCareerProgramRepository gradStudentCareerProgramRepository, GradStudentCareerProgramTransformer gradStudentCareerProgramTransformer, StudentNonGradReasonTransformer studentNonGradReasonTransformer, GradStudentService gradStudentService, HistoryService historyService, GradValidation validation, EducGradStudentApiConstants constants) {
        this.webClient = webClient;
        this.graduationStatusRepository = graduationStatusRepository;
        this.studentStatusRepository = studentStatusRepository;
        this.gradStatusEventRepository = gradStatusEventRepository;
        this.graduationStatusTransformer = graduationStatusTransformer;
        this.gradStudentOptionalProgramRepository = gradStudentOptionalProgramRepository;
        this.gradStudentOptionalProgramTransformer = gradStudentOptionalProgramTransformer;
        this.gradStudentCareerProgramRepository = gradStudentCareerProgramRepository;
        this.gradStudentCareerProgramTransformer = gradStudentCareerProgramTransformer;
        this.studentNonGradReasonRepository = studentNonGradReasonRepository;
        this.studentNonGradReasonTransformer = studentNonGradReasonTransformer;

        this.gradStudentService = gradStudentService;
        this.historyService = historyService;
        this.validation = validation;
        this.constants = constants;
    }

    @Retry(name = "generalgetcall")
    public GraduationStudentRecord getGraduationStatusForAlgorithm(UUID studentID) {
        logger.debug("getGraduationStatusForAlgorithm");
        Optional<GraduationStudentRecordEntity> responseOptional = graduationStatusRepository.findById(studentID);
        return responseOptional.map(gs -> {
            GraduationStudentRecord gradStatus = graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(gs);
            List<StudentCareerProgramEntity> studentCareerProgramEntities = gradStudentCareerProgramRepository.findByStudentID(studentID);
            gradStatus.setCareerPrograms(gradStudentCareerProgramTransformer.transformToDTO(studentCareerProgramEntities));
            return gradStatus;
        }).orElse(null);
    }

    /**
     * Returns true if student has a valid program completion date
     * @param studentID
     * @return
     */
    public boolean hasStudentGraduated(UUID studentID) throws EntityNotFoundException {
        Optional<GraduationStudentRecordEntity> responseOptional = graduationStatusRepository.findById(studentID);
        if(responseOptional.isPresent()){
            GraduationStudentRecordEntity graduationStudentRecord = responseOptional.get();
            return graduationStudentRecord.getProgramCompletionDate() != null;
        }
        throw new EntityNotFoundException(String.format("Student with ID: {} not found", studentID));
    }

    @Retry(name = "generalgetcall")
    public GraduationStudentRecord getGraduationStatus(UUID studentID, String accessToken) {
        logger.debug("getGraduationStatus");
        Optional<GraduationStudentRecordEntity> responseOptional = graduationStatusRepository.findById(studentID);
        if (responseOptional.isPresent()) {
            GraduationStudentRecord gradStatus = graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(responseOptional.get());
            if (gradStatus.getProgram() != null) {
                gradStatus.setProgramName(getProgramName(gradStatus.getProgram(), accessToken));
            }
            if (gradStatus.getSchoolOfRecord() != null)
                gradStatus.setSchoolName(getSchoolName(gradStatus.getSchoolOfRecord(), accessToken));

            if (gradStatus.getStudentStatus() != null) {
                Optional<StudentStatusEntity> statusEntity = studentStatusRepository.findById(StringUtils.toRootUpperCase(gradStatus.getStudentStatus()));
                statusEntity.ifPresent(studentStatusEntity -> gradStatus.setStudentStatusName(studentStatusEntity.getLabel()));
            }
            if (gradStatus.getSchoolAtGrad() != null)
                gradStatus.setSchoolAtGradName(getSchoolName(gradStatus.getSchoolAtGrad(), accessToken));

            List<StudentCareerProgramEntity> studentCareerProgramEntities = gradStudentCareerProgramRepository.findByStudentID(studentID);
            gradStatus.setCareerPrograms(gradStudentCareerProgramTransformer.transformToDTO(studentCareerProgramEntities));
            List<StudentOptionalProgram> studentOptionalPrograms = getStudentGradOptionalProgram(studentID, accessToken);
            gradStatus.setOptionalPrograms(studentOptionalPrograms);
            return gradStatus;
        } else {
            return null;
        }

    }

    public GraduationStudentRecord getGraduationStatus(UUID studentID) throws EntityNotFoundException {
        Optional<GraduationStudentRecordEntity> responseOptional = graduationStatusRepository.findById(studentID);
        if (responseOptional.isPresent()) {
            return graduationStatusTransformer.transformToDTO(responseOptional.get());
        }
        throw new EntityNotFoundException(String.format("Student with ID: %s not found", studentID));
    }

    @Transactional
    @Retry(name = "generalpostcall")
    public Pair<GraduationStudentRecord, GradStatusEvent> saveGraduationStatus(UUID studentID, GraduationStudentRecord graduationStatus, Long batchId, String accessToken) throws JsonProcessingException {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        GraduationStudentRecordEntity sourceObject = graduationStatusTransformer.transformToEntity(graduationStatus);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            BeanUtils.copyProperties(sourceObject, gradEntity, CREATE_USER, CREATE_DATE, "recalculateGradStatus", "recalculateProjectedGrad", "programCompletionDate", "studentProjectedGradData");
            gradEntity.setBatchId(batchId);
            if(!gradEntity.getProgram().equalsIgnoreCase("SCCP") && !gradEntity.getProgram().equalsIgnoreCase("NOPROG")) {
                gradEntity.setProgramCompletionDate(sourceObject.getProgramCompletionDate());
            }
            if (sourceObject.getStudentProjectedGradData() != null) {
                gradEntity.setStudentProjectedGradData(sourceObject.getStudentProjectedGradData());
            }
            if(batchId != null) {
                resetBatchFlags(gradEntity, false);
            }

            gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
            historyService.createStudentHistory(gradEntity, GRAD_ALG);
            final GradStatusEvent gradStatusEvent = createGradStatusEvent(gradEntity.getUpdateUser(), gradEntity,
                    EventType.GRAD_STUDENT_GRADUATED, EventOutcome.GRAD_STATUS_UPDATED, GRAD_ALG, accessToken);

            if (gradStatusEvent != null) {
                gradStatusEventRepository.save(gradStatusEvent);
            }
            return Pair.of(graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(gradEntity), gradStatusEvent);
        } else {
            sourceObject = graduationStatusRepository.saveAndFlush(sourceObject);
            final GradStatusEvent gradStatusEvent = createGradStatusEvent(sourceObject.getUpdateUser(), sourceObject,
                    EventType.GRAD_STUDENT_GRADUATED, EventOutcome.GRAD_STATUS_UPDATED, GRAD_ALG, accessToken);
            if (gradStatusEvent != null) {
                gradStatusEventRepository.save(gradStatusEvent);
            }
            return Pair.of(graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(sourceObject), gradStatusEvent);
        }
    }

    @Transactional
    @Retry(name = "generalpostcall")
    public Pair<GraduationStudentRecord, GradStatusEvent> updateGraduationStatus(UUID studentID, GraduationStudentRecord graduationStatus, String accessToken) throws JsonProcessingException {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        GraduationStudentRecordEntity sourceObject = graduationStatusTransformer.transformToEntity(graduationStatus);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            ValidateDataResult hasDataChanged = validateData(sourceObject, gradEntity, accessToken);
            if (validation.hasErrors()) {
                validation.stopOnErrors();
                return Pair.of(new GraduationStudentRecord(), null);
            }
            if(hasDataChanged.hasDataChanged() && !sourceObject.getProgram().equalsIgnoreCase(gradEntity.getProgram())) {
                deleteStudentOptionalPrograms(sourceObject.getStudentID());
                deleteStudentCareerPrograms(sourceObject.getStudentID());
                if(gradEntity.getProgram().equalsIgnoreCase("SCCP")) {
                    sourceObject.setRecalculateGradStatus("Y");
                    sourceObject.setRecalculateProjectedGrad("Y");
                    sourceObject.setStudentGradData(null);
                    sourceObject.setProgramCompletionDate(null);
                    sourceObject.setHonoursStanding(null);
                    sourceObject.setGpa(null);
                    sourceObject.setSchoolAtGrad(null);
                    archiveStudentAchievements(sourceObject.getStudentID(),accessToken);
                } else {
                    deleteStudentAchievements(sourceObject.getStudentID(), accessToken);
                }
            }

            if (hasDataChanged.hasDataChanged()) {
                gradEntity.setRecalculateGradStatus(hasDataChanged.getRecalculateGradStatus());
                gradEntity.setRecalculateProjectedGrad(hasDataChanged.getRecalculateProgectedGrad());
            }

            if(!hasDataChanged.hasDataChanged()) {
                if ("".equals(sourceObject.getRecalculateGradStatus()) || "N".equalsIgnoreCase(sourceObject.getRecalculateGradStatus())) {
                    gradEntity.setRecalculateGradStatus(null);
                } else {
                    gradEntity.setRecalculateGradStatus(sourceObject.getRecalculateGradStatus());
                }
                if ("".equals(sourceObject.getRecalculateProjectedGrad()) || "N".equalsIgnoreCase(sourceObject.getRecalculateProjectedGrad())) {
                    gradEntity.setRecalculateProjectedGrad(null);
                } else {
                    gradEntity.setRecalculateProjectedGrad(sourceObject.getRecalculateProjectedGrad());
                }
            }

            BeanUtils.copyProperties(sourceObject, gradEntity, CREATE_USER, CREATE_DATE, "studentGradData", "studentProjectedGradData", "recalculateGradStatus", "recalculateProjectedGrad");
            gradEntity.setProgramCompletionDate(sourceObject.getProgramCompletionDate());
            gradEntity.setUpdateUser(null);
            gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
            historyService.createStudentHistory(gradEntity, USER_EDIT);
            final GradStatusEvent gradStatusEvent = createGradStatusEvent(gradEntity.getUpdateUser(), gradEntity,
                    EventType.GRAD_STUDENT_UPDATED, EventOutcome.GRAD_STATUS_UPDATED, USER_EDIT, accessToken);
            if (gradStatusEvent != null) {
                gradStatusEventRepository.save(gradStatusEvent);
            }
            return Pair.of(graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(gradEntity), gradStatusEvent);
        } else {
            validation.addErrorAndStop(String.format("Student ID [%s] does not exists", studentID));
            return Pair.of(graduationStatus, null);
        }
    }

    @Generated
    @Transactional
    public GraduationStudentRecordSearchResult searchGraduationStudentRecords(final StudentSearchRequest searchRequest, final String accessToken) {
        final GraduationStudentRecordSearchResult searchResult = new GraduationStudentRecordSearchResult();

        List<String> studentIds = new ArrayList<>();
        if(searchRequest.getPens() != null && !searchRequest.getPens().isEmpty()) {
            for(String pen: searchRequest.getPens()) {
                List<GradSearchStudent> students = gradStudentService.getStudentByPenFromStudentAPI(pen, accessToken);
                for(GradSearchStudent st: students) {
                    if(Boolean.TRUE.equals(searchRequest.getValidateInput())) {
                        var gradStudent = graduationStatusRepository.findByStudentID(UUID.fromString(st.getStudentID()));
                        if (gradStudent == null) {
                            searchResult.addError(GraduationStudentRecordSearchResult.PEN_VALIDATION_ERROR, st.getPen());
                            continue;
                        }
                    }
                    if(!"MER".equalsIgnoreCase(st.getStudentStatus())) {
                        studentIds.add(st.getStudentID());
                        switch(st.getStudentStatus()) {
                            case "ARC": {
                                String errorString = String.format(GraduationStudentRecordSearchResult.STUDENT_STATUS_VALIDATION_WARNING, "ARC");
                                searchResult.addError(errorString, st.getPen());
                            }
                                break;
                            case "TER": {
                                String errorString = String.format(GraduationStudentRecordSearchResult.STUDENT_STATUS_VALIDATION_WARNING, "TER");
                                searchResult.addError(errorString, st.getPen());
                            }
                                break;
                            case "DEC": {
                                String errorString = String.format(GraduationStudentRecordSearchResult.STUDENT_STATUS_VALIDATION_WARNING, "DEC");
                                searchResult.addError(errorString, st.getPen());
                            }
                                break;
                            default:
                                break;
                        }
                    } else {
                        String errorString = String.format(GraduationStudentRecordSearchResult.STUDENT_STATUS_VALIDATION_ERROR, "MER");
                        searchResult.addError(errorString, st.getPen());
                    }
                }
            }
        }

        if(searchRequest.getSchoolOfRecords() != null && !searchRequest.getSchoolOfRecords().isEmpty() && Boolean.TRUE.equals(searchRequest.getValidateInput())) {
            for(String schoolOfRecord: searchRequest.getSchoolOfRecords()) {
                String schoolName = getSchoolName(schoolOfRecord, accessToken);
                if(schoolName == null) {
                    searchResult.addError(GraduationStudentRecordSearchResult.MINCODE_VALIDATION_ERROR, schoolOfRecord);
                }
            }
        }

        if(searchRequest.getDistricts() != null && !searchRequest.getDistricts().isEmpty() && Boolean.TRUE.equals(searchRequest.getValidateInput())) {
            for(String district: searchRequest.getDistricts()) {
                String districtName = getDistrictName(district, accessToken);
                if(districtName == null) {
                    searchResult.addError(GraduationStudentRecordSearchResult.DISTRICT_VALIDATION_ERROR, district);
                }
            }
        }

        if(searchRequest.getPrograms() != null && !searchRequest.getPrograms().isEmpty() && Boolean.TRUE.equals(searchRequest.getValidateInput())) {
            for(String program: searchRequest.getPrograms()) {
                String programName = getProgramName(program, accessToken);
                if(programName == null) {
                    searchResult.addError(GraduationStudentRecordSearchResult.PROGRAM_VALIDATION_ERROR, null);
                }
            }
        }

        if(searchRequest.getDistricts() != null && !searchRequest.getDistricts().isEmpty()) {
            List<CommonSchool> schools = new ArrayList<>(getSchools(accessToken));
            for(Iterator<CommonSchool> it = schools.iterator(); it.hasNext();) {
                CommonSchool school = it.next();
                if(!searchRequest.getDistricts().contains(school.getDistNo())) {
                    it.remove();
                } else {
                    if(searchRequest.getSchoolCategoryCodes() != null && !searchRequest.getSchoolCategoryCodes().isEmpty()) {
                        if(!searchRequest.getSchoolCategoryCodes().contains(school.getSchoolCategoryCode())) {
                            it.remove();
                        } else {
                            searchRequest.getSchoolOfRecords().add(school.getDistNo() + school.getSchlNo());
                        }
                    } else {
                        searchRequest.getSchoolOfRecords().add(school.getDistNo() + school.getSchlNo());
                    }
                }
            }
        }
        if(!StringUtils.isBlank(searchRequest.getSchoolOfRecord()) && !searchRequest.getSchoolOfRecords().contains(searchRequest.getSchoolOfRecord())) {
            searchRequest.getSchoolOfRecords().add(searchRequest.getSchoolOfRecord());
        }
        if(!StringUtils.isBlank(searchRequest.getGradProgram()) && !searchRequest.getPrograms().contains(searchRequest.getGradProgram())) {
            searchRequest.getPrograms().add(searchRequest.getGradProgram());
        }
        // validate the search criteria that has any values to avoid bringing up all students
        if (studentIds.isEmpty()
            && searchRequest.getSchoolOfRecords().isEmpty()
            && searchRequest.getPrograms().isEmpty()
            && searchRequest.getGradDateFrom() == null && searchRequest.getGradDateTo() == null) {
            searchResult.setStudentIDs(new ArrayList<>());
            return searchResult;
        }
        GraduationStudentRecordSearchCriteria searchCriteria = GraduationStudentRecordSearchCriteria.builder()
                .studentIds(studentIds)
                .schoolOfRecords(searchRequest.getSchoolOfRecords())
                .programs(searchRequest.getPrograms())
                .gradDateFrom(searchRequest.getGradDateFrom())
                .gradDateTo(searchRequest.getGradDateTo())
                .build();

        Specification<GraduationStudentRecordSearchEntity> spec = new GraduationStudentRecordSearchSpecification(searchCriteria);
        List<GraduationStudentRecordSearchEntity> results = graduationStudentRecordSearchRepository.findAll(Specification.where(spec));
        List<UUID> students = new ArrayList<>();
        if (results != null && !results.isEmpty()) {
            students = results.stream().map(GraduationStudentRecordSearchEntity::getStudentID).collect(Collectors.toList());
        }
        searchResult.setStudentIDs(students);
        return searchResult;
    }

    public StudentDemographic getStudentDemographics(String pen, String accessToken) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM");
        List<GradSearchStudent> gradSearchStudents = gradStudentService.getStudentByPenFromStudentAPI(pen, accessToken);
        if(gradSearchStudents.isEmpty()) {
            validation.addErrorAndStop("Student with pen %s not found", pen);
        }
        GradSearchStudent gradSearchStudent = gradSearchStudents.get(0);
        assert gradSearchStudent != null;

        String gradDate = null;
        String formerStudent = "F";
        Optional<GraduationStudentRecordEntity> graduationStudentRecordEntityOptional = graduationStatusRepository.findById(UUID.fromString(gradSearchStudent.getStudentID()));
        if(graduationStudentRecordEntityOptional.isPresent()) {
            GraduationStudentRecordEntity graduationStudentRecordEntity = graduationStudentRecordEntityOptional.get();
            long sessionInterval = Integer.MAX_VALUE;
            GraduationData graduationData = null;
            if(StringUtils.isNotBlank(graduationStudentRecordEntity.getStudentGradData())) {
                try {
                    graduationData = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(graduationStudentRecordEntity.getStudentGradData(), GraduationData.class);
                } catch (JsonProcessingException e) {
                    logger.debug("Parsing Graduation Data Error {}", e.getOriginalMessage());
                }
            }
            if(graduationData != null) {
                sessionInterval = graduationData.getSessionDateMonthsIntervalNow();
            }
            if(graduationStudentRecordEntity.getProgramCompletionDate() != null) {
                gradDate = simpleDateFormat.format(graduationStudentRecordEntity.getProgramCompletionDate());
            }
            if(sessionInterval <= 6 || ("CUR".equalsIgnoreCase(graduationStudentRecordEntity.getStudentStatus()))
            ) {
                formerStudent = "C";
            }
        }

        CommonSchool commonSchool = getCommonSchool(accessToken, gradSearchStudent.getSchoolOfRecord());
        if(commonSchool == null) {
            validation.addErrorAndStop("Common School with mincode %s not found", gradSearchStudent.getMincode());
        }

        School school = getSchool(gradSearchStudent.getSchoolOfRecord(), accessToken);
        if(school == null) {
            validation.addErrorAndStop("School with mincode %s not found", gradSearchStudent.getMincode());
        }

        String englishCert = "";
        String frenchCert = "";
        String dogwood = null;
        String sccDate = null;
        List<GradStudentCertificates> gradStudentCertificates = getGradStudentCertificates(gradSearchStudent.getStudentID(), accessToken);
        for(GradStudentCertificates certificates: gradStudentCertificates) {
            String certificateTypeCode = certificates.getGradCertificateTypeCode();
            dogwood = (gradDate != null && school != null && "Y".equalsIgnoreCase(school.getCertificateEligibility())) ? "Y" : "N";
            sccDate = "SCCP".equalsIgnoreCase(gradSearchStudent.getProgram()) && school != null && "Y".equalsIgnoreCase(school.getCertificateEligibility()) ? gradDate : null;
            switch(certificateTypeCode) {
                case "E":
                    englishCert = certificateTypeCode;
                    break;
                case "EI","A","AI","FN","FNA","SCFN","O":
                    englishCert = "E";
                    break;
                case "S","F":
                    frenchCert = certificateTypeCode;
                    break;
                default:
                    break;
            }
        }
        assert commonSchool != null;
        assert school != null;
        return StudentDemographic.builder()
                .studentID(gradSearchStudent.getStudentID())
                .pen(pen)
                .legalFirstName(gradSearchStudent.getLegalFirstName())
                .legalMiddleNames(gradSearchStudent.getLegalMiddleNames())
                .legalLastName(gradSearchStudent.getLegalLastName())
                .dob(gradSearchStudent.getDob())
                .sexCode(gradSearchStudent.getSexCode())
                .genderCode(gradSearchStudent.getGenderCode())
                .citizenship(gradSearchStudent.getStudentCitizenship())
                .usualFirstName(gradSearchStudent.getUsualFirstName())
                .usualMiddleNames(gradSearchStudent.getUsualMiddleNames())
                .usualLastName(gradSearchStudent.getUsualLastName())
                .email(gradSearchStudent.getEmail())
                .emailVerified(gradSearchStudent.getEmailVerified())
                .deceasedDate(gradSearchStudent.getDeceasedDate())
                .postalCode(gradSearchStudent.getPostalCode())
                .gradeCode(gradSearchStudent.getGradeCode())
                .gradeYear(gradSearchStudent.getGradeYear())
                .demogCode(gradSearchStudent.getDemogCode())
                .statusCode(gradSearchStudent.getStatusCode())
                .trueStudentID(gradSearchStudent.getTrueStudentID())
                .gradProgram(gradSearchStudent.getProgram())
                .gradDate(gradDate)
                .dogwoodFlag(dogwood)
                .frenchCert(frenchCert)
                .englishCert(englishCert)
                .sccDate(sccDate)
                .transcriptEligibility(gradSearchStudent.getTranscriptEligibility())
                .mincode(school.getMinCode())
                .schoolCategory(commonSchool.getSchoolCategoryCode())
                .schoolType("02".equalsIgnoreCase(commonSchool.getSchoolCategoryCode()) ? "02" : "")
                .schoolName(school.getSchoolName())
                .formerStudent(formerStudent)
                .build();
    }

    private List<GradStudentCertificates> getGradStudentCertificates(String studentID, String accessToken) {
        return webClient.get().uri(String.format(constants.getStudentCertificates(), studentID))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                })
                .retrieve().bodyToMono(new ParameterizedTypeReference<List<GradStudentCertificates>>() {
                }).block();
    }

    public List<CommonSchool> getSchools(String accessToken) {
        return webClient.get().uri((constants.getSchoolsSchoolApiUrl()))
            .headers(h -> {
                h.setBearerAuth(accessToken);
                h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
            })
            .retrieve().bodyToMono(new ParameterizedTypeReference<List<CommonSchool>>() {
        }).block();
    }

    public CommonSchool getCommonSchool(String accessToken, String mincode) {
        return webClient.get().uri(String.format(constants.getSchoolByMincodeSchoolApiUrl(), mincode))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                })
                .retrieve().bodyToMono(CommonSchool.class).block();
    }

    private School getSchool(String minCode, String accessToken) {
        return webClient.get()
                .uri(String.format(constants.getSchoolByMincodeUrl(), minCode))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                })
                .retrieve()
                .bodyToMono(School.class)
                .block();
    }

    private String getSchoolName(String minCode, String accessToken) {
        School schObj = getSchool(minCode, accessToken);
        if (schObj != null)
            return schObj.getSchoolName();
        else
            return null;
    }

    private District getDistrict(String districtCode, String accessToken) {
        return webClient.get()
                .uri(String.format(constants.getDistrictByDistrictCodeUrl(), districtCode))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                })
                .retrieve()
                .bodyToMono(District.class)
                .block();
    }

    private String getDistrictName(String districtCode, String accessToken) {
        District distObj = getDistrict(districtCode, accessToken);
        if (distObj != null)
            return distObj.getDistrictName();
        else
            return null;
    }

    private GradProgram getProgram(String programCode, String accessToken) {
        return webClient.get()
                .uri(String.format(constants.getGradProgramNameUrl(), programCode))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                })
                .retrieve()
                .bodyToMono(GradProgram.class)
                .block();
    }

    private String getProgramName(String programCode, String accessToken) {
        GradProgram gradProgram = getProgram(programCode, accessToken);
        if (gradProgram != null)
            return gradProgram.getProgramName();
        return null;
    }

    private void validateStudentStatus(String studentStatus) {
        if (studentStatus.equalsIgnoreCase("MER")) {
            validation.addErrorAndStop("Student GRAD data cannot be updated for students with a status of 'M' merged");
        }
        if (studentStatus.equalsIgnoreCase("DEC")) {
            validation.addWarning("This student is showing as deceased.  Confirm the students' status before re-activating by setting their status to 'A' if they are currently attending school");
        }
    }

    private void validateStudent(GraduationStudentRecord graduationStudentRecord) {
        validateStudentStatus(graduationStudentRecord.getStudentStatus());
    }

    private void validateProgram(GraduationStudentRecordEntity sourceEntity, String accessToken) {
        GradProgram gradProgram = webClient.get()
                .uri(String.format(constants.getGradProgramNameUrl(), sourceEntity.getProgram()))
                .headers(h -> {
            h.setBearerAuth(accessToken);
            h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
        })
                .retrieve()
                .bodyToMono(GradProgram.class)
                .block();
        if (gradProgram == null) {
            validation.addError(String.format("Program [%s] is invalid", sourceEntity.getProgram()));
        } else {
            if (sourceEntity.getProgram().contains("1950")) {
                if (!sourceEntity.getStudentGrade().equalsIgnoreCase("AD")
                        && !sourceEntity.getStudentGrade().equalsIgnoreCase("AN")) {
                    validation.addError(
                            String.format("Student grade should be one of AD or AN if the student program is [%s]",
                                    sourceEntity.getProgram()));
                }
            } else {
                if (sourceEntity.getStudentGrade().equalsIgnoreCase("AD")
                        || sourceEntity.getStudentGrade().equalsIgnoreCase("AN")) {
                    validation.addError(
                            String.format("Student grade should not be AD or AN for this program [%s]",
                                    sourceEntity.getProgram()));
                }
            }
        }
    }

    private void validateSchool(String minCode, String accessToken) {
        School schObj = webClient.get()
                .uri(String.format(constants.getSchoolByMincodeUrl(), minCode))
                .headers(h -> {
            h.setBearerAuth(accessToken);
            h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
        })
                .retrieve()
                .bodyToMono(School.class)
                .block();
        if (schObj == null) {
            validation.addError(
                    String.format("Invalid School entered, School [%s] does not exist on the School table", minCode));
        } else {
            if (schObj.getOpenFlag().equalsIgnoreCase("N")) {
                validation.addWarning(String.format("This School [%s] is Closed", minCode));
            }
        }
    }

    private void validateStudentGrade(GraduationStudentRecordEntity sourceEntity, GraduationStudentRecordEntity existingEntity, String accessToken) {
        Student studentObj = webClient.get()
                .uri(String.format(constants.getPenStudentApiByStudentIdUrl(), sourceEntity.getStudentID()))
                .headers(h -> {
            h.setBearerAuth(accessToken);
            h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
        })
                .retrieve()
                .bodyToMono(Student.class)
                .block();
        if(studentObj != null) {
            if (sourceEntity.getStudentStatus() != null && studentObj.getStatusCode() != null &&
                !PenGradStudentStatusEnum.valueOf(sourceEntity.getStudentStatus()).toString().equalsIgnoreCase(studentObj.getStatusCode())
            ) {
                validation.addError("Status code selected does not match with the PEN data for this student");
            }
            if (sourceEntity.getStudentGrade() != null && (sourceEntity.getStudentGrade().equalsIgnoreCase("AN")
                    || sourceEntity.getStudentGrade().equalsIgnoreCase("AD"))
                    && calculateAge(studentObj.getDob()) < 18) {
                validation.addError("Adult student should be at least 18 years old");
            }
        }

        if(sourceEntity.getProgram().contains("1950") && !sourceEntity.getStudentGrade().equalsIgnoreCase(existingEntity.getStudentGrade()) && sourceEntity.getStudentGrade().compareTo("AN") == 0 && sourceEntity.getStudentGrade().compareTo("AD") == 0) {
            validation.addError("Student Grade Should be AD or AN");
        }
        
    }

    private ValidateDataResult validateData(GraduationStudentRecordEntity sourceEntity, GraduationStudentRecordEntity existingEntity, String accessToken) {
        ValidateDataResult hasDataChanged = new ValidateDataResult();
        validateStudentStatus(existingEntity.getStudentStatus());

        if (sourceEntity.getProgram() != null && !sourceEntity.getProgram().equalsIgnoreCase(existingEntity.getProgram())) {
            hasDataChanged.recalculateAll();
            validateProgram(sourceEntity, accessToken);
        }

        if (sourceEntity.getProgramCompletionDate() != null && !sourceEntity.getProgramCompletionDate().equals(existingEntity.getProgramCompletionDate())) {
            hasDataChanged.recalculateAll();
        }
        
        if (sourceEntity.getSchoolOfRecord() != null && !sourceEntity.getSchoolOfRecord().equalsIgnoreCase(existingEntity.getSchoolOfRecord())) {
            hasDataChanged.recalculateAll();
            validateSchool(sourceEntity.getSchoolOfRecord(), accessToken);
        }        
        
        if (sourceEntity.getSchoolAtGrad() != null && !sourceEntity.getSchoolAtGrad().equalsIgnoreCase(existingEntity.getSchoolAtGrad())) {
            hasDataChanged.recalculateAll();
            validateSchool(sourceEntity.getSchoolAtGrad(), accessToken);
        }
        
        if ((sourceEntity.getStudentGrade() != null && !sourceEntity.getStudentGrade().equalsIgnoreCase(existingEntity.getStudentGrade()))) {
            hasDataChanged.recalculateAll();
            validateStudentGrade(sourceEntity,existingEntity,accessToken);
        }

        if (sourceEntity.getStudentStatus() != null && !sourceEntity.getStudentStatus().equalsIgnoreCase(existingEntity.getStudentStatus())) {
            hasDataChanged.recalculateAll();
            validateStudentGrade(sourceEntity,existingEntity,accessToken);
        }

        return hasDataChanged;
    }

    private void deleteStudentOptionalPrograms(UUID studentID) {
        List<StudentOptionalProgramEntity> studOpList = gradStudentOptionalProgramRepository.findByStudentID(studentID);
        if(!studOpList.isEmpty()) {
            for (StudentOptionalProgramEntity studentOptionalProgramEntity : studOpList) {
                historyService.createStudentOptionalProgramHistory(studentOptionalProgramEntity,"USERDELETE");
                gradStudentOptionalProgramRepository.deleteById(studentOptionalProgramEntity.getId());
            }
        }
    }

    private void deleteStudentCareerPrograms(UUID studentID) {
        gradStudentCareerProgramRepository.deleteByStudentID(studentID);
    }

    private String getHonoursFlag(String gPA) {
        if (Float.parseFloat(gPA) > 3)
            return "Y";
        else
            return "N";
    }

    public List<StudentOptionalProgram> getStudentGradOptionalProgram(UUID studentID, String accessToken) {
        List<StudentOptionalProgram> optionalProgramList =
                gradStudentOptionalProgramTransformer.transformToDTO(gradStudentOptionalProgramRepository.findByStudentID(studentID));
        optionalProgramList.forEach(sP -> {
            OptionalProgram gradOptionalProgram = webClient.get()
                    .uri(String.format(constants.getGradOptionalProgramNameUrl(), sP.getOptionalProgramID()))
                    .headers(h -> {
              h.setBearerAuth(accessToken);
              h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
          })
                    .retrieve()
                    .bodyToMono(OptionalProgram.class)
                    .block();
            if(gradOptionalProgram != null) {
                sP.setOptionalProgramName(gradOptionalProgram.getOptionalProgramName());
                sP.setOptionalProgramCode(gradOptionalProgram.getOptProgramCode());
                sP.setProgramCode(gradOptionalProgram.getGraduationProgramCode());
            }
        });
        return optionalProgramList;
    }

    public int calculateAge(String dob) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(dob, dateFormatter);
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthDate, currentDate).getYears();
    }

    public StudentOptionalProgram saveStudentGradOptionalProgram(StudentOptionalProgram gradStudentOptionalProgram) {
        Optional<StudentOptionalProgramEntity> gradStudentOptionalOptional =
                gradStudentOptionalProgramRepository.findById(gradStudentOptionalProgram.getId());
        logger.debug("Save with payload ==> Student Optional Program ID: {}", gradStudentOptionalProgram.getId());
        StudentOptionalProgramEntity sourceObject = gradStudentOptionalProgramTransformer.transformToEntity(gradStudentOptionalProgram);
        sourceObject.setUpdateUser(null); //this change is just till idir login is fixed
        if (gradStudentOptionalOptional.isPresent()) {
            StudentOptionalProgramEntity gradEnity = gradStudentOptionalOptional.get();
            logger.debug(" -> Student Optional Program Entity is found for ID: {} === Entity ID: {}", gradStudentOptionalProgram.getId(), gradEnity.getId());
            BeanUtils.copyProperties(sourceObject, gradEnity, CREATE_USER, CREATE_DATE);
            gradEnity.setOptionalProgramCompletionDate(sourceObject.getOptionalProgramCompletionDate());
            gradEnity = gradStudentOptionalProgramRepository.save(gradEnity);
            historyService.createStudentOptionalProgramHistory(gradEnity,GRAD_ALG);
            return gradStudentOptionalProgramTransformer.transformToDTO(gradEnity);
        } else {
            logger.debug(" -> Student Optional Program Entity is not found for ID: {}", gradStudentOptionalProgram.getId());
            return null;
        }
    }

    @Transactional
    public StudentOptionalProgram createStudentGradOptionalProgram(UUID studentID, StudentOptionalProgram gradStudentOptionalProgram) throws EntityNotFoundException {
        gradStudentOptionalProgram.setStudentID(studentID);
        validateStudent(getGraduationStatus(studentID));
        StudentOptionalProgramEntity sourceObject = gradStudentOptionalProgramTransformer.transformToEntity(gradStudentOptionalProgram);
        sourceObject.setUpdateUser(null); //this change is just till idir login is fixed
        StudentOptionalProgramEntity gradEnity = new StudentOptionalProgramEntity();
        logger.debug(" -> Creating Student Optional Program Entity for student ID: {}", studentID);
        BeanUtils.copyProperties(sourceObject, gradEnity, CREATE_USER, CREATE_DATE);
        gradEnity.setOptionalProgramCompletionDate(sourceObject.getOptionalProgramCompletionDate());
        gradEnity = gradStudentOptionalProgramRepository.save(gradEnity);
        historyService.createStudentOptionalProgramHistory(gradEnity, USER_CREATE);
        graduationStatusRepository.updateGradStudentRecalculationFlags(studentID, "Y", "Y");
        return gradStudentOptionalProgramTransformer.transformToDTO(gradEnity);
    }

    @Transactional
    public StudentOptionalProgram updateStudentGradOptionalProgram(UUID studentID, UUID optionalProgramID, StudentOptionalProgram gradStudentOptionalProgram) throws EntityNotFoundException {
        validateStudent(getGraduationStatus(studentID));
        gradStudentOptionalProgram.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgram.setStudentID(studentID);
        Optional<StudentOptionalProgramEntity> gradStudentOptionalProgramEntityOptional =
                gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID);
        StudentOptionalProgramEntity sourceObject = gradStudentOptionalProgramTransformer.transformToEntity(gradStudentOptionalProgram);
        sourceObject.setUpdateUser(null); //this change is just till idir login is fixed
        if (gradStudentOptionalProgramEntityOptional.isPresent()) {
            StudentOptionalProgramEntity gradEnity = gradStudentOptionalProgramEntityOptional.get();
            logger.debug(" -> Student {} Optional Program Entity is found for ID: {} === Entity ID: {}", studentID, optionalProgramID, gradEnity.getId());
            BeanUtils.copyProperties(sourceObject, gradEnity, "id", "studentID", "optionalProgramID", UPDATE_USER, UPDATE_DATE);
            gradEnity.setOptionalProgramCompletionDate(sourceObject.getOptionalProgramCompletionDate());
            gradEnity = gradStudentOptionalProgramRepository.save(gradEnity);
            historyService.createStudentOptionalProgramHistory(gradEnity,GRAD_ALG);
            graduationStatusRepository.updateGradStudentRecalculationFlags(studentID, "Y", "Y");
            return gradStudentOptionalProgramTransformer.transformToDTO(gradEnity);
        } else {
            String msg = "Student %s optional program % was not found";
            throw new EntityNotFoundException(String.format(msg, studentID, optionalProgramID));
        }
    }

    @Transactional
    public void deleteStudentGradOptionalProgram(UUID studentID, UUID optionalProgramID, String careerProgramID) throws EntityNotFoundException {
        validateStudent(getGraduationStatus(studentID));
        Optional<StudentOptionalProgramEntity> gradStudentOptionalOptional =
                gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID);
        logger.debug("Save with payload ==> Student Optional Program ID: {}", optionalProgramID);
        if (gradStudentOptionalOptional.isPresent()) {
            StudentOptionalProgramEntity gradEnity = gradStudentOptionalOptional.get();
            logger.debug(" -> Student Optional Program Entity is found for ID: {} === Entity ID: {}", optionalProgramID, optionalProgramID);
            gradStudentOptionalProgramRepository.delete(gradEnity);
            historyService.createStudentOptionalProgramHistory(gradEnity, USER_DELETE);
            if(StringUtils.isNotBlank(careerProgramID)) {
                gradStudentCareerProgramRepository.deleteStudentCareerProgramEntityByStudentIDAndId(studentID, UUID.fromString(careerProgramID));
            }
            graduationStatusRepository.updateGradStudentRecalculationFlags(studentID, "Y", "Y");
        } else {
            String msg = "Student Optional Program %s for student %s is not found";
            throw new EntityNotFoundException(String.format(msg, optionalProgramID, studentID));
        }
    }
    
    public StudentOptionalProgram updateStudentGradOptionalProgram(StudentOptionalProgramReq gradStudentOptionalProgramReq, String accessToken) {
        Optional<StudentOptionalProgramEntity> gradStudentOptionalOptional = Optional.empty();
        if(gradStudentOptionalProgramReq.getId() != null)
            gradStudentOptionalOptional = gradStudentOptionalProgramRepository.findById(gradStudentOptionalProgramReq.getId());

        StudentOptionalProgramEntity sourceObject = new StudentOptionalProgramEntity();
        OptionalProgram gradOptionalProgram = webClient.get()
                .uri(String.format(constants.getGradOptionalProgramDetailsUrl(), gradStudentOptionalProgramReq.getMainProgramCode(),gradStudentOptionalProgramReq.getOptionalProgramCode()))
                .headers(h -> {
            h.setBearerAuth(accessToken);
            h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
        })
                .retrieve()
                .bodyToMono(OptionalProgram.class)
                .block();
        sourceObject.setId(gradStudentOptionalProgramReq.getId());
        sourceObject.setStudentID(gradStudentOptionalProgramReq.getStudentID());
        sourceObject.setOptionalProgramCompletionDate(gradStudentOptionalProgramReq.getOptionalProgramCompletionDate() != null ?Date.valueOf(gradStudentOptionalProgramReq.getOptionalProgramCompletionDate()) : null);
        sourceObject.setOptionalProgramID(gradOptionalProgram != null ? gradOptionalProgram.getOptionalProgramID() : null);
        if (gradStudentOptionalOptional.isPresent()) {
            StudentOptionalProgramEntity gradEntity = gradStudentOptionalOptional.get();
            if(gradEntity.getOptionalProgramID().equals(sourceObject.getOptionalProgramID())) {
                sourceObject.setStudentOptionalProgramData(gradEntity.getStudentOptionalProgramData());
            } else {
                sourceObject.setStudentOptionalProgramData(null);
            }
            BeanUtils.copyProperties(sourceObject, gradEntity, CREATE_USER, CREATE_DATE, "id");
            gradEntity.setOptionalProgramCompletionDate(sourceObject.getOptionalProgramCompletionDate());
            gradEntity = gradStudentOptionalProgramRepository.save(gradEntity);
            historyService.createStudentOptionalProgramHistory(gradEntity, USER_EDIT);
            return gradStudentOptionalProgramTransformer.transformToDTO(gradEntity);
        } else {
            sourceObject = gradStudentOptionalProgramRepository.save(sourceObject);
            historyService.createStudentOptionalProgramHistory(sourceObject, USER_CREATE);
            return gradStudentOptionalProgramTransformer.transformToDTO(sourceObject);
        }
    }

    public List<UUID> getStudentsForGraduation() {
        return graduationStatusRepository.findByRecalculateGradStatusForBatch("Y");
    }

    public List<UUID> getStudentsForProjectedGraduation() {
       return graduationStatusRepository.findByRecalculateProjectedGradForBatch("Y");
    }

    public BatchGraduationStudentRecord getStudentForBatch(UUID studentID) {
        Optional<BatchGraduationStudentRecord> optional = graduationStatusRepository.findByStudentIDForBatch(studentID);
        return optional.isPresent()? optional.get() : null;
    }

    @Retry(name = "generalgetcall")
    public StudentOptionalProgram getStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode(
            UUID studentID, String optionalProgramID, String accessToken) {
        UUID optionalProgramIDUUID = UUID.fromString(optionalProgramID);
        Optional<StudentOptionalProgramEntity> gradStudentOptionalOptional =
                gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramIDUUID);
        if (gradStudentOptionalOptional.isPresent()) {
            StudentOptionalProgram responseObj = gradStudentOptionalProgramTransformer.transformToDTO(gradStudentOptionalOptional);
            OptionalProgram gradOptionalProgram = webClient.get()
                    .uri(String.format(constants.getGradOptionalProgramNameUrl(), responseObj.getOptionalProgramID()))
                    .headers(h -> {
              h.setBearerAuth(accessToken);
              h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
          })
                    .retrieve()
                    .bodyToMono(OptionalProgram.class)
                    .block();
            if(gradOptionalProgram != null) {
                responseObj.setOptionalProgramName(gradOptionalProgram.getOptionalProgramName());
                responseObj.setOptionalProgramCode(gradOptionalProgram.getOptProgramCode());
                responseObj.setProgramCode(gradOptionalProgram.getGraduationProgramCode());
            }
            return responseObj;
        }
        return null;
    }

    public boolean getStudentStatus(String statusCode) {
        List<GraduationStudentRecordEntity> gradList = graduationStatusRepository.existsByStatusCode(statusCode);
        return !gradList.isEmpty();
    }

    @Transactional
    @Retry(name = "generalpostcall")
    public Pair<GraduationStudentRecord, GradStatusEvent> undoCompletionStudent(UUID studentID, String ungradReasonCode, String ungradDesc, String accessToken) throws JsonProcessingException {
        if(StringUtils.isNotBlank(ungradReasonCode)) {
            UndoCompletionReason ungradReasonObj = webClient.get().uri(String.format(constants.getUndoCompletionReasonDetailsUrl(),ungradReasonCode))
              .headers(h -> {
                  h.setBearerAuth(accessToken);
                  h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
              })
              .retrieve().bodyToMono(UndoCompletionReason.class).block();
            if(ungradReasonObj != null) {
                Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
                if (gradStatusOptional.isPresent()) {
                    GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
                    saveUndoCompletionReason(studentID,ungradReasonCode,ungradDesc,accessToken);
                    deleteStudentAchievements(studentID,accessToken);
                    gradEntity.setRecalculateGradStatus("Y");
                    gradEntity.setRecalculateProjectedGrad("Y");
                    gradEntity.setStudentGradData(null);
                    gradEntity.setProgramCompletionDate(null);
                    gradEntity.setHonoursStanding(null);
                    gradEntity.setGpa(null);
                    gradEntity.setSchoolAtGrad(null);
                    gradEntity.setUpdateUser(null);
                    gradEntity = graduationStatusRepository.save(gradEntity);
                    historyService.createStudentHistory(gradEntity, USER_UNDO_CMPL);
                    final GradStatusEvent gradStatusEvent = createGradStatusEvent(gradEntity.getUpdateUser(), gradEntity,
                            EventType.GRAD_STUDENT_UNDO_COMPLETION, EventOutcome.GRAD_STATUS_UPDATED, USER_UNDO_CMPL, accessToken);
                    if (gradStatusEvent != null) {
                        gradStatusEventRepository.save(gradStatusEvent);
                    }
                    return Pair.of(graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(gradEntity), gradStatusEvent);
                } else {
                    validation.addErrorAndStop(String.format("Student ID [%s] does not exists", studentID));
                    return Pair.of(null, null);
                }
            }else {
                validation.addErrorAndStop(String.format("Invalid Ungrad Reason Code [%s]",ungradReasonCode));
                return Pair.of(null, null);
            }
        }else {
            validation.addErrorAndStop("Ungrad Reason Code is required");
            return Pair.of(null, null);
        }
    }
    
    public void deleteStudentAchievements(UUID studentID,String accessToken) {
        try {
            webClient.delete().uri(String.format(constants.getDeleteStudentAchievements(), studentID))
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                    }).retrieve().onStatus(p -> p.value() == 404, error -> Mono.error(new Exception("Credential Not Found"))).bodyToMono(Integer.class).block();
        }catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    public void archiveStudentAchievements(UUID studentID,String accessToken) {
        try {
            webClient.delete().uri(String.format(constants.getArchiveStudentAchievements(), studentID))
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                    }).retrieve().onStatus(p -> p.value() == 404, error -> Mono.error(new Exception("Credential Not Found"))).bodyToMono(Integer.class).block();
        }catch (Exception e) {
            logger.error(e.getLocalizedMessage());
        }
    }


    public void saveUndoCompletionReason(UUID studentID, String ungradReasonCode, String unGradDesc,String accessToken) {
        StudentUndoCompletionReason toBeSaved = new StudentUndoCompletionReason();
        toBeSaved.setGraduationStudentRecordID(studentID);
        toBeSaved.setUndoCompletionReasonCode(ungradReasonCode);
        toBeSaved.setUndoCompletionReasonDescription(unGradDesc);
        webClient.post().uri(String.format(constants.getSaveStudentUndoCompletionReasonByStudentIdUrl(),studentID))
            .headers(h -> {
                h.setBearerAuth(accessToken);
                h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
            })
            .body(BodyInserters.fromValue(toBeSaved))
            .retrieve().bodyToMono(StudentUndoCompletionReason.class).block();
    }

    private GradStatusEvent createGradStatusEvent(String updateUser,
                                                  GraduationStudentRecordEntity graduationStudentRecord,
                                                  EventType eventType, EventOutcome eventOutcome,
                                                  String activityCode,
                                                  String accessToken) throws JsonProcessingException {
        if (!constants.isTraxUpdateEnabled()) {
            return null;
        }
        if (StringUtils.isBlank(graduationStudentRecord.getPen())) {
            GradSearchStudent gradSearchStudent = gradStudentService.getStudentByStudentIDFromStudentAPI(graduationStudentRecord.getStudentID().toString(), accessToken);
            if (gradSearchStudent != null) {
                graduationStudentRecord.setPen(gradSearchStudent.getPen());
            }
        }
        GradStatusEventPayloadDTO eventPayload = EducGradStudentApiUtils.transform(graduationStudentRecord);
        String jsonString = JsonUtil.getJsonStringFromObject(eventPayload);
        return GradStatusEvent.builder()
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .createUser(StringUtils.isBlank(updateUser)? EducGradStudentApiConstants.DEFAULT_CREATED_BY : updateUser)
                .updateUser(StringUtils.isBlank(updateUser)? EducGradStudentApiConstants.DEFAULT_UPDATED_BY : updateUser)
                .eventPayload(jsonString)
                .eventType(eventType.toString())
                .eventStatus(DB_COMMITTED.toString())
                .eventOutcome(eventOutcome.toString())
                .activityCode(activityCode)
                .sagaId(StringUtils.isNotBlank(ThreadLocalStateUtil.getCorrelationID())? UUID.fromString(ThreadLocalStateUtil.getCorrelationID()) : null)
                .build();
    }

    @Retry(name = "generalpostcall")
    public boolean restoreGradStudentRecord(UUID studentID,boolean isGraduated) {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEnity = gradStatusOptional.get();
            gradEnity.setRecalculateGradStatus("Y");
            if(!isGraduated) {
                gradEnity.setProgramCompletionDate(null);
                gradEnity.setHonoursStanding(null);
                gradEnity.setGpa(null);
                gradEnity.setSchoolAtGrad(null);
            }
            graduationStatusRepository.save(gradEnity);
            return true;
        }
        return false;
    }

    @Retry(name = "generalpostcall")
    public GraduationStudentRecord saveStudentRecordDistributionRun(UUID studentID, Long batchId,String activityCode) {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            gradEntity.setUpdateUser(null);
            gradEntity.setUpdateDate(null);
            gradEntity.setBatchId(batchId);
            gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
            historyService.createStudentHistory(gradEntity, activityCode);
            return graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(gradEntity);
        }
        return null;
    }

    @Retry(name = "generalpostcall")
    public GraduationStudentRecord saveStudentRecordProjectedTVRRun(UUID studentID, Long batchId, ProjectedRunClob projectedRunClob) {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        String projectedClob = null;
        try {
            projectedClob = new ObjectMapper().writeValueAsString(projectedRunClob);
        } catch (JsonProcessingException e) {
            logger.debug("JSON error {}",e.getMessage());
        }
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            gradEntity.setUpdateUser(null);
            gradEntity.setUpdateDate(null);
            gradEntity.setBatchId(batchId);
            if(batchId != null) {
                resetBatchFlags(gradEntity, true);
            }
            gradEntity.setStudentProjectedGradData(projectedClob);
            gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
            historyService.createStudentHistory(gradEntity, "GRADPROJECTED");
            return graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(gradEntity);
        }
        return null;
    }

    public List<GraduationStudentRecord> getStudentDataByStudentIDs(List<UUID> studentIds) {
        return graduationStatusTransformer.tToDForBatch(graduationStatusRepository.findByStudentIDIn(studentIds));
    }

    public List<UUID> getStudentsForYearlyDistribution() {
        PageRequest nextPage = PageRequest.of(0, PAGE_SIZE);
        Page<UUID> studentGuids = graduationStatusRepository.findStudentsForYearlyDistribution(nextPage);
        return processStudentDataList(studentGuids);
    }

    public GraduationStudentRecord getDataForBatch(UUID studentID,String accessToken) {
        GraduationStudentRecord ent = graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(graduationStatusRepository.findByStudentID(studentID));
        return  processReceivedStudent(ent,accessToken);
    }

    public StudentNonGradReason getNonGradReason(String pen) {
        List<StudentNonGradReasonEntity> results = studentNonGradReasonRepository.findByPen(pen);
        if (results != null && !results.isEmpty()) {
            return studentNonGradReasonTransformer.transformToDTO(results.get(0));
        }
        return null;
    }

    private List<UUID> processStudentDataList(Page<UUID> studentGuids) {
        List<UUID> result = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        if(studentGuids.hasContent()) {
            PageRequest nextPage;
            List<UUID> studentGuidsInBatch = studentGuids.getContent();
            result.addAll(studentGuidsInBatch);
            final int totalNumberOfPages = studentGuids.getTotalPages();
            logger.debug("Total number of pages: {}, total rows count {}", totalNumberOfPages, studentGuids.getTotalElements());

            List<Callable<Object>> tasks = new ArrayList<>();

            for (int i = 1; i < totalNumberOfPages; i++) {
                nextPage = PageRequest.of(i, PAGE_SIZE);
                UUIDPageTask pageTask = new UUIDPageTask(nextPage);
                tasks.add(pageTask);
            }

            processUUIDDataTasksAsync(tasks, result);
        }
        logger.debug("Completed in {} sec, total objects aquared {}", (System.currentTimeMillis() - startTime) / 1000, result.size());
        return result;
    }

    private void processUUIDDataTasksAsync(List<Callable<Object>> tasks, List<UUID> result) {
        if(tasks.isEmpty()) return;
        List<Future<Object>> executionResult;
        ExecutorService executorService = Executors.newWorkStealingPool();
        try {
            executionResult = executorService.invokeAll(tasks);
            for (Future<?> f : executionResult) {
                Object o = f.get();
                if(o instanceof Pair<?, ?>) {
                    Pair<PageRequest, List<UUID>> taskResult = (Pair<PageRequest, List<UUID>>) o;
                    result.addAll(taskResult.getRight());
                    logger.debug("Page {} processed successfully", taskResult.getLeft().getPageNumber());
                } else {
                    logger.error("Error during the task execution: {}", f.get());
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            logger.error("Unable to process Student Data: {} ", ex.getLocalizedMessage());
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown();
        }
    }

    private GraduationStudentRecord processReceivedStudent(GraduationStudentRecord ent,String accessToken) {
        try {
            if(ent.getStudentGradData() != null) {
                GraduationData existingData = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(ent.getStudentGradData(), GraduationData.class);

                ent.setPen(existingData.getGradStudent().getPen());
                ent.setLegalFirstName(existingData.getGradStudent().getLegalFirstName());
                ent.setLegalMiddleNames(existingData.getGradStudent().getLegalMiddleNames());
                ent.setLegalLastName(existingData.getGradStudent().getLegalLastName());
            }else {
                Student stuData = webClient.get().uri(String.format(constants.getPenStudentApiByStudentIdUrl(), ent.getStudentID()))
                        .headers(h -> {
                            h.setBearerAuth(accessToken);
                            h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                        }).retrieve().bodyToMono(Student.class).block();
                if(stuData != null) {
                    ent.setPen(stuData.getPen());
                    ent.setLegalFirstName(stuData.getLegalFirstName());
                    ent.setLegalMiddleNames(stuData.getLegalMiddleNames());
                    ent.setLegalLastName(stuData.getLegalLastName());
                }
            }
        } catch (JsonProcessingException e) {
            logger.debug("Parsing Error {}",e.getOriginalMessage());
        }
        return ent;
    }

    public List<GraduationStudentRecord> getStudentsForSchoolReport(String schoolOfRecord) {
        return graduationStatusTransformer.tToDForBatch(graduationStatusRepository.findBySchoolOfRecord(schoolOfRecord));
    }

    public List<UUID> getStudentsForAmalgamatedSchoolReport(String schoolOfRecord,String type) {
        return graduationStatusTransformer.tToDForAmalgamation(graduationStatusRepository.findBySchoolOfRecordAmalgamated(schoolOfRecord),type);
    }

    public List<GraduationStudentRecord> updateStudentFlagReadyForBatchJobByStudentIDs(String batchJobType, List<UUID> studentIDs) {
        logger.debug("updateStudentFlagReadyForBatchJobByStudentIDs");
        return studentIDs.stream()
                .map(stid -> updateStudentFlagReadyForBatchJob(stid, batchJobType))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private GraduationStudentRecord updateStudentFlagReadyForBatchJob(UUID studentID, String batchJobType) {
        logger.debug("updateStudentFlagReadyByJobType for studentID - {}", studentID);
        GraduationStudentRecord result = null;
        Optional<GraduationStudentRecordEntity> optional = graduationStatusRepository.findById(studentID);
        if (optional.isPresent()) {
            GraduationStudentRecordEntity entity = optional.get();
            result = saveBatchFlagsOfGraduationStudentRecord(entity, batchJobType);
        }
        return result;
    }

    private GraduationStudentRecord saveBatchFlagsOfGraduationStudentRecord(GraduationStudentRecordEntity entity, String batchJobType) {
        boolean isUpdated = false;
        if (entity.getBatchId() != null) {
            if (StringUtils.equals("REGALG", batchJobType)) {
                if (entity.getRecalculateGradStatus() == null || StringUtils.equals("N", entity.getRecalculateGradStatus())) {
                    entity.setRecalculateGradStatus("Y");
                    isUpdated = true;
                }
            } else {
                if (entity.getRecalculateProjectedGrad() == null || StringUtils.equals("N", entity.getRecalculateProjectedGrad())) {
                    entity.setRecalculateProjectedGrad("Y");
                    isUpdated = true;
                }
            }
            if (isUpdated) {
                graduationStatusRepository.save(entity);
                return graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(entity);
            }
        }
        return null;
    }

    private void resetBatchFlags(GraduationStudentRecordEntity gradEntity, boolean projectedRun) {
        String flag = null;
        if (gradEntity.getProgram().equalsIgnoreCase("SCCP") && EducGradStudentApiUtils.isDateInFuture(gradEntity.getProgramCompletionDate())) {
            flag = "Y";
        }

        if (projectedRun)
            gradEntity.setRecalculateProjectedGrad(flag);
        else
            gradEntity.setRecalculateGradStatus(flag);
    }

    class UUIDPageTask implements Callable<Object> {

        private final PageRequest pageRequest;

        public UUIDPageTask(PageRequest pageRequest) {
            this.pageRequest = pageRequest;
        }

        @Override
        public Object call() throws Exception {
            Page<UUID> studentGuids = graduationStatusRepository.findStudentsForYearlyDistribution(pageRequest);
            return Pair.of(pageRequest, studentGuids.getContent());
        }
    }
}
