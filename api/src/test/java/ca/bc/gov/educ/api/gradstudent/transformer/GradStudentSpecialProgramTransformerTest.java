package ca.bc.gov.educ.api.gradstudent.transformer;

import ca.bc.gov.educ.api.gradstudent.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.gradstudent.entity.GradStudentSpecialProgramEntity;
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
        GradStudentSpecialProgram gradStudentSpecialProgram = new GradStudentSpecialProgram();
        gradStudentSpecialProgram.setId(UUID.randomUUID());
        gradStudentSpecialProgram.setStudentID(UUID.randomUUID());
        gradStudentSpecialProgram.setProgramCode("Special");
        gradStudentSpecialProgram.setSpecialProgramName("Special program");
        gradStudentSpecialProgram.setPen("123456789");

        GradStudentSpecialProgramEntity gradStudentSpecialProgramEntity = new GradStudentSpecialProgramEntity();
        gradStudentSpecialProgramEntity.setId(gradStudentSpecialProgram.getId());
        gradStudentSpecialProgramEntity.setStudentID(gradStudentSpecialProgram.getStudentID());
        gradStudentSpecialProgramEntity.setProgramCode(gradStudentSpecialProgram.getSpecialProgramCode());

        Mockito.when(modelMapper.map(gradStudentSpecialProgramEntity, GradStudentSpecialProgram.class)).thenReturn(gradStudentSpecialProgram);
        var result = gradStudentSpecialProgramTransformer.transformToDTO(gradStudentSpecialProgramEntity);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentSpecialProgramEntity.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentSpecialProgramEntity.getStudentID());
        assertThat(result.getSpecialProgramCode()).isEqualTo(gradStudentSpecialProgramEntity.getProgramCode());

    }

    @Test
    public void testTransformOptionalToDTO() {
        GradStudentSpecialProgram gradStudentSpecialProgram = new GradStudentSpecialProgram();
        gradStudentSpecialProgram.setId(UUID.randomUUID());
        gradStudentSpecialProgram.setStudentID(UUID.randomUUID());
        gradStudentSpecialProgram.setProgramCode("Special");
        gradStudentSpecialProgram.setSpecialProgramName("Special program");
        gradStudentSpecialProgram.setPen("123456789");

        GradStudentSpecialProgramEntity gradStudentSpecialProgramEntity = new GradStudentSpecialProgramEntity();
        gradStudentSpecialProgramEntity.setId(gradStudentSpecialProgram.getId());
        gradStudentSpecialProgramEntity.setStudentID(gradStudentSpecialProgram.getStudentID());
        gradStudentSpecialProgramEntity.setProgramCode(gradStudentSpecialProgram.getSpecialProgramCode());

        Mockito.when(modelMapper.map(gradStudentSpecialProgramEntity, GradStudentSpecialProgram.class)).thenReturn(gradStudentSpecialProgram);
        var result = gradStudentSpecialProgramTransformer.transformToDTO(Optional.of(gradStudentSpecialProgramEntity));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentSpecialProgramEntity.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentSpecialProgramEntity.getStudentID());
        assertThat(result.getSpecialProgramCode()).isEqualTo(gradStudentSpecialProgramEntity.getProgramCode());
    }

    @Test
    public void testTransformToEntity() {
        GradStudentSpecialProgram gradStudentSpecialProgram = new GradStudentSpecialProgram();
        gradStudentSpecialProgram.setId(UUID.randomUUID());
        gradStudentSpecialProgram.setStudentID(UUID.randomUUID());
        gradStudentSpecialProgram.setProgramCode("Special");
        gradStudentSpecialProgram.setSpecialProgramName("Special program");
        gradStudentSpecialProgram.setPen("123456789");

        GradStudentSpecialProgramEntity gradStudentSpecialProgramEntity = new GradStudentSpecialProgramEntity();
        gradStudentSpecialProgramEntity.setId(gradStudentSpecialProgram.getId());
        gradStudentSpecialProgramEntity.setStudentID(gradStudentSpecialProgram.getStudentID());
        gradStudentSpecialProgramEntity.setProgramCode(gradStudentSpecialProgram.getSpecialProgramCode());

        Mockito.when(modelMapper.map(gradStudentSpecialProgram, GradStudentSpecialProgramEntity.class)).thenReturn(gradStudentSpecialProgramEntity);
        var result = gradStudentSpecialProgramTransformer.transformToEntity(gradStudentSpecialProgram);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentSpecialProgram.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentSpecialProgram.getStudentID());
        assertThat(result.getProgramCode()).isEqualTo(gradStudentSpecialProgram.getSpecialProgramCode());
    }
}
