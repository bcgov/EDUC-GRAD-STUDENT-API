package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReportGradStudentDataRepository extends JpaRepository<ReportGradStudentDataEntity, UUID> {

    List<ReportGradStudentDataEntity> findReportGradStudentDataEntityByMincodeStartsWithOrderBySchoolNameAscLastNameAsc(String minCode);

    List<ReportGradStudentDataEntity> findReportGradStudentDataEntityByGraduationStudentRecordIdInOrderBySchoolNameAscLastNameAsc(List<UUID> graduationStudentRecordId);
}