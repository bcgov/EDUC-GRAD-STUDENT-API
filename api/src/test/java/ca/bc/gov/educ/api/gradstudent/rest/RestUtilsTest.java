package ca.bc.gov.educ.api.gradstudent.rest;

import ca.bc.gov.educ.api.gradstudent.exception.GradStudentAPIRuntimeException;
import ca.bc.gov.educ.api.gradstudent.messaging.MessagePublisher;
import ca.bc.gov.educ.api.gradstudent.model.dto.LetterGrade;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CourseAllowableCreditRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CourseCharacteristicsRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CourseCodeRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.GraduationProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.OptionalProgramCode;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static ca.bc.gov.educ.api.gradstudent.rest.RestUtils.NATS_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RestUtilsTest {
    @Mock
    private WebClient webClient;


    @Mock
    private MessagePublisher messagePublisher;

    @InjectMocks
    private RestUtils restUtils;

    @Mock
    private EducGradStudentApiConstants constants;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restUtils = spy(new RestUtils(messagePublisher, webClient, constants));
    }


    @Test
    void testGetCoursesByExternalID_WhenRequestTimesOut_ShouldThrowGradDataCollectionAPIRuntimeException() {
        UUID correlationID = UUID.randomUUID();
        String externalID = "YPR  0B";

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(null));

        GradStudentAPIRuntimeException exception = assertThrows(
                GradStudentAPIRuntimeException.class,
                () -> restUtils.getCoursesByExternalID(correlationID, externalID)
        );

        assertEquals(NATS_TIMEOUT + correlationID, exception.getMessage());
    }

    @Test
    void testGetCoursesByExternalID_WhenExceptionOccurs_ShouldThrowGradDataCollectionAPIRuntimeException() {
        UUID correlationID = UUID.randomUUID();
        String externalID = "YPR  0B";
        Exception mockException = new Exception("exception");

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.failedFuture(mockException));

        assertThrows(
                GradStudentAPIRuntimeException.class,
                () -> restUtils.getCoursesByExternalID(correlationID, externalID)
        );
    }

    @Test
    void testGetCoursesByExternalID_WhenValidExternalID_ShouldReturnCoregCoursesRecord() {
        UUID correlationID = UUID.randomUUID();
        String externalID = "YPR  0B";

        CoregCoursesRecord expectedRecord = new CoregCoursesRecord(
                "123456",
                "77",
                "BA PARKS AND RECREATION 10B",
                "2009-09-01T00:00:00",
                null,
                null,
                "G",
                null,
                "N",
                Set.of(new CourseCodeRecord("123456", "YPR 10B", "39"), new CourseCodeRecord("123456", "YPR--0B", "38")),
                new CourseCharacteristicsRecord("2818", "123456", "LANGTYP", "ENG", "English"),
                new CourseCharacteristicsRecord("2933", "123456", "CC", "BA", "Board Authority Authorized"),
                Set.of(
                        new CourseAllowableCreditRecord("2169728", "4", "123456", "2009-09-01 00:00:00", null),
                        new CourseAllowableCreditRecord("2169729", "3", "123456", "2009-09-01 00:00:00", null)
                ),
                null
        );

        String jsonResponse = """
        {
            "courseID": "123456",
            "sifSubjectCode": "77",
            "courseTitle": "BA PARKS AND RECREATION 10B",
            "startDate": "2009-09-01T00:00:00",
            "genericCourseType": "G",
            "externalIndicator": "N",
            "courseCode": [
                {"courseID": "123456", "externalCode": "YPR 10B", "originatingSystem": "39"},
                {"courseID": "123456", "externalCode": "YPR--0B", "originatingSystem": "38"}
            ],
            "courseCharacteristics": {"id": "2818", "courseID": "123456", "type": "LANGTYP", "code": "ENG", "description": "English"},
            "courseCategory": {"id": "2933", "courseID": "123456", "type": "CC", "code": "BA", "description": "Board Authority Authorized"},
            "courseAllowableCredit": [
                {"cacID": "2169728", "creditValue": "4", "courseID": "123456", "startDate": "2009-09-01 00:00:00", "endDate": null},
                {"cacID": "2169729", "creditValue": "3", "courseID": "123456", "startDate": "2009-09-01 00:00:00", "endDate": null}
            ]
        }
         """;
        byte[] mockResponseData = jsonResponse.getBytes(StandardCharsets.UTF_8);

        io.nats.client.Message mockMessage = mock(io.nats.client.Message.class);
        when(mockMessage.getData()).thenReturn(mockResponseData);

        when(messagePublisher.requestMessage(anyString(), any(byte[].class)))
                .thenReturn(CompletableFuture.completedFuture(mockMessage));

        CoregCoursesRecord actualRecord = restUtils.getCoursesByExternalID(correlationID, externalID);

        assertEquals(expectedRecord, actualRecord);
    }

    @Test
    void testPopulateOptionalProgramMap() {
        List<OptionalProgramCode> mockOptionalPrograms = List.of(
                new OptionalProgramCode(UUID.randomUUID(), "FR", "SCCP French Certificate", "", 1, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString()),
                new OptionalProgramCode(UUID.randomUUID(), "AD", "Advanced Placement", "", 2, "2020-01-01T00:00:00", "2099-12-31T23:59:59", "", "", "unitTests", LocalDateTime.now().toString(), "unitTests", LocalDateTime.now().toString())
        );

        doReturn(mockOptionalPrograms).when(restUtils).getOptionalProgramCodeList();

        restUtils.populateOptionalProgramsMap();

        assertEquals(2, restUtils.getOptionalProgramCodeList().size());
        assertEquals("SCCP French Certificate", restUtils.getOptionalProgramCodeList().get(0).getOptionalProgramName());
    }

    @Test
    void testPopulateGradProgramCodesMap() {
        List<GraduationProgramCode> mockGradPrograms = List.of(
                new GraduationProgramCode("1950", "Adult Graduation Program", "Description for 1950", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred"),
                new GraduationProgramCode("2023", "B.C. Graduation Program", "Description for 2023", 4, LocalDate.now().toString(), LocalDate.now().minusYears(2).toString(), "associatedCred")
        );

        doReturn(mockGradPrograms).when(restUtils).getGraduationProgramCodeList(false);

        restUtils.populateGradProgramCodesMap();

        assertEquals(2, restUtils.getGraduationProgramCodeList(false).size());
        assertEquals("Adult Graduation Program", restUtils.getGraduationProgramCodeList(false).get(0).getProgramName());
    }
}
