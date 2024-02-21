BEGIN
EXECUTE IMMEDIATE 'DROP INDEX X_OPTPROG_GRADSTUDID';
EXCEPTION
  WHEN OTHERS THEN
    IF SQLCODE != -1418 THEN
      RAISE;
END IF;
END;
/

CREATE INDEX X_OPTPROG_GRADSTUDID
    ON STUDENT_OPTIONAL_PROGRAM (GRADUATION_STUDENT_RECORD_ID)
    TABLESPACE API_GRAD_IDX;