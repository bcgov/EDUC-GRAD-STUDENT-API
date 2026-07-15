package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.EdwGraduationSnapshot;
import ca.bc.gov.educ.api.gradstudent.model.dto.SchoolClob;
import ca.bc.gov.educ.api.gradstudent.model.dto.SnapshotResponse;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.EdwGraduationSnapshotEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.EDWGraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.StudentNonGradReasonTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.EdwGraduationSnapshotRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentNonGradReasonRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants.SECOND_DEFAULT_DATE_FORMAT;

@Service
public class EdwSnapshotService {

    final EdwGraduationSnapshotRepository edwGraduationSnapshotRepository;

    final EDWGraduationStatusTransformer edwGraduationStatusTransformer;

    final EducGradStudentApiConstants constants;

    final WebClient webClient;
    final GradValidation validation;
    final GraduationStudentRecordRepository graduationStudentRecordRepository;
    final RESTService restService;
    final SchoolService schoolService;

    @Autowired
    public EdwSnapshotService(
                EdwGraduationSnapshotRepository edwGraduationSnapshotRepository,
                EDWGraduationStatusTransformer edwGraduationStatusTransformer,
                GraduationStudentRecordRepository graduationStudentRecordRepository,
                StudentNonGradReasonRepository studentNonGradReasonRepository,
                StudentNonGradReasonTransformer studentNonGradReasonTransformer,
                EducGradStudentApiConstants constants,
                @Qualifier("studentApiClient") WebClient webClient,
                GradValidation validation,
                RESTService restService,
                SchoolService schoolService) {
        this.constants = constants;
        this.webClient = webClient;
        this.validation = validation;
        this.edwGraduationSnapshotRepository = edwGraduationSnapshotRepository;
        this.edwGraduationStatusTransformer = edwGraduationStatusTransformer;
        this.graduationStudentRecordRepository = graduationStudentRecordRepository;
        this.restService = restService;
        this.schoolService = schoolService;
    }

    public EdwGraduationSnapshot retrieve(Integer gradYear, String pen) {
        return edwGraduationStatusTransformer.transformToDTO(edwGraduationSnapshotRepository.findByGradYearAndPen(gradYear, pen));
    }

    public List<EdwGraduationSnapshot> retrieveAll(Integer gradYear) {
        return edwGraduationStatusTransformer.transformToDTO(edwGraduationSnapshotRepository.findByGradYear(gradYear));
    }

    public List<EdwGraduationSnapshot> retrieveByPage(Integer gradYear, Pageable pageable) {
        return edwGraduationStatusTransformer.transformToDTO(edwGraduationSnapshotRepository.findByGradYear(gradYear, pageable));
    }

    public Integer countAllByGradYear(Integer gradYear) {
        return edwGraduationSnapshotRepository.countAllByGradYear(gradYear);
    }

    public List<String> getEdwSnapshotSchools(Integer gradYear) {
        Date startDate = getGradYearStartDate(gradYear);
        Date endDate = getGradYearEndDate(gradYear);
        return loadSchoolClobs(graduationStudentRecordRepository.findEdwSnapshotSchoolOfRecordIds(startDate, endDate)).values().stream()
                .map(SchoolClob::getMinCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .sorted()
                .toList();
    }

    public List<SnapshotResponse> getEdwSnapshotStudents(Integer gradYear, String minCode) {
        School school = schoolService.getSchoolByMincode(minCode);
        if (school == null || StringUtils.isBlank(school.getSchoolId())) {
            return List.of();
        }
        UUID schoolOfRecordId = UUID.fromString(school.getSchoolId());
        Date startDate = getGradYearStartDate(gradYear);
        Date endDate = getGradYearEndDate(gradYear);
        List<GraduationStudentRecordEntity> students = graduationStudentRecordRepository.findEdwSnapshotStudentsBySchoolOfRecordId(schoolOfRecordId, startDate, endDate);
        Map<UUID, SchoolClob> schoolClobs = loadSchoolClobs(List.of(schoolOfRecordId));
        return students.stream()
                .sorted(Comparator.comparing(GraduationStudentRecordEntity::getPen, Comparator.nullsLast(String::compareTo)))
                .map(student -> toSnapshotResponse(student, schoolClobs.get(student.getSchoolOfRecordId())))
                .toList();
    }

    @Transactional
    public EdwGraduationSnapshot saveEdwGraduationSnapshot(EdwGraduationSnapshot toBeSaved) {
        Optional<EdwGraduationSnapshotEntity> optional = edwGraduationSnapshotRepository.findByGradYearAndPen(toBeSaved.getGradYear(), toBeSaved.getPen());
        EdwGraduationSnapshotEntity toBeSavedEntity = edwGraduationStatusTransformer.transformToEntity(toBeSaved);
        // populate timestamp
        toBeSavedEntity.setRunDate(LocalDate.now());
        String sessionDate = toBeSavedEntity.getGradYear().toString() + "/09/01";
        toBeSavedEntity.setSessionDate(EducGradStudentApiUtils.parseLocalDate(sessionDate, SECOND_DEFAULT_DATE_FORMAT));

        EdwGraduationSnapshotEntity savedEntity;
        if (optional.isPresent()) { // update
            EdwGraduationSnapshotEntity entity = optional.get();
            BeanUtils.copyProperties(toBeSavedEntity, entity, "gradYear", "pen");
            savedEntity = edwGraduationSnapshotRepository.saveAndFlush(entity);
        } else { // create
            savedEntity = edwGraduationSnapshotRepository.saveAndFlush(toBeSavedEntity);
        }
        return edwGraduationStatusTransformer.transformToDTO(savedEntity);
    }

    private SnapshotResponse toSnapshotResponse(GraduationStudentRecordEntity student, SchoolClob schoolClob) {
        SnapshotResponse response = new SnapshotResponse();
        response.setPen(student.getPen());
        response.setStudentGrade(student.getStudentGrade());
        response.setSchoolOfRecordId(student.getSchoolOfRecordId() != null ? student.getSchoolOfRecordId().toString() : null);
        response.setSchoolOfRecord(schoolClob != null ? schoolClob.getMinCode() : null);
        response.setGraduatedDate(student.getProgramCompletionDate() != null
                ? EducGradStudentApiUtils.formatDate(student.getProgramCompletionDate(), EducGradStudentApiConstants.TRAX_DATE_FORMAT)
                : null);
        response.setGpa(StringUtils.isNotBlank(student.getGpa()) ? new BigDecimal(student.getGpa()) : null);
        response.setHonourFlag(student.getHonoursStanding());
        return response;
    }

    private Map<UUID, SchoolClob> loadSchoolClobs(List<UUID> schoolIds) {
        return schoolIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(id -> Map.entry(id, restService.get(String.format(constants.getSchoolClobBySchoolIdUrl(), id), SchoolClob.class, webClient)))
                .filter(entry -> entry.getValue() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (left, right) -> left, HashMap::new));
    }

    private Date getGradYearStartDate(Integer gradYear) {
        return Date.from(LocalDate.of(gradYear - 1, Month.SEPTEMBER, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private Date getGradYearEndDate(Integer gradYear) {
        return Date.from(LocalDate.of(gradYear, Month.SEPTEMBER, 1).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
