package ca.bc.gov.educ.api.gradstudent.service;


import ca.bc.gov.educ.api.gradstudent.constant.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentStatusEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentOptionalProgramTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
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
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.gradstudent.constant.EventStatus.DB_COMMITTED;

@Service
public class GraduationStatusService {

    private static final Logger logger = LoggerFactory.getLogger(GraduationStatusService.class);

    private static final String CREATE_USER = "createUser";
    private static final String CREATE_DATE = "createDate";
    private static final String GRAD_ALG = "GRADALG";
    private static final String USER_EDIT = "USEREDIT";

    final
    WebClient webClient;

    final GraduationStudentRecordRepository graduationStatusRepository;
    final StudentStatusRepository studentStatusRepository;
    final GradStatusEventRepository gradStatusEventRepository;
    final GraduationStatusTransformer graduationStatusTransformer;
    final StudentOptionalProgramRepository gradStudentOptionalProgramRepository;
    final GradStudentOptionalProgramTransformer gradStudentOptionalProgramTransformer;
    final GradStudentService gradStudentService;
    final HistoryService historyService;
    final GradValidation validation;
    final EducGradStudentApiConstants constants;

    @Autowired
    GraduationStudentRecordSearchRepository graduationStudentRecordSearchRepository;

    @Autowired
    public GraduationStatusService(WebClient webClient, GraduationStudentRecordRepository graduationStatusRepository, StudentStatusRepository studentStatusRepository, GradStatusEventRepository gradStatusEventRepository, GraduationStatusTransformer graduationStatusTransformer, StudentOptionalProgramRepository gradStudentOptionalProgramRepository, GradStudentOptionalProgramTransformer gradStudentOptionalProgramTransformer, GradStudentService gradStudentService, HistoryService historyService, GradValidation validation, EducGradStudentApiConstants constants) {
        this.webClient = webClient;
        this.graduationStatusRepository = graduationStatusRepository;
        this.studentStatusRepository = studentStatusRepository;
        this.gradStatusEventRepository = gradStatusEventRepository;
        this.graduationStatusTransformer = graduationStatusTransformer;
        this.gradStudentOptionalProgramRepository = gradStudentOptionalProgramRepository;
        this.gradStudentOptionalProgramTransformer = gradStudentOptionalProgramTransformer;
        this.gradStudentService = gradStudentService;
        this.historyService = historyService;
        this.validation = validation;
        this.constants = constants;
    }

    @Retry(name = "generalgetcall")
    public GraduationStudentRecord getGraduationStatusForAlgorithm(UUID studentID) {
        logger.debug("getGraduationStatusForAlgorithm");
        Optional<GraduationStudentRecordEntity> responseOptional = graduationStatusRepository.findById(studentID);
        return responseOptional.map(graduationStatusTransformer::transformToDTO).orElse(null);
    }

    @Retry(name = "generalgetcall")
    public GraduationStudentRecord getGraduationStatus(UUID studentID, String accessToken) {
        logger.debug("getGraduationStatus");
        Optional<GraduationStudentRecordEntity> responseOptional = graduationStatusRepository.findById(studentID);
        if (responseOptional.isPresent()) {
            GraduationStudentRecord gradStatus = graduationStatusTransformer.transformToDTO(responseOptional.get());
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

            return gradStatus;
        } else {
            return null;
        }

    }

    @Transactional
    @Retry(name = "generalpostcall")
    public Pair<GraduationStudentRecord, GradStatusEvent> saveGraduationStatus(UUID studentID, GraduationStudentRecord graduationStatus, Long batchId, String accessToken) throws JsonProcessingException {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        GraduationStudentRecordEntity sourceObject = graduationStatusTransformer.transformToEntity(graduationStatus);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            BeanUtils.copyProperties(sourceObject, gradEntity, CREATE_USER, CREATE_DATE,"recalculateProjectedGrad","programCompletionDate","consumerEducationRequirementMet");
            gradEntity.setRecalculateGradStatus(null);
            if(batchId == null) {
                gradEntity.setRecalculateGradStatus("Y");
            }

            gradEntity.setBatchId(batchId);
            if(!gradEntity.getProgram().equalsIgnoreCase("SCCP") && !gradEntity.getProgram().equalsIgnoreCase("NOPROG")) {
                gradEntity.setProgramCompletionDate(sourceObject.getProgramCompletionDate());
            }
            gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
            historyService.createStudentHistory(gradEntity, GRAD_ALG);
            final GraduationStudentRecord savedGraduationStatus = graduationStatusTransformer.transformToDTO(gradEntity);
            final GradStatusEvent gradStatusEvent = createGradStatusEvent(gradEntity.getCreateUser(), gradEntity.getUpdateUser(),
                    savedGraduationStatus, EventType.UPDATE_GRAD_STATUS, EventOutcome.GRAD_STATUS_UPDATED, GRAD_ALG, accessToken);
            gradStatusEventRepository.save(gradStatusEvent);
            return Pair.of(savedGraduationStatus, gradStatusEvent);
        } else {
            sourceObject = graduationStatusRepository.saveAndFlush(sourceObject);
            final GraduationStudentRecord savedGraduationStatus = graduationStatusTransformer.transformToDTO(sourceObject);
            final GradStatusEvent gradStatusEvent = createGradStatusEvent(sourceObject.getCreateUser(), sourceObject.getUpdateUser(), savedGraduationStatus, EventType.CREATE_GRAD_STATUS, EventOutcome.GRAD_STATUS_CREATED, GRAD_ALG, accessToken);
            gradStatusEventRepository.save(gradStatusEvent);
            return Pair.of(savedGraduationStatus, gradStatusEvent);
        }
    }

    @Transactional
    @Retry(name = "generalpostcall")
    public Pair<GraduationStudentRecord, GradStatusEvent> updateGraduationStatus(UUID studentID, GraduationStudentRecord graduationStatus, String accessToken) throws JsonProcessingException {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        GraduationStudentRecordEntity sourceObject = graduationStatusTransformer.transformToEntity(graduationStatus);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            boolean hasDataChanged = validateData(sourceObject, gradEntity, accessToken);
            if (validation.hasErrors()) {
                validation.stopOnErrors();
                return Pair.of(new GraduationStudentRecord(), null);
            }
            if(hasDataChanged && !sourceObject.getProgram().equalsIgnoreCase(gradEntity.getProgram())) {
                deleteStudentOptionalPrograms(sourceObject.getStudentID());
                if(gradEntity.getProgram().equalsIgnoreCase("SCCP")) {
                    sourceObject.setRecalculateGradStatus("Y");
                    sourceObject.setRecalculateProjectedGrad("Y");
                    sourceObject.setStudentGradData(null);
                    sourceObject.setProgramCompletionDate(null);
                    sourceObject.setHonoursStanding(null);
                    sourceObject.setGpa(null);
                    sourceObject.setSchoolAtGrad(null);
                    archiveStudentAchievements(sourceObject.getStudentID(),accessToken);
                }else {
                    deleteStudentAchievements(sourceObject.getStudentID(), accessToken);
                }
            }
            if (hasDataChanged) {
                gradEntity.setRecalculateGradStatus("Y");
                gradEntity.setRecalculateProjectedGrad("Y");
            } else {
                gradEntity.setRecalculateGradStatus(null);
                gradEntity.setRecalculateProjectedGrad(null);
            }
            BeanUtils.copyProperties(sourceObject, gradEntity, CREATE_USER, CREATE_DATE, "studentGradData", "recalculateGradStatus", "recalculateProjectedGrad","consumerEducationRequirementMet");
            gradEntity.setProgramCompletionDate(sourceObject.getProgramCompletionDate());
            gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
            historyService.createStudentHistory(gradEntity, USER_EDIT);
            final GraduationStudentRecord updatedGraduationStatus = graduationStatusTransformer.transformToDTO(gradEntity);
            final GradStatusEvent gradStatusEvent = createGradStatusEvent(gradEntity.getCreateUser(), gradEntity.getUpdateUser(),
                    updatedGraduationStatus, EventType.UPDATE_GRAD_STATUS, EventOutcome.GRAD_STATUS_UPDATED, USER_EDIT, accessToken);
            gradStatusEventRepository.save(gradStatusEvent);
            return Pair.of(updatedGraduationStatus, gradStatusEvent);
        } else {
            validation.addErrorAndStop(String.format("Student ID [%s] does not exists", studentID));
            return Pair.of(graduationStatus, null);
        }
    }

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

        GraduationStudentRecordSearchCriteria searchCriteria = GraduationStudentRecordSearchCriteria.builder()
                .studentIds(studentIds)
                .schoolOfRecords(searchRequest.getSchoolOfRecords())
                .programs(searchRequest.getPrograms())
                .gradDateFrom(searchRequest.getGradDateFrom())
                .gradDateTo(searchRequest.getGradDateTo())
                .build();

        Specification<GraduationStudentRecordEntity> spec = new GraduationStudentRecordSearchSpecification(searchCriteria);
        List<GraduationStudentRecord> students = graduationStatusTransformer.transformToDTO(graduationStudentRecordSearchRepository.findAll(Specification.where(spec)));
        searchResult.setGraduationStudentRecords(students);

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
            if(graduationStudentRecordEntity.getProgramCompletionDate() != null) {
                gradDate = simpleDateFormat.format(graduationStudentRecordEntity.getProgramCompletionDate());
            }
            if("CUR".equalsIgnoreCase(graduationStudentRecordEntity.getStudentStatus())
                || "TER".equalsIgnoreCase(graduationStudentRecordEntity.getStudentStatus())
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
            dogwood = (!"SCCP".equalsIgnoreCase(gradSearchStudent.getProgram()) && certificates.getDistributionDate() != null) ? "Y" : "N";
            sccDate = ("SCCP".equalsIgnoreCase(gradSearchStudent.getProgram()) && certificates.getDistributionDate() != null) ? simpleDateFormat.format(certificates.getDistributionDate()) : null;
            switch(certificateTypeCode) {
                case "E":
                    englishCert = certificateTypeCode;
                    break;
                case "EI":
                case "A":
                case "AI":
                case "O":
                    englishCert = "E";
                    break;
                case "S":
                case "F":
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

    private boolean validateData(GraduationStudentRecordEntity sourceEntity, GraduationStudentRecordEntity existingEntity, String accessToken) {
        boolean hasDataChanged = false;
        validateStudentStatus(existingEntity.getStudentStatus());
        if (!sourceEntity.getProgram().equalsIgnoreCase(existingEntity.getProgram())) {
            hasDataChanged = true;
            validateProgram(sourceEntity, accessToken);
        }
        
        if (sourceEntity.getSchoolOfRecord() != null && !sourceEntity.getSchoolOfRecord().equalsIgnoreCase(existingEntity.getSchoolOfRecord())) {
            hasDataChanged = true;
            validateSchool(sourceEntity.getSchoolOfRecord(), accessToken);
        }        
        
        if (sourceEntity.getSchoolAtGrad() != null && !sourceEntity.getSchoolAtGrad().equalsIgnoreCase(existingEntity.getSchoolAtGrad())) {
            hasDataChanged = true;
            validateSchool(sourceEntity.getSchoolAtGrad(), accessToken);
        }
        
        if ((sourceEntity.getStudentGrade() != null && !sourceEntity.getStudentGrade().equalsIgnoreCase(existingEntity.getStudentGrade()))
				|| (sourceEntity.getStudentStatus() != null && !sourceEntity.getStudentStatus().equalsIgnoreCase(existingEntity.getStudentStatus()))) {
            hasDataChanged = true;
            validateStudentGrade(sourceEntity,existingEntity,accessToken);
        }
        if (sourceEntity.getGpa() != null && !sourceEntity.getGpa().equalsIgnoreCase(existingEntity.getGpa())) {
            hasDataChanged = true;
            sourceEntity.setHonoursStanding(getHonoursFlag(sourceEntity.getGpa()));
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
    
    public StudentOptionalProgram updateStudentGradOptionalProgram(StudentOptionalProgramReq gradStudentOptionalProgramReq,String accessToken) {
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
        sourceObject.setOptionalProgramID(gradOptionalProgram != null ?gradOptionalProgram.getOptionalProgramID():null);
        if (gradStudentOptionalOptional.isPresent()) {
            StudentOptionalProgramEntity gradEntity = gradStudentOptionalOptional.get();
            if(gradEntity.getOptionalProgramID().equals(sourceObject.getOptionalProgramID())) {
                sourceObject.setStudentOptionalProgramData(gradEntity.getStudentOptionalProgramData());
            }else {
                sourceObject.setStudentOptionalProgramData(null);
            }
            BeanUtils.copyProperties(sourceObject, gradEntity, CREATE_USER, CREATE_DATE,"id");
            gradEntity.setOptionalProgramCompletionDate(sourceObject.getOptionalProgramCompletionDate());
            gradEntity = gradStudentOptionalProgramRepository.save(gradEntity);
            historyService.createStudentOptionalProgramHistory(gradEntity, USER_EDIT);
            return gradStudentOptionalProgramTransformer.transformToDTO(gradEntity);
        } else {
            sourceObject = gradStudentOptionalProgramRepository.save(sourceObject);
            historyService.createStudentOptionalProgramHistory(sourceObject, USER_EDIT);
            return gradStudentOptionalProgramTransformer.transformToDTO(sourceObject);
        }
    }

    public List<BatchGraduationStudentRecord> getStudentsForGraduation() {
        return graduationStatusRepository.findByRecalculateGradStatusForBatch("Y");
    }

    public List<BatchGraduationStudentRecord> getStudentsForProjectedGraduation() {
       return graduationStatusRepository.findByRecalculateProjectedGradForBatch("Y");
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
                    gradEntity = graduationStatusRepository.save(gradEntity);
                    historyService.createStudentHistory(gradEntity, "USERUNDOCMPL");
                    final GraduationStudentRecord graduationStatus = graduationStatusTransformer.transformToDTO(gradEntity);
                    final GradStatusEvent gradStatusEvent = createGradStatusEvent(gradEntity.getCreateUser(), gradEntity.getUpdateUser(),
                            graduationStatus, EventType.UPDATE_GRAD_STATUS, EventOutcome.GRAD_STATUS_UPDATED, "USERUNDOCMPL", accessToken);
                    gradStatusEventRepository.save(gradStatusEvent);
                    return Pair.of(graduationStatus, gradStatusEvent);
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
    
    private void deleteStudentAchievements(UUID studentID,String accessToken) {
    	try {
            webClient.delete().uri(String.format(constants.getDeleteStudentAchievements(), studentID))
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                    }).retrieve().onStatus(p -> p.value() == 404, error -> Mono.error(new Exception("Credential Not Found"))).bodyToMono(Integer.class).block();
        }catch (Exception e) {
            logger.info(e.getLocalizedMessage());
        }
	}

    private void archiveStudentAchievements(UUID studentID,String accessToken) {
        try {
            webClient.delete().uri(String.format(constants.getArchiveStudentAchievements(), studentID))
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                    }).retrieve().onStatus(p -> p.value() == 404, error -> Mono.error(new Exception("Credential Not Found"))).bodyToMono(Integer.class).block();
        }catch (Exception e) {
        logger.info(e.getLocalizedMessage());
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

    private GradStatusEvent createGradStatusEvent(String createUser, String updateUser,
                                                  GraduationStudentRecord graduationStatus,
                                                  EventType eventType, EventOutcome eventOutcome,
                                                  String activityCode,
                                                  String accessToken) throws JsonProcessingException {
        if (StringUtils.isBlank(graduationStatus.getPen())) {
            GradSearchStudent gradSearchStudent = gradStudentService.getStudentByStudentIDFromStudentAPI(graduationStatus.getStudentID().toString(), accessToken);
            if (gradSearchStudent != null) {
                graduationStatus.setPen(gradSearchStudent.getPen());
            }
        }
        String jsonString = JsonUtil.getJsonStringFromObject(graduationStatus);
        return GradStatusEvent.builder()
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .createUser(StringUtils.isBlank(createUser)? EducGradStudentApiConstants.DEFAULT_CREATED_BY : createUser)
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
            return graduationStatusTransformer.transformToDTO(gradEntity);
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
            gradEntity.setRecalculateProjectedGrad(null);
            if(batchId == null) {
                gradEntity.setRecalculateProjectedGrad("Y");
            }
            gradEntity.setStudentProjectedGradData(projectedClob);
            gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
            historyService.createStudentHistory(gradEntity, "GRADPROJECTED");
            return graduationStatusTransformer.transformToDTO(gradEntity);
        }
        return null;
    }

    public List<GraduationStudentRecord> getStudentDataByStudentIDs(List<UUID> studentIds) {
        return graduationStatusTransformer.tToDForBatch(graduationStatusRepository.findByStudentIDIn(studentIds));
    }

    public List<UUID> getStudentsForYearlyDistribution() {
        List<GraduationStudentRecordEntity> studentLists = graduationStatusRepository.findStudentsForYearlyDistribution();
        if(!studentLists.isEmpty())
            return studentLists.stream().map(GraduationStudentRecordEntity::getStudentID).collect(Collectors.toList());
        return  new ArrayList<>();
    }

    public GraduationStudentRecord getDataForBatch(UUID studentID,String accessToken) {
        GraduationStudentRecord ent = graduationStatusTransformer.transformToDTO(graduationStatusRepository.findByStudentID(studentID));
        return  processReceivedStudent(ent,accessToken);
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
        return graduationStatusTransformer.tToDForAmalgamation(graduationStatusRepository.findBySchoolOfRecord(schoolOfRecord),type);
    }

}
