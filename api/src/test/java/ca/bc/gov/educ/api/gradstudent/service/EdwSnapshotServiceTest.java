package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.EdwGraduationSnapshot;
import ca.bc.gov.educ.api.gradstudent.model.entity.EdwGraduationSnapshotEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.EDWGraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.EdwGraduationSnapshotRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EdwSnapshotServiceTest {

    @Autowired
    EdwSnapshotService edwSnapshotService;

    @Autowired
    EducGradStudentApiConstants constants;

    @Autowired
    EDWGraduationStatusTransformer edwGraduationStatusTransformer;

    @MockBean
    EdwGraduationSnapshotRepository edwGraduationSnapshotRepository;

    @MockBean
    GradValidation validation;

    @Autowired
    WebClient webClient;

    @Autowired
    FetchGradStatusSubscriber fetchGradStatusSubscriber;

    @Test
    public void testRetrieve() {
        Integer gradYear = 2023;
        String pen = "123456789";

        EdwGraduationSnapshotEntity entity = new EdwGraduationSnapshotEntity();
        entity.setGradYear(gradYear.longValue());
        entity.setPen(pen);
        entity.setGraduationFlag("N");

        when(edwGraduationSnapshotRepository.findByGradYearAndPen(gradYear, pen)).thenReturn(Optional.of(entity));

        var result = edwSnapshotService.retrieve(gradYear, pen);

        assertThat(result).isNotNull();
        assertThat(result.getPen()).isEqualTo(pen);
    }

    @Test
    public void testRetrieveAll() {
        Integer gradYear = 2023;
        String pen = "123456789";

        EdwGraduationSnapshotEntity entity = new EdwGraduationSnapshotEntity();
        entity.setGradYear(gradYear.longValue());
        entity.setPen(pen);
        entity.setGraduationFlag("N");

        when(edwGraduationSnapshotRepository.findByGradYear(gradYear)).thenReturn(List.of(entity));

        var results = edwSnapshotService.retrieveAll(gradYear);

        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getPen()).isEqualTo(pen);
    }

    @Test
    public void testRetrieveByPage() {
        Integer gradYear = 2023;
        String pen = "123456789";

        PageRequest pageRequest = PageRequest.of(1, 10);

        EdwGraduationSnapshotEntity entity = new EdwGraduationSnapshotEntity();
        entity.setGradYear(gradYear.longValue());
        entity.setPen(pen);
        entity.setGraduationFlag("N");

        Page<EdwGraduationSnapshotEntity> page = new PageImpl<>(List.of(entity));

        when(edwGraduationSnapshotRepository.findByGradYear(gradYear, pageRequest)).thenReturn(page);

        var results = edwSnapshotService.retrieveByPage(gradYear, pageRequest);

        assertThat(results).isNotNull();
        assertThat(results.get(0).getPen()).isEqualTo(pen);
    }

    @Test
    public void testCountAllByGradYear() {
        Integer gradYear = 2023;

        when(edwGraduationSnapshotRepository.countAllByGradYear(gradYear)).thenReturn(2);

        var result = edwSnapshotService.countAllByGradYear(gradYear);

        assertThat(result).isEqualTo(2);
    }

    @Test
    public void testSaveEdwGraduationStatusForNew() {
        Integer gradYear = 2023;
        String pen = "123456789";

        EdwGraduationSnapshot snapshotRequest = new EdwGraduationSnapshot();
        snapshotRequest.setGradYear(gradYear);
        snapshotRequest.setPen(pen);
        snapshotRequest.setGraduationFlag("N");

        EdwGraduationSnapshotEntity entity = new EdwGraduationSnapshotEntity();
        entity.setGradYear(gradYear.longValue());
        entity.setPen(pen);
        entity.setGraduationFlag("N");

        when(edwGraduationSnapshotRepository.findByGradYearAndPen(gradYear, pen)).thenReturn(Optional.empty());
        when(edwGraduationSnapshotRepository.saveAndFlush(any())).thenReturn(entity);
        var result = edwSnapshotService.saveEdwGraduationSnapshot(snapshotRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPen()).isEqualTo(pen);
    }

    @Test
    public void testSaveEdwGraduationStatusForUpdate() {
        Integer gradYear = 2023;
        String pen = "123456789";

        EdwGraduationSnapshot snapshotRequest = new EdwGraduationSnapshot();
        snapshotRequest.setGradYear(gradYear);
        snapshotRequest.setPen(pen);
        snapshotRequest.setGraduationFlag("N");

        EdwGraduationSnapshotEntity entity = new EdwGraduationSnapshotEntity();
        entity.setGradYear(gradYear.longValue());
        entity.setPen(pen);
        entity.setGraduationFlag("N");

        when(edwGraduationSnapshotRepository.findByGradYearAndPen(gradYear, pen)).thenReturn(Optional.of(entity));
        when(edwGraduationSnapshotRepository.saveAndFlush(entity)).thenReturn(entity);
        var result = edwSnapshotService.saveEdwGraduationSnapshot(snapshotRequest);

        assertThat(result).isNotNull();
        assertThat(result.getPen()).isEqualTo(pen);
    }

}
