package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("integration-test")
@Transactional
public class GraduationStudentRecordRepositoryTest {

    @Autowired
    private GraduationStudentRecordRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    private UUID schoolId1;
    private UUID schoolId2;
    private GraduationStudentRecordEntity curStudentInSchool1;
    private GraduationStudentRecordEntity terStudentInSchool1;
    private GraduationStudentRecordEntity curStudentInSchool2;

    private static final String CLOB_WITH_CUR_STATUS = "{\"gradStatus\":{\"studentStatus\":\"CUR\"},\"graduated\":true}";

    @Before
    public void setUp() {
        schoolId1 = UUID.randomUUID();
        schoolId2 = UUID.randomUUID();

        // CUR student in school 1 — the primary archive target
        curStudentInSchool1 = buildEntity("CUR", schoolId1, CLOB_WITH_CUR_STATUS);
        repository.save(curStudentInSchool1);

        // TER student in school 1 — same school, different status, must NOT be archived
        terStudentInSchool1 = buildEntity("TER", schoolId1, null);
        repository.save(terStudentInSchool1);

        // CUR student in school 2 — different school, must NOT be affected by school-filtered archive
        curStudentInSchool2 = buildEntity("CUR", schoolId2, null);
        repository.save(curStudentInSchool2);

        entityManager.flush();
    }

    @Test
    public void testArchiveBySchool_updatesStatusToArc() {
        Integer count = repository.archiveStudents(List.of(schoolId1), "CUR", "ARC", 100L, "TEST_USER");
        entityManager.flush();
        entityManager.clear();

        assertThat(count).isEqualTo(1);
        Optional<GraduationStudentRecordEntity> result = repository.findById(curStudentInSchool1.getStudentID());
        assertThat(result).isPresent();
        assertThat(result.get().getStudentStatus()).isEqualTo("ARC");
        assertThat(result.get().getBatchId()).isEqualTo(100L);
    }

    @Test
    public void testArchiveBySchool_doesNotAffectOtherSchools() {
        repository.archiveStudents(List.of(schoolId1), "CUR", "ARC", 100L, "TEST_USER");
        entityManager.flush();
        entityManager.clear();

        Optional<GraduationStudentRecordEntity> result = repository.findById(curStudentInSchool2.getStudentID());
        assertThat(result).isPresent();
        assertThat(result.get().getStudentStatus()).isEqualTo("CUR");
    }

    @Test
    public void testArchiveBySchool_doesNotAffectDifferentStatusInSameSchool() {
        repository.archiveStudents(List.of(schoolId1), "CUR", "ARC", 100L, "TEST_USER");
        entityManager.flush();
        entityManager.clear();

        Optional<GraduationStudentRecordEntity> result = repository.findById(terStudentInSchool1.getStudentID());
        assertThat(result).isPresent();
        assertThat(result.get().getStudentStatus()).isEqualTo("TER");
    }

    @Test
    public void testArchiveBySchool_withMultipleSchools_archivesBoth() {
        Integer count = repository.archiveStudents(List.of(schoolId1, schoolId2), "CUR", "ARC", 101L, "TEST_USER");
        entityManager.flush();
        entityManager.clear();

        assertThat(count).isEqualTo(2);
        assertThat(repository.findById(curStudentInSchool1.getStudentID()).map(GraduationStudentRecordEntity::getStudentStatus))
                .hasValue("ARC");
        assertThat(repository.findById(curStudentInSchool2.getStudentID()).map(GraduationStudentRecordEntity::getStudentStatus))
                .hasValue("ARC");
    }

    @Test
    public void testArchiveAll_updatesAllMatchingStatusRegardlessOfSchool() {
        Integer count = repository.archiveStudents("CUR", "ARC", 200L, "TEST_USER");
        entityManager.flush();
        entityManager.clear();

        assertThat(count).isGreaterThanOrEqualTo(2);
        assertThat(repository.findById(curStudentInSchool1.getStudentID()).map(GraduationStudentRecordEntity::getStudentStatus))
                .hasValue("ARC");
        assertThat(repository.findById(curStudentInSchool2.getStudentID()).map(GraduationStudentRecordEntity::getStudentStatus))
                .hasValue("ARC");
    }

    @Test
    public void testArchiveAll_doesNotAffectNonMatchingStatus() {
        repository.archiveStudents("CUR", "ARC", 200L, "TEST_USER");
        entityManager.flush();
        entityManager.clear();

        Optional<GraduationStudentRecordEntity> result = repository.findById(terStudentInSchool1.getStudentID());
        assertThat(result).isPresent();
        assertThat(result.get().getStudentStatus()).isEqualTo("TER");
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private GraduationStudentRecordEntity buildEntity(String status, UUID schoolOfRecordId, String studentGradData) {
        GraduationStudentRecordEntity entity = new GraduationStudentRecordEntity();
        entity.setStudentID(UUID.randomUUID());
        entity.setStudentStatus(status);
        entity.setSchoolOfRecordId(schoolOfRecordId);
        entity.setProgram("2018-EN");
        entity.setStudentGradData(studentGradData);
        return entity;
    }
}
