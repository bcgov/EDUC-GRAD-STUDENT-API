package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.service.EdwSnapshotService;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.ResponseHelper;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

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

        Mockito.when(edwSnapshotService.saveEdwGraduationSnapshot(snapshotRequest)).thenReturn(snapshotRequest);
        Mockito.when(responseHelper.GET(snapshotRequest)).thenReturn(ResponseEntity.ok().body(snapshotRequest));
        var result = edwSnapshotController
                .saveGradStatusForEDW(snapshotRequest);
        Mockito.verify(edwSnapshotService).saveEdwGraduationSnapshot(snapshotRequest);
        assertThat(result).isNotNull();
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
