-- API_GRAD_STUDENT.STUDENT_NONGRAD_REASONS_VW source

CREATE OR REPLACE FORCE EDITIONABLE VIEW "API_GRAD_STUDENT"."STUDENT_NONGRAD_REASONS_VW" ("STUDENT_PEN", "STUDENT_GUID", "TRANSCRIPTRULE1", "DESCRIPTION1", "GRADRULE1", "PROJECTED1", "TRANSCRIPTRULE2", "DESCRIPTION2", "GRADRULE2", "PROJECTED2", "TRANSCRIPTRULE3", "DESCRIPTION3", "GRADRULE3", "PROJECTED3", "TRANSCRIPTRULE4", "DESCRIPTION4", "GRADRULE4", "PROJECTED4", "TRANSCRIPTRULE5", "DESCRIPTION5", "GRADRULE5", "PROJECTED5", "TRANSCRIPTRULE6", "DESCRIPTION6", "GRADRULE6", "PROJECTED6", "TRANSCRIPTRULE7", "DESCRIPTION7", "GRADRULE7", "PROJECTED7", "TRANSCRIPTRULE8", "DESCRIPTION8", "GRADRULE8", "PROJECTED8", "TRANSCRIPTRULE9", "DESCRIPTION9", "GRADRULE9", "PROJECTED9", "TRANSCRIPTRULE10", "DESCRIPTION10", "GRADRULE10", "PROJECTED10", "TRANSCRIPTRULE11", "DESCRIPTION11", "GRADRULE11", "PROJECTED11", "TRANSCRIPTRULE12", "DESCRIPTION12", "GRADRULE12", "PROJECTED12") AS
  WITH j_data AS (
     SELECT d.graduation_student_record_id,
       TREAT (
           d.STUDENT_GRAD_DATA AS json
     ) AS STUDENT_GRAD_DATA
     FROM API_GRAD_STUDENT.GRADUATION_STUDENT_RECORD d
)
SELECT
    x.student_pen,
    student_guid,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[0].transcriptRule,1,2) AS transcriptRule1,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[0].description,1,30) AS description1,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[0].rule,1,3) as gradrule1,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[0].projected,1,5) AS projected1,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[1].transcriptRule,1,2) AS transcriptRule2,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[1].description,1,30) AS description2,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[1].rule,1,3)  as gradrule2,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[1].projected,1,5) AS projected2,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[2].transcriptRule,1,2) AS transcriptRule3,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[2].description,1,30) AS description3,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[2].rule,1,3)  as gradrule3,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[2].projected,1,5) AS projected3,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[3].transcriptRule,1,2) AS transcriptRule4,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[3].description,1,30) AS description4,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[3].rule,1,3)  as gradrule4,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[3].projected,1,5) AS projected4,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[4].transcriptRule,1,2) AS transcriptRule5,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[4].description,1,30) AS description5,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[4].rule,1,3)  as gradrule5,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[4].projected,1,5) AS projected5,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[5].transcriptRule,1,2) AS transcriptRule6,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[5].description,1,30) AS description6,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[5].rule,1,3)  as gradrule6,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[5].projected,1,5) AS projected6,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[6].transcriptRule,1,2) AS transcriptRule7,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[6].description,1,30) AS description7,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[6].rule,1,3)  as gradrule7,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[6].projected,1,5) AS projected7,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[7].transcriptRule,1,2) AS transcriptRule8,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[7].description,1,30) AS description8,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[7].rule,1,3)  as gradrule8,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[7].projected,1,5) AS projected8,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[8].transcriptRule,1,2) AS transcriptRule9,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[8].description,1,30) AS description9,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[8].rule,1,3)  as gradrule9,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[8].projected,1,5) AS projected9,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[9].transcriptRule,1,2) AS transcriptRule10,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[9].description,1,30) AS description10,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[9].rule,1,3)  as gradrule10,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[9].projected,1,5) AS projected10,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[10].transcriptRule,1,2) AS transcriptRule11,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[10].description,1,30) AS description11,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[10].rule,1,3)  as gradrule11,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[10].projected,1,5) AS projected11,

    substr(j.STUDENT_GRAD_DATA.nonGradReasons[11].transcriptRule,1,2) AS transcriptRule12,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[11].description,1,30) AS description12,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[11].rule,1,3)  as gradrule12,
    substr(j.STUDENT_GRAD_DATA.nonGradReasons[11].projected,1,5) AS projected12

FROM j_data j
         join graduation_student_record student on student.graduation_student_record_id = j.graduation_student_record_id
         join student_guid_pen_xref x on x.student_guid = student.graduation_student_record_id
WHERE student.STUDENT_GRAD_DATA IS json;