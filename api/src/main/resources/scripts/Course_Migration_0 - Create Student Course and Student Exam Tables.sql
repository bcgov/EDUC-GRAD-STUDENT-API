CREATE TABLE STUDENT_COURSE_EXAM
    NOLOGGING
    PARALLEL 4
AS
SELECT
    SYS_GUID() AS STUDENT_COURSE_EXAM_ID,
    prov_exam.PROV_SCHOOL_PCT as SCHOOL_PERCENT,
    prov_exam.PROV_EXAM_PCT as EXAM_PERCENT,
    prov_exam.BEST_SCHOOL_PCT as SCHOOL_BEST_PERCENT,
    prov_exam.BEST_EXAM_PCT as EXAM_BEST_PERCENT,
    TRIM(prov_exam.PROV_SPEC_CASE) as EXAM_SPECIAL_CASE_CODE,
    TRIM(prov_exam.TO_WRITE_FLAG) as TO_WRITE_FLAG,
    TRIM(prov_exam.WROTE_FLAG) as WROTE_FLAG,
    'STUDENT_COURSE_MIGRATION' as CREATE_USER,
    sysdate as CREATE_DATE,
    'STUDENT_COURSE_MIGRATION' as UPDATE_USER,
    sysdate as UPDATE_DATE,
    TRIM(prov_exam.CRSE_CODE) AS COURSE_CODE,
    TRIM(prov_exam.CRSE_LEVEL) AS COURSE_LEVEL,
    TRIM(prov_exam.CRSE_SESSION) AS COURSE_SESSION,
    TRIM(prov_exam.STUD_NO) AS PEN
FROM PROV_EXAM  prov_exam;

CREATE INDEX idx_exam_lookup
ON STUDENT_COURSE_EXAM (
     PEN,
     COURSE_CODE,
     COURSE_LEVEL,
     COURSE_SESSION
    );

CREATE TABLE STUDENT_COURSE
    NOLOGGING
    PARALLEL 8
AS
    WITH raw_union AS (
        SELECT
        STUD_NO                           AS PEN,
        CRSE_CODE                       AS CRSE_CODE,
        CRSE_LEVEL                      AS CRSE_LEVEL,
        CRSE_SESSION                 AS CRSE_SESSION,
        PRED_PCT                         AS PRED_PCT,
        PRED_LG                            AS PRED_LG,
        FINAL_PCT                         AS FINAL_PCT,
        FINAL_LG                            AS FINAL_LG,
        NUM_CREDITS                  AS NUM_CREDITS,
        CRSE_TYPE                       AS EQUIV_OR_CHALLENGE,
        GRAD_REQT_TYPE           AS FINE_ARTS_APPLIED_SKILLS,
        RELATED_CRSE                AS RELATED_CRSE,
        RELATED_LEVEL               AS RELATED_LEVEL,
        COURSE_DESC                 AS CUSTOMIZED_CRSE_NAME,
        NULL                        AS SCHOOL_PCT,
        NULL                        AS EXAM_PCT,
        NULL                        AS BEST_SCHOOL_PCT,
        NULL                        AS BEST_EXAM_PCT,
        NULL                        AS MET_LIT_NUM_REQT,
        NULL                        AS SPECIAL_CASE,
        NULL                        AS TO_WRITE_FLAG,
        NULL						  AS WROTE_FLAG
        FROM STUD_XCRSE x

        UNION ALL

        SELECT
        STUD_NO                           AS PEN,
        CRSE_CODE                      AS CRSE_CODE,
        CRSE_LEVEL                      AS CRSE_LEVEL,
        CRSE_SESSION                 AS CRSE_SESSION,
        PRED_PCT                         AS PRED_PCT,
        PRED_LG                            AS PRED_LG,
        PROV_FINAL_PCT             AS FINAL_PCT,
        FINAL_LG                            AS FINAL_LG,
        NUM_CREDITS                  AS NUM_CREDITS,
        CRSE_TYPE                        AS EQUIV_OR_CHALLENGE,
        NULL                                    AS FINE_ARTS_APPLIED_SKILLS,
        NULL                                    AS RELATED_CRSE,
        NULL                                    AS RELATED_LEVEL,
        NULL                                    AS CUSTOMIZED_CRSE_NAME,
        PROV_SCHOOL_PCT         AS SCHOOL_PCT,
        PROV_EXAM_PCT              AS EXAM_PCT,
        BEST_SCHOOL_PCT          AS BEST_SCHOOL_PCT,
        BEST_EXAM_PCT               AS BEST_EXAM_PCT,
        MET_LIT_NUM_REQT         AS MET_LIT_NUM_REQT,
        PROV_SPEC_CASE             AS SPECIAL_CASE,
        TO_WRITE_FLAG                 AS TO_WRITE_FLAG,
        WROTE_FLAG                      AS WROTE_FLAG
        FROM PROV_EXAM p
        ),
    preprocessed AS (
     SELECT
     SYS_GUID()                                             AS STUDENT_COURSE_ID,
    TRIM(PEN)                                                AS PEN,
    TRIM(CRSE_SESSION)                           AS COURSE_SESSION,
    TRIM(CRSE_CODE)                                 AS COURSE_CODE,
    TRIM(CRSE_LEVEL)                                AS COURSE_LEVEL,
    PRED_PCT,
    TRIM(PRED_LG)                                       AS PRED_LG,
    FINAL_PCT,
    TRIM(FINAL_LG)                                      AS FINAL_LG,
    NUM_CREDITS,
    TRIM(CUSTOMIZED_CRSE_NAME)       AS CUSTOMIZED_CRSE_NAME,
    TRIM(EQUIV_OR_CHALLENGE)              AS EQUIV_OR_CHALLENGE,
    TRIM(FINE_ARTS_APPLIED_SKILLS)     AS FINE_ARTS_APPLIED_SKILLS,
    RELATED_CRSE,
    RELATED_LEVEL,
    CASE
    WHEN TRIM(CRSE_CODE) IS NULL THEN NULL
    WHEN TRIM(CRSE_LEVEL) IS NULL THEN TRIM(CRSE_CODE)
    WHEN LENGTH(RPAD(CRSE_CODE,5,' ') || TRIM(CRSE_LEVEL)) > 7
    THEN SUBSTR(RPAD(CRSE_CODE,5,' ') || TRIM(CRSE_LEVEL),1,8)
    ELSE RPAD(CRSE_CODE,5,' ') || TRIM(CRSE_LEVEL)
    END AS COURSE_CODE_EXPR,
    CASE
    WHEN TRIM(RELATED_CRSE) IS NULL THEN NULL
    WHEN TRIM(RELATED_LEVEL) IS NULL THEN TRIM(RELATED_CRSE)
    WHEN LENGTH(RPAD(RELATED_CRSE,5,' ') || TRIM(RELATED_LEVEL)) > 7
    THEN SUBSTR(RPAD(RELATED_CRSE,5,' ') || TRIM(RELATED_LEVEL),1,8)
    ELSE RPAD(RELATED_CRSE,5,' ') || TRIM(RELATED_LEVEL)
    END AS RELATED_COURSE_CODE_EXPR,
    SCHOOL_PCT,
    EXAM_PCT,
    BEST_SCHOOL_PCT,
    BEST_EXAM_PCT,
    MET_LIT_NUM_REQT,
    SPECIAL_CASE,
    TO_WRITE_FLAG,
    WROTE_FLAG
    FROM raw_union
    )
SELECT
    p.STUDENT_COURSE_ID,
    sce.STUDENT_COURSE_EXAM_ID,
    sld.STUDENT_ID                                   AS GRADUATION_STUDENT_RECORD_ID,
    ccm.CRS_ID                                           AS COURSE_ID,
    p.COURSE_SESSION                            AS COURSE_SESSION,
    p.PRED_PCT                                          AS INTERIM_PERCENT,
    p.PRED_LG                                             AS INTERIM_LETTER_GRADE,
    p.FINAL_PCT                                          AS FINAL_PERCENT,
    p.FINAL_LG                                             AS FINAL_LETTER_GRADE,
    p.NUM_CREDITS                                   AS NUMBER_CREDITS,
    p.CUSTOMIZED_CRSE_NAME             AS CUSTOM_COURSE_NAME,
    p.EQUIV_OR_CHALLENGE                    AS EQUIVALENT_OR_CHALLENGE_CODE,
    p.FINE_ARTS_APPLIED_SKILLS           AS FINE_ARTS_APPLIED_SKILLS_CODE,
    rccm.CRS_ID                                           AS RELATED_COURSE_ID,
    'STUDENT_COURSE_MIGRATION'       AS CREATE_USER,
    SYSDATE                                                 AS CREATE_DATE,
    'STUDENT_COURSE_MIGRATION'       AS UPDATE_USER,
    SYSDATE                                                 AS UPDATE_DATE
FROM preprocessed p
     JOIN STUDENT_LINK sld
          ON sld.PEN = p.PEN
     LEFT JOIN STUDENT_COURSE_EXAM sce
               ON sce.PEN = p.PEN
                   AND sce.COURSE_CODE = p.COURSE_CODE
                   AND sce.COURSE_LEVEL = p.COURSE_LEVEL
                   AND sce.COURSE_SESSION = p.COURSE_SESSION
     LEFT JOIN CRSE_COURSE_CODE_MAPPINGS ccm
               ON ccm.ORIGINATING_SYSTEM_CHAR_ID = '39'
                   AND ccm.EXTERNAL_CODE = p.COURSE_CODE_EXPR
     LEFT JOIN CRSE_COURSE_CODE_MAPPINGS rccm
               ON rccm.ORIGINATING_SYSTEM_CHAR_ID = '39'
                   AND rccm.EXTERNAL_CODE = p.RELATED_COURSE_CODE_EXPR;

ALTER TABLE STUDENT_COURSE_EXAM DROP (PEN, COURSE_CODE, COURSE_LEVEL, COURSE_SESSION);