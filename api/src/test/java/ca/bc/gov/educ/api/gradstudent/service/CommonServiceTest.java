package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.dto.GradCareerProgram;
import ca.bc.gov.educ.api.gradstudent.dto.StudentNote;
import ca.bc.gov.educ.api.gradstudent.entity.StudentCareerProgramEntity;
import ca.bc.gov.educ.api.gradstudent.entity.StudentRecordNoteEntity;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCareerProgramRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentNoteRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.sql.Date;
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
public class CommonServiceTest {

    @Autowired
    EducGradStudentApiConstants constants;

    @Autowired
    private CommonService commonService;

    @MockBean
    private StudentCareerProgramRepository gradStudentCareerProgramRepository;

    @MockBean
    private StudentNoteRepository studentNoteRepository;

    @MockBean
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersMock;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriMock;
    @Mock
    private WebClient.RequestBodySpec requestBodyMock;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriMock;
    @Mock
    private WebClient.ResponseSpec responseMock;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetStudentCareerProgram() {
        // UUID
        final UUID studentID = UUID.randomUUID();
        final String pen = "123456789";
        // Career Program
        final GradCareerProgram gradCareerProgram = new GradCareerProgram();
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
        final GradCareerProgram gradCareerProgram = new GradCareerProgram();
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
        when(this.responseMock.bodyToMono(GradCareerProgram.class)).thenReturn(Mono.just(gradCareerProgram));

        var result = commonService.getAllGradStudentCareerProgramList(studentID.toString(), "accessToken");

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
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
        final String pen = "123456789";

        final List<StudentRecordNoteEntity> allNotesList = new ArrayList<>();

        final StudentRecordNoteEntity note1 = new StudentRecordNoteEntity();
        note1.setId(UUID.randomUUID());
        note1.setStudentID(studentID);
        note1.setNote("Test1 Comments");
        note1.setUpdateDate(new Date(System.currentTimeMillis()));
        allNotesList.add(note1);

        final StudentRecordNoteEntity note2 = new StudentRecordNoteEntity();
        note2.setId(UUID.randomUUID());
        note2.setStudentID(studentID);
        note2.setNote("Test2 Comments");
        note2.setUpdateDate(new Date(System.currentTimeMillis() + 100000L));
        allNotesList.add(note2);

        when(studentNoteRepository.findByStudentID(studentID)).thenReturn(allNotesList);

        var result = commonService.getAllStudentNotes(studentID);

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(0).getStudentID()).isEqualTo(studentID.toString());
        assertThat(result.get(1).getStudentID()).isEqualTo(studentID.toString());
        assertThat(result.get(0).getNote()).isEqualTo(note2.getNote());
        assertThat(result.get(1).getNote()).isEqualTo(note1.getNote());
    }

    @Test
    public void testSaveStudentNote_thenReturnCreateSuccess() {
        // ID
        final UUID studentID = UUID.randomUUID();
        final String pen = "123456789";

        final StudentNote studentNote = new StudentNote();
        studentNote.setStudentID(studentID.toString());
        studentNote.setNote("Test Note Body");

        final StudentRecordNoteEntity studentNoteEntity = new StudentRecordNoteEntity();
        studentNoteEntity.setStudentID(studentID);
        studentNoteEntity.setNote("Test Note Body");

        final Optional<StudentRecordNoteEntity> optional = Optional.of(studentNoteEntity);

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
        final String pen = "123456789";

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
        final String pen = "123456789";

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

        assertThat(result).isEqualTo(0);

    }
}
