package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.repository.ReportGradStudentDataRepository;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.*;

import static ca.bc.gov.educ.api.gradstudent.model.dto.Condition.AND;
import static ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@ActiveProfiles("integration-test")
@SpringBootTest
public class GradStudentControllerTest {

    @Mock
    private GradStudentService gradStudentService;

    @InjectMocks
    private GradStudentController gradStudentController;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ReportGradStudentDataRepository reportGradStudentDataRepository;

    @Test
    public void testFake() {
        assertEquals(20-10, 40-30);
    }

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


        StudentSearchRequest studentSearchRequest = StudentSearchRequest.builder().legalLastName(lastName).mincode(mincode).build();

        Mockito.when(gradStudentService.getStudentFromStudentAPI(studentSearchRequest, 1, 5, null)).thenReturn(studentSearch);
        gradStudentController.getGradNPenGradStudentFromStudentAPI(null, lastName, null, null, null, null, null, mincode, null, null,
                null, 1, 5, null);
        Mockito.verify(gradStudentService).getStudentFromStudentAPI(studentSearchRequest, 1, 5, null);

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


        Mockito.when(gradStudentService.getStudentByPenFromStudentAPI(pen, "accessToken")).thenReturn(Arrays.asList(gradSearchStudent));
        gradStudentController.getGradStudentByPenFromStudentAPI(pen, "accessToken");
        Mockito.verify(gradStudentService).getStudentByPenFromStudentAPI(pen, "accessToken");
    }

    @Test
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


        Mockito.when(gradStudentService.addNewPenFromStudentAPI(student, "accessToken")).thenReturn(student);
        gradStudentController.addNewPenFromStudentAPI(student, "accessToken");
        Mockito.verify(gradStudentService).addNewPenFromStudentAPI(student, "accessToken");
    }

    @Test
    public void testSearchGraduationStudentRecords() {
        StudentSearchRequest searchRequest = StudentSearchRequest.builder().schoolIds(List.of(UUID.randomUUID())).build();
        Mockito.when(gradStudentService.getStudentIDsBySearchCriteriaOrAll(searchRequest)).thenReturn(List.of(UUID.randomUUID()));
        gradStudentController.searchGraduationStudentRecords(searchRequest);
        Mockito.verify(gradStudentService).getStudentIDsBySearchCriteriaOrAll(searchRequest);
    }

    @Test
    public void testReadGradStudentPaginated_Always_ShouldReturnStatusOk() throws Exception {
        var schoolID = UUID.randomUUID();
//        var incomingFileset = incomingFilesetRepository.save(createMockIncomingFilesetEntityWithAllFilesLoaded());

        ReportGradStudentDataEntity entity = new ReportGradStudentDataEntity();
        entity.setGraduationStudentRecordId(UUID.randomUUID());
        entity.setSchoolOfRecordId(schoolID);
        reportGradStudentDataRepository.save(entity);
        final SearchCriteria criteria = SearchCriteria.builder().condition(AND).key("schoolOfRecordId").operation(FilterOperation.EQUAL).value(schoolID.toString()).valueType(ValueType.UUID).build();

        final List<SearchCriteria> criteriaList = new ArrayList<>();
        criteriaList.add(criteria);

        final List<Search> searches = new LinkedList<>();
        searches.add(Search.builder().searchCriteriaList(criteriaList).build());

        final var objectMapper = new ObjectMapper();
        final String criteriaJSON = objectMapper.writeValueAsString(searches);
        final MvcResult result = this.mockMvc
                .perform(get(GRAD_STUDENT_API_ROOT_MAPPING + EducGradStudentApiConstants.GRAD_STUDENT_PAGINATION)
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_GRAD_GRADUATION_STATUS")))
                        .param("searchCriteriaList", criteriaJSON)
                        .contentType(APPLICATION_JSON))
                .andReturn();
        this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    public void testGetGraduationCountsBySchools_WithNoSchoolIds_ShouldReject() throws Exception {

        mockMvc.perform(get(GRAD_STUDENT_API_ROOT_MAPPING + EducGradStudentApiConstants.GRADUATION_COUNTS)
                        .with(jwt().jwt(jwt -> jwt.claim("scope", "READ_GRAD_GRADUATION_STATUS")))
                        .contentType(APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

}
