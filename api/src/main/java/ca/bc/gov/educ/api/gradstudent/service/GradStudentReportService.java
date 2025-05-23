package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.Generated;
import ca.bc.gov.educ.api.gradstudent.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradSchoolYearEndEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.ReportGradStudentTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.ReportGradSchoolYearEndRepository;
import ca.bc.gov.educ.api.gradstudent.repository.ReportGradStudentDataRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService.PAGE_SIZE;

@Service
public class GradStudentReportService extends GradBaseService {

    private static final Logger logger = LoggerFactory.getLogger(GradStudentReportService.class);

    private final ReportGradStudentDataRepository reportGradStudentDataRepository;
    private final ReportGradSchoolYearEndRepository reportGradSchoolYearEndRepository;
    private final ReportGradStudentTransformer reportGradStudentTransformer;
    private final EducGradStudentApiConstants constants;
    private final WebClient studentApiClient;

    public GradStudentReportService(EducGradStudentApiConstants constants, ReportGradStudentDataRepository reportGradStudentDataRepository, ReportGradSchoolYearEndRepository reportGradSchoolYearEndRepository, ReportGradStudentTransformer reportGradStudentTransformer, @Qualifier("studentApiClient") WebClient studentApiClient) {
        this.constants = constants;
        this.reportGradStudentDataRepository = reportGradStudentDataRepository;
        this.reportGradSchoolYearEndRepository = reportGradSchoolYearEndRepository;
        this.reportGradStudentTransformer = reportGradStudentTransformer;
        this.studentApiClient = studentApiClient;
    }



    public List<ReportGradStudentData> getGradStudentDataBySchoolId(UUID schoolId) {
        return reportGradStudentTransformer.transformToDTO(reportGradStudentDataRepository.findReportGradStudentDataEntityBySchoolOfRecordIdOrderBySchoolNameAscLastNameAsc(schoolId));
    }

    public List<ReportGradStudentData> getGradStudentDataByStudentGuids(List<UUID> studentIds) {
        return reportGradStudentTransformer.transformToDTO(reportGradStudentDataRepository.findReportGradStudentDataEntityByGraduationStudentRecordIdInOrderBySchoolNameAscLastNameAsc(studentIds));
    }

    public List<ReportGradStudentData> getGradStudentDataForNonGradYearEndReport() {
        PageRequest nextPage = PageRequest.of(0, PAGE_SIZE);
        Page<ReportGradStudentDataEntity> reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityByProgramCompletionDateAndStudentStatusAndStudentGrade(nextPage);
        return processReportGradStudentDataList(reportGradStudentDataPage);
    }

    public List<ReportGradStudentData> getGradStudentDataForNonGradYearEndReportBySchool(UUID schoolId) {
        PageRequest nextPage = PageRequest.of(0, PAGE_SIZE);
        if(schoolId == null) {
            throw new IllegalArgumentException("Invalid schoolId: " + schoolId);
        }
        Page<ReportGradStudentDataEntity> reportGradStudentDataPage;
        reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityBySchoolOfRecordIdAndProgramCompletionDateAndStudentStatusAndStudentGrade(schoolId, nextPage);
        return processReportGradStudentDataList(schoolId, null, reportGradStudentDataPage);
    }

    public List<ReportGradStudentData> getGradStudentDataForNonGradYearEndReportByDistrict(UUID districtId) {
        PageRequest nextPage = PageRequest.of(0, PAGE_SIZE);
        if(districtId == null) {
            throw new IllegalArgumentException("Invalid districtId: " + districtId);
        }
        Page<ReportGradStudentDataEntity> reportGradStudentDataPage;
        reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityByDistrictIdAndProgramCompletionDateAndStudentStatusAndStudentGrade(districtId, nextPage);
        return processReportGradStudentDataList(null, districtId, reportGradStudentDataPage);
    }

    public List<UUID> getGradSchoolsForNonGradYearEndReport() {
        return reportGradSchoolYearEndRepository.findAll().stream().map(ReportGradSchoolYearEndEntity::getSchoolId).toList();
    }

    public List<UUID> getGradDistrictsForNonGradYearEndReport(String accessToken) {
        List<UUID> districtIds = new ArrayList<>();
        List<UUID> schoolIds = reportGradSchoolYearEndRepository.findAll().stream().map(ReportGradSchoolYearEndEntity::getSchoolId).toList();
        schoolIds.forEach(schoolId -> {
            School school = getSchool(schoolId, accessToken);
            if (school != null && school.getDistrictId() != null) {
                UUID districtId = UUID.fromString(school.getDistrictId());
                if (!districtIds.contains(districtId)) {
                    districtIds.add(districtId);
                }
            }
        });
        return districtIds;
    }

    private List<ReportGradStudentData> processReportGradStudentDataList(Page<ReportGradStudentDataEntity> reportGradStudentDataPage) {
        return createAndExecuteReportGradStudentDataTasks(null, null, reportGradStudentDataPage);
    }

    private List<ReportGradStudentData> processReportGradStudentDataList(UUID schoolId, UUID districtId, Page<ReportGradStudentDataEntity> reportGradStudentDataPage) {
        return createAndExecuteReportGradStudentDataTasks(schoolId, districtId, reportGradStudentDataPage);
    }

    private List<ReportGradStudentData> createAndExecuteReportGradStudentDataTasks(UUID schoolId, UUID districtId, Page<ReportGradStudentDataEntity> reportGradStudentDataPage) {
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
                ReportGradStudentDataPageTask pageTask = new ReportGradStudentDataPageTask(schoolId, null, nextPage);
                tasks.add(pageTask);
            }

            processReportGradStudentDataTasksAsync(tasks, result);
        }
        logger.debug("Completed in {} sec, total objects acquired {}", (System.currentTimeMillis() - startTime) / 1000, result.size());
        return result;
    }

    @Generated
    private void processReportGradStudentDataTasksAsync(List<Callable<Object>> tasks, List<ReportGradStudentData> result) {
        if(tasks.isEmpty()) return;
        List<Future<Object>> executionResult;
        ExecutorService executorService = Executors.newWorkStealingPool();
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

        private final UUID schoolId;

        private final UUID districtId;

        public ReportGradStudentDataPageTask(UUID schoolId, UUID districtId, PageRequest pageRequest) {
            this.pageRequest = pageRequest;
            this.schoolId = schoolId;
            this.districtId = districtId;
        }

        @Override
        @Generated
        public Object call() throws Exception {
            Page<ReportGradStudentDataEntity> reportGradStudentDataPage;

            if (districtId != null) {
                reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityByDistrictIdAndProgramCompletionDateAndStudentStatusAndStudentGrade(districtId, pageRequest);
            } else if (schoolId != null) {
                reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityBySchoolOfRecordIdAndProgramCompletionDateAndStudentStatusAndStudentGrade(schoolId, pageRequest);
            } else {
                reportGradStudentDataPage = reportGradStudentDataRepository.findReportGradStudentDataEntityByProgramCompletionDateAndStudentStatusAndStudentGrade(pageRequest);
            }
            return Pair.of(pageRequest, reportGradStudentTransformer.transformToDTO(reportGradStudentDataPage.getContent()));
        }
    }

    @Override
    protected WebClient getWebClient() {
        return studentApiClient;
    }

    @Override
    protected EducGradStudentApiConstants getConstants() {
        return constants;
    }

}
