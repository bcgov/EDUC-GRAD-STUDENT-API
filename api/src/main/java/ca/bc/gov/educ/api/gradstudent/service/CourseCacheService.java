package ca.bc.gov.educ.api.gradstudent.service;

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

    private final WebClient studentApiClient;
    private final RESTService restService;
    private final JsonTransformer jsonTransformer;
    final EducGradStudentApiConstants constants;

    private final Map<UUID, ExaminableCourse> examinableCourseCache = new ConcurrentHashMap<>();
    private final Map<String, LetterGrade> letterGradeCache = new ConcurrentHashMap<>();

    @Autowired
    public CourseCacheService(@Qualifier("studentApiClient") WebClient studentApiClient, RESTService restService, JsonTransformer jsonTransformer, EducGradStudentApiConstants constants) {
        this.studentApiClient = studentApiClient;
        this.restService = restService;
        this.jsonTransformer = jsonTransformer;
        this.constants = constants;
    }

    public List<ExaminableCourse> getExaminableCoursesFromCache() {
        //return CollectionUtils.isEmpty(examinableCourseCache) ? fetchExaminableCourses(): examinableCourseCache.values().stream().toList();
        return Collections.emptyList();
    }

    public List<LetterGrade> getLetterGradesFromCache() {
        return CollectionUtils.isEmpty(letterGradeCache) ? fetchLetterGrades(): letterGradeCache.values().stream().toList();
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
        var response = restService.get(String.format(constants.getExaminableCourseDetailUrl()), List.class, studentApiClient);
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
            log.error("Failed to load Examinable Course: {}", e.getMessage(), e);
        }
    }

    private List<LetterGrade> fetchLetterGrades() {
        var response = restService.get(String.format(constants.getLetterGradesUrl()), List.class, studentApiClient);
        return response != null ? jsonTransformer.convertValue(response, new TypeReference<List<LetterGrade>>() {}) : Collections.emptyList();
    }
}
