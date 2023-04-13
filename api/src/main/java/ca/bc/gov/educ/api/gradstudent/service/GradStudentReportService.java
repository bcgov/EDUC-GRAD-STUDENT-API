package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.Generated;
import ca.bc.gov.educ.api.gradstudent.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.ReportGradStudentTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.ReportGradDistrictYearEndRepository;
import ca.bc.gov.educ.api.gradstudent.repository.ReportGradSchoolYearEndRepository;
import ca.bc.gov.educ.api.gradstudent.repository.ReportGradStudentDataRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService.PAGE_SIZE;

@Service
public class GradStudentReportService {

    private static final Logger logger = LoggerFactory.getLogger(GradStudentReportService.class);

    @Autowired
    ReportGradStudentDataRepository reportGradStudentDataRepository;
    @Autowired
    ReportGradSchoolYearEndRepository reportGradSchoolYearEndRepository;
    @Autowired
    ReportGradDistrictYearEndRepository reportGradDistrictYearEndRepository;
    @Autowired
    ReportGradStudentTransformer reportGradStudentTransformer;

    public List<ReportGradStudentData> getGradStudentDataByMincode(String mincode) {
        return reportGradStudentTransformer.transformToDTO(reportGradStudentDataRepository.findReportGradStudentDataEntityByMincodeStartsWithOrderByMincodeAscSchoolNameAscLastNameAsc(mincode));
    }

    public List<ReportGradStudentData> getGradStudentDataByStudentGuids(List<UUID> studentIds) {
        return reportGradStudentTransformer.transformToDTO(reportGradStudentDataRepository.findReportGradStudentDataEntityByGraduationStudentRecordIdInOrderByMincodeAscSchoolNameAscLastNameAsc(studentIds));
    }

    public List<ReportGradStudentData> getGradStudentDataForNonGradYearEndReport(String mincode) {
        PageRequest nextPage = PageRequest.of(0, PAGE_SIZE);
        if(StringUtils.isBlank(mincode)) {
            throw new IllegalArgumentException("Invalid mincode: " + mincode);
        }
        Page<ReportGradStudentDataEntity> reportGradStudentDataPage;
        if(mincode.length() == 3) {
            reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityByDistcodeAndProgramCompletionDateAndStudentStatusAndStudentGrade(mincode, nextPage);
        } else {
            reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityByMincodeAndProgramCompletionDateAndStudentStatusAndStudentGrade(mincode, nextPage);
        }
        return processReportGradStudentDataList(mincode, reportGradStudentDataPage);
    }

    public List<String> getGradSchoolsForNonGradYearEndReport() {
        return reportGradSchoolYearEndRepository.findAll().stream().map(s->s.getMincode()).toList();
    }

    public List<String> getGradDistrictsForNonGradYearEndReport() {
        return reportGradDistrictYearEndRepository.findAll().stream().map(s->s.getMincode()).toList();
    }

    private List<ReportGradStudentData> processReportGradStudentDataList(String mincode, Page<ReportGradStudentDataEntity> reportGradStudentDataPage) {
        List<ReportGradStudentData> result = new ArrayList<>();
        long startTime = System.currentTimeMillis();
        if(reportGradStudentDataPage.hasContent()) {
            PageRequest nextPage;
            List<ReportGradStudentDataEntity> reportGradStudentDataInBatch = reportGradStudentDataPage.getContent();
            result.addAll(reportGradStudentTransformer.transformToDTO(reportGradStudentDataInBatch));
            final int totalNumberOfPages = reportGradStudentDataPage.getTotalPages();
            logger.debug("Total number of pages: {}, total rows count {}", totalNumberOfPages, reportGradStudentDataPage.getTotalElements());

            List<Callable<Object>> tasks = new ArrayList<>();

            for (int i = 1; i < totalNumberOfPages; i++) {
                nextPage = PageRequest.of(i, PAGE_SIZE);
                ReportGradStudentDataPageTask pageTask = new ReportGradStudentDataPageTask(mincode, nextPage);
                tasks.add(pageTask);
            }

            processReportGradStudentDataTasksAsync(tasks, result, totalNumberOfPages);
        }
        logger.debug("Completed in {} sec, total objects acquired {}", (System.currentTimeMillis() - startTime) / 1000, result.size());
        return result;
    }

    @Generated
    private void processReportGradStudentDataTasksAsync(List<Callable<Object>> tasks, List<ReportGradStudentData> result, int numberOfThreads) {
        if(tasks.isEmpty()) return;
        List<Future<Object>> executionResult;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        try {
            executionResult = executorService.invokeAll(tasks);
            for (Future<?> f : executionResult) {
                Object o = f.get();
                if(o instanceof Pair<?, ?>) {
                    Pair<PageRequest, List<ReportGradStudentData>> taskResult = (Pair<PageRequest, List<ReportGradStudentData>>) o;
                    result.addAll(taskResult.getRight());
                    logger.debug("Page {} processed successfully", taskResult.getLeft().getPageNumber());
                } else {
                    logger.error("Error during the task execution: {}", f.get());
                }
            }
        } catch (InterruptedException | ExecutionException ex) {
            logger.error("Unable to process Student Data: {} ", ex.getLocalizedMessage());
            Thread.currentThread().interrupt();
        } finally {
            executorService.shutdown();
        }
    }

    class ReportGradStudentDataPageTask implements Callable<Object> {

        private final PageRequest pageRequest;
        private final String mincode;

        public ReportGradStudentDataPageTask(String mincode, PageRequest pageRequest) {
            this.pageRequest = pageRequest;
            this.mincode = mincode;
        }

        @Override
        @Generated
        public Object call() throws Exception {
            assert mincode != null;
            Page<ReportGradStudentDataEntity> reportGradStudentDataPage;
            if(mincode.length() == 3) {
                reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityByDistcodeAndProgramCompletionDateAndStudentStatusAndStudentGrade(mincode, pageRequest);
            } else {
                reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityByMincodeAndProgramCompletionDateAndStudentStatusAndStudentGrade(mincode, pageRequest);
            }
            return Pair.of(pageRequest, reportGradStudentTransformer.transformToDTO(reportGradStudentDataPage.getContent()));
        }
    }

}
