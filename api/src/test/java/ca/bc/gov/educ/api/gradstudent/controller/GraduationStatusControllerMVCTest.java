package ca.bc.gov.educ.api.gradstudent.controller;


import ca.bc.gov.educ.api.gradstudent.EducGradStudentApiApplication;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = {EducGradStudentApiApplication.class})
@AutoConfigureMockMvc
class GraduationStatusControllerMVCTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    GraduationStatusService gradStatusService;

    @MockBean
    JwtDecoder jwtDecoder;

    private static final String BASE = "/api/v1/student/gradstudent/studentid/";
    private static final String TOKEN = "test-token";


    @Test
    void badRequest_whenProgramCompletionDateInvalid() throws Exception {
        String studentId = UUID.randomUUID().toString();
        when(jwtDecoder.decode(TOKEN)).thenReturn(getJwt());
        String url = BASE + studentId;

        // invalid format: should be YYYY/MM
        var body = """
                {"studentID": "%s", "programCompletionDate": "2025-09" }
                """.formatted(studentId);

        mockMvc.perform(post(url)
                        .header("Authorization", "Bearer " + TOKEN)
                        .with(csrf()) // only needed if CSRF is enabled for POST
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("YYYY/MM")));
        verifyNoInteractions(gradStatusService);
    }

    @Test
    void badRequest_whenStudentIdInvalid() throws Exception {
        String studentId = UUID.randomUUID().toString();
        when(jwtDecoder.decode(TOKEN)).thenReturn(getJwt());
        String url = BASE + studentId;

        // invalid format: should be a valid studentId
        var body = """
                {"studentID": null}
                """;

        mockMvc.perform(post(url)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Student ID is required")));
        verifyNoInteractions(gradStatusService);
    }

    private Jwt getJwt() {
        return Jwt.withTokenValue(TOKEN)
                .header("alg", "none")
                // include scope so Spring maps it to SCOPE_UPDATE_GRADUATION_STUDENT
                .claim("scope", "SCOPE_UPDATE_GRAD_GRADUATION_STATUS")
                .claim("client_id", "grad-client")
                .build();
    }


}
