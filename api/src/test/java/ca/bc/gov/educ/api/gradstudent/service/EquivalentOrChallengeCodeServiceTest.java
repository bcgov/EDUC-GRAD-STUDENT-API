package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.entity.EquivalentOrChallengeCodeEntity;
import ca.bc.gov.educ.api.gradstudent.repository.EquivalentOrChallengeCodeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class EquivalentOrChallengeCodeServiceTest  extends BaseIntegrationTest {

    @Autowired
    private EquivalentOrChallengeCodeService equivalentOrChallengeCodeService;

    @MockBean
    private EquivalentOrChallengeCodeRepository equivalentOrChallengeCodeRepository;

    @Test
    public void testGetEquivalentOrChallengeCodeList() {
        EquivalentOrChallengeCodeEntity obj = new EquivalentOrChallengeCodeEntity();
        obj.setEquivalentOrChallengeCode("equivalentOrChallengeCode");
        obj.setDescription("EquivalentOrChallengeCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        Mockito.when(equivalentOrChallengeCodeRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"))).thenReturn(List.of(obj));
        var result = equivalentOrChallengeCodeService.findAll();
        assertNotNull(result);
        assertThat(result).isNotEmpty();
    }

    @Test
    public void testGetEquivalentOrChallengeCode() {
        EquivalentOrChallengeCodeEntity obj = new EquivalentOrChallengeCodeEntity();
        obj.setEquivalentOrChallengeCode("equivalentOrChallengeCode");
        obj.setDescription("EquivalentOrChallengeCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        Mockito.when(equivalentOrChallengeCodeRepository.findById("equivalentOrChallengeCode")).thenReturn(Optional.of(obj));
        var result = equivalentOrChallengeCodeService.findByEquivalentOrChallengeCode("equivalentOrChallengeCode");
        assertNotNull(result);
    }

    @Test
    public void testGetEquivalentOrChallengeCode_noContent() {
        Mockito.when(equivalentOrChallengeCodeRepository.findById("equivalentOrChallengeCode")).thenReturn(Optional.empty());
        var result = equivalentOrChallengeCodeService.findByEquivalentOrChallengeCode("equivalentOrChallengeCode");
        assertNull(result);

    }
}
