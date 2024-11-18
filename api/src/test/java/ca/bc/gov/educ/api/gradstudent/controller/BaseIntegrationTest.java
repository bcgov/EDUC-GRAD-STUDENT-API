package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.EducGradStudentApiApplication;
import ca.bc.gov.educ.api.gradstudent.constant.StudentStatusCodes;
import ca.bc.gov.educ.api.gradstudent.model.dto.ArchiveStudentsSagaData;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentGradeCodeEntity;
import ca.bc.gov.educ.api.gradstudent.repository.StudentGradeCodeRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.constant.EventType.INITIATED;
import static ca.bc.gov.educ.api.gradstudent.constant.SagaEnum.ARCHIVE_STUDENTS_SAGA;
import static ca.bc.gov.educ.api.gradstudent.constant.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants.API_NAME;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EducGradStudentApiApplication.class})
@ActiveProfiles("test")
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

  protected ArchiveStudentsSagaData getArchiveStudentsSagaData() {
    return ArchiveStudentsSagaData.builder()
            .batchId(123456)
            .updateUser("TEST")
            .studentStatusCode(StudentStatusCodes.CURRENT.getCode())
            .build();
  }

  protected SagaEntity createMockSaga() {
    return SagaEntity.builder()
            .updateDate(LocalDateTime.now().minusMinutes(15))
            .createUser(API_NAME)
            .updateUser(API_NAME)
            .createDate(LocalDateTime.now().minusMinutes(15))
            .sagaName(ARCHIVE_STUDENTS_SAGA.toString())
            .status(IN_PROGRESS.toString())
            .sagaState(INITIATED.toString())
            .build();
  }

  protected GraduationStudentRecordEntity createMockGraduationStudentRecord() {
    GraduationStudentRecordEntity graduationStatusEntity = new GraduationStudentRecordEntity();
    graduationStatusEntity.setStudentID(UUID.randomUUID());
    graduationStatusEntity.setPen("123456789");
    graduationStatusEntity.setStudentStatus("CUR");
    return graduationStatusEntity;
  }
}
