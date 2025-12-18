package ca.bc.gov.educ.api.gradstudent.service.event;

import ca.bc.gov.educ.api.gradstudent.model.dto.external.student.v1.StudentUpdate;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@RunWith(SpringRunner.class)
@ActiveProfiles("integration-test")
@SpringBootTest
class GraduationStudentRecordServiceTest {

    @Autowired
    private GraduationStudentRecordRepository graduationStudentRecordRepository;

    @Autowired
    private GraduationStudentRecordService graduationStudentRecordService;

    @Test
    @Transactional
    void handleStudentUpdated() {
        UUID studentID = UUID.randomUUID();
        StudentUpdate studentUpdate = StudentUpdate.builder()
                .pen("321654987")
                .genderCode("F")
                .legalFirstName("Danielle")
                .build();
        GraduationStudentRecordEntity graduationStudentRecordEntity = GraduationStudentRecordEntity.builder()
                .studentID(studentID)
                .studentStatus("CUR")
                .build();
        GradStatusEvent event = GradStatusEvent.builder()
                .updateUser("Test")
                .build();
        graduationStudentRecordService.handleStudentUpdated(studentUpdate, graduationStudentRecordEntity, event);
        Optional<GraduationStudentRecordEntity> savedEntity = graduationStudentRecordRepository.findById(studentID);
        savedEntity.ifPresent(studentRecordEntity -> assertEquals(graduationStudentRecordEntity.getStudentID(), studentRecordEntity.getStudentID()));
    }
}