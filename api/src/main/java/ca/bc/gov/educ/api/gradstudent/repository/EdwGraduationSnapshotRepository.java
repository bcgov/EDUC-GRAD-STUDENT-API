package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.EdwSnapshotID;
import ca.bc.gov.educ.api.gradstudent.model.entity.EdwGraduationSnapshotEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * The interface EDW Graduation Status repository
 */
@Repository
public interface EdwGraduationSnapshotRepository extends JpaRepository<EdwGraduationSnapshotEntity, EdwSnapshotID> {

    Optional<EdwGraduationSnapshotEntity> findByGradYearAndPen(Integer gradYear, String pen);
    List<EdwGraduationSnapshotEntity> findByGradYear(Integer gradYear);

    Page<EdwGraduationSnapshotEntity> findByGradYear(Integer gradYear, Pageable pageable);

    Integer countAllByGradYear(Integer gradYear);

}
