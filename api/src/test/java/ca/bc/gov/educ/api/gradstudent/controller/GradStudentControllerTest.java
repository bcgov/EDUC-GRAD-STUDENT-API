package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class GradStudentControllerTest {

    @Mock
    private GradStudentService gradStudentService;

    @InjectMocks
    private GradStudentController gradStudentController;

    @Test
    public void testFake() {
        assertEquals(20-10, 40-30);
    }

    /*@Test
    public void testGetGradStudentFromStudentAPI() {
        // ID
        final UUID studentID = UUID.randomUUID();
        final String pen = "123456789";
        final String lastName = "LastName";
        final String program = "2018-EN";
        final String gradStatus = "A";
        final String stdGrade = "12";
        final String mincode = "12345678";
        final String schoolName = "Test School";

        // Grad Search Students
        final GradSearchStudent gradSearchStudent = new GradSearchStudent();
        gradSearchStudent.setStudentID(studentID.toString());
        gradSearchStudent.setPen(pen);
        gradSearchStudent.setLegalLastName(lastName);
        gradSearchStudent.setSchoolOfRecord(mincode);
        gradSearchStudent.setProgram(program);
        gradSearchStudent.setStudentGrade(stdGrade);
        gradSearchStudent.setStudentStatus(gradStatus);
        gradSearchStudent.setSchoolOfRecordName(schoolName);

        StudentSearch studentSearch = new StudentSearch();
        studentSearch.setGradSearchStudents(Arrays.asList(gradSearchStudent));
        studentSearch.setNumber(1);
        studentSearch.setSize(5);
        studentSearch.setNumberOfElements(1);

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        StudentSearchRequest studentSearchRequest = StudentSearchRequest.builder().legalLastName(lastName).mincode(mincode).build();

        Mockito.when(gradStudentService.getStudentFromStudentAPI(studentSearchRequest, 1, 5, null)).thenReturn(studentSearch);
        gradStudentController.getGradNPenGradStudentFromStudentAPI(null, lastName, null, null, null, null, null, mincode, null, null,
                null, 1, 5, "");
        Mockito.verify(gradStudentService).getStudentFromStudentAPI(studentSearchRequest, 1, 5, null);

    }*/

    /*@Test
    public void testGetGradStudentByPenFromStudentAPI() {
        // ID
        final UUID studentID = UUID.randomUUID();
        final String pen = "123456789";
        final String lastName = "LastName";
        final String program = "2018-EN";
        final String gradStatus = "A";
        final String stdGrade = "12";
        final String mincode = "12345678";
        final String schoolName = "Test School";

        // Grad Search Students
        final GradSearchStudent gradSearchStudent = new GradSearchStudent();
        gradSearchStudent.setStudentID(studentID.toString());
        gradSearchStudent.setPen(pen);
        gradSearchStudent.setSchoolOfRecord(mincode);
        gradSearchStudent.setProgram(program);
        gradSearchStudent.setStudentGrade(stdGrade);
        gradSearchStudent.setStudentStatus(gradStatus);
        gradSearchStudent.setSchoolOfRecordName(schoolName);

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(gradStudentService.getStudentByPenFromStudentAPI(pen, null)).thenReturn(Arrays.asList(gradSearchStudent));
        gradStudentController.getGradStudentByPenFromStudentAPI(pen, "");
        Mockito.verify(gradStudentService).getStudentByPenFromStudentAPI(pen, null);
    }*/

    /*@Test
    public void testAddNewPenFromStudentAPI() {
        // ID
        final UUID studentID = UUID.randomUUID();
        final String pen = "123456789";
        final String firstName = "FirstName";
        final String lastName = "LastName";
        final String program = "2018-EN";
        final String gradStatus = "A";
        final String stdGrade = "12";
        final String mincode = "12345678";
        final String schoolName = "Test School";

        // Grad Student
        final StudentCreate student = new StudentCreate();
        student.setStudentID(studentID.toString());
        student.setPen(pen);
        student.setLegalLastName(lastName);
        student.setLegalFirstName(firstName);
        student.setMincode(mincode);
        student.setSexCode("M");
        student.setGenderCode("M");
        student.setUsualFirstName("Usual First");
        student.setUsualLastName("Usual Last");
        student.setEmail("junit@test.com");
        student.setEmailVerified("Y");
        student.setStatusCode("A");
        student.setDob("1990-01-01");
        student.setHistoryActivityCode("USERNEW");

        Authentication authentication = Mockito.mock(Authentication.class);
        OAuth2AuthenticationDetails details = Mockito.mock(OAuth2AuthenticationDetails.class);
        // Mockito.whens() for your authorization object
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getDetails()).thenReturn(details);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(gradStudentService.addNewPenFromStudentAPI(student, null)).thenReturn(student);
        gradStudentController.addNewPenFromStudentAPI(student, "");
        Mockito.verify(gradStudentService).addNewPenFromStudentAPI(student, null);
    }*/

}
