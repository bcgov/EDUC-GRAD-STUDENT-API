package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration-test")
public class ReportGradStudentPaginationRepositoryTest {

    @Autowired
    private ReportGradStudentPaginationRepository reportGradStudentPaginationRepository;

    @Autowired
    private ReportGradStudentDataRepository reportGradStudentDataRepository;

    private ReportGradStudentDataEntity testStudent;

    @Before
    public void setUp() {
        testStudent = new ReportGradStudentDataEntity();
        testStudent.setGraduationStudentRecordId(UUID.randomUUID());
        testStudent.setSchoolOfRecordId(UUID.randomUUID());
        testStudent.setStudentStatus("CUR");
        testStudent.setPen("123456789");
        testStudent = reportGradStudentDataRepository.save(testStudent);
    }

    @After
    public void tearDown() {
        if (testStudent != null) {
            reportGradStudentDataRepository.delete(testStudent);
        }
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    public void testStreamAll_WithSpecification() {
        Specification<ReportGradStudentDataEntity> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("schoolOfRecordId"), testStudent.getSchoolOfRecordId());

        try (Stream<ReportGradStudentDataEntity> resultStream =
                     reportGradStudentPaginationRepository.streamAll(spec)) {
            assertNotNull(resultStream);
            List<ReportGradStudentDataEntity> results = resultStream.toList();
            assertEquals(1, results.size());
            assertEquals(testStudent.getGraduationStudentRecordId(), results.get(0).getGraduationStudentRecordId());
        }
    }
}
