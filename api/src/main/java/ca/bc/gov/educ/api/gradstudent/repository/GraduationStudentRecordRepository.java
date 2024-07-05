package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.dto.BatchGraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GraduationStudentRecordRepository extends JpaRepository<GraduationStudentRecordEntity, UUID> {

    List<GraduationStudentRecordEntity> findAll();

	@Query("select c.studentID from GraduationStudentRecordEntity c where c.recalculateGradStatus=:recalculateFlag")
	List<UUID> findByRecalculateGradStatusForBatch(String recalculateFlag);

	@Query("select c.studentID from GraduationStudentRecordEntity c where c.recalculateProjectedGrad=:recalculateProjectedGrad")
	List<UUID> findByRecalculateProjectedGradForBatch(String recalculateProjectedGrad);

	@Query("select new ca.bc.gov.educ.api.gradstudent.model.dto.BatchGraduationStudentRecord(c.program,c.programCompletionDate,c.schoolOfRecord,c.studentID) from GraduationStudentRecordEntity c where c.studentID=:studentID")
	Optional<BatchGraduationStudentRecord> findByStudentIDForBatch(UUID studentID);

	GraduationStudentRecordEntity findByStudentID(UUID studentID);

	@Query("select c from GraduationStudentRecordEntity c where c.studentStatus=:statusCode")
	List<GraduationStudentRecordEntity> existsByStatusCode(String statusCode);
	
	@Query(value="SELECT si.* FROM graduation_student_record si where "
			+ "(:gradProgram is null or si.graduation_program_code = :gradProgram) and "
			+ "(:schoolOfRecord is null or si.school_of_record = :schoolOfRecord)",nativeQuery = true)
	public Page<GraduationStudentRecordEntity> findStudentWithFilter(String gradProgram,String schoolOfRecord, Pageable paging);

    List<GraduationStudentRecordEntity> findByStudentIDIn(List<UUID> studentIds);

	@Query("select c.studentID from GraduationStudentRecordEntity c where c.programCompletionDate is null and c.studentStatus='CUR' and (c.studentGrade='AD' or c.studentGrade='12')")
	Page<UUID> findStudentsForYearlyDistribution(Pageable page);

	@Query("select c from GraduationStudentRecordEntity c where c.schoolOfRecord=:schoolOfRecord and c.studentStatus='CUR'")
	List<GraduationStudentRecordEntity> findBySchoolOfRecord(String schoolOfRecord);

	@Query("select c from GraduationStudentRecordEntity c where c.schoolOfRecord=:schoolOfRecord and c.studentStatus='CUR' and (c.studentGrade='AD' or c.studentGrade='12')")
	List<GraduationStudentRecordEntity> findBySchoolOfRecordAmalgamated(String schoolOfRecord);

	@Query("select count(*) from GraduationStudentRecordEntity c where c.schoolOfRecord=:schoolOfRecord and c.studentStatus='CUR' and (c.studentGrade='AD' or c.studentGrade='12')")
	Integer countBySchoolOfRecordAmalgamated(String schoolOfRecord);

	// Data Conversion
	@Modifying
    @Query(value="insert into STUDENT_GUID_PEN_XREF(STUDENT_GUID, STUDENT_PEN, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)\n"
            + "values (:studentGuid, :pen, :userName, :currentTime, :userName, :currentTime) ", nativeQuery=true)
    void createStudentGuidPenXrefRecord(
            @Param("studentGuid") UUID studentGuid,
            @Param("pen") String pen,
            @Param("userName") String userName,
            @Param("currentTime") LocalDateTime currentTime);

    @Modifying
    @Query(value="update STUDENT_GUID_PEN_XREF set STUDENT_PEN = :pen, UPDATE_USER = :userName, UPDATE_DATE = :currentTime\n"
            + "where STUDENT_GUID = :studentGuid", nativeQuery=true)
    void updateStudentGuidPenXrefRecord(
            @Param("studentGuid") UUID studentGuid,
            @Param("pen") String pen,
            @Param("userName") String userName,
            @Param("currentTime") LocalDateTime currentTime);

    @Query(value="select count(*) from STUDENT_GUID_PEN_XREF gpx \n" +
            "where gpx.STUDENT_GUID = :studentGuid", nativeQuery=true)
    long countStudentGuidPenXrefRecord(@Param("studentGuid") UUID studentGuid);

    @Query(value="select STUDENT_GUID from STUDENT_GUID_PEN_XREF \n"
            + "where STUDENT_PEN = :pen", nativeQuery = true)
    byte[] findStudentID(@Param("pen") String pen);
}
