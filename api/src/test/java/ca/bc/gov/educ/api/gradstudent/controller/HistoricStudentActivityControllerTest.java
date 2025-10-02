package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.entity.HistoricStudentActivityEntity;
import ca.bc.gov.educ.api.gradstudent.repository.HistoricStudentActivityRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class HistoricStudentActivityControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private HistoricStudentActivityRepository historicStudentActivityRepository;

    private UUID testStudentId;
    private HistoricStudentActivityEntity testEntity;

    @BeforeEach
    public void setUp() {
        testEntity = createHistoricStudentActivityEntity();
        testEntity = historicStudentActivityRepository.save(testEntity);
        testStudentId = testEntity.getGraduationStudentRecordID();
    }

    @AfterEach
    public void tearDown() {
        historicStudentActivityRepository.deleteAll();
    }

    @Test
    void testGetHistoricStudentActivities_withValidStudentId_shouldReturnActivities() throws Exception {
        // Given
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        // When & Then
        this.mockMvc.perform(get(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING + 
                EducGradStudentApiConstants.HISTORIC_STUDENT_ACTIVITY_MAPPING.replace("{studentID}", testStudentId.toString()))
                .with(mockAuthority))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].historicStudentActivityId").value(testEntity.getHistoricStudentActivityID().toString()))
                .andExpect(jsonPath("$[0].graduationStudentRecordId").value(testEntity.getGraduationStudentRecordID().toString()))
                .andExpect(jsonPath("$[0].type").value("ADD"))
                .andExpect(jsonPath("$[0].program").value("2023"))
                .andExpect(jsonPath("$[0].userId").value("USER123"));
    }

    @Test
    void testGetHistoricStudentActivities_withNoActivities_shouldReturnEmptyList() throws Exception {
        // Given
        UUID studentIdWithNoActivities = UUID.randomUUID();
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        // When & Then
        this.mockMvc.perform(get(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING + 
                EducGradStudentApiConstants.HISTORIC_STUDENT_ACTIVITY_MAPPING.replace("{studentID}", studentIdWithNoActivities.toString()))
                .with(mockAuthority))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void testGetHistoricStudentActivities_withInvalidScope_shouldReturn403() throws Exception {
        // Given
        final GrantedAuthority grantedAuthority = () -> "SCOPE_INVALID_SCOPE";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        // When & Then
        this.mockMvc.perform(get(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING + 
                EducGradStudentApiConstants.HISTORIC_STUDENT_ACTIVITY_MAPPING.replace("{studentID}", testStudentId.toString()))
                .with(mockAuthority))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void testGetHistoricStudentActivities_withInvalidStudentId_shouldReturn400() throws Exception {
        // Given
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        // When & Then
        this.mockMvc.perform(get(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING + 
                EducGradStudentApiConstants.HISTORIC_STUDENT_ACTIVITY_MAPPING.replace("{studentID}", "invalid-uuid"))
                .with(mockAuthority))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    private HistoricStudentActivityEntity createHistoricStudentActivityEntity() {
        HistoricStudentActivityEntity entity = new HistoricStudentActivityEntity();
        entity.setHistoricStudentActivityID(UUID.randomUUID());
        entity.setGraduationStudentRecordID(UUID.randomUUID()); // Will be updated after save
        entity.setDate(LocalDateTime.now());
        entity.setType("ADD");
        entity.setProgram("2023");
        entity.setUserID("USER123");
        entity.setBatch("001");
        entity.setSeqNo("0001");
        entity.setCreateUser("TEST_USER");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateUser("TEST_USER");
        entity.setUpdateDate(LocalDateTime.now());
        return entity;
    }
}
