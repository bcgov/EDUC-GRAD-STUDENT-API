package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradBusinessRuleException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CommonServiceTest extends BaseIntegrationTest {

    @Autowired EducGradStudentApiConstants constants;
    @Autowired CommonService commonService;
    @Autowired GradStudentReportService gradStudentReportService;
    @MockBean  ReportGradStudentDataRepository reportGradStudentDataRepository;
    @MockBean  ReportGradSchoolYearEndRepository reportGradSchoolYearEndRepository;

    @MockBean GradStudentService gradStudentService;
    @MockBean GraduationStatusService graduationStatusService;
    @MockBean StudentCareerProgramRepository gradStudentCareerProgramRepository;
    @MockBean StudentNoteRepository studentNoteRepository;
    @MockBean StudentStatusRepository studentStatusRepository;
    @MockBean HistoryActivityRepository historyActivityRepository;
    @MockBean WebClient webClient;

    @MockBean
    FetchGradStatusSubscriber fetchGradStatusSubscriber;
    @Mock
    WebClient.RequestHeadersSpec requestHeadersMock;
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
        studentStatusRepository.save(createStudentStatuses());
    }

    @After
    public void tearDown() {
        /**
         * Placeholder method
         */
    }

    @Test
    public void testGetReportGradStudentDataBySchoolId() {
        // ID
        UUID studentID = UUID.randomUUID();
        UUID schoolId = UUID.randomUUID();

        ReportGradStudentDataEntity reportGradStudentDataEntity = new ReportGradStudentDataEntity();
        reportGradStudentDataEntity.setGraduationStudentRecordId(studentID);
        reportGradStudentDataEntity.setSchoolOfRecordId(schoolId);
        reportGradStudentDataEntity.setFirstName("Jonh");

        when(reportGradStudentDataRepository.findReportGradStudentDataEntityBySchoolOfRecordIdOrderBySchoolNameAscLastNameAsc(schoolId)).thenReturn(List.of(reportGradStudentDataEntity));
        var result = gradStudentReportService.getGradStudentDataBySchoolId(schoolId);
        assertThat(result).isNotNull();

        ReportGradSchoolYearEndEntity schoolYearEndEntity = new ReportGradSchoolYearEndEntity();
        schoolYearEndEntity.setSchoolId(reportGradStudentDataEntity.getSchoolOfRecordId());

        when(reportGradSchoolYearEndRepository.findAll()).thenReturn(List.of(schoolYearEndEntity));
        var schoolIds = gradStudentReportService.getGradSchoolsForNonGradYearEndReport();
        assertThat(schoolIds).hasSize(1);
        assertThat(schoolIds.get(0)).isEqualTo(schoolId);
    }

    @Test
    public void testGetReportGradStudentDataByDistrict() {
        UUID schoolId = UUID.randomUUID();
        UUID districtId = UUID.randomUUID();

        ReportGradSchoolYearEndEntity districtYearEndEntity = new ReportGradSchoolYearEndEntity();
        districtYearEndEntity.setSchoolId(schoolId);

        School school = new School();
        school.setSchoolId(districtYearEndEntity.getSchoolId().toString());
        school.setMincode("12345678");
        school.setDistrictId(districtId.toString());

        when(reportGradSchoolYearEndRepository.findAll()).thenReturn(List.of(districtYearEndEntity));

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getSchoolBySchoolIdUrl(), schoolId))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(School.class)).thenReturn(Mono.just(school));

        var districtIds = gradStudentReportService.getGradDistrictsForNonGradYearEndReport("accessToken");
        assertThat(districtIds).hasSize(1);
        assertThat(districtIds.get(0)).isEqualTo(districtId);
    }

    @Test
    public void testGetReportGradStudentData() {
        // ID
        UUID studentID = UUID.randomUUID();

        ReportGradStudentDataEntity reportGradStudentDataEntity = new ReportGradStudentDataEntity();
        reportGradStudentDataEntity.setGraduationStudentRecordId(studentID);
        reportGradStudentDataEntity.setFirstName("Jonh");

        when(reportGradStudentDataRepository.findReportGradStudentDataEntityByGraduationStudentRecordIdInOrderBySchoolNameAscLastNameAsc(List.of(reportGradStudentDataEntity.getGraduationStudentRecordId()))).thenReturn(List.of(reportGradStudentDataEntity));
        var result = gradStudentReportService.getGradStudentDataByStudentGuids(List.of(reportGradStudentDataEntity.getGraduationStudentRecordId()));
        assertThat(result).isNotNull();

    }

    @Test
    public void testGetStudentCareerProgram() {
        // UUID
        final UUID studentID = UUID.randomUUID();
        // Career Program
        final CareerProgram gradCareerProgram = new CareerProgram();
        gradCareerProgram.setCode("TEST");
        gradCareerProgram.setDescription("Test Code Name");

        // Student Career Program Data
        final List<StudentCareerProgramEntity> gradStudentCareerProgramList = new ArrayList<>();
        final StudentCareerProgramEntity studentCareerProgram = new StudentCareerProgramEntity();
        studentCareerProgram.setId(UUID.randomUUID());
        studentCareerProgram.setStudentID(studentID);
        studentCareerProgram.setCareerProgramCode(gradCareerProgram.getCode());
        gradStudentCareerProgramList.add(studentCareerProgram);

        when(gradStudentCareerProgramRepository.existsByCareerProgramCode(gradCareerProgram.getCode())).thenReturn(gradStudentCareerProgramList);
        var result = commonService.getStudentCareerProgram(gradCareerProgram.getCode());
        assertThat(result).isTrue();
    }

    @Test
    public void testGetAllStudentCareerProgramsList() {
        // UUID
        final UUID studentID = UUID.randomUUID();
        // Career Program
        final CareerProgram gradCareerProgram = new CareerProgram();
        gradCareerProgram.setCode("TEST");
        gradCareerProgram.setDescription("Test Code Name");

        // Student Career Program Data
        final List<StudentCareerProgramEntity> gradStudentCareerProgramList = new ArrayList<>();
        final StudentCareerProgramEntity studentCareerProgram1 = new StudentCareerProgramEntity();
        studentCareerProgram1.setId(UUID.randomUUID());
        studentCareerProgram1.setStudentID(studentID);
        studentCareerProgram1.setCareerProgramCode(gradCareerProgram.getCode());
        gradStudentCareerProgramList.add(studentCareerProgram1);

        final StudentCareerProgramEntity studentCareerProgram2 = new StudentCareerProgramEntity();
        studentCareerProgram2.setId(UUID.randomUUID());
        studentCareerProgram2.setStudentID(studentID);
        studentCareerProgram2.setCareerProgramCode(gradCareerProgram.getCode());
        gradStudentCareerProgramList.add(studentCareerProgram2);

        when(gradStudentCareerProgramRepository.findByStudentID((studentID))).thenReturn(gradStudentCareerProgramList);

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getCareerProgramByCodeUrl(), gradCareerProgram.getCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CareerProgram.class)).thenReturn(Mono.just(gradCareerProgram));

        var result = commonService.getAllGradStudentCareerProgramList(studentID.toString(), "accessToken");

        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getStudentID()).isEqualTo(studentID);
        assertThat(result.get(0).getCareerProgramCode()).isEqualTo(gradCareerProgram.getCode());
        assertThat(result.get(0).getCareerProgramName()).isEqualTo(gradCareerProgram.getDescription());
        assertThat(result.get(1).getStudentID()).isEqualTo(studentID);
        assertThat(result.get(1).getCareerProgramCode()).isEqualTo(gradCareerProgram.getCode());
        assertThat(result.get(1).getCareerProgramName()).isEqualTo(gradCareerProgram.getDescription());
    }

    @Test
    public void testGetAllStudentNotes() {
        // UUID
        final UUID studentID = UUID.randomUUID();

        final List<StudentRecordNoteEntity> allNotesList = new ArrayList<>();

        final StudentRecordNoteEntity note1 = new StudentRecordNoteEntity();
        note1.setId(UUID.randomUUID());
        note1.setStudentID(studentID);
        note1.setNote("Test1 Comments");
        note1.setUpdateDate(LocalDateTime.now());
        allNotesList.add(note1);

        final StudentRecordNoteEntity note2 = new StudentRecordNoteEntity();
        note2.setId(UUID.randomUUID());
        note2.setStudentID(studentID);
        note2.setNote("Test2 Comments");
        note2.setUpdateDate(LocalDateTime.now());
        allNotesList.add(note2);

        when(studentNoteRepository.findByStudentID(studentID)).thenReturn(allNotesList);

        var result = commonService.getAllStudentNotes(studentID);

        assertThat(result).isNotNull().hasSize(2);
        assertThat(result.get(0).getStudentID()).isEqualTo(studentID.toString());
        assertThat(result.get(1).getStudentID()).isEqualTo(studentID.toString());
    }

    @Test
    public void testSaveStudentNote_thenReturnCreateSuccess() {
        // ID
        final UUID studentID = UUID.randomUUID();

        final StudentNote studentNote = new StudentNote();
        studentNote.setStudentID(studentID.toString());
        studentNote.setNote("Test Note Body");

        final StudentRecordNoteEntity studentNoteEntity = new StudentRecordNoteEntity();
        studentNoteEntity.setStudentID(studentID);
        studentNoteEntity.setNote("Test Note Body");

        when(this.studentNoteRepository.save(studentNoteEntity)).thenReturn(studentNoteEntity);

        var result = commonService.saveStudentNote(studentNote);

        assertThat(result).isNotNull();
        assertThat(result.getStudentID()).isEqualTo(studentID.toString());
        assertThat(result.getNote()).isEqualTo(studentNote.getNote());
    }

    @Test
    public void testSaveStudentNoteWithExistingOne_thenReturnUpdateSuccess() {
        // ID
        final UUID noteID = UUID.randomUUID();
        final UUID studentID = UUID.randomUUID();

        final StudentNote studentNote = new StudentNote();
        studentNote.setId(noteID);
        studentNote.setStudentID(studentID.toString());
        studentNote.setNote("Test Note Body");

        final StudentRecordNoteEntity studentNoteEntity = new StudentRecordNoteEntity();
        studentNoteEntity.setId(noteID);
        studentNoteEntity.setStudentID(studentID);
        studentNoteEntity.setNote("Test Note Body");

        final Optional<StudentRecordNoteEntity> optional = Optional.of(studentNoteEntity);

        when(this.studentNoteRepository.findById(noteID)).thenReturn(optional);
        when(this.studentNoteRepository.save(studentNoteEntity)).thenReturn(studentNoteEntity);

        var result = commonService.saveStudentNote(studentNote);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(noteID);
        assertThat(result.getStudentID()).isEqualTo(studentID.toString());
        assertThat(result.getNote()).isEqualTo(studentNote.getNote());
    }

    @Test
    public void testDeleteNote() {
        // ID
        final UUID noteID = UUID.randomUUID();
        final UUID studentID = UUID.randomUUID();

        final StudentRecordNoteEntity studentNoteEntity = new StudentRecordNoteEntity();
        studentNoteEntity.setId(noteID);
        studentNoteEntity.setStudentID(studentID);
        studentNoteEntity.setNote("Test Note Body");

        final Optional<StudentRecordNoteEntity> optional = Optional.of(studentNoteEntity);

        when(this.studentNoteRepository.findById(noteID)).thenReturn(optional);

        var result = commonService.deleteNote(noteID);

        assertThat(result).isEqualTo(1);

    }

    @Test
    public void testDeleteNoteWhenGivenNoteIdDoesNotExist() {
        final UUID noteID = UUID.randomUUID();
        final Optional<StudentRecordNoteEntity> optional = Optional.empty();

        when(this.studentNoteRepository.findById(noteID)).thenReturn(optional);

        var result = commonService.deleteNote(noteID);

        assertThat(result).isZero();

    }
    
    @Test
	public void testGetAllStudentStatusCodeList() {
		List<StudentStatusEntity> gradStudentStatusList = new ArrayList<>();
		StudentStatusEntity obj = new StudentStatusEntity();
		obj.setCode("DC");
		obj.setDescription("Data Correction by School");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(LocalDateTime.now());
		obj.setUpdateDate(LocalDateTime.now());
		gradStudentStatusList.add(obj);
		obj = new StudentStatusEntity();
		obj.setCode("CC");
		obj.setDescription("Courses not complete");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(LocalDateTime.now());
		obj.setUpdateDate(LocalDateTime.now());
		gradStudentStatusList.add(obj);
		Mockito.when(studentStatusRepository.findAll()).thenReturn(gradStudentStatusList);
        List<StudentStatus> result = commonService.getAllStudentStatusCodeList();
        assertThat(result).isNotNull();
	}
	
	@Test
	public void testGetSpecificStudentStatusCode() {
		String reasonCode = "DC";
		StudentStatus obj = new StudentStatus();
		obj.setCode("DC");
		obj.setDescription("Data Correction by School");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(LocalDateTime.now());
		obj.setUpdateDate(LocalDateTime.now());
		StudentStatusEntity objEntity = new StudentStatusEntity();
		objEntity.setCode("DC");
		objEntity.setDescription("Data Correction by School");
		objEntity.setCreateUser("GRADUATION");
		objEntity.setUpdateUser("GRADUATION");
		objEntity.setCreateDate(LocalDateTime.now());
		objEntity.setUpdateDate(LocalDateTime.now());
		Optional<StudentStatusEntity> ent = Optional.of(objEntity);
		Mockito.when(studentStatusRepository.findById(reasonCode)).thenReturn(ent);
        StudentStatus result = commonService.getSpecificStudentStatusCode(reasonCode);
        assertThat(result).isNotNull();
	}
	
	@Test
	public void testGetSpecificStudentStatusCodeReturnsNull() {
		String reasonCode = "DC";
		Mockito.when(studentStatusRepository.findById(reasonCode)).thenReturn(Optional.empty());
        StudentStatus result = commonService.getSpecificStudentStatusCode(reasonCode);
        assertThat(result).isNull();
	}
	
	@Test
	public void testCreateStudentStatus() {
		StudentStatus obj = new StudentStatus();
		obj.setCode("DC");
		obj.setDescription("Data Correction by School");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(LocalDateTime.now());
		obj.setUpdateDate(LocalDateTime.now());
		StudentStatusEntity objEntity = new StudentStatusEntity();
		objEntity.setCode("DC");
		objEntity.setDescription("Data Correction by School");
		objEntity.setCreateUser("GRADUATION");
		objEntity.setUpdateUser("GRADUATION");
		objEntity.setCreateDate(LocalDateTime.now());
		objEntity.setUpdateDate(LocalDateTime.now());
		Mockito.when(studentStatusRepository.findById(obj.getCode())).thenReturn(Optional.empty());
		Mockito.when(studentStatusRepository.save(objEntity)).thenReturn(objEntity);
        StudentStatus result = commonService.createStudentStatus(obj);
        assertThat(result).isNotNull();
		
	}
	
	@Test(expected = GradBusinessRuleException.class)
	public void testCreateStudentStatus_codeAlreadyExists() {
		StudentStatus obj = new StudentStatus();
		obj.setCode("DC");
		obj.setDescription("Data Correction by School");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(LocalDateTime.now());
		obj.setUpdateDate(LocalDateTime.now());
		StudentStatusEntity objEntity = new StudentStatusEntity();
		objEntity.setCode("DC");
		objEntity.setDescription("Data Correction by School");
		objEntity.setCreateUser("GRADUATION");
		objEntity.setUpdateUser("GRADUATION");
		objEntity.setCreateDate(LocalDateTime.now());
		objEntity.setUpdateDate(LocalDateTime.now());
		Optional<StudentStatusEntity> ent = Optional.of(objEntity);
		Mockito.when(studentStatusRepository.findById(obj.getCode())).thenReturn(ent);
		StudentStatus result = commonService.createStudentStatus(obj);
        assertThat(result).isNotNull();
		
	}
	
	@Test
	public void testUpdateStudentStatus() {
		StudentStatus obj = new StudentStatus();
		obj.setCode("DC");
		obj.setDescription("Data Correction by Schools");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(LocalDateTime.now());
		obj.setUpdateDate(LocalDateTime.now());
		StudentStatusEntity objEntity = new StudentStatusEntity();
		objEntity.setCode("DC");
		objEntity.setDescription("Data Correction by School");
		objEntity.setCreateUser("GRADUATION");
		objEntity.setUpdateUser("GRADUATION");
		objEntity.setCreateDate(LocalDateTime.now());
		objEntity.setUpdateDate(LocalDateTime.now());
		Optional<StudentStatusEntity> ent = Optional.of(objEntity);
		Mockito.when(studentStatusRepository.findById(obj.getCode())).thenReturn(ent);
		Mockito.when(studentStatusRepository.save(objEntity)).thenReturn(objEntity);
        StudentStatus result = commonService.updateStudentStatus(obj);
        assertThat(result).isNotNull();
	}
	
	@Test
	public void testUpdateStudentStatus_noCreatedUpdatedByData() {
		StudentStatus obj = new StudentStatus();
		obj.setCode("DC");
		obj.setDescription("Data Correction by Schools");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(LocalDateTime.now());
		obj.setUpdateDate(LocalDateTime.now());
		StudentStatusEntity objEntity = new StudentStatusEntity();
		objEntity.setCode("DC");
		objEntity.setDescription("Data Correction by School");
		objEntity.setCreateUser("GRADUATION");
		objEntity.setCreateDate(LocalDateTime.now());
		Optional<StudentStatusEntity> ent = Optional.of(objEntity);
		Mockito.when(studentStatusRepository.findById(obj.getCode())).thenReturn(ent);
		Mockito.when(studentStatusRepository.save(objEntity)).thenReturn(objEntity);
        StudentStatus result = commonService.updateStudentStatus(obj);
        assertThat(result).isNotNull();
	}
	
	@Test(expected = GradBusinessRuleException.class)
	public void testUpdateStudentStatus_codeAlreadyExists() {
		StudentStatus obj = new StudentStatus();
		obj.setCode("DC");
		obj.setDescription("Data Correction by Schools");
		obj.setCreateUser("GRADUATION");
		obj.setUpdateUser("GRADUATION");
		obj.setCreateDate(LocalDateTime.now());
		obj.setUpdateDate(LocalDateTime.now());
		StudentStatusEntity objEntity = new StudentStatusEntity();
		objEntity.setCode("DC");
		objEntity.setDescription("Data Correction by School");
		objEntity.setCreateUser("GRADUATION");
		objEntity.setUpdateUser("GRADUATION");
		objEntity.setCreateDate(LocalDateTime.now());
		objEntity.setUpdateDate(LocalDateTime.now());
		Mockito.when(studentStatusRepository.findById(obj.getCode())).thenReturn(Optional.empty());
        StudentStatus result = commonService.updateStudentStatus(obj);
        assertThat(result).isNotNull();
	}

    @Test
    public void testDeleteStudentStatus_withGivenStudentStatus() {
        Mockito.when(graduationStatusService.getStudentStatus("DC")).thenReturn(false);
        var result = commonService.deleteStudentStatus("DC");
        assertThat(result).isEqualTo(1);
    }

	@Test(expected = GradBusinessRuleException.class)
    public void testDeleteStudentStatus_whenCodeDoesNotExist() {
        Mockito.when(graduationStatusService.getStudentStatus("DC")).thenReturn(true);
        commonService.deleteStudentStatus("DC");
    }
	
	private StudentStatusEntity createStudentStatuses() {
    	StudentStatusEntity objEntity = new StudentStatusEntity();
		objEntity.setCode("A");
		objEntity.setDescription("Active");
		objEntity.setCreateUser("GRADUATION");
		objEntity.setUpdateUser("GRADUATION");
		objEntity.setCreateDate(LocalDateTime.now());
		objEntity.setUpdateDate(LocalDateTime.now());
		return objEntity;
	}

    @Test
    public void testGetAllHistoryActivityCodeList() {
        List<HistoryActivityCodeEntity> gradHistoryActivityList = new ArrayList<>();
        HistoryActivityCodeEntity obj = new HistoryActivityCodeEntity();
        obj.setCode("DC");
        obj.setDescription("Data Correction by School");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        gradHistoryActivityList.add(obj);
        obj = new HistoryActivityCodeEntity();
        obj.setCode("CC");
        obj.setDescription("Courses not complete");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        gradHistoryActivityList.add(obj);
        Mockito.when(historyActivityRepository.findAll()).thenReturn(gradHistoryActivityList);
        List<HistoryActivity> result = commonService.getAllHistoryActivityCodeList();
        assertThat(result).isNotNull().hasSize(2);
    }

    @Test
    public void testGetSpecificHistoryActivityCode() {
        String reasonCode = "DC";
        HistoryActivity obj = new HistoryActivity();
        obj.setCode("DC");
        obj.setDescription("Data Correction by School");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        HistoryActivityCodeEntity objEntity = new HistoryActivityCodeEntity();
        objEntity.setCode("DC");
        objEntity.setDescription("Data Correction by School");
        objEntity.setCreateUser("GRADUATION");
        objEntity.setUpdateUser("GRADUATION");
        objEntity.setCreateDate(LocalDateTime.now());
        objEntity.setUpdateDate(LocalDateTime.now());
        Optional<HistoryActivityCodeEntity> ent = Optional.of(objEntity);
        Mockito.when(historyActivityRepository.findById(reasonCode)).thenReturn(ent);
        HistoryActivity  historyActivity = commonService.getSpecificHistoryActivityCode(reasonCode);
        assertThat(historyActivity).isNotNull();
    }

    @Test
    public void testGetSpecificHistoryActivityCodeReturnsNull() {
        String reasonCode = "DC";
        when(historyActivityRepository.findById(reasonCode)).thenReturn(Optional.empty());
        HistoryActivity  historyActivity = commonService.getSpecificHistoryActivityCode(reasonCode);
        assertThat(historyActivity).isNull();
    }

    @Test
    public  void testGetGradStudentAlgorithmData() {
        UUID studentID = UUID.randomUUID();
        String accessToken = "accessToken";
        String pen = "128385861";

        // Career Program
        final CareerProgram gradCareerProgram = new CareerProgram();
        gradCareerProgram.setCode("TEST");
        gradCareerProgram.setDescription("Test Code Name");

        GradSearchStudent gss = new GradSearchStudent();
        gss.setStudentID(studentID.toString());
        gss.setPen(pen);
        gss.setStudentGrade("12");

        GraduationStudentRecord gradStudentRecord = new GraduationStudentRecord();
        gradStudentRecord.setStudentID(studentID);
        gradStudentRecord.setPen(pen);
        gradStudentRecord.setStudentGrade("12");
        gradStudentRecord.setStudentCitizenship("C");

        when(this.webClient.get()).thenReturn(this.requestHeadersUriMock);
        when(this.requestHeadersUriMock.uri(String.format(constants.getCareerProgramByCodeUrl(), gradCareerProgram.getCode()))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.headers(any(Consumer.class))).thenReturn(this.requestHeadersMock);
        when(this.requestHeadersMock.retrieve()).thenReturn(this.responseMock);
        when(this.responseMock.bodyToMono(CareerProgram.class)).thenReturn(Mono.just(gradCareerProgram));

        List<StudentCareerProgramEntity> cpList = new ArrayList<>();
        StudentCareerProgramEntity spg = new StudentCareerProgramEntity();
        spg.setStudentID(studentID);
        spg.setCareerProgramCode("TEST");
        cpList.add(spg);

        when(gradStudentService.getStudentByStudentIDFromStudentAPI(studentID.toString(), accessToken)).thenReturn(gss);
        when(graduationStatusService.getGraduationStatusForAlgorithm(studentID)).thenReturn(gradStudentRecord);
        when(gradStudentCareerProgramRepository.findByStudentID(studentID)).thenReturn(cpList);

        GradStudentAlgorithmData data =  commonService.getGradStudentAlgorithmData(studentID.toString(),accessToken);
        assertThat(data).isNotNull();
        assertThat(data.getGraduationStudentRecord().getStudentCitizenship()).isNotNull();

        gradStudentRecord.setStudentCitizenship(null);

        data =  commonService.getGradStudentAlgorithmData(studentID.toString(),accessToken);
        assertThat(data).isNotNull();
        assertThat(data.getGraduationStudentRecord().getStudentCitizenship()).isNull();
    }

    @Test
    public void testGetDeceasedStudentIDs() {
        UUID studentID1 = UUID.randomUUID();
        UUID studentID2 = UUID.randomUUID();

        when(gradStudentService.getStudentIDsByStatusCode(Arrays.asList(studentID1, studentID2), "DEC")).thenReturn(Arrays.asList(studentID1, studentID2));
        List<UUID> results = commonService.getDeceasedStudentIDs(Arrays.asList(studentID1, studentID2));

        assertThat(results).isNotEmpty();
    }
}
