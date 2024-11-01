package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

class CommonControllerIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetAllStudentGradeCodes_ShouldReturnCodes() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_STUDENT_GRADE_CODES";
    final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

    this.mockMvc.perform(get(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING + EducGradStudentApiConstants.GET_ALL_STUDENT_GRADE_CODES).with(mockAuthority)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(4)).andExpect(jsonPath("$[0].studentGradeCode").value("07"));
  }

  @Test
  void testGetAllStudentGradeCodes_GivenInvalidScope_ShouldReturn403() throws Exception {
    final GrantedAuthority grantedAuthority = () -> "SCOPE_INVALID_SCOPE";
    final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

    this.mockMvc.perform(get(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING + EducGradStudentApiConstants.GET_ALL_STUDENT_GRADE_CODES).with(mockAuthority)).andDo(print()).andExpect(status().isForbidden());
  }
}
