package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordPaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationEntity;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration-test")
public class StudentOptionalProgramPaginationRepositoryTest {

    @Autowired
    private StudentOptionalProgramPaginationRepository studentOptionalProgramPaginationRepository;

    @Autowired
    private GradStudentPaginationRepository gradStudentPaginationRepository;

    private GraduationStudentRecordPaginationEntity testStudent;
    private StudentOptionalProgramPaginationEntity testOptionalProgram;

    @Before
    public void setUp() {
        testStudent = new GraduationStudentRecordPaginationEntity();
        testStudent.setStudentID(UUID.randomUUID());
        testStudent.setProgram("2018-EN");
        testStudent.setStudentStatus("CUR");
        testStudent = gradStudentPaginationRepository.save(testStudent);

        testOptionalProgram = new StudentOptionalProgramPaginationEntity();
        testOptionalProgram.setStudentOptionalProgramID(UUID.randomUUID());
        testOptionalProgram.setOptionalProgramID(UUID.randomUUID());
        testOptionalProgram.setCompletionDate(new Date());
        testOptionalProgram.setGraduationStudentRecordEntity(testStudent);
        testOptionalProgram = studentOptionalProgramPaginationRepository.save(testOptionalProgram);
    }

    @After
    public void tearDown() {
        if (testOptionalProgram != null) {
            studentOptionalProgramPaginationRepository.delete(testOptionalProgram);
        }
        if (testStudent != null) {
            gradStudentPaginationRepository.delete(testStudent);
        }
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    public void testStreamAll_WithNullSpecification() {
        // Act & Assert
        try (Stream<StudentOptionalProgramPaginationEntity> resultStream =
                studentOptionalProgramPaginationRepository.streamAll(null)) {
            assertNotNull(resultStream);
            List<StudentOptionalProgramPaginationEntity> results = resultStream.toList();
            assertFalse(results.isEmpty());
        }
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    public void testStreamAll_WithSpecification() {
        // Arrange
        Specification<StudentOptionalProgramPaginationEntity> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("optionalProgramID"), testOptionalProgram.getOptionalProgramID());

        // Act & Assert
        try (Stream<StudentOptionalProgramPaginationEntity> resultStream =
                studentOptionalProgramPaginationRepository.streamAll(spec)) {
            assertNotNull(resultStream);
            List<StudentOptionalProgramPaginationEntity> results = resultStream.toList();
            assertEquals(1, results.size());
            assertEquals(testOptionalProgram.getStudentOptionalProgramID(), results.get(0).getStudentOptionalProgramID());
        }
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    public void testStreamAll_WithSpecification_NoResults() {
        // Arrange
        UUID nonExistentOptionalProgramID = UUID.randomUUID();
        Specification<StudentOptionalProgramPaginationEntity> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("optionalProgramID"), nonExistentOptionalProgramID);

        // Act & Assert
        try (Stream<StudentOptionalProgramPaginationEntity> resultStream =
                studentOptionalProgramPaginationRepository.streamAll(spec)) {
            assertNotNull(resultStream);
            List<StudentOptionalProgramPaginationEntity> results = resultStream.toList();
            assertEquals(0, results.size());
        }
    }

    @Test
    @org.springframework.transaction.annotation.Transactional
    public void testStreamAll_WithStudentFilter() {
        // Arrange
        Specification<StudentOptionalProgramPaginationEntity> spec = (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("graduationStudentRecordEntity").get("studentID"), testStudent.getStudentID());

        // Act & Assert
        try (Stream<StudentOptionalProgramPaginationEntity> resultStream =
                studentOptionalProgramPaginationRepository.streamAll(spec)) {
            assertNotNull(resultStream);
            List<StudentOptionalProgramPaginationEntity> results = resultStream.toList();
            assertEquals(1, results.size());
            assertEquals(testStudent.getStudentID(), results.get(0).getGraduationStudentRecordEntity().getStudentID());
        }
    }
}

