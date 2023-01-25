ALTER TABLE API_GRAD_STUDENT.GRADUATION_STUDENT_RECORD ADD "STUDENT_CITIZENSHIP_CODE" VARCHAR2(1)
CONSTRAINT "GRDSTUD_REC_STUD_CITIZ_CD_FK" REFERENCES "STUDENT_CITIZENSHIP_CODE" ("STUDENT_CITIZENSHIP_CODE");

ALTER TABLE API_GRAD_STUDENT.GRADUATION_STUDENT_RECORD_HISTORY ADD "STUDENT_CITIZENSHIP_CODE" VARCHAR2(1)
CONSTRAINT "GRDSTUD_REC_HSTRY_STUD_CITIZ_CD_FK" REFERENCES "STUDENT_CITIZENSHIP_CODE" ("STUDENT_CITIZENSHIP_CODE");

