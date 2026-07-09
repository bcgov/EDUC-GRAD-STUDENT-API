package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.model.dto.EdwGraduationSnapshot;
import ca.bc.gov.educ.api.gradstudent.model.dto.SnapshotResponse;
import ca.bc.gov.educ.api.gradstudent.service.EdwSnapshotService;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.ResponseHelper;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class EdwSnapshotControllerTest {

    @Mock
    private EdwSnapshotService edwSnapshotService;

    @Mock
    ResponseHelper responseHelper;

    @Mock
    GradValidation validation;

    @Mock
    Publisher publisher;

    @InjectMocks
    private EdwSnapshotController edwSnapshotController;

    @Test
    public void testSaveGradStatusForEDW() {
        Integer gradYear = 2023;
        String pen = "123456789";

        EdwGraduationSnapshot snapshotRequest = new EdwGraduationSnapshot();
        snapshotRequest.setGradYear(gradYear);
        snapshotRequest.setPen(pen);
        snapshotRequest.setGraduationFlag("N");

        when(edwSnapshotService.saveEdwGraduationSnapshot(snapshotRequest)).thenReturn(snapshotRequest);
        when(responseHelper.GET(snapshotRequest)).thenReturn(ResponseEntity.ok().body(snapshotRequest));
        var result = edwSnapshotController
                .saveGradStatusForEDW(snapshotRequest);
        verify(edwSnapshotService).saveEdwGraduationSnapshot(snapshotRequest);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testGetSchoolsForEDWSnapshot() {
        Integer gradYear = 2026;
        List<String> schools = List.of("12345678", "87654321");

        when(edwSnapshotService.getEdwSnapshotSchools(gradYear)).thenReturn(schools);
        when(responseHelper.GET(schools)).thenReturn(ResponseEntity.ok().body(schools));

        var result = edwSnapshotController.getSchoolsForEDWSnapshot(gradYear);

        verify(edwSnapshotService).getEdwSnapshotSchools(gradYear);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).containsExactlyElementsOf(schools);
    }

    @Test
    public void testGetStudentsForEDWSnapshot() {
        Integer gradYear = 2026;
        String minCode = "12345678";
        SnapshotResponse snapshotResponse = new SnapshotResponse();
        snapshotResponse.setPen("123456789");
        snapshotResponse.setSchoolOfRecord(minCode);

        List<SnapshotResponse> students = List.of(snapshotResponse);
        when(edwSnapshotService.getEdwSnapshotStudents(gradYear, minCode)).thenReturn(students);
        when(responseHelper.GET(students)).thenReturn(ResponseEntity.ok().body(students));

        var result = edwSnapshotController.getStudentsForEDWSnapshot(gradYear, minCode);

        verify(edwSnapshotService).getEdwSnapshotStudents(gradYear, minCode);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).hasSize(1);
    }
}
