package ca.bc.gov.educ.api.gradstudent.util;

public class EducGradStudentApiConstants {
    //API end-point Mapping constants
    public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String GRAD_STUDENT_API_ROOT_MAPPING = "/api/" + API_VERSION ;
    public static final String GRAD_STUDENT_BY_PEN = "/{pen}";
    public static final String GRAD_STUDENT_BY_PEN_STUDENT_API = "pen/{pen}";
    public static final String GRAD_STUDENT_BY_LAST_NAME = "/gradstudent";
    public static final String GRAD_STUDENT_BY_FIRST_NAME = "/studentsearchfirstname";
    public static final String GRAD_STUDENT_BY_MULTIPLE_PENS = "/multipen";
    public static final String GRAD_STUDENT_BY_ANY_NAME = "/studentsearch";
    public static final String GRAD_STUDENT_BY_ANY_NAMES = "/studentssearch";
    
    //Application Properties Constants
    public static final String ENDPOINT_SCHOOL_BY_MIN_CODE_URL = "${endpoint.school-api.school-by-min-code.url}";
    public static final String ENDPOINT_COUNTRY_BY_COUNTRY_CODE_URL = "${endpoint.code-api.country.country-by-country-code.url}";
    public static final String ENDPOINT_PROVINCE_BY_PROV_CODE_URL = "${endpoint.code-api.province.province-by-prov-code.url}";
    public static final String ENDPOINT_ALL_COUNTRY_URL = "${endpoint.code-api.country.all-countries.url}";
    public static final String ENDPOINT_ALL_PROVINCE_URL = "${endpoint.code-api.province.all-provinces.url}"; 
    public static final String ENDPOINT_PEN_STUDENT_API_URL="${endpoint.pen-student-api.search.url}";
    public static final String ENDPOINT_PEN_STUDENT_API_BY_PEN_URL="${endpoint.pen-student-api.by-pen.url}";
    public static final String ENDPOINT_GRAD_STUDENT_API_URL="${endpoint.graduation-status-api.read-grad-status.url}";
    
}
