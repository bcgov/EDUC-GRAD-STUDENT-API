ALTER TABLE API_GRAD_STUDENT.GRADUATION_STUDENT_RECORD ADD RECALCULATE_PROJECTED_GRAD VARCHAR2(1);
ALTER TABLE API_GRAD_STUDENT.GRADUATION_STUDENT_RECORD ADD BATCH_ID NUMBER(19,0);
ALTER TABLE API_GRAD_STUDENT.GRADUATION_STUDENT_RECORD_HISTORY ADD RECALCULATE_PROJECTED_GRAD VARCHAR2(1);
ALTER TABLE API_GRAD_STUDENT.GRADUATION_STUDENT_RECORD_HISTORY ADD BATCH_ID NUMBER(19,0);