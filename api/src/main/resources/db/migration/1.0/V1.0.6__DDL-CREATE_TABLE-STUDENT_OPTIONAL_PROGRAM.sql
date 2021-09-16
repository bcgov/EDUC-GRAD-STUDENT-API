
CREATE TABLE "STUDENT_OPTIONAL_PROGRAM"
(	"STUDENT_OPTIONAL_PROGRAM_ID" RAW(16) DEFAULT SYS_GUID(),
     "OPTIONAL_PROGRAM_ID" RAW(16) NOT NULL ENABLE,
     "GRADUATION_STUDENT_RECORD_ID" RAW(16) NOT NULL ENABLE,
     "COMPLETION_DATE" DATE,
     "PROGRAM_NOTE" CLOB,
     "CREATE_USER" VARCHAR2(32 BYTE) DEFAULT USER NOT NULL ENABLE,
     "CREATE_DATE" DATE DEFAULT SYSTIMESTAMP NOT NULL ENABLE,
     "UPDATE_USER" VARCHAR2(32 BYTE) DEFAULT USER NOT NULL ENABLE,
     "UPDATE_DATE" DATE DEFAULT SYSTIMESTAMP NOT NULL ENABLE,
     CONSTRAINT "GRAD_STUDENT_SPECIAL_PROGRAMS_PK" PRIMARY KEY ("STUDENT_OPTIONAL_PROGRAM_ID")
         USING INDEX TABLESPACE "API_GRAD_BLOB_DATA"  ENABLE,
     CONSTRAINT "FK_GRADUTION_STUDENT_RECORD" FOREIGN KEY ("GRADUATION_STUDENT_RECORD_ID")
         REFERENCES "GRADUATION_STUDENT_RECORD" ("GRADUATION_STUDENT_RECORD_ID") ENABLE
) SEGMENT CREATION IMMEDIATE
    NOCOMPRESS LOGGING
    TABLESPACE "API_GRAD_DATA" NO INMEMORY
    LOB ("PROGRAM_NOTE") STORE AS SECUREFILE (
    TABLESPACE "API_GRAD_BLOB_DATA" ENABLE STORAGE IN ROW
    NOCACHE LOGGING  NOCOMPRESS  KEEP_DUPLICATES) ;

