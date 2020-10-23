package ca.bc.gov.educ.api.gradstudent.endpoint;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/api/v1")
public interface GradStudentEndpoint {

    /**
     * Gets Student details by pen.
     *
     * @param pen the pen
     * @return the student details by pen
     */
    @GetMapping("/{pen}")
    GradStudentEntity getGradStudentByPen(@PathVariable String pen);

}
