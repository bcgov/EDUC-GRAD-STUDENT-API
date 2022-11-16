package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordSearchEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.Date;
import java.util.UUID;

public class GraduationStudentRecordSearchSpecification implements Specification<GraduationStudentRecordSearchEntity> {

    private static final Logger logger = LoggerFactory.getLogger(GraduationStudentRecordSearchSpecification.class);
    private static final String PROGRAM_COMPLETION_DATE = "programCompletionDate";

    private final GraduationStudentRecordSearchCriteria searchCriteria;

    public GraduationStudentRecordSearchSpecification(GraduationStudentRecordSearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    @Override
    @Nullable
    public Predicate toPredicate(Root<GraduationStudentRecordSearchEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        logger.debug("toPredicate()");
        if (searchCriteria.getStudentIds() != null && !searchCriteria.getStudentIds().isEmpty()) {
            return criteriaBuilder.and(root.get("studentID").as(UUID.class).in(searchCriteria.getStudentUUIDs()),
                    criteriaBuilder.notEqual(root.get("studentStatus"), "MER")
            );
        } else if (searchCriteria.getSchoolOfRecords() != null && !searchCriteria.getSchoolOfRecords().isEmpty()) {
            Predicate curStatusOptional = criteriaBuilder.equal(root.get("studentStatus"), "CUR");
            if(searchCriteria.getGradDateFrom() != null && searchCriteria.getGradDateTo() != null) {
                curStatusOptional = criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get(PROGRAM_COMPLETION_DATE).as(Date.class), searchCriteria.getGradDateFrom())
                        ,criteriaBuilder.lessThanOrEqualTo(root.get(PROGRAM_COMPLETION_DATE).as(Date.class), searchCriteria.getGradDateTo())
                );
            }
            return criteriaBuilder.and(root.get("schoolOfRecord").in(searchCriteria.getSchoolOfRecords()),
                    curStatusOptional
            );
        } else if (searchCriteria.getPrograms() != null && !searchCriteria.getPrograms().isEmpty()) {
            Predicate curStatusOptional = criteriaBuilder.equal(root.get("studentStatus"), "CUR");
            if(searchCriteria.getGradDateFrom() != null && searchCriteria.getGradDateTo() != null) {
                curStatusOptional = criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get(PROGRAM_COMPLETION_DATE).as(Date.class), searchCriteria.getGradDateFrom())
                        ,criteriaBuilder.lessThanOrEqualTo(root.get(PROGRAM_COMPLETION_DATE).as(Date.class), searchCriteria.getGradDateTo())
                );
            }
            return criteriaBuilder.and(root.get("program").in(searchCriteria.getPrograms()),
                    curStatusOptional
            );
        }
        return criteriaBuilder.and();
    }
}
