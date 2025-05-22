package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.entity.FineArtsAppliedSkillsCodeEntity;
import ca.bc.gov.educ.api.gradstudent.repository.FineArtsAppliedSkillsCodeRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@SpringBootTest
@RunWith(SpringRunner.class)
public class FineArtsAppliedSkillsCodeServiceTest  extends BaseIntegrationTest {

    @Autowired
    private FineArtsAppliedSkillsCodeService fineArtsAppliedSkillsCodeService;

    @MockBean
    private FineArtsAppliedSkillsCodeRepository fineArtsAppliedSkillsCodeRepository;

    @MockBean
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository;

    @MockBean
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    @MockBean
    public ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    @Qualifier("courseApiClient")
    public WebClient courseApiWebClient;

    @MockBean
    @Qualifier("gradCoregApiClient")
    public WebClient coregApiWebClient;

    @Test
    public void testGetFineArtsAppliedSkillsCodeList() {
        List<FineArtsAppliedSkillsCodeEntity> fineArtsAppliedSkillsCodes = new ArrayList<>();
        FineArtsAppliedSkillsCodeEntity obj = new FineArtsAppliedSkillsCodeEntity();
        obj.setFineArtsAppliedSkillsCode("fineArtsAppliedSkillsCode");
        obj.setDescription("FineArtsAppliedSkillsCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        fineArtsAppliedSkillsCodes.add(obj);
        Mockito.when(fineArtsAppliedSkillsCodeRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"))).thenReturn(fineArtsAppliedSkillsCodes);
        var result = fineArtsAppliedSkillsCodeService.findAll();
        assertNotNull(result);
        assertThat(result).isNotEmpty();
    }

    @Test
    public void testGetFineArtsAppliedSkillsCode() {
        FineArtsAppliedSkillsCodeEntity obj = new FineArtsAppliedSkillsCodeEntity();
        obj.setFineArtsAppliedSkillsCode("fineArtsAppliedSkillsCode");
        obj.setDescription("FineArtsAppliedSkillsCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        Mockito.when(fineArtsAppliedSkillsCodeRepository.findById("fineArtsAppliedSkillsCode")).thenReturn(Optional.of(obj));
        var result = fineArtsAppliedSkillsCodeService.findByFineArtsAppliedSkillsCode("fineArtsAppliedSkillsCode");
        assertNotNull(result);
    }

    @Test
    public void testGetFineArtsAppliedSkillsCode_noContent() {
        Mockito.when(fineArtsAppliedSkillsCodeRepository.findById("fineArtsAppliedSkillsCode")).thenReturn(Optional.empty());
        var result = fineArtsAppliedSkillsCodeService.findByFineArtsAppliedSkillsCode("fineArtsAppliedSkillsCode");
        assertNull(result);
    }
}
