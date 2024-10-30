INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('01','Grade 1','First grade',180,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'N');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('02','Grade 2','Second grade',170,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'N');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('03','Grade 3','Third grade',160,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'N');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('04','Grade 4','Fourth grade',150,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'N');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('05','Grade 5','Fifth grade',140,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'N');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('06','Grade 6','Sixth grade',130,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'N');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('07','Grade 7','Seventh grade',120,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'N');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('08','Grade 8','Eighth grade',90,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'Y');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('09','Grade 9','Ninth grade',100,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'Y');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('10','Grade 10','Tenth grade',10,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'Y');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('11','Grade 11','Eleventh grade',20,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'Y');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('12','Grade 12','Twelfth grade',30,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'Y');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('AD','Adult Grad','Student on the Adult Graduation Program who is expected to graduate this year (subgrade)',40,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'Y');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('AN','Adult Non Grad','Student on the Adult Graduation Program who is not expected to graduate this year (subgrade)',50,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'Y');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('GA','Graduated Adult','An adult student who has graduated in BC or another jurisdiction (subgrade)',60,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'Y');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('GR','Graduated','Used by MyEdBC schools only when a student withdraws or the school believes they have finished their program',110,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'N');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('KF','Kindergarten Full-time','After 2012, the only valid grade code for kindergarten (subgrade)',200,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'N');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('SU','Secondary Upgraded','Secondary ungraded (subgrade)',80,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'Y');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('EU','Elementary Upgraded','Elementary ungraded (subgrade)',190,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'N');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('HS','Home School','Students whose parents provide their educational program and who are registered with the school (subgrade)',70,TIMESTAMP'2024-10-01 00:00:00.0',NULL, 'Y');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('KH','Kindergarten Half','Kindergarten half-time, applicable only until 2012 (subgrade)',210,TIMESTAMP'2024-10-01 00:00:00.0',TIMESTAMP'2012-06-30 00:00:00.0', 'N');

INSERT INTO STUDENT_GRADE_CODE (STUDENT_GRADE_CODE,LABEL,DESCRIPTION,DISPLAY_ORDER,EFFECTIVE_DATE,EXPIRY_DATE, EXPECTED)
VALUES ('OT','Other','A historic grade code applied by TRAX when an unexpected grade code was submitted for a student',220,TIMESTAMP'2024-10-01 00:00:00.0',TIMESTAMP'2024-10-01 00:00:00.0', 'Y');