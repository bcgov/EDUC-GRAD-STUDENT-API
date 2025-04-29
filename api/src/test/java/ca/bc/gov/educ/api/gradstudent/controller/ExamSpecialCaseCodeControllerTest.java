package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.ExamSpecialCaseCode;
import ca.bc.gov.educ.api.gradstudent.service.ExamSpecialCaseCodeService;
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
public class ExamSpecialCaseCodeControllerTest {

    @Mock
    private ExamSpecialCaseCodeService examSpecialCaseCodeService;

    @InjectMocks
    private ExamSpecialCaseCodeController examSpecialCaseCodeController;

    @Test
    public void testGetExamSpecialCaseCodeList() {
        List<ExamSpecialCaseCode> examSpecialCaseCodes = new ArrayList<>();
        ExamSpecialCaseCode obj = new ExamSpecialCaseCode();
        obj.setExamSpecialCaseCode("examSpecialCaseCode");
        obj.setDescription("ExamSpecialCaseCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        examSpecialCaseCodes.add(obj);
        Mockito.when(examSpecialCaseCodeService.findAll()).thenReturn(examSpecialCaseCodes);
        examSpecialCaseCodeController.getExamSpecialCaseCodes();
        Mockito.verify(examSpecialCaseCodeService).findAll();

    }

    @Test
    public void testGetExamSpecialCaseCode() {
        ExamSpecialCaseCode obj = new ExamSpecialCaseCode();
        obj.setExamSpecialCaseCode("examSpecialCaseCode");
        obj.setDescription("ExamSpecialCaseCode Description");
        obj.setCreateUser("GRADUATION");
        obj.setUpdateUser("GRADUATION");
        obj.setCreateDate(LocalDateTime.now());
        obj.setUpdateDate(LocalDateTime.now());
        Mockito.when(examSpecialCaseCodeService.findBySpecialCaseCode("examSpecialCaseCode")).thenReturn(obj);
        examSpecialCaseCodeController.getExamSpecialCaseCode("examSpecialCaseCode");
        Mockito.verify(examSpecialCaseCodeService).findBySpecialCaseCode("examSpecialCaseCode");
    }
}