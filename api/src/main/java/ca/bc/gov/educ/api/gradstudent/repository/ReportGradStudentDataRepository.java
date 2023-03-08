package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportGradStudentDataRepository extends JpaRepository<ReportGradStudentDataEntity, UUID> {

    List<ReportGradStudentDataEntity> findReportGradStudentDataEntityByMincodeStartsWithOrderByMincodeAscSchoolNameAscLastNameAsc(String minCode);

    List<ReportGradStudentDataEntity> findReportGradStudentDataEntityByGraduationStudentRecordIdInOrderByMincodeAscSchoolNameAscLastNameAsc(List<UUID> graduationStudentRecordId);
}