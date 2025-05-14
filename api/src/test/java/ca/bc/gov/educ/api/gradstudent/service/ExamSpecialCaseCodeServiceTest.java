package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.entity.ExamSpecialCaseCodeEntity;
import ca.bc.gov.educ.api.gradstudent.repository.ExamSpecialCaseCodeRepository;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ExamSpecialCaseCodeServiceTest  extends BaseIntegrationTest {

    @Autowired
    private ExamSpecialCaseCodeService examSpecialCaseCodeService;

    @MockBean
    private ExamSpecialCaseCodeRepository examSpecialCaseCodeRepository;

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
    public void testGetExamSpecialCaseCodeList() {
        List<ExamSpecialCaseCodeEntity> examSpecialCaseCodes = new ArrayList<>();
        ExamSpecialCaseCodeEntity obj = new ExamSpecialCaseCodeEntity();
        obj.setExamSpecialCaseCode("examSpecialCaseCode");
        obj.setDescription("ExamSpecialCaseCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        examSpecialCaseCodes.add(obj);
        Mockito.when(examSpecialCaseCodeRepository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"))).thenReturn(examSpecialCaseCodes);
        var result = examSpecialCaseCodeService.findAll();
        assertNotNull(result);
        assertThat(result).isNotEmpty();
    }

    @Test
    public void testGetExamSpecialCaseCode() {
        ExamSpecialCaseCodeEntity obj = new ExamSpecialCaseCodeEntity();
        obj.setExamSpecialCaseCode("examSpecialCaseCode");
        obj.setDescription("ExamSpecialCaseCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        Mockito.when(examSpecialCaseCodeRepository.findById("examSpecialCaseCode")).thenReturn(Optional.of(obj));
        var result = examSpecialCaseCodeService.findBySpecialCaseCode("examSpecialCaseCode");
        assertNotNull(result);
    }

    @Test
    public void testGetExamSpecialCaseCode_noContent() {
        Mockito.when(examSpecialCaseCodeRepository.findById("examSpecialCaseCode")).thenReturn(Optional.empty());
        var result = examSpecialCaseCodeService.findBySpecialCaseCode("examSpecialCaseCode");
        assertNull(result);
    }
}
