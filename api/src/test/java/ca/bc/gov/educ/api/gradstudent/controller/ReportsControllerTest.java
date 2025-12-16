package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.DownloadableReportResponse;
import ca.bc.gov.educ.api.gradstudent.model.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationData;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.OptionalProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordPaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCoursePaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CourseCodeRecord;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCoursePaginationRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramRepository;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigInteger;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
    @Autowired
    private StudentOptionalProgramRepository studentOptionalProgramRepository;
    @Autowired
    private StudentCoursePaginationRepository studentCoursePaginationRepository;
    @Autowired
    private ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordPaginationRepository graduationStudentRecordPaginationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        studentOptionalProgramRepository.deleteAll();
        studentCoursePaginationRepository.deleteAll();
        graduationStudentRecordPaginationRepository.deleteAll();
        graduationStudentRecordRepository.deleteAll();
    }

    @Test
    void testGetDownloadableReport_YukonReport_withMissingDistrictID_ShouldReturnBadRequest() throws Exception {
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

        UUID districtID = null;

        var fromDate = LocalDate.now().minusDays(10).toString();
        var toDate = LocalDate.now().toString();

        this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/" + districtID + "/download/" + fromDate + "/" + toDate)
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    void testGetDownloadableReport_YukonReport_withMissingDates_ShouldReturnBadRequest() throws Exception {
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

        this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/" + UUID.randomUUID() + "/download/" + " " + "/" + " ")
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isBadRequest());
    }

    @Test
    void testGetDownloadableReport_getYukonReport_ShouldReturnCSVFile() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_STUDENT_REPORT";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);
        var optProg = new OptionalProgramCode(UUID.randomUUID(), "DD", "Dual Dogwood", "", 3, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString());

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

        StudentOptionalProgramEntity optProgram = new StudentOptionalProgramEntity();
        optProgram.setStudentID(student.getStudentID());
        optProgram.setOptionalProgramID(optProg.getOptionalProgramID());
        optProgram.setOptionalProgramCompletionDate(Date.valueOf(LocalDate.now().minusDays(10)));
        studentOptionalProgramRepository.save(optProgram);

        var district = createMockDistrict();
        var school = createMockSchoolTombstone();
        school.setSchoolId(String.valueOf(schoolID));
        school.setDistrictId(district.getDistrictId());

        when(restUtils.getDistrictByDistrictID(any())).thenReturn(Optional.of(district));
        when(restUtils.getSchoolList()).thenReturn(List.of(school));
        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(school));
        when(restUtils.getOptionalProgramCodeList()).thenReturn(List.of(optProg));

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

    @Test
    void testGetDownloadableReport_getYukonReport_WithNoStudent_ShouldReturnCSVFile() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_STUDENT_REPORT";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);
        var optProg1 = new OptionalProgramCode(UUID.randomUUID(), "DD", "Dual Dogwood", "", 3, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString());
        var optProg2 = new OptionalProgramCode(UUID.randomUUID(), "FI", "French Immersion", "", 3, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString());

        GraduationData data = new GraduationData();
        data.setGradStudent(null);
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
        student.setStudentGradData(JsonUtil.getJsonStringFromObject(data));
        graduationStudentRecordRepository.save(student);

        StudentOptionalProgramEntity optProgram1 = new StudentOptionalProgramEntity();
        optProgram1.setStudentID(student.getStudentID());
        optProgram1.setOptionalProgramID(optProg1.getOptionalProgramID());
        optProgram1.setOptionalProgramCompletionDate(Date.valueOf(LocalDate.now().minusDays(10)));

        StudentOptionalProgramEntity optProgram2 = new StudentOptionalProgramEntity();
        optProgram2.setStudentID(student.getStudentID());
        optProgram2.setOptionalProgramID(optProg2.getOptionalProgramID());
        optProgram2.setOptionalProgramCompletionDate(Date.valueOf(LocalDate.now().minusDays(10)));
        studentOptionalProgramRepository.saveAll(List.of(optProgram1, optProgram2));

        var district = createMockDistrict();
        var school = createMockSchoolTombstone();
        school.setSchoolId(String.valueOf(schoolID));
        school.setDistrictId(district.getDistrictId());

        when(restUtils.getDistrictByDistrictID(any())).thenReturn(Optional.of(district));
        when(restUtils.getSchoolList()).thenReturn(List.of(school));
        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(school));
        when(restUtils.getOptionalProgramCodeList()).thenReturn(List.of(optProg1, optProg2));

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


    @Test
    void testGetDownloadableReport_getYukonReport_WithStudentData_ShouldReturnCSVFile() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_STUDENT_REPORT";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);
        var optProg = new OptionalProgramCode(UUID.randomUUID(), "FI", "French Immersion", "", 3, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString());

        GraduationData data = new GraduationData();
        data.setGradStudent(GradSearchStudent.builder().legalFirstName("Test").legalLastName("Test").pen("123456789").gradeCode("08").build());
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
        student.setStudentGradData(JsonUtil.getJsonStringFromObject(data));
        graduationStudentRecordRepository.save(student);

        StudentOptionalProgramEntity optProgram = new StudentOptionalProgramEntity();
        optProgram.setStudentID(student.getStudentID());
        optProgram.setOptionalProgramID(optProg.getOptionalProgramID());
        optProgram.setOptionalProgramCompletionDate(Date.valueOf(LocalDate.now().minusDays(10)));
        studentOptionalProgramRepository.save(optProgram);

        var district = createMockDistrict();
        var school = createMockSchoolTombstone();
        school.setSchoolId(String.valueOf(schoolID));
        school.setDistrictId(district.getDistrictId());

        when(restUtils.getDistrictByDistrictID(any())).thenReturn(Optional.of(district));
        when(restUtils.getSchoolList()).thenReturn(List.of(school));
        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(school));
        when(restUtils.getOptionalProgramCodeList()).thenReturn(List.of(optProg));

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

    @Test
    void testGetCourseStudentSearchReport_ShouldReturnCSVFile() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var schoolID = UUID.randomUUID();

        var gradStudent = new GraduationStudentRecordPaginationEntity();
        gradStudent.setStudentID(UUID.randomUUID());
        gradStudent.setPen("123456789");
        gradStudent.setLegalFirstName("John");
        gradStudent.setLegalLastName("Doe");
        gradStudent.setLegalMiddleNames("M");
        gradStudent.setStudentStatus("CUR");
        gradStudent.setProgram("2018-EN");
        gradStudent.setStudentGrade("12");
        gradStudent.setSchoolOfRecord("12345678");
        gradStudent.setSchoolAtGraduation("12345678");
        gradStudent.setSchoolOfRecordId(schoolID);
        gradStudent.setSchoolAtGraduationId(schoolID);
        gradStudent.setDob(Date.valueOf(LocalDate.of(2005, 5, 15)));
        gradStudent.setProgramCompletionDate(Date.valueOf(LocalDate.now().minusDays(10)));
        gradStudent = graduationStudentRecordPaginationRepository.save(gradStudent);

        StudentCoursePaginationEntity course = new StudentCoursePaginationEntity();
        course.setStudentCourseID(UUID.randomUUID());
        course.setGraduationStudentRecordEntity(gradStudent);
        course.setCourseID(BigInteger.valueOf(872087L));
        course.setCourseSession("202309");
        course.setInterimPercent(85.5);
        course.setInterimLetterGrade("A");
        course.setCompletedCoursePercentage(88.0);
        course.setFinalLetterGrade("A");
        course.setCredits(4);
        course.setEquivOrChallenge("E");
        course.setFineArtsAppliedSkillsCode("F");
        studentCoursePaginationRepository.save(course);

        var school = createMockSchoolTombstone();
        school.setSchoolId(String.valueOf(schoolID));
        school.setMincode("12345678");
        school.setDisplayName("Test High School");

        var courseCodeRecord = new CourseCodeRecord();
        courseCodeRecord.setCourseID("872087");
        courseCodeRecord.setExternalCode("FRAN  11");

        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(school));
        when(restUtils.getCoreg39CourseByID("872087")).thenReturn(Optional.of(courseCodeRecord));

        var searchCriteria = "[{\"condition\":\"AND\",\"searchCriteriaList\":[{\"key\":\"graduationStudentRecordEntity.program\",\"operation\":\"eq\",\"value\":\"2018-EN\",\"valueType\":\"STRING\"}]}]";

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/course-students/search/download")
                                .param("searchCriteriaList", searchCriteria)
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("PEN");
        assertThat(csvContent).contains("123456789");
        assertThat(csvContent).contains("Doe");
        assertThat(csvContent).contains("FRAN");
        assertThat(csvContent).contains("11");
        assertThat(csvContent).contains("E");
        assertThat(csvContent).contains("F");
        assertThat(csvContent).contains("No");
    }

    @Test
    void testGetCourseStudentSearchReport_WithNoSearchCriteria_ShouldReturnAllRecords() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var schoolID = UUID.randomUUID();

        var gradStudent = new GraduationStudentRecordPaginationEntity();
        gradStudent.setStudentID(UUID.randomUUID());
        gradStudent.setPen("987654321");
        gradStudent.setLegalLastName("Smith");
        gradStudent.setStudentStatus("CUR");
        gradStudent.setProgram("2023-EN");
        gradStudent.setStudentGrade("11");
        gradStudent.setSchoolOfRecord("87654321");
        gradStudent.setSchoolOfRecordId(schoolID);
        gradStudent = graduationStudentRecordPaginationRepository.save(gradStudent);

        StudentCoursePaginationEntity course = new StudentCoursePaginationEntity();
        course.setStudentCourseID(UUID.randomUUID());
        course.setGraduationStudentRecordEntity(gradStudent);
        course.setCourseID(BigInteger.valueOf(872066L));
        course.setCourseSession("202401");
        course.setCredits(4);
        studentCoursePaginationRepository.save(course);

        var school = createMockSchoolTombstone();
        school.setSchoolId(String.valueOf(schoolID));
        school.setDisplayName("Another School");

        var courseCodeRecord = new CourseCodeRecord();
        courseCodeRecord.setCourseID("872066");
        courseCodeRecord.setExternalCode("INDT 10D");

        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(school));
        when(restUtils.getCoreg39CourseByID("872066")).thenReturn(Optional.of(courseCodeRecord));

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/course-students/search/download")
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("987654321");
        assertThat(csvContent).contains("Smith");
    }

    @Test
    void testGetCourseStudentSearchReport_WithMissingCourseInCache_ShouldHandleGracefully() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var gradStudent = new GraduationStudentRecordPaginationEntity();
        gradStudent.setStudentID(UUID.randomUUID());
        gradStudent.setPen("111222333");
        gradStudent.setLegalLastName("Test");
        gradStudent.setStudentStatus("CUR");
        gradStudent.setProgram("2018-EN");
        gradStudent = graduationStudentRecordPaginationRepository.save(gradStudent);

        StudentCoursePaginationEntity course = new StudentCoursePaginationEntity();
        course.setStudentCourseID(UUID.randomUUID());
        course.setGraduationStudentRecordEntity(gradStudent);
        course.setCourseID(BigInteger.valueOf(999999L));
        course.setCourseSession("202309");
        course.setCredits(4);
        studentCoursePaginationRepository.save(course);

        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.empty());
        when(restUtils.getCoreg39CourseByID("999999")).thenReturn(Optional.empty());

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/course-students/search/download")
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("111222333");
    }

}
