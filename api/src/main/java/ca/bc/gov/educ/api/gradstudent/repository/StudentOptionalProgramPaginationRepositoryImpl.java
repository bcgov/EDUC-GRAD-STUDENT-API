package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.dto.OptionalProgramReport;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Repository
public class StudentOptionalProgramPaginationRepositoryImpl implements StudentOptionalProgramPaginationRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;
    /**
     * @param spec The specification to filter results (can be null for all results)
     * @return Stream of StudentOptionalProgramPaginationEntity
     */
    @Override
    @Transactional(readOnly = true)
    public Stream<StudentOptionalProgramPaginationEntity> streamAll(Specification<StudentOptionalProgramPaginationEntity> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentOptionalProgramPaginationEntity> cq = cb.createQuery(StudentOptionalProgramPaginationEntity.class);
        Root<StudentOptionalProgramPaginationEntity> root = cq.from(StudentOptionalProgramPaginationEntity.class);

        if (spec != null) {
            cq.where(spec.toPredicate(root, cq, cb));
        }
        TypedQuery<StudentOptionalProgramPaginationEntity> query = entityManager.createQuery(cq);


        query.setHint("org.hibernate.fetchSize", 5000);
        query.setHint("org.hibernate.readOnly", true);
        query.setHint("org.hibernate.cacheable", false);
        query.setHint("jakarta.persistence.cache.storeMode", "BYPASS");
        query.setHint("jakarta.persistence.query.timeout", 300000);

        return query.getResultStream();
    }

    /**
     * Stream optional program data using native SQL
     *
     * @param whereClause WHERE clause for filtering (without "WHERE" keyword)
     * @return Stream of OptionalProgramReport
     */
    @Override
    @Transactional(readOnly = true)
    public Stream<OptionalProgramReport> streamForOptionalProgramReport(String whereClause) {
        String sql = """
            SELECT 
                gsr.PEN,
                gsr.STUDENT_STATUS_CODE,
                gsr.LEGAL_FIRST_NAME,
                gsr.LEGAL_LAST_NAME,
                gsr.LEGAL_MIDDLE_NAMES,
                gsr.DOB,
                gsr.STUDENT_GRADE,
                gsr.GRADUATION_PROGRAM_CODE,
                gsr.PROGRAM_COMPLETION_DATE,
                gsr.SCHOOL_OF_RECORD,
                gsr.SCHOOL_OF_RECORD_ID,
                gsr.SCHOOL_AT_GRADUATION,
                gsr.SCHOOL_AT_GRADUATION_ID,
                sop.OPTIONAL_PROGRAM_ID,
                sop.COMPLETION_DATE
            FROM STUDENT_OPTIONAL_PROGRAM sop
            LEFT JOIN GRADUATION_STUDENT_RECORD gsr ON sop.GRADUATION_STUDENT_RECORD_ID = gsr.GRADUATION_STUDENT_RECORD_ID
            """ + (whereClause != null && !whereClause.isBlank() ? " WHERE " + whereClause : "");

        Query query = entityManager.createNativeQuery(sql);
        query.setHint("org.hibernate.fetchSize", 5000);
        query.setHint("org.hibernate.readOnly", true);

        Stream<Object[]> resultStream = query.getResultStream();

        return resultStream.map(row -> new OptionalProgramReport(
            row[0] != null ? row[0].toString() : null,
            row[1] != null ? row[1].toString() : null,
            row[2] != null ? row[2].toString() : null,
            row[3] != null ? row[3].toString() : null,
            row[4] != null ? row[4].toString() : null,
            row[5] != null ? ((Timestamp) row[5]).toLocalDateTime().toLocalDate() : null,
            row[6] != null ? row[6].toString() : null,
            row[7] != null ? row[7].toString() : null,
            row[8] != null ? ((Timestamp) row[8]).toLocalDateTime().toLocalDate() : null,
            row[9] != null ? row[9].toString() : null,
            convertToUUID(row[10]),
            row[11] != null ? row[11].toString() : null,
            convertToUUID(row[12]),
            convertToUUID(row[13]),
            row[14] != null ? ((Timestamp) row[14]).toLocalDateTime().toLocalDate() : null
        ));
    }

    /**
     * Convert Oracle UUID  to java.util.UUID.
     */
    private UUID convertToUUID(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof byte[] bytes) {
            if (bytes.length == 16) {
                java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(bytes);
                long high = bb.getLong();
                long low = bb.getLong();
                return new UUID(high, low);
            }
        } else if (value instanceof String) {
            return UUID.fromString((String) value);
        }
        return null;
    }
}
