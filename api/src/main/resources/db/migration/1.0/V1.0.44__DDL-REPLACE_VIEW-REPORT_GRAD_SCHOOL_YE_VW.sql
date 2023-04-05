CREATE OR REPLACE VIEW REPORT_GRAD_SCHOOL_YE_VW AS
    SELECT DISTINCT
        A1.SCHOOL_OF_RECORD MINCODE
    FROM
        GRADUATION_STUDENT_RECORD A1
    WHERE
        A1.PROGRAM_COMPLETION_DATE is null
        AND A1.STUDENT_STATUS_CODE = 'CUR'
        AND (A1.STUDENT_GRADE ='AD' or A1.STUDENT_GRADE='12')
;


