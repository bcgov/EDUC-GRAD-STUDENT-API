BEGIN
    EXECUTE IMMEDIATE '
        ALTER TABLE STUDENT_OPTIONAL_PROGRAM
        ADD CONSTRAINT STUDENT_OPTIONAL_PROGRAM_UK UNIQUE (
             GRADUATION_STUDENT_RECORD_ID
            ,OPTIONAL_PROGRAM_ID
        ) ENABLE';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -2261 THEN
            RAISE;
        END IF;
END;
/

