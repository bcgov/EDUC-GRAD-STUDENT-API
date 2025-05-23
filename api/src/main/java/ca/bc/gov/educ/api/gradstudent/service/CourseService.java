package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@Slf4j
public class CourseService {

    private final WebClient studentApiClient;
    private final RESTService restService;
    private final CourseCacheService courseCacheService;
    private final JsonTransformer jsonTransformer;
    final EducGradStudentApiConstants constants;

    public CourseService(@Qualifier("studentApiClient") WebClient studentApiClient, RESTService restService, CourseCacheService courseCacheService, JsonTransformer jsonTransformer, EducGradStudentApiConstants constants) {
        this.studentApiClient = studentApiClient;
        this.restService = restService;
        this.courseCacheService = courseCacheService;
        this.jsonTransformer = jsonTransformer;
        this.constants = constants;
    }


    public List<Course> getCourses(List<String> courseIDs) {
        if(!CollectionUtils.isEmpty(courseIDs)) {
            CourseSearchRequest courseSearchRequest = new CourseSearchRequest();
            courseSearchRequest.setCourseIds(courseIDs);
            var response = restService.post(String.format(constants.getCourseDetailSearchUrl()), courseSearchRequest, List.class, studentApiClient);
            return jsonTransformer.convertValue(response, new TypeReference<List<Course>>() {});
        }
        return Collections.emptyList();
    }

    public List<ExaminableCourse> getExaminableCourses(List<String> courseIDs) {
        List<ExaminableCourse> examinableCourses = courseCacheService.getExaminableCoursesFromCache();
        return examinableCourses.stream().filter(examinableCourse -> courseIDs.contains(examinableCourse.getCourseID())).toList();
    }

    public List<LetterGrade> getLetterGrades() {
        return courseCacheService.getLetterGradesFromCache();
    }

}
