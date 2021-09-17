
CREATE TABLE "STATUS_SHEDLOCK"
(	"NAME" VARCHAR2(64 BYTE),
     "LOCK_UNTIL" TIMESTAMP (3),
     "LOCKED_AT" TIMESTAMP (3),
     "LOCKED_BY" VARCHAR2(255 BYTE),
     CONSTRAINT "STATUS_SHEDLOCK_PK" PRIMARY KEY ("NAME")
         USING INDEX TABLESPACE "API_GRAD_DATA"  ENABLE
) SEGMENT CREATION IMMEDIATE
    NOCOMPRESS LOGGING
    TABLESPACE "API_GRAD_DATA" NO INMEMORY ;

