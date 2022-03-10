package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

public class GraduationStudentRecordSearchSpecification implements Specification<GraduationStudentRecordEntity> {

    private static final Logger logger = LoggerFactory.getLogger(GraduationStudentRecordSearchSpecification.class);

    private final GraduationStudentRecordSearchCriteria searchCriteria;

    public GraduationStudentRecordSearchSpecification(GraduationStudentRecordSearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    @Override
    @Nullable
    public Predicate toPredicate(Root<GraduationStudentRecordEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        logger.debug("toPredicate()");
        if (searchCriteria.getStudentIds() != null && !searchCriteria.getStudentIds().isEmpty()) {
            return criteriaBuilder.and(root.get("studentID").in(searchCriteria.getStudentIds()),
                    criteriaBuilder.notEqual(root.get("studentStatus"), "MER")
            );
        } else if (searchCriteria.getSchoolOfRecords() != null && !searchCriteria.getSchoolOfRecords().isEmpty()) {
            return criteriaBuilder.and(root.get("schoolOfRecord").in(searchCriteria.getSchoolOfRecords()),
                    criteriaBuilder.equal(root.get("studentStatus"), "CUR")
            );
        } else if (searchCriteria.getDistricts() != null && !searchCriteria.getDistricts().isEmpty()) {
            return criteriaBuilder.and(criteriaBuilder.substring(root.get("schoolOfRecord").as(String.class), 0,3).in(searchCriteria.getDistricts()),
                    criteriaBuilder.equal(root.get("studentStatus"), "CUR")
            );
        } else if (searchCriteria.getPrograms() != null && !searchCriteria.getPrograms().isEmpty()) {
            return criteriaBuilder.and(root.get("program").in(searchCriteria.getPrograms()),
                    criteriaBuilder.equal(root.get("studentStatus"), "CUR")
            );
        }
        return criteriaBuilder.and();
    }
}
