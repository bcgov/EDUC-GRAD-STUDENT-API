package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchoolServiceTest {

  @InjectMocks
  private SchoolService schoolService;

  @Mock
  private EducGradStudentApiConstants constants;

  @Mock
  private RESTService restService;

  @Mock
  private JsonTransformer jsonTransformer;

  @Test
  void testGetSchoolByMincode_returnsSchool_whenValidMincode() {
    String mincode = "12345678";
    School expectedSchool = new School();
    expectedSchool.setMincode(mincode);

    List<Object> rawSchoolList = List.of(new Object());

    when(constants.getSchoolsByMincodeUrl()).thenReturn("http://mock-url/schools/%s");
    when(restService.get("http://mock-url/schools/12345678", List.class, null)).thenReturn(rawSchoolList);
    when(jsonTransformer.convertValue(any(), ArgumentMatchers.<TypeReference<School>>any())).thenReturn(expectedSchool);

    School result = schoolService.getSchoolByMincode(mincode);

    assertNotNull(result);
    assertEquals(mincode, result.getMincode());
    verify(restService).get("http://mock-url/schools/12345678", List.class, null);
    verify(jsonTransformer).convertValue(any(), ArgumentMatchers.<TypeReference<School>>any());
  }

  @Test
  void testGetSchoolByMincode_returnsNull_whenMincodeIsNull() {
    School result = schoolService.getSchoolByMincode(null);
    assertNull(result);
    verifyNoInteractions(restService, jsonTransformer);
  }

  @Test
  void testGetSchoolByMincode_returnsNull_whenNoSchoolFound() {
    when(restService.get(anyString(), eq(List.class), eq(null))).thenReturn(Collections.emptyList());
    when(constants.getSchoolsByMincodeUrl()).thenReturn("http://mock-url/schools/%s");

    School result = schoolService.getSchoolByMincode("99999999");

    assertNull(result);
    verify(restService).get("http://mock-url/schools/99999999", List.class, null);
    verifyNoInteractions(jsonTransformer);
  }
}
