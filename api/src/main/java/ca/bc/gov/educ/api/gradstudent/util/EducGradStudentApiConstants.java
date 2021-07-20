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

    //Default Date format constants
    public static final String DEFAULT_CREATED_BY = "GradStudentAPI";
    public static final Date DEFAULT_CREATED_TIMESTAMP = new Date();
    public static final String DEFAULT_UPDATED_BY = "GradStudentAPI";
    public static final Date DEFAULT_UPDATED_TIMESTAMP = new Date();

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public static final String TRAX_DATE_FORMAT = "yyyyMM";

    //Endpoints
    @Value("${endpoint.school-api.school-by-min-code.url}")
    private String schoolByMincodeUrl;

    @Value("${endpoint.code-api.career_program.career_program-by-career-code.url}")
    private String careerProgramByCodeUrl;

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

    @Value("${endpoint.code-api.country.country-by-country-code.url}")
    private String countryByCountryCodeUrl;

    @Value("${endpoint.code-api.province.province-by-prov-code.url}")
    private String provinceByProvinceCodeUrl;

    @Value("${endpoint.code-api.country.all-countries.url}")
    private String allCountriesUrl;

    @Value("${endpoint.code-api.province.all-provinces.url}")
    private String allProvincesUrl;

    @Value("${endpoint.pen-student-api.search.url}")
    private String penStudentApiUrl;

    @Value("${endpoint.pen-student-api.by-pen.url}")
    private String penStudentApiByPenUrl;

    @Value("${endpoint.graduation-status-api.read-grad-status.url}")
    private String gradStatusForStudentUrl;
    
}
