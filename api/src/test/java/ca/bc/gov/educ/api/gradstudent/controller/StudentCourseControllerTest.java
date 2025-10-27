package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseActivityType;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;

import ca.bc.gov.educ.api.gradstudent.service.StudentCourseService;
import ca.bc.gov.educ.api.gradstudent.util.ResponseHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.util.Pair;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class StudentCourseControllerTest {

    @InjectMocks
    StudentCourseController  studentCourseController;

    @Mock
    StudentCourseService studentCourseService;

    @Mock
    ResponseHelper responseHelper;

    @Test
    public void testGetStudentCourses() {
        UUID studentID = UUID.randomUUID();
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setCourseID("1");
        studentCourse.setCourseSession("202404");
        ResponseEntity<List<StudentCourse>> expectedResponse = ResponseEntity.ok(Arrays.asList(studentCourse));
        when(responseHelper.GET(Arrays.asList(studentCourse))).thenReturn(expectedResponse);

        when(studentCourseService.getStudentCourses(studentID)).thenReturn(Arrays.asList(studentCourse));
        ResponseEntity<List<StudentCourse>> actual = studentCourseController.getStudentCourses(studentID);
        assertThat(actual).isEqualTo(expectedResponse);
        verify(studentCourseService).getStudentCourses(studentID);
    }

    @Test
    public void testGetStudentCourseHistory() {
        UUID studentID = UUID.randomUUID();
        StudentCourseHistory studentCourseHistory = new StudentCourseHistory();
        studentCourseHistory.setCourseID("1");
        studentCourseHistory.setCourseSession("202404");
        studentCourseHistory.setActivityCode(StudentCourseActivityType.USERCOURSEADD.name());
        ResponseEntity<List<StudentCourseHistory>> expectedResponse = ResponseEntity.ok(Arrays.asList(studentCourseHistory));
        when(responseHelper.GET(Arrays.asList(studentCourseHistory))).thenReturn(expectedResponse);

        when(studentCourseService.getStudentCourseHistory(studentID)).thenReturn(Arrays.asList(studentCourseHistory));
        ResponseEntity<List<StudentCourseHistory>> actual = studentCourseController.getStudentCourseHistory(studentID);
        assertThat(actual).isEqualTo(expectedResponse);
        verify(studentCourseService).getStudentCourseHistory(studentID);
    }

    @Test
    public void testCreateStudentCourses() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setCourseID("1");
        studentCourse.setCourseSession("202404");
        StudentCourseValidationIssue validationIssue = new StudentCourseValidationIssue();
        validationIssue.setHasPersisted(true);
        validationIssue.setCourseID("1");
        validationIssue.setCourseSession("202404");
        ResponseEntity<List<StudentCourseValidationIssue>> expectedResponse = ResponseEntity.ok(Arrays.asList(validationIssue));
        when(responseHelper.GET(Arrays.asList(validationIssue))).thenReturn(expectedResponse);

        when(studentCourseService.saveStudentCourses(studentID, List.of(studentCourse), false)).thenReturn(Pair.of(Arrays.asList(validationIssue),null));
        ResponseEntity<List<StudentCourseValidationIssue>> actual = studentCourseController.createStudentCourses(studentID, List.of(studentCourse));
        assertThat(actual).isEqualTo(expectedResponse);
        verify(studentCourseService).saveStudentCourses(studentID, List.of(studentCourse), false);
    }

    @Test
    public void testUpdateStudentCourses() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        StudentCourse studentCourse = new StudentCourse();
        studentCourse.setId(UUID.randomUUID().toString());
        studentCourse.setCourseID("1");
        studentCourse.setCourseSession("202404");
        StudentCourseValidationIssue validationIssue = new StudentCourseValidationIssue();
        validationIssue.setHasPersisted(true);
        validationIssue.setCourseID("1");
        validationIssue.setCourseSession("202404");
        ResponseEntity<StudentCourseValidationIssue> expectedResponse = ResponseEntity.ok(validationIssue);
        when(responseHelper.GET(validationIssue)).thenReturn(expectedResponse);
        when(studentCourseService.saveStudentCourses(studentID, List.of(studentCourse), true)).thenReturn(Pair.of(Arrays.asList(validationIssue),null));
        ResponseEntity<StudentCourseValidationIssue> actual = studentCourseController.updateStudentCourses(studentID, studentCourse);
        assertThat(actual).isEqualTo(expectedResponse);
        verify(studentCourseService).saveStudentCourses(studentID, List.of(studentCourse), true);
    }

    @Test
    public void testDeleteStudentCourses() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        UUID courseID = UUID.randomUUID();
        StudentCourseValidationIssue validationIssue = new StudentCourseValidationIssue();
        validationIssue.setHasPersisted(true);
        validationIssue.setCourseID("1");
        validationIssue.setCourseSession("202404");
        ResponseEntity<List<StudentCourseValidationIssue>> expectedResponse = ResponseEntity.ok(Arrays.asList(validationIssue));
        when(responseHelper.GET(Arrays.asList(validationIssue))).thenReturn(expectedResponse);

        when(studentCourseService.deleteStudentCourses(studentID, List.of(courseID))).thenReturn(Pair.of(Arrays.asList(validationIssue), null));
        ResponseEntity<List<StudentCourseValidationIssue>> actual = studentCourseController.deleteStudentCourses(studentID, List.of(courseID));
        assertThat(actual).isEqualTo(expectedResponse);
        verify(studentCourseService).deleteStudentCourses(studentID, List.of(courseID));
    }

    @Test
    public void testTransferStudentCourses_withValidationIssues_returnsOk() throws JsonProcessingException {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        StudentCoursesTransferReq request = new StudentCoursesTransferReq();
        request.setSourceStudentId(sourceId);
        request.setTargetStudentId(targetId);

        ValidationIssue issue = ValidationIssue.builder()
            .validationFieldName("studentCourseId")
            .validationIssueMessage("Course already exists")
            .validationIssueSeverityCode("ERROR")
            .build();

        List<ValidationIssue> issues = List.of(issue);
        ResponseEntity<List<ValidationIssue>> expectedResponse = ResponseEntity.ok(issues);

        when(studentCourseService.transferStudentCourse(request)).thenReturn(Pair.of(issues, null));

        ResponseEntity<List<ValidationIssue>> actual = studentCourseController.transferStudentCourses(request);

        assertThat(actual).isEqualTo(expectedResponse);
        verify(studentCourseService).transferStudentCourse(request);
    }

    @Test
    public void testTransferStudentCourses_noIssues_returnsNoContent() throws JsonProcessingException {
        UUID sourceId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        StudentCoursesTransferReq request = new StudentCoursesTransferReq();
        request.setSourceStudentId(sourceId);
        request.setTargetStudentId(targetId);

        when(studentCourseService.transferStudentCourse(request)).thenReturn(Pair.of(Collections.emptyList(), null));

        ResponseEntity<List<ValidationIssue>> actual = studentCourseController.transferStudentCourses(request);

        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(actual.getBody()).isNull();
        verify(studentCourseService).transferStudentCourse(request);
    }
}
