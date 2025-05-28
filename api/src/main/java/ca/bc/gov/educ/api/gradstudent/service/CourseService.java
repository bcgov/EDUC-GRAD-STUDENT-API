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

    private final WebClient courseApiClient;
    private final RESTService restService;
    private final JsonTransformer jsonTransformer;
    final EducGradStudentApiConstants constants;

    public CourseService(@Qualifier("courseApiClient") WebClient courseApiClient, RESTService restService, JsonTransformer jsonTransformer, EducGradStudentApiConstants constants) {
        this.courseApiClient = courseApiClient;
        this.restService = restService;
        this.jsonTransformer = jsonTransformer;
        this.constants = constants;
    }

    public List<Course> getCourses(List<String> courseIDs) {
        if(!CollectionUtils.isEmpty(courseIDs)) {
            CourseSearchRequest courseSearchRequest = new CourseSearchRequest();
            courseSearchRequest.setCourseIds(courseIDs);
            var response = restService.post(String.format(constants.getCourseDetailSearchUrl()), courseSearchRequest, List.class, courseApiClient);
            return jsonTransformer.convertValue(response, new TypeReference<List<Course>>() {});
        }
        return Collections.emptyList();
    }

}
