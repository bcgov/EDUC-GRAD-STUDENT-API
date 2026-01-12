package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.dto.BatchGraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationCountProjection;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GraduationStudentRecordRepository extends JpaRepository<GraduationStudentRecordEntity, UUID> {

	boolean existsByStudentID(UUID studentId);

    List<GraduationStudentRecordEntity> findAll();

	@Query("select c.studentID from GraduationStudentRecordEntity c where c.recalculateGradStatus=:recalculateFlag and c.studentStatus <> 'MER'")
	List<UUID> findByRecalculateGradStatusForBatch(String recalculateFlag);

	@Query("select c.studentID from GraduationStudentRecordEntity c where c.recalculateProjectedGrad=:recalculateProjectedGrad and c.studentStatus <> 'MER'")
	List<UUID> findByRecalculateProjectedGradForBatch(String recalculateProjectedGrad);

	@Query("select new ca.bc.gov.educ.api.gradstudent.model.dto.BatchGraduationStudentRecord(c.program,c.programCompletionDate,c.schoolOfRecordId, c.studentID) from GraduationStudentRecordEntity c where c.studentID=:studentID")
	Optional<BatchGraduationStudentRecord> findByStudentIDForBatch(UUID studentID);

	GraduationStudentRecordEntity findByStudentID(UUID studentID);

	Optional<GraduationStudentRecordEntity> findOptionalByStudentID(UUID studentID);

	@Query("select c from GraduationStudentRecordEntity c where c.studentStatus=:statusCode")
	List<GraduationStudentRecordEntity> existsByStatusCode(String statusCode);
	
	@Query(value="SELECT si.* FROM graduation_student_record si where "
			+ "(:gradProgram is null or si.graduation_program_code = :gradProgram) and "
			+ "(:schoolOfRecord is null or si.school_of_record = :schoolOfRecord)",nativeQuery = true)
	public Page<GraduationStudentRecordEntity> findStudentWithFilter(String gradProgram,String schoolOfRecord, Pageable paging);

    List<GraduationStudentRecordView> findByStudentIDIn(List<UUID> studentIds);

	@Query("select c.studentID from GraduationStudentRecordEntity c where c.programCompletionDate is null and c.studentStatus='CUR' and (c.studentGrade='AD' or c.studentGrade='12')")
	Page<UUID> findStudentsForYearlyDistribution(Pageable page);

	List<GraduationStudentRecordView> findBySchoolOfRecordIdAndStudentStatus(UUID schoolOfRecordId, String studentStatus);

	@Query("select c.studentID from GraduationStudentRecordEntity c where c.schoolOfRecordId IN (:schoolOfRecordIds) and c.studentStatus=:studentStatus")
	List<UUID> findBySchoolOfRecordIdInAndStudentStatus(List<UUID> schoolOfRecordIds, String studentStatus);

	@Query("select c.studentID from GraduationStudentRecordEntity c where c.schoolOfRecordId IN (:schoolOfRecordIds)")
	List<UUID> findBySchoolOfRecordIdIn(List<UUID> schoolOfRecordIds);

    @Query("select c.studentID " +
            "from GraduationStudentRecordEntity c " +
            "where c.schoolOfRecordId in :schoolOfRecordIds " +
            "and c.studentGrade in :studentGrades " +
			"and c.studentStatus in :statuses " +
            "and c.program in :graduationProgramCodes ")
    List<UUID> findCurrentStudentUUIDsByProgramInAndSchoolOfRecordInAndGradeIn(@Param("graduationProgramCodes") List<String> graduationProgramCodes, @Param("studentGrades") List<String> studentGrades, @Param("schoolOfRecordIds") List<UUID> schoolOfRecordIds, @Param("statuses") List<String> statuses);

	@Query("select c.studentID from GraduationStudentRecordEntity c where c.studentStatus=:studentStatus")
	List<UUID> findByStudentStatus(String studentStatus);

	@Query("select c.studentID from GraduationStudentRecordEntity c where c.studentStatus=:studentStatus")
	Page<UUID> findByStudentStatus(String studentStatus, Pageable paging);

	@Query("select distinct c.studentID from GraduationStudentRecordEntity c")
	List<UUID> findAllStudentGuids();

	List<GraduationStudentRecordView> findBySchoolOfRecordIdAndStudentStatusAndStudentGradeIn(UUID schoolOfRecordId, String studentStatus, List<String> studentGrade);

	@Query("select count(*) from GraduationStudentRecordEntity c where c.schoolOfRecordId=:schoolOfRecordId and c.studentStatus='CUR' and (c.studentGrade='AD' or c.studentGrade='12')")
	Integer countBySchoolOfRecordAmalgamated(UUID schoolOfRecordId);

	@Query("select count(*) from GraduationStudentRecordEntity c where c.schoolOfRecordId IN (:schoolOfRecordIds) and c.studentStatus=:studentStatus")
	Long countBySchoolOfRecordsAndStudentStatus(List<UUID> schoolOfRecordIds, String studentStatus);

	@Query("select count(*) from GraduationStudentRecordEntity c where c.schoolOfRecordId IN (:schoolOfRecordIds)")
	Long countBySchoolOfRecords(List<UUID> schoolOfRecordIds);

	@Query("select count(*) from GraduationStudentRecordEntity c where c.studentStatus=:studentStatus")
	Long countByStudentStatus(String studentStatus);

	@Modifying
	@Query(value="update graduation_student_record set student_status_code = :inStudStatTo, batch_id = :batchId, student_grad_data = json_transform(student_grad_data, SET '$.gradStatus.studentStatus' = :inStudStatTo IGNORE ON MISSING), update_date = SYSDATE, update_user = :userName where school_of_record_id in (:inSor) and student_status_code = :inStudStatFrom", nativeQuery=true)
	Integer archiveStudents(List<UUID> inSor, String inStudStatFrom, String inStudStatTo, long batchId, String userName);

	@Modifying
	@Query(value="update graduation_student_record set student_status_code = :inStudStatTo, batch_id = :batchId, student_grad_data = json_transform(student_grad_data, SET '$.gradStatus.studentStatus' = :inStudStatTo IGNORE ON MISSING), update_date = SYSDATE, update_user = :userName where student_status_code = :inStudStatFrom", nativeQuery=true)
	Integer archiveStudents(String inStudStatFrom, String inStudStatTo, long batchId, String userName);

	// Data Conversion
	/**
	 * Obtain student ids from student api. xref table to be removed.
	 * @deprecated since 1.27.0 — use {@link ca.bc.gov.educ.api.gradstudent.service.GradStudentService#getStudentIDsByPens(List)} instead.
	 */
	@Deprecated(forRemoval = true)
	@Modifying
    @Query(value="insert into STUDENT_GUID_PEN_XREF(STUDENT_GUID, STUDENT_PEN, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)\n"
            + "values (:studentGuid, :pen, :userName, :currentTime, :userName, :currentTime) ", nativeQuery=true)
    void createStudentGuidPenXrefRecord(
            @Param("studentGuid") UUID studentGuid,
            @Param("pen") String pen,
            @Param("userName") String userName,
            @Param("currentTime") LocalDateTime currentTime);

	/**
	 * Obtain student ids from student api. xref table to be removed.
	 * @deprecated since 1.27.0 — use {@link ca.bc.gov.educ.api.gradstudent.service.GradStudentService#getStudentIDsByPens(List)} instead.
	 */
	@Deprecated(forRemoval = true)
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

	/**
	 * Obtain student ids from student api. xref table to be removed.
	 * @deprecated since 1.27.0 — use {@link ca.bc.gov.educ.api.gradstudent.service.GradStudentService#getStudentIDsByPens(List)} instead.
	 */
	@Deprecated(forRemoval = true)
	@Query(value="select e.studentID from StudentGuidPenXrefEntity e where e.pen in (:pens)")
	List<UUID> findStudentIDsByPenIn(@Param("pens") List<String> pens);

	@Query("select c.studentID from GraduationStudentRecordEntity c where c.studentStatus = :statusCode and c.studentID in :studentIDList")
	List<UUID> filterGivenStudentsByStatusCode(@Param("studentIDList") List<UUID> studentIDs, @Param("statusCode") String statusCode);

	// Student Status
	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.studentStatus = :statusCode, e.updateUser = :updateUser, e.updateDate = :updateDate where e.studentID = :studentGuid")
	void updateStudentStatus(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "statusCode") String statusCode, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

	// Student Grade
	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.studentGrade = :studentGrade, e.updateUser = :updateUser, e.updateDate = :updateDate where e.studentID = :studentGuid")
	void updateStudentGrade(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "studentGrade") String studentGrade, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

	// GRAD Program
	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.program = :gradProgramCode, e.updateUser = :updateUser, e.updateDate = :updateDate where e.studentID = :studentGuid")
	void updateGradProgram(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "gradProgramCode") String gradProgramCode, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

	// School of Record ID
	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.schoolOfRecordId = :schoolGuid, e.updateUser = :updateUser, e.updateDate = :updateDate where e.studentID = :studentGuid")
	void updateSchoolOfRecordId(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "schoolGuid") UUID schoolGuid, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

	// Citizenship
	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.studentCitizenship = :citizenshipCode, e.updateUser = :updateUser, e.updateDate = :updateDate where e.studentID = :studentGuid")
	void updateStudentCitizenship(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "citizenshipCode") String citizenshipCode, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

	// Adult Start Date
	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.adultStartDate = :adultStartDate, e.updateUser = :updateUser, e.updateDate = :updateDate where e.studentID = :studentGuid")
	void updateAdultStartDate(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "adultStartDate") Date adultStartDate, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

	// Program Completion Date
	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.programCompletionDate = :programCompletionDate, e.updateUser = :updateUser, e.updateDate = :updateDate where e.studentID = :studentGuid")
	void updateProgramCompletionDate(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "programCompletionDate") Date programCompletionDate, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

	// Graduation Status Clob Data
	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.studentGradData = :gradStatusClob, e.updateUser = :updateUser, e.updateDate = :updateDate where e.studentID = :studentGuid")
	void updateGradStatusClob(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "gradStatusClob") String gradStatusClob, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

	// Projected Graduation Status Clob Data
	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.studentProjectedGradData = :projectedGradClob, e.updateUser = :updateUser, e.updateDate = :updateDate where e.studentID = :studentGuid")
	void updateProjectedGradClob(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "projectedGradClob") String projectedGradClob, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

	// Recalculate Graduation Status Flag
	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.recalculateGradStatus = :recalculateGradStatus, e.updateUser = :updateUser, e.updateDate = :updateDate where e.studentID = :studentGuid")
	void updateRecalculateGradStatusFlag(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "recalculateGradStatus") String recalculateGradStatus, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

	// Recalculate Projected Graduation Flag
	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.recalculateProjectedGrad = :recalculateProjectedGrad, e.updateUser = :updateUser, e.updateDate = :updateDate where e.studentID = :studentGuid")
	void updateRecalculateProjectedGradFlag(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "recalculateProjectedGrad") String recalculateProjectedGrad, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.recalculateGradStatus = :recalculateGradStatus, e.recalculateProjectedGrad = :recalculateProjectedGrad where e.studentID = :studentGuid")
	void updateGradStudentRecalculationAllFlags(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "recalculateGradStatus") String recalculateGradStatus, @Param(value = "recalculateProjectedGrad") String recalculateProjectedGrad);

	@Modifying
	@Query("update GraduationStudentRecordEntity e set e.recalculateGradStatus = :recalculateGradStatus where e.studentID = :studentGuid")
	void updateGradStudentRecalculationRecalculateGradStatusFlag(@Param(value = "studentGuid") UUID studentGuid, @Param(value = "recalculateGradStatus") String recalculateGradStatus);

	@Modifying
	@Query( "update GraduationStudentRecordEntity e set e.recalculateProjectedGrad = 'Y' where e.studentStatus = 'CUR' and e.programCompletionDate is null and (e.studentGrade = '12' or e.studentGrade = 'AD')")
	void updateGradStudentRecalcFlagsForCurrentStudentsWithNullCompletion();

	@Modifying
	@Query( "update GraduationStudentRecordEntity e set e.batchId = :batchId where e.studentID in :studentIDs")
	Integer updateGraduationStudentRecordEntitiesBatchIdWhereStudentIDsIn(Long batchId, List<UUID> studentIDs);

	@Modifying
	@Query( "update GraduationStudentRecordEntity e set e.batchId = :batchId where e.studentStatus = :studentStatus")
	Integer updateGraduationStudentRecordEntitiesBatchIdWhereStudentStatus(Long batchId, String studentStatus);

	@Query("SELECT gsr.schoolOfRecordId AS schoolOfRecordId, " +
			"COUNT(CASE WHEN gsr.studentStatus = 'CUR' AND gsr.programCompletionDate IS NOT NULL AND gsr.program <> 'SCCP' THEN 1 ELSE NULL END) AS currentGraduates, " +
			"COUNT(CASE WHEN gsr.studentStatus = 'CUR' AND gsr.programCompletionDate IS NULL AND gsr.studentGrade = '12' AND gsr.program IS NOT NULL AND gsr.program <> 'SCCP' THEN 1 ELSE NULL END) AS currentNonGraduates " +
			"FROM GraduationStudentRecordEntity gsr " + 
			"WHERE gsr.schoolOfRecordId IN (:schoolIDs) " +
			"GROUP BY gsr.schoolOfRecordId")
	List<GraduationCountProjection> countCurrentGraduatesAndNonGraduatesBySchoolOfRecordIn(@Param("schoolIDs") List<UUID> schoolIDs);


	/**
	 * Find a GraduationStudentRecord By Student ID using generics. Pass an object with the
	 * same subset of field names, getters/setters of GraduationStudentRecordEntity to return
	 * objects with a subset of values. More info: https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html
	 * @param studentId the student ID
	 * @param type The class type of the object you wish to use
	 * @return
	 * @param <T>
	 */
	<T> T findByStudentID(UUID studentId, Class<T> type);

	List<GraduationStudentRecordEntity> findByProgramCompletionDateIsGreaterThanEqualAndProgramCompletionDateIsLessThanEqualAndSchoolAtGradIdIn(Date startDate, Date endDate, List<UUID> schoolOfRecordIds);
}
