package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.FieldName;
import ca.bc.gov.educ.api.gradstudent.constant.FieldType;
import ca.bc.gov.educ.api.gradstudent.constant.TraxEventType;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.repository.*;
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
class DataConversionServiceTest extends BaseIntegrationTest {
    @Autowired
    EducGradStudentApiConstants constants;
    @Autowired
    DataConversionService dataConversionService;

    @MockBean
    HistoryService historyService;

    @MockBean
    GraduationStatusService graduationStatusService;

    @MockBean
    GraduationStudentRecordRepository graduationStatusRepository;
    @MockBean
    StudentOptionalProgramRepository gradStudentOptionalProgramRepository;
    @MockBean
    StudentCareerProgramRepository gradStudentCareerProgramRepository;
    @MockBean
    GraduationStudentRecordHistoryRepository graduationStatusHistoryRepository;
    @MockBean
    StudentOptionalProgramHistoryRepository gradStudentOptionalProgramHistoryRepository;

    @MockBean
    GradValidation validation;
    @Autowired
    WebClient webClient;

    @Autowired
    FetchGradStatusSubscriber fetchGradStatusSubscriber;

    @Mock
    WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;

    @Test
    public void testGraduationStudentRecordAsNew() {
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
        graduationStatusEntity.setStudentStatus("CUR");
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
        when(graduationStatusRepository.countStudentGuidPenXrefRecord(studentID)).thenReturn(1L);

        var result = dataConversionService.saveGraduationStudentRecord(studentID, input, false);

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
        String pen = "123456789";
        String oldMincode = "12312312";
        String newMincode = "12345678";
        String oldProgram = "2018-EN";
        String newProgram = "2018-PF";
        String oldGrade = "12";
        String newGrade = "11";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("CUR");
        graduationStatusEntity.setStudentGrade(oldGrade);
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram(oldProgram);
        graduationStatusEntity.setSchoolOfRecord(oldMincode);
        graduationStatusEntity.setSchoolAtGrad(oldMincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        OngoingUpdateRequestDTO requestDTO = new OngoingUpdateRequestDTO();
        requestDTO.setPen(pen);
        requestDTO.setStudentID(studentID.toString());
        requestDTO.setEventType(TraxEventType.UPD_GRAD);

        OngoingUpdateFieldDTO field1 = OngoingUpdateFieldDTO.builder()
                .type(FieldType.DATE).name(FieldName.SLP_DATE).value(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM"))
                .build();
        requestDTO.getUpdateFields().add(field1);

        OngoingUpdateFieldDTO field2 = OngoingUpdateFieldDTO.builder()
                .type(FieldType.STRING).name(FieldName.GRAD_PROGRAM).value(newProgram)
                .build();
        requestDTO.getUpdateFields().add(field2);

        OngoingUpdateFieldDTO field3 = OngoingUpdateFieldDTO.builder()
                .type(FieldType.STRING).name(FieldName.STUDENT_GRADE).value(newGrade)
                .build();
        requestDTO.getUpdateFields().add(field3);

        OngoingUpdateFieldDTO field4 = OngoingUpdateFieldDTO.builder()
                .type(FieldType.STRING).name(FieldName.CITIZENSHIP).value("C")
                .build();
        requestDTO.getUpdateFields().add(field4);

        OngoingUpdateFieldDTO field5 = OngoingUpdateFieldDTO.builder()
                .type(FieldType.STRING).name(FieldName.SCHOOL_OF_RECORD).value(newMincode)
                .build();
        requestDTO.getUpdateFields().add(field5);

        OngoingUpdateFieldDTO field6 = OngoingUpdateFieldDTO.builder()
                .type(FieldType.STRING).name(FieldName.RECALC_GRAD_ALG).value(null)
                .build();
        requestDTO.getUpdateFields().add(field6);

        OngoingUpdateFieldDTO field7 = OngoingUpdateFieldDTO.builder()
                .type(FieldType.STRING).name(FieldName.RECALC_TVR).value("Y")
                .build();
        requestDTO.getUpdateFields().add(field7);

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus(null);
        savedGraduationStatus.setRecalculateProjectedGrad("Y");
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());
        savedGraduationStatus.setProgram(newProgram);
        savedGraduationStatus.setStudentGrade(newGrade);
        savedGraduationStatus.setStudentCitizenship("C");
        savedGraduationStatus.setSchoolOfRecord(newMincode);

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        when(this.webClient.delete()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getDeleteStudentAchievements(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Integer.class)).thenReturn(Mono.just(0));

        var result = dataConversionService.updateGraduationStatusByFields(requestDTO, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getPen()).isEqualTo(graduationStatusEntity.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(graduationStatusEntity.getStudentStatus());
        assertThat(result.getGpa()).isEqualTo(graduationStatusEntity.getGpa());

        assertThat(result.getProgramCompletionDate()).isEqualTo(field1.getValue());
        assertThat(result.getProgram()).isEqualTo(field2.getValue());
        assertThat(result.getStudentGrade()).isEqualTo(field3.getValue());
        assertThat(result.getStudentCitizenship()).isEqualTo(field4.getValue());
        assertThat(result.getSchoolOfRecord()).isEqualTo(field5.getValue());
        assertThat(result.getRecalculateGradStatus()).isNull();
        assertThat(result.getRecalculateProjectedGrad()).isEqualTo("Y");
    }

    @Test
    public void testGraduationStudentRecordAsOngoingUpdateForStudentStatus() {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";
        String oldStatus = "ARC";
        String newStatus = "CUR";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(oldStatus);
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        OngoingUpdateRequestDTO requestDTO = new OngoingUpdateRequestDTO();
        requestDTO.setPen(pen);
        requestDTO.setStudentID(studentID.toString());
        requestDTO.setEventType(TraxEventType.UPD_STD_STATUS);

        OngoingUpdateFieldDTO field = OngoingUpdateFieldDTO.builder()
                .type(FieldType.STRING).name(FieldName.STUDENT_STATUS).value(newStatus)
                .build();
        requestDTO.getUpdateFields().add(field);

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus(null);
        savedGraduationStatus.setStudentStatus(newStatus);

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        when(this.webClient.delete()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getDeleteStudentAchievements(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Integer.class)).thenReturn(Mono.just(0));

        var result = dataConversionService.updateGraduationStatusByFields(requestDTO, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getPen()).isEqualTo(graduationStatusEntity.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(newStatus);
        assertThat(result.getSchoolOfRecord()).isEqualTo(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(graduationStatusEntity.getGpa());

        assertThat(result.getRecalculateGradStatus()).isNull();
    }

    @Test
    public void testGraduationStudentRecordAsOngoingUpdateForStudentMergedStatus() {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";
        String oldStatus = "ARC";
        String newStatus = "MER";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(oldStatus);
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        OngoingUpdateRequestDTO requestDTO = new OngoingUpdateRequestDTO();
        requestDTO.setPen(pen);
        requestDTO.setStudentID(studentID.toString());
        requestDTO.setEventType(TraxEventType.UPD_STD_STATUS);

        OngoingUpdateFieldDTO field = OngoingUpdateFieldDTO.builder()
                .type(FieldType.STRING).name(FieldName.STUDENT_STATUS).value(newStatus)
                .build();
        requestDTO.getUpdateFields().add(field);

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus(null);
        savedGraduationStatus.setRecalculateProjectedGrad(null);
        savedGraduationStatus.setStudentGradData(null);
        savedGraduationStatus.setStudentProjectedGradData(null);
        savedGraduationStatus.setStudentStatus(newStatus);

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        when(this.webClient.delete()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getDeleteStudentAchievements(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Integer.class)).thenReturn(Mono.just(0));

        var result = dataConversionService.updateGraduationStatusByFields(requestDTO, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getPen()).isEqualTo(graduationStatusEntity.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(newStatus);
        assertThat(result.getSchoolOfRecord()).isEqualTo(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(graduationStatusEntity.getGpa());

        assertThat(result.getStudentGradData()).isNull();
        assertThat(result.getStudentProjectedGradData()).isNull();
        assertThat(result.getRecalculateGradStatus()).isNull();
        assertThat(result.getRecalculateProjectedGrad()).isNull();
    }

    @Test
    public void testGraduationStudentRecordAsOngoingUpdateForStudentArchivedStatus() {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";
        String oldStatus = "CUR";
        String newStatus = "ARC";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus(oldStatus);
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        OngoingUpdateRequestDTO requestDTO = new OngoingUpdateRequestDTO();
        requestDTO.setPen(pen);
        requestDTO.setStudentID(studentID.toString());
        requestDTO.setEventType(TraxEventType.UPD_STD_STATUS);

        OngoingUpdateFieldDTO field = OngoingUpdateFieldDTO.builder()
                .type(FieldType.STRING).name(FieldName.STUDENT_STATUS).value(newStatus)
                .build();
        requestDTO.getUpdateFields().add(field);

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus(null);
        savedGraduationStatus.setRecalculateProjectedGrad(null);
        savedGraduationStatus.setStudentGradData(null);
        savedGraduationStatus.setStudentProjectedGradData(null);
        savedGraduationStatus.setStudentStatus(newStatus);

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        when(this.webClient.delete()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getDeleteStudentAchievements(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Integer.class)).thenReturn(Mono.just(0));

        var result = dataConversionService.updateGraduationStatusByFields(requestDTO, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getPen()).isEqualTo(graduationStatusEntity.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(newStatus);
        assertThat(result.getSchoolOfRecord()).isEqualTo(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(graduationStatusEntity.getGpa());

        assertThat(result.getStudentGradData()).isNull();
        assertThat(result.getStudentProjectedGradData()).isNull();
        assertThat(result.getRecalculateGradStatus()).isNull();
        assertThat(result.getRecalculateProjectedGrad()).isNull();
    }

    @Test
    public void testGraduationStudentRecordAsOngoingUpdateWhenSCCPStudent_isChangedTo_2018EN() {
        // ID
        UUID studentID = UUID.randomUUID();
        String pen = "123456789";
        String mincode = "12345678";
        String oldProgram = "SCCP";
        String newProgram = "2018-EN";

        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen(pen);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram(oldProgram);
        graduationStatusEntity.setSchoolOfRecord(mincode);
        graduationStatusEntity.setSchoolAtGrad(mincode);
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));

        OngoingUpdateRequestDTO requestDTO = new OngoingUpdateRequestDTO();
        requestDTO.setPen(pen);
        requestDTO.setStudentID(studentID.toString());
        requestDTO.setEventType(TraxEventType.UPD_GRAD);

        OngoingUpdateFieldDTO field1 = OngoingUpdateFieldDTO.builder()
                .type(FieldType.DATE).name(FieldName.SLP_DATE).value(EducGradStudentApiUtils.formatDate(graduationStatusEntity.getProgramCompletionDate(), "yyyy/MM"))
                .build();
        requestDTO.getUpdateFields().add(field1);

        OngoingUpdateFieldDTO field2 = OngoingUpdateFieldDTO.builder()
                .type(FieldType.STRING).name(FieldName.GRAD_PROGRAM).value(newProgram)
                .build();
        requestDTO.getUpdateFields().add(field2);

        GraduationStudentRecordEntity savedGraduationStatus = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatusEntity, savedGraduationStatus);
        savedGraduationStatus.setRecalculateGradStatus(null);
        savedGraduationStatus.setProgramCompletionDate(graduationStatusEntity.getProgramCompletionDate());
        savedGraduationStatus.setProgram(newProgram);

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));
        when(graduationStatusRepository.saveAndFlush(graduationStatusEntity)).thenReturn(savedGraduationStatus);

        when(this.webClient.delete()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getArchiveStudentAchievements(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestBodyMock);
        when(this.requestBodyMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Integer.class)).thenReturn(Mono.just(0));

        var result = dataConversionService.updateGraduationStatusByFields(requestDTO, "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(graduationStatusEntity.getStudentID());
        assertThat(result.getPen()).isEqualTo(graduationStatusEntity.getPen());
        assertThat(result.getStudentStatus()).isEqualTo(graduationStatusEntity.getStudentStatus());
        assertThat(result.getProgram()).isEqualTo(newProgram);
        assertThat(result.getSchoolOfRecord()).isEqualTo(graduationStatusEntity.getSchoolOfRecord());
        assertThat(result.getGpa()).isEqualTo(graduationStatusEntity.getGpa());

        assertThat(result.getRecalculateGradStatus()).isNull();
        assertThat(result.getProgramCompletionDate()).isEqualTo(field1.getValue());
    }

    @Test
    public void testGraduationStudentRecordAsNewForOngoingUpdate() {
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

        var result = dataConversionService.saveGraduationStudentRecord(studentID, graduationStatus, true);

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

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setProgram("2018-EN");
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setPen(pen);

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
        studentOptionalProgramReq.setMainProgramCode("2018-EN");
        studentOptionalProgramReq.setOptionalProgramCode("FI");
        studentOptionalProgramReq.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), dateFormat));

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStudentRecordEntity));
        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.empty());
        when(gradStudentOptionalProgramRepository.save(any(StudentOptionalProgramEntity.class))).thenReturn(gradStudentOptionalProgramEntity);
        doNothing().when(historyService).createStudentOptionalProgramHistory(any(), any());
        when(graduationStatusService.getOptionalProgram(optionalProgram.getGraduationProgramCode(), optionalProgram.getOptProgramCode(), "accessToken")).thenReturn(optionalProgram);

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

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setProgram("2018-EN");
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setPen(pen);

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
        studentOptionalProgramReq.setMainProgramCode("2018-EN");
        studentOptionalProgramReq.setOptionalProgramCode("FI");
        studentOptionalProgramReq.setStudentOptionalProgramData("{}");
        studentOptionalProgramReq.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), dateFormat));

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStudentRecordEntity));
        when(gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID)).thenReturn(Optional.of(gradStudentOptionalProgramEntity));
        when(gradStudentOptionalProgramRepository.save(gradStudentOptionalProgramEntity)).thenReturn(gradStudentOptionalProgramEntity);
        when(graduationStatusService.getOptionalProgram(optionalProgram.getGraduationProgramCode(), optionalProgram.getOptProgramCode(), "accessToken")).thenReturn(optionalProgram);

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
        UUID gradStudentOptionalProgramID = UUID.randomUUID();
        UUID gradStudentCareerProgramID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID optionalProgramID = UUID.randomUUID();
        String careerProgramCode = "Test";
        String pen = "123456789";

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setProgram("2018-EN");
        graduationStudentRecordEntity.setStudentID(studentID);
        graduationStudentRecordEntity.setPen(pen);

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgramID);
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(optionalProgramID);
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));

        StudentOptionalProgram studentOptionalProgram = new StudentOptionalProgram();
        BeanUtils.copyProperties(gradStudentOptionalProgramEntity, studentOptionalProgram);
        studentOptionalProgram.setOptionalProgramCode("CP");
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        StudentCareerProgramEntity gradStudentCareerProgramEntity = new StudentCareerProgramEntity();
        gradStudentCareerProgramEntity.setId(gradStudentCareerProgramID);
        gradStudentCareerProgramEntity.setStudentID(studentID);
        gradStudentCareerProgramEntity.setCareerProgramCode(careerProgramCode);

        StudentCareerProgram studentCareerProgram = new StudentCareerProgram();
        BeanUtils.copyProperties(gradStudentCareerProgramEntity, studentCareerProgram);

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("CP");
        optionalProgram.setOptionalProgramName("Career Program");

        when(graduationStatusRepository.findById(studentID)).thenReturn(Optional.of(graduationStudentRecordEntity));
        when(gradStudentCareerProgramRepository.findByStudentIDAndCareerProgramCode(studentID, careerProgramCode)).thenReturn(Optional.empty());
        when(gradStudentCareerProgramRepository.save(gradStudentCareerProgramEntity)).thenReturn(gradStudentCareerProgramEntity);
        when(graduationStatusService.getOptionalProgram(optionalProgram.getGraduationProgramCode(), optionalProgram.getOptProgramCode(), "accessToken")).thenReturn(optionalProgram);

        var result = dataConversionService.saveStudentCareerProgram(studentCareerProgram);
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

    @Test
    public void testDeleteAll() {
        // ID
        UUID studentID = UUID.randomUUID();

        when(graduationStatusRepository.existsById(studentID)).thenReturn(true);

        boolean isExceptionThrown = false;
        try {
            dataConversionService.deleteAllDependencies(studentID);
            dataConversionService.deleteGraduationStatus(studentID);
        } catch (Exception e) {
            isExceptionThrown = true;
        }
        assertThat(isExceptionThrown).isFalse();

    }

    @Test
    public void testSaveStudentOptionalProgram_whenOptionalProgram_doesNotExist_returnsNull() {
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

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        StudentOptionalProgramRequestDTO studentOptionalProgramReq = new StudentOptionalProgramRequestDTO();
        studentOptionalProgramReq.setId(gradStudentOptionalProgramID);
        studentOptionalProgramReq.setStudentID(studentID);
        studentOptionalProgramReq.setPen(pen);
        studentOptionalProgramReq.setMainProgramCode("2018-EN");
        studentOptionalProgramReq.setOptionalProgramCode("FI");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramDetailsUrl(),optionalProgram.getGraduationProgramCode(), optionalProgram.getOptProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.empty());

        var result = dataConversionService.saveStudentOptionalProgram(studentOptionalProgramReq, "123");
        assertThat(result).isNull();
    }

    @Test
    public void testSaveStudentOptionalProgram_whenOptionalProgram_throwsException_returnsNull() {
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
        studentOptionalProgram.setOptionalProgramCode("AD");
        studentOptionalProgram.setOptionalProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStudentOptionalProgramEntity.getOptionalProgramCompletionDate(), "yyyy-MM-dd" ));

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setGraduationProgramCode("SCCP");
        optionalProgram.setOptProgramCode("AD");
        optionalProgram.setOptionalProgramName("French Immersion");

        StudentOptionalProgramRequestDTO studentOptionalProgramReq = new StudentOptionalProgramRequestDTO();
        studentOptionalProgramReq.setId(gradStudentOptionalProgramID);
        studentOptionalProgramReq.setStudentID(studentID);
        studentOptionalProgramReq.setPen(pen);
        studentOptionalProgramReq.setMainProgramCode("SCCP");
        studentOptionalProgramReq.setOptionalProgramCode("AD");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramDetailsUrl(),optionalProgram.getGraduationProgramCode(), optionalProgram.getOptProgramCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenThrow(new RuntimeException("Program API is down!"));

        var result = dataConversionService.saveStudentOptionalProgram(studentOptionalProgramReq, "123");
        assertThat(result).isNull();
    }
}
