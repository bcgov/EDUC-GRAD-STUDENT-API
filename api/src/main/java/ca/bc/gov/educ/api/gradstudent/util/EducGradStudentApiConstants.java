package ca.bc.gov.educ.api.gradstudent.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
    public static final String GRAD_STUDENT_BY_ANY_NAMES = "/studentssearch";

    // Endpoints
    @Value("${endpoint.school-api.school-by-min-code.url}")
    private String schoolByMincodeUrl;

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
