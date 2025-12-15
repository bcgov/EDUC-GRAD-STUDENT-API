package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.OptionalProgramCodes;
import ca.bc.gov.educ.api.gradstudent.constant.v1.YukonReportHeader;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.exception.GradStudentAPIRuntimeException;
import ca.bc.gov.educ.api.gradstudent.model.dto.DownloadableReportResponse;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationData;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.OptionalProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.District;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramRepository;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class CSVReportService {

    private final RestUtils restUtils;
    private final GraduationStudentRecordRepository graduationStudentRecordRepository;
    private final StudentOptionalProgramRepository studentOptionalProgramRepository;

    public DownloadableReportResponse generateYukonReport(UUID districtID, String fromDate, String toDate) {
        var district = restUtils.getDistrictByDistrictID(districtID.toString()).orElseThrow(() -> new EntityNotFoundException(District.class, "districtID", districtID.toString()));
        List<OptionalProgramCode> optionalProgramCodes = restUtils.getOptionalProgramCodeList();

        List<UUID> schoolsInDistrict = restUtils.getSchoolList()
                .stream()
                .filter(school -> Objects.equals(school.getDistrictId(), district.getDistrictId()))
                .map(School::getSchoolId)
                .map(UUID::fromString)
                .toList();

        Date from = Date.valueOf(LocalDate.parse(fromDate));
        Date to = Date.valueOf(LocalDate.parse(toDate));

        List<GraduationStudentRecordEntity> results = graduationStudentRecordRepository.findByProgramCompletionDateIsGreaterThanEqualAndProgramCompletionDateIsLessThanEqualAndSchoolAtGradIdIn(from, to, schoolsInDistrict);
        List<String> headers = Arrays.stream(YukonReportHeader.values()).map(YukonReportHeader::getCode).toList();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                .build();
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(byteArrayOutputStream));
            CSVPrinter csvPrinter = new CSVPrinter(writer, csvFormat);

            csvPrinter.printRecord(headers);
            for (GraduationStudentRecordEntity result : results) {
                var school = restUtils.getSchoolBySchoolID(result.getSchoolAtGradId().toString()).orElseThrow(() -> new EntityNotFoundException(School.class, "schoolAtGradID", result.getSchoolAtGradId().toString()));
                List<String> csvRowData = prepareDataForCsv(result, school, optionalProgramCodes);
                csvPrinter.printRecord(csvRowData);
            }
            csvPrinter.flush();

            var downloadableReport = new DownloadableReportResponse();
            downloadableReport.setReportType("yukon-report");
            downloadableReport.setDocumentData(Base64.getEncoder().encodeToString(byteArrayOutputStream.toByteArray()));

            return downloadableReport;
        } catch (IOException e) {
            throw new GradStudentAPIRuntimeException(e);
        }
    }

    private List<String> prepareDataForCsv(GraduationStudentRecordEntity student, School school, List<OptionalProgramCode> optionalProgramCodes) {
        var studentData = StringUtils.isNotBlank(student.getStudentGradData()) ? deriveStudentData(student) : null;
        return new ArrayList<>(Arrays.asList(
                school.getMincode(),
                school.getDisplayName(),
                studentData != null &&  studentData.getGradStudent() != null ? studentData.getGradStudent().getPen() : "",
                studentData != null &&  studentData.getGradStudent() != null ? studentData.getGradStudent().getLegalLastName() : "",
                studentData != null &&  studentData.getGradStudent() != null ? studentData.getGradStudent().getLegalFirstName() : "",
                studentData != null &&  studentData.getGradStudent() != null ? studentData.getGradStudent().getLegalMiddleNames() : "",
                student.getProgram(),
                getOptionalProgram(student.getStudentID(), student.getProgram(), optionalProgramCodes),
                student.getProgramCompletionDate() != null ? EducGradStudentApiUtils.formatDate(student.getProgramCompletionDate(), EducGradStudentApiConstants.YUKON_DATE_FORMAT) : ""
        ));
    }

    private String getOptionalProgram(UUID studentID, String gradProgram, List<OptionalProgramCode> optionalProgramCodes) {
        var fiProgram = optionalProgramCalc(studentID, gradProgram, optionalProgramCodes, OptionalProgramCodes.FI.getCode());
        var ddProgram = optionalProgramCalc(studentID, gradProgram, optionalProgramCodes, OptionalProgramCodes.DD.getCode());
        var prog = StringUtils.isBlank(fiProgram) ? ddProgram : fiProgram;
        return  StringUtils.isNotBlank(fiProgram) && StringUtils.isNotBlank(ddProgram) ? fiProgram + "," + ddProgram : prog;
    }

    private GraduationData deriveStudentData(GraduationStudentRecordEntity studentRecord) {
        GraduationData graduationData;
        try {
            graduationData = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(studentRecord.getStudentGradData(), GraduationData.class);
        } catch (Exception e) {
            throw new GradStudentAPIRuntimeException(e);
        }
        return graduationData;
    }

    private String optionalProgramCalc(UUID studentID, String gradProgram, List<OptionalProgramCode> optionalProgramCodes, String programCode) {
        var studentOptionalProgram = studentOptionalProgramRepository.findByStudentID(studentID);
        var optProgram = getOptionalProgramCode(optionalProgramCodes, programCode,  gradProgram);

        if(optProgram.isPresent()) {
            var hasProgram = studentOptionalProgram.stream()
                    .anyMatch(optProg -> Objects.equals(optProg.getOptionalProgramID(), optProgram.get().getOptionalProgramID()));

            return hasProgram ? optProgram.get().getOptionalProgramName() : "";
        }
       return "";
    }

    private Optional<OptionalProgramCode> getOptionalProgramCode(List<OptionalProgramCode> optionalProgramCodes, String incomingProgramCode, String gradProgram) {
        return  optionalProgramCodes
                .stream()
                .filter(program -> program.getOptProgramCode().equalsIgnoreCase(incomingProgramCode)
                        && StringUtils.isNotBlank(gradProgram)
                        && StringUtils.isNotBlank(program.getGraduationProgramCode())
                        && program.getGraduationProgramCode().equalsIgnoreCase(gradProgram)).findFirst();
    }
}
