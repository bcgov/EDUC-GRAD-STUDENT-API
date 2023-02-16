package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.repository.ReportGradStudentDataRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class GradStudentReportServiceTest {

    @Autowired GradStudentReportService gradStudentReportService;
    @MockBean ReportGradStudentDataRepository reportGradStudentDataRepository;

    @Before
    public void setUp() {
        openMocks(this);
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testGetReportGradStudentData() {
        // ID
        UUID studentID = UUID.randomUUID();

        ReportGradStudentDataEntity reportGradStudentDataEntity = new ReportGradStudentDataEntity();
        reportGradStudentDataEntity.setGraduationStudentRecordId(studentID);
        reportGradStudentDataEntity.setFirstName("Jonh");

        when(reportGradStudentDataRepository.findReportGradStudentDataEntityByMincodeStartsWithOrderBySchoolNameAscLastNameAsc("005")).thenReturn(List.of(reportGradStudentDataEntity));
        var result = gradStudentReportService.getGradStudentDataByMincode("005");
        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(1);
    }

}
