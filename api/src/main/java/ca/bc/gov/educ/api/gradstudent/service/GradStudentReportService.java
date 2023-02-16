package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.gradstudent.model.transformer.ReportGradStudentTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.ReportGradStudentDataRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class GradStudentReportService {

    final EducGradStudentApiConstants constants;
    final WebClient webClient;
    final ReportGradStudentDataRepository reportGradStudentDataRepository;
    final ReportGradStudentTransformer reportGradStudentTransformer;

    @Autowired
    public GradStudentReportService(EducGradStudentApiConstants constants, WebClient webClient, ReportGradStudentDataRepository reportGradStudentDataRepository, ReportGradStudentTransformer reportGradStudentTransformer) {
        this.constants = constants;
        this.webClient = webClient;
        this.reportGradStudentDataRepository = reportGradStudentDataRepository;
        this.reportGradStudentTransformer = reportGradStudentTransformer;
    }

    public List<ReportGradStudentData> getGradStudentDataByMincode(String mincode) {
        return reportGradStudentTransformer.transformToDTO(reportGradStudentDataRepository.findReportGradStudentDataEntityByMincodeStartsWithOrderBySchoolNameAscLastNameAsc(mincode));
    }

}
