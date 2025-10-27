package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.util.JsonTransformer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportGradStudentTransformerTest {

  @Mock
  private ModelMapper modelMapper;

  @Mock
  private JsonTransformer jsonTransformer;

  @InjectMocks
  private ReportGradStudentTransformer transformer;

  private ReportGradStudentDataEntity entity;

  @BeforeEach
  void setUp() {
    entity = new ReportGradStudentDataEntity();
    entity.setGraduationStudentRecordId(UUID.randomUUID());
    entity.setDistrictId(UUID.randomUUID().toString());

    ReportGradStudentData dto = new ReportGradStudentData();
    when(modelMapper.map(entity, ReportGradStudentData.class)).thenReturn(dto);

    TypeFactory typeFactory = TypeFactory.defaultInstance();
    when(jsonTransformer.getTypeFactory()).thenReturn(typeFactory);
  }

  private static Stream<Arguments> provideDistrictIds() {
    return Stream.of(
        Arguments.of(UUID.randomUUID().toString(), true),
        Arguments.of("invalid-uuid", false),
        Arguments.of(null, false)
    );
  }

  @ParameterizedTest
  @MethodSource("provideDistrictIds")
  void testTransformToDTO(String districtId, boolean isValidUUID) {
    entity.setDistrictId(districtId);
    List<ReportGradStudentData> result = transformer.transformToDTO(Collections.singletonList(entity));
    assertNotNull(result);
    if (isValidUUID) {
      assertNotNull(result.get(0).getDistrictId());
    } else {
      assertNull(result.get(0).getDistrictId());
    }
  }
}