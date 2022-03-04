package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class GraduationStudentRecordSearchSpecification {

    private static final Logger logger = LoggerFactory.getLogger(GraduationStudentRecordSearchSpecification.class);

    public static Specification<GraduationStudentRecordEntity> findByCriteria(final GraduationStudentRecordSearchCriteria searchCriteria) {
        return new Specification<GraduationStudentRecordEntity>() {
            @Override
            public Predicate toPredicate(Root<GraduationStudentRecordEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
                if (searchCriteria.getPens() != null && !searchCriteria.getPens().isEmpty()) {
                    return criteriaBuilder.and(root.get("pen").in(searchCriteria.getPens()));
                } else if (searchCriteria.getSchoolOfRecords() != null && !searchCriteria.getSchoolOfRecords().isEmpty()) {
                    return criteriaBuilder.and(root.get("schoolOfRecord").in(searchCriteria.getSchoolOfRecords()));
                } else if (searchCriteria.getDistricts() != null && !searchCriteria.getDistricts().isEmpty()) {
                    return criteriaBuilder.substring(root.get("schoolOfRecord"), 3).in(searchCriteria.getDistricts());
                } else if (searchCriteria.getPrograms() != null && !searchCriteria.getPrograms().isEmpty()) {
                    return criteriaBuilder.and(root.get("program").in(searchCriteria.getPrograms()));
                }
                return criteriaBuilder.and();
            }
        };
    }
}
