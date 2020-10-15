package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.endpoint.GradStudentEndpoint;
import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@EnableResourceServer
@Slf4j
public class GradStudentController implements GradStudentEndpoint {

    private static Logger logger = LoggerFactory.getLogger(GradStudentController.class);

    @Autowired
    GradStudentService gradStudentService;

    @Override
    public GradStudentEntity getGradStudentByPen(String pen) {
        return gradStudentService.getStudentByPen(pen).get();
    }
}
