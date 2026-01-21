package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.DownloadableReportResponse;
import ca.bc.gov.educ.api.gradstudent.model.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationData;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.OptionalProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordPaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCoursePaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CourseCodeRecord;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCoursePaginationRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramPaginationRepository;
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
    private StudentOptionalProgramPaginationRepository studentOptionalProgramPaginationRepository;
    @Autowired
    private ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordPaginationRepository graduationStudentRecordPaginationRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        studentOptionalProgramRepository.deleteAll();
        studentOptionalProgramPaginationRepository.deleteAll();
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
        course.setFinalPercent(88.0);
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

    @Test
    void testGetProgramStudentSearchReport_ShouldReturnCSVFile() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var schoolID = UUID.randomUUID();

        var gradStudent = new GraduationStudentRecordEntity();
        gradStudent.setStudentID(UUID.randomUUID());
        gradStudent.setPen("123456789");
        gradStudent.setLegalFirstName("Jane");
        gradStudent.setLegalLastName("Smith");
        gradStudent.setLegalMiddleNames("Anne");
        gradStudent.setStudentStatus("CUR");
        gradStudent.setProgram("2018-EN");
        gradStudent.setStudentGrade("12");
        gradStudent.setSchoolOfRecordId(schoolID);
        gradStudent.setSchoolAtGradId(schoolID);
        gradStudent.setDob(LocalDateTime.of(2005, 3, 20, 0, 0));
        gradStudent.setProgramCompletionDate(Date.valueOf(LocalDate.now().minusDays(5)));
        gradStudent.setAdultStartDate(Date.valueOf(LocalDate.of(2023, 9, 1)));
        gradStudent.setRecalculateGradStatus("Y");
        gradStudent.setRecalculateProjectedGrad("N");
        graduationStudentRecordRepository.save(gradStudent);

        var school = createMockSchoolTombstone();
        school.setSchoolId(String.valueOf(schoolID));
        school.setMincode("12345678");
        school.setDisplayName("Test High School");

        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(school));

        var searchCriteria = "[{\"condition\":\"AND\",\"searchCriteriaList\":[{\"key\":\"program\",\"operation\":\"eq\",\"value\":\"2018-EN\",\"valueType\":\"STRING\"}]}]";

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/program-students/search/download")
                                .param("searchCriteriaList", searchCriteria)
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("PEN");
        assertThat(csvContent).contains("Student Status");
        assertThat(csvContent).contains("Surname");
        assertThat(csvContent).contains("Given Name");
        assertThat(csvContent).contains("Middle Name");
        assertThat(csvContent).contains("123456789");
        assertThat(csvContent).contains("Current");
        assertThat(csvContent).contains("Smith");
        assertThat(csvContent).contains("Jane");
        assertThat(csvContent).contains("Anne");
        assertThat(csvContent).contains("2018-EN");
        assertThat(csvContent).contains("12345678");
        assertThat(csvContent).contains("Test High School");
        assertThat(csvContent).contains("Yes");
        assertThat(csvContent).contains("No");
    }

    @Test
    void testGetProgramStudentSearchReport_WithNoSearchCriteria_ShouldReturnAllRecords() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var schoolID = UUID.randomUUID();

        var gradStudent = new GraduationStudentRecordEntity();
        gradStudent.setStudentID(UUID.randomUUID());
        gradStudent.setPen("987654321");
        gradStudent.setLegalFirstName("Bob");
        gradStudent.setLegalLastName("Johnson");
        gradStudent.setStudentStatus("ARC");
        gradStudent.setProgram("2023-EN");
        gradStudent.setStudentGrade("11");
        gradStudent.setSchoolOfRecordId(schoolID);
        gradStudent.setDob(LocalDateTime.of(2006, 7, 15, 0, 0));
        gradStudent.setRecalculateGradStatus("N");
        gradStudent.setRecalculateProjectedGrad("Y");
        graduationStudentRecordRepository.save(gradStudent);

        var school = createMockSchoolTombstone();
        school.setSchoolId(String.valueOf(schoolID));
        school.setMincode("87654321");
        school.setDisplayName("Another School");

        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(school));

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/program-students/search/download")
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("987654321");
        assertThat(csvContent).contains("Archived");
        assertThat(csvContent).contains("Johnson");
        assertThat(csvContent).contains("Bob");
        assertThat(csvContent).contains("87654321");
    }

    @Test
    void testGetProgramStudentSearchReport_WithMultipleStudents_ShouldReturnAll() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var schoolID1 = UUID.randomUUID();
        var schoolID2 = UUID.randomUUID();

        var gradStudent1 = new GraduationStudentRecordEntity();
        gradStudent1.setStudentID(UUID.randomUUID());
        gradStudent1.setPen("111111111");
        gradStudent1.setLegalFirstName("Alice");
        gradStudent1.setLegalLastName("Williams");
        gradStudent1.setStudentStatus("CUR");
        gradStudent1.setProgram("2018-EN");
        gradStudent1.setStudentGrade("12");
        gradStudent1.setSchoolOfRecordId(schoolID1);
        gradStudent1.setSchoolAtGradId(schoolID1);
        gradStudent1.setDob(LocalDateTime.of(2005, 1, 10, 0, 0));
        gradStudent1.setRecalculateGradStatus("Y");
        gradStudent1.setRecalculateProjectedGrad("Y");

        var gradStudent2 = new GraduationStudentRecordEntity();
        gradStudent2.setStudentID(UUID.randomUUID());
        gradStudent2.setPen("222222222");
        gradStudent2.setLegalFirstName("Charlie");
        gradStudent2.setLegalLastName("Brown");
        gradStudent2.setStudentStatus("TER");
        gradStudent2.setProgram("2018-PF");
        gradStudent2.setStudentGrade("10");
        gradStudent2.setSchoolOfRecordId(schoolID2);
        gradStudent2.setDob(LocalDateTime.of(2007, 11, 25, 0, 0));
        gradStudent2.setRecalculateGradStatus("N");
        gradStudent2.setRecalculateProjectedGrad("N");

        graduationStudentRecordRepository.saveAll(List.of(gradStudent1, gradStudent2));

        var school1 = createMockSchoolTombstone();
        school1.setSchoolId(String.valueOf(schoolID1));
        school1.setMincode("11111111");
        school1.setDisplayName("School One");

        var school2 = createMockSchoolTombstone();
        school2.setSchoolId(String.valueOf(schoolID2));
        school2.setMincode("22222222");
        school2.setDisplayName("School Two");

        when(restUtils.getSchoolBySchoolID(schoolID1.toString())).thenReturn(Optional.of(school1));
        when(restUtils.getSchoolBySchoolID(schoolID2.toString())).thenReturn(Optional.of(school2));

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/program-students/search/download")
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("111111111");
        assertThat(csvContent).contains("222222222");
        assertThat(csvContent).contains("Williams");
        assertThat(csvContent).contains("Brown");
        assertThat(csvContent).contains("Current");
        assertThat(csvContent).contains("Terminated");
        assertThat(csvContent).contains("11111111");
        assertThat(csvContent).contains("22222222");
    }

    @Test
    void testGetProgramStudentSearchReport_WithNullFields_ShouldHandleGracefully() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var gradStudent = new GraduationStudentRecordEntity();
        gradStudent.setStudentID(UUID.randomUUID());
        gradStudent.setPen("333333333");
        gradStudent.setStudentStatus("CUR");
        gradStudent.setProgram("2018-EN");

        graduationStudentRecordRepository.save(gradStudent);

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/program-students/search/download")
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("333333333");
        assertThat(csvContent).contains("Current");
    }

    @Test
    void testGetProgramStudentSearchReport_WithMissingSchoolInCache_ShouldHandleGracefully() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var schoolID = UUID.randomUUID();

        var gradStudent = new GraduationStudentRecordEntity();
        gradStudent.setStudentID(UUID.randomUUID());
        gradStudent.setPen("444444444");
        gradStudent.setLegalLastName("NoSchool");
        gradStudent.setStudentStatus("CUR");
        gradStudent.setProgram("2018-EN");
        gradStudent.setSchoolOfRecordId(schoolID);
        gradStudent.setSchoolAtGradId(schoolID);
        gradStudent.setDob(LocalDateTime.of(2005, 6, 10, 0, 0));
        graduationStudentRecordRepository.save(gradStudent);

        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.empty());

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/program-students/search/download")
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("444444444");
        assertThat(csvContent).contains("NoSchool");
    }

    @Test
    void testGetProgramStudentSearchReport_WithAllStudentStatusTypes_ShouldMapCorrectly() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var gradStudent1 = new GraduationStudentRecordEntity();
        gradStudent1.setStudentID(UUID.randomUUID());
        gradStudent1.setPen("555555551");
        gradStudent1.setLegalLastName("Current");
        gradStudent1.setStudentStatus("CUR");
        gradStudent1.setProgram("2018-EN");

        var gradStudent2 = new GraduationStudentRecordEntity();
        gradStudent2.setStudentID(UUID.randomUUID());
        gradStudent2.setPen("555555552");
        gradStudent2.setLegalLastName("Archived");
        gradStudent2.setStudentStatus("ARC");
        gradStudent2.setProgram("2018-EN");

        var gradStudent3 = new GraduationStudentRecordEntity();
        gradStudent3.setStudentID(UUID.randomUUID());
        gradStudent3.setPen("555555553");
        gradStudent3.setLegalLastName("Deceased");
        gradStudent3.setStudentStatus("DEC");
        gradStudent3.setProgram("2018-EN");

        var gradStudent4 = new GraduationStudentRecordEntity();
        gradStudent4.setStudentID(UUID.randomUUID());
        gradStudent4.setPen("555555554");
        gradStudent4.setLegalLastName("Merged");
        gradStudent4.setStudentStatus("MER");
        gradStudent4.setProgram("2018-EN");

        var gradStudent5 = new GraduationStudentRecordEntity();
        gradStudent5.setStudentID(UUID.randomUUID());
        gradStudent5.setPen("555555555");
        gradStudent5.setLegalLastName("Terminated");
        gradStudent5.setStudentStatus("TER");
        gradStudent5.setProgram("2018-EN");

        graduationStudentRecordRepository.saveAll(List.of(gradStudent1, gradStudent2, gradStudent3, gradStudent4, gradStudent5));

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/program-students/search/download")
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("Current");
        assertThat(csvContent).contains("Archived");
        assertThat(csvContent).contains("Deceased");
        assertThat(csvContent).contains("Merged");
        assertThat(csvContent).contains("Terminated");
    }

    @Test
    void testGetOptionalProgramStudentSearchReport_ShouldReturnCSVFile() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var schoolID = UUID.randomUUID();
        var optionalProgramID = UUID.randomUUID();

        var gradStudent = new GraduationStudentRecordPaginationEntity();
        gradStudent.setStudentID(UUID.randomUUID());
        gradStudent.setPen("123456789");
        gradStudent.setLegalFirstName("John");
        gradStudent.setLegalLastName("Doe");
        gradStudent.setLegalMiddleNames("Michael");
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

        StudentOptionalProgramPaginationEntity optionalProgram = new StudentOptionalProgramPaginationEntity();
        optionalProgram.setStudentOptionalProgramID(UUID.randomUUID());
        optionalProgram.setGraduationStudentRecordEntity(gradStudent);
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setCompletionDate(Date.valueOf(LocalDate.now().minusDays(5)));
        studentOptionalProgramPaginationRepository.save(optionalProgram);

        var school = createMockSchoolTombstone();
        school.setSchoolId(String.valueOf(schoolID));
        school.setMincode("12345678");
        school.setDisplayName("Test High School");

        var optionalProgramCode = new OptionalProgramCode();
        optionalProgramCode.setOptionalProgramID(optionalProgramID);
        optionalProgramCode.setOptProgramCode("FI");
        optionalProgramCode.setOptionalProgramName("French Immersion");

        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(school));
        when(restUtils.getOptionalProgramCodeList()).thenReturn(List.of(optionalProgramCode));

        var searchCriteria = "[{\"condition\":\"AND\",\"searchCriteriaList\":[{\"key\":\"graduationStudentRecordEntity.program\",\"operation\":\"eq\",\"value\":\"2018-EN\",\"valueType\":\"STRING\"}]}]";

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/optional-program-students/search/download")
                                .param("searchCriteriaList", searchCriteria)
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("PEN");
        assertThat(csvContent).contains("Optional Program");
        assertThat(csvContent).contains("123456789");
        assertThat(csvContent).contains("Doe");
        assertThat(csvContent).contains("John");
        assertThat(csvContent).contains("Michael");
        assertThat(csvContent).contains("Current");
        assertThat(csvContent).contains("French Immersion");
        assertThat(csvContent).contains("2005-05-15");
    }

    @Test
    void testGetOptionalProgramStudentSearchReport_WithNoSearchCriteria_ShouldReturnAllRecords() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var schoolID = UUID.randomUUID();
        var optionalProgramID = UUID.randomUUID();

        var gradStudent = new GraduationStudentRecordPaginationEntity();
        gradStudent.setStudentID(UUID.randomUUID());
        gradStudent.setPen("987654321");
        gradStudent.setLegalFirstName("Jane");
        gradStudent.setLegalLastName("Smith");
        gradStudent.setStudentStatus("CUR");
        gradStudent.setProgram("2023-EN");
        gradStudent.setStudentGrade("11");
        gradStudent.setSchoolOfRecord("87654321");
        gradStudent.setSchoolOfRecordId(schoolID);
        gradStudent = graduationStudentRecordPaginationRepository.save(gradStudent);

        StudentOptionalProgramPaginationEntity optionalProgram = new StudentOptionalProgramPaginationEntity();
        optionalProgram.setStudentOptionalProgramID(UUID.randomUUID());
        optionalProgram.setGraduationStudentRecordEntity(gradStudent);
        optionalProgram.setOptionalProgramID(optionalProgramID);
        optionalProgram.setCompletionDate(Date.valueOf(LocalDate.now()));
        studentOptionalProgramPaginationRepository.save(optionalProgram);

        var school = createMockSchoolTombstone();
        school.setSchoolId(String.valueOf(schoolID));
        school.setDisplayName("Another School");

        var optionalProgramCode = new OptionalProgramCode();
        optionalProgramCode.setOptionalProgramID(optionalProgramID);
        optionalProgramCode.setOptProgramCode("CP");
        optionalProgramCode.setOptionalProgramName("Career Program");

        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(school));
        when(restUtils.getOptionalProgramCodeList()).thenReturn(List.of(optionalProgramCode));

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/optional-program-students/search/download")
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("987654321");
        assertThat(csvContent).contains("Smith");
        assertThat(csvContent).contains("Jane");
        assertThat(csvContent).contains("Career Program");
    }

    @Test
    void testGetOptionalProgramStudentSearchReport_WithMissingOptionalProgramInCache_ShouldHandleGracefully() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var optionalProgramID = UUID.randomUUID();

        var gradStudent = new GraduationStudentRecordPaginationEntity();
        gradStudent.setStudentID(UUID.randomUUID());
        gradStudent.setPen("111222333");
        gradStudent.setLegalLastName("Test");
        gradStudent.setStudentStatus("CUR");
        gradStudent.setProgram("2018-EN");
        gradStudent = graduationStudentRecordPaginationRepository.save(gradStudent);

        StudentOptionalProgramPaginationEntity optionalProgram = new StudentOptionalProgramPaginationEntity();
        optionalProgram.setStudentOptionalProgramID(UUID.randomUUID());
        optionalProgram.setGraduationStudentRecordEntity(gradStudent);
        optionalProgram.setOptionalProgramID(optionalProgramID);
        studentOptionalProgramPaginationRepository.save(optionalProgram);

        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.empty());
        when(restUtils.getOptionalProgramCodeList()).thenReturn(List.of());

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/optional-program-students/search/download")
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("111222333");
        assertThat(csvContent).contains("Test");
    }

    @Test
    void testGetOptionalProgramStudentSearchReport_WithMultipleStudents_ShouldReturnAllMatching() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var optionalProgramID1 = UUID.randomUUID();
        var optionalProgramID2 = UUID.randomUUID();

        var gradStudent1 = new GraduationStudentRecordPaginationEntity();
        gradStudent1.setStudentID(UUID.randomUUID());
        gradStudent1.setPen("111111111");
        gradStudent1.setLegalFirstName("Alice");
        gradStudent1.setLegalLastName("Anderson");
        gradStudent1.setStudentStatus("CUR");
        gradStudent1.setProgram("2018-EN");
        gradStudent1 = graduationStudentRecordPaginationRepository.save(gradStudent1);

        var gradStudent2 = new GraduationStudentRecordPaginationEntity();
        gradStudent2.setStudentID(UUID.randomUUID());
        gradStudent2.setPen("222222222");
        gradStudent2.setLegalFirstName("Bob");
        gradStudent2.setLegalLastName("Brown");
        gradStudent2.setStudentStatus("ARC");
        gradStudent2.setProgram("2018-EN");
        gradStudent2 = graduationStudentRecordPaginationRepository.save(gradStudent2);

        StudentOptionalProgramPaginationEntity optProgram1 = new StudentOptionalProgramPaginationEntity();
        optProgram1.setStudentOptionalProgramID(UUID.randomUUID());
        optProgram1.setGraduationStudentRecordEntity(gradStudent1);
        optProgram1.setOptionalProgramID(optionalProgramID1);
        optProgram1.setCompletionDate(Date.valueOf(LocalDate.now()));
        studentOptionalProgramPaginationRepository.save(optProgram1);

        StudentOptionalProgramPaginationEntity optProgram2 = new StudentOptionalProgramPaginationEntity();
        optProgram2.setStudentOptionalProgramID(UUID.randomUUID());
        optProgram2.setGraduationStudentRecordEntity(gradStudent2);
        optProgram2.setOptionalProgramID(optionalProgramID2);
        optProgram2.setCompletionDate(Date.valueOf(LocalDate.now().minusDays(30)));
        studentOptionalProgramPaginationRepository.save(optProgram2);

        var optionalProgramCode1 = new OptionalProgramCode();
        optionalProgramCode1.setOptionalProgramID(optionalProgramID1);
        optionalProgramCode1.setOptProgramCode("FI");
        optionalProgramCode1.setOptionalProgramName("French Immersion");

        var optionalProgramCode2 = new OptionalProgramCode();
        optionalProgramCode2.setOptionalProgramID(optionalProgramID2);
        optionalProgramCode2.setOptProgramCode("CP");
        optionalProgramCode2.setOptionalProgramName("Career Program");

        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.empty());
        when(restUtils.getOptionalProgramCodeList()).thenReturn(List.of(optionalProgramCode1, optionalProgramCode2));

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/optional-program-students/search/download")
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("111111111");
        assertThat(csvContent).contains("Anderson");
        assertThat(csvContent).contains("French Immersion");
        assertThat(csvContent).contains("222222222");
        assertThat(csvContent).contains("Brown");
        assertThat(csvContent).contains("Career Program");
        assertThat(csvContent).contains("Current");
        assertThat(csvContent).contains("Archived");
    }

    @Test
    void testGetStudentSearchReport_WithSearchCriteria_ShouldReturnFilteredRecords() throws Exception {
        final GrantedAuthority grantedAuthority = () -> "SCOPE_READ_GRAD_GRADUATION_STATUS";
        final SecurityMockMvcRequestPostProcessors.OidcLoginRequestPostProcessor mockAuthority = oidcLogin().authorities(grantedAuthority);

        var schoolID = UUID.randomUUID();

        var gradStudent = new GraduationStudentRecordEntity();
        gradStudent.setStudentID(UUID.randomUUID());
        gradStudent.setPen("555555555");
        gradStudent.setLegalFirstName("Alice");
        gradStudent.setLegalLastName("Williams");
        gradStudent.setLegalMiddleNames("Marie");
        gradStudent.setStudentStatus("CUR");
        gradStudent.setProgram("2018-EN");
        gradStudent.setStudentGrade("12");
        gradStudent.setGenderCode("F");
        gradStudent.setSchoolOfRecordId(schoolID);
        gradStudent.setDob(LocalDateTime.of(2005, 3, 20, 0, 0));
        gradStudent.setProgramCompletionDate(Date.valueOf(LocalDate.of(2023, 6, 1)));
        gradStudent.setRecalculateGradStatus("Y");
        gradStudent.setRecalculateProjectedGrad("N");
        graduationStudentRecordRepository.save(gradStudent);

        var school = createMockSchoolTombstone();
        school.setSchoolId(String.valueOf(schoolID));
        school.setMincode("11223344");
        school.setDisplayName("Williams High School");

        when(restUtils.getSchoolBySchoolID(any())).thenReturn(Optional.of(school));

        var searchCriteria = "[{\"condition\":\"AND\",\"searchCriteriaList\":[{\"key\":\"program\",\"operation\":\"eq\",\"value\":\"2018-EN\",\"valueType\":\"STRING\"}]}]";

        var resultActions = this.mockMvc.perform(
                        get(EducGradStudentApiConstants.BASE_URL_REPORT + "/students/search/download")
                                .param("searchCriteriaList", searchCriteria)
                                .with(mockAuthority))
                .andDo(print()).andExpect(status().isOk());

        String csvContent = resultActions.andReturn().getResponse().getContentAsString();
        assertThat(csvContent).isNotBlank();
        assertThat(csvContent).contains("PEN");
        assertThat(csvContent).contains("Student Status");
        assertThat(csvContent).contains("Surname");
        assertThat(csvContent).contains("Given Name");
        assertThat(csvContent).contains("Middle Name");
        assertThat(csvContent).contains("Birthdate");
        assertThat(csvContent).contains("Gender");
        assertThat(csvContent).contains("Grade");
        assertThat(csvContent).contains("Program");
        assertThat(csvContent).contains("Completion Date");
        assertThat(csvContent).contains("School of Record Code");
        assertThat(csvContent).contains("School of Record Name");
        assertThat(csvContent).contains("Recalculate Grad Status?");
        assertThat(csvContent).contains("Recalculate Projected Grad?");
        assertThat(csvContent).contains("555555555");
        assertThat(csvContent).contains("Current");
        assertThat(csvContent).contains("Williams");
        assertThat(csvContent).contains("Alice");
        assertThat(csvContent).contains("Marie");
        assertThat(csvContent).contains("2005-03-20");
        assertThat(csvContent).contains("F");
        assertThat(csvContent).contains("12");
        assertThat(csvContent).contains("2018-EN");
        assertThat(csvContent).contains("2023/06");
        assertThat(csvContent).contains("11223344");
        assertThat(csvContent).contains("Williams High School");
        assertThat(csvContent).contains("Y");
        assertThat(csvContent).contains("N");
    }

}
