package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.dto.CourseReport;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCoursePaginationEntity;
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

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Repository
public class StudentCoursePaginationRepositoryImpl implements StudentCoursePaginationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * @param spec The specification to filter results (can be null for all results)
     * @return Stream of StudentCoursePaginationEntity
     */
    @Override
    @Transactional(readOnly = true)
    public Stream<StudentCoursePaginationEntity> streamAll(Specification<StudentCoursePaginationEntity> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<StudentCoursePaginationEntity> cq = cb.createQuery(StudentCoursePaginationEntity.class);
        Root<StudentCoursePaginationEntity> root = cq.from(StudentCoursePaginationEntity.class);

        if (spec != null) {
            cq.where(spec.toPredicate(root, cq, cb));
        }

        TypedQuery<StudentCoursePaginationEntity> query = entityManager.createQuery(cq);

        query.setHint("org.hibernate.fetchSize", 5000);
        query.setHint("org.hibernate.readOnly", true);
        query.setHint("org.hibernate.cacheable", false);
        query.setHint("jakarta.persistence.cache.storeMode", "BYPASS");
        query.setHint("jakarta.persistence.query.timeout", 300000);

        return query.getResultStream();
    }

    /**
     * Stream course report data using native SQL with only required columns
     * To avoid hibernate n+1 efficiency issues
     */
    @Transactional(readOnly = true)
    public Stream<CourseReport> streamForCourseReport(String whereClause) {
        String sql = """
            SELECT 
                gsr.PEN,
                gsr.STUDENT_STATUS_CODE,
                gsr.LEGAL_LAST_NAME,
                gsr.DOB,
                gsr.STUDENT_GRADE,
                gsr.GRADUATION_PROGRAM_CODE,
                gsr.PROGRAM_COMPLETION_DATE,
                gsr.SCHOOL_OF_RECORD,
                gsr.SCHOOL_OF_RECORD_ID,
                gsr.SCHOOL_AT_GRADUATION,
                gsr.SCHOOL_AT_GRADUATION_ID,
                sc.COURSE_ID,
                sc.COURSE_SESSION,
                sc.INTERIM_PERCENT,
                sc.INTERIM_LETTER_GRADE,
                sc.FINAL_PERCENT,
                sc.FINAL_LETTER_GRADE,
                CAST(sc.NUMBER_CREDITS AS NUMBER(2,0)),
                sc.EQUIVALENT_OR_CHALLENGE_CODE,
                sc.FINE_ARTS_APPLIED_SKILLS_CODE,
                sc.STUDENT_COURSE_EXAM_ID
            FROM STUDENT_COURSE sc
            LEFT JOIN GRADUATION_STUDENT_RECORD gsr ON sc.GRADUATION_STUDENT_RECORD_ID = gsr.GRADUATION_STUDENT_RECORD_ID
            """ + (whereClause != null && !whereClause.isBlank() ? " WHERE " + whereClause : "");

        Query query = entityManager.createNativeQuery(sql);
        query.setHint("org.hibernate.fetchSize", 5000);
        query.setHint("org.hibernate.readOnly", true);

        Stream<Object[]> resultStream = query.getResultStream();

        return resultStream.map(row -> new CourseReport(
            row[0] != null ? row[0].toString() : null,
            row[1] != null ? row[1].toString() : null,
            row[2] != null ? row[2].toString() : null,
            row[3] != null ? ((Timestamp) row[3]).toLocalDateTime().toLocalDate() : null,
            row[4] != null ? row[4].toString() : null,
            row[5] != null ? row[5].toString() : null,
            row[6] != null ? ((Timestamp) row[6]).toLocalDateTime().toLocalDate() : null,
            row[7] != null ? row[7].toString() : null,
            convertToUUID(row[8]),
            row[9] != null ? row[9].toString() : null,
            convertToUUID(row[10]),
            row[11] != null ? new BigInteger(row[11].toString()) : null,
            row[12] != null ? row[12].toString() : null,
            convertToDouble(row[13]),
            row[14] != null ? row[14].toString() : null,
            convertToDouble(row[15]),
            row[16] != null ? row[16].toString() : null,
            convertToInteger(row[17]),
            row[18] != null ? row[18].toString() : null,
            row[19] != null ? row[19].toString() : null,
            convertToUUID(row[20])
        ));
    }

    /**
     * Convert Oracle UUID column to java.util.UUID
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

    /**
     * Safely convert value to Double, handling Number types and null.
     */
    private Double convertToDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Safely convert value to Integer, handling Number types and null.
     */
    private Integer convertToInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof Boolean boolVal) {
            return boolVal ? 1 : 0;
        }
        try {
            String strValue = value.toString().trim();
            if (strValue.isEmpty()) {
                return null;
            }
            if ("true".equalsIgnoreCase(strValue)) {
                return 1;
            }
            if ("false".equalsIgnoreCase(strValue)) {
                return 0;
            }
            if (strValue.contains(".")) {
                return (int) Double.parseDouble(strValue);
            }
            return Integer.parseInt(strValue);
        } catch (Exception e) {
            return null;
        }
    }
}
