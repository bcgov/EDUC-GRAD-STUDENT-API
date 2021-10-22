package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class GradStudentOptionalProgramTransformerTest {
    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    GradStudentOptionalProgramTransformer gradStudentOptionalProgramTransformer;

    @Test
    public void testTransformToDTO() {
        StudentOptionalProgram gradStudentOptionalProgram = new StudentOptionalProgram();
        gradStudentOptionalProgram.setId(UUID.randomUUID());
        gradStudentOptionalProgram.setStudentID(UUID.randomUUID());
        gradStudentOptionalProgram.setProgramCode("Optional");
        gradStudentOptionalProgram.setOptionalProgramName("Optional program");
        gradStudentOptionalProgram.setPen("123456789");

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgram.getId());
        gradStudentOptionalProgramEntity.setStudentID(gradStudentOptionalProgram.getStudentID());
        gradStudentOptionalProgramEntity.setOptionalProgramID(new UUID(1, 1));

        Mockito.when(modelMapper.map(gradStudentOptionalProgramEntity, StudentOptionalProgram.class)).thenReturn(gradStudentOptionalProgram);
        var result = gradStudentOptionalProgramTransformer.transformToDTO(gradStudentOptionalProgramEntity);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentOptionalProgramEntity.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentOptionalProgramEntity.getStudentID());

    }

    @Test
    public void testTransformOptionalToDTO() {
        StudentOptionalProgram gradStudentOptionalProgram = new StudentOptionalProgram();
        gradStudentOptionalProgram.setId(UUID.randomUUID());
        gradStudentOptionalProgram.setStudentID(UUID.randomUUID());
        gradStudentOptionalProgram.setProgramCode("Optional");
        gradStudentOptionalProgram.setOptionalProgramName("Optional program");
        gradStudentOptionalProgram.setPen("123456789");

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgram.getId());
        gradStudentOptionalProgramEntity.setStudentID(gradStudentOptionalProgram.getStudentID());

        Mockito.when(modelMapper.map(gradStudentOptionalProgramEntity, StudentOptionalProgram.class)).thenReturn(gradStudentOptionalProgram);
        var result = gradStudentOptionalProgramTransformer.transformToDTO(Optional.of(gradStudentOptionalProgramEntity));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentOptionalProgramEntity.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentOptionalProgramEntity.getStudentID());
    }

    @Test
    public void testTransformToEntity() {
        StudentOptionalProgram gradStudentOptionalProgram = new StudentOptionalProgram();
        gradStudentOptionalProgram.setId(UUID.randomUUID());
        gradStudentOptionalProgram.setStudentID(UUID.randomUUID());
        gradStudentOptionalProgram.setProgramCode("Optional");
        gradStudentOptionalProgram.setOptionalProgramName("Optional program");
        gradStudentOptionalProgram.setPen("123456789");

        StudentOptionalProgramEntity gradStudentOptionalProgramEntity = new StudentOptionalProgramEntity();
        gradStudentOptionalProgramEntity.setId(gradStudentOptionalProgram.getId());
        gradStudentOptionalProgramEntity.setStudentID(gradStudentOptionalProgram.getStudentID());

        Mockito.when(modelMapper.map(gradStudentOptionalProgram, StudentOptionalProgramEntity.class)).thenReturn(gradStudentOptionalProgramEntity);
        var result = gradStudentOptionalProgramTransformer.transformToEntity(gradStudentOptionalProgram);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentOptionalProgram.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentOptionalProgram.getStudentID());
    }
}
