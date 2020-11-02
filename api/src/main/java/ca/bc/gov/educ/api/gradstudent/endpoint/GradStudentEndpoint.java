package ca.bc.gov.educ.api.gradstudent.endpoint;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;

@RequestMapping("/api/v1")
public interface GradStudentEndpoint {

    /**
     * Gets Student details by pen.
     *
     * @param pen the pen
     * @return the student details by pen
     */
    @GetMapping("/{pen}")
    GradStudent getGradStudentByPen(@PathVariable String pen);
    
    /**
     * Gets Student details by pen.
     *
     * @param pen the pen
     * @return the student details by pen
     */
    @GetMapping("/gradstudent")
    List<GradStudent> getGradStudentByLastName(
    		@RequestParam(value = "lastName", required = true) String lastName,
    		@RequestParam(value = "pageNo", required = false,defaultValue = "0") Integer pageNo, 
            @RequestParam(value = "pageSize", required = false,defaultValue = "150") Integer pageSize);

}
