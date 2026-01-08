package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentNote;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.penservices.v1.StudentMerge;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentRecordNoteEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.StudentNoteTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentNoteRepository;
import ca.bc.gov.educ.api.gradstudent.service.event.StudentMergeEventHandlerService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
class StudentMergeEventHandlerServiceTest extends BaseIntegrationTest {

    @Autowired
    StudentMergeEventHandlerService studentMergeEventHandlerService;
    @Autowired
    GradStatusEventRepository gradStatusEventRepository;
    @Autowired
    GraduationStudentRecordRepository graduationStatusRepository;
    @Autowired
    HistoryService historyService;

    @Autowired
    StudentNoteRepository studentNoteRepository;
    @MockBean
    GradStudentService gradStudentService;
    @MockBean
    SchoolService schoolService;
    @Autowired
    StudentNoteTransformer studentNoteTransformer;
    @Autowired
    private CommonService commonService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gradStatusEventRepository.deleteAll();
    }

    @Test
    void testHandleEvent_givenEventType_CREATE_MERGE_studentID_doesNotExist_trueStudentID_doesNotExist() throws IOException {
        UUID studentID = UUID.randomUUID();
        UUID trueStudentID = UUID.randomUUID();
        var mergeStudentPayload = createStudentMergePayload(studentID, trueStudentID);
        final GradStatusEvent event = GradStatusEvent
                .builder()
                .eventType(EventType.CREATE_MERGE.name())
                .eventPayload(JsonUtil.getJsonStringFromObject(mergeStudentPayload))
                .build();
        Boolean mergeResult = studentMergeEventHandlerService.processMergeEvent(event);
        assertTrue(mergeResult);
    }

    @Test
    void testHandleEvent_givenEventType_CREATE_MERGE_studentID_doesExist_trueStudentID_doesNotExist_adoptSuccess() throws IOException {
        UUID studentID = UUID.randomUUID();
        //Create Record in Grad
        GraduationStudentRecord graduationStudentRecord = createGraduationStudentRecord(studentID);
        GraduationStudentRecordEntity graduationStudentRecordEntity = createGraduationStudentRecordEntity(graduationStudentRecord);
        graduationStatusRepository.save(graduationStudentRecordEntity);
        //Create Record in Grad
        UUID trueStudentID = UUID.randomUUID();
        GradSearchStudent gradSearchStudent = createMockGradSearchStudent();
        gradSearchStudent.setStudentID(trueStudentID.toString());
        Student request = Student.builder().studentID(trueStudentID.toString()).build();
        School school = School.builder()
                .schoolId(UUID.randomUUID().toString())
                .mincode(gradSearchStudent.getMincode())
                .schoolCategoryCode("PUB")
                .schoolReportingRequirementCode("STANDARD")
                .build();
        when(gradStudentService.getStudentByStudentIDFromStudentAPI(request.getStudentID()))
                .thenReturn(gradSearchStudent);
        when(schoolService.getSchoolByMincode(any())).thenReturn(school);
        //Merge Process
        var mergeStudentPayload = createStudentMergePayload(studentID, trueStudentID);
        final GradStatusEvent event = GradStatusEvent
                .builder()
                .eventType(EventType.CREATE_MERGE.name())
                .eventPayload(JsonUtil.getJsonStringFromObject(mergeStudentPayload))
                .build();
        Boolean mergeResult = studentMergeEventHandlerService.processMergeEvent(event);
        assertTrue(mergeResult);
        GraduationStudentRecordEntity gradStudentRecord = graduationStatusRepository.findByStudentID(studentID);
        assertEquals("MER", gradStudentRecord.getStudentStatus());
    }

    @Test
    void testHandleEvent_givenEventType_CREATE_MERGE_studentID_doesExist_trueStudentID_doesNotExist_adoptSuccess_WithNotes() throws IOException {
        UUID studentID = UUID.randomUUID();
        //Create Record in Grad
        GraduationStudentRecord graduationStudentRecord = createGraduationStudentRecord(studentID);
        GraduationStudentRecordEntity graduationStudentRecordEntity = createGraduationStudentRecordEntity(graduationStudentRecord);
        graduationStatusRepository.save(graduationStudentRecordEntity);
        //Add notes
        List<StudentRecordNoteEntity> studentNoteEntityList = createNotesEntityForStudent(studentID);
        studentNoteRepository.saveAll(studentNoteEntityList);
        List<StudentNote>  studentNotes = commonService.getAllStudentNotes(studentID);
        assertThat(studentNotes).isNotEmpty().hasSize(2);
        //Create Record in Grad
        UUID trueStudentID = UUID.randomUUID();
        GradSearchStudent gradSearchStudent = createMockGradSearchStudent();
        gradSearchStudent.setStudentID(trueStudentID.toString());
        Student request = Student.builder().studentID(trueStudentID.toString()).build();
        School school = School.builder()
                .schoolId(UUID.randomUUID().toString())
                .mincode(gradSearchStudent.getMincode())
                .schoolCategoryCode("PUB")
                .schoolReportingRequirementCode("STANDARD")
                .build();
        when(gradStudentService.getStudentByStudentIDFromStudentAPI(request.getStudentID()))
                .thenReturn(gradSearchStudent);
        when(schoolService.getSchoolByMincode(any())).thenReturn(school);
        //Merge Process
        var mergeStudentPayload = createStudentMergePayload(studentID, trueStudentID);
        final GradStatusEvent event = GradStatusEvent
                .builder()
                .eventType(EventType.CREATE_MERGE.name())
                .eventPayload(JsonUtil.getJsonStringFromObject(mergeStudentPayload))
                .build();
        Boolean mergeResult = studentMergeEventHandlerService.processMergeEvent(event);
        assertTrue(mergeResult);
        GraduationStudentRecordEntity gradStudentRecord = graduationStatusRepository.findByStudentID(studentID);
        assertEquals("MER", gradStudentRecord.getStudentStatus());
    }

    @Test
    void testHandleEvent_givenEventType_CREATE_MERGE_studentID_doesExist_trueStudentID_doesExist() throws IOException {
        UUID studentID = UUID.randomUUID();
        //Create Record in Grad
        GraduationStudentRecord graduationStudentRecord = createGraduationStudentRecord(studentID);
        GraduationStudentRecordEntity graduationStudentRecordEntity = createGraduationStudentRecordEntity(graduationStudentRecord);
        graduationStatusRepository.save(graduationStudentRecordEntity);
        //Create Record in Grad
        UUID trueStudentID = UUID.randomUUID();
        GraduationStudentRecord graduationTrueStudentRecord = createGraduationStudentRecord(trueStudentID);
        GraduationStudentRecordEntity graduationTrueStudentRecordEntity = createGraduationStudentRecordEntity(graduationTrueStudentRecord);
        graduationStatusRepository.save(graduationTrueStudentRecordEntity);
        //Merge Process
        var mergeStudentPayload = createStudentMergePayload(studentID, trueStudentID);
        final GradStatusEvent event = GradStatusEvent
                .builder()
                .eventType(EventType.CREATE_MERGE.name())
                .eventPayload(JsonUtil.getJsonStringFromObject(mergeStudentPayload))
                .build();
        Boolean mergeResult = studentMergeEventHandlerService.processMergeEvent(event);
        assertTrue(mergeResult);
    }

    @Test
    void testHandleEvent_givenEventType_DE_MERGE_studentID_doesExist_trueStudentID_doesExist() throws IOException {
        UUID studentID = UUID.randomUUID();
        //Create Record in Grad
        GraduationStudentRecord graduationStudentRecord = createGraduationStudentRecord(studentID);
        graduationStudentRecord.setStudentStatus("CUR");
        GraduationStudentRecordEntity graduationStudentRecordEntity = createGraduationStudentRecordEntity(graduationStudentRecord);
        var savedStud = graduationStatusRepository.save(graduationStudentRecordEntity);
        historyService.createStudentHistory(savedStud, "USERADD");
        savedStud.setStudentStatus("MER");
        savedStud.setUpdateDate(LocalDateTime.now().plusDays(1));
        historyService.createStudentHistory(savedStud, "USERDEMERGE");
        //Create Record in Grad
        UUID trueStudentID = UUID.randomUUID();
        GraduationStudentRecord graduationTrueStudentRecord = createGraduationStudentRecord(trueStudentID);
        GraduationStudentRecordEntity graduationTrueStudentRecordEntity = createGraduationStudentRecordEntity(graduationTrueStudentRecord);
        graduationStatusRepository.save(graduationTrueStudentRecordEntity);
        //Merge Process
        var mergeStudentPayload = createStudentMergePayload(studentID, trueStudentID);
        final GradStatusEvent event = GradStatusEvent
                .builder()
                .eventType(EventType.DELETE_MERGE.name())
                .eventPayload(JsonUtil.getJsonStringFromObject(mergeStudentPayload))
                .build();
        Boolean mergeResult = studentMergeEventHandlerService.processDeMergeEvent(event);
        assertTrue(mergeResult);
    }

    @Test
    void testHandleEvent_givenEventType_CREATE_MERGE_studentID_doesExist_trueStudentID_doesExist_WithNotes() throws IOException {
        UUID studentID = UUID.randomUUID();
        //Create Record in Grad
        GraduationStudentRecord graduationStudentRecord = createGraduationStudentRecord(studentID);
        GraduationStudentRecordEntity graduationStudentRecordEntity = createGraduationStudentRecordEntity(graduationStudentRecord);
        graduationStatusRepository.save(graduationStudentRecordEntity);
        //Add notes
        List<StudentRecordNoteEntity> studentNoteEntityList = createNotesEntityForStudent(studentID);
        studentNoteRepository.saveAll(studentNoteEntityList);
        List<StudentNote>  studentNotes = commonService.getAllStudentNotes(studentID);
        assertThat(studentNotes).isNotEmpty().hasSize(2);
        //Create Record in Grad
        UUID trueStudentID = UUID.randomUUID();
        GraduationStudentRecord graduationTrueStudentRecord = createGraduationStudentRecord(trueStudentID);
        GraduationStudentRecordEntity graduationTrueStudentRecordEntity = createGraduationStudentRecordEntity(graduationTrueStudentRecord);
        graduationStatusRepository.save(graduationTrueStudentRecordEntity);
        //Merge Process
        var mergeStudentPayload = createStudentMergePayload(studentID, trueStudentID);
        final GradStatusEvent event = GradStatusEvent
                .builder()
                .eventType(EventType.CREATE_MERGE.name())
                .eventPayload(JsonUtil.getJsonStringFromObject(mergeStudentPayload))
                .build();
        Boolean mergeResult = studentMergeEventHandlerService.processMergeEvent(event);
        assertTrue(mergeResult);
    }

    @Test
    void testHandleEvent_givenEventType_INVALID_MERGE_DIRECTION() throws IOException {
        UUID studentID = UUID.randomUUID();
        UUID trueStudentID = UUID.randomUUID();
        var mergeStudentPayload = createInvalidStudentMergePayload(studentID, trueStudentID);
        final GradStatusEvent event = GradStatusEvent
                .builder()
                .eventType(EventType.CREATE_MERGE.name())
                .eventPayload(JsonUtil.getJsonStringFromObject(mergeStudentPayload))
                .build();
        Boolean mergeResult = studentMergeEventHandlerService.processMergeEvent(event);
        assertTrue(mergeResult);
    }

    @Test
    void testHandleEvent_givenEventType_DEMERGE() throws IOException {
        UUID studentID = UUID.randomUUID();
        UUID trueStudentID = UUID.randomUUID();
        var deMergeStudentPayload = createInvalidStudentMergePayload(studentID, trueStudentID);
        final GradStatusEvent event = GradStatusEvent
                .builder()
                .eventType(EventType.DELETE_MERGE.name())
                .eventPayload(JsonUtil.getJsonStringFromObject(deMergeStudentPayload))
                .build();
        assertDoesNotThrow(() -> studentMergeEventHandlerService.processDeMergeEvent(event));
    }

    private List<StudentRecordNoteEntity> createNotesEntityForStudent(UUID studentID) {
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
        note2.setUpdateDate(LocalDateTime.now(Clock.offset(Clock.systemDefaultZone(), Duration.ofHours(3))));
        allNotesList.add(note2);
        return allNotesList;
    }

    private GraduationStudentRecord createGraduationStudentRecord(UUID studentID) {
        GraduationStudentRecord graduationStatus = new GraduationStudentRecord();
        graduationStatus.setStudentID(studentID);
        graduationStatus.setPen("123456789");
        graduationStatus.setStudentStatus("A");
        graduationStatus.setRecalculateGradStatus("Y");
        graduationStatus.setProgram("2018-EN");
        graduationStatus.setSchoolOfRecordId(null);
        graduationStatus.setSchoolAtGradId(null);
        graduationStatus.setGpa("4");
        graduationStatus.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(new Date(System.currentTimeMillis()), "yyyy/MM"));
        return graduationStatus;
    }

    private GraduationStudentRecordEntity createGraduationStudentRecordEntity(GraduationStudentRecord graduationStatus) {
        GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        BeanUtils.copyProperties(graduationStatus, graduationStatusEntity);
        graduationStatusEntity.setProgramCompletionDate(new Date(System.currentTimeMillis()));
        return graduationStatusEntity;
    }

    private List<StudentMerge> createStudentMergePayload(UUID studentID, UUID trueStudentID) {
        final List<StudentMerge> studentMerges = new ArrayList<>();
        final StudentMerge merge = new StudentMerge();
        merge.setStudentID(studentID.toString());
        merge.setMergeStudentID(trueStudentID.toString());
        merge.setStudentMergeDirectionCode("TO");
        merge.setStudentMergeSourceCode("MI");
        studentMerges.add(merge);
        return studentMerges;
    }

    private List<StudentMerge> createInvalidStudentMergePayload(UUID studentID, UUID trueStudentID) {
        final List<StudentMerge> studentMerges = new ArrayList<>();
        final StudentMerge merge = new StudentMerge();
        merge.setStudentID(studentID.toString());
        merge.setMergeStudentID(trueStudentID.toString());
        merge.setStudentMergeDirectionCode("XYZ");
        merge.setStudentMergeSourceCode("MI");
        studentMerges.add(merge);
        return studentMerges;
    }

}
