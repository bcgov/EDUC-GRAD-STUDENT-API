package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.EquivalentOrChallengeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.ExamSpecialCaseCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.ExaminableCourse;
import ca.bc.gov.educ.api.gradstudent.model.dto.LetterGrade;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class CourseCacheService {

    private final WebClient courseApiClient;
    private final WebClient graduationApiClient;
    private final RESTService restService;
    private final JsonTransformer jsonTransformer;
    private final EducGradStudentApiConstants constants;
    private final ExamSpecialCaseCodeService examSpecialCaseCodeService;
    private final EquivalentOrChallengeCodeService equivalentOrChallengeCodeService;

    private final Map<UUID, ExaminableCourse> examinableCourseCache = new ConcurrentHashMap<>();
    private final Map<String, LetterGrade> letterGradeCache = new ConcurrentHashMap<>();
    private final Map<String, ExamSpecialCaseCode> examSpecialCaseCodeCache = new ConcurrentHashMap<>();
    private final Map<String, EquivalentOrChallengeCode> equivalentOrChallengeCodeCache = new ConcurrentHashMap<>();


    @Autowired
    public CourseCacheService(@Qualifier("courseApiClient") WebClient courseApiClient, @Qualifier("graduationApiClient") WebClient graduationApiClient, RESTService restService, JsonTransformer jsonTransformer, EducGradStudentApiConstants constants,
                              ExamSpecialCaseCodeService examSpecialCaseCodeService, EquivalentOrChallengeCodeService equivalentOrChallengeCodeService) {
        this.courseApiClient = courseApiClient;
        this.graduationApiClient = graduationApiClient;
        this.restService = restService;
        this.jsonTransformer = jsonTransformer;
        this.constants = constants;
        this.examSpecialCaseCodeService = examSpecialCaseCodeService;
        this.equivalentOrChallengeCodeService = equivalentOrChallengeCodeService;
    }

    public List<ExaminableCourse> getExaminableCoursesFromCache() {
        return CollectionUtils.isEmpty(examinableCourseCache) ? fetchExaminableCourses(): examinableCourseCache.values().stream().toList();
    }

    public List<LetterGrade> getLetterGradesFromCache() {
        return CollectionUtils.isEmpty(letterGradeCache) ? fetchLetterGrades(): letterGradeCache.values().stream().toList();
    }

    public List<ExamSpecialCaseCode> getExamSpecialCaseCodesFromCache() {
        return CollectionUtils.isEmpty(examSpecialCaseCodeCache) ? fetchExamSpecialCaseCodes(): examSpecialCaseCodeCache.values().stream().toList();
    }

    public List<EquivalentOrChallengeCode> getEquivalentOrChallengeCodesFromCache() {
        return CollectionUtils.isEmpty(equivalentOrChallengeCodeCache) ? fetchEquivalentOrChallengeCodes(): equivalentOrChallengeCodeCache.values().stream().toList();
    }

    @Async("cacheExecutor")
    public void loadExaminableCourses() {
        log.info("Loading Examinable Course cache");
        try {
            List<ExaminableCourse> examinableCourses = fetchExaminableCourses();
            Map<UUID, ExaminableCourse> newCache = new ConcurrentHashMap<>();
            examinableCourses.forEach(examinableCourse -> newCache.put(examinableCourse.getExaminableCourseID(), examinableCourse));
            examinableCourseCache.clear();
            examinableCourseCache.putAll(newCache);
            log.info("Examinable Course cache successfully loaded with {} entries.", examinableCourses.size());
        } catch (Exception e) {
            log.error("Failed to load Examinable Course: {}", e.getMessage(), e);
        }
    }

    private List<ExaminableCourse> fetchExaminableCourses() {
        var response = restService.get(String.format(constants.getExaminableCourseDetailUrl()), List.class, courseApiClient);
        return response != null ? jsonTransformer.convertValue(response, new TypeReference<List<ExaminableCourse>>() {}) : Collections.emptyList();
    }

    @Async("cacheExecutor")
    public void loadLetterGrades() {
        log.info("Loading Letter Grade cache");
        try {
            List<LetterGrade> letterGrades = fetchLetterGrades();
            Map<String, LetterGrade> newCache = new ConcurrentHashMap<>();
            letterGrades.forEach(letterGrade -> newCache.put(letterGrade.getGrade(), letterGrade));
            letterGradeCache.clear();
            letterGradeCache.putAll(newCache);
            log.info("Letter Grade cache successfully loaded with {} entries.", letterGrades.size());
        } catch (Exception e) {
            log.error("Failed to load Letter Grade: {}", e.getMessage(), e);
        }
    }

    private List<LetterGrade> fetchLetterGrades() {
        var response = restService.get(String.format(constants.getLetterGradesUrl()), List.class, graduationApiClient);
        return response != null ? jsonTransformer.convertValue(response, new TypeReference<List<LetterGrade>>() {}) : Collections.emptyList();
    }


    @Async("cacheExecutor")
    public void loadExamSpecialCases() {
        log.info("Loading Exam Special Case cache");
        try {
            List<ExamSpecialCaseCode> examSpecialCaseCodes = fetchExamSpecialCaseCodes();
            Map<String, ExamSpecialCaseCode> newCache = new ConcurrentHashMap<>();
            examSpecialCaseCodes.forEach(examSpecialCaseCode -> newCache.put(examSpecialCaseCode.getExamSpecialCaseCode(), examSpecialCaseCode));
            examSpecialCaseCodeCache.clear();
            examSpecialCaseCodeCache.putAll(newCache);
            log.info("Special Case cache successfully loaded with {} entries.", examSpecialCaseCodes.size());
        } catch (Exception e) {
            log.error("Failed to load Special Case: {}", e.getMessage(), e);
        }
    }

    @Async("cacheExecutor")
    public void loadEquivalentOrChallenges() {
        log.info("Loading EquivalentOrChallenge cache");
        try {
            List<EquivalentOrChallengeCode> equivalentOrChallengeCodes = fetchEquivalentOrChallengeCodes();
            Map<String, EquivalentOrChallengeCode> newCache = new ConcurrentHashMap<>();
            equivalentOrChallengeCodes.forEach(equivalentOrChallengeCode -> newCache.put(equivalentOrChallengeCode.getEquivalentOrChallengeCode(), equivalentOrChallengeCode));
            equivalentOrChallengeCodeCache.clear();
            equivalentOrChallengeCodeCache.putAll(newCache);
            log.info("EquivalentOrChallenge cache successfully loaded with {} entries.", equivalentOrChallengeCodes.size());
        } catch (Exception e) {
            log.error("Failed to load EquivalentOrChallenge : {}", e.getMessage(), e);
        }
    }

    private List<ExamSpecialCaseCode> fetchExamSpecialCaseCodes() {
        return examSpecialCaseCodeService.findAll();
    }

    private List<EquivalentOrChallengeCode> fetchEquivalentOrChallengeCodes() {
        return equivalentOrChallengeCodeService.findAll();
    }

}
