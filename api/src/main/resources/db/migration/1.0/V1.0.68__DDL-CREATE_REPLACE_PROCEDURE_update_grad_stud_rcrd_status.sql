create or replace PROCEDURE update_grad_stud_rcrd_status (
    in_sor              IN VARCHAR2,
    in_stud_stat_from   IN graduation_student_record.student_status_code%TYPE DEFAULT 'CUR',
    in_stud_stat_to     IN graduation_student_record.student_status_code%TYPE DEFAULT 'ARC',
    in_batch_id         IN graduation_student_record.batch_id%TYPE DEFAULT -1,
    out_stud_count      OUT INTEGER
) IS

    t_sors         t_school_of_record;
    v_sor          graduation_student_record.school_of_record%TYPE;

    v_limit_bulk   PLS_INTEGER DEFAULT 10000;
    v_commit_after PLS_INTEGER DEFAULT 1000;

    failure_in_forall EXCEPTION;
    PRAGMA exception_init ( failure_in_forall, -24381 );

    l_last         PLS_INTEGER;
    l_start        PLS_INTEGER;
    l_end          PLS_INTEGER;

    TYPE stud_info_rt IS RECORD (
                                    graduation_student_record_id graduation_student_record.graduation_student_record_id%TYPE,
                                    student_status_code          graduation_student_record.student_status_code%TYPE
                                );

    TYPE stud_info_tt IS
        TABLE OF stud_info_rt INDEX BY PLS_INTEGER;
    l_stud_info    stud_info_tt;

    CURSOR stud_stat_cur IS
        SELECT
            graduation_student_record_id,
            student_status_code
        FROM
            graduation_student_record
        WHERE
                1 = 1
          AND school_of_record = v_sor
          AND student_status_code = in_stud_stat_from;

BEGIN
    out_stud_count := 0;
    IF in_sor IS NOT NULL THEN
        SELECT
            regexp_substr(in_sor, '[^,]+', 1, level)
                BULK COLLECT
        INTO t_sors
        FROM
            dual
        CONNECT BY
            regexp_substr(in_sor, '[^,]+', 1, level) IS NOT NULL;
    ELSE
        select
            distinct SCHOOL_OF_RECORD
                BULK COLLECT
        INTO t_sors
        from
            graduation_student_record
        where
                student_status_code = in_stud_stat_from;
    END IF;

    FOR rec IN (
        SELECT
            column_value sor
        FROM
            TABLE ( t_sors )
        ) LOOP
            v_sor := rec.sor;
            OPEN stud_stat_cur;
            LOOP
                FETCH stud_stat_cur
                    BULK COLLECT INTO l_stud_info LIMIT v_limit_bulk;
                EXIT WHEN l_stud_info.count = 0;
                l_start := 1;
                l_last := l_stud_info.count;
                out_stud_count := out_stud_count + l_last;
                LOOP
                    EXIT WHEN l_start > l_last;
                    l_end := least(l_start + v_commit_after - 1, l_last);
                    BEGIN
                        dbms_output.put_line('School ' || v_sor || ' start - end: ' || l_start || '-' || l_end);
                        FORALL indx IN l_start..l_end SAVE EXCEPTIONS
                            UPDATE graduation_student_record
                            SET
                                student_status_code = in_stud_stat_to,
                                batch_id = in_batch_id,
                                update_user = 'Batch Archive Process',
                                update_date = SYSDATE,
                                student_grad_data =
                                    json_transform(student_grad_data, SET '$.gradStatus.studentStatus' = in_stud_stat_to IGNORE ON MISSING)
                            WHERE
                                    graduation_student_record_id = l_stud_info(indx).graduation_student_record_id;

                    EXCEPTION
                        WHEN failure_in_forall THEN
                            /** Display the errors. **/
                            dbms_output.put_line(dbms_utility.format_error_stack);
                            dbms_output.put_line('Updated '
                                || SQL%rowcount
                                || ' rows.');
                            FOR indx IN 1..SQL%bulk_exceptions.count LOOP
                                    dbms_output.put_line('Error '
                                        || indx
                                        || ' occurred on index '
                                        || SQL%bulk_exceptions(indx).error_index
                                        || '  with error code '
                                        || SQL%bulk_exceptions(indx).error_code);
                                END LOOP;

                    END;
                    COMMIT;
                    --ROLLBACK;
                    l_start := l_end + 1;
                END LOOP;
                COMMIT;
                --ROLLBACK;
            END LOOP;
            CLOSE stud_stat_cur;
        END LOOP;

END;



