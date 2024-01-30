package ca.bc.gov.educ.api.gradstudent.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;

@Repository
public interface GraduationStudentRecordHistoryRepository extends JpaRepository<GraduationStudentRecordHistoryEntity, UUID> {

    List<GraduationStudentRecordHistoryEntity> findAll();
	List<GraduationStudentRecordHistoryEntity> findByStudentID(UUID studentID);
    Page<GraduationStudentRecordHistoryEntity> findByBatchId(Long batchId, Pageable paging);
    GraduationStudentRecordHistoryEntity findByBatchId(Long batchId);
    void deleteByStudentID(UUID studentID);
}
