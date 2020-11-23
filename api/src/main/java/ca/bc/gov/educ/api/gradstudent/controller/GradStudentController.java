package ca.bc.gov.educ.api.gradstudent.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.educ.api.gradstudent.endpoint.GradStudentEndpoint;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;
import lombok.extern.slf4j.Slf4j;

@RestController
//@EnableResourceServer
@Slf4j
public class GradStudentController implements GradStudentEndpoint {

    private static Logger logger = LoggerFactory.getLogger(GradStudentController.class);

    @Autowired
    GradStudentService gradStudentService;

    @Override
    public GradStudent getGradStudentByPen(String pen) {
        return gradStudentService.getStudentByPen(pen);
    }

	@Override
	public List<GradStudent> getGradStudentByLastName(String lastName,Integer pageNo, Integer pageSize) {
		return gradStudentService.getStudentByLastName(lastName,pageNo,pageSize);
	}
	
	@Override
	public List<GradStudent> getGradStudentByFirstName(String firstName, Integer pageNo, Integer pageSize) {
		return gradStudentService.getStudentByFirstName(firstName,pageNo,pageSize);
	}
	
	@Override
	public List<GradStudent> getGradStudentByLastNameAndFirstName(String lastName,String firstName, Integer pageNo, Integer pageSize) {
		return gradStudentService.getStudentByLastNameAndFirstName(lastName,firstName,pageNo,pageSize);
	}
	
	@Override
    public List<GradStudent> getGradStudentByPens(@RequestParam(value = "penList", required = true) List<String> penList) {
        return gradStudentService.getStudentByPens(penList);
    }
}
