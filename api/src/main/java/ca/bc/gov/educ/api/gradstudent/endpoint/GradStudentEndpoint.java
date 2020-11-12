package ca.bc.gov.educ.api.gradstudent.endpoint;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;

@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
public interface GradStudentEndpoint {

    /**
     * Gets Student details by pen.
     *
     * @param pen the pen
     * @return the student details by pen
     */
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_PEN)
    GradStudent getGradStudentByPen(@PathVariable String pen);
    
    /**
     * Gets Student details by pen.
     *
     * @param pen the pen
     * @return the student details by pen
     */
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_LAST_NAME)
    List<GradStudent> getGradStudentByLastName(
    		@RequestParam(value = "lastName", required = true) String lastName,
    		@RequestParam(value = "pageNo", required = false,defaultValue = "0") Integer pageNo, 
            @RequestParam(value = "pageSize", required = false,defaultValue = "50") Integer pageSize);

}
