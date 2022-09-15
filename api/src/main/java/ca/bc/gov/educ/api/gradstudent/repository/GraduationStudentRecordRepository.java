package ca.bc.gov.educ.api.gradstudent.repository;

import java.util.List;
import java.util.UUID;

import ca.bc.gov.educ.api.gradstudent.model.dto.BatchGraduationStudentRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;

@Repository
public interface GraduationStudentRecordRepository extends JpaRepository<GraduationStudentRecordEntity, UUID> {

    List<GraduationStudentRecordEntity> findAll();

	List<GraduationStudentRecordEntity> findByRecalculateGradStatus(String recalulateFlag);
	List<GraduationStudentRecordEntity> findByRecalculateProjectedGrad(String recalculateProjectedGrad);

	@Query("select new ca.bc.gov.educ.api.gradstudent.model.dto.BatchGraduationStudentRecord(c.program,c.programCompletionDate,c.schoolOfRecord,c.studentGrade,c.studentStatus,c.studentID) from GraduationStudentRecordEntity c where c.recalculateGradStatus=:recalculateFlag")
	List<BatchGraduationStudentRecord> findByRecalculateGradStatusForBatch(String recalculateFlag);

	@Query("select new ca.bc.gov.educ.api.gradstudent.model.dto.BatchGraduationStudentRecord(c.program,c.programCompletionDate,c.schoolOfRecord,c.studentGrade,c.studentStatus,c.studentID) from GraduationStudentRecordEntity c where c.recalculateProjectedGrad=:recalculateProjectedGrad")
	List<BatchGraduationStudentRecord> findByRecalculateProjectedGradForBatch(String recalculateProjectedGrad);

	GraduationStudentRecordEntity findByStudentID(UUID studentID);

	@Query("select c from GraduationStudentRecordEntity c where c.studentStatus=:statusCode")
	List<GraduationStudentRecordEntity> existsByStatusCode(String statusCode);
	
	@Query(value="SELECT si.* FROM graduation_student_record si where "
			+ "(:gradProgram is null or si.graduation_program_code = :gradProgram) and "
			+ "(:schoolOfRecord is null or si.school_of_record = :schoolOfRecord)",nativeQuery = true)
	public Page<GraduationStudentRecordEntity> findStudentWithFilter(String gradProgram,String schoolOfRecord, Pageable paging);

    List<GraduationStudentRecordEntity> findByStudentIDIn(List<UUID> studentIds);

	@Query("select c from GraduationStudentRecordEntity c where c.programCompletionDate is null and c.studentStatus='CUR' and (c.studentGrade='AD' or c.studentGrade='12')")
    List<GraduationStudentRecordEntity> findStudentsForYearlyDistribution();

	@Query("select c from GraduationStudentRecordEntity c where c.schoolOfRecord=:schoolOfRecord and c.studentStatus='CUR' and (c.studentGrade='AD' or c.studentGrade='12')")
	List<GraduationStudentRecordEntity> findBySchoolOfRecord(String schoolOfRecord);
}
