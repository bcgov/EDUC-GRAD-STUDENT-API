package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
}
