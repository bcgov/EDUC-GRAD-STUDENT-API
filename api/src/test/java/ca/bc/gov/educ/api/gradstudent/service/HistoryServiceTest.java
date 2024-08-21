package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationData;
import ca.bc.gov.educ.api.gradstudent.model.dto.OptionalProgram;
import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.HistoryActivityCodeEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordHistoryRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.HistoryActivityRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramHistoryRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class HistoryServiceTest {

    @Autowired EducGradStudentApiConstants constants;
    @Autowired HistoryService historyService;
    @MockBean CommonService commonService;
    @MockBean GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository;
    @MockBean GraduationStudentRecordRepository graduationStudentRecordRepository;
    @MockBean HistoryActivityRepository historyActivityRepository;
    @MockBean StudentOptionalProgramHistoryRepository studentOptionalProgramHistoryRepository;
    @MockBean WebClient webClient;

    @MockBean
    FetchGradStatusSubscriber fetchGradStatusSubscriber;
    @Mock WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock WebClient.RequestBodySpec requestBodyMock;
    @Mock WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock WebClient.ResponseSpec responseMock;

    // NATS
    @MockBean NatsConnection natsConnection;
    @MockBean Publisher publisher;
    @MockBean Subscriber subscriber;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetStudentEditHistory() {
        // ID
        UUID studentID = UUID.randomUUID();

        List<GraduationStudentRecordHistoryEntity> histList = new ArrayList<>();
        GraduationStudentRecordHistoryEntity graduationStatusEntity = new GraduationStudentRecordHistoryEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setHistoryID(new UUID(1,1));
        graduationStatusEntity.setActivityCode("GRADALG");
        histList.add(graduationStatusEntity);
        when(graduationStudentRecordHistoryRepository.findByStudentID(studentID)).thenReturn(histList);
        var result = historyService.getStudentEditHistory(studentID);
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public  void testGetStudentOptionalProgramHistory() {
        UUID studentID = UUID.randomUUID();
        List<StudentOptionalProgramHistoryEntity> histList = new ArrayList<>();
        StudentOptionalProgramHistoryEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramHistoryEntity();
        gradStudentOptionalProgramEntity.setStudentOptionalProgramID(new UUID(1,1));
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(new UUID(2,2));
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));
        gradStudentOptionalProgramEntity.setHistoryId(new UUID(3,3));
        gradStudentOptionalProgramEntity.setActivityCode("GRADALG");
        histList.add(gradStudentOptionalProgramEntity);

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(gradStudentOptionalProgramEntity.getOptionalProgramID());
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        HistoryActivityCodeEntity ent = new HistoryActivityCodeEntity();
        ent.setCode("GRADALG");
        ent.setDescription("aadsad");
        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramNameUrl(),gradStudentOptionalProgramEntity.getOptionalProgramID()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        when(studentOptionalProgramHistoryRepository.findByStudentID(studentID)).thenReturn(histList);
        when(historyActivityRepository.findById("GRADALG")).thenReturn(Optional.of(ent));
        var result = historyService.getStudentOptionalProgramEditHistory(studentID,"accessToken");
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);

    }

    @Test
    public void testCreateStudentHistory() {
        UUID studentID = new UUID(1, 1);
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("12321321");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setSchoolOfRecord("12345678");

        when(graduationStudentRecordRepository.findById(studentID)).thenReturn(Optional.of(graduationStatusEntity));

        historyService.createStudentHistory(graduationStatusEntity, "ACTIVITYCODE");

        assertThat(graduationStatusEntity).isNotNull();
    }

    @Test
    public void testGetStudentHistoryByHistoryID() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID historyID = UUID.randomUUID();
        GraduationStudentRecordHistoryEntity graduationStatusEntity = new GraduationStudentRecordHistoryEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setRecalculateGradStatus("Y");
        graduationStatusEntity.setProgram("2018-EN");
        graduationStatusEntity.setSchoolOfRecord("223333");
        graduationStatusEntity.setGpa("4");
        graduationStatusEntity.setHistoryID(new UUID(1,1));
        graduationStatusEntity.setActivityCode("GRADALG");
        when(graduationStudentRecordHistoryRepository.findById(historyID)).thenReturn(Optional.of(graduationStatusEntity));
        var result = historyService.getStudentHistoryByID(historyID);
        assertThat(result).isNotNull();
    }

    @Test
    public  void testGetStudentOptionalProgramHistoryByID() {
        UUID studentID = UUID.randomUUID();
        UUID historyID = UUID.randomUUID();
        StudentOptionalProgramHistoryEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramHistoryEntity();
        gradStudentOptionalProgramEntity.setStudentOptionalProgramID(new UUID(1,1));
        gradStudentOptionalProgramEntity.setStudentID(studentID);
        gradStudentOptionalProgramEntity.setOptionalProgramID(new UUID(2,2));
        gradStudentOptionalProgramEntity.setOptionalProgramCompletionDate(new Date(System.currentTimeMillis()));
        gradStudentOptionalProgramEntity.setHistoryId(new UUID(3,3));
        gradStudentOptionalProgramEntity.setActivityCode("GRADALG");

        OptionalProgram optionalProgram = new OptionalProgram();
        optionalProgram.setOptionalProgramID(gradStudentOptionalProgramEntity.getOptionalProgramID());
        optionalProgram.setGraduationProgramCode("2018-EN");
        optionalProgram.setOptProgramCode("FI");
        optionalProgram.setOptionalProgramName("French Immersion");

        HistoryActivityCodeEntity ent = new HistoryActivityCodeEntity();
        ent.setCode("GRADALG");
        ent.setDescription("aadsad");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getGradOptionalProgramNameUrl(),gradStudentOptionalProgramEntity.getOptionalProgramID()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(OptionalProgram.class)).thenReturn(Mono.just(optionalProgram));

        when(studentOptionalProgramHistoryRepository.findById(historyID)).thenReturn(Optional.of(gradStudentOptionalProgramEntity));
        when(historyActivityRepository.findById("GRADALG")).thenReturn(Optional.of(ent));


        var result = historyService.getStudentOptionalProgramHistoryByID(historyID,"accessToken");
        assertThat(result).isNotNull();
    }

    @Test
    public void testGetStudentHistoryByBatchID() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID historyID = UUID.randomUUID();
        List<GraduationStudentRecordHistoryEntity> histList = new ArrayList<>();

        Student std = new Student();
        std.setPen("123123");
        std.setLegalFirstName("Asdad");
        std.setLegalMiddleNames("Adad");
        std.setLegalLastName("sadad");

        GradSearchStudent serObj = new GradSearchStudent();
        serObj.setPen("123123");
        serObj.setLegalFirstName("Asdad");
        serObj.setLegalMiddleNames("Adad");
        serObj.setLegalLastName("sadad");
        GraduationData gd = new GraduationData();
        gd.setGradStudent(serObj);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getPenStudentApiByStudentIdUrl(),studentID))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(Student.class)).thenReturn(Mono.just(std));

        GraduationStudentRecordHistoryEntity graduationStatusEntity = new GraduationStudentRecordHistoryEntity();
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
        graduationStatusEntity.setHistoryID(new UUID(1,1));
        graduationStatusEntity.setActivityCode("GRADALG");
        graduationStatusEntity.setBatchId(4000L);
        graduationStatusEntity.setPen("123123");
        graduationStatusEntity.setLegalFirstName("Asdad");
        graduationStatusEntity.setLegalMiddleNames("Adad");
        graduationStatusEntity.setLegalLastName("sadad");
        histList.add(graduationStatusEntity);
        Pageable paging = PageRequest.of(0, 10);
        Page<GraduationStudentRecordHistoryEntity> hPage = new PageImpl(histList);
        when(graduationStudentRecordHistoryRepository.findByBatchId(4000L,paging)).thenReturn(hPage);
        Page<GraduationStudentRecordHistoryEntity> list = historyService.getStudentHistoryByBatchID(4000L, 0, 10,null);
        assertThat(list).isNotEmpty();
        assertThat(list.getContent()).hasSize(1);
    }

    @Test
    public void testUpdateStudentRecordHistoryDistributionRun() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID historyID = UUID.randomUUID();
        List<GraduationStudentRecordHistoryEntity> histList = new ArrayList<>();

        Student std = new Student();
        std.setPen("123123");
        std.setLegalFirstName("Asdad");
        std.setLegalMiddleNames("Adad");
        std.setLegalLastName("sadad");

        GradSearchStudent serObj = new GradSearchStudent();
        serObj.setPen("123123");
        serObj.setLegalFirstName("Asdad");
        serObj.setLegalMiddleNames("Adad");
        serObj.setLegalLastName("sadad");
        GraduationData gd = new GraduationData();
        gd.setGradStudent(serObj);

        GraduationStudentRecordHistoryEntity graduationStudentRecordHistoryEntity = new GraduationStudentRecordHistoryEntity();
        graduationStudentRecordHistoryEntity.setStudentID(studentID);
        graduationStudentRecordHistoryEntity.setStudentStatus("A");
        graduationStudentRecordHistoryEntity.setRecalculateGradStatus("Y");
        graduationStudentRecordHistoryEntity.setProgram("2018-EN");
        graduationStudentRecordHistoryEntity.setSchoolOfRecord("223333");
        graduationStudentRecordHistoryEntity.setGpa("4");
        graduationStudentRecordHistoryEntity.setHistoryID(new UUID(1,1));
        graduationStudentRecordHistoryEntity.setActivityCode("GRADALG");
        graduationStudentRecordHistoryEntity.setBatchId(4000L);
        graduationStudentRecordHistoryEntity.setPen("123123");
        graduationStudentRecordHistoryEntity.setLegalFirstName("Asdad");
        graduationStudentRecordHistoryEntity.setLegalMiddleNames("Adad");
        graduationStudentRecordHistoryEntity.setLegalLastName("sadad");
        histList.add(graduationStudentRecordHistoryEntity);
        Pageable paging = PageRequest.of(0, 10);
        Page<GraduationStudentRecordHistoryEntity> hPage = new PageImpl(histList);

        GraduationStudentRecordEntity graduationStudentRecordEntity = new GraduationStudentRecordEntity();
        graduationStudentRecordEntity.setStudentID(studentID);

        when(graduationStudentRecordHistoryRepository.findByBatchId(4000L, PageRequest.of(0, Integer.SIZE))).thenReturn(hPage);
        when(graduationStudentRecordHistoryRepository.findByStudentID(studentID)).thenReturn(List.of(graduationStudentRecordHistoryEntity));
        when(graduationStudentRecordHistoryRepository.updateGradStudentUpdateUser(4000L, "USER", LocalDateTime.now())).thenReturn(1);
        when(graduationStudentRecordRepository.findByStudentID(studentID)).thenReturn(graduationStudentRecordEntity);

        var result = historyService.updateStudentRecordHistoryDistributionRun(4000L, "USER", "activityCode", List.of(studentID));
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0);

        when(graduationStudentRecordHistoryRepository.findByBatchId(4000L, PageRequest.of(0, Integer.SIZE))).thenReturn(null);
        result = historyService.updateStudentRecordHistoryDistributionRun(4000L, "USER", "activityCode", List.of(studentID));
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(1);

        when(graduationStudentRecordHistoryRepository.findByBatchId(4000L, PageRequest.of(0, Integer.SIZE))).thenReturn(new PageImpl(List.of()));
        result = historyService.updateStudentRecordHistoryDistributionRun(4000L, "USER", "activityCode", List.of(studentID));
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(1);

        result = historyService.updateStudentRecordHistoryDistributionRun(4000L, "USER", "activityCode", List.of());
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0);

        result = historyService.updateStudentRecordHistoryDistributionRun(4000L, "USER", "", List.of());
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(0);
    }
}
