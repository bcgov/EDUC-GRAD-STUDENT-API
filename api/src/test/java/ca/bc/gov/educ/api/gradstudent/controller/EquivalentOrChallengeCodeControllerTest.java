package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.EquivalentOrChallengeCode;
import ca.bc.gov.educ.api.gradstudent.service.EquivalentOrChallengeCodeService;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class EquivalentOrChallengeCodeControllerTest {

    @Mock
    private EquivalentOrChallengeCodeService equivalentOrChallengeCodeService;
    @InjectMocks
    private EquivalentOrChallengeCodeController equivalentOrChallengeCodeController;

    @Test
    public void testGetEquivalentOrChallengeCodeList() {
        List<EquivalentOrChallengeCode> equivalentOrChallengeCodes = new ArrayList<>();
        EquivalentOrChallengeCode obj = new EquivalentOrChallengeCode();
        obj.setEquivalentOrChallengeCode("equivalentOrChallengeCode");
        obj.setDescription("EquivalentOrChallengeCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        equivalentOrChallengeCodes.add(obj);
        Mockito.when(equivalentOrChallengeCodeService.findAll()).thenReturn(equivalentOrChallengeCodes);
        ResponseEntity response = equivalentOrChallengeCodeController.getEquivalentOrChallengeCodes();
        Mockito.verify(equivalentOrChallengeCodeService).findAll();
        assertNotNull(response.getBody());
    }

    @Test
    public void testGetEquivalentOrChallengeCode() {
        EquivalentOrChallengeCode obj = new EquivalentOrChallengeCode();
        obj.setEquivalentOrChallengeCode("equivalentOrChallengeCode");
        obj.setDescription("EquivalentOrChallengeCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        Mockito.when(equivalentOrChallengeCodeService.findByEquivalentOrChallengeCode("equivalentOrChallengeCode")).thenReturn(obj);
        equivalentOrChallengeCodeController.getEquivalentOrChallengeCode("equivalentOrChallengeCode");
        Mockito.verify(equivalentOrChallengeCodeService).findByEquivalentOrChallengeCode("equivalentOrChallengeCode");
    }
}
