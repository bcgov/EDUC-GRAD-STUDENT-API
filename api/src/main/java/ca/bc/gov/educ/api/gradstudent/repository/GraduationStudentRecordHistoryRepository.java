package ca.bc.gov.educ.api.gradstudent.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;

@Repository
public interface GraduationStudentRecordHistoryRepository extends JpaRepository<GraduationStudentRecordHistoryEntity, UUID> {

    List<GraduationStudentRecordHistoryEntity> findAll();
	List<GraduationStudentRecordHistoryEntity> findByStudentID(UUID studentID);
    Page<GraduationStudentRecordHistoryEntity> findByBatchId(Long batchId, Pageable paging);
    void deleteByStudentID(UUID studentID);

    @Modifying
    @Query(value="update GRADUATION_STUDENT_RECORD_HISTORY set UPDATE_USER = :updateUser, UPDATE_DATE = :updateDate where BATCH_ID = :batchId", nativeQuery=true)
    void updateGradStudentUpdateUser(@Param(value = "batchId") Long batchId, @Param(value = "updateUser") String updateUser, @Param(value = "updateDate") LocalDateTime updateDate);

}
