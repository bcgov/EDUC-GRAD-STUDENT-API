-- STUDENT_COURSE_EXAM definition

CREATE TABLE "STUDENT_COURSE_EXAM"
(	"STUDENT_COURSE_EXAM_ID" RAW(16) DEFAULT SYS_GUID(),
     "SCHOOL_PERCENT" NUMBER(3),
     "EXAM_PERCENT" NUMBER(3),
     "SCHOOL_BEST_PERCENT" NUMBER(3),
     "EXAM_BEST_PERCENT" NUMBER(3),
     "EXAM_SPECIAL_CASE_CODE" VARCHAR2(1),
     "TO_WRITE_FLAG" VARCHAR2(1),
     "CREATE_USER" VARCHAR2(32) DEFAULT USER NOT NULL ENABLE,
     "CREATE_DATE" DATE DEFAULT SYSTIMESTAMP NOT NULL ENABLE,
     "UPDATE_USER" VARCHAR2(32) DEFAULT USER NOT NULL ENABLE,
     "UPDATE_DATE" DATE DEFAULT SYSTIMESTAMP NOT NULL ENABLE,
     CONSTRAINT "STUDENT_COURSE_EXAM_PK" PRIMARY KEY ("STUDENT_COURSE_EXAM_ID")
         USING INDEX TABLESPACE "API_GRAD_IDX"  ENABLE,
     CONSTRAINT "EXAM_SPL_CASECD_FK" FOREIGN KEY ("EXAM_SPECIAL_CASE_CODE")
     	  REFERENCES "EXAM_SPECIAL_CASE_CODE" ("EXAM_SPECIAL_CASE_CODE") ENABLE
) SEGMENT CREATION IMMEDIATE
 NOCOMPRESS LOGGING
  TABLESPACE "API_GRAD_DATA"   NO INMEMORY ;
