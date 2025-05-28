package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.EducGradStudentApiApplication;
import ca.bc.gov.educ.api.gradstudent.model.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentGradeCodeEntity;
import ca.bc.gov.educ.api.gradstudent.repository.StudentGradeCodeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = {EducGradStudentApiApplication.class})
@ActiveProfiles("integration-test")
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {
  
  @Autowired
  StudentGradeCodeRepository studentGradeCodeRepository;

  @BeforeEach
  public void before() {
    studentGradeCodeRepository.saveAll(studentGradeCodeData());
  }

  @AfterEach
  public void resetState() {
    studentGradeCodeRepository.deleteAll();
  }

  public List<StudentGradeCodeEntity> studentGradeCodeData() {
    List<StudentGradeCodeEntity> entities = new ArrayList<>();
    entities.add(StudentGradeCodeEntity.builder().studentGradeCode("07").description("Grade 7").label("Grade 7").effectiveDate(LocalDateTime.now()).expected("N").displayOrder(1).build());

    entities.add(StudentGradeCodeEntity.builder().studentGradeCode("08").description("Grade 8").label("Grade 8").effectiveDate(LocalDateTime.now()).expected("Y").displayOrder(2).build());

    entities.add(StudentGradeCodeEntity.builder().studentGradeCode("09").description("Grade 9").label("Grade 9").effectiveDate(LocalDateTime.now()).expected("Y").displayOrder(3).build());

    entities.add(StudentGradeCodeEntity.builder().studentGradeCode("10").description("Grade 10").label("Grade 10").effectiveDate(LocalDateTime.now()).expected("Y").displayOrder(4).build());

    return entities;
  }

  public GradSearchStudent createMockGradSearchStudent() {
    return GradSearchStudent.builder()
        .studentID(UUID.randomUUID().toString())
        .gradeCode("12")
        .statusCode("A")
        .program("NCSF")
        .schoolOfRecordId(UUID.randomUUID().toString())
        .build();
  }
}
