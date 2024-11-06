package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GraduationStudentRecordGradStatus;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService.PAGE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GraduationStatusServiceTest {

    @Autowired EducGradStudentApiConstants constants;
    @Autowired GraduationStatusService graduationStatusService;
    @MockBean GradStudentService gradStudentService;
    @MockBean HistoryService historyService;
    @Autowired GradStudentReportService gradStudentReportService;
    @Autowired JsonTransformer jsonTransformer;
    @MockBean GraduationStudentRecordRepository graduationStatusRepository;
    @MockBean StudentOptionalProgramRepository gradStudentOptionalProgramRepository;
    @MockBean StudentCareerProgramRepository gradStudentCareerProgramRepository;
    @MockBean ReportGradStudentDataRepository reportGradStudentDataRepository;
    @MockBean StudentNonGradReasonRepository studentNonGradReasonRepository;
    @MockBean GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository;
    @MockBean CommonService commonService;
    @MockBean GradValidation validation;
    @Autowired WebClient webClient;

    @Autowired
    FetchGradStatusSubscriber fetchGradStatusSubscriber;
    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;

    @MockBean
    GraduationStudentRecordSearchRepository graduationStudentRecordSearchRepository;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testHasStudentGraduated_GivenValidProgramCompletionDate_ExpectTrue() throws EntityNotFoundException {
        UUID studentID = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setProgramCompletionDate(new java.util.Date());
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        boolean result = graduationStatusService.hasStudentGraduated(studentID);
        assertTrue(result);
    }

    @Test
    public void testHasStudentGraduated_GivenNoProgramCompletionDate_ExpectFalse() throws EntityNotFoundException {
        UUID studentID = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        boolean result = graduationStatusService.hasStudentGraduated(studentID);
        assertFalse(result);
    }

    @Test
    public void testHasStudentGraduated_GivenNotFound_ExpectEntityNotFoundException() throws EntityNotFoundException {
        UUID studentID = UUID.randomUUID();
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            graduationStatusService.hasStudentGraduated(studentID);
        });
    }

    @Test
    public void testGetGraduationStatus_GivenValidProgramCompletionDate_ExpectTrue() throws EntityNotFoundException {
        UUID studentID = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setProgramCompletionDate(new java.util.Date());
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        GraduationStudentRecord result = graduationStatusService.getGraduationStatus(studentID);
        assertNotNull(result);
    }

    @Test
    public void testGetGraduationStatus_givenNotFound_ExpectEntityNotFoundExcetpion() {
        UUID studentID = UUID.randomUUID();
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> {
            graduationStatusService.getGraduationStatus(studentID);
        });
    }

    @Test
    public void testGetGraduationStatusProjection_GivenValidProgramCompletionDate_ExpectTrue() throws EntityNotFoundException {
        UUID studentID = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setProgramCompletionDate(new java.util.Date());
        when(graduationStatusRepository.findByStudentID(studentID, GraduationStudentRecordGradStatus.class)).thenReturn(new GraduationStudentRecordGradStatus(studentID, "2018-EN", new java.util.Date()));
        GraduationStudentRecordGradStatus result = graduationStatusService.getGraduationStatusProjection(studentID);
        assertNotNull(result);
    }

    @Test
    public void testGetGraduationStatusProjection_givenNotFound_ExpectEntityNotFoundExcetpion() {
        UUID studentID = UUID.randomUUID();
        when(graduationStatusRepository.findByStudentID(studentID, GraduationStudentRecordGradStatus.class)).thenReturn(null);
        assertThrows(EntityNotFoundException.class, () -> {
            graduationStatusService.getGraduationStatusProjection(studentID);
        });
    }

    @Test
    public void testGetGraduationStatusForAlgorithm() {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("123456789");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setGpa("4");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        var result = graduationStatusService.getGraduationStatusForAlgorithm(studentID);
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getPen()).isEqualTo(graduationStatusEntity.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(graduationStatusEntity.getStudentStatus());
        assertThat(result.getRecalculateGradStatus()).isEqualTo(graduationStatusEntity.getRecalculateGradStatus());
        assertThat(result.getProgram()).isEqualTo(graduationStatusEntity.getProgram());
        assertThat(result.getSchoolOfRecord()).isEqualTo(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(graduationStatusEntity.getGpa());
    }

    @Test
    public void testGetGraduationStatus() {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("123456789");
        graduationStatusEntity.setStudentStatus("CUR");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setStudentStatus("A");

        StudentStatus studentStatus = new StudentStatus();
        studentStatus.setCode("CUR");
        studentStatus.setDescription(null);

        GradProgram program = new GradProgram();
        program.setProgramCode("2018-EN");
        program.setProgramName("Graduation Program 2018");

        School school = new School();
        school.setMinCode(mincode);
        school.setSchoolName("Test School");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));

        when(this.webClient.get()).thenReturn(requestHeadersUriMock);
        when(requestHeadersUriMock.uri(String.format(constants.getGradProgramNameUrl(),program.getProgramCode()))).thenReturn(requestHeadersMock);
        when(requestHeadersMock.headers(any(Consumer.class))).thenReturn(requestHeadersMock);
        when(requestHeadersMock.retrieve()).thenReturn(responseMock);
        when(responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(program));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeUrl(),mincode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        when(commonService.getSpecificStudentStatusCode(graduationStatusEntity.getStudentStatus())).thenReturn(studentStatus);

        var result = graduationStatusService.getGraduationStatus(studentID, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getPen()).isEqualTo(graduationStatusEntity.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(graduationStatusEntity.getStudentStatus());
        assertThat(result.getRecalculateGradStatus()).isEqualTo(graduationStatusEntity.getRecalculateGradStatus());
        assertThat(result.getProgram()).isEqualTo(graduationStatusEntity.getProgram());
        assertThat(result.getSchoolOfRecord()).isEqualTo(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(graduationStatusEntity.getGpa());

        assertThat(result.getStudentStatusName()).isEqualTo(studentStatus.getDescription());
        assertThat(result.getProgramName()).isEqualTo(program.getProgramName());
        assertThat(result.getSchoolName()).isEqualTo(school.getSchoolName());
        assertThat(result.getSchoolAtGradName()).isEqualTo(school.getSchoolName());
    }

    @Test
    public void testGetGraduationStatus_withoutprogram() {
        // ID
        UUID studentID = UUID.randomUUID();

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("123456789");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram(null);
        graduationStatusEntity.setSchoolOfRecord(null);
        graduationStatusEntity.setSchoolAtGrad(null);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setStudentStatus(null);
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        var result = graduationStatusService.getGraduationStatus(studentID, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getPen()).isEqualTo(graduationStatusEntity.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(graduationStatusEntity.getStudentStatus());
        assertThat(result.getRecalculateGradStatus()).isEqualTo(graduationStatusEntity.getRecalculateGradStatus());
        assertThat(result.getProgram()).isEqualTo(graduationStatusEntity.getProgram());
        assertThat(result.getSchoolOfRecord()).isEqualTo(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(graduationStatusEntity.getGpa());
    }

    @Test
    public void testSaveGraduationStatusAsNew() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();

        GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen("123456789");
        graduationStatus.setStudentStatus("A");
        graduationStatus.setRecalculateGradStatus("Y");
        graduationStatus.setProgram("2018-EN");
        graduationStatus.setSchoolOfRecord(null);
        graduationStatus.setSchoolAtGrad(null);
        graduationStatus.setGpa("4");
        graduationStatus.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM"));

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatus, graduationStatusEntity);
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.empty());
        when(graduationStatusRepository.saveAndFlush(any(GraduationStudentRecordEntity.class))).thenReturn(graduationStatusEntity);

        var response = graduationStatusService.saveGraduationStatus(studentID, graduationStatus, null,"accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getPen()).isEqualTo(graduationStatusEntity.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(graduationStatusEntity.getStudentStatus());
        assertThat(result.getProgram()).isEqualTo(graduationStatusEntity.getProgram());
        assertThat(result.getSchoolOfRecord()).isEqualTo(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(graduationStatusEntity.getGpa());

        assertThat(result.getRecalculateGradStatus()).isEqualTo(graduationStatus.getRecalculateGradStatus());
        assertThat(result.getProgramCompletionDate()).isEqualTo(graduationStatus.getProgramCompletionDate());
    }

    @Test
    public void testSaveGraduationStatus() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("123456789");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        GraduationStudentRecord input = new GraduationStudentRecord();
        BeanUtils.copyProperties(graduationStatusEntity, input);
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus(null);
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        var response = graduationStatusService.saveGraduationStatus(studentID, input,null, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getPen()).isEqualTo(graduationStatusEntity.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(graduationStatusEntity.getStudentStatus());
        assertThat(result.getProgram()).isEqualTo(graduationStatusEntity.getProgram());
        assertThat(result.getSchoolOfRecord()).isEqualTo(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(graduationStatusEntity.getGpa());

        assertThat(result.getRecalculateGradStatus()).isNull();
        assertThat(result.getProgramCompletionDate()).isEqualTo(input.getProgramCompletionDate());
    }

    @Test
    public void testUpdateGraduationStatus_givenSameData_whenDataIsValidated_thenReturnSuccess() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setStudentGrade("12");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        GraduationStudentRecord input = new GraduationStudentRecord();
        BeanUtils.copyProperties(graduationStatusEntity, input);
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        StudentOptionalProgramEntity optEnt = new StudentOptionalProgramEntity();
        optEnt.setId(UUID.randomUUID());
        optEnt.setStudentID(studentID);
        optEnt.setOptionalProgramID(UUID.randomUUID());
        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);
        when(gradStudentOptionalProgramRepository.findByStudentID(studentID)).thenReturn(List.of(optEnt));

        var response = graduationStatusService.updateGraduationStatus(studentID, input, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(savedGraduationStatus.getStudentID());
        assertThat(result.getPen()).isEqualTo(savedGraduationStatus.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(savedGraduationStatus.getStudentStatus());
        assertThat(result.getStudentGrade()).isEqualTo(savedGraduationStatus.getStudentGrade());
        assertThat(result.getProgram()).isEqualTo(savedGraduationStatus.getProgram());
        assertThat(result.getSchoolOfRecord()).isEqualTo(savedGraduationStatus.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(savedGraduationStatus.getGpa());

        assertThat(result.getRecalculateGradStatus()).isNull();
        assertThat(result.getProgramCompletionDate()).isEqualTo(input.getProgramCompletionDate());
    }

    @Test
    public void testUpdateGraduationStatus_givenDifferentStudentGrades_whenStudentGradeIsValidated_thenReturnSuccess() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("CUR");
        graduationStatusEntity.setStudentGrade("11");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        GraduationStudentRecord input = new GraduationStudentRecord();
        BeanUtils.copyProperties(graduationStatusEntity, input);
        input.setRecalculateGradStatus(null);
        input.setStudentGrade("12");
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setStudentGrade("12");
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        Student student = new Student();
        student.setStudentID(studentID.toString());
        student.setPen(pen);
        student.setStatusCode("A");
        student.setGradeCode("12");
        student.setEmail("qa@test.com");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getPenStudentApiByStudentIdUrl(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Student.class)).thenReturn(Mono.just(student));

        var response = graduationStatusService.updateGraduationStatus(studentID, input, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(savedGraduationStatus.getStudentID());
        assertThat(result.getPen()).isEqualTo(savedGraduationStatus.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(savedGraduationStatus.getStudentStatus());
        assertThat(result.getStudentGrade()).isEqualTo(savedGraduationStatus.getStudentGrade());
        assertThat(result.getProgram()).isEqualTo(savedGraduationStatus.getProgram());
        assertThat(result.getSchoolOfRecord()).isEqualTo(savedGraduationStatus.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(savedGraduationStatus.getGpa());

        assertThat(result.getRecalculateGradStatus()).isEqualTo(savedGraduationStatus.getRecalculateGradStatus());
        assertThat(result.getProgramCompletionDate()).isEqualTo(input.getProgramCompletionDate());
    }

    @Test
    public void testUpdateGraduationStatus_givenDifferentPrograms_whenProgramIsValidated_thenReturnSuccess() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setStudentGrade("12");
        graduationStatusEntity.setProgram("2018-pf");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        GraduationStudentRecord input = new GraduationStudentRecord();
        BeanUtils.copyProperties(graduationStatusEntity, input);
        input.setRecalculateGradStatus(null);
        input.setProgram("2018-EN");
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setProgram("2018-EN");
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        GradProgram program = new GradProgram();
        program.setProgramCode("2018-EN");
        program.setProgramName("Graduation Program 2018");

        StudentOptionalProgramEntity optEnt = new StudentOptionalProgramEntity();
        optEnt.setId(UUID.randomUUID());
        optEnt.setStudentID(studentID);
        optEnt.setOptionalProgramID(UUID.randomUUID());
        when(gradStudentOptionalProgramRepository.findByStudentID(studentID)).thenReturn(List.of(optEnt));

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradProgramNameUrl(),program.getProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(program));

        when(this.webClient.delete()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getDeleteStudentAchievements(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Integer.class)).thenReturn(Mono.just(0));

        var response = graduationStatusService.updateGraduationStatus(studentID, input, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(savedGraduationStatus.getStudentID());
        assertThat(result.getPen()).isEqualTo(savedGraduationStatus.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(savedGraduationStatus.getStudentStatus());
        assertThat(result.getProgram()).isEqualTo(savedGraduationStatus.getProgram());
        assertThat(result.getSchoolOfRecord()).isEqualTo(savedGraduationStatus.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(savedGraduationStatus.getGpa());

        assertThat(result.getRecalculateGradStatus()).isEqualTo(savedGraduationStatus.getRecalculateGradStatus());
        assertThat(result.getProgramCompletionDate()).isEqualTo(input.getProgramCompletionDate());
    }

    @Test
    public void testUpdateGraduationStatus_givenDifferentPrograms_whenProgramIsValidated_thenReturnSuccess_SCCP() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setStudentGrade("12");
        graduationStatusEntity.setProgram("SCCP");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        GraduationStudentRecord input = new GraduationStudentRecord();
        BeanUtils.copyProperties(graduationStatusEntity, input);
        input.setRecalculateGradStatus(null);
        input.setProgram("2018-EN");
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setProgram("2018-EN");
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        GradProgram program = new GradProgram();
        program.setProgramCode("2018-EN");
        program.setProgramName("Graduation Program 2018");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradProgramNameUrl(),program.getProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(program));

        when(this.webClient.delete()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getArchiveStudentAchievements(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Integer.class)).thenReturn(Mono.just(0));

        var response = graduationStatusService.updateGraduationStatus(studentID, input, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(savedGraduationStatus.getStudentID());
        assertThat(result.getPen()).isEqualTo(savedGraduationStatus.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(savedGraduationStatus.getStudentStatus());
        assertThat(result.getProgram()).isEqualTo(savedGraduationStatus.getProgram());
        assertThat(result.getSchoolOfRecord()).isEqualTo(savedGraduationStatus.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(savedGraduationStatus.getGpa());

        assertThat(result.getRecalculateGradStatus()).isEqualTo(savedGraduationStatus.getRecalculateGradStatus());
        assertThat(result.getProgramCompletionDate()).isEqualTo(input.getProgramCompletionDate());
    }

    @Test
    public void testSaveGraduationStatus_givenBatchId_whenProgramCompletionDate_isFuture_thenReturnSuccess_SCCP() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String mincode = "12345678";
        Long batchId = 1234L;
        java.util.Date futureDate = DateUtils.addMonths(new Date(System.currentTimeMillis()), 1);
        Date programCompletionDate = new Date(futureDate.getTime());

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("123456789");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("SCCP");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(programCompletionDate);

        GraduationStudentRecord input = new GraduationStudentRecord();
        BeanUtils.copyProperties(graduationStatusEntity, input);
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(programCompletionDate, "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());
        savedGraduationStatus.setPen(null);

        GradSearchStudent gss = new GradSearchStudent();
        gss.setPen("123456789");
        gss.setMincode(mincode);
        gss.setStudentID(studentID.toString());
        gss.setStudentStatus("CUR");
        gss.setStudentGrade("12");
        gss.setLegalFirstName("Test");
        gss.setLegalMiddleNames("Master");
        gss.setLegalLastName("QA");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);
        when(gradStudentService.getStudentByStudentIDFromStudentAPI(studentID.toString(), "accessToken")).thenReturn(gss);

        var response = graduationStatusService.saveGraduationStatus(studentID, input, batchId, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getPen()).isEqualTo(graduationStatusEntity.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(graduationStatusEntity.getStudentStatus());
        assertThat(result.getProgram()).isEqualTo(graduationStatusEntity.getProgram());
        assertThat(result.getSchoolOfRecord()).isEqualTo(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(graduationStatusEntity.getGpa());

        assertThat(result.getRecalculateGradStatus()).isEqualTo("Y");
        assertThat(result.getProgramCompletionDate()).isEqualTo(input.getProgramCompletionDate());
    }

    @Test
    public void testUpdateGraduationStatus_givenDifferentPrograms_when1950ProgramIsValidated_thenReturnErrorWithEmptyObject() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setStudentGrade("12");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        GraduationStudentRecord input = new GraduationStudentRecord();
        BeanUtils.copyProperties(graduationStatusEntity, input);
        input.setRecalculateGradStatus(null);
        input.setProgram("1950-en");
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setProgram("2018-EN");
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        GradProgram program = new GradProgram();
        program.setProgramCode("1950-en");
        program.setProgramName("Graduation Program 1950");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);
        when(validation.hasErrors()).thenReturn(true);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradProgramNameUrl(),program.getProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(program));

        var response = graduationStatusService.updateGraduationStatus(studentID, input, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isNull();
        assertThat(result.getPen()).isNull();
        assertThat(result.getStudentStatus()).isNull();
        assertThat(result.getProgram()).isNull();
        assertThat(result.getSchoolOfRecord()).isNull();
        assertThat(result.getGpa()).isNull();

        assertThat(result.getRecalculateGradStatus()).isNull();
        assertThat(result.getProgramCompletionDate()).isNull();
    }

    @Test
    public void testUpdateGraduationStatus_givenDifferentPrograms_whenProgramIsValidatedForAdultGrade_thenReturnErrorWithEmptyObject() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setStudentGrade("AD");
        graduationStatusEntity.setProgram("2018-pf");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        GraduationStudentRecord input = new GraduationStudentRecord();
        BeanUtils.copyProperties(graduationStatusEntity, input);
        input.setRecalculateGradStatus(null);
        input.setProgram("2018-EN");
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setProgram("2018-EN");
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        GradProgram program = new GradProgram();
        program.setProgramCode("2018-EN");
        program.setProgramName("Graduation Program 2018");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);
        when(validation.hasErrors()).thenReturn(true);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradProgramNameUrl(),program.getProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(program));

        var response = graduationStatusService.updateGraduationStatus(studentID, input, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isNull();
        assertThat(result.getPen()).isNull();
        assertThat(result.getStudentStatus()).isNull();
        assertThat(result.getProgram()).isNull();
        assertThat(result.getSchoolOfRecord()).isNull();
        assertThat(result.getGpa()).isNull();

        assertThat(result.getRecalculateGradStatus()).isNull();
        assertThat(result.getProgramCompletionDate()).isNull();
    }

    @Test
    public void testUpdateGraduationStatus_givenDifferentSchoolOfRecords_whenSchoolIsValidated_thenReturnSuccess() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";
        String newMincode = "87654321";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setStudentGrade("12");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        GraduationStudentRecord input = new GraduationStudentRecord();
        BeanUtils.copyProperties(graduationStatusEntity, input);
        input.setRecalculateGradStatus(null);
        input.setSchoolOfRecord(newMincode);
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setSchoolOfRecord(newMincode);
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        School school = new School();
        school.setMinCode(newMincode);
        school.setSchoolName("Test School");
        school.setOpenFlag("Y");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeUrl(),newMincode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        var response = graduationStatusService.updateGraduationStatus(studentID, input, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(savedGraduationStatus.getStudentID());
        assertThat(result.getPen()).isEqualTo(savedGraduationStatus.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(savedGraduationStatus.getStudentStatus());
        assertThat(result.getProgram()).isEqualTo(savedGraduationStatus.getProgram());
        assertThat(result.getSchoolOfRecord()).isEqualTo(savedGraduationStatus.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(savedGraduationStatus.getGpa());

        assertThat(result.getRecalculateGradStatus()).isEqualTo(savedGraduationStatus.getRecalculateGradStatus());
        assertThat(result.getProgramCompletionDate()).isEqualTo(input.getProgramCompletionDate());
    }

    @Test
    public void testUpdateGraduationStatus_givenDifferentSchoolOfGrads_whenSchoolIsValidated_thenReturnSuccess() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";
        String newMincode = "87654321";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setStudentGrade("12");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        GraduationStudentRecord input = new GraduationStudentRecord();
        BeanUtils.copyProperties(graduationStatusEntity, input);
        input.setRecalculateGradStatus(null);
        input.setSchoolAtGrad(newMincode);
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setSchoolAtGrad(newMincode);
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        School school = new School();
        school.setMinCode(newMincode);
        school.setSchoolName("Test School");
        school.setOpenFlag("Y");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeUrl(),newMincode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        var response = graduationStatusService.updateGraduationStatus(studentID, input, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(savedGraduationStatus.getStudentID());
        assertThat(result.getPen()).isEqualTo(savedGraduationStatus.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(savedGraduationStatus.getStudentStatus());
        assertThat(result.getProgram()).isEqualTo(savedGraduationStatus.getProgram());
        assertThat(result.getSchoolAtGrad()).isEqualTo(savedGraduationStatus.getSchoolAtGrad());
        assertThat(result.getGpa()).isEqualTo(savedGraduationStatus.getGpa());

        assertThat(result.getRecalculateGradStatus()).isEqualTo(savedGraduationStatus.getRecalculateGradStatus());
        assertThat(result.getProgramCompletionDate()).isEqualTo(input.getProgramCompletionDate());
    }

    @Test
    public void testUpdateGraduationStatus_givenDifferentGPAs_whenHonoursStandingIsValidated_thenReturnSuccess() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setStudentGrade("12");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("3");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        GraduationStudentRecord input = new GraduationStudentRecord();
        BeanUtils.copyProperties(graduationStatusEntity, input);
        input.setRecalculateGradStatus(null);
        input.setGpa("4");
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setGpa("4");
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        var response = graduationStatusService.updateGraduationStatus(studentID, input, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(savedGraduationStatus.getStudentID());
        assertThat(result.getPen()).isEqualTo(savedGraduationStatus.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(savedGraduationStatus.getStudentStatus());
        assertThat(result.getProgram()).isEqualTo(savedGraduationStatus.getProgram());
        assertThat(result.getSchoolAtGrad()).isEqualTo(savedGraduationStatus.getSchoolAtGrad());
        assertThat(result.getGpa()).isEqualTo(savedGraduationStatus.getGpa());

        assertThat(result.getRecalculateGradStatus()).isEqualTo(savedGraduationStatus.getRecalculateGradStatus());
        assertThat(result.getProgramCompletionDate()).isEqualTo(input.getProgramCompletionDate());
    }

    @Test
    public void testGetStudentGradOptionalProgram() {
        // ID
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        when(gradStudentOptionalProgramRepository.findByStudentID(studentID)).thenReturn(List.of(gradStudentOptionalProgramEntity));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramNameUrl(),optionalProgramID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        var result = graduationStatusService.getStudentGradOptionalProgram(studentID, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        StudentOptionalProgram responseStudentOptionalProgram = result.get(0);
        assertThat(responseStudentOptionalProgram.getStudentID()).isEqualTo(gradStudentOptionalProgramEntity.getStudentID());
        assertThat(responseStudentOptionalProgram.getOptionalProgramID()).isEqualTo(gradStudentOptionalProgramEntity.getOptionalProgramID());
        assertThat(responseStudentOptionalProgram.getOptionalProgramName()).isEqualTo(optionalProgram.getOptionalProgramName());
        assertThat(responseStudentOptionalProgram.getOptionalProgramCode()).isEqualTo(optionalProgram.getOptProgramCode());
        assertThat(responseStudentOptionalProgram.getProgramCode()).isEqualTo(optionalProgram.getGraduationProgramCode());
    }

    @Test
    public void testSaveStudentGradOptionalProgram() {
        // ID
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        when(gradStudentOptionalProgramRepository.findById(gradStudentOptionalProgramID)).thenReturn(Optional.of(gradStudentOptionalProgramEntity));
        when(gradStudentOptionalProgramRepository.save(gradStudentOptionalProgramEntity)).thenReturn(gradStudentOptionalProgramEntity);

        var result = graduationStatusService.saveStudentGradOptionalProgram(studentOptionalProgram);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentOptionalProgramEntity.getId());
        assertThat(result.getOptionalProgramID()).isEqualTo(gradStudentOptionalProgramEntity.getOptionalProgramID());
        assertThat(result.getOptionalProgramCompletionDate()).isEqualTo(EducGradStudentApiUtils.parseDateFromString(studentOptionalProgram.getOptionalProgramCompletionDate()));
    }

    @Test
    public void testCreateCRUDStudentGradOptionalProgram() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        Optional<GraduationStudentRecordEntity> optionalGraduationStudentRecordEntity = Optional.of(graduationStudentRecordEntity);

        when(graduationStatusRepository.findById(studentID)).thenReturn(optionalGraduationStudentRecordEntity);
        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.empty());
        when(gradStudentOptionalProgramRepository.save(any())).thenReturn(gradStudentOptionalProgramEntity);
        doNothing().when(historyService).createStudentOptionalProgramHistory(gradStudentOptionalProgramEntity, "USER_CREATE");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationAllFlags(studentID, "Y", "Y");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationRecalculateGradStatusFlag(studentID, "Y");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramNameUrl(),optionalProgramID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        var result = graduationStatusService.createStudentOptionalProgram(studentID, optionalProgramID, "accessToken");
        assertThat(result).isNotNull();

        graduationStudentRecordEntity.setStudentStatus("ARC");
        result = graduationStatusService.createStudentOptionalProgram(studentID, optionalProgramID, "accessToken");

        assertThat(result).isNotNull();
    }

    @Test
    public void testCreateCRUDStudentGradOptionalProgram_when_already_Exists() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        Optional<GraduationStudentRecordEntity> optionalGraduationStudentRecordEntity = Optional.of(graduationStudentRecordEntity);

        when(graduationStatusRepository.findById(studentID)).thenReturn(optionalGraduationStudentRecordEntity);
        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.of(gradStudentOptionalProgramEntity));
        when(gradStudentOptionalProgramRepository.save(any())).thenReturn(gradStudentOptionalProgramEntity);
        doNothing().when(historyService).createStudentOptionalProgramHistory(gradStudentOptionalProgramEntity, "USER_CREATE");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationAllFlags(studentID, "Y", "Y");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationRecalculateGradStatusFlag(studentID, "Y");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramNameUrl(),optionalProgramID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        var result = graduationStatusService.createStudentOptionalProgram(studentID, optionalProgramID, "accessToken");
        assertThat(result).isNull();

        graduationStudentRecordEntity.setStudentStatus("ARC");
        result = graduationStatusService.createStudentOptionalProgram(studentID, optionalProgramID, "accessToken");

        assertThat(result).isNull();
    }

    @Test
    public void testCreateCRUDStudentGradOptionalProgram_whenGivenOptionalProgramID_is_Null() {
        // ID
        UUID studentID = UUID.randomUUID();

        doThrow(new EntityNotFoundException("Is Required")).when(validation).addErrorAndStop(any());

        boolean isBadRequest = false;
        try {
            graduationStatusService.createStudentOptionalProgram(studentID, null, "accessToken");
        } catch (EntityNotFoundException ex) {
            isBadRequest = true;
        }

        assertThat(isBadRequest).isTrue();
    }

    @Test
    public void testCreateCRUDStudentGradOptionalProgram_whenOptionalProgram_is_notFound() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        Optional<GraduationStudentRecordEntity> optionalGraduationStudentRecordEntity = Optional.of(graduationStudentRecordEntity);

        when(graduationStatusRepository.findById(studentID)).thenReturn(optionalGraduationStudentRecordEntity);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramNameUrl(),optionalProgramID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.empty());

        when(validation.hasErrors()).thenReturn(true);
        doThrow(new EntityNotFoundException("Not Found")).when(validation).addNotFoundErrorAndStop(any());

        boolean isNotFound = false;
        try {
            graduationStatusService.createStudentOptionalProgram(studentID, optionalProgramID, "accessToken");
        } catch (EntityNotFoundException ex) {
            isNotFound = true;
        }

        assertThat(isNotFound).isTrue();
    }

    @Test
    public void testCreateCRUDStudentGradOptionalProgram_whenOptionalProgram_has_Null_PrimaryKey() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(null);
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("CP");
        optionalProgram.setOptionalProgramName("Career Program");

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        Optional<GraduationStudentRecordEntity> optionalGraduationStudentRecordEntity = Optional.of(graduationStudentRecordEntity);

        when(graduationStatusRepository.findById(studentID)).thenReturn(optionalGraduationStudentRecordEntity);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramNameUrl(),optionalProgramID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        when(validation.hasErrors()).thenReturn(true);
        doThrow(new EntityNotFoundException("Not Found")).when(validation).addNotFoundErrorAndStop(any());

        boolean isNotFound = false;
        try {
            graduationStatusService.createStudentOptionalProgram(studentID, optionalProgramID, "accessToken");
        } catch (EntityNotFoundException ex) {
            isNotFound = true;
        }

        assertThat(isNotFound).isTrue();
    }

    @Test
    public void testCreateCRUDStudentGradCareerPrograms() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();
        String gradProgram = "2018-EN";
        String careerProgramCode = "XA";

        // CP
        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);

        StudentCareerProgramEntity gradStudentCareerProgramEntity = new StudentCareerProgramEntity();
        gradStudentCareerProgramEntity.setStudentID(studentID);
        gradStudentCareerProgramEntity.setCareerProgramCode(careerProgramCode);

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode(gradProgram);
        optionalProgram.setOptProgramCode("CP");
        optionalProgram.setOptionalProgramName("Career Program");

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setProgram(gradProgram);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        Optional<GraduationStudentRecordEntity> optionalGraduationStudentRecordEntity = Optional.of(graduationStudentRecordEntity);

        StudentCareerProgramRequestDTO studentCareerProgramReq = new StudentCareerProgramRequestDTO();
        studentCareerProgramReq.getCareerProgramCodes().addAll(Arrays.asList(careerProgramCode, "XB"));

        CareerProgram careerProgram = new CareerProgram();
        careerProgram.setCode(careerProgramCode);

        when(graduationStatusRepository.findById(studentID)).thenReturn(optionalGraduationStudentRecordEntity);
        when(gradStudentCareerProgramRepository.findByStudentIDAndCareerProgramCode(studentID, careerProgramCode)).thenReturn(Optional.empty());
        when(gradStudentCareerProgramRepository.save(any())).thenReturn(gradStudentCareerProgramEntity);
        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.empty());
        when(gradStudentOptionalProgramRepository.save(any())).thenReturn(gradStudentOptionalProgramEntity);
        doNothing().when(historyService).createStudentOptionalProgramHistory(gradStudentOptionalProgramEntity, "USER_CREATE");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationAllFlags(studentID, "Y", "Y");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationRecalculateGradStatusFlag(studentID, "Y");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramDetailsUrl(),optionalProgram.getGraduationProgramCode(), optionalProgram.getOptProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getCareerProgramByCodeUrl(),careerProgramCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CareerProgram.class)).thenReturn(Mono.just(careerProgram));

        var result = graduationStatusService.createStudentCareerPrograms(studentID, studentCareerProgramReq, "123");
        assertThat(result).isNotNull();

        graduationStudentRecordEntity.setStudentStatus("ARC");
        result = graduationStatusService.createStudentCareerPrograms(studentID, studentCareerProgramReq, "123");

        assertThat(result).isNotNull();
    }

    @Test
    public void testCreateCRUDStudentGradCareerPrograms_when_already_Exists() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();
        String gradProgram = "2018-EN";
        String careerProgramCode = "XA";

        // CP
        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);

        StudentCareerProgramEntity gradStudentCareerProgramEntity = new StudentCareerProgramEntity();
        gradStudentCareerProgramEntity.setStudentID(studentID);
        gradStudentCareerProgramEntity.setCareerProgramCode(careerProgramCode);

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode(gradProgram);
        optionalProgram.setOptProgramCode("CP");
        optionalProgram.setOptionalProgramName("Career Program");

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setProgram(gradProgram);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        Optional<GraduationStudentRecordEntity> optionalGraduationStudentRecordEntity = Optional.of(graduationStudentRecordEntity);

        StudentCareerProgramRequestDTO studentCareerProgramReq = new StudentCareerProgramRequestDTO();
        studentCareerProgramReq.getCareerProgramCodes().addAll(Arrays.asList(careerProgramCode));

        CareerProgram careerProgram = new CareerProgram();
        careerProgram.setCode(careerProgramCode);

        when(graduationStatusRepository.findById(studentID)).thenReturn(optionalGraduationStudentRecordEntity);
        when(gradStudentCareerProgramRepository.findByStudentIDAndCareerProgramCode(studentID, careerProgramCode)).thenReturn(Optional.of(gradStudentCareerProgramEntity));
        when(gradStudentCareerProgramRepository.save(any())).thenReturn(gradStudentCareerProgramEntity);
        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.empty());
        when(gradStudentOptionalProgramRepository.save(any())).thenReturn(gradStudentOptionalProgramEntity);
        doNothing().when(historyService).createStudentOptionalProgramHistory(gradStudentOptionalProgramEntity, "USER_CREATE");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationAllFlags(studentID, "Y", "Y");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationRecalculateGradStatusFlag(studentID, "Y");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramDetailsUrl(),optionalProgram.getGraduationProgramCode(), optionalProgram.getOptProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getCareerProgramByCodeUrl(),careerProgramCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CareerProgram.class)).thenReturn(Mono.just(careerProgram));

        var result = graduationStatusService.createStudentCareerPrograms(studentID, studentCareerProgramReq, "123");
        assertThat(result).isEmpty();

        graduationStudentRecordEntity.setStudentStatus("ARC");
        result = graduationStatusService.createStudentCareerPrograms(studentID, studentCareerProgramReq, "123");

        assertThat(result).isEmpty();
    }

    @Test
    public void testCreateCRUDStudentGradCareerProgram_whenGivenCareerProgramCode_is_Null() {
        // ID
        UUID studentID = UUID.randomUUID();

        doThrow(new GradBusinessRuleException("Is Required")).when(validation).addErrorAndStop(any());

        StudentCareerProgramRequestDTO requestDTO = new StudentCareerProgramRequestDTO();
        boolean isBadRequest = false;
        try {
            graduationStatusService.createStudentCareerPrograms(studentID, requestDTO, "accessToken");
        } catch (GradBusinessRuleException ex) {
            isBadRequest = true;
        }

        assertThat(isBadRequest).isTrue();
    }

    @Test
    public void testCreateCRUDStudentGradCareerProgram_whenCareerProgram_is_notFound() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();
        String careerProgramCode = "XA";

        StudentCareerProgramEntity gradStudentCareerProgramEntity = new StudentCareerProgramEntity();
        gradStudentCareerProgramEntity.setStudentID(studentID);
        gradStudentCareerProgramEntity.setCareerProgramCode(careerProgramCode);

        StudentCareerProgram studentCareerProgram = new StudentCareerProgram();
        BeanUtils.copyProperties(gradStudentCareerProgramEntity, studentCareerProgram);

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        Optional<GraduationStudentRecordEntity> optionalGraduationStudentRecordEntity = Optional.of(graduationStudentRecordEntity);

        StudentCareerProgramRequestDTO requestDTO = new StudentCareerProgramRequestDTO();
        requestDTO.getCareerProgramCodes().addAll(Arrays.asList(careerProgramCode, "XB"));

        when(graduationStatusRepository.findById(studentID)).thenReturn(optionalGraduationStudentRecordEntity);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getCareerProgramByCodeUrl(),careerProgramCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CareerProgram.class)).thenReturn(Mono.empty());

        when(validation.hasErrors()).thenReturn(true);
        doThrow(new EntityNotFoundException("Not Found")).when(validation).stopOnNotFoundErrors();

        boolean isNotFound= false;
        try {
            graduationStatusService.createStudentCareerPrograms(studentID, requestDTO, "accessToken");
        } catch (GradBusinessRuleException ex) {
            isNotFound = true;
        }

        assertThat(isNotFound).isTrue();
    }

    @Test
    public void testCreateCRUDStudentGradCareerProgram_whenCareerProgram_is_Null_PrimaryKey() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();
        String careerProgramCode = "XA";

        StudentCareerProgramEntity gradStudentCareerProgramEntity = new StudentCareerProgramEntity();
        gradStudentCareerProgramEntity.setStudentID(studentID);
        gradStudentCareerProgramEntity.setCareerProgramCode(careerProgramCode);

        StudentCareerProgram studentCareerProgram = new StudentCareerProgram();
        BeanUtils.copyProperties(gradStudentCareerProgramEntity, studentCareerProgram);

        CareerProgram careerProgram = new CareerProgram();

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        Optional<GraduationStudentRecordEntity> optionalGraduationStudentRecordEntity = Optional.of(graduationStudentRecordEntity);

        StudentCareerProgramRequestDTO requestDTO = new StudentCareerProgramRequestDTO();
        requestDTO.getCareerProgramCodes().addAll(Arrays.asList(careerProgramCode, "XB"));

        when(graduationStatusRepository.findById(studentID)).thenReturn(optionalGraduationStudentRecordEntity);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getCareerProgramByCodeUrl(),careerProgramCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CareerProgram.class)).thenReturn(Mono.just(careerProgram));

        when(validation.hasErrors()).thenReturn(true);
        doThrow(new EntityNotFoundException("Not Found")).when(validation).stopOnNotFoundErrors();

        boolean isNotFound= false;
        try {
            graduationStatusService.createStudentCareerPrograms(studentID, requestDTO, "accessToken");
        } catch (GradBusinessRuleException ex) {
            isNotFound = true;
        }

        assertThat(isNotFound).isTrue();
    }

    @Test
    public void testDeleteCRUDStudentGradOptionalProgram() {
        // ID
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        Optional<GraduationStudentRecordEntity> optionalGraduationStudentRecordEntity = Optional.of(graduationStudentRecordEntity);

        when(graduationStatusRepository.findById(studentID)).thenReturn(optionalGraduationStudentRecordEntity);
        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.of(gradStudentOptionalProgramEntity));
        doNothing().when(gradStudentOptionalProgramRepository).delete(gradStudentOptionalProgramEntity);
        doNothing().when(historyService).createStudentOptionalProgramHistory(gradStudentOptionalProgramEntity, "USER_DELETE");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationAllFlags(studentID, "Y", "Y");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationRecalculateGradStatusFlag(studentID, "Y");

        graduationStatusService.deleteStudentOptionalProgram(studentID, optionalProgramID, "123");
        assertThat(graduationStudentRecordEntity).isNotNull();
    }

    @Test
    public void testDeleteCRUDStudentGradCareerProgram() {
        // ID
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();
        String gradProgram = "2018-EN";

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode(gradProgram);
        optionalProgram.setOptProgramCode("CP");
        optionalProgram.setOptionalProgramName("Career Program");

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setProgram(gradProgram);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        Optional<GraduationStudentRecordEntity> optionalGraduationStudentRecordEntity = Optional.of(graduationStudentRecordEntity);

        historyService.createStudentOptionalProgramHistory(gradStudentOptionalProgramEntity, "USER_DELETE");

        when(graduationStatusRepository.findById(studentID)).thenReturn(optionalGraduationStudentRecordEntity);
        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.of(gradStudentOptionalProgramEntity));
        doNothing().when(gradStudentOptionalProgramRepository).delete(gradStudentOptionalProgramEntity);
        doNothing().when(historyService).createStudentOptionalProgramHistory(gradStudentOptionalProgramEntity, "USER_DELETE");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationAllFlags(studentID, "Y", "Y");
        doNothing().when(graduationStatusRepository).updateGradStudentRecalculationRecalculateGradStatusFlag(studentID, "Y");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramDetailsUrl(),optionalProgram.getGraduationProgramCode(), optionalProgram.getOptProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        StudentCareerProgramEntity studentCareerProgramEntity = new StudentCareerProgramEntity();
        studentCareerProgramEntity.setCareerProgramCode("AB");
        when(gradStudentCareerProgramRepository.findByStudentIDAndCareerProgramCode(studentID, "AB")).thenReturn(Optional.of(studentCareerProgramEntity));
        when(gradStudentCareerProgramRepository.findByStudentID(studentID)).thenReturn(new ArrayList<>());

        graduationStatusService.deleteStudentCareerProgram(studentID, "AB", "123");
        assertThat(graduationStudentRecordEntity).isNotNull();
    }

    @Test
    public void testUpdateStudentGradOptionalProgram() {
        // ID
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();
        String pen = "123456789";

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        StudentOptionalProgramReq gradStudentOptionalProgramReq = new StudentOptionalProgramReq();
        gradStudentOptionalProgramReq.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramReq.setStudentID(studentID);
        gradStudentOptionalProgramReq.setPen(pen);
        gradStudentOptionalProgramReq.setMainProgramCode("2018-EN");
        gradStudentOptionalProgramReq.setOptionalProgramCode("FI");
        gradStudentOptionalProgramReq.setOptionalProgramCompletionDate(studentOptionalProgram.getOptionalProgramCompletionDate());

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        when(gradStudentOptionalProgramRepository.findById(gradStudentOptionalProgramID)).thenReturn(Optional.of(gradStudentOptionalProgramEntity));
        when(gradStudentOptionalProgramRepository.save(gradStudentOptionalProgramEntity)).thenReturn(gradStudentOptionalProgramEntity);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramDetailsUrl(),optionalProgram.getGraduationProgramCode(), optionalProgram.getOptProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        var result = graduationStatusService.updateStudentGradOptionalProgram(gradStudentOptionalProgramReq, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentOptionalProgramEntity.getId());
        assertThat(result.getOptionalProgramID()).isEqualTo(gradStudentOptionalProgramEntity.getOptionalProgramID());
        assertThat(result.getOptionalProgramCompletionDate()).isEqualTo(EducGradStudentApiUtils.parseDateFromString(studentOptionalProgram.getOptionalProgramCompletionDate()));
    }

    @Test
    public void testGetStudentsForGraduation() {
        UUID studentID = UUID.randomUUID();
        when(graduationStatusRepository.findByRecalculateGradStatusForBatch("Y")).thenReturn(List.of(studentID));
        var result = graduationStatusService.getStudentsForGraduation();
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        UUID responseStudentID = result.get(0);
        assertThat(responseStudentID).isEqualTo(studentID);
    }

    @Test
    public void testGetStudentForBatch() {
        UUID studentID = UUID.randomUUID();
        BatchGraduationStudentRecord graduationStudentForBatch = new BatchGraduationStudentRecord("2018-EN",null,"12345678", studentID);
        when(graduationStatusRepository.findByStudentIDForBatch(studentID)).thenReturn(Optional.of(graduationStudentForBatch));
        var result = graduationStatusService.getStudentForBatch(studentID);
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(studentID);
    }

    @Test
    public void testGetStudentForBatch_whenStudentIDisMismatched_returns_null() {
        UUID studentID = UUID.randomUUID();
        when(graduationStatusRepository.findByStudentIDForBatch(studentID)).thenReturn(Optional.empty());
        var result = graduationStatusService.getStudentForBatch(studentID);
        assertThat(result).isNull();
    }


    @Test
    public void testSearchGraduationStudentRecords() {

        final UUID studentId = UUID.randomUUID();
        final String pen = "142524123";
        final String schoolOfRecord = "06299164";
        final String distCode = "062";
        final String programCode = "2018-EN";

        List<String> pens = new ArrayList<>();
        pens.add(pen);

        Student student = new Student();
        student.setPen(pen);
        student.setStudentID(studentId.toString());

        List<String> schoolOfRecords = new ArrayList<>();
        //schoolOfRecords.add(schoolOfRecord);

        School school = new School();
        school.setMinCode(schoolOfRecord);
        school.setSchoolName("Test School");
        school.setSchoolCategoryCode("02");
        school.setSchoolCategoryCodeInstitute("INDEPEN");

        List<String> districts = new ArrayList<>();
        districts.add(distCode);

        List<String> schoolCategoryCodes = new ArrayList<>();
        schoolCategoryCodes.add("02");

        District district = new District();
        district.setDistrictNumber(distCode);
        district.setDistrictName("Test District");

        List<String> programs = new ArrayList<>();
        //programs.add(programCode);

        GradProgram program = new GradProgram();
        program.setProgramCode(programCode);
        program.setProgramName(programCode);

        GradSearchStudent gradSearchStudent = new GradSearchStudent();
        gradSearchStudent.setStudentID(studentId.toString());
        gradSearchStudent.setPen(pen);
        gradSearchStudent.setStudentStatus("ARC");
        gradSearchStudent.setProgram(programCode);
        gradSearchStudent.setSchoolOfRecord(schoolOfRecord);


        StudentSearchRequest searchRequest = StudentSearchRequest.builder()
                .pens(pens)
                .schoolOfRecords(schoolOfRecords)
                .districts(districts)
                //.schoolCategoryCodes(schoolCategoryCodes)
                .programs(programs)
                .validateInput(false)
                .build();

        GraduationStudentRecordSearchEntity graduationStudentRecordEntity = new GraduationStudentRecordSearchEntity();
        graduationStudentRecordEntity.setStudentID(studentId);
        graduationStudentRecordEntity.setProgram(programCode);
        graduationStudentRecordEntity.setSchoolOfRecord(schoolOfRecord);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        GraduationStudentRecordSearchCriteria searchCriteria = GraduationStudentRecordSearchCriteria.builder()
                .studentIds(searchRequest.getPens())
                .schoolOfRecords(searchRequest.getSchoolOfRecords())
                .districts(searchRequest.getDistricts())
                .programs(searchRequest.getPrograms())
                .build();
        Specification<GraduationStudentRecordSearchEntity> spec = new GraduationStudentRecordSearchSpecification(searchCriteria);

        when(graduationStudentRecordSearchRepository.findAll(Specification.where(spec))).thenReturn(List.of(graduationStudentRecordEntity));


        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getPenStudentApiByPenUrl(),pen))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Student.class)).thenReturn(Mono.just(student));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeUrl(),schoolOfRecord))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        final ParameterizedTypeReference<List<School>> schoolsType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolsByDistrictNumberUrl(),distCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(schoolsType)).thenReturn(Mono.just(List.of(school)));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getDistrictByDistrictCodeUrl(),distCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(District.class)).thenReturn(Mono.just(district));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradProgramNameUrl(),programCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(program));

        when(gradStudentService.getStudentByPenFromStudentAPI(pen, "accessToken")).thenReturn(List.of(gradSearchStudent));

        GraduationStudentRecordSearchResult result = graduationStatusService.searchGraduationStudentRecords(searchRequest, "accessToken");
        assertThat(result).isNotNull();

        gradSearchStudent.setStudentStatus("ARC");
        when(gradStudentService.getStudentByPenFromStudentAPI(pen, "accessToken")).thenReturn(List.of(gradSearchStudent));
        result = graduationStatusService.searchGraduationStudentRecords(searchRequest, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getValidationErrors()).isNotNull();
        assertThat(result.getValidationErrors().get(String.format(GraduationStudentRecordSearchResult.STUDENT_STATUS_VALIDATION_WARNING, "ARC"))).isNotNull();

        gradSearchStudent.setStudentStatus("TER");
        when(gradStudentService.getStudentByPenFromStudentAPI(pen, "accessToken")).thenReturn(List.of(gradSearchStudent));
        result = graduationStatusService.searchGraduationStudentRecords(searchRequest, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getValidationErrors()).isNotNull();
        assertThat(result.getValidationErrors().get(String.format(GraduationStudentRecordSearchResult.STUDENT_STATUS_VALIDATION_WARNING, "TER"))).isNotNull();

        gradSearchStudent.setStudentStatus("DEC");
        when(gradStudentService.getStudentByPenFromStudentAPI(pen, "accessToken")).thenReturn(List.of(gradSearchStudent));
        result = graduationStatusService.searchGraduationStudentRecords(searchRequest, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getValidationErrors()).isNotNull();
        assertThat(result.getValidationErrors().get(String.format(GraduationStudentRecordSearchResult.STUDENT_STATUS_VALIDATION_WARNING, "DEC"))).isNotNull();
    }

    @Test
    public void testSearchGraduationStudentRecords_validateTrue() {

        final UUID studentId = UUID.randomUUID();
        final String pen = "142524123";
        final String pen2 = "1425241232";
        final String pen3 = "14252412321";
        final String schoolOfRecord = "06299164";
        final String distCode = "062";
        final String programCode = "2018-EN";

        List<String> pens = new ArrayList<>();
        pens.add(pen);
        pens.add(pen2);
        pens.add(pen3);

        Student student = new Student();
        student.setPen(pen);
        student.setStudentID(studentId.toString());

        List<String> schoolOfRecords = new ArrayList<>();
        schoolOfRecords.add(schoolOfRecord);

        School school = new School();
        school.setMinCode(schoolOfRecord);
        school.setSchoolName("Test School");
        school.setSchoolCategoryCode("02");
        school.setSchoolCategoryCodeInstitute("INDEPEN");

        List<String> districts = new ArrayList<>();
        districts.add(distCode);

        List<String> schoolCategoryCodes = new ArrayList<>();
        schoolCategoryCodes.add("02");

        District district = new District();
        district.setDistrictNumber(distCode);
        district.setDistrictName("Test District");

        List<String> programs = new ArrayList<>();
        //programs.add(programCode);

        GradProgram program = new GradProgram();
        program.setProgramCode(programCode);
        program.setProgramName(programCode);

        GradSearchStudent gradSearchStudent = new GradSearchStudent();
        gradSearchStudent.setStudentID(studentId.toString());
        gradSearchStudent.setPen(pen);
        gradSearchStudent.setStudentStatus("ARC");
        gradSearchStudent.setProgram(programCode);
        gradSearchStudent.setSchoolOfRecord(schoolOfRecord);


        StudentSearchRequest searchRequest = StudentSearchRequest.builder()
                .pens(pens)
                .schoolOfRecords(schoolOfRecords)
                .districts(districts)
                //.schoolCategoryCodes(schoolCategoryCodes)
                .programs(programs)
                .validateInput(true)
                .build();

        GraduationStudentRecordSearchEntity graduationStudentRecordEntity = new GraduationStudentRecordSearchEntity();
        graduationStudentRecordEntity.setStudentID(studentId);
        graduationStudentRecordEntity.setProgram(programCode);
        graduationStudentRecordEntity.setSchoolOfRecord(schoolOfRecord);
        graduationStudentRecordEntity.setStudentStatus("CUR");

        GraduationStudentRecordSearchCriteria searchCriteria = GraduationStudentRecordSearchCriteria.builder()
                .studentIds(searchRequest.getPens())
                .schoolOfRecords(searchRequest.getSchoolOfRecords())
                .districts(searchRequest.getDistricts())
                .programs(searchRequest.getPrograms())
                .build();
        Specification<GraduationStudentRecordSearchEntity> spec = new GraduationStudentRecordSearchSpecification(searchCriteria);

        when(graduationStudentRecordSearchRepository.findAll(Specification.where(spec))).thenReturn(List.of(graduationStudentRecordEntity));


        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getPenStudentApiByPenUrl(),pen))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Student.class)).thenReturn(Mono.just(student));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeUrl(),schoolOfRecord))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        final ParameterizedTypeReference<List<School>> schoolsType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolsByDistrictNumberUrl(),distCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(schoolsType)).thenReturn(Mono.just(List.of(school)));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getDistrictByDistrictCodeUrl(),distCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(District.class)).thenReturn(Mono.just(district));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradProgramNameUrl(),programCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(GradProgram.class)).thenReturn(Mono.just(program));

        when(gradStudentService.getStudentByPenFromStudentAPI(pen, "accessToken")).thenReturn(List.of(gradSearchStudent));

        var result = graduationStatusService.searchGraduationStudentRecords(searchRequest, "accessToken");
        assertThat(result).isNotNull();
        assertThat(result.getValidationErrors()).isNotEmpty();

    }


    @Test
    public void testGetStudentsForProjectedGraduation() {
        UUID studentID = UUID.randomUUID();
        when(graduationStatusRepository.findByRecalculateProjectedGradForBatch("Y")).thenReturn(List.of(studentID));
        var result = graduationStatusService.getStudentsForProjectedGraduation();
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        UUID responseStudentID = result.get(0);
        assertThat(responseStudentID).isEqualTo(studentID);
    }

    @Test
    public void testGetStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode() {
        // ID
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.of(gradStudentOptionalProgramEntity));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramNameUrl(),optionalProgramID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        var result = graduationStatusService.getStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode(studentID, optionalProgramID.toString(), "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(gradStudentOptionalProgramEntity.getStudentID());
        assertThat(result.getOptionalProgramID()).isEqualTo(gradStudentOptionalProgramEntity.getOptionalProgramID());
        assertThat(result.getProgramCode()).isEqualTo(optionalProgram.getGraduationProgramCode());
        assertThat(result.getOptionalProgramCode()).isEqualTo(optionalProgram.getOptProgramCode());
        assertThat(result.getOptionalProgramName()).isEqualTo(optionalProgram.getOptionalProgramName());
    }

    @Test
    public void testGetStudentStatus() {
        String statusCode = "A";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(UUID.randomUUID());
        graduationStatusEntity.setPen("123456789");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setSchoolOfRecord("12345678");
        graduationStatusEntity.setRecalculateGradStatus("Y");

        when(graduationStatusRepository.existsByStatusCode(statusCode)).thenReturn(List.of(graduationStatusEntity));
        var result = graduationStatusService.getStudentStatus(statusCode);
        assertThat(result).isTrue();
    }

    @Test
    public void testUgradStudent() throws JsonProcessingException {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String ungradReasonCode = "NM";
        String ungradReasonDesc = "FDFS";

        UndoCompletionReason ungradReasons = new UndoCompletionReason();
        ungradReasons.setCode(ungradReasonCode);
        ungradReasons.setDescription("Not Met");

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setSchoolOfRecord("12345678");

        GraduationStudentRecordEntity responseGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, responseGraduationStatus);
        responseGraduationStatus.setRecalculateGradStatus("Y");
        responseGraduationStatus.setRecalculateProjectedGrad("Y");
        responseGraduationStatus.setProgramCompletionDate(null);
        responseGraduationStatus.setHonoursStanding(null);
        responseGraduationStatus.setGpa(null);
        responseGraduationStatus.setSchoolAtGrad(null);

        StudentUndoCompletionReason responseStudentUndoCompletionReasons = new StudentUndoCompletionReason();
        responseStudentUndoCompletionReasons.setGraduationStudentRecordID(studentID);
        responseStudentUndoCompletionReasons.setUndoCompletionReasonCode(ungradReasonCode);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getUndoCompletionReasonDetailsUrl(),ungradReasonCode))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(UndoCompletionReason.class)).thenReturn(Mono.just(ungradReasons));

        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(String.format(constants.getSaveStudentUndoCompletionReasonByStudentIdUrl(),studentID))).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(StudentUndoCompletionReason.class)).thenReturn(Mono.just(responseStudentUndoCompletionReasons));

        when(this.webClient.delete()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getDeleteStudentAchievements(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Integer.class)).thenReturn(Mono.just(0));

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.save(responseGraduationStatus)).thenReturn(responseGraduationStatus);

        StudentOptionalProgramEntity studentOptionalProgramEntity = new StudentOptionalProgramEntity();
        studentOptionalProgramEntity.setId(UUID.randomUUID());
        studentOptionalProgramEntity.setStudentID(studentID);
        studentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        when(gradStudentOptionalProgramRepository.findByStudentID(studentID)).thenReturn(List.of(studentOptionalProgramEntity));
        doNothing().when(historyService).createStudentOptionalProgramHistory(any(), any());

        var response = graduationStatusService.undoCompletionStudent(studentID, ungradReasonCode,ungradReasonDesc, "accessToken");
        assertThat(response).isNotNull();

        var result = response.getLeft();
        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getRecalculateGradStatus()).isEqualTo("Y");
        assertThat(result.getProgramCompletionDate()).isNull();
        assertThat(result.getHonoursStanding()).isNull();
        assertThat(result.getGpa()).isNull();
        assertThat(result.getSchoolAtGrad()).isNull();
    }

    @Test
    public void saveUndoCompletionReason() {
        // ID
        UUID studentID = UUID.randomUUID();
        String ungradReasonCode = "NM";
        String ungradReasonDesc= "FDFS";

        StudentUndoCompletionReason responseStudentUndoCompletionReasons = new StudentUndoCompletionReason();
        responseStudentUndoCompletionReasons.setGraduationStudentRecordID(studentID);
        responseStudentUndoCompletionReasons.setUndoCompletionReasonCode(ungradReasonCode);

        when(this.webClient.post()).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.uri(String.format(constants.getSaveStudentUndoCompletionReasonByStudentIdUrl(),studentID))).thenReturn(this.requestBodyUriMock);
        when(this.requestBodyUriMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.body(any(BodyInserter.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(StudentUndoCompletionReason.class)).thenReturn(Mono.just(responseStudentUndoCompletionReasons));

        graduationStatusService.saveUndoCompletionReason(studentID, ungradReasonCode,ungradReasonDesc, "accessToken");
        assertThat(responseStudentUndoCompletionReasons.getUndoCompletionReasonCode()).isEqualTo("NM");
    }

    @Test
    public void testRestoreGradStudentRecord() {
    	 UUID studentID = new UUID(1, 1);
		 GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
	     graduationStatusEntity.setStudentID(studentID);
	     graduationStatusEntity.setPen("12321321");
	     graduationStatusEntity.setStudentStatus("A");
	     graduationStatusEntity.setSchoolOfRecord("12345678");

	     GraduationStudentRecordEntity graduationStatusEntity2 = new GraduationStudentRecordEntity();
	     graduationStatusEntity2.setStudentID(studentID);
	     graduationStatusEntity2.setPen("12321321");
	     graduationStatusEntity2.setStudentStatus("A");
	     graduationStatusEntity2.setSchoolOfRecord("12345678");
	     graduationStatusEntity2.setRecalculateGradStatus("Y");
	     graduationStatusEntity2.setProgramCompletionDate(null);
	     graduationStatusEntity2.setHonoursStanding(null);
	     graduationStatusEntity2.setGpa(null);
	     graduationStatusEntity2.setSchoolAtGrad(null);



    	when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
    	when(graduationStatusRepository.saveAndFlush(graduationStatusEntity2)).thenReturn(graduationStatusEntity2);

    	boolean res = graduationStatusService.restoreGradStudentRecord(studentID, false);
        assertThat(res).isTrue();

    }

    @Test
    public void testRestoreGradStudentRecord_graduated() {
    	 UUID studentID = new UUID(1, 1);
		 GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
	     graduationStatusEntity.setStudentID(studentID);
	     graduationStatusEntity.setPen("12321321");
	     graduationStatusEntity.setStudentStatus("A");
	     graduationStatusEntity.setSchoolOfRecord("12345678");

	     GraduationStudentRecordEntity graduationStatusEntity2 = new GraduationStudentRecordEntity();
	     graduationStatusEntity2.setStudentID(studentID);
	     graduationStatusEntity2.setPen("12321321");
	     graduationStatusEntity2.setStudentStatus("A");
	     graduationStatusEntity2.setSchoolOfRecord("12345678");
	     graduationStatusEntity2.setRecalculateGradStatus("Y");


    	when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
    	when(graduationStatusRepository.save(graduationStatusEntity2)).thenReturn(graduationStatusEntity2);

    	boolean res = graduationStatusService.restoreGradStudentRecord(studentID, true);
        assertThat(res).isTrue();
    }

    @Test
    public void testSaveStudentRecord_projectedRun() {
        UUID studentID = new UUID(1, 1);
        Long batchId = null;
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("12321321");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setSchoolOfRecord("12345678");

        ProjectedRunClob projectedRunClob = new ProjectedRunClob();
        projectedRunClob.setGraduated(true);
        projectedRunClob.setNonGradReasons(null);

        String projectedClob = null;
        try {
            projectedClob = new ObjectMapper().writeValueAsString(projectedRunClob);
        } catch (JsonProcessingException e) {}

        GraduationStudentRecordEntity graduationStatusEntity2 = new GraduationStudentRecordEntity();
        graduationStatusEntity2.setStudentID(studentID);
        graduationStatusEntity2.setPen("12321321");
        graduationStatusEntity2.setStudentStatus("A");
        graduationStatusEntity2.setSchoolOfRecord("12345678");
        graduationStatusEntity2.setRecalculateProjectedGrad("Y");
        graduationStatusEntity2.setStudentProjectedGradData(projectedClob);
        graduationStatusEntity2.setBatchId(batchId);

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(graduationStatusEntity2);

        GraduationStudentRecord res = graduationStatusService.saveStudentRecordProjectedTVRRun(studentID, batchId, projectedRunClob);
        assertThat(res).isNotNull();
    }

    @Test
    public void testSaveStudentRecord_projectedRun_whenProgramCompletionDate_isFuture_thenReturnSuccess_SCCP() {
        UUID studentID = new UUID(1, 1);
        Long batchId = 1234L;
        Date programCompletionDate = new Date(System.currentTimeMillis() + 86400000L);  // add one day as milliseconds
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("12321321");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setSchoolOfRecord("12345678");
        graduationStatusEntity.setProgram("SCCP");
        graduationStatusEntity.setProgramCompletionDate(programCompletionDate);

        ProjectedRunClob projectedRunClob = new ProjectedRunClob();
        projectedRunClob.setGraduated(true);
        projectedRunClob.setNonGradReasons(null);

        String projectedClob = null;
        try {
            projectedClob = new ObjectMapper().writeValueAsString(projectedRunClob);
        } catch (JsonProcessingException e) {}

        GraduationStudentRecordEntity graduationStatusEntity2 = new GraduationStudentRecordEntity();
        graduationStatusEntity2.setStudentID(studentID);
        graduationStatusEntity2.setPen("12321321");
        graduationStatusEntity2.setStudentStatus("A");
        graduationStatusEntity2.setSchoolOfRecord("12345678");
        graduationStatusEntity2.setRecalculateProjectedGrad("Y");
        graduationStatusEntity2.setStudentProjectedGradData(projectedClob);
        graduationStatusEntity.setProgram("SCCP");
        graduationStatusEntity.setProgramCompletionDate(programCompletionDate);
        graduationStatusEntity2.setBatchId(batchId);

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(graduationStatusEntity2);

        GraduationStudentRecord res = graduationStatusService.saveStudentRecordProjectedTVRRun(studentID, batchId, projectedRunClob);
        assertThat(res).isNotNull();
    }

    @Test
    public void testSaveStudentRecord_DistributionRun() {
        UUID studentID = new UUID(1, 1);
        Long batchId = null;
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("12321321");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setSchoolOfRecord("12345678");


        GraduationStudentRecordEntity graduationStatusEntity2 = new GraduationStudentRecordEntity();
        graduationStatusEntity2.setStudentID(studentID);
        graduationStatusEntity2.setPen("12321321");
        graduationStatusEntity2.setStudentStatus("A");
        graduationStatusEntity2.setSchoolOfRecord("12345678");
        graduationStatusEntity2.setBatchId(batchId);


        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity2)).thenReturn(graduationStatusEntity2);

        GraduationStudentRecord res =graduationStatusService.saveStudentRecordDistributionRun(studentID, batchId, "ACTIVITYCODE", "USER");
        assertThat(res).isNotNull();
    }

    @Test
    public void testSaveStudentRecord_DistributionRun_2() {

        GraduationStatusService graduationStatusServiceMock = mock(GraduationStatusService.class);

        UUID studentID = new UUID(1, 1);
        Long batchId = null;
        GraduationStudentRecordView graduationStatusEntity = new GraduationStudentRecordView() {

            @Override
            public String getProgram() {
                return null;
            }

            @Override
            public java.util.Date getProgramCompletionDate() {
                return null;
            }

            @Override
            public String getGpa() {
                return null;
            }

            @Override
            public String getHonoursStanding() {
                return null;
            }

            @Override
            public String getRecalculateGradStatus() {
                return null;
            }

            @Override
            public String getSchoolOfRecord() {
                return "12345678";
            }

            @Override
            public String getStudentGrade() {
                return null;
            }

            @Override
            public String getStudentStatus() {
                return "A";
            }

            @Override
            public UUID getStudentID() {
                return studentID;
            }

            @Override
            public String getSchoolAtGrad() {
                return null;
            }

            @Override
            public String getRecalculateProjectedGrad() {
                return null;
            }

            @Override
            public Long getBatchId() {
                return null;
            }

            @Override
            public String getConsumerEducationRequirementMet() {
                return null;
            }

            @Override
            public String getStudentCitizenship() {
                return null;
            }

            @Override
            public java.util.Date getAdultStartDate() {
                return null;
            }

            @Override
            public String getStudentProjectedGradData() {
                return null;
            }

            @Override
            public UUID getSchoolOfRecordId() {
                return null;
            }

            @Override
            public UUID getSchoolAtGraduationId() {
                return null;
            }

            @Override
            public LocalDateTime getCreateDate() {
                return null;
            }

            @Override
            public LocalDateTime getUpdateDate() {
                return null;
            }
        };

        when(graduationStatusRepository.findByStudentIDIn(List.of(studentID))).thenReturn(List.of(graduationStatusEntity));

        doNothing().when(graduationStatusServiceMock).saveStudentHistoryRecordArchiveStudentsRun(studentID, batchId, "ACTIVITYCODE");
        graduationStatusServiceMock.saveStudentHistoryRecordArchiveStudentsRun(studentID, batchId, "ACTIVITYCODE");
        Mockito.verify(graduationStatusServiceMock).saveStudentHistoryRecordArchiveStudentsRun(studentID, batchId, "ACTIVITYCODE");

        graduationStatusService.saveStudentHistoryRecordArchiveStudentsRun(studentID, batchId, "ACTIVITYCODE");

    }

    @Test
    public void testGetStudentDataByStudentIds() {
        // ID
        List<UUID> sList = Arrays.asList(UUID.randomUUID());
        List<GraduationStudentRecordView> histList = new ArrayList<>();

        GradSearchStudent serObj = new GradSearchStudent();
        serObj.setPen("123123");
        serObj.setLegalFirstName("Asdad");
        serObj.setLegalMiddleNames("Adad");
        serObj.setLegalLastName("sadad");
        GraduationData gd = new GraduationData();
        gd.setGradStudent(serObj);

        GraduationStudentRecordView graduationStatusEntity = new GraduationStudentRecordView() {
            @Override
            public String getProgram() {
                return null;
            }

            @Override
            public java.util.Date getProgramCompletionDate() {
                return null;
            }

            @Override
            public String getGpa() {
                return null;
            }

            @Override
            public String getHonoursStanding() {
                return null;
            }

            @Override
            public String getRecalculateGradStatus() {
                return null;
            }

            @Override
            public String getSchoolOfRecord() {
                return null;
            }

            @Override
            public String getStudentGrade() {
                return null;
            }

            @Override
            public String getStudentStatus() {
                return null;
            }

            @Override
            public UUID getStudentID() {
                return null;
            }

            @Override
            public String getSchoolAtGrad() {
                return null;
            }

            @Override
            public String getRecalculateProjectedGrad() {
                return null;
            }

            @Override
            public Long getBatchId() {
                return null;
            }

            @Override
            public String getConsumerEducationRequirementMet() {
                return null;
            }

            @Override
            public String getStudentCitizenship() {
                return null;
            }

            @Override
            public java.util.Date getAdultStartDate() {
                return null;
            }

            @Override
            public String getStudentProjectedGradData() {
                return null;
            }

            @Override
            public UUID getSchoolOfRecordId() {
                return null;
            }

            @Override
            public UUID getSchoolAtGraduationId() {
                return null;
            }

            @Override
            public LocalDateTime getCreateDate() {
                return null;
            }

            @Override
            public LocalDateTime getUpdateDate() {
                return null;
            }
        };
        histList.add(graduationStatusEntity);
        when(graduationStatusRepository.findByStudentIDIn(sList)).thenReturn(histList);
        List<GraduationStudentRecord> list = graduationStatusService.getStudentDataByStudentIDs(sList);
        assertThat(list).isNotEmpty().hasSize(1);
    }

    @Test
    public void testGetStudentsForYearlyDistribution() {

        List<GraduationStudentRecordEntity> histList = new ArrayList<>();

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(new UUID(1,1));
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setBatchId(4000L);
        graduationStatusEntity.setPen("123123");
        graduationStatusEntity.setLegalFirstName("Asdad");
        graduationStatusEntity.setLegalMiddleNames("Adad");
        graduationStatusEntity.setLegalLastName("sadad");
        histList.add(graduationStatusEntity);

        when(graduationStatusRepository.findStudentsForYearlyDistribution(PageRequest.of(0, PAGE_SIZE))).thenReturn(new Page() {

            @Override
            public Iterator<UUID> iterator() {
                return getContent().listIterator();
            }

            @Override
            public int getNumber() {
                return 1;
            }

            @Override
            public int getSize() {
                return 1;
            }

            @Override
            public int getNumberOfElements() {
                return 1;
            }

            @Override
            public List<UUID> getContent() {
                return List.of(graduationStatusEntity.getStudentID());
            }

            @Override
            public boolean hasContent() {
                return !getContent().isEmpty();
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public Pageable nextPageable() {
                return null;
            }

            @Override
            public Pageable previousPageable() {
                return null;
            }

            @Override
            public int getTotalPages() {
                return getContent().size();
            }

            @Override
            public long getTotalElements() {
                return getContent().size();
            }

            @Override
            public Page map(Function converter) {
                return null;
            }
        });

        List<UUID> list = graduationStatusService.getStudentsForYearlyDistribution();
        assertThat(list).isNotEmpty().hasSize(1);

        ReportGradStudentDataEntity reportGradStudentData = new ReportGradStudentDataEntity();
        reportGradStudentData.setGraduationStudentRecordId(graduationStatusEntity.getStudentID());

        when(reportGradStudentDataRepository.findReportGradStudentDataEntityByProgramCompletionDateAndStudentStatusAndStudentGrade(PageRequest.of(0, PAGE_SIZE))).thenReturn(new Page() {

            @Override
            public Iterator<ReportGradStudentDataEntity> iterator() {
                return getContent().listIterator();
            }

            @Override
            public int getNumber() {
                return 1;
            }

            @Override
            public int getSize() {
                return 1;
            }

            @Override
            public int getNumberOfElements() {
                return 1;
            }

            @Override
            public List<ReportGradStudentDataEntity> getContent() {
                return List.of(reportGradStudentData);
            }

            @Override
            public boolean hasContent() {
                return !getContent().isEmpty();
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public Pageable nextPageable() {
                return null;
            }

            @Override
            public Pageable previousPageable() {
                return null;
            }

            @Override
            public int getTotalPages() {
                return getContent().size();
            }

            @Override
            public long getTotalElements() {
                return getContent().size();
            }

            @Override
            public Page map(Function converter) {
                return null;
            }
        });

        var result = gradStudentReportService.getGradStudentDataForNonGradYearEndReport();
        assertThat(result).isNotEmpty().hasSize(1);

        when(reportGradStudentDataRepository.findReportGradStudentDataEntityByMincodeAndProgramCompletionDateAndStudentStatusAndStudentGrade(graduationStatusEntity.getSchoolOfRecord(), PageRequest.of(0, PAGE_SIZE))).thenReturn(new Page() {

            @Override
            public Iterator<ReportGradStudentDataEntity> iterator() {
                return getContent().listIterator();
            }

            @Override
            public int getNumber() {
                return 1;
            }

            @Override
            public int getSize() {
                return 1;
            }

            @Override
            public int getNumberOfElements() {
                return 1;
            }

            @Override
            public List<ReportGradStudentDataEntity> getContent() {
                return List.of(reportGradStudentData);
            }

            @Override
            public boolean hasContent() {
                return !getContent().isEmpty();
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public Pageable nextPageable() {
                return null;
            }

            @Override
            public Pageable previousPageable() {
                return null;
            }

            @Override
            public int getTotalPages() {
                return getContent().size();
            }

            @Override
            public long getTotalElements() {
                return getContent().size();
            }

            @Override
            public Page map(Function converter) {
                return null;
            }
        });

        result = gradStudentReportService.getGradStudentDataForNonGradYearEndReport(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result).isNotEmpty().hasSize(1);

        graduationStatusEntity.setSchoolOfRecord("001");
        assertThat(graduationStatusEntity.getSchoolOfRecord()).hasSize(3);
        when(reportGradStudentDataRepository.findReportGradStudentDataEntityByDistcodeAndProgramCompletionDateAndStudentStatusAndStudentGrade(graduationStatusEntity.getSchoolOfRecord(), PageRequest.of(0, PAGE_SIZE))).thenReturn(new Page() {

            @Override
            public Iterator<ReportGradStudentDataEntity> iterator() {
                return getContent().listIterator();
            }

            @Override
            public int getNumber() {
                return 1;
            }

            @Override
            public int getSize() {
                return 1;
            }

            @Override
            public int getNumberOfElements() {
                return 1;
            }

            @Override
            public List<ReportGradStudentDataEntity> getContent() {
                return List.of(reportGradStudentData);
            }

            @Override
            public boolean hasContent() {
                return !getContent().isEmpty();
            }

            @Override
            public Sort getSort() {
                return null;
            }

            @Override
            public boolean isFirst() {
                return false;
            }

            @Override
            public boolean isLast() {
                return false;
            }

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public Pageable nextPageable() {
                return null;
            }

            @Override
            public Pageable previousPageable() {
                return null;
            }

            @Override
            public int getTotalPages() {
                return getContent().size();
            }

            @Override
            public long getTotalElements() {
                return getContent().size();
            }

            @Override
            public Page map(Function converter) {
                return null;
            }
        });

        result = gradStudentReportService.getGradStudentDataForNonGradYearEndReport(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result).isNotEmpty().hasSize(1);

        assertThrows("Invalid mincode: null", IllegalArgumentException.class, () -> {
            gradStudentReportService.getGradStudentDataForNonGradYearEndReport(null);
        });
    }

    @Test
    public void testGetStudentsForSchoolReport() {
        String mincode = "123213123";
        GraduationStudentRecordView graduationStatus = new GraduationStudentRecordView() {

            @Override
            public String getProgram() {
                return null;
            }

            @Override
            public java.util.Date getProgramCompletionDate() {
                return null;
            }

            @Override
            public String getGpa() {
                return null;
            }

            @Override
            public String getHonoursStanding() {
                return null;
            }

            @Override
            public String getRecalculateGradStatus() {
                return null;
            }

            @Override
            public String getSchoolOfRecord() {
                return mincode;
            }

            @Override
            public String getStudentGrade() {
                return "AD";
            }

            @Override
            public String getStudentStatus() {
                return "CUR";
            }

            @Override
            public UUID getStudentID() {
                return new UUID(1,1);
            }

            @Override
            public String getSchoolAtGrad() {
                return mincode;
            }

            @Override
            public String getRecalculateProjectedGrad() {
                return null;
            }

            @Override
            public Long getBatchId() {
                return null;
            }

            @Override
            public String getConsumerEducationRequirementMet() {
                return null;
            }

            @Override
            public String getStudentCitizenship() {
                return null;
            }

            @Override
            public java.util.Date getAdultStartDate() {
                return null;
            }

            @Override
            public String getStudentProjectedGradData() {
                return null;
            }

            @Override
            public UUID getSchoolOfRecordId() {
                return null;
            }

            @Override
            public UUID getSchoolAtGraduationId() {
                return null;
            }

            @Override
            public LocalDateTime getCreateDate() {
                return null;
            }

            @Override
            public LocalDateTime getUpdateDate() {
                return null;
            }
        };
        when(graduationStatusRepository.findBySchoolOfRecordAndStudentStatus(mincode, "CUR")).thenReturn(List.of(graduationStatus));
        var result = graduationStatusService.getStudentsForSchoolReport(mincode);
        assertThat(result).isNotNull().hasSize(1);
        GraduationStudentRecord responseGraduationStatus = result.get(0);
        assertThat(responseGraduationStatus.getSchoolOfRecord()).isEqualTo(mincode);
    }

    @Test
    public void testGetDataForBatch() {
        UUID studentID = UUID.randomUUID();
        GradSearchStudent serObj = new GradSearchStudent();
        serObj.setPen("123123");
        serObj.setLegalFirstName("Asdad");
        serObj.setLegalMiddleNames("Adad");
        serObj.setLegalLastName("sadad");
        GraduationData gd = new GraduationData();
        gd.setGradStudent(serObj);

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setStudentStatus("A");
        try {
            graduationStatusEntity.setStudentGradData(new ObjectMapper().writeValueAsString(gd));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setBatchId(4000L);
        graduationStatusEntity.setPen("123123");
        graduationStatusEntity.setLegalFirstName("Asdad");
        graduationStatusEntity.setLegalMiddleNames("Adad");
        graduationStatusEntity.setLegalLastName("sadad");

        Mockito.when(graduationStatusRepository.findByStudentID(studentID)).thenReturn(graduationStatusEntity);

        GraduationStudentRecord res = graduationStatusService.getDataForBatch(studentID,"accessToken");
        assertThat(res).isNotNull();
    }

    @Test
    public void testGetDataForBatch_else() {
        UUID studentID = UUID.randomUUID();
        GradSearchStudent serObj = new GradSearchStudent();
        serObj.setPen("123123");
        serObj.setLegalFirstName("Asdad");
        serObj.setLegalMiddleNames("Adad");
        serObj.setLegalLastName("sadad");
        GraduationData gd = new GraduationData();
        gd.setGradStudent(serObj);

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setStudentGradData(null);
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setBatchId(4000L);
        graduationStatusEntity.setPen("123123");
        graduationStatusEntity.setLegalFirstName("Asdad");
        graduationStatusEntity.setLegalMiddleNames("Adad");
        graduationStatusEntity.setLegalLastName("sadad");

        Student std = new Student();
        std.setPen("123123");
        std.setLegalFirstName("Asdad");
        std.setLegalMiddleNames("Adad");
        std.setLegalLastName("sadad");

        Mockito.when(graduationStatusRepository.findByStudentID(studentID)).thenReturn(graduationStatusEntity);

        when(this.webClient.get()).thenReturn(requestHeadersUriMock);
        when(requestHeadersUriMock.uri(String.format(constants.getPenStudentApiByStudentIdUrl(),studentID))).thenReturn(requestHeadersMock);
        when(requestHeadersMock.headers(any(Consumer.class))).thenReturn(requestHeadersMock);
        when(requestHeadersMock.retrieve()).thenReturn(responseMock);
        when(responseMock.bodyToMono(Student.class)).thenReturn(Mono.just(std));

        GraduationStudentRecord res = graduationStatusService.getDataForBatch(studentID,"accessToken");
        assertThat(res).isNotNull();
    }

    @Test
    public void testGetStudentsCountForAmalgamatedSchoolReport() {
        Mockito.when(graduationStatusRepository.countBySchoolOfRecordAmalgamated("12345678")).thenReturn(1);

        Integer count = graduationStatusService.countStudentsForAmalgamatedSchoolReport("12345678");
        assertThat(count).isNotNull().isEqualTo(1);

    }

    @Test
    public void testCountBySchoolOfRecordsAndStudentStatus() {
        Mockito.when(graduationStatusRepository.countBySchoolOfRecordsAndStudentStatus(List.of("12345678"), "CUR")).thenReturn(1L);
        Long count = graduationStatusService.countBySchoolOfRecordsAndStudentStatus(List.of("12345678"), "CUR");
        assertThat(count).isNotNull().isEqualTo(1L);

        Mockito.when(graduationStatusRepository.countByStudentStatus("CUR")).thenReturn(1L);
        count = graduationStatusService.countBySchoolOfRecordsAndStudentStatus(List.of(), "CUR");
        assertThat(count).isNotNull().isEqualTo(1L);

        Mockito.when(graduationStatusRepository.countBySchoolOfRecords(List.of("12345678"))).thenReturn(1L);
        count = graduationStatusService.countBySchoolOfRecordsAndStudentStatus(List.of("12345678"), null);
        assertThat(count).isNotNull().isEqualTo(1L);

        Mockito.when(graduationStatusRepository.countBySchoolOfRecords(List.of("12345678"))).thenReturn(1L);
        count = graduationStatusService.countBySchoolOfRecordsAndStudentStatus(List.of("12345678"), "null");
        assertThat(count).isNotNull().isEqualTo(1L);

        Mockito.when(graduationStatusRepository.count()).thenReturn(2L);
        count = graduationStatusService.countBySchoolOfRecordsAndStudentStatus(List.of(), null);
        assertThat(count).isNotNull().isEqualTo(2L);

    }

    @Test
    public void testArchiveStudents() {

        UUID studentID = new UUID(1, 1);
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("12321321");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setSchoolOfRecord("12345678");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        Mockito.when(graduationStatusRepository.findBySchoolOfRecordInAndStudentStatus(List.of("12345678"), "CUR")).thenReturn(List.of(studentID));
        Mockito.when(graduationStatusRepository.archiveStudents(List.of("12345678"), "CUR", "ARC", 1L, "USER")).thenReturn(1);
        Mockito.when(graduationStatusRepository.updateGraduationStudentRecordEntitiesBatchIdWhereStudentIDsIn(1L, List.of(studentID))).thenReturn(1);
        Mockito.when(historyService.updateStudentRecordHistoryDistributionRun(1L, "USER", "USERSTUDARC", List.of(studentID))).thenReturn(1);

        Integer count = graduationStatusService.archiveStudents(1L, List.of("12345678"), "CUR", "USER");
        assertThat(count).isNotNull().isEqualTo(1);
    }

    @Test
    public void testArchiveStudentEmpty() {
        LocalDateTime updateDate = LocalDateTime.now();
        UUID studentID = new UUID(1, 1);
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("12321321");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setSchoolOfRecord("12345678");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        Mockito.when(graduationStatusRepository.findByStudentStatus("CUR")).thenReturn(List.of(studentID));
        Mockito.when(graduationStatusRepository.archiveStudents("CUR", "ARC", 1L, "USER")).thenReturn(1);
        Mockito.when(historyService.updateStudentRecordHistoryDistributionRun(1L, "USER", "USERSTUDARC", List.of(studentID))).thenReturn(1);

        Integer count = graduationStatusService.archiveStudents(1L, List.of(), "CUR", "USER");
        assertThat(count).isNotNull().isEqualTo(0);
    }

    @Test
    public void testGetStudentsForAmalgamatedSchoolReport() {
        List<UUID> res = amalgamatedReports("TVRNONGRAD",false);
        assertThat(res).isNotNull().hasSize(1);

        res = amalgamatedReports("TVRGRAD",true);
        assertThat(res).isNotNull().hasSize(1);

        res = amalgamatedReports("GRAD",false);
        assertThat(res).isNotNull().isEmpty();
    }

    private List<UUID> amalgamatedReports(String type,boolean isGraduated) {
        String mincode = "21313121";

        ProjectedRunClob projectedRunClob = new ProjectedRunClob();
        projectedRunClob.setGraduated(isGraduated);
        projectedRunClob.setNonGradReasons(null);

        GraduationStudentRecordView graduationStatusEntity = new GraduationStudentRecordView() {
            @Override
            public String getProgram() {
                return "2018-EN";
            }

            @Override
            public java.util.Date getProgramCompletionDate() {
                return null;
            }

            @Override
            public String getGpa() {
                return "4";
            }

            @Override
            public String getHonoursStanding() {
                return null;
            }

            @Override
            public String getRecalculateGradStatus() {
                return "Y";
            }

            @Override
            public String getSchoolOfRecord() {
                return "21313121";
            }

            @Override
            public String getStudentGrade() {
                return null;
            }

            @Override
            public String getStudentStatus() {
                return "A";
            }

            @Override
            public UUID getStudentID() {
                return UUID.randomUUID();
            }

            @Override
            public String getSchoolAtGrad() {
                return null;
            }

            @Override
            public String getRecalculateProjectedGrad() {
                return "Y";
            }

            @Override
            public Long getBatchId() {
                return 4000L;
            }

            @Override
            public String getConsumerEducationRequirementMet() {
                return null;
            }

            @Override
            public String getStudentCitizenship() {
                return "C";
            }

            @Override
            public java.util.Date getAdultStartDate() {
                return null;
            }

            @Override
            public String getStudentProjectedGradData() {
                return jsonTransformer.marshall(projectedRunClob);
            }

            @Override
            public UUID getSchoolOfRecordId() {
                return null;
            }

            @Override
            public UUID getSchoolAtGraduationId() {
                return null;
            }

            @Override
            public LocalDateTime getCreateDate() {
                return null;
            }

            @Override
            public LocalDateTime getUpdateDate() {
                return null;
            }
        };
        Mockito.when(graduationStatusRepository.findBySchoolOfRecordAndStudentStatusAndStudentGradeIn(mincode, "CUR", List.of("AD", "12"))).thenReturn(List.of(graduationStatusEntity));

        return graduationStatusService.getStudentsForAmalgamatedSchoolReport(mincode,type);
    }

    @Test
    public void testUpdateStudentFlagReadyForBatchJobByStudentIDs_when_relcalculateGradStatus_is_null() {
        UUID studentID1 = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStatusEntity1 = new GraduationStudentRecordEntity();
        graduationStatusEntity1.setStudentID(studentID1);
        graduationStatusEntity1.setStudentStatus("A");
        graduationStatusEntity1.setRecalculateProjectedGrad("Y");
        graduationStatusEntity1.setProgram("2018-EN");
        graduationStatusEntity1.setSchoolOfRecord("21313121");
        graduationStatusEntity1.setGpa("4");
        graduationStatusEntity1.setBatchId(4000L);
        graduationStatusEntity1.setPen("123123");
        graduationStatusEntity1.setLegalFirstName("Asdad");
        graduationStatusEntity1.setLegalMiddleNames("Adad");
        graduationStatusEntity1.setLegalLastName("sadad");

        UUID studentID2 = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStatusEntity2 = new GraduationStudentRecordEntity();
        graduationStatusEntity2.setStudentID(studentID2);
        graduationStatusEntity2.setStudentStatus("A");
        graduationStatusEntity2.setRecalculateGradStatus("Y");
        graduationStatusEntity2.setProgram("2018-EN");
        graduationStatusEntity2.setSchoolOfRecord("21313121");
        graduationStatusEntity2.setGpa("4");
        graduationStatusEntity2.setBatchId(4000L);
        graduationStatusEntity2.setPen("123456");
        graduationStatusEntity2.setLegalFirstName("Test");
        graduationStatusEntity2.setLegalMiddleNames("QA");
        graduationStatusEntity2.setLegalLastName("Student");

        List<UUID> studentIDs = new ArrayList<>();
        studentIDs.add(studentID1);
        studentIDs.add(studentID2);

        Mockito.when(graduationStatusRepository.findById(studentID1)).thenReturn(Optional.of(graduationStatusEntity1));
        Mockito.when(graduationStatusRepository.findById(studentID2)).thenReturn(Optional.of(graduationStatusEntity2));

        graduationStatusService.updateStudentFlagReadyForBatchJobByStudentIDs("REGALG", studentIDs);
        assertThat(studentIDs).hasSize(2);
    }

    @Test
    public void testUpdateStudentFlagReadyForBatchJobByStudentIDs_when_relcalculateProjectedGrad_is_null() {
        UUID studentID1 = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStatusEntity1 = new GraduationStudentRecordEntity();
        graduationStatusEntity1.setStudentID(studentID1);
        graduationStatusEntity1.setStudentStatus("A");
        graduationStatusEntity1.setRecalculateProjectedGrad("Y");
        graduationStatusEntity1.setProgram("2018-EN");
        graduationStatusEntity1.setSchoolOfRecord("21313121");
        graduationStatusEntity1.setGpa("4");
        graduationStatusEntity1.setBatchId(4000L);
        graduationStatusEntity1.setPen("123123");
        graduationStatusEntity1.setLegalFirstName("Asdad");
        graduationStatusEntity1.setLegalMiddleNames("Adad");
        graduationStatusEntity1.setLegalLastName("sadad");

        UUID studentID2 = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStatusEntity2 = new GraduationStudentRecordEntity();
        graduationStatusEntity2.setStudentID(studentID2);
        graduationStatusEntity2.setStudentStatus("A");
        graduationStatusEntity2.setRecalculateGradStatus("Y");
        graduationStatusEntity2.setProgram("2018-EN");
        graduationStatusEntity2.setSchoolOfRecord("21313121");
        graduationStatusEntity2.setGpa("4");
        graduationStatusEntity2.setBatchId(4000L);
        graduationStatusEntity2.setPen("123456");
        graduationStatusEntity2.setLegalFirstName("Test");
        graduationStatusEntity2.setLegalMiddleNames("QA");
        graduationStatusEntity2.setLegalLastName("Student");

        List<UUID> studentIDs = new ArrayList<>();
        studentIDs.add(studentID1);
        studentIDs.add(studentID2);

        Mockito.when(graduationStatusRepository.findById(studentID1)).thenReturn(Optional.of(graduationStatusEntity1));
        Mockito.when(graduationStatusRepository.findById(studentID2)).thenReturn(Optional.of(graduationStatusEntity2));

        graduationStatusService.updateStudentFlagReadyForBatchJobByStudentIDs("TVRRUN", studentIDs);
        assertThat(studentIDs).hasSize(2);
    }

    @Test
    public void testUpdateStudentFlagReadyForBatchJobByStudentIDs_when_batchID_is_null() {
        UUID studentID1 = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStatusEntity1 = new GraduationStudentRecordEntity();
        graduationStatusEntity1.setStudentID(studentID1);
        graduationStatusEntity1.setStudentStatus("A");
        graduationStatusEntity1.setRecalculateProjectedGrad("Y");
        graduationStatusEntity1.setProgram("2018-EN");
        graduationStatusEntity1.setSchoolOfRecord("21313121");
        graduationStatusEntity1.setGpa("4");
        graduationStatusEntity1.setPen("123123");
        graduationStatusEntity1.setLegalFirstName("Asdad");
        graduationStatusEntity1.setLegalMiddleNames("Adad");
        graduationStatusEntity1.setLegalLastName("sadad");

        UUID studentID2 = UUID.randomUUID();
        GraduationStudentRecordEntity graduationStatusEntity2 = new GraduationStudentRecordEntity();
        graduationStatusEntity2.setStudentID(studentID2);
        graduationStatusEntity2.setStudentStatus("A");
        graduationStatusEntity2.setRecalculateGradStatus("Y");
        graduationStatusEntity2.setProgram("2018-EN");
        graduationStatusEntity2.setSchoolOfRecord("21313121");
        graduationStatusEntity2.setGpa("4");
        graduationStatusEntity2.setPen("123456");
        graduationStatusEntity2.setLegalFirstName("Test");
        graduationStatusEntity2.setLegalMiddleNames("QA");
        graduationStatusEntity2.setLegalLastName("Student");

        List<UUID> studentIDs = new ArrayList<>();
        studentIDs.add(studentID1);
        studentIDs.add(studentID2);

        Mockito.when(graduationStatusRepository.findById(studentID1)).thenReturn(Optional.of(graduationStatusEntity1));
        Mockito.when(graduationStatusRepository.findById(studentID2)).thenReturn(Optional.of(graduationStatusEntity2));

        graduationStatusService.updateStudentFlagReadyForBatchJobByStudentIDs("TVRRUN", studentIDs);
        assertThat(studentIDs).isNotEmpty();
    }

    @Test
    public void testGetNonGradReasonByPen() {
        String pen = "123456789";

        StudentNonGradReasonEntity entity = new StudentNonGradReasonEntity();
        entity.setPen(pen);
        entity.setGradRule1("Rule1");
        entity.setTranscriptRule1("Tr1");
        entity.setDescription1("Test Rule1 Description");
        Mockito.when(studentNonGradReasonRepository.findByPen(pen)).thenReturn(List.of(entity));

        var result = graduationStatusService.getNonGradReason(pen);

        assertThat(result).isNotNull();
        assertThat(result.getGradRule1()).isEqualTo(entity.getGradRule1());
    }

    @Test
    public void testGetNonGradReasonByPen_whenNonGradReason_isNot_Found() {
        String pen = "123456789";

        StudentNonGradReasonEntity entity = new StudentNonGradReasonEntity();
        entity.setPen(pen);
        entity.setGradRule1("Rule1");
        entity.setTranscriptRule1("Tr1");
        entity.setDescription1("Test Rule1 Description");

        Mockito.when(studentNonGradReasonRepository.findByPen(pen)).thenReturn(new ArrayList<>());

        var result = graduationStatusService.getNonGradReason(pen);

        assertThat(result).isNull();
    }
}
