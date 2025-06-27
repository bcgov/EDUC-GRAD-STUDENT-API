package ca.bc.gov.educ.api.gradstudent.model.mapper;

import org.mapstruct.Mapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper
public class LocalDateTimeMapper {

    /**
     * Map string.
     *
     * @param dateTime the date time
     * @return the string
     */
    public String map(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dateTime);
    }

    /**
     * Map local date time.
     *
     * @param dateTime the date time
     * @return the local date time
     */
    public LocalDateTime map(String dateTime) {
        if (dateTime == null) {
            return null;
        }
        return LocalDateTime.parse(dateTime);
    }

}

