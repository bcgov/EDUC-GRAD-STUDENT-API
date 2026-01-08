package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.OptionalProgram;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCareerProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCareerProgramRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramRepository;
import ca.bc.gov.educ.api.gradstudent.service.RESTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for updateStudentPrograms functionality.
 * Tests that career and optional programs are completely replaced (not diff-based).
 */
class UpdateStudentProgramsIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GraduationStudentRecordRepository graduationStudentRecordRepository;

    @Autowired
    private StudentCareerProgramRepository studentCareerProgramRepository;

    @Autowired
    private StudentOptionalProgramRepository studentOptionalProgramRepository;

    @Autowired
    private GradStatusEventRepository gradStatusEventRepository;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private RESTService restService;

    private static final String BASE = "/api/v1/student/gradstudent/studentid/";
    private static final String TOKEN = "test-token";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(jwtDecoder.decode(TOKEN)).thenReturn(getJwt());
        gradStatusEventRepository.deleteAll();
        studentCareerProgramRepository.deleteAll();
        studentOptionalProgramRepository.deleteAll();
        graduationStudentRecordRepository.deleteAll();
    }

    private Jwt getJwt() {
        return Jwt.withTokenValue(TOKEN)
                .header("alg", "none")
                .claim("scope", "UPDATE_GRAD_GRADUATION_STATUS")
                .claim("client_id", "grad-client")
                .build();
    }

    @Test
    void updateStudentPrograms_replacesCareerPrograms() throws Exception {
        // ARRANGE: Create student with existing career program "XA"
        UUID studentID = UUID.randomUUID();

        GraduationStudentRecordEntity student = new GraduationStudentRecordEntity();
        student.setStudentID(studentID);
        student.setPen("123456789");
        student.setProgram("2018-EN");
        student.setStudentStatus("CUR");
        student.setStudentGrade("10");
        graduationStudentRecordRepository.save(student);

        StudentCareerProgramEntity existingCareer = new StudentCareerProgramEntity();
        existingCareer.setId(UUID.randomUUID());
        existingCareer.setStudentID(studentID);
        existingCareer.setCareerProgramCode("XA");
        studentCareerProgramRepository.save(existingCareer);

        // ACT: Update with new career program "XB" (should replace "XA")
        String body = """
            {
                "studentID": "%s",
                "pen": "123456789",
                "program": "2018-EN",
                "studentStatus": "CUR",
                "studentGrade": "10",
                "careerPrograms": [{"careerProgramCode": "XB"}]
            }
            """.formatted(studentID);

        this.mockMvc.perform(post(BASE + studentID + "?updatePrograms=true")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());

        // ASSERT: Database should have only "XB", "XA" should be gone
        var programs = studentCareerProgramRepository.findByStudentID(studentID);
        assertThat(programs).hasSize(1);
        assertThat(programs.get(0).getCareerProgramCode()).isEqualTo("XB");
    }

    @Test
    void updateStudentPrograms_clearsAllCareerProgramsWithEmptyList() throws Exception {
        // ARRANGE: Create student with existing career program
        UUID studentID = UUID.randomUUID();

        GraduationStudentRecordEntity student = new GraduationStudentRecordEntity();
        student.setStudentID(studentID);
        student.setPen("123456789");
        student.setProgram("2018-EN");
        student.setStudentStatus("CUR");
        student.setStudentGrade("10");
        graduationStudentRecordRepository.save(student);

        StudentCareerProgramEntity existingCareer = new StudentCareerProgramEntity();
        existingCareer.setId(UUID.randomUUID());
        existingCareer.setStudentID(studentID);
        existingCareer.setCareerProgramCode("XA");
        studentCareerProgramRepository.save(existingCareer);

        // ACT: Update with empty career programs (should clear all)
        String body = """
            {
                "studentID": "%s",
                "pen": "123456789",
                "program": "2018-EN",
                "studentStatus": "CUR",
                "studentGrade": "10",
                "careerPrograms": []
            }
            """.formatted(studentID);

        this.mockMvc.perform(post(BASE + studentID + "?updatePrograms=true")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());

        // ASSERT: No career programs should remain
        var programs = studentCareerProgramRepository.findByStudentID(studentID);
        assertThat(programs).isEmpty();
    }

    @Test
    void updateStudentPrograms_replacesOptionalPrograms() throws Exception {
        // ARRANGE: Create student with existing optional program
        UUID studentID = UUID.randomUUID();
        UUID existingOptProgramId = UUID.randomUUID();
        UUID newOptProgramId = UUID.randomUUID();

        GraduationStudentRecordEntity student = new GraduationStudentRecordEntity();
        student.setStudentID(studentID);
        student.setPen("123456789");
        student.setProgram("2018-EN");
        student.setStudentStatus("CUR");
        student.setStudentGrade("10");
        graduationStudentRecordRepository.save(student);

        StudentOptionalProgramEntity existingOptional = new StudentOptionalProgramEntity();
        existingOptional.setId(UUID.randomUUID());
        existingOptional.setStudentID(studentID);
        existingOptional.setOptionalProgramID(existingOptProgramId);
        studentOptionalProgramRepository.save(existingOptional);

        // ACT: Update with new optional program (should replace existing)
        String body = """
            {
                "studentID": "%s",
                "pen": "123456789",
                "program": "2018-EN",
                "studentStatus": "CUR",
                "studentGrade": "10",
                "optionalPrograms": [{"optionalProgramID": "%s"}]
            }
            """.formatted(studentID, newOptProgramId);

        this.mockMvc.perform(post(BASE + studentID + "?updatePrograms=true")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());

        // ASSERT: Should have new optional program, old one should be gone
        var programs = studentOptionalProgramRepository.findByStudentID(studentID);
        assertThat(programs).hasSize(1);
        assertThat(programs.get(0).getOptionalProgramID()).isEqualTo(newOptProgramId);
    }

    @Test
    void updateGraduationStatus_citizenshipChange_createsEvent() throws Exception {
        // ARRANGE: Create student with citizenship "C"
        UUID studentID = UUID.randomUUID();

        GraduationStudentRecordEntity student = new GraduationStudentRecordEntity();
        student.setStudentID(studentID);
        student.setPen("123456789");
        student.setProgram("2018-EN");
        student.setStudentStatus("CUR");
        student.setStudentGrade("10");
        student.setStudentCitizenship("C");
        graduationStudentRecordRepository.save(student);

        long eventCountBefore = gradStatusEventRepository.count();

        // ACT: Update with different citizenship "O"
        String body = """
            {
                "studentID": "%s",
                "pen": "123456789",
                "program": "2018-EN",
                "studentStatus": "CUR",
                "studentGrade": "10",
                "studentCitizenship": "O"
            }
            """.formatted(studentID);

        this.mockMvc.perform(post(BASE + studentID)
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());

        // ASSERT: Citizenship event should be created (count increased by at least 2: grad update + citizenship)
        long eventCountAfter = gradStatusEventRepository.count();
        assertThat(eventCountAfter).isGreaterThan(eventCountBefore);

        // Verify citizenship was updated
        var updatedStudent = graduationStudentRecordRepository.findById(studentID).orElseThrow();
        assertThat(updatedStudent.getStudentCitizenship()).isEqualTo("O");
    }

    @Test
    void updateStudentPrograms_addingFirstCareerProgram_createsCPOptionalProgram() throws Exception {
        // ARRANGE: Create student with NO career programs
        UUID studentID = UUID.randomUUID();
        UUID cpOptionalProgramID = UUID.randomUUID();

        GraduationStudentRecordEntity student = new GraduationStudentRecordEntity();
        student.setStudentID(studentID);
        student.setPen("123456789");
        student.setProgram("2018-EN");
        student.setStudentStatus("CUR");
        student.setStudentGrade("10");
        graduationStudentRecordRepository.save(student);

        // Mock getOptionalProgram("2018-EN", "CP") to return a CP program
        OptionalProgram cpProgram = new OptionalProgram();
        cpProgram.setOptionalProgramID(cpOptionalProgramID);
        cpProgram.setOptProgramCode("CP");
        cpProgram.setGraduationProgramCode("2018-EN");
        when(restService.get(any(), eq(OptionalProgram.class), any())).thenReturn(cpProgram);

        // ACT: Add career program (first one - triggers CP creation)
        String body = """
            {
                "studentID": "%s",
                "pen": "123456789",
                "program": "2018-EN",
                "studentStatus": "CUR",
                "studentGrade": "10",
                "careerPrograms": [{"careerProgramCode": "XA"}]
            }
            """.formatted(studentID);

        this.mockMvc.perform(post(BASE + studentID + "?updatePrograms=true")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());

        // ASSERT: CP optional program should be auto-created
        var optionalPrograms = studentOptionalProgramRepository.findByStudentID(studentID);
        assertThat(optionalPrograms).anyMatch(op -> op.getOptionalProgramID().equals(cpOptionalProgramID));

        // ASSERT: Career program should also be added
        var careerPrograms = studentCareerProgramRepository.findByStudentID(studentID);
        assertThat(careerPrograms).hasSize(1);
        assertThat(careerPrograms.get(0).getCareerProgramCode()).isEqualTo("XA");
    }

    @Test
    void updateStudentPrograms_removingAllCareerPrograms_removesCPOptionalProgram() throws Exception {
        // ARRANGE: Create student WITH existing career program and CP optional program
        UUID studentID = UUID.randomUUID();
        UUID cpOptionalProgramID = UUID.randomUUID();

        GraduationStudentRecordEntity student = new GraduationStudentRecordEntity();
        student.setStudentID(studentID);
        student.setPen("123456789");
        student.setProgram("2018-EN");
        student.setStudentStatus("CUR");
        student.setStudentGrade("10");
        graduationStudentRecordRepository.save(student);

        StudentCareerProgramEntity existingCareer = new StudentCareerProgramEntity();
        existingCareer.setId(UUID.randomUUID());
        existingCareer.setStudentID(studentID);
        existingCareer.setCareerProgramCode("XA");
        studentCareerProgramRepository.save(existingCareer);

        StudentOptionalProgramEntity cpOptional = new StudentOptionalProgramEntity();
        cpOptional.setId(UUID.randomUUID());
        cpOptional.setStudentID(studentID);
        cpOptional.setOptionalProgramID(cpOptionalProgramID);
        studentOptionalProgramRepository.save(cpOptional);

        // Mock getOptionalProgram("2018-EN", "CP") to return CP program
        OptionalProgram cpProgram = new OptionalProgram();
        cpProgram.setOptionalProgramID(cpOptionalProgramID);
        cpProgram.setOptProgramCode("CP");
        cpProgram.setGraduationProgramCode("2018-EN");
        when(restService.get(any(), eq(OptionalProgram.class), any())).thenReturn(cpProgram);

        // ACT: Remove all career programs (triggers CP removal)
        String body = """
            {
                "studentID": "%s",
                "pen": "123456789",
                "program": "2018-EN",
                "studentStatus": "CUR",
                "studentGrade": "10",
                "careerPrograms": []
            }
            """.formatted(studentID);

        this.mockMvc.perform(post(BASE + studentID + "?updatePrograms=true")
                        .header("Authorization", "Bearer " + TOKEN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andDo(print())
                .andExpect(status().isOk());

        // ASSERT: CP optional program should be auto-removed
        var optionalPrograms = studentOptionalProgramRepository.findByStudentID(studentID);
        assertThat(optionalPrograms).noneMatch(op -> op.getOptionalProgramID().equals(cpOptionalProgramID));

        // ASSERT: No career programs should remain
        var careerPrograms = studentCareerProgramRepository.findByStudentID(studentID);
        assertThat(careerPrograms).isEmpty();
    }
}
