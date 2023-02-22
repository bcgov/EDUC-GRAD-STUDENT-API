package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCareerProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCareerProgramRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class DataConversionServiceTest {
    @Autowired
    EducGradStudentApiConstants constants;
    @Autowired
    DataConversionService dataConversionService;

    @MockBean
    HistoryService historyService;

    @MockBean
    GraduationStudentRecordRepository graduationStatusRepository;
    @MockBean
    StudentOptionalProgramRepository gradStudentOptionalProgramRepository;
    @MockBean
    StudentCareerProgramRepository gradStudentCareerProgramRepository;

    @MockBean
    GradValidation validation;
    @MockBean
    WebClient webClient;
    @Mock
    WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;
    // NATS
    @MockBean
    NatsConnection natsConnection;
    @MockBean
    Publisher publisher;
    @MockBean
    Subscriber subscriber;

    @Test
    public void testGraduationStudentRecordAsNew() {
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

        var result = dataConversionService.saveGraduationStudentRecord(studentID, graduationStatus, false);

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
    public void testGraduationStudentRecordAsUpdate() {
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
        when(graduationStatusRepository.countStudentGuidPenXrefRecord(studentID)).thenReturn(1L);

        var result = dataConversionService.saveGraduationStudentRecord(studentID, input,false);

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
    public void testGraduationStudentRecordAsOngoingUpdate() {
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

        var result = dataConversionService.saveGraduationStudentRecord(studentID, input,true);

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

    @ParameterizedTest
    @CsvSource({
            "yyyy/MM",
            "yyyy-MM-dd"
    })
    void testSaveStudentOptionalProgramAsNew(String dateFormat) {
        saveStudentOptionalProgramAsNew(dateFormat);
    }

    private void saveStudentOptionalProgramAsNew(String dateFormat) {
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
        studentOptionalProgram.setOptionalProgramCode("FI");
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        StudentOptionalProgramRequestDTO studentOptionalProgramReq = new StudentOptionalProgramRequestDTO();
        studentOptionalProgramReq.setId(gradStudentOptionalProgramID);
        studentOptionalProgramReq.setStudentID(studentID);
        studentOptionalProgramReq.setPen(pen);
        studentOptionalProgramReq.setMainProgramCode("2018-en");
        studentOptionalProgramReq.setOptionalProgramCode("FI");
        studentOptionalProgramReq.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), dateFormat));

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-en");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.empty());
        when(gradStudentOptionalProgramRepository.save(any(StudentOptionalProgramEntity.class))).thenReturn(gradStudentOptionalProgramEntity);
        doNothing().when(historyService).createStudentOptionalProgramHistory(any(), any());

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramDetailsUrl(),optionalProgram.getGraduationProgramCode(), optionalProgram.getOptProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        var result = dataConversionService.saveStudentOptionalProgram(studentOptionalProgramReq, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentOptionalProgramEntity.getId());
        assertThat(result.getOptionalProgramID()).isEqualTo(gradStudentOptionalProgramEntity.getOptionalProgramID());
        assertThat(result.getOptionalProgramCompletionDate()).isEqualTo(EducGradStudentApiUtils.parseDateFromString(studentOptionalProgram.getOptionalProgramCompletionDate()));
    }

    @ParameterizedTest
    @CsvSource({
            "yyyy/MM",
            "yyyy-MM-dd"
    })
    void testSaveStudentOptionalProgramAsUpdate(String dateFormat) {
        saveStudentOptionalProgramAsUpdate(dateFormat);
    }

    private void saveStudentOptionalProgramAsUpdate(String dateFormat) {
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
        studentOptionalProgram.setOptionalProgramCode("FI");
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        StudentOptionalProgramRequestDTO studentOptionalProgramReq = new StudentOptionalProgramRequestDTO();
        studentOptionalProgramReq.setId(gradStudentOptionalProgramID);
        studentOptionalProgramReq.setStudentID(studentID);
        studentOptionalProgramReq.setPen(pen);
        studentOptionalProgramReq.setMainProgramCode("2018-en");
        studentOptionalProgramReq.setOptionalProgramCode("FI");
        studentOptionalProgramReq.setStudentOptionalProgramData("{}");
        studentOptionalProgramReq.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), dateFormat));

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-en");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.of(gradStudentOptionalProgramEntity));
        when(gradStudentOptionalProgramRepository.save(gradStudentOptionalProgramEntity)).thenReturn(gradStudentOptionalProgramEntity);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramDetailsUrl(),optionalProgram.getGraduationProgramCode(), optionalProgram.getOptProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        var result = dataConversionService.saveStudentOptionalProgram(studentOptionalProgramReq, "accessToken");
        doNothing().when(historyService).createStudentOptionalProgramHistory(any(), any());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentOptionalProgramEntity.getId());
        assertThat(result.getOptionalProgramID()).isEqualTo(gradStudentOptionalProgramEntity.getOptionalProgramID());
        assertThat(result.getOptionalProgramCompletionDate()).isEqualTo(EducGradStudentApiUtils.parseDateFromString(studentOptionalProgram.getOptionalProgramCompletionDate()));
    }

    @Test
    public void testSaveStudentCareerProgramAsNew() {
        // ID
        UUID gradStudentCareerProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        String careerProgramCode = "Test";
        String pen = "123456789";

        StudentCareerProgramEntity gradStudentCareerProgramEntity = new StudentCareerProgramEntity();
        gradStudentCareerProgramEntity.setId(gradStudentCareerProgramID);
        gradStudentCareerProgramEntity.setStudentID(studentID);
        gradStudentCareerProgramEntity.setCareerProgramCode(careerProgramCode);

        StudentCareerProgram studentOptionalProgram = new StudentCareerProgram();
        BeanUtils.copyProperties(gradStudentCareerProgramEntity, studentOptionalProgram);

        when(gradStudentCareerProgramRepository.findByStudentIDAndCareerProgramCode(studentID, careerProgramCode)).thenReturn(Optional.empty());
        when(gradStudentCareerProgramRepository.save(gradStudentCareerProgramEntity)).thenReturn(gradStudentCareerProgramEntity);

        var result = dataConversionService.saveStudentCareerProgram(studentOptionalProgram);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentCareerProgramEntity.getId());
        assertThat(result.getCareerProgramCode()).isEqualTo(gradStudentCareerProgramEntity.getCareerProgramCode());

    }

    @Test
    public void testSaveStudentCareerProgramAsUpdate() {
        // ID
        UUID gradStudentCareerProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        String careerProgramCode = "Test";
        String pen = "123456789";

        StudentCareerProgramEntity gradStudentCareerProgramEntity = new StudentCareerProgramEntity();
        gradStudentCareerProgramEntity.setId(gradStudentCareerProgramID);
        gradStudentCareerProgramEntity.setStudentID(studentID);
        gradStudentCareerProgramEntity.setCareerProgramCode(careerProgramCode);

        StudentCareerProgram studentOptionalProgram = new StudentCareerProgram();
        BeanUtils.copyProperties(gradStudentCareerProgramEntity, studentOptionalProgram);

        when(gradStudentCareerProgramRepository.findByStudentIDAndCareerProgramCode(studentID, careerProgramCode)).thenReturn(Optional.of(gradStudentCareerProgramEntity));
        when(gradStudentCareerProgramRepository.save(gradStudentCareerProgramEntity)).thenReturn(gradStudentCareerProgramEntity);

        var result = dataConversionService.saveStudentCareerProgram(studentOptionalProgram);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentCareerProgramEntity.getId());
        assertThat(result.getCareerProgramCode()).isEqualTo(gradStudentCareerProgramEntity.getCareerProgramCode());
    }

    @Test
    public void testDeleteStudentOptionalProgram() {
        // ID
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.of(gradStudentOptionalProgramEntity));
        doNothing().when(historyService).createStudentOptionalProgramHistory(any(), any());

        boolean isExceptionThrown = false;
        try {
            dataConversionService.deleteStudentOptionalProgram(optionalProgramID, studentID);
        } catch (Exception e) {
            isExceptionThrown = true;
        }
        assertThat(isExceptionThrown).isFalse();
    }

    @Test
    public void testDeleteStudentCareerProgram() {
        // ID
        UUID gradStudentCareerProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        String careerProgramCode = "Test";

        StudentCareerProgramEntity gradStudentCareerProgramEntity = new StudentCareerProgramEntity();
        gradStudentCareerProgramEntity.setId(gradStudentCareerProgramID);
        gradStudentCareerProgramEntity.setStudentID(studentID);
        gradStudentCareerProgramEntity.setCareerProgramCode(careerProgramCode);

        when(gradStudentCareerProgramRepository.findByStudentIDAndCareerProgramCode(studentID, careerProgramCode)).thenReturn(Optional.of(gradStudentCareerProgramEntity));
        doNothing().when(historyService).createStudentOptionalProgramHistory(any(), any());

        boolean isExceptionThrown = false;
        try {
            dataConversionService.deleteStudentCareerProgram(careerProgramCode, studentID);
        } catch (Exception e) {
            isExceptionThrown = true;
        }
        assertThat(isExceptionThrown).isFalse();
    }
}