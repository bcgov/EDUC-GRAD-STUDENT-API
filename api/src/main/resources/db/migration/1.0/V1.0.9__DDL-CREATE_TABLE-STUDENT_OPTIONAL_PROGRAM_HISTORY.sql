-- API_GRAD_STUDENT.STUDENT_OPTIONAL_PROGRAM_HISTORY definition

CREATE TABLE "STUDENT_OPTIONAL_PROGRAM_HISTORY"
   (	"STUDENT_OPTIONAL_PROGRAM_HISTORY_ID" RAW(16) DEFAULT SYS_GUID(),
	"HISTORY_ACTIVITY_CODE" VARCHAR2(15) NOT NULL ENABLE,
	"STUDENT_OPTIONAL_PROGRAM_ID" RAW(16) NOT NULL ENABLE,
	"OPTIONAL_PROGRAM_ID" RAW(16) NOT NULL ENABLE,
	"GRADUATION_STUDENT_RECORD_ID" RAW(16) NOT NULL ENABLE,
	"COMPLETION_DATE" DATE,
	"PROGRAM_NOTE" CLOB,
	"CREATE_USER" VARCHAR2(32) NOT NULL ENABLE,
	"CREATE_DATE" DATE NOT NULL ENABLE,
	"UPDATE_USER" VARCHAR2(32) NOT NULL ENABLE,
	"UPDATE_DATE" DATE NOT NULL ENABLE,
	 CONSTRAINT "STUDENT_OPTIONAL_PROGRAM_HISTORY_PK" PRIMARY KEY ("STUDENT_OPTIONAL_PROGRAM_HISTORY_ID")
  USING INDEX TABLESPACE "API_GRAD_IDX"  ENABLE,
	 CONSTRAINT "STUD_OPTPGM_HSTRY_HSTRY_ACTVTYCD_FK" FOREIGN KEY ("HISTORY_ACTIVITY_CODE")
	  REFERENCES "HISTORY_ACTIVITY_CODE" ("HISTORY_ACTIVITY_CODE") ENABLE,
	 CONSTRAINT "STUD_OPTPGM_HSTRY_GRDSTUD_REC_FK" FOREIGN KEY ("GRADUATION_STUDENT_RECORD_ID")
         REFERENCES "GRADUATION_STUDENT_RECORD" ("GRADUATION_STUDENT_RECORD_ID") ENABLE
   ) SEGMENT CREATION DEFERRED
 NOCOMPRESS LOGGING
  TABLESPACE "API_GRAD_DATA"   NO INMEMORY
 LOB ("PROGRAM_NOTE") STORE AS SECUREFILE (
  TABLESPACE "API_GRAD_BLOB_DATA" ENABLE STORAGE IN ROW
  NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES ) ;