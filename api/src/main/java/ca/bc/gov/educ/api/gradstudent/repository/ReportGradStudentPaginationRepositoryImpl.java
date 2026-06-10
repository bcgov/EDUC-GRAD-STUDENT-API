package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Stream;

@Repository
public class ReportGradStudentPaginationRepositoryImpl implements ReportGradStudentPaginationRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * @param spec The specification to filter results (can be null for all results)
     * @return Stream of ReportGradStudentDataEntity
     */
    @Override
    @Transactional(readOnly = true)
    public Stream<ReportGradStudentDataEntity> streamAll(Specification<ReportGradStudentDataEntity> spec) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ReportGradStudentDataEntity> cq = cb.createQuery(ReportGradStudentDataEntity.class);
        Root<ReportGradStudentDataEntity> root = cq.from(ReportGradStudentDataEntity.class);

        if (spec != null) {
            cq.where(spec.toPredicate(root, cq, cb));
        }

        TypedQuery<ReportGradStudentDataEntity> query = entityManager.createQuery(cq);

        query.setHint("org.hibernate.fetchSize", 5000);
        query.setHint("org.hibernate.readOnly", true);
        query.setHint("org.hibernate.cacheable", false);
        query.setHint("jakarta.persistence.cache.storeMode", "BYPASS");
        query.setHint("jakarta.persistence.query.timeout", 300000);

        return query.getResultStream();
    }
}
