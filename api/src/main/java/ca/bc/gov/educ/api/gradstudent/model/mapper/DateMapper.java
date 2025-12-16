package ca.bc.gov.educ.api.gradstudent.model.mapper;

import org.mapstruct.Mapper;

import java.text.SimpleDateFormat;
import java.util.Date;

@Mapper
public class DateMapper {

    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    /**
     * Map Date to String.
     *
     * @param date the date
     * @return the string
     */
    public String map(Date date) {
        if (date == null) {
            return null;
        }
        return DATE_TIME_FORMAT.format(date);
    }

    /**
     * Map String to Date.
     *
     * @param dateStr the date string
     * @return the date
     */
    public Date map(String dateStr) {
        if (dateStr == null) {
            return null;
        }
        try {
            return DATE_TIME_FORMAT.parse(dateStr);
        } catch (Exception e) {
            return null;
        }
    }

}

