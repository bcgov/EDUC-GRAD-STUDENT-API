package ca.bc.gov.educ.api.gradstudent.transformer;

import ca.bc.gov.educ.api.gradstudent.dto.GradStudentCareerProgram;
import ca.bc.gov.educ.api.gradstudent.entity.GradStudentCareerProgramEntity;
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
public class GradStudentCareerProgramTransformerTest {
    @Mock
    ModelMapper modelMapper;

    @InjectMocks
    GradStudentCareerProgramTransformer gradStudentCareerProgramTransformer;

    @Test
    public void testTransformToDTO() {
        GradStudentCareerProgram gradStudentCareerProgram = new GradStudentCareerProgram();
        gradStudentCareerProgram.setId(UUID.randomUUID());
        gradStudentCareerProgram.setStudentID(UUID.randomUUID());
        gradStudentCareerProgram.setCareerProgramCode("career");
        gradStudentCareerProgram.setCareerProgramName("career program");
        gradStudentCareerProgram.setPen("123456789");

        GradStudentCareerProgramEntity gradStudentCareerProgramEntity = new GradStudentCareerProgramEntity();
        gradStudentCareerProgramEntity.setId(gradStudentCareerProgram.getId());
        gradStudentCareerProgramEntity.setStudentID(gradStudentCareerProgram.getStudentID());
        gradStudentCareerProgramEntity.setCareerProgramCode(gradStudentCareerProgram.getCareerProgramCode());

        Mockito.when(modelMapper.map(gradStudentCareerProgramEntity, GradStudentCareerProgram.class)).thenReturn(gradStudentCareerProgram);
        var result = gradStudentCareerProgramTransformer.transformToDTO(gradStudentCareerProgramEntity);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentCareerProgramEntity.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentCareerProgramEntity.getStudentID());
        assertThat(result.getCareerProgramCode()).isEqualTo(gradStudentCareerProgramEntity.getCareerProgramCode());

    }

    @Test
    public void testTransformOptionalToDTO() {
        GradStudentCareerProgram gradStudentCareerProgram = new GradStudentCareerProgram();
        gradStudentCareerProgram.setId(UUID.randomUUID());
        gradStudentCareerProgram.setStudentID(UUID.randomUUID());
        gradStudentCareerProgram.setCareerProgramCode("career");
        gradStudentCareerProgram.setCareerProgramName("career program");
        gradStudentCareerProgram.setPen("123456789");

        GradStudentCareerProgramEntity gradStudentCareerProgramEntity = new GradStudentCareerProgramEntity();
        gradStudentCareerProgramEntity.setId(gradStudentCareerProgram.getId());
        gradStudentCareerProgramEntity.setStudentID(gradStudentCareerProgram.getStudentID());
        gradStudentCareerProgramEntity.setCareerProgramCode(gradStudentCareerProgram.getCareerProgramCode());

        Mockito.when(modelMapper.map(gradStudentCareerProgramEntity, GradStudentCareerProgram.class)).thenReturn(gradStudentCareerProgram);
        var result = gradStudentCareerProgramTransformer.transformToDTO(Optional.of(gradStudentCareerProgramEntity));
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentCareerProgramEntity.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentCareerProgramEntity.getStudentID());
        assertThat(result.getCareerProgramCode()).isEqualTo(gradStudentCareerProgramEntity.getCareerProgramCode());
    }

    @Test
    public void testTransformToEntity() {
        GradStudentCareerProgram gradStudentCareerProgram = new GradStudentCareerProgram();
        gradStudentCareerProgram.setId(UUID.randomUUID());
        gradStudentCareerProgram.setStudentID(UUID.randomUUID());
        gradStudentCareerProgram.setCareerProgramCode("career");
        gradStudentCareerProgram.setCareerProgramName("career program");
        gradStudentCareerProgram.setPen("123456789");

        GradStudentCareerProgramEntity gradStudentCareerProgramEntity = new GradStudentCareerProgramEntity();
        gradStudentCareerProgramEntity.setId(gradStudentCareerProgram.getId());
        gradStudentCareerProgramEntity.setStudentID(gradStudentCareerProgram.getStudentID());
        gradStudentCareerProgramEntity.setCareerProgramCode(gradStudentCareerProgram.getCareerProgramCode());

        Mockito.when(modelMapper.map(gradStudentCareerProgram, GradStudentCareerProgramEntity.class)).thenReturn(gradStudentCareerProgramEntity);
        var result = gradStudentCareerProgramTransformer.transformToEntity(gradStudentCareerProgram);
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(gradStudentCareerProgram.getId());
        assertThat(result.getStudentID()).isEqualTo(gradStudentCareerProgram.getStudentID());
        assertThat(result.getCareerProgramCode()).isEqualTo(gradStudentCareerProgram.getCareerProgramCode());
    }
}
