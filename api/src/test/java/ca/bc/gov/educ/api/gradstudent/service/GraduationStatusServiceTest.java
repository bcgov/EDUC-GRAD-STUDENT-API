package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordSearchRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GraduationStatusServiceTest {

    @Autowired
    EducGradStudentApiConstants constants;
    @Autowired GraduationStatusService graduationStatusService;
    @MockBean GradStudentService gradStudentService;
    @MockBean GraduationStudentRecordRepository graduationStatusRepository;
    @MockBean StudentOptionalProgramRepository gradStudentOptionalProgramRepository;
    @MockBean CommonService commonService;
    @MockBean GradValidation validation;
    @MockBean WebClient webClient;
    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;
    // NATS
    @MockBean NatsConnection natsConnection;
    @MockBean Publisher publisher;
    @MockBean Subscriber subscriber;

    @Autowired
    GraduationStudentRecordSearchRepository graduationStudentRecordSearchRepository;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

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
        graduationStatusEntity.setProgram("2018-en");
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
        graduationStatusEntity.setProgram("2018-en");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setStudentStatus("A");

        StudentStatus studentStatus = new StudentStatus();
        studentStatus.setCode("CUR");
        studentStatus.setDescription(null);

        GradProgram program = new GradProgram();
        program.setProgramCode("2018-en");
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
        graduationStatus.setProgram("2018-en");
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
        graduationStatusEntity.setProgram("2018-en");
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
        graduationStatusEntity.setProgram("2018-en");
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

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

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
        graduationStatusEntity.setProgram("2018-en");
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
        input.setProgram("2018-en");
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setProgram("2018-en");
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        GradProgram program = new GradProgram();
        program.setProgramCode("2018-en");
        program.setProgramName("Graduation Program 2018");

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
        input.setProgram("2018-en");
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setProgram("2018-en");
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        GradProgram program = new GradProgram();
        program.setProgramCode("2018-en");
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
        graduationStatusEntity.setProgram("2018-en");
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
        savedGraduationStatus.setProgram("2018-en");
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
        input.setProgram("2018-en");
        input.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM" ));

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus("Y");
        savedGraduationStatus.setProgram("2018-en");
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());

        GradProgram program = new GradProgram();
        program.setProgramCode("2018-en");
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
        graduationStatusEntity.setProgram("2018-en");
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
        graduationStatusEntity.setProgram("2018-en");
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
        graduationStatusEntity.setProgram("2018-en");
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
        optionalProgram.setGraduationProgramCode("2018-en");
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
        gradStudentOptionalProgramReq.setMainProgramCode("2018-en");
        gradStudentOptionalProgramReq.setOptionalProgramCode("FI");
        gradStudentOptionalProgramReq.setOptionalProgramCompletionDate(studentOptionalProgram.getOptionalProgramCompletionDate());

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-en");
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
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(UUID.randomUUID());
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setRecalculateGradStatus("Y");

        when(graduationStatusRepository.findByRecalculateGradStatus(graduationStatusEntity.getRecalculateGradStatus())).thenReturn(List.of(graduationStatusEntity));
        var result = graduationStatusService.getStudentsForGraduation();
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        GraduationStudentRecord responseGraduationStatus = result.get(0);
        assertThat(responseGraduationStatus.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(responseGraduationStatus.getProgram()).isEqualTo(graduationStatusEntity.getProgram());
    }

    /*@Test
    public void testSearchGraduationStudentRecords() {

        final UUID studentId = UUID.randomUUID();
        final String pen = "142524123";
        final String schoolOfRecord = "06299164";
        final String distCode = "062";
        final String programCode = "2018-EN";

        List<String> pens = new ArrayList<>();
        //pens.add(pen);

        Student student = new Student();
        student.setPen(pen);
        student.setStudentID(studentId.toString());

        List<String> schoolOfRecords = new ArrayList<>();
        //schoolOfRecords.add(schoolOfRecord);

        School school = new School();
        school.setMinCode(schoolOfRecord);
        school.setSchoolName("Test School");

        CommonSchool commonSchool = new CommonSchool();
        commonSchool.setSchlNo(schoolOfRecord);
        commonSchool.setSchoolName(school.getSchoolName());
        commonSchool.setSchoolCategoryCode("02");

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

//        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
//        graduationStudentRecordEntity.setStudentID(studentId);
//        graduationStudentRecordEntity.setProgram(programCode);
//        graduationStudentRecordEntity.setSchoolOfRecord(schoolOfRecord);
//        graduationStudentRecordEntity.setStudentStatus("CUR");

//        GraduationStudentRecordSearchCriteria searchCriteria = GraduationStudentRecordSearchCriteria.builder()
//                .studentIds(searchRequest.getPens())
//                .schoolOfRecords(searchRequest.getSchoolOfRecords())
//                .districts(searchRequest.getDistricts())
//                .programs(searchRequest.getPrograms())
//                .build();
//        Specification<GraduationStudentRecordEntity> spec = new GraduationStudentRecordSearchSpecification(searchCriteria);

//        when(graduationStudentRecordSearchRepository.findAll(Specification.where(spec))).thenReturn(List.of(graduationStudentRecordEntity));

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

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

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolByMincodeSchoolApiUrl(),schoolOfRecord))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CommonSchool.class)).thenReturn(Mono.just(commonSchool));

        final ParameterizedTypeReference<List<CommonSchool>> commonSchoolsType = new ParameterizedTypeReference<>() {
        };

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(constants.getSchoolsSchoolApiUrl())).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(commonSchoolsType)).thenReturn(Mono.just(List.of(commonSchool)));

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

    }*/


    @Test
    public void testGetStudentsForProjectedGraduation() {
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(UUID.randomUUID());
        graduationStatusEntity.setStudentStatus("CUR");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setRecalculateProjectedGrad("Y");

        when(graduationStatusRepository.findByRecalculateProjectedGrad(graduationStatusEntity.getRecalculateProjectedGrad())).thenReturn(List.of(graduationStatusEntity));
        var result = graduationStatusService.getStudentsForProjectedGraduation();
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
        GraduationStudentRecord responseGraduationStatus = result.get(0);
        assertThat(responseGraduationStatus.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(responseGraduationStatus.getProgram()).isEqualTo(graduationStatusEntity.getProgram());
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
        optionalProgram.setGraduationProgramCode("2018-en");
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
    	
    	graduationStatusService.restoreGradStudentRecord(studentID, false);
    	
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
    	
    	graduationStatusService.restoreGradStudentRecord(studentID, true);
    	
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

        GraduationStudentRecordEntity graduationStatusEntity2 = new GraduationStudentRecordEntity();
        graduationStatusEntity2.setStudentID(studentID);
        graduationStatusEntity2.setPen("12321321");
        graduationStatusEntity2.setStudentStatus("A");
        graduationStatusEntity2.setSchoolOfRecord("12345678");
        graduationStatusEntity2.setRecalculateProjectedGrad(null);
        graduationStatusEntity2.setBatchId(batchId);


        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity2)).thenReturn(graduationStatusEntity2);

        graduationStatusService.saveStudentRecordProjectedTVRRun(studentID, batchId);

    }

    @Test
    public void testGetStudentDataByStudentIds() {
        // ID
        List<UUID> sList = Arrays.asList(UUID.randomUUID());
        List<GraduationStudentRecordEntity> histList = new ArrayList<>();

        GradSearchStudent serObj = new GradSearchStudent();
        serObj.setPen("123123");
        serObj.setLegalFirstName("Asdad");
        serObj.setLegalMiddleNames("Adad");
        serObj.setLegalLastName("sadad");
        GraduationData gd = new GraduationData();
        gd.setGradStudent(serObj);

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(sList.get(0));
        graduationStatusEntity.setStudentStatus("A");
        try {
            graduationStatusEntity.setStudentGradData(new ObjectMapper().writeValueAsString(gd));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-en");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setBatchId(4000L);
        graduationStatusEntity.setPen("123123");
        graduationStatusEntity.setLegalFirstName("Asdad");
        graduationStatusEntity.setLegalMiddleNames("Adad");
        graduationStatusEntity.setLegalLastName("sadad");
        histList.add(graduationStatusEntity);

        when(graduationStatusRepository.findByStudentIDIn(sList)).thenReturn(histList);
        List<GraduationStudentRecordEntity> list = graduationStatusService.getStudentDataByStudentIDs(sList,null);
        assertThat(list).isNotEmpty();
        assertThat(list).hasSize(1);
    }

    @Test
    public void testGetStudentsForYearlyDistribution() {

        List<GraduationStudentRecordEntity> histList = new ArrayList<>();

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(new UUID(1,1));
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-en");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setBatchId(4000L);
        graduationStatusEntity.setPen("123123");
        graduationStatusEntity.setLegalFirstName("Asdad");
        graduationStatusEntity.setLegalMiddleNames("Adad");
        graduationStatusEntity.setLegalLastName("sadad");
        histList.add(graduationStatusEntity);

        when(graduationStatusRepository.findStudentsForYearlyDistribution()).thenReturn(histList);
        List<UUID> list = graduationStatusService.getStudentsForYearlyDistribution();
        assertThat(list).isNotEmpty();
        assertThat(list).hasSize(1);
    }
}
