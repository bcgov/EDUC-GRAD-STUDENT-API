package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class CourseService {

    final EducGradStudentApiConstants constants;
    private final WebClient webClient;

    public List<Course> getCourses(List<String> courseIDs, String accessToken) {
        if(!CollectionUtils.isEmpty(courseIDs)) {
            return webClient.post().uri(String.format(constants.getCourseDetailSearchUrl()))
                    .bodyValue(courseIDs)
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Course>>() {}).block();
        }
        return Collections.emptyList();
    }

    public List<ExaminableCourse> getExaminableCourses(List<String> courseIDs, String accessToken) {
//        if(!CollectionUtils.isEmpty(courseIDs)) {
//            return webClient.post().uri(String.format(constants.getCourseExaminableSearchUrl()))
//                    .bodyValue(courseIDs)
//                    .headers(h -> h.setBearerAuth(accessToken))
//                    .retrieve()
//                    .bodyToMono(new ParameterizedTypeReference<List<ExaminableCourse>>() {}).block();
//        }
        return Collections.emptyList();
    }

    public List<LetterGrade> getLetterGrades(String accessToken) {
            return webClient.get().uri(String.format(constants.getLetterGradesUrl()))
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve().bodyToMono(new ParameterizedTypeReference<List<LetterGrade>>() {
                    }).block();
    }

}
