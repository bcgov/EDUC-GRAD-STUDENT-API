UPDATE HISTORY_ACTIVITY_CODE SET EXPIRY_DATE = NULL WHERE to_char(EXPIRY_DATE, 'yyyy-mm-dd') = '2099-12-31';
UPDATE STUDENT_STATUS_CODE SET EXPIRY_DATE = NULL WHERE to_char(EXPIRY_DATE, 'yyyy-mm-dd') = '2099-12-31' OR to_char(EXPIRY_DATE, 'yyyy-mm-dd') = '1999-12-31' ;
