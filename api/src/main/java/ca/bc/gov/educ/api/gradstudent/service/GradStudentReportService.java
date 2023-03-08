package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.gradstudent.model.transformer.ReportGradStudentTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.ReportGradStudentDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GradStudentReportService {

    @Autowired
    ReportGradStudentDataRepository reportGradStudentDataRepository;
    @Autowired
    ReportGradStudentTransformer reportGradStudentTransformer;

    public List<ReportGradStudentData> getGradStudentDataByMincode(String mincode) {
        return reportGradStudentTransformer.transformToDTO(reportGradStudentDataRepository.findReportGradStudentDataEntityByMincodeStartsWithOrderByMincodeAscSchoolNameAscLastNameAsc(mincode));
    }

    public List<ReportGradStudentData> getGradStudentDataByStudentGuids(List<UUID> studentIds) {
        return reportGradStudentTransformer.transformToDTO(reportGradStudentDataRepository.findReportGradStudentDataEntityByGraduationStudentRecordIdInOrderByMincodeAscSchoolNameAscLastNameAsc(studentIds));
    }
}
