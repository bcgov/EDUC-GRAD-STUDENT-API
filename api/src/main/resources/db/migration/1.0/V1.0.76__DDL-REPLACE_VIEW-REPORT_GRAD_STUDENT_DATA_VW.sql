DROP VIEW REPORT_GRAD_STUDENT_DATA_VW;

CREATE OR REPLACE VIEW REPORT_GRAD_STUDENT_DATA_VW AS
SELECT
    A1.GRADUATION_STUDENT_RECORD_ID,
    A1.SCHOOL_OF_RECORD_ID SCHOOL_OF_RECORD_ID,
    A1.SCHOOL_AT_GRADUATION_ID SCHOOL_AT_GRAD_ID,
    A1.STUDENT_STATUS_CODE STATUS,
    A1.UPDATE_DATE,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStudent.pen' RETURNING VARCHAR2(10)) PEN,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStudent.legalFirstName' RETURNING VARCHAR2(64)) FIRST_NAME,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStudent.legalMiddleNames' RETURNING VARCHAR2(64)) MIDDLE_NAME,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStudent.legalLastName' RETURNING VARCHAR2(64)) LAST_NAME,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStudent.studentGrade' RETURNING VARCHAR2(64)) GRADE,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStudent.localID' RETURNING VARCHAR2(128)) LOCAL_ID,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStudent.dob' RETURNING VARCHAR2(128)) DOB,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.school.districtId' RETURNING VARCHAR2(128)) DISTRICT_ID,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.school.districtName' RETURNING VARCHAR2(128)) DISTRICT_NAME,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.school.schoolName' RETURNING VARCHAR2(128)) SCHOOL_NAME,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.school.address1' RETURNING VARCHAR2(128)) SCHOOL_ADDRESS1,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.school.address2' RETURNING VARCHAR2(128)) SCHOOL_ADDRESS2,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.school.city' RETURNING VARCHAR2(128)) SCHOOL_CITY,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.school.provCode' RETURNING VARCHAR2(128)) SCHOOL_PROVINCE,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.school.countryCode' RETURNING VARCHAR2(128)) SCHOOL_COUNTRY,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.school.postal' RETURNING VARCHAR2(128)) SCHOOL_POSTAL,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradProgram.programCode' RETURNING VARCHAR2(64)) PROGRAM_CODE,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradProgram.programName' RETURNING VARCHAR2(128)) PROGRAM_NAME,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStatus.programCompletionDate' RETURNING VARCHAR2(10)) PROGRAM_COMPLETION_DATE,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStatus.honoursStanding' RETURNING VARCHAR2(10)) HONORS_STANDING,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.graduated' RETURNING VARCHAR2(10)) IS_GRADUATED,
    JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.studentCertificatesTranscript.transcriptTypeCode' RETURNING VARCHAR2(16)) TRANSCRIPT_TYPE_CODE,
    JSON_QUERY(A1.STUDENT_GRAD_DATA, '$.studentCertificatesTranscript.certificateTypeCodes') CERTIFICATE_TYPE_CODES,
    JSON_QUERY(A1.STUDENT_GRAD_DATA, '$.studentCourses.studentCourseList.courseName' WITH WRAPPER) STUDENT_COURSES,
    JSON_QUERY(A1.STUDENT_GRAD_DATA, '$.nonGradReasons') NON_GRAD_REASONS
FROM
    API_GRAD_STUDENT.GRADUATION_STUDENT_RECORD A1
WHERE A1.STUDENT_STATUS_CODE NOT IN ('MER', 'DEC')

;


