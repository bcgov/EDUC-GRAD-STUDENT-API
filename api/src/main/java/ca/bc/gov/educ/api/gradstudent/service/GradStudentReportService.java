package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.Generated;
import ca.bc.gov.educ.api.gradstudent.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.ReportGradStudentTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.ReportGradStudentDataRepository;
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
    ReportGradStudentTransformer reportGradStudentTransformer;

    public List<ReportGradStudentData> getGradStudentDataByMincode(String mincode) {
        return reportGradStudentTransformer.transformToDTO(reportGradStudentDataRepository.findReportGradStudentDataEntityByMincodeStartsWithOrderByMincodeAscSchoolNameAscLastNameAsc(mincode));
    }

    public List<ReportGradStudentData> getGradStudentDataByStudentGuids(List<UUID> studentIds) {
        return reportGradStudentTransformer.transformToDTO(reportGradStudentDataRepository.findReportGradStudentDataEntityByGraduationStudentRecordIdInOrderByMincodeAscSchoolNameAscLastNameAsc(studentIds));
    }

    public List<ReportGradStudentData> getGradStudentDataForNonGradYearEndReport() {
        PageRequest nextPage = PageRequest.of(0, PAGE_SIZE);
        Page<ReportGradStudentDataEntity> reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityByProgramCompletionDateAndStudentStatusAndStudentGrade(nextPage);
        return processReportGradStudentDataList(reportGradStudentDataPage);
    }

    private List<ReportGradStudentData> processReportGradStudentDataList(Page<ReportGradStudentDataEntity> reportGradStudentDataPage) {
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
                ReportGradStudentDataPageTask pageTask = new ReportGradStudentDataPageTask(nextPage);
                tasks.add(pageTask);
            }

            try {
                processReportGradStudentDataTasksAsync(tasks, result, totalNumberOfPages);
            } catch (Exception ex) {
                logger.error("Unable to process Student Data: {} ", ex.getLocalizedMessage());
            }
        }
        logger.debug("Completed in {} sec, total objects acquired {}", (System.currentTimeMillis() - startTime) / 1000, result.size());
        return result;
    }

    @Generated
    private void processReportGradStudentDataTasksAsync(List<Callable<Object>> tasks, List<ReportGradStudentData> result, int numberOfThreads) throws Exception {
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
            throw new Exception(ex.toString());
        } finally {
            executorService.shutdown();
        }
    }

    class ReportGradStudentDataPageTask implements Callable<Object> {

        private final PageRequest pageRequest;

        public ReportGradStudentDataPageTask(PageRequest pageRequest) {
            this.pageRequest = pageRequest;
        }

        @Override
        @Generated
        public Object call() throws Exception {
            Page<ReportGradStudentDataEntity> reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityByProgramCompletionDateAndStudentStatusAndStudentGrade(pageRequest);
            return Pair.of(pageRequest, reportGradStudentTransformer.transformToDTO(reportGradStudentDataPage.getContent()));
        }
    }

}
