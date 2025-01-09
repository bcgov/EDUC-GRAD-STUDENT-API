CREATE OR REPLACE VIEW REPORT_GRAD_SCHOOL_YE_VW AS
SELECT
    COUNT(GRADUATION_STUDENT_RECORD_ID) STUD_NUM,
    A1.SCHOOL_OF_RECORD_ID SCHOOL_ID
FROM
    GRADUATION_STUDENT_RECORD A1
WHERE
    A1.PROGRAM_COMPLETION_DATE is null
  AND A1.STUDENT_STATUS_CODE = 'CUR'
  AND (A1.STUDENT_GRADE ='AD' or A1.STUDENT_GRADE='12')
GROUP BY A1.SCHOOL_OF_RECORD_ID
ORDER BY 1 DESC;

DROP VIEW REPORT_GRAD_DISTRICT_YE_VW;



