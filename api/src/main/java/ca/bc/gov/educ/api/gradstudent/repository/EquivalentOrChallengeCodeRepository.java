package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.EquivalentOrChallengeCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EquivalentOrChallengeCodeRepository extends JpaRepository<EquivalentOrChallengeCodeEntity, String> {
}
