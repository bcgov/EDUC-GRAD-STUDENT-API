package ca.bc.gov.educ.api.gradstudent.util;

public interface PermissionsContants {
	String _PREFIX = "#oauth2.hasAnyScope('";
	String _SUFFIX = "')";

	String UPDATE_GRADUATION_STUDENT = _PREFIX + "UPDATE_GRAD_GRADUATION_STATUS" + _SUFFIX;
	String READ_GRADUATION_STUDENT = _PREFIX + "READ_GRAD_GRADUATION_STATUS" + _SUFFIX;
	String UPDATE_GRADUATION_STUDENT_SPECIAL_PROGRAM = _PREFIX + "UPDATE_GRAD_STUDENT_SPECIAL_DATA" + _SUFFIX;
	String READ_GRADUATION_STUDENT_SPECIAL_PROGRAM = _PREFIX + "READ_GRAD_STUDENT_SPECIAL_DATA" + _SUFFIX;

	String READ_GRAD_STUDENT_CAREER_DATA = _PREFIX + "READ_GRAD_STUDENT_CAREER_DATA" + _SUFFIX;
	String READ_GRAD_STUDENT_UNGRAD_REASONS_DATA = _PREFIX + "READ_GRAD_STUDENT_UNGRAD_REASONS_DATA" + _SUFFIX;
	String CREATE_GRAD_STUDENT_UNGRAD_REASONS_DATA = _PREFIX + "CREATE_GRAD_STUDENT_UNGRAD_REASONS_DATA" + _SUFFIX;
	String UPDATE_GRADUATION_STUDENT_REPORTS = _PREFIX + "UPDATE_GRAD_STUDENT_REPORT_DATA" + _SUFFIX;
	String READ_GRADUATION_STUDENT_REPORTS = _PREFIX + "READ_GRAD_STUDENT_REPORT_DATA" + _SUFFIX;
	String READ_GRADUATION_STUDENT_CERTIFICATES = _PREFIX + "READ_GRAD_STUDENT_CERTIFICATE_DATA" + _SUFFIX;
	String UPDATE_GRADUATION_STUDENT_CERTIFICATES = _PREFIX + "UPDATE_GRAD_STUDENT_CERTIFICATE_DATA" + _SUFFIX;
	String READ_GRAD_ALGORITHM_RULES = _PREFIX + "READ_GRAD_ALGORITHM_RULES_DATA" + _SUFFIX;
	String READ_GRAD_STUDENT_NOTES_DATA = _PREFIX + "READ_GRAD_STUDENT_NOTES_DATA" + _SUFFIX;
	String CREATE_OR_UPDATE_GRAD_STUDENT_NOTES_DATA = _PREFIX + "CREATE_GRAD_STUDENT_NOTES_DATA', 'UPDATE_GRAD_STUDENT_NOTES_DATA" + _SUFFIX;
	String DELETE_GRAD_STUDENT_NOTES_DATA = _PREFIX + "DELETE_GRAD_STUDENT_NOTES_DATA" + _SUFFIX;
	
	String CREATE_STUDENT_STATUS = _PREFIX + "CREATE_GRAD_STUDENT_STATUS_CODE_DATA"+ _SUFFIX;
	String READ_GRAD_STUDENT_STATUS = _PREFIX + "READ_GRAD_STUDENT_STATUS_CODE_DATA" + _SUFFIX;
	String DELETE_STUDENT_STATUS = _PREFIX + "DELETE_GRAD_STUDENT_STATUS_CODE_DATA"+ _SUFFIX;
	String UPDATE_STUDENT_STATUS = _PREFIX + "UPDATE_GRAD_STUDENT_STATUS_CODE_DATA"+ _SUFFIX;
}
