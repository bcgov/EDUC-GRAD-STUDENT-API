package ca.bc.gov.educ.api.gradstudent.util;

public class PermissionsConstants {
	private PermissionsConstants() {}

	public static final String PREFIX = "hasAuthority('";
	public static final String SUFFIX = "')";

	public static final String UPDATE_GRADUATION_STUDENT = PREFIX + "SCOPE_UPDATE_GRAD_GRADUATION_STATUS" + SUFFIX;
	public static final String READ_GRADUATION_STUDENT = PREFIX + "SCOPE_READ_GRAD_GRADUATION_STATUS" + SUFFIX;
	public static final String UPDATE_GRADUATION_STUDENT_OPTIONAL_PROGRAM = PREFIX + "SCOPE_UPDATE_GRAD_STUDENT_SPECIAL_DATA" + SUFFIX;
	public static final String READ_GRADUATION_STUDENT_OPTIONAL_PROGRAM = PREFIX + "SCOPE_READ_GRAD_STUDENT_SPECIAL_DATA" + SUFFIX;

	public static final String READ_GRAD_STUDENT_CAREER_DATA = PREFIX + "SCOPE_READ_GRAD_STUDENT_CAREER_DATA" + SUFFIX;
	public static final String READ_GRAD_STUDENT_UNGRAD_REASONS_DATA = PREFIX + "SCOPE_READ_GRAD_STUDENT_UNGRAD_REASONS_DATA" + SUFFIX;
	public static final String CREATE_GRAD_STUDENT_UNGRAD_REASONS_DATA = PREFIX + "SCOPE_CREATE_GRAD_STUDENT_UNGRAD_REASONS_DATA" + SUFFIX;
	public static final String UPDATE_GRADUATION_STUDENT_REPORTS = PREFIX + "SCOPE_UPDATE_GRAD_STUDENT_REPORT_DATA" + SUFFIX;
	public static final String READ_GRADUATION_STUDENT_REPORTS = PREFIX + "SCOPE_READ_GRAD_STUDENT_REPORT_DATA" + SUFFIX;
	public static final String READ_GRADUATION_STUDENT_CERTIFICATES = PREFIX + "SCOPE_READ_GRAD_STUDENT_CERTIFICATE_DATA" + SUFFIX;
	public static final String UPDATE_GRADUATION_STUDENT_CERTIFICATES = PREFIX + "SCOPE_UPDATE_GRAD_STUDENT_CERTIFICATE_DATA" + SUFFIX;
	public static final String READ_GRAD_ALGORITHM_RULES = PREFIX + "SCOPE_READ_GRAD_ALGORITHM_RULES_DATA" + SUFFIX;
	public static final String READ_GRAD_STUDENT_NOTES_DATA = PREFIX + "SCOPE_READ_GRAD_STUDENT_NOTES_DATA" + SUFFIX;
	public static final String CREATE_OR_UPDATE_GRAD_STUDENT_NOTES_DATA = PREFIX + "SCOPE_CREATE_GRAD_STUDENT_NOTES_DATA" + SUFFIX
		+ " and " + PREFIX + "SCOPE_UPDATE_GRAD_STUDENT_NOTES_DATA" + SUFFIX;
	public static final String DELETE_GRAD_STUDENT_NOTES_DATA = PREFIX + "SCOPE_DELETE_GRAD_STUDENT_NOTES_DATA" + SUFFIX;

	public static final String CREATE_STUDENT_STATUS = PREFIX + "SCOPE_CREATE_GRAD_STUDENT_STATUS_CODE_DATA"+ SUFFIX;
	public static final String READ_GRAD_STUDENT_STATUS = PREFIX + "SCOPE_READ_GRAD_STUDENT_STATUS_CODE_DATA" + SUFFIX;
	public static final String READ_GRAD_HISTORY_ACTIVITY = PREFIX + "SCOPE_READ_GRAD_HISTORY_ACTIVITY_CODE_DATA" + SUFFIX;
	public static final String DELETE_STUDENT_STATUS = PREFIX + "SCOPE_DELETE_GRAD_STUDENT_STATUS_CODE_DATA"+ SUFFIX;
	public static final String UPDATE_STUDENT_STATUS = PREFIX + "SCOPE_UPDATE_GRAD_STUDENT_STATUS_CODE_DATA"+ SUFFIX;
	public static final String STUDENT_ALGORITHM_DATA = PREFIX + "SCOPE_READ_GRAD_GRADUATION_STATUS" + SUFFIX + " and "
			+ PREFIX + "SCOPE_READ_GRAD_STUDENT_DATA" + SUFFIX;
}
