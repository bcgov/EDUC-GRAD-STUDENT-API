package ca.bc.gov.educ.api.gradstudent.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Getter
@Setter
public class EducGradStatusApiConstants {

    //API end-point Mapping constants
	public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String GRADUATION_STATUS_API_ROOT_MAPPING = "/api/" + API_VERSION + "/gradstatus";
    public static final String GRADUATION_STATUS_BY_STUDENT_ID = "/studentid/{studentID}";
    public static final String GRADUATION_STATUS_BY_STUDENT_ID_FOR_ALGORITHM = "/studentid/{studentID}/algorithm";
    public static final String GRAD_STUDENT_UPDATE_BY_STUDENT_ID = "/gradstudent/studentid/{studentID}";
    public static final String GRADUATE_STUDENT_BY_PEN = "/pen/{pen}";
    public static final String GRAD_STUDENT_SPECIAL_PROGRAM_BY_PEN = "/specialprogram/studentid/{studentID}";
    public static final String GRAD_STUDENT_SPECIAL_PROGRAM_BY_PEN_PROGRAM_SPECIAL_PROGRAM = "/specialprogram/{studentID}/{specialProgramID}";
    public static final String SAVE_GRAD_STUDENT_SPECIAL_PROGRAM = "/specialprogram";
    public static final String UPDATE_GRAD_STUDENT_SPECIAL_PROGRAM = "/gradstudent/specialprogram";
    public static final String GRAD_STUDENT_RECALCULATE = "/recalculate";
    public static final String GET_STUDENT_STATUS_BY_STATUS_CODE_MAPPING = "/checkstudentstatus/{statusCode}";
    public static final String UNGRAD_STUDENT = "/ungradstudent/studentid/{studentID}";
    
    //Default Attribute value constants
    public static final String DEFAULT_CREATED_BY = "GraduationStatusAPI";
    protected static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "GraduationStatusAPI";
    protected static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    //Default Date format constants
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    
    public static final String TRAX_DATE_FORMAT = "yyyyMM";

    //Endpoints
	@Value("${endpoint.grad-program-management-api.special_program_name_by_special_program_id.url}")
	private String gradSpecialProgramNameUrl;

	@Value("${endpoint.grad-program-management-api.special_program_id_by_program_code_special_program_code.url}")
	private String gradSpecialProgramDetailsUrl;

	@Value("${endpoint.grad-program-management-api.program_name_by_program_code.url}")
	private String gradProgramNameUrl;

	@Value("${endpoint.school-api.school-name-by-mincode.url}")
	private String gradSchoolNameUrl;

	@Value("${endpoint.code-api.student-status.student-status-by-status-code.url}")
	private String studentStatusUrl;

    @Value("${endpoint.pen-student-api.by-studentid.url}")
	private String penStudentApiByStudentIdUrl;

    @Value("${endpoint.grad-common-api.save-student-ungrad-reason.url}")
	private String saveStudentUngradReasonByStudentIdUrl;

    @Value("${endpoint.code-api.ungrad-reason.ungrad-reason-by-reason-code.url}")
	private String ungradReasonDetailsUrl;

}
