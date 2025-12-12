package ca.bc.gov.educ.api.gradstudent.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtils {

    private DateUtils(){}


    public static LocalDateTime stringToLocalDateTime(DateTimeFormatter formatter, String date){
        return LocalDate.parse(date, formatter).atStartOfDay();
    }

    // Date
    public static LocalDate toLocalDate(Date date) {
        if(date == null) return null;
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static Date toDate(LocalDate localDate) {
        if(localDate == null) return null;
        return Date.from(localDate.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    // DateTime
    public static LocalDateTime toLocalDateTime(Date date) {
        if(date == null) return null;
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static Date toDate(LocalDateTime localDateTime) {
        if(localDateTime == null) return null;
        return Date.from(localDateTime
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }


}
