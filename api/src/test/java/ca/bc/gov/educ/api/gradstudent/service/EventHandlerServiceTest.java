package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventType;
import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CourseAllowableCreditRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CourseCharacteristicsRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CourseCodeRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.CourseStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.CourseStudentDetail;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.DemographicStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.OptionalProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseExamEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCourseRepository;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import ca.bc.gov.educ.api.gradstudent.service.event.EventHandlerService;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.constant.EventType.ASSESSMENT_STUDENT_UPDATE;
import static ca.bc.gov.educ.api.gradstudent.constant.Topics.GRAD_STUDENT_API_TOPIC;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@Slf4j
class EventHandlerServiceTest extends BaseIntegrationTest {
    @Autowired
    EventHandlerService eventHandlerService;
    @Autowired
    RestUtils restUtils;
    @MockBean
    @Qualifier("studentApiClient")
    WebClient webClient;
    @Autowired
    GraduationStudentRecordRepository graduationStudentRecordRepository;
    @Autowired
    StudentCourseRepository studentCourseRepository;
    @Autowired
    GradStatusEventRepository gradStatusEventRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        CoregCoursesRecord coursesRecord = new CoregCoursesRecord();
        coursesRecord.setStartDate(LocalDateTime.of(1983, 2, 1, 0, 0).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
        coursesRecord.setCompletionEndDate(LocalDate.of(9999, 5, 1).format(DateTimeFormatter.ISO_LOCAL_DATE));
        coursesRecord.setCourseID("101");
        Set<CourseCodeRecord> courseCodes = new HashSet<>();
        CourseCodeRecord traxCode = new CourseCodeRecord();
        traxCode.setCourseID("856787");
        traxCode.setExternalCode("PH 11");
        traxCode.setOriginatingSystem("39"); // TRAX
        courseCodes.add(traxCode);
        CourseCodeRecord myEdBCCode = new CourseCodeRecord();
        myEdBCCode.setCourseID("856787");
        myEdBCCode.setExternalCode("MPH--11");
        myEdBCCode.setOriginatingSystem("38"); // MyEdBC
        courseCodes.add(myEdBCCode);
        coursesRecord.setCourseCode(courseCodes);
        Set<CourseAllowableCreditRecord> courseAllowableCredits = new HashSet<>();
        CourseAllowableCreditRecord courseAllowableCreditRecord = new CourseAllowableCreditRecord();
        courseAllowableCreditRecord.setCourseID("856787");
        courseAllowableCreditRecord.setCreditValue("3");
        courseAllowableCreditRecord.setCacID("2145166");
        courseAllowableCreditRecord.setStartDate("1970-01-01 00:00:00");
        courseAllowableCreditRecord.setEndDate(null);
        courseAllowableCredits.add(courseAllowableCreditRecord);
        coursesRecord.setCourseAllowableCredit(courseAllowableCredits);
        CourseCharacteristicsRecord courseCategory = new CourseCharacteristicsRecord();
        courseCategory.setId("2932");
        courseCategory.setType("CC");
        courseCategory.setCode("BA");
        courseCategory.setDescription("");
        coursesRecord.setCourseCategory(courseCategory);
        coursesRecord.setGenericCourseType("G");
        when(restUtils.getCoursesByExternalID(any(), any())).thenReturn(coursesRecord);

        when(restUtils.getOptionalProgramCodeList()).thenReturn(
                List.of(
                        new OptionalProgramCode(UUID.randomUUID(), "FR", "SCCP French Certificate", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new OptionalProgramCode(UUID.randomUUID(), "AD", "Advanced Placement", "", 2, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                        new OptionalProgramCode(UUID.randomUUID(), "DD", "Dual Dogwood", "", 3, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
                )
        );

        studentCourseRepository.deleteAll();
        graduationStudentRecordRepository.deleteAll();
        gradStatusEventRepository.deleteAll();
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_DEM_DATA__whenNoStudentExist_shouldCreateStudentWithEventOutcome_DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var demStudent = createMockDemographicStudent("N", "REGULAR");
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(createmockStudent());
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_DEM_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(demStudent))
                .build();
        var response = eventHandlerService.handleProcessStudentDemDataEvent(event);
        assertThat(response.getLeft()).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response.getLeft());
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_DEM_DATA__whenNoStudentExist_shouldCreateStudentAndOptionalProgsWithEventOutcome_DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var demStudent = createMockDemographicStudent("N", "CSF");
        demStudent.setGradRequirementYear("SSCP");
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(createmockStudent());
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_DEM_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(demStudent))
                .build();
        var response = eventHandlerService.handleProcessStudentDemDataEvent(event);
        assertThat(response.getLeft()).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response.getLeft());
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_DEM_DATA__whenNoStudentExistAndIsSummer_shouldCreateStudentWithEventOutcome_DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var demStudent = createMockDemographicStudent("Y", "CSF");
        var studentFromApi = createmockStudent();
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentFromApi);
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_DEM_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(demStudent))
                .build();
        var response = eventHandlerService.handleProcessStudentDemDataEvent(event);
        assertThat(response.getLeft()).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response.getLeft());
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);

        var student = graduationStudentRecordRepository.findOptionalByStudentID(UUID.fromString(studentFromApi.getStudentID()));
        assertThat(student).isPresent();
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_DEM_DATA__whenStudentExists_shouldUpdateStudentWithEventOutcome_DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var demStudent = createMockDemographicStudent("Y", "CSF");
        var studentFromApi = createmockStudent();
        graduationStudentRecordRepository.save(createMockGraduationStudentRecordEntity(UUID.fromString(studentFromApi.getStudentID()), UUID.fromString(demStudent.getSchoolID())));
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentFromApi);
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_DEM_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(demStudent))
                .build();
        var response = eventHandlerService.handleProcessStudentDemDataEvent(event);
        assertThat(response.getLeft()).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response.getLeft());
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_COURSE_DATA__whenCourseDoesNotExists_shouldCreateCourseWithEventOutcome_COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var course = createMockCourseStudent("N", "APPEND");
        var studentFromApi = createmockStudent();
        graduationStudentRecordRepository.save(createMockGraduationStudentRecordEntity(UUID.fromString(studentFromApi.getStudentID()), UUID.randomUUID()));
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentFromApi);
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_COURSE_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(course))
                .build();
        var response = eventHandlerService.handleProcessStudentCourseDataEvent(event);
        assertThat(response).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_COURSE_DATA__whenCourseDoesNotExists_shouldCreateCourseAndOptionalProgWithEventOutcome_COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var course = createMockCourseStudent("N", "APPEND");
        course.getStudentDetails().get(0).setCourseCode("FRAL");
        course.getStudentDetails().get(0).setCourseLevel("10");
        var studentFromApi = createmockStudent();
        var studentGradRecord = createMockGraduationStudentRecordEntity(UUID.fromString(studentFromApi.getStudentID()), UUID.randomUUID());
        studentGradRecord.setProgram("2023-EN");
        graduationStudentRecordRepository.save(studentGradRecord);
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentFromApi);
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_COURSE_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(course))
                .build();
        var response = eventHandlerService.handleProcessStudentCourseDataEvent(event);
        assertThat(response).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_COURSE_DATA__whenExists_shouldCreateCourseWithEventOutcome_COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var course = createMockCourseStudent("Y", "APPEND");
        var studentFromApi = createmockStudent();

        graduationStudentRecordRepository.save(createMockGraduationStudentRecordEntity(UUID.fromString(studentFromApi.getStudentID()), UUID.randomUUID()));
        studentCourseRepository.save(createStudentCourseEntity(UUID.fromString(studentFromApi.getStudentID()),"101","202401"));
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentFromApi);
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_COURSE_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(course))
                .build();
        var response = eventHandlerService.handleProcessStudentCourseDataEvent(event);
        assertThat(response).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_COURSE_DATA__whenExistsAndCourseStatus_W_shouldDeleteCpurseWithEventOutcome_COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var course = createMockCourseStudent("Y", "APPEND");
        course.getStudentDetails().get(0).setCourseStatus("W");
        var studentFromApi = createmockStudent();

        graduationStudentRecordRepository.save(createMockGraduationStudentRecordEntity(UUID.fromString(studentFromApi.getStudentID()), UUID.randomUUID()));
        studentCourseRepository.save(createStudentCourseEntity(UUID.fromString(studentFromApi.getStudentID()),"101","202401"));
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentFromApi);
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_COURSE_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(course))
                .build();
        var response = eventHandlerService.handleProcessStudentCourseDataEvent(event);
        assertThat(response).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
    }

    @Test
    void testHandleEvent_givenEventTypeASSESSMENT_STUDENT_UPDATE_shouldUpdateGradFlags() throws IOException {
        var sagaId = UUID.randomUUID();
        var studentFromApi = createmockStudent();
        GradStatusEvent event = GradStatusEvent
                .builder()
                .eventType(String.valueOf(ASSESSMENT_STUDENT_UPDATE))
                .sagaId(sagaId)
                .eventPayload(JsonUtil.getJsonStringFromObject(studentFromApi.getStudentID()))
                .eventOutcome("ASSESSMENT_STUDENT_UPDATED")
                .eventStatus("DB_COMMITTED")
                .build();
        gradStatusEventRepository.save(event);
        var demStudent = createMockDemographicStudent("Y", "CSF");

        var gradStudent = createMockGraduationStudentRecordEntity(UUID.fromString(studentFromApi.getStudentID()), UUID.fromString(demStudent.getSchoolID()));
        gradStudent.setRecalculateGradStatus("N");
        gradStudent.setRecalculateProjectedGrad("N");
        graduationStudentRecordRepository.save(gradStudent);
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentFromApi);

        eventHandlerService.handleAssessmentUpdatedDataEvent(event);
        var student = graduationStudentRecordRepository.findOptionalByStudentID(UUID.fromString(studentFromApi.getStudentID()));
        assertThat(student).isPresent();
        assertThat(student.get().getRecalculateGradStatus()).isEqualTo("Y");
        assertThat(student.get().getRecalculateProjectedGrad()).isEqualTo("Y");
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_COURSE_DATA__whenReportingModeIsREPLACE_shouldReplaceExistingCourseWithEventOutcome_COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var course = createMockCourseStudent("N", "REPLACE");
        var studentFromApi = createmockStudent();
        graduationStudentRecordRepository.save(createMockGraduationStudentRecordEntity(UUID.fromString(studentFromApi.getStudentID()), UUID.randomUUID()));
        var mockCourse = createStudentCourseEntity(UUID.fromString(studentFromApi.getStudentID()),"101","202401");
        mockCourse.setCourseExam(null);
        studentCourseRepository.save(mockCourse);
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentFromApi);
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_COURSE_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(course))
                .build();
        var response = eventHandlerService.handleProcessStudentCourseDataEvent(event);
        assertThat(response).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
    }

    @Test
    void testHandleEvent_givenEventTypePROCESS_STUDENT_COURSE_DATA__whenReportingModeIsREPLACE_shouldNotDeleteExaminableCourseWithEventOutcome_COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API() throws IOException {
        var course = createMockCourseStudent("N", "REPLACE");
        var studentFromApi = createmockStudent();
        graduationStudentRecordRepository.save(createMockGraduationStudentRecordEntity(UUID.fromString(studentFromApi.getStudentID()), UUID.randomUUID()));
        var mockCourse = createStudentCourseEntity(UUID.fromString(studentFromApi.getStudentID()),"101","202401");
        mockCourse.setCourseExam(StudentCourseExamEntity.builder().build());
        studentCourseRepository.save(mockCourse);
        when(restUtils.getStudentByPEN(any(), any())).thenReturn(studentFromApi);
        var sagaId = UUID.randomUUID();
        final Event event = Event
                .builder()
                .eventType(EventType.PROCESS_STUDENT_COURSE_DATA)
                .sagaId(sagaId)
                .replyTo(String.valueOf(GRAD_STUDENT_API_TOPIC))
                .eventPayload(JsonUtil.getJsonStringFromObject(course))
                .build();
        var response = eventHandlerService.handleProcessStudentCourseDataEvent(event);
        assertThat(response).isNotEmpty();
        Event responseEvent = JsonUtil.getObjectFromJsonBytes(Event.class, response);
        assertThat(responseEvent).isNotNull();
        assertThat(responseEvent.getEventOutcome()).isEqualTo(EventOutcome.COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);

        var examinableCourse = studentCourseRepository.findAll();
        assertThat(examinableCourse.size()).isEqualTo(2);
    }


    private StudentCourseEntity createStudentCourseEntity(UUID studentID, String courseId, String courseSession) {
        StudentCourseEntity studentCourseEntity = new StudentCourseEntity();
        studentCourseEntity.setStudentID(studentID);
        studentCourseEntity.setCourseID(new BigInteger(courseId));
        studentCourseEntity.setCourseSession(courseSession);
        return studentCourseEntity;
    }

    private CourseStudent createMockCourseStudent(String isSummerCollection, String submissionMode) {
       var courseDetail = CourseStudentDetail.builder()
                .pen("123456789")
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .createUser("ABC")
                .updateUser("ABC")
                .courseMonth("01")
                .courseYear("2024")
                .courseStatus("A")
                .courseType("E")
                .courseDescription("COMP")
                .courseGraduationRequirement("A")
                .finalLetterGrade("A")
                .finalPercentage("92")
                .numberOfCredits("3")
                .interimPercentage("70")
                .interimLetterGrade("C+")
                .courseCode("PH")
                .courseLevel("12")
                .build();

       return CourseStudent.builder().submissionModeCode(submissionMode).isSummerCollection(isSummerCollection).pen("123456789").studentDetails(List.of(courseDetail)).build();
    }

    private Student createmockStudent() {
        Student studentApiStudent = new Student();
        studentApiStudent.setStudentID(UUID.randomUUID().toString());
        studentApiStudent.setPen("123456789");
        studentApiStudent.setLocalID("8887555");
        studentApiStudent.setLegalLastName("JACKSON");
        studentApiStudent.setLegalFirstName("JIM");
        studentApiStudent.setDob("1990-01-01");
        studentApiStudent.setStatusCode("A");
        return studentApiStudent;
    }

    private GraduationStudentRecordEntity createMockGraduationStudentRecordEntity(UUID studentID, UUID schoolID) {
        final GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
        graduationStatusEntity.setStudentID(studentID);
        graduationStatusEntity.setPen("123456789");
        graduationStatusEntity.setStudentStatus("A");
        graduationStatusEntity.setStudentGrade("12");
        graduationStatusEntity.setProgram("2023-EN");
        graduationStatusEntity.setSchoolOfRecordId(schoolID);
        return graduationStatusEntity;
    }

    private DemographicStudent createMockDemographicStudent(String isSummerCollection, String schoolReportingRequirementCode) {
        return DemographicStudent.builder()
                .pen("123456789")
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .createUser("ABC")
                .updateUser("ABC")
                .grade("08")
                .birthdate("19900101")
                .citizenship("C")
                .gradRequirementYear("2023")
                .programCode1("AA")
                .programCode2("AB")
                .programCode3("AC")
                .programCode4("FR")
                .programCode5("DD")
                .studentStatus("A")
                .mincode("03636018")
                .schoolID(UUID.randomUUID().toString())
                .isSummerCollection(isSummerCollection)
                .schoolCertificateCompletionDate("20230701")
                .schoolReportingRequirementCode(schoolReportingRequirementCode)
                .build();
    }

}
