package ca.bc.gov.educ.api.gradstudent.model.mapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.HistoricStudentActivity;
import ca.bc.gov.educ.api.gradstudent.model.entity.HistoricStudentActivityEntity;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class HistoricStudentActivityMapperTest {

    private final HistoricStudentActivityMapper mapper = Mappers.getMapper(HistoricStudentActivityMapper.class);

    @Test
    public void testToStructure_mapsAllFieldsCorrectly() {
        // Given
        HistoricStudentActivityEntity entity = createHistoricStudentActivityEntity();

        // When
        HistoricStudentActivity dto = mapper.toStructure(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getHistoricStudentActivityID()).isEqualTo(entity.getHistoricStudentActivityID());
        assertThat(dto.getGraduationStudentRecordID()).isEqualTo(entity.getGraduationStudentRecordID());
        assertThat(dto.getDate()).isEqualTo(entity.getDate());
        assertThat(dto.getType()).isEqualTo(entity.getType());
        assertThat(dto.getProgram()).isEqualTo(entity.getProgram());
        assertThat(dto.getUserID()).isEqualTo(entity.getUserID());
        assertThat(dto.getBatch()).isEqualTo(entity.getBatch());
        assertThat(dto.getSeqNo()).isEqualTo(entity.getSeqNo());
        assertThat(dto.getCreateUser()).isEqualTo(entity.getCreateUser());
        assertThat(dto.getCreateDate()).isEqualTo(entity.getCreateDate());
        assertThat(dto.getUpdateUser()).isEqualTo(entity.getUpdateUser());
        assertThat(dto.getUpdateDate()).isEqualTo(entity.getUpdateDate());
    }

    @Test
    public void testToStructure_withNullEntity_returnsNull() {
        // When
        HistoricStudentActivity dto = mapper.toStructure(null);

        // Then
        assertThat(dto).isNull();
    }

    @Test
    public void testToStructure_withNullFields_handlesGracefully() {
        // Given
        HistoricStudentActivityEntity entity = new HistoricStudentActivityEntity();
        entity.setHistoricStudentActivityID(UUID.randomUUID());
        // All other fields are null

        // When
        HistoricStudentActivity dto = mapper.toStructure(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getHistoricStudentActivityID()).isEqualTo(entity.getHistoricStudentActivityID());
        assertThat(dto.getGraduationStudentRecordID()).isNull();
        assertThat(dto.getDate()).isNull();
        assertThat(dto.getType()).isNull();
        assertThat(dto.getProgram()).isNull();
        assertThat(dto.getUserID()).isNull();
        assertThat(dto.getBatch()).isNull();
        assertThat(dto.getSeqNo()).isNull();
        assertThat(dto.getCreateUser()).isNull();
        assertThat(dto.getCreateDate()).isNotNull();
        assertThat(dto.getUpdateUser()).isNull();
        assertThat(dto.getUpdateDate()).isNotNull();
    }

    @Test
    public void testToStructure_withEmptyStrings_mapsCorrectly() {
        // Given
        HistoricStudentActivityEntity entity = new HistoricStudentActivityEntity();
        entity.setHistoricStudentActivityID(UUID.randomUUID());
        entity.setGraduationStudentRecordID(UUID.randomUUID());
        entity.setDate(LocalDateTime.now());
        entity.setType("");
        entity.setProgram("");
        entity.setUserID("");
        entity.setBatch("");
        entity.setSeqNo("");
        entity.setCreateUser("");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateUser("");
        entity.setUpdateDate(LocalDateTime.now());

        // When
        HistoricStudentActivity dto = mapper.toStructure(entity);

        // Then
        assertThat(dto).isNotNull();
        assertThat(dto.getType()).isEmpty();
        assertThat(dto.getProgram()).isEmpty();
        assertThat(dto.getUserID()).isEmpty();
        assertThat(dto.getBatch()).isEmpty();
        assertThat(dto.getSeqNo()).isEmpty();
        assertThat(dto.getCreateUser()).isEmpty();
        assertThat(dto.getUpdateUser()).isEmpty();
    }

    private HistoricStudentActivityEntity createHistoricStudentActivityEntity() {
        HistoricStudentActivityEntity entity = new HistoricStudentActivityEntity();
        entity.setHistoricStudentActivityID(UUID.randomUUID());
        entity.setGraduationStudentRecordID(UUID.randomUUID());
        entity.setDate(LocalDateTime.now());
        entity.setType("ADD");
        entity.setProgram("2023");
        entity.setUserID("USER123");
        entity.setBatch("001");
        entity.setSeqNo("0001");
        entity.setCreateUser("TEST_USER");
        entity.setCreateDate(LocalDateTime.now());
        entity.setUpdateUser("TEST_USER");
        entity.setUpdateDate(LocalDateTime.now());
        return entity;
    }
}

