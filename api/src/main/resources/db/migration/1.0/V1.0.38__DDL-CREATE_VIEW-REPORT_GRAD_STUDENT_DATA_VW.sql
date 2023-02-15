CREATE OR REPLACE VIEW REPORT_GRAD_STUDENT_DATA_VW AS
    SELECT
        A1.GRADUATION_STUDENT_RECORD_ID,
        NVL(A1.SCHOOL_AT_GRADUATION, A1.SCHOOL_OF_RECORD) MINCODE,
        JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStudent.pen' RETURNING VARCHAR2(10) DEFAULT 'error' ON ERROR) PEN,
        JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStudent.legalFirstName' RETURNING VARCHAR2(64) DEFAULT 'error' ON ERROR) FIRST_NAME,
        JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStudent.legalMiddleNames' RETURNING VARCHAR2(64) DEFAULT 'error' ON ERROR) MIDDLE_NAME,
        JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStudent.legalLastName' RETURNING VARCHAR2(64) DEFAULT 'error' ON ERROR) LAST_NAME,
        JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.school.districtName' RETURNING VARCHAR2(128) DEFAULT 'error' ON ERROR) DISTRICT_NAME,
        JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.school.schoolName' RETURNING VARCHAR2(128) DEFAULT 'error' ON ERROR) SCHOOL_NAME,
        JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradProgram.programCode' RETURNING VARCHAR2(64) DEFAULT 'error' ON ERROR) PROGRAM_CODE,
        JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradProgram.programName' RETURNING VARCHAR2(128) DEFAULT 'error' ON ERROR) PROGRAM_NAME,
        JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.gradStatus.programCompletionDate' RETURNING VARCHAR2(10) DEFAULT 'error' ON ERROR) PROGRAM_COMPLETION_DATE,
        JSON_VALUE(A1.STUDENT_GRAD_DATA, '$.graduated' RETURNING VARCHAR2(10) DEFAULT 'error' ON ERROR) IS_GRADUATED
    FROM
        GRADUATION_STUDENT_RECORD A1

;

