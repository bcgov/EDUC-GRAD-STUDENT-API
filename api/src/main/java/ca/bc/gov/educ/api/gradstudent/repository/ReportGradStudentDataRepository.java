package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ReportGradStudentDataRepository extends JpaRepository<ReportGradStudentDataEntity, UUID> {

    List<ReportGradStudentDataEntity> findReportGradStudentDataEntityBySchoolOfRecordIdOrderBySchoolNameAscLastNameAsc(UUID schoolOfRecordId);

    List<ReportGradStudentDataEntity> findReportGradStudentDataEntityByGraduationStudentRecordIdInOrderBySchoolNameAscLastNameAsc(List<UUID> graduationStudentRecordId);

    @Query("select c from ReportGradStudentDataEntity c where c.graduationStudentRecordId in (" +
            " select s.studentID from GraduationStudentRecordEntity s where s.programCompletionDate is null and s.studentStatus='CUR' and (s.studentGrade='AD' or s.studentGrade='12'))")
    Page<ReportGradStudentDataEntity> findReportGradStudentDataEntityByProgramCompletionDateAndStudentStatusAndStudentGrade(Pageable page);

    @Query("select c from ReportGradStudentDataEntity c where c.schoolOfRecordId = :schoolOfRecordId and c.graduationStudentRecordId in (" +
            " select s.studentID from GraduationStudentRecordEntity s where s.programCompletionDate is null and s.studentStatus='CUR' and (s.studentGrade='AD' or s.studentGrade='12'))")
    Page<ReportGradStudentDataEntity> findReportGradStudentDataEntityBySchoolOfRecordIdAndProgramCompletionDateAndStudentStatusAndStudentGrade(UUID schoolOfRecordId, Pageable page);

    @Query("select c from ReportGradStudentDataEntity c where c.districtId = :districtId and c.graduationStudentRecordId in (" +
            " select s.studentID from GraduationStudentRecordEntity s where s.programCompletionDate is null and s.studentStatus='CUR' and (s.studentGrade='AD' or s.studentGrade='12'))")
    Page<ReportGradStudentDataEntity> findReportGradStudentDataEntityByDistrictIdAndProgramCompletionDateAndStudentStatusAndStudentGrade(UUID districtId, Pageable page);

}