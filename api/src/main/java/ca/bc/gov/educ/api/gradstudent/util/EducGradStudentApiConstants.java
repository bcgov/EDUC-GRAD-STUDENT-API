package ca.bc.gov.educ.api.gradstudent.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class EducGradStudentApiConstants {
    /**
     * The constant GRAD_STATUS_API.
     */
    public static final String API_NAME = "GRAD-STUDENT-API";
    public static final String STREAM_NAME="GRAD_STATUS_EVENT_STREAM";
    public static final String CORRELATION_ID = "correlationID";

    //API end-point Mapping constants
    public static final String API_ROOT_MAPPING = "";
    public static final String API_VERSION = "v1";
    public static final String GRAD_STUDENT_API_ROOT_MAPPING = "/api/" + API_VERSION + "/student" ;
    public static final String STUDENT_COUNT = "/count";
    public static final String STUDENT_ARCHIVE = "/archive";
    public static final String GRAD_STUDENT_BY_PEN = "/{pen}";
    public static final String GRAD_STUDENT_BY_PEN_STUDENT_API = "/pen/{pen}";
    public static final String GRAD_STUDENT_DEMOG_BY_PEN = "/demog/pen/{pen}";
    public static final String GRAD_STUDENT_BY_STUDENT_ID_STUDENT_API = "/stdid/{studentID}";
    public static final String GRAD_STUDENT_BY_STUDENT_ID_GRAD="/grad/{studentID}";
    public static final String GRAD_STUDENT_BY_LAST_NAME = "/gradstudent";
    public static final String GRAD_STUDENT_BY_FIRST_NAME = "/studentsearchfirstname";
    public static final String GRAD_STUDENT_BY_MULTIPLE_STUDENTIDS = "/multistudentids";
    public static final String GRAD_STUDENT_BY_LIST_CRITERIAS = "/studentlistsearch";
    public static final String GRAD_STUDENT_BY_ANY_NAME = "/studentsearch";
    public static final String GRAD_STUDENT_BY_ANY_NAME_ONLY = "/gradstudentsearch";
    public static final String SEARCH_GRAD_STUDENTS = "/gradsearch";
    public static final String GRADUATION_STATUS_BY_STUDENT_ID = "/studentid/{studentID}";
    public static final String GRADUATION_STATUS_BY_STUDENT_ID_FOR_ALGORITHM = "/studentid/{studentID}/algorithm";
    public static final String GRAD_STUDENT_UPDATE_BY_STUDENT_ID = "/gradstudent/studentid/{studentID}";
    public static final String GRADUATION_RECORD_BY_STUDENT_ID_PROJECTED_RUN = "/projected/studentid/{studentID}";
    public static final String GRADUATION_RECORD_BY_STUDENT_ID_DISTRIBUTION_RUN = "/distribution/studentid/{studentID}";
    public static final String GRADUATION_RECORD_HISTORY_BY_BATCH_ID_DISTRIBUTION_RUN = "/distribution/batchid/{batchID}";
    public static final String UPDATE_GRAD_STUDENT_FLAG_BY_BATCH_JOB_TYPE_AND_MULTIPLE_STUDENTIDS = "/multistudentids/batchflag/jobtype/{batchJobType}";
    public static final String GRAD_STUDENT_NON_GRAD_REASON_BY_PEN = "/pen/{pen}/nongrad-reason";

    public static final String GRAD_STUDENT_OPTIONAL_PROGRAM_BY_PEN = "/optionalprogram/studentid/{studentID}";
    public static final String GRAD_STUDENT_OPTIONAL_PROGRAM_BY_PEN_PROGRAM_OPTIONAL_PROGRAM = "/optionalprogram/{studentID}/{optionalProgramID}";
    public static final String SAVE_GRAD_STUDENT_OPTIONAL_PROGRAM = "/optionalprogram";
    public static final String UPDATE_GRAD_STUDENT_OPTIONAL_PROGRAM = "/gradstudent/optionalprogram";
    public static final String GRAD_STUDENT_OPTIONAL_PROGRAM_BY_ID = "/{studentID}/optionalPrograms/{optionalProgramID}";
    public static final String GRAD_STUDENT_CAREER_PROGRAMS = "/{studentID}/careerPrograms";
    public static final String GRAD_STUDENT_CAREER_PROGRAMS_BY_CODE = "/{studentID}/careerPrograms/{careerProgramCode}";
    public static final String GRAD_STUDENT_OPTIONAL_PROGRAMS = "/{studentID}/optionalPrograms";
    public static final String GRAD_STUDENT_RECALCULATE = "/recalculate";
    public static final String GRAD_STUDENT_PROJECTED_RUN = "/projected";
    public static final String GRAD_STUDENT_BY_STUDENT_ID_FOR_BATCH_RUN = "/batch/gradstudent/studentid/{studentID}";
    public static final String STUDENT_LIST_FOR_SCHOOL_REPORT = "/batch/schoolreport/{schoolOfRecord}";
    public static final String STUDENT_LIST_FOR_AMALGAMATED_SCHOOL_REPORT = "/amalgamated/schoolreport/{schoolOfRecord}/type/{type}";
    public static final String STUDENT_COUNT_FOR_AMALGAMATED_SCHOOL_REPORT = "/amalgamated/schoolreport/{schoolOfRecord}" + STUDENT_COUNT;
    public static final String STUDENT_RECORD_STUDENT_ID_BATCH_RUN = "/batch/{studentID}";
    public static final String GET_STUDENT_STATUS_BY_STATUS_CODE_MAPPING = "/checkstudentstatus/{statusCode}";
    public static final String UNGRAD_STUDENT = "/undocompletionstudent/studentid/{studentID}";

    public static final String GET_ALL_STUDENT_CAREER_MAPPING = "/studentcareerprogram/studentid/{studentID}";
    public static final String STUDENT_REPORT = "/studentreport";
    public static final String STUDENT_CERTIFICATE = "/studentcertificate";
    public static final String STUDENT_CERTIFICATE_BY_STUDENTID = "/studentcertificate/{studentID}";
    public static final String GET_ALL_ALGORITHM_RULES_MAPPING="/algorithmrules";

    public static final String GET_ALGORITHM_RULES_MAIN_PROGRAM = "/algorithm-rules/main/{programCode}";
    public static final String GET_ALGORITHM_RULES_OPTIONAL_PROGRAM = "/algorithm-rules/optional/{programCode}/{optionalProgramCode}";

    public static final String GET_ALL_STUDENT_UNGRAD_MAPPING = "/studentungradreason/studentid/{studentID}";
    public static final String GET_STUDENT_UNGRAD_BY_REASON_CODE_MAPPING = "/ungrad/{reasonCode}";
    public static final String GET_STUDENT_CAREER_PROGRAM_BY_CAREER_PROGRAM_CODE_MAPPING = "/career/{cpCode}";
    public static final String GET_STUDENT_CERTIFICATE_BY_CERTIFICATE_CODE_MAPPING = "/certificate/{certificateTypeCode}";
    public static final String GET_STUDENT_REPORT_BY_REPORT_CODE_MAPPING = "/report/{reportTypeCode}";
    public static final String GET_ALL_STUDENT_NOTES_MAPPING = "/studentnotes/studentid/{studentID}";
    public static final String STUDENT_NOTES_MAPPING = "/studentnotes";
    public static final String STUDENT_NOTES_DELETE_MAPPING = "/studentnotes/{noteID}";
    
    public static final String GET_ALL_STUDENT_STATUS_MAPPING = "/studentstatus";
    public static final String GET_ALL_STUDENT_STATUS_BY_CODE_MAPPING = "/studentstatus/{statusCode}";
    public static final String STUDENT_ALGORITHM_DATA = "/algorithmdata/{studentID}";
    public static final String GET_ALL_STUDENT_REPORT_DATA_BY_MINCODE = "/studentschoolreportdata/{mincode}";
    public static final String GET_ALL_STUDENT_REPORT_DATA = "/studentschoolreportdata";
    public static final String GET_ALL_STUDENT_NON_GRAD_REPORT_DATA = "/studentnongradreportdata";
    public static final String GET_ALL_STUDENT_NON_GRAD_REPORT_DATA_MINCODE = "/studentnongradreportdata/{mincode}";
    public static final String GET_ALL_SCHOOL_NON_GRAD_REPORT_DATA = "/schoolnongradreportdata";
    public static final String GET_ALL_DISTRICT_NON_GRAD_REPORT_DATA = "/districtnongradreportdata";
    public static final String GET_DECEASED_STUDENT_ID = "/deceasedstudentid";
    
    public static final String RETURN_TO_ORIGINAL_STATE = "/algorithmerror/{studentID}";

    public static final String GRAD_STUDENT_HISTORY = "/studentHistory/{studentID}";
    public static final String GRAD_STUDENT_OPTIONAL_PROGRAM_HISTORY = "/studentOptionalProgramHistory/{studentID}";
    public static final String GRAD_STUDENT_HISTORY_BY_ID = "/studentHistory/historyid/{historyID}";
    public static final String GRAD_STUDENT_OPTIONAL_PROGRAM_HISTORY_BY_ID = "/studentOptionalProgramHistory/historyid/{historyID}";

    public static final String GRAD_STUDENT_HISTORY_BY_BATCH_ID = "/studentHistory/batchid/{batchId}";

    public static final String GET_ALL_HISTORY_ACTIVITY_MAPPING = "/historyactivity";
    public static final String GET_ALL_HISTORY_ACTIVITY_BY_CODE_MAPPING = "/historyactivity/{activityCode}";

    public static final String GET_STUDENTS_FOR_YEARLY_DISTRIBUTION = "/yearlydistribution";

    // Data Conversion : Initial Student Load & Ongoing Update from TRAX to GRAD
    public static final String CONV_GRADUATION_STATUS_BY_STUDENT_ID = "/conv/studentid/{studentID}";
    public static final String CONV_STUDENT_OPTIONAL_PROGRAM = "/conv/studentoptionalprogram";
    public static final String CONV_STUDENT_CAREER_PROGRAM = "/conv/studentcareerprogram";
    public static final String CONV_GRADUATION_STATUS_FOR_ONGOING_UPDATES = "/conv/ongoingupdate/gradstatus";

    public static final String CONV_STUDENT_OPTIONAL_PROGRAM_BY_STUDENT_ID = "/conv/studentoptionalprogram/{optionalProgramID}/{studentID}";
    public static final String CONV_STUDENT_CAREER_PROGRAM_BY_STUDENT_ID = "/conv/studentcareerprogram/{careerProgramCode}/{studentID}";

    public static final String EDW_GRADUATION_STATUS_SNAPSHOT = "/edw/snapshot";

    //Default Date format constants
    public static final String DEFAULT_CREATED_BY = "API_GRAD_STUDENT";
    public static final String DEFAULT_UPDATED_BY = "API_GRAD_STUDENT";

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SECOND_DEFAULT_DATE_FORMAT = "yyyy/MM/dd";
    public static final String SECOND_DEFAULT_DATE_TIME_FORMAT = "yyyy/MM/dd HH:mm:ss";
    public static final String TRAX_DATE_FORMAT = "yyyyMM";
    public static final String PROGRAM_COMPLETION_DATE_FORMAT = "yyyy/MM";
    public static final String DATE_FORMAT = "yyyy/MM/dd";

    //NATS
    @Value("${nats.url}")
    String natsUrl;

    @Value("${nats.maxReconnect}")
    Integer natsMaxReconnect;

    @Value("${nats.connectionName}")
    private String connectionName;

    //Endpoints
    @Value("${endpoint.grad-trax-api.school-by-min-code.url}")
    private String schoolByMincodeUrl;

    @Value("${endpoint.grad-trax-api.district-by-district-code.url}")
    private String districtByDistrictCodeUrl;

    @Value("${endpoint.grad-program-api.career_program-by-career-code.url}")
    private String careerProgramByCodeUrl;

	@Value("${endpoint.grad-program-api.optional_program_name_by_optional_program_id.url}")
	private String gradOptionalProgramNameUrl;

    @Value("${endpoint.grad-program-api.optional_program_id_by_program_code_optional_program_code.url}")
    private String gradOptionalProgramDetailsUrl;

    @Value("${endpoint.grad-program-api.program_name_by_program_code.url}")
    private String gradProgramNameUrl;

    @Value("${endpoint.pen-student-api.by-studentid.url}")
    private String penStudentApiByStudentIdUrl;

    @Value("${endpoint.grad-student-graduation-api.save-student-ungrad-reason.url}")
    private String saveStudentUndoCompletionReasonByStudentIdUrl;

    @Value("${endpoint.grad-student-graduation-api.ungrad-reason.ungrad-reason-by-reason-code.url}")
    private String undoCompletionReasonDetailsUrl;

    @Value("${endpoint.pen-student-api.search.url}")
    private String penStudentApiSearchUrl;

    @Value("${endpoint.pen-student-api.by-pen.url}")
    private String penStudentApiByPenUrl;

    @Value("${endpoint.pen-student-api.student.url}")
    private String penStudentApiUrl;
    
    @Value("${endpoint.grad-graduation-report-api.delete-student-achievement.url}")
    private String deleteStudentAchievements;


    @Value("${endpoint.grad-graduation-report-api.student-certificates.url}")
    private String studentCertificates;

    @Value("${endpoint.grad-graduation-report-api.archive-student-achievement.url}")
    private String archiveStudentAchievements;

    @Value("${endpoint.grad-trax-api.commonschool-by-mincode.url}")
    private String schoolByMincodeSchoolApiUrl;

    @Value("${endpoint.grad-trax-api.all-commonschools.url}")
    private String schoolsSchoolApiUrl;
    
    // Splunk LogHelper Enabled
    @Value("${splunk.log-helper.enabled}")
    private boolean splunkLogHelperEnabled;

    // Incremental Trax Update
    @Value("${trax.update.enabled}")
    private boolean traxUpdateEnabled;

    // Data Conversion option
    @Value("${data-conversion.student-guid-pen-xref.enabled}")
    private boolean studentGuidPenXrefEnabled;

    // Scheduler: ongoing updates from GRAD to TRAX
    @Value("${cron.scheduled.process.events.stan.run}")
    private String gradToTraxCronRun;

    @Value("${cron.scheduled.process.events.stan.lockAtLeastFor}")
    private String gradToTraxLockAtLeastFor;

    @Value("${cron.scheduled.process.events.stan.lockAtMostFor}")
    private String gradToTraxLockAtMostFor;

    @Value("${cron.scheduled.process.events.stan.threshold}")
    private int gradToTraxProcessingThreshold;

    @Value("${cron.scheduled.process.purge-old-records.staleInDays}")
    private int recordsStaleInDays;
}
