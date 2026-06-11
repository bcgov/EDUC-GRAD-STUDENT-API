package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.Condition;
import ca.bc.gov.educ.api.gradstudent.model.dto.FilterOperation;
import ca.bc.gov.educ.api.gradstudent.model.dto.Search;
import ca.bc.gov.educ.api.gradstudent.model.dto.SearchCriteria;
import ca.bc.gov.educ.api.gradstudent.model.dto.ValueType;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStudentSearchDataEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CSVReportServiceTest {

    @Mock
    private RestUtils restUtils;
    @Mock
    private GraduationStudentRecordRepository graduationStudentRecordRepository;
    @Mock
    private StudentCoursePaginationRepository studentCoursePaginationRepository;
    @Mock
    private StudentOptionalProgramPaginationRepository studentOptionalProgramPaginationRepository;
    @Mock
    private StudentOptionalProgramPaginationLeanRepository studentOptionalProgramPaginationLeanRepository;
    @Mock
    private GradStudentSearchService gradStudentSearchService;
    @Mock
    private ReportGradStudentSearchService reportGradStudentSearchService;
    @Mock
    private GradStudentSearchRepository gradStudentSearchRepository;
    @Mock
    private EntityManager entityManager;
    @Mock
    private ReportGradStudentPaginationRepository reportGradStudentPaginationRepository;

    @InjectMocks
    private CSVReportService csvReportService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void generateStudentSearchReportGradStudentDataStream_writesCsvRowUsingSchoolAtGradAndCompletionDateString() throws Exception {
        UUID schoolAtGradId = UUID.randomUUID();
        UUID schoolOfRecordId = UUID.randomUUID();

        ReportGradStudentDataEntity entity = new ReportGradStudentDataEntity();
        entity.setGraduationStudentRecordId(UUID.randomUUID());
        entity.setPen("123456789");
        entity.setLocalID("900148");
        entity.setLastName("DOE");
        entity.setFirstName("JANE");
        entity.setMiddleName("Q");
        entity.setDob("2008/11/20");
        entity.setStudentGrade("12");
        entity.setProgramCode("2023-EN");
        entity.setProgramCompletionDate("2025/06");
        entity.setSchoolAtGradId(schoolAtGradId);
        entity.setSchoolOfRecordId(schoolOfRecordId);
        entity.setHonorsStanding("Y");

        School school = new School();
        school.setDisplayName("Mount Baker Secondary");

        when(reportGradStudentSearchService.setSpecificationAndSortCriteria(any(), any(), any(), anyList())).thenReturn(null);
        when(reportGradStudentPaginationRepository.streamAll(null)).thenReturn(Stream.of(entity));
        when(restUtils.getSchoolBySchoolID(schoolAtGradId.toString())).thenReturn(Optional.of(school));

        MockHttpServletResponse response = new MockHttpServletResponse();

        csvReportService.generateStudentSearchReportGradStudentDataStream("", null, response);

        String csv = response.getContentAsString();
        String[] lines = csv.split("\\R");

        assertEquals("PEN,LOCAL_ID,LAST_NAME,FIRST_NAME,MIDDLE_NAME,BIRTH_DATE,GRADE,PROGRAM,PROGRAM_COMPLETION_DATE,SCHOOL_AT_GRADUATION,HONOURS_STANDING", lines[0]);
        assertEquals("123456789,900148,DOE,JANE,Q,2008/11/20,12,2023-EN,2025/06,Mount Baker Secondary,Yes", lines[1]);
        assertTrue(response.getHeader("Content-Disposition").contains("CurrentStudentsSearch-"));
        assertEquals("text/csv;charset=UTF-8", response.getContentType());
        verify(restUtils).getSchoolBySchoolID(schoolAtGradId.toString());
        verify(restUtils, never()).getSchoolBySchoolID(schoolOfRecordId.toString());
    }

    @Test
    void generateStudentSearchReportGradStudentDataStream_includesSchoolMincodeInFilenameWhenSchoolFilterPresent() throws Exception {
        UUID schoolAtGradId = UUID.randomUUID();
        UUID schoolOfRecordId = UUID.randomUUID();
        String searchCriteriaListJson = buildSchoolOfRecordSearchCriteriaListJson(schoolOfRecordId);

        ReportGradStudentDataEntity entity = new ReportGradStudentDataEntity();
        entity.setGraduationStudentRecordId(UUID.randomUUID());
        entity.setPen("123456789");
        entity.setLocalID("900148");
        entity.setLastName("DOE");
        entity.setFirstName("JANE");
        entity.setMiddleName("Q");
        entity.setDob("2008/11/20");
        entity.setStudentGrade("12");
        entity.setProgramCode("2023-EN");
        entity.setProgramCompletionDate("2025/06");
        entity.setSchoolAtGradId(schoolAtGradId);
        entity.setHonorsStanding("N");

        School schoolAtGrad = new School();
        schoolAtGrad.setDisplayName("Mount Baker Secondary");

        School schoolOfRecord = new School();
        schoolOfRecord.setMincode("12345678");

        when(reportGradStudentSearchService.setSpecificationAndSortCriteria(eq(""), eq(searchCriteriaListJson), any(), anyList())).thenReturn(null);
        when(reportGradStudentPaginationRepository.streamAll(null)).thenReturn(Stream.of(entity));
        when(restUtils.getSchoolBySchoolID(schoolAtGradId.toString())).thenReturn(Optional.of(schoolAtGrad));
        when(restUtils.getSchoolBySchoolID(schoolOfRecordId.toString())).thenReturn(Optional.of(schoolOfRecord));

        MockHttpServletResponse response = new MockHttpServletResponse();

        csvReportService.generateStudentSearchReportGradStudentDataStream("", searchCriteriaListJson, response);

        assertTrue(response.getHeader("Content-Disposition").contains("CurrentStudentsSearch-12345678-"));
        verify(reportGradStudentSearchService).setSpecificationAndSortCriteria(eq(""), eq(searchCriteriaListJson), any(), anyList());
        verify(restUtils).getSchoolBySchoolID(schoolOfRecordId.toString());
    }

    @Test
    void generateStudentSearchReportGradStudentDataStream_fallsBackToDefaultFilenameWhenSchoolLookupReturnsEmpty() throws Exception {
        UUID schoolAtGradId = UUID.randomUUID();
        UUID schoolOfRecordId = UUID.randomUUID();
        String searchCriteriaListJson = buildSchoolOfRecordSearchCriteriaListJson(schoolOfRecordId);

        ReportGradStudentDataEntity entity = new ReportGradStudentDataEntity();
        entity.setGraduationStudentRecordId(UUID.randomUUID());
        entity.setPen("123456789");
        entity.setLocalID("900148");
        entity.setLastName("DOE");
        entity.setFirstName("JANE");
        entity.setMiddleName("Q");
        entity.setDob("2008/11/20");
        entity.setStudentGrade("12");
        entity.setProgramCode("2023-EN");
        entity.setProgramCompletionDate("2025/06");
        entity.setSchoolAtGradId(schoolAtGradId);
        entity.setHonorsStanding("Y");

        School schoolAtGrad = new School();
        schoolAtGrad.setDisplayName("Mount Baker Secondary");

        when(reportGradStudentSearchService.setSpecificationAndSortCriteria(eq(""), eq(searchCriteriaListJson), any(), anyList())).thenReturn(null);
        when(reportGradStudentPaginationRepository.streamAll(null)).thenReturn(Stream.of(entity));
        when(restUtils.getSchoolBySchoolID(schoolAtGradId.toString())).thenReturn(Optional.of(schoolAtGrad));
        when(restUtils.getSchoolBySchoolID(schoolOfRecordId.toString())).thenReturn(Optional.empty());

        MockHttpServletResponse response = new MockHttpServletResponse();

        csvReportService.generateStudentSearchReportGradStudentDataStream("", searchCriteriaListJson, response);

        assertTrue(response.getHeader("Content-Disposition").contains("CurrentStudentsSearch-"));
        assertTrue(!response.getHeader("Content-Disposition").contains("CurrentStudentsSearch--"));
        verify(restUtils).getSchoolBySchoolID(schoolOfRecordId.toString());
    }

    @Test
    void generateStudentSearchReportGradStudentDataStream_fallsBackToDefaultFilenameWhenSearchCriteriaJsonIsInvalid() throws Exception {
        UUID schoolAtGradId = UUID.randomUUID();
        String searchCriteriaListJson = "not-json";

        ReportGradStudentDataEntity entity = new ReportGradStudentDataEntity();
        entity.setGraduationStudentRecordId(UUID.randomUUID());
        entity.setPen("123456789");
        entity.setLocalID("900148");
        entity.setLastName("DOE");
        entity.setFirstName("JANE");
        entity.setMiddleName("Q");
        entity.setDob("2008/11/20");
        entity.setStudentGrade("12");
        entity.setProgramCode("2023-EN");
        entity.setProgramCompletionDate("2025/06");
        entity.setSchoolAtGradId(schoolAtGradId);
        entity.setHonorsStanding("Y");

        School schoolAtGrad = new School();
        schoolAtGrad.setDisplayName("Mount Baker Secondary");

        when(reportGradStudentSearchService.setSpecificationAndSortCriteria(eq(""), eq(searchCriteriaListJson), any(), anyList())).thenReturn(null);
        when(reportGradStudentPaginationRepository.streamAll(null)).thenReturn(Stream.of(entity));
        when(restUtils.getSchoolBySchoolID(schoolAtGradId.toString())).thenReturn(Optional.of(schoolAtGrad));

        MockHttpServletResponse response = new MockHttpServletResponse();

        csvReportService.generateStudentSearchReportGradStudentDataStream("", searchCriteriaListJson, response);

        assertTrue(response.getHeader("Content-Disposition").contains("CurrentStudentsSearch-"));
        verify(restUtils, never()).getSchoolBySchoolID(eq("not-json"));
    }

    @Test
    void generateStudentSearchReportStream_writesExpectedCsvRow() throws Exception {
        UUID schoolOfRecordId = UUID.randomUUID();
        String searchCriteriaListJson = buildSchoolOfRecordSearchCriteriaListJson(schoolOfRecordId);

        GradStudentSearchDataEntity entity = new GradStudentSearchDataEntity();
        entity.setStudentID(UUID.randomUUID());
        entity.setPen("123456789");
        entity.setLegalLastName("DOE");
        entity.setLegalFirstName("JANE");
        entity.setLegalMiddleNames("Q");
        entity.setDob(LocalDateTime.of(2008, 11, 20, 0, 0));
        entity.setGenderCode("F");
        entity.setStudentGrade("12");
        entity.setProgram("2023-EN");
        entity.setProgramCompletionDate(Date.from(LocalDateTime.of(2025, 6, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant()));
        entity.setSchoolOfRecordId(schoolOfRecordId);
        entity.setStudentStatus("CUR");
        entity.setRecalculateGradStatus("Y");
        entity.setRecalculateProjectedGrad("N");

        School schoolOfRecord = new School();
        schoolOfRecord.setMincode("12345678");
        schoolOfRecord.setDisplayName("Mount Baker Secondary");

        when(gradStudentSearchService.setSpecificationAndSortCriteria(eq(""), eq(searchCriteriaListJson), any(), anyList())).thenReturn(null);
        when(gradStudentSearchRepository.streamAll(null)).thenReturn(Stream.of(entity));
        when(restUtils.getSchoolBySchoolID(schoolOfRecordId.toString())).thenReturn(Optional.of(schoolOfRecord));

        MockHttpServletResponse response = new MockHttpServletResponse();

        csvReportService.generateStudentSearchReportStream(searchCriteriaListJson, response);

        String csv = response.getContentAsString();
        String[] lines = csv.split("\\R");

        assertEquals("PEN,STUDENT STATUS,SURNAME,GIVEN NAME,MIDDLE NAME,BIRTHDATE,GENDER,GRADE,PROGRAM,COMPLETION DATE,SCHOOL OF RECORD CODE,SCHOOL OF RECORD NAME,RECALCULATE GRAD STATUS?,RECALCULATE PROJECTED GRAD?", lines[0].toUpperCase());
        assertEquals("123456789,Current,DOE,JANE,Q,2008-11-20,F,12,2023-EN,2025/06,12345678,Mount Baker Secondary,Y,N", lines[1]);
        assertTrue(response.getHeader("Content-Disposition").contains("StudentSearch-"));
        verify(gradStudentSearchService).setSpecificationAndSortCriteria(eq(""), eq(searchCriteriaListJson), any(), anyList());
        verify(restUtils).getSchoolBySchoolID(schoolOfRecordId.toString());
    }

    @Test
    void generateProgramStudentSearchReportStream_writesExpectedCsvRow() throws Exception {
        UUID schoolOfRecordId = UUID.randomUUID();
        UUID schoolAtGradId = UUID.randomUUID();
        String searchCriteriaListJson = buildSchoolOfRecordSearchCriteriaListJson(schoolOfRecordId);

        GradStudentSearchDataEntity entity = new GradStudentSearchDataEntity();
        entity.setStudentID(UUID.randomUUID());
        entity.setPen("123456789");
        entity.setStudentStatus("CUR");
        entity.setLegalLastName("DOE");
        entity.setLegalFirstName("JANE");
        entity.setLegalMiddleNames("Q");
        entity.setDob(LocalDateTime.of(2008, 11, 20, 0, 0));
        entity.setStudentGrade("12");
        entity.setProgram("2023-EN");
        entity.setProgramCompletionDate(Date.from(LocalDateTime.of(2025, 6, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant()));
        entity.setSchoolOfRecordId(schoolOfRecordId);
        entity.setSchoolAtGraduationId(schoolAtGradId);
        entity.setAdultStartDate(Date.from(LocalDateTime.of(2024, 9, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant()));

        School schoolOfRecord = new School();
        schoolOfRecord.setMincode("12345678");
        schoolOfRecord.setDisplayName("Mount Baker Secondary");

        School schoolAtGrad = new School();
        schoolAtGrad.setMincode("87654321");
        schoolAtGrad.setDisplayName("Grad School");

        when(gradStudentSearchService.setSpecificationAndSortCriteria(eq(""), eq(searchCriteriaListJson), any(), anyList())).thenReturn(null);
        when(gradStudentSearchRepository.streamAll(null)).thenReturn(Stream.of(entity));
        when(restUtils.getSchoolBySchoolID(schoolOfRecordId.toString())).thenReturn(Optional.of(schoolOfRecord));
        when(restUtils.getSchoolBySchoolID(schoolAtGradId.toString())).thenReturn(Optional.of(schoolAtGrad));

        MockHttpServletResponse response = new MockHttpServletResponse();

        csvReportService.generateProgramStudentSearchReportStream(searchCriteriaListJson, response);

        String csv = response.getContentAsString();
        String[] lines = csv.split("\\R");

        assertEquals("PEN,STUDENT STATUS,SURNAME,GIVEN NAME,MIDDLE NAME,BIRTHDATE,GRADE,PROGRAM,COMPLETION DATE,SCHOOL OF RECORD CODE,SCHOOL OF RECORD NAME,SCHOOL OF GRADUATION CODE,SCHOOL OF GRADUATION NAME,ADULT START DATE", lines[0].toUpperCase());
        assertEquals("123456789,Current,DOE,JANE,Q,2008-11-20,12,2023-EN,2025-06-01,12345678,Mount Baker Secondary,87654321,Grad School,2024-09-01", lines[1]);
        assertTrue(response.getHeader("Content-Disposition").contains("StudentProgramSearch-"));
        verify(gradStudentSearchService).setSpecificationAndSortCriteria(eq(""), eq(searchCriteriaListJson), any(), anyList());
        verify(restUtils).getSchoolBySchoolID(schoolOfRecordId.toString());
        verify(restUtils).getSchoolBySchoolID(schoolAtGradId.toString());
    }

    private String buildSchoolOfRecordSearchCriteriaListJson(UUID schoolOfRecordId) throws Exception {
        SearchCriteria schoolCriteria = SearchCriteria.builder()
                .key("schoolOfRecordId")
                .operation(FilterOperation.EQUAL)
                .value(schoolOfRecordId.toString())
                .valueType(ValueType.UUID)
                .condition(Condition.AND)
                .build();

        Search search = Search.builder()
                .condition(null)
                .searchCriteriaList(java.util.List.of(schoolCriteria))
                .build();

        return objectMapper.writeValueAsString(java.util.List.of(search));
    }
}
