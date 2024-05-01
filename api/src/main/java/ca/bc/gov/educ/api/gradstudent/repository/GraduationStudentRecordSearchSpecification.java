package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.constant.Generated;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordSearchEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.Nullable;

import java.time.LocalDate;
import java.util.UUID;

public class GraduationStudentRecordSearchSpecification implements Specification<GraduationStudentRecordSearchEntity> {

    private static final Logger logger = LoggerFactory.getLogger(GraduationStudentRecordSearchSpecification.class);
    private static final String PROGRAM_COMPLETION_DATE = "programCompletionDate";
    private static final String STUDENT_STATUS = "studentStatus";

    private final GraduationStudentRecordSearchCriteria searchCriteria;

    public GraduationStudentRecordSearchSpecification(GraduationStudentRecordSearchCriteria searchCriteria) {
        this.searchCriteria = searchCriteria;
    }

    @Override
    @Nullable
    @Generated
    public Predicate toPredicate(Root<GraduationStudentRecordSearchEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        logger.debug("toPredicate()");
        Predicate curStatusOptional;
        if(StringUtils.containsAnyIgnoreCase(searchCriteria.activityCode, "USERDIST", "USERDISTOC", "USERDISTRC", "USERDISTOT", "USERDISTRT")) {
            curStatusOptional = criteriaBuilder.notEqual(root.get(STUDENT_STATUS), "MER");
        } else {
            curStatusOptional = criteriaBuilder.equal(root.get(STUDENT_STATUS), "CUR");
        }
        if (searchCriteria.getStudentIds() != null && !searchCriteria.getStudentIds().isEmpty()) {
            return criteriaBuilder.and(root.get("studentID").as(UUID.class).in(searchCriteria.getStudentUUIDs()),
              curStatusOptional
            );
        } else if (searchCriteria.getSchoolOfRecords() != null && !searchCriteria.getSchoolOfRecords().isEmpty()) {
            Predicate datesRangePredicate = null;
            if(searchCriteria.getGradDateFrom() != null && searchCriteria.getGradDateTo() != null) {
                datesRangePredicate = criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get(PROGRAM_COMPLETION_DATE).as(LocalDate.class), searchCriteria.getGradDateFrom())
                        ,criteriaBuilder.lessThanOrEqualTo(root.get(PROGRAM_COMPLETION_DATE).as(LocalDate.class), searchCriteria.getGradDateTo())
                );
            }
            if(datesRangePredicate == null) {
                curStatusOptional = criteriaBuilder.equal(root.get(STUDENT_STATUS), "CUR");
                datesRangePredicate = curStatusOptional;
            }
            return criteriaBuilder.and(root.get("schoolOfRecord").in(searchCriteria.getSchoolOfRecords()),
                    curStatusOptional, datesRangePredicate
            );
        } else if (searchCriteria.getPrograms() != null && !searchCriteria.getPrograms().isEmpty()) {
            Predicate datesRangePredicate = null;
            if(searchCriteria.getGradDateFrom() != null && searchCriteria.getGradDateTo() != null) {
                datesRangePredicate = criteriaBuilder.and(
                        criteriaBuilder.greaterThanOrEqualTo(root.get(PROGRAM_COMPLETION_DATE).as(LocalDate.class), searchCriteria.getGradDateFrom())
                        ,criteriaBuilder.lessThanOrEqualTo(root.get(PROGRAM_COMPLETION_DATE).as(LocalDate.class), searchCriteria.getGradDateTo())
                );
            }
            if(datesRangePredicate == null) {
                curStatusOptional = criteriaBuilder.equal(root.get(STUDENT_STATUS), "CUR");
                datesRangePredicate = curStatusOptional;
            }
            return criteriaBuilder.and(root.get("program").in(searchCriteria.getPrograms()),
                    curStatusOptional, datesRangePredicate
            );
        }
        return criteriaBuilder.and(curStatusOptional);
    }
}
