package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

public class GraduationStudentRecordSearchSpecification {

    public static Specification<GraduationStudentRecordEntity> findByCriteria(final GraduationStudentRecordSearchCriteria searchCriteria) {
        return new Specification<GraduationStudentRecordEntity>() {
            @Override
            public Predicate toPredicate(Root<GraduationStudentRecordEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();
                if (searchCriteria.getPens() != null && !searchCriteria.getPens().isEmpty()) {
                    predicates.add(root.get("pen").in(searchCriteria.getPens()));
                } else if (searchCriteria.getSchoolOfRecords() != null && !searchCriteria.getSchoolOfRecords().isEmpty()) {
                    predicates.add(root.get("schoolOfRecord").in(searchCriteria.getPens()));
                } else if (searchCriteria.getDistricts() != null && !searchCriteria.getDistricts().isEmpty()) {

                } else if (searchCriteria.getPrograms() != null && !searchCriteria.getPrograms().isEmpty()) {
                    predicates.add(root.get("program").in(searchCriteria.getPens()));
                }
                return criteriaBuilder.and(predicates.toArray(new Predicate[predicates.size()]));
            }
        };
    }
}
