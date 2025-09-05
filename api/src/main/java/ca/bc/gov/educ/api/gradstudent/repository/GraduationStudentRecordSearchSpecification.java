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
    private static final String SCHOOL_AT_GRAD_ID = "schoolAtGradId";
    private static final String SCHOOL_OF_RECORD_ID = "schoolOfRecordId";

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
        if (searchCriteria.getStudentIds() != null && !searchCriteria.getStudentIds().isEmpty()) {
            curStatusOptional = criteriaBuilder.not(root.get(STUDENT_STATUS).in("MER"));
            return criteriaBuilder.and(root.get("studentID").as(UUID.class).in(searchCriteria.getStudentIds()),
                    curStatusOptional
            );
        }
        boolean certDist = StringUtils.equalsAnyIgnoreCase(searchCriteria.activityCode, "GRADALG", "USERDISTOC", "USERDISTRC");
        if(certDist) {
            curStatusOptional = criteriaBuilder.not(root.get(STUDENT_STATUS).in("MER", "DEC"));
        } else {
            curStatusOptional = criteriaBuilder.equal(root.get(STUDENT_STATUS), "CUR");
        }

        Predicate datesRangePredicate = criteriaBuilder.and();
        if(searchCriteria.getGradDateFrom() != null && searchCriteria.getGradDateTo() != null) {
            boolean transcriptDist = StringUtils.equalsAnyIgnoreCase(searchCriteria.activityCode,  "USERDISTOT", "USERDISTRT");
            if(transcriptDist) {
                curStatusOptional = criteriaBuilder.not(root.get(STUDENT_STATUS).in("MER", "DEC"));
            }
            datesRangePredicate = criteriaBuilder.and(
                    criteriaBuilder.greaterThanOrEqualTo(root.get(PROGRAM_COMPLETION_DATE).as(LocalDate.class), searchCriteria.getGradDateFrom())
                    ,criteriaBuilder.lessThanOrEqualTo(root.get(PROGRAM_COMPLETION_DATE).as(LocalDate.class), searchCriteria.getGradDateTo())
            );
        }
        Predicate schoolOfRecordPredicate = criteriaBuilder.and();
        if (searchCriteria.getSchoolIds() != null && !searchCriteria.getSchoolIds().isEmpty()) {
            /***
            Predicate schoolAtGraduationIsNull = criteriaBuilder.isNull(root.get(SCHOOL_AT_GRADUATION));
            Predicate schoolAtGraduationIsNotNull = criteriaBuilder.isNotNull(root.get(SCHOOL_AT_GRADUATION));
            Predicate schoolOfRecordPredicate = criteriaBuilder.and(root.get(SCHOOL_OF_RECORD).in(searchCriteria.getSchoolOfRecords()), schoolAtGraduationIsNull);
            Predicate schoolAtGraduationPredicate = criteriaBuilder.and(root.get(SCHOOL_AT_GRADUATION).in(searchCriteria.getSchoolOfRecords()), schoolAtGraduationIsNotNull);
            Predicate schoolOfRecordPredicateOr = criteriaBuilder.and(criteriaBuilder.or(schoolOfRecordPredicate));
            Predicate schoolAtGraduationPredicateOr = criteriaBuilder.and(criteriaBuilder.or(schoolAtGraduationPredicate));
            Predicate finalPredicate = criteriaBuilder.or(schoolOfRecordPredicateOr, schoolAtGraduationPredicateOr);
            schoolOfRecordPredicate = criteriaBuilder.and(curStatusOptional, datesRangePredicate, finalPredicate);
             ***/
            if(certDist) {
                schoolOfRecordPredicate = criteriaBuilder.and(root.get(SCHOOL_AT_GRAD_ID).as(UUID.class).in(searchCriteria.getSchoolIds()));
            } else {
                schoolOfRecordPredicate = criteriaBuilder.and(root.get(SCHOOL_OF_RECORD_ID).as(UUID.class).in(searchCriteria.getSchoolIds()));
            }
        }
        Predicate programPredicate = criteriaBuilder.and();
        if (searchCriteria.getPrograms() != null && !searchCriteria.getPrograms().isEmpty()) {
            programPredicate = criteriaBuilder.and(root.get("program").in(searchCriteria.getPrograms()));
        }
        return criteriaBuilder.and(curStatusOptional, datesRangePredicate, schoolOfRecordPredicate, programPredicate);
    }
}
