package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface GraduationStudentRecordHistoryRepository extends JpaRepository<GraduationStudentRecordHistoryEntity, UUID> {

    List<GraduationStudentRecordHistoryEntity> findAll();
	List<GraduationStudentRecordHistoryEntity> findByStudentID(UUID studentID);
    Page<GraduationStudentRecordHistoryEntity> findByBatchId(Long batchId, Pageable paging);
    void deleteByStudentID(UUID studentID);

    @Modifying
    @Query(value="update GRADUATION_STUDENT_RECORD_HISTORY set UPDATE_USER = :updateUser, UPDATE_DATE = :updateDate where BATCH_ID = :batchId", nativeQuery=true)
    Integer updateGradStudentUpdateUser(@Param(value = "batchId") Long batchId, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

    @Modifying
    @Query(value="update GRADUATION_STUDENT_RECORD_HISTORY set HISTORY_ACTIVITY_CODE = :activityCode, UPDATE_USER = :updateUser, UPDATE_DATE = :updateDate where BATCH_ID = :batchId", nativeQuery=true)
    Integer updateGradStudentUpdateUser(@Param(value = "batchId") Long batchId, @Param(value = "activityCode") String activityCode, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

    @Modifying
    @Query(value="INSERT INTO GRADUATION_STUDENT_RECORD_HISTORY (\n" +
            "    GRADUATION_STUDENT_RECORD_HISTORY_ID,\n" +
            "    HISTORY_ACTIVITY_CODE,\n" +
            "    GRADUATION_STUDENT_RECORD_ID,\n" +
            "    GRADUATION_PROGRAM_CODE,\n" +
            "    GPA,\n" +
            "    STUDENT_STATUS_CODE,\n" +
            "    HONOURS_STANDING,\n" +
            "    PROGRAM_COMPLETION_DATE,\n" +
            "    RECALCULATE_GRAD_STATUS,\n" +
            "    SCHOOL_OF_RECORD,\n" +
            "    STUDENT_GRADE,\n" +
            "    SCHOOL_AT_GRADUATION,\n" +
            "    CREATE_USER,\n" +
            "    CREATE_DATE,\n" +
            "    UPDATE_USER,\n" +
            "    UPDATE_DATE,\n" +
            "    RECALCULATE_PROJECTED_GRAD,\n" +
            "    BATCH_ID,\n" +
            "    CONSUMER_EDUC_REQT_MET,\n" +
            "    STUDENT_CITIZENSHIP_CODE,\n" +
            "    ADULT_START_DATE,\n" +
            "    SCHOOL_OF_RECORD_ID,\n" +
            "    SCHOOL_AT_GRADUATION_ID\n" +
            ") SELECT \n" +
            "    SYS_GUID(),\n" +
            "    :activityCode,\n" +
            "    GRADUATION_STUDENT_RECORD_ID,\n" +
            "    GRADUATION_PROGRAM_CODE,\n" +
            "    GPA,\n" +
            "    STUDENT_STATUS_CODE,\n" +
            "    HONOURS_STANDING,\n" +
            "    PROGRAM_COMPLETION_DATE,\n" +
            "    RECALCULATE_GRAD_STATUS,\n" +
            "    SCHOOL_OF_RECORD,\n" +
            "    STUDENT_GRADE,\n" +
            "    SCHOOL_AT_GRADUATION,\n" +
            "    CREATE_USER,\n" +
            "    CREATE_DATE,\n" +
            "    :updateUser,\n" +
            "    :updateDate,\n" +
            "    RECALCULATE_PROJECTED_GRAD,\n" +
            "    BATCH_ID,\n" +
            "    CONSUMER_EDUC_REQT_MET,\n" +
            "    STUDENT_CITIZENSHIP_CODE,\n" +
            "    ADULT_START_DATE,\n" +
            "    SCHOOL_OF_RECORD_ID,\n" +
            "    SCHOOL_AT_GRADUATION_ID\n" +
            "FROM GRADUATION_STUDENT_RECORD\n" +
            "WHERE BATCH_ID = :batchId", nativeQuery=true)
    Integer insertGraduationStudentRecordHistoryByBatchId(@Param(value = "batchId") Long batchId, @Param(value = "activityCode") String activityCode, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

    @Modifying
    @Query(value="INSERT INTO GRADUATION_STUDENT_RECORD_HISTORY (\n" +
            "    GRADUATION_STUDENT_RECORD_HISTORY_ID,\n" +
            "    HISTORY_ACTIVITY_CODE,\n" +
            "    GRADUATION_STUDENT_RECORD_ID,\n" +
            "    GRADUATION_PROGRAM_CODE,\n" +
            "    GPA,\n" +
            "    STUDENT_STATUS_CODE,\n" +
            "    HONOURS_STANDING,\n" +
            "    PROGRAM_COMPLETION_DATE,\n" +
            "    RECALCULATE_GRAD_STATUS,\n" +
            "    SCHOOL_OF_RECORD,\n" +
            "    STUDENT_GRADE,\n" +
            "    SCHOOL_AT_GRADUATION,\n" +
            "    CREATE_USER,\n" +
            "    CREATE_DATE,\n" +
            "    UPDATE_USER,\n" +
            "    UPDATE_DATE,\n" +
            "    RECALCULATE_PROJECTED_GRAD,\n" +
            "    BATCH_ID,\n" +
            "    CONSUMER_EDUC_REQT_MET,\n" +
            "    STUDENT_CITIZENSHIP_CODE,\n" +
            "    ADULT_START_DATE,\n" +
            "    SCHOOL_OF_RECORD_ID,\n" +
            "    SCHOOL_AT_GRADUATION_ID\n" +
            ") SELECT \n" +
            "    SYS_GUID(),\n" +
            "    :activityCode,\n" +
            "    GRADUATION_STUDENT_RECORD_ID,\n" +
            "    GRADUATION_PROGRAM_CODE,\n" +
            "    GPA,\n" +
            "    STUDENT_STATUS_CODE,\n" +
            "    HONOURS_STANDING,\n" +
            "    PROGRAM_COMPLETION_DATE,\n" +
            "    RECALCULATE_GRAD_STATUS,\n" +
            "    SCHOOL_OF_RECORD,\n" +
            "    STUDENT_GRADE,\n" +
            "    SCHOOL_AT_GRADUATION,\n" +
            "    CREATE_USER,\n" +
            "    CREATE_DATE,\n" +
            "    :updateUser,\n" +
            "    :updateDate,\n" +
            "    RECALCULATE_PROJECTED_GRAD,\n" +
            "    BATCH_ID,\n" +
            "    CONSUMER_EDUC_REQT_MET,\n" +
            "    STUDENT_CITIZENSHIP_CODE,\n" +
            "    ADULT_START_DATE,\n" +
            "    SCHOOL_OF_RECORD_ID,\n" +
            "    SCHOOL_AT_GRADUATION_ID\n" +
            "FROM GRADUATION_STUDENT_RECORD\n" +
            "WHERE BATCH_ID = :batchId and GRADUATION_STUDENT_RECORD_ID IN (:studentIDs)", nativeQuery=true)
    Integer insertGraduationStudentRecordHistoryByBatchIdAndStudentIDs(@Param(value = "batchId") Long batchId, @Param(value = "studentIDs") List<UUID> studentIDs, @Param(value = "activityCode") String activityCode, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

}
