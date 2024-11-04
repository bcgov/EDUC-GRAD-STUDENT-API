package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GraduationStudentRecordHistoryRepository extends JpaRepository<GraduationStudentRecordHistoryEntity, UUID> {

    List<GraduationStudentRecordHistoryEntity> findAll();
	List<GraduationStudentRecordHistoryEntity> findByStudentID(UUID studentID);
    Page<GraduationStudentRecordHistoryEntity> findByBatchId(Long batchId, Pageable paging);
    void deleteByStudentID(UUID studentID);

    @Modifying
    @Query(value="update GRADUATION_STUDENT_RECORD_HISTORY set UPDATE_USER = :updateUser, UPDATE_DATE = SYSDATE where BATCH_ID = :batchId", nativeQuery=true)
    Integer updateGradStudentUpdateUser(@Param(value = "batchId") Long batchId, @Param(value = "updateUser") String updateUser);

    @Modifying
    @Query(value="update GRADUATION_STUDENT_RECORD_HISTORY set HISTORY_ACTIVITY_CODE = :activityCode, UPDATE_USER = :updateUser, UPDATE_DATE = SYSDATE where BATCH_ID = :batchId", nativeQuery=true)
    Integer updateGradStudentUpdateUser(@Param(value = "batchId") Long batchId, @Param(value = "activityCode") String activityCode, @Param(value = "updateUser") String updateUser);

    String INSERT_INTO_GRADUATION_STUDENT_RECORD_HISTORY_BY_BATCH_ID_SQL = """
            INSERT INTO GRADUATION_STUDENT_RECORD_HISTORY (
                    GRADUATION_STUDENT_RECORD_HISTORY_ID,
                    HISTORY_ACTIVITY_CODE,
                    GRADUATION_STUDENT_RECORD_ID,
                    GRADUATION_PROGRAM_CODE,
                    GPA,
                    STUDENT_STATUS_CODE,
                    HONOURS_STANDING,
                    PROGRAM_COMPLETION_DATE,
                    RECALCULATE_GRAD_STATUS,
                    SCHOOL_OF_RECORD,
                    STUDENT_GRADE,
                    SCHOOL_AT_GRADUATION,
                    CREATE_USER,
                    CREATE_DATE,
                    UPDATE_USER,
                    UPDATE_DATE,
                    RECALCULATE_PROJECTED_GRAD,
                    BATCH_ID,
                    CONSUMER_EDUC_REQT_MET,
                    STUDENT_CITIZENSHIP_CODE,
                    ADULT_START_DATE,
                    SCHOOL_OF_RECORD_ID,
                    SCHOOL_AT_GRADUATION_ID
                ) SELECT 
                    SYS_GUID(),
                    :activityCode,
                    GRADUATION_STUDENT_RECORD_ID,
                    GRADUATION_PROGRAM_CODE,
                    GPA,
                    STUDENT_STATUS_CODE,
                    HONOURS_STANDING,
                    PROGRAM_COMPLETION_DATE,
                    RECALCULATE_GRAD_STATUS,
                    SCHOOL_OF_RECORD,
                    STUDENT_GRADE,
                    SCHOOL_AT_GRADUATION,
                    CREATE_USER,
                    CREATE_DATE,
                    :updateUser,
                    SYSDATE,
                    RECALCULATE_PROJECTED_GRAD,
                    BATCH_ID,
                    CONSUMER_EDUC_REQT_MET,
                    STUDENT_CITIZENSHIP_CODE,
                    ADULT_START_DATE,
                    SCHOOL_OF_RECORD_ID,
                    SCHOOL_AT_GRADUATION_ID
                FROM GRADUATION_STUDENT_RECORD
                WHERE BATCH_ID = :batchId
            """;
    @Modifying
    @Query(value= INSERT_INTO_GRADUATION_STUDENT_RECORD_HISTORY_BY_BATCH_ID_SQL, nativeQuery=true)
    Integer insertGraduationStudentRecordHistoryByBatchId(@Param(value = "batchId") Long batchId, @Param(value = "activityCode") String activityCode, @Param(value = "updateUser") String updateUser);

    String INSERT_INTO_GRADUATION_STUDENT_RECORD_HISTORY_BY_BATCH_ID_AND_STUDENT_ID_IN_SQL = """
            INSERT INTO GRADUATION_STUDENT_RECORD_HISTORY (
                GRADUATION_STUDENT_RECORD_HISTORY_ID,
                HISTORY_ACTIVITY_CODE,
                GRADUATION_STUDENT_RECORD_ID,
                GRADUATION_PROGRAM_CODE,
                GPA,
                STUDENT_STATUS_CODE,
                HONOURS_STANDING,
                PROGRAM_COMPLETION_DATE,
                RECALCULATE_GRAD_STATUS,
                SCHOOL_OF_RECORD,
                STUDENT_GRADE,
                SCHOOL_AT_GRADUATION,
                CREATE_USER,
                CREATE_DATE,
                UPDATE_USER,
                UPDATE_DATE,
                RECALCULATE_PROJECTED_GRAD,
                BATCH_ID,
                CONSUMER_EDUC_REQT_MET,
                STUDENT_CITIZENSHIP_CODE,
                ADULT_START_DATE,
                SCHOOL_OF_RECORD_ID,
                SCHOOL_AT_GRADUATION_ID
            ) SELECT 
                SYS_GUID(),
                :activityCode,
                GRADUATION_STUDENT_RECORD_ID,
                GRADUATION_PROGRAM_CODE,
                GPA,
                STUDENT_STATUS_CODE,
                HONOURS_STANDING,
                PROGRAM_COMPLETION_DATE,
                RECALCULATE_GRAD_STATUS,
                SCHOOL_OF_RECORD,
                STUDENT_GRADE,
                SCHOOL_AT_GRADUATION,
                CREATE_USER,
                CREATE_DATE,
                :updateUser,
                SYSDATE,
                RECALCULATE_PROJECTED_GRAD,
                BATCH_ID,
                CONSUMER_EDUC_REQT_MET,
                STUDENT_CITIZENSHIP_CODE,
                ADULT_START_DATE,
                SCHOOL_OF_RECORD_ID,
                SCHOOL_AT_GRADUATION_ID
            FROM GRADUATION_STUDENT_RECORD
            WHERE BATCH_ID = :batchId and GRADUATION_STUDENT_RECORD_ID IN (:studentIDs)
            """;
    @Modifying
    @Query(value= INSERT_INTO_GRADUATION_STUDENT_RECORD_HISTORY_BY_BATCH_ID_AND_STUDENT_ID_IN_SQL, nativeQuery=true)
    Integer insertGraduationStudentRecordHistoryByBatchIdAndStudentIDs(@Param(value = "batchId") Long batchId, @Param(value = "studentIDs") List<UUID> studentIDs, @Param(value = "activityCode") String activityCode, @Param(value = "updateUser") String updateUser);

    @Modifying
    @Query(value= INSERT_INTO_GRADUATION_STUDENT_RECORD_HISTORY_BY_STUDENT_ID_IN_SQL, nativeQuery=true)
    Integer insertGraduationStudentRecordHistoryByStudentId(@Param(value = "studentID") UUID studentID, @Param(value = "activityCode") String activityCode);

    String INSERT_INTO_GRADUATION_STUDENT_RECORD_HISTORY_BY_STUDENT_ID_IN_SQL = """
            INSERT INTO GRADUATION_STUDENT_RECORD_HISTORY (
                GRADUATION_STUDENT_RECORD_HISTORY_ID,
                HISTORY_ACTIVITY_CODE,
                GRADUATION_STUDENT_RECORD_ID,
                GRADUATION_PROGRAM_CODE,
                GPA,
                STUDENT_STATUS_CODE,
                HONOURS_STANDING,
                PROGRAM_COMPLETION_DATE,
                RECALCULATE_GRAD_STATUS,
                SCHOOL_OF_RECORD,
                STUDENT_GRADE,
                SCHOOL_AT_GRADUATION,
                CREATE_USER,
                CREATE_DATE,
                UPDATE_USER,
                UPDATE_DATE,
                RECALCULATE_PROJECTED_GRAD,
                BATCH_ID,
                CONSUMER_EDUC_REQT_MET,
                STUDENT_CITIZENSHIP_CODE,
                ADULT_START_DATE,
                SCHOOL_OF_RECORD_ID,
                SCHOOL_AT_GRADUATION_ID
            ) SELECT 
                SYS_GUID(),
                :activityCode,
                GRADUATION_STUDENT_RECORD_ID,
                GRADUATION_PROGRAM_CODE,
                GPA,
                STUDENT_STATUS_CODE,
                HONOURS_STANDING,
                PROGRAM_COMPLETION_DATE,
                RECALCULATE_GRAD_STATUS,
                SCHOOL_OF_RECORD,
                STUDENT_GRADE,
                SCHOOL_AT_GRADUATION,
                CREATE_USER,
                CREATE_DATE,
                UPDATE_USER,
                SYSDATE,
                RECALCULATE_PROJECTED_GRAD,
                BATCH_ID,
                CONSUMER_EDUC_REQT_MET,
                STUDENT_CITIZENSHIP_CODE,
                ADULT_START_DATE,
                SCHOOL_OF_RECORD_ID,
                SCHOOL_AT_GRADUATION_ID
            FROM GRADUATION_STUDENT_RECORD
            WHERE GRADUATION_STUDENT_RECORD_ID = :studentID 
            """;
}
