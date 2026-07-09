package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import ca.bc.gov.educ.api.gradstudent.model.dto.EdwGraduationSnapshot;
import ca.bc.gov.educ.api.gradstudent.model.dto.SchoolClob;
import ca.bc.gov.educ.api.gradstudent.model.dto.SnapshotResponse;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.EdwGraduationSnapshotEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.EDWGraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.EdwGraduationSnapshotRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class EdwSnapshotServiceTest extends BaseIntegrationTest {

    @Autowired
    EdwSnapshotService edwSnapshotService;

    @Autowired
    EducGradStudentApiConstants constants;

    @Autowired
    EDWGraduationStatusTransformer edwGraduationStatusTransformer;

    @MockBean
    EdwGraduationSnapshotRepository edwGraduationSnapshotRepository;

    @MockBean
    GraduationStudentRecordRepository graduationStudentRecordRepository;

    @MockBean
    GradValidation validation;

    @MockBean
    @Qualifier("studentApiClient")
    WebClient webClient;

    @MockBean
    RESTService restService;

    @MockBean
    SchoolService schoolService;

    @MockBean
    FetchGradStatusSubscriber fetchGradStatusSubscriber;

    // NATS
    @MockBean
    NatsConnection natsConnection;
    @MockBean
    Publisher publisher;
    @MockBean
    Subscriber subscriber;

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

    @Test
    public void testGetSchoolsForSnapshotUsesGradYearWindow() {
        Integer gradYear = 2026;
        UUID schoolId = UUID.randomUUID();
        SchoolClob schoolClob = new SchoolClob();
        schoolClob.setMinCode("12345678");
        Date expectedStart = Date.from(LocalDate.of(2025, Month.SEPTEMBER, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date expectedEnd = Date.from(LocalDate.of(2026, Month.AUGUST, 31).atStartOfDay(ZoneId.systemDefault()).toInstant());

        when(graduationStudentRecordRepository.findEdwSnapshotSchoolOfRecordIds(expectedStart, expectedEnd)).thenReturn(List.of(schoolId));
        when(restService.get(anyString(), eq(SchoolClob.class), eq(webClient))).thenReturn(schoolClob);

        var result = edwSnapshotService.getEdwSnapshotSchools(gradYear);

        assertThat(result).containsExactly("12345678");
    }

    @Test
    public void testGetStudentsForSnapshotUsesGradYearWindow() {
        Integer gradYear = 2026;
        UUID schoolId = UUID.randomUUID();
        School school = new School();
        school.setSchoolId(schoolId.toString());
        SchoolClob schoolClob = new SchoolClob();
        schoolClob.setMinCode("12345678");
        GraduationStudentRecordEntity student = new GraduationStudentRecordEntity();
        student.setPen("123456789");
        student.setSchoolOfRecordId(schoolId);
        student.setStudentGrade("12");
        Date expectedStart = Date.from(LocalDate.of(2025, Month.SEPTEMBER, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date expectedEnd = Date.from(LocalDate.of(2026, Month.AUGUST, 31).atStartOfDay(ZoneId.systemDefault()).toInstant());

        when(schoolService.getSchoolByMincode("12345678")).thenReturn(school);
        when(graduationStudentRecordRepository.findEdwSnapshotStudentsBySchoolOfRecordId(schoolId, expectedStart, expectedEnd)).thenReturn(List.of(student));
        when(restService.get(anyString(), eq(SchoolClob.class), eq(webClient))).thenReturn(schoolClob);

        List<SnapshotResponse> result = edwSnapshotService.getEdwSnapshotStudents(gradYear, "12345678");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPen()).isEqualTo("123456789");
        assertThat(result.get(0).getSchoolOfRecord()).isEqualTo("12345678");
    }

}
