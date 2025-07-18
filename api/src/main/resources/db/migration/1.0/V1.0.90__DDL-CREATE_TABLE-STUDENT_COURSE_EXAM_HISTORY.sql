-- STUDENT_COURSE_EXAM_HISTORY definition

CREATE TABLE "STUDENT_COURSE_EXAM_HISTORY"
(	"STUDENT_COURSE_EXAM_HISTORY_ID" RAW(16) DEFAULT SYS_GUID(),
    "STUDENT_COURSE_EXAM_ID" RAW(16) NOT NULL ENABLE,
    "SCHOOL_PERCENT" NUMBER(3),
    "EXAM_PERCENT" NUMBER(3),
    "SCHOOL_BEST_PERCENT" NUMBER(3),
    "EXAM_BEST_PERCENT" NUMBER(3),
    "EXAM_SPECIAL_CASE_CODE" VARCHAR2(1),
    "TO_WRITE_FLAG" VARCHAR2(1),
    "WROTE_FLAG" VARCHAR2(1),
    "CREATE_USER" VARCHAR2(32) DEFAULT USER NOT NULL ENABLE,
    "CREATE_DATE" DATE DEFAULT SYSTIMESTAMP NOT NULL ENABLE,
    "UPDATE_USER" VARCHAR2(32) DEFAULT USER NOT NULL ENABLE,
    "UPDATE_DATE" DATE DEFAULT SYSTIMESTAMP NOT NULL ENABLE,
     CONSTRAINT "STUDENT_COURSE_EXAM_HISTORY_PK" PRIMARY KEY ("STUDENT_COURSE_EXAM_HISTORY_ID")
         USING INDEX TABLESPACE "API_GRAD_IDX"  ENABLE
) SEGMENT CREATION IMMEDIATE
 NOCOMPRESS LOGGING
  TABLESPACE "API_GRAD_DATA"   NO INMEMORY ;
