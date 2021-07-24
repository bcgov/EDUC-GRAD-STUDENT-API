package ca.bc.gov.educ.api.gradstudent.transformer;

import ca.bc.gov.educ.api.gradstudent.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradstudent.entity.StudentOptionalProgramEntity;
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
public class GradStudentSpecialProgramTransformerTest {
    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    GradStudentSpecialProgramTransformer gradStudentSpecialProgramTransformer;

    @Test
    public void testTransformToDTO() {
        StudentOptionalProgram gradStudentSpecialProgram = new StudentOptionalProgram();
        gradStudentSpecialProgram.setId(UUID.randomUUID());
        gradStudentSpecialProgram.setStudentID(UUID.randomUUID());
        gradStudentSpecialProgram.setProgramCode("Special");
        gradStudentSpecialProgram.setSpecialProgramName("Special program");
        gradStudentSpecialProgram.setPen("123456789");

        StudentOptionalProgramEntity gradStudentSpecialProgramEntity = new StudentOptionalProgramEntity();
        gradStudentSpecialProgramEntity.setId(gradStudentSpecialProgram.getId());
        gradStudentSpecialProgramEntity.setStudentID(gradStudentSpecialProgram.getStudentID());
        gradStudentSpecialProgramEntity.setOptionalProgramID(new UUID(1, 1));

        Mockito.when(modelMapper.map(gradStudentSpecialProgramEntity, StudentOptionalProgram.class)).thenReturn(gradStudentSpecialProgram);
        var result = gradStudentSpecialProgramTransformer.transformToDTO(gradStudentSpecialProgramEntity);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentSpecialProgramEntity.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentSpecialProgramEntity.getStudentID());

    }

    @Test
    public void testTransformOptionalToDTO() {
        StudentOptionalProgram gradStudentSpecialProgram = new StudentOptionalProgram();
        gradStudentSpecialProgram.setId(UUID.randomUUID());
        gradStudentSpecialProgram.setStudentID(UUID.randomUUID());
        gradStudentSpecialProgram.setProgramCode("Special");
        gradStudentSpecialProgram.setSpecialProgramName("Special program");
        gradStudentSpecialProgram.setPen("123456789");

        StudentOptionalProgramEntity gradStudentSpecialProgramEntity = new StudentOptionalProgramEntity();
        gradStudentSpecialProgramEntity.setId(gradStudentSpecialProgram.getId());
        gradStudentSpecialProgramEntity.setStudentID(gradStudentSpecialProgram.getStudentID());

        Mockito.when(modelMapper.map(gradStudentSpecialProgramEntity, StudentOptionalProgram.class)).thenReturn(gradStudentSpecialProgram);
        var result = gradStudentSpecialProgramTransformer.transformToDTO(Optional.of(gradStudentSpecialProgramEntity));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentSpecialProgramEntity.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentSpecialProgramEntity.getStudentID());
    }

    @Test
    public void testTransformToEntity() {
        StudentOptionalProgram gradStudentSpecialProgram = new StudentOptionalProgram();
        gradStudentSpecialProgram.setId(UUID.randomUUID());
        gradStudentSpecialProgram.setStudentID(UUID.randomUUID());
        gradStudentSpecialProgram.setProgramCode("Special");
        gradStudentSpecialProgram.setSpecialProgramName("Special program");
        gradStudentSpecialProgram.setPen("123456789");

        StudentOptionalProgramEntity gradStudentSpecialProgramEntity = new StudentOptionalProgramEntity();
        gradStudentSpecialProgramEntity.setId(gradStudentSpecialProgram.getId());
        gradStudentSpecialProgramEntity.setStudentID(gradStudentSpecialProgram.getStudentID());

        Mockito.when(modelMapper.map(gradStudentSpecialProgram, StudentOptionalProgramEntity.class)).thenReturn(gradStudentSpecialProgramEntity);
        var result = gradStudentSpecialProgramTransformer.transformToEntity(gradStudentSpecialProgram);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentSpecialProgram.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentSpecialProgram.getStudentID());
    }
}
