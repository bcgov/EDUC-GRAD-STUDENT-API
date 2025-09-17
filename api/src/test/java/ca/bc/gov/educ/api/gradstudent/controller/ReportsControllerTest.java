package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramRepository;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReportsControllerTest extends BaseIntegrationTest {

    protected static final ObjectMapper objectMapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RestUtils restUtils;
    @Autowired
    private GraduationStudentRecordRepository graduationStudentRecordRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        graduationStudentRecordRepository.deleteAll();
    }

    @Test
    void testGetDownloadableReport_LTP12ItemAnalysis_ShouldReturnCSVFile() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_STUDENT_REPORT";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var schoolID = UUID.randomUUID();
        GraduationStudentRecordEntity student = new GraduationStudentRecordEntity();
        student.setStudentID(UUID.randomUUID());
        student.setPen("123456789");
        student.setStudentStatus("A");
        student.setRecalculateGradStatus("Y");
        student.setProgram("2023-EN");
        student.setSchoolOfRecordId(schoolID);
        student.setSchoolAtGradId(schoolID);
        student.setGpa("4");
        student.setProgramCompletionDate(Date.valueOf(LocalDate.now().minusDays(10)));

        graduationStudentRecordRepository.save(student);

        var district = createMockDistrict();
        var school = createMockSchoolTombstone();
        school.setSchoolId(String.valueOf(schoolID));
        school.setDistrictId(district.getDistrictId());

        when(restUtils.getDistrictByDistrictID(any())).thenReturn(Optional.of(district));
        when(restUtils.getSchoolList()).thenReturn(List.of(school));
        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(school));

        var fromDate = LocalDate.now().minusDays(10).toString();
        var toDate = LocalDate.now().toString();

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/" + district.getDistrictId() + "/download/" + fromDate + "/" + toDate)
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        val summary = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), DownloadableReportResponse.class);

        assertThat(summary).isNotNull();
        assertThat(summary.getReportType()).isEqualTo("yukon-report");
        assertThat(summary.getDocumentData()).isNotBlank();
    }

}
