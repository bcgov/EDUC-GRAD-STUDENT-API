package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.dto.Student;
import ca.bc.gov.educ.api.gradstudent.dto.StudentSearch;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import java.util.Arrays;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class GradStudentControllerTest {

    @Mock
    private GradStudentService gradStudentService;

    @InjectMocks
    private GradStudentController gradStudentController;

    @Test
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

        Mockito.when(gradStudentService.getStudentFromStudentAPI(
                null, lastName, null, null, null, null, null, mincode, null, null,
                null, 1, 5, null)).thenReturn(studentSearch);
        gradStudentController.getGradStudentFromStudentAPI(null, lastName, null, null, null, null, null, mincode, null, null,
                null, 1, 5);
        Mockito.verify(gradStudentService).getStudentFromStudentAPI(null, lastName, null, null, null, null, null, mincode, null, null,
                null, 1, 5, null);

    }

    @Test
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
        gradStudentController.getGradStudentByPenFromStudentAPI(pen);
        Mockito.verify(gradStudentService).getStudentByPenFromStudentAPI(pen, null);
    }

}
