package ca.bc.gov.educ.api.gradstudent.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class EducGradStudentApiUtils {

	private static final Logger logger = LoggerFactory.getLogger(EducGradStudentApiUtils.class);
	
    public static String formatDate (Date date) {
        if (date == null)
            return null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        return simpleDateFormat.format(date);
    }

    public static String formatDate (Date date, String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        return simpleDateFormat.format(date);
    }

    public static Date parseDate (String dateString) {
        if (dateString == null || "".compareTo(dateString) == 0)
            return null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }

    public static Date parseDate (String dateString, String dateFormat) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return date;
    }
    
    public static String parseDateFromString (String sessionDate) {
        if (sessionDate == null)
            return null;

        
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(sessionDate);
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return localDate.getYear() +"/"+ String.format("%02d", localDate.getMonthValue());
            
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }       
    }
    
    public static String parseTraxDate (String sessionDate) {
        if (sessionDate == null)
            return null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        Date date = new Date();

        try {
            date = simpleDateFormat.parse(sessionDate);
            LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            return localDate.getYear() +"/"+ String.format("%02d", localDate.getMonthValue());
            
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }       
    }

	public static HttpHeaders getHeaders (String accessToken)
    {
		HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.setBearerAuth(accessToken);
        return httpHeaders;
    }
	
	 public static String parsingTraxDate(String sessionDate) {
    	 String actualSessionDate = sessionDate + "/01";
    	 Date temp = new Date();
		 String sDates = null;
         try {
            temp = EducGradStudentApiUtils.parseDate(actualSessionDate, "yyyy/MM/dd");
            sDates = EducGradStudentApiUtils.formatDate(temp, "yyyy-MM-dd");
         } catch (ParseException pe) {
            logger.error("ERROR: " + pe.getMessage());
         }
         return sDates;
    }
}
