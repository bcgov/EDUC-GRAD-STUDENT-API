package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentOptionalProgramPagination;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordPaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration-test")
public class StudentOptionalProgramPaginationTransformerTest {

    @Autowired
    private StudentOptionalProgramPaginationTransformer transformer;

    private StudentOptionalProgramPaginationEntity entity;
    private GraduationStudentRecordPaginationEntity studentEntity;

    @Before
    public void setUp() {
        studentEntity = new GraduationStudentRecordPaginationEntity();
        studentEntity.setStudentID(UUID.randomUUID());
        studentEntity.setPen("123456789");
        studentEntity.setLegalFirstName("John");
        studentEntity.setLegalLastName("Doe");
        studentEntity.setProgram("2018-EN");
        studentEntity.setStudentStatus("CUR");

        entity = new StudentOptionalProgramPaginationEntity();
        entity.setStudentOptionalProgramID(UUID.randomUUID());
        entity.setOptionalProgramID(UUID.randomUUID());
        entity.setCompletionDate(new Date());
        entity.setGraduationStudentRecordEntity(studentEntity);
    }

    @Test
    public void testTransformToDTO_Success() {
        // Act
        StudentOptionalProgramPagination result = transformer.transformToDTO(entity);

        // Assert
        assertNotNull(result);
        assertEquals(entity.getStudentOptionalProgramID(), result.getStudentOptionalProgramID());
        assertEquals(entity.getOptionalProgramID(), result.getOptionalProgramID());
        assertEquals(entity.getCompletionDate(), result.getCompletionDate());
        assertNotNull(result.getGradStudent());
        assertEquals(studentEntity.getStudentID(), result.getGradStudent().getStudentID());
        assertEquals(studentEntity.getPen(), result.getGradStudent().getPen());
        assertEquals(studentEntity.getLegalFirstName(), result.getGradStudent().getLegalFirstName());
        assertEquals(studentEntity.getLegalLastName(), result.getGradStudent().getLegalLastName());
    }

    @Test
    public void testTransformToDTO_WithNullCompletionDate() {
        // Arrange
        entity.setCompletionDate(null);

        // Act
        StudentOptionalProgramPagination result = transformer.transformToDTO(entity);

        // Assert
        assertNotNull(result);
        assertNull(result.getCompletionDate());
        assertNotNull(result.getGradStudent());
        assertEquals(studentEntity.getStudentID(), result.getGradStudent().getStudentID());
    }

    @Test
    public void testTransformToDTO_VerifyGraduationStudentRecordMapping() {

        // Act
        StudentOptionalProgramPagination result = transformer.transformToDTO(entity);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getGradStudent());
        assertEquals(studentEntity.getStudentID(), result.getGradStudent().getStudentID());
        assertEquals(studentEntity.getProgram(), result.getGradStudent().getProgram());
        assertEquals(studentEntity.getStudentStatus(), result.getGradStudent().getStudentStatus());
    }
}

