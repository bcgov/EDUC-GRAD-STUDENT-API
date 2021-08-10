package ca.bc.gov.educ.api.gradstudent.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Getter
@Setter
public class EducGradStudentApiConstants {
    //API end-point Mapping constants
    public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String GRAD_STUDENT_API_ROOT_MAPPING = "/api/" + API_VERSION + "/student" ;
    public static final String GRAD_STUDENT_BY_PEN = "/{pen}";
    public static final String GRAD_STUDENT_BY_PEN_STUDENT_API = "/pen/{pen}";
    public static final String GRAD_STUDENT_BY_STUDENT_ID_STUDENT_API = "/stdid/{studentID}";
    public static final String GRAD_STUDENT_BY_LAST_NAME = "/gradstudent";
    public static final String GRAD_STUDENT_BY_FIRST_NAME = "/studentsearchfirstname";
    public static final String GRAD_STUDENT_BY_MULTIPLE_PENS = "/multipen";
    public static final String GRAD_STUDENT_BY_ANY_NAME = "/studentsearch";
    public static final String GRADUATION_STATUS_BY_STUDENT_ID = "/studentid/{studentID}";
    public static final String GRADUATION_STATUS_BY_STUDENT_ID_FOR_ALGORITHM = "/studentid/{studentID}/algorithm";
    public static final String GRAD_STUDENT_UPDATE_BY_STUDENT_ID = "/gradstudent/studentid/{studentID}";

    public static final String GRAD_STUDENT_SPECIAL_PROGRAM_BY_PEN = "/specialprogram/studentid/{studentID}";
    public static final String GRAD_STUDENT_SPECIAL_PROGRAM_BY_PEN_PROGRAM_SPECIAL_PROGRAM = "/specialprogram/{studentID}/{specialProgramID}";
    public static final String SAVE_GRAD_STUDENT_SPECIAL_PROGRAM = "/specialprogram";
    public static final String UPDATE_GRAD_STUDENT_SPECIAL_PROGRAM = "/gradstudent/specialprogram";
    public static final String GRAD_STUDENT_RECALCULATE = "/recalculate";
    public static final String GET_STUDENT_STATUS_BY_STATUS_CODE_MAPPING = "/checkstudentstatus/{statusCode}";
    public static final String UNGRAD_STUDENT = "/ungradstudent/studentid/{studentID}";

    public static final String GET_ALL_STUDENT_CAREER_MAPPING = "/studentcareerprogram/pen/{pen}";
    public static final String STUDENT_REPORT = "/studentreport";
    public static final String STUDENT_CERTIFICATE = "/studentcertificate";
    public static final String STUDENT_CERTIFICATE_BY_STUDENTID = "/studentcertificate/{studentID}";
    public static final String GET_ALL_ALGORITHM_RULES_MAPPING="/algorithmrules";

    public static final String GET_ALGORITHM_RULES_MAIN_PROGRAM = "/algorithm-rules/main/{programCode}";
    public static final String GET_ALGORITHM_RULES_SPECIAL_PROGRAM = "/algorithm-rules/special/{programCode}/{specialProgramCode}";

    public static final String GET_ALL_STUDENT_UNGRAD_MAPPING = "/studentungradreason/studentid/{studentID}";
    public static final String GET_STUDENT_UNGRAD_BY_REASON_CODE_MAPPING = "/ungrad/{reasonCode}";
    public static final String GET_STUDENT_CAREER_PROGRAM_BY_CAREER_PROGRAM_CODE_MAPPING = "/career/{cpCode}";
    public static final String GET_STUDENT_CERTIFICATE_BY_CERTIFICATE_CODE_MAPPING = "/certificate/{certificateTypeCode}";
    public static final String GET_STUDENT_REPORT_BY_REPORT_CODE_MAPPING = "/report/{reportTypeCode}";
    public static final String GET_ALL_STUDENT_NOTES_MAPPING = "/studentnotes/pen/{pen}";
    public static final String STUDENT_NOTES_MAPPING = "/studentnotes";
    public static final String STUDENT_NOTES_DELETE_MAPPING = "/studentnotes/{noteID}";
    
    public static final String GET_ALL_STUDENT_STATUS_MAPPING = "/studentstatus";
    public static final String GET_ALL_STUDENT_STATUS_BY_CODE_MAPPING = "/studentstatus/{statusCode}";
    public static final String STUDENT_ALGORITHM_DATA = "/algorithmdata/{studentID}";
    //Default Date format constants
    public static final String DEFAULT_CREATED_BY = "API_GRAD_STUDENT";
    public static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "API_GRAD_STUDENT";
    public static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static final String TRAX_DATE_FORMAT = "yyyyMM";

    //Endpoints
    @Value("${endpoint.grad-trax-api.school-by-min-code.url}")
    private String schoolByMincodeUrl;

    @Value("${endpoint.grad-program-api.career_program-by-career-code.url}")
    private String careerProgramByCodeUrl;

	@Value("${endpoint.grad-program-api.optional_program_name_by_optional_program_id.url}")
	private String gradSpecialProgramNameUrl;

    @Value("${endpoint.grad-program-api.special_program_id_by_program_code_special_program_code.url}")
    private String gradSpecialProgramDetailsUrl;

    @Value("${endpoint.grad-program-api.program_name_by_program_code.url}")
    private String gradProgramNameUrl;

    @Value("${endpoint.pen-student-api.by-studentid.url}")
    private String penStudentApiByStudentIdUrl;

    @Value("${endpoint.grad-student-graduation-api.save-student-ungrad-reason.url}")
    private String saveStudentUngradReasonByStudentIdUrl;

    @Value("${endpoint.grad-student-graduation-api.ungrad-reason.ungrad-reason-by-reason-code.url}")
    private String ungradReasonDetailsUrl;

    @Value("${endpoint.pen-student-api.search.url}")
    private String penStudentApiUrl;

    @Value("${endpoint.pen-student-api.by-pen.url}")
    private String penStudentApiByPenUrl;
    
}
