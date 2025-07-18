package ca.bc.gov.educ.api.gradstudent.model.dc;

/**
 * The enum Event outcome.
 */
public enum EventOutcome {
  VALIDATION_SUCCESS_NO_ERROR_WARNING,
  VALIDATION_SUCCESS_WITH_ERROR,
  PEN_MATCH_PROCESSED,
  GRAD_STATUS_FETCHED,
  GRAD_STATUS_RESULTS_PROCESSED,
  PEN_MATCH_RESULTS_PROCESSED,
  READ_FROM_TOPIC_SUCCESS,
  INITIATE_SUCCESS,
  SAGA_COMPLETED,
  ENROLLED_PROGRAMS_WRITTEN,
  ADDITIONAL_STUDENT_ATTRIBUTES_CALCULATED,
  STUDENT_NOT_FOUND,
  DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API,
  COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API,
  ASSESSMENT_STUDENT_UPDATED,
  SCHOOL_OF_RECORD_UPDATED
}
