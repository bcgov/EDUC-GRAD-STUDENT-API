package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.FineArtsAppliedSkillsCode;
import ca.bc.gov.educ.api.gradstudent.service.FineArtsAppliedSkillsCodeService;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@ExtendWith(MockitoExtension.class)
public class FineArtsAppliedSkillsCodeControllerTest {

    @Mock
    private FineArtsAppliedSkillsCodeService fineArtsAppliedSkillsCodeService;

    @InjectMocks
    private FineArtsAppliedSkillsCodeController fineArtsAppliedSkillsCodeController;

    @Test
    public void testGetFineArtsAppliedSkillsCodeList() {
        List<FineArtsAppliedSkillsCode> fineArtsAppliedSkillsCodes = new ArrayList<>();
        FineArtsAppliedSkillsCode obj = new FineArtsAppliedSkillsCode();
        obj.setFineArtsAppliedSkillsCode("fineArtsAppliedSkillsCode");
        obj.setDescription("FineArtsAppliedSkillsCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        fineArtsAppliedSkillsCodes.add(obj);
        Mockito.when(fineArtsAppliedSkillsCodeService.findAll()).thenReturn(fineArtsAppliedSkillsCodes);
        fineArtsAppliedSkillsCodeController.getFineArtsAppliedSkillsCodes();
        Mockito.verify(fineArtsAppliedSkillsCodeService).findAll();

    }

    @Test
    public void testGetFineArtsAppliedSkillsCode() {
        FineArtsAppliedSkillsCode obj = new FineArtsAppliedSkillsCode();
        obj.setFineArtsAppliedSkillsCode("fineArtsAppliedSkillsCode");
        obj.setDescription("FineArtsAppliedSkillsCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        Mockito.when(fineArtsAppliedSkillsCodeService.findByFineArtsAppliedSkillsCode("fineArtsAppliedSkillsCode")).thenReturn(obj);
        fineArtsAppliedSkillsCodeController.getFineArtsAppliedSkillsCode("fineArtsAppliedSkillsCode");
        Mockito.verify(fineArtsAppliedSkillsCodeService).findByFineArtsAppliedSkillsCode("fineArtsAppliedSkillsCode");
    }

}
