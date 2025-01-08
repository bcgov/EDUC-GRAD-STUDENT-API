package ca.bc.gov.educ.api.gradstudent.util;

import ca.bc.gov.educ.api.gradstudent.model.dto.GradStatusEventPayloadDTO;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

public class EducGradStudentApiUtils {

	private static final Logger logger = LoggerFactory.getLogger(EducGradStudentApiUtils.class);
    private static final String ERROR_MSG = "Error : {}";

	private EducGradStudentApiUtils() {}
	
    public static String formatDate (Date date) {
        if (date == null)
            return null;

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        return simpleDateFormat.format(date);
    }

    public static String formatDate (Date date, String dateFormat) {
	    if(date == null) {
	        return null;
        }
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

    public static LocalDate parseLocalDate(String dateString, String dateFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return LocalDate.parse(dateString, formatter);
    }

    public static String parseDateFromString (String sessionDate) {
        if (sessionDate == null)
            return null;
        return parseDateByFormat(sessionDate, EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
    }

    public static String parseTraxDate (String sessionDate) {
        if (sessionDate == null)
            return null;
        return parseDateByFormat(sessionDate, EducGradStudentApiConstants.TRAX_DATE_FORMAT);
    }

    private static String parseDateByFormat(final String sessionDate, final String dateFormat) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat);
        try {
            Date date = simpleDateFormat.parse(sessionDate);
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
    	 Date temp;
		 String sDates = null;
         try {
            temp = toLastDayOfMonth(EducGradStudentApiUtils.parseDate(actualSessionDate, "yyyy/MM/dd"));
            sDates = EducGradStudentApiUtils.formatDate(temp, EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
         } catch (ParseException pe) {
            logger.error("ERROR: {}", pe.getMessage());
         }
         return sDates;
    }

    public static String parsingEdwDate(String sessionDate) {
        String actualSessionDate = sessionDate + "01";
        Date temp = new Date();
        String sDates = null;
        try {
            temp = EducGradStudentApiUtils.parseDate(actualSessionDate, "yyyyMMdd");
            sDates = EducGradStudentApiUtils.formatDate(temp, EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        } catch (ParseException pe) {
            logger.error("ERROR: {}", pe.getMessage());
        }
        return sDates;
    }

    public static Date parsingProgramCompletionDate(String sessionDate) {
        String actualSessionDate = sessionDate + "/01";
        Date temp;
        Date sDate = null;
        try {
            temp = toLastDayOfMonth(EducGradStudentApiUtils.parseDate(actualSessionDate, EducGradStudentApiConstants.DATE_FORMAT));
            String sDates = EducGradStudentApiUtils.formatDate(temp, EducGradStudentApiConstants.DATE_FORMAT);
            sDate = EducGradStudentApiUtils.parseDate(sDates, EducGradStudentApiConstants.DATE_FORMAT);
        } catch (ParseException pe) {
            logger.error(ERROR_MSG,pe.getMessage());
        }
        return sDate;
    }

    public static boolean isDateInFuture(Date programCompletionDate) {
        if (programCompletionDate != null) {
            String sessionDate = EducGradStudentApiUtils.formatDate(programCompletionDate, EducGradStudentApiConstants.PROGRAM_COMPLETION_DATE_FORMAT);
            Date pCD = EducGradStudentApiUtils.parsingProgramCompletionDate(sessionDate);
            int diff = EducGradStudentApiUtils.getDifferenceInDays(EducGradStudentApiUtils.getProgramCompletionDate(pCD), EducGradStudentApiUtils.getCurrentDate());
            return diff < 0;
        }
        return false;
    }

    public static String getCurrentDate() {
        Date gradDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat(EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        return dateFormat.format(gradDate);
    }

    public static String getProgramCompletionDate(Date pcd) {
        DateFormat dateFormat = new SimpleDateFormat(EducGradStudentApiConstants.DEFAULT_DATE_FORMAT);
        return dateFormat.format(pcd);
    }

    public static int getDifferenceInDays(String date1, String date2) {
        Period diff = Period.between(
                LocalDate.parse(date1),
                LocalDate.parse(date2));
        return diff.getDays() + diff.getMonths()*30;
    }

    public static GradStatusEventPayloadDTO transform(GraduationStudentRecordEntity graduationStudentRecord) {
        return GradStatusEventPayloadDTO.builder()
                .pen(graduationStudentRecord.getPen())
                .program(graduationStudentRecord.getProgram())
                .programCompletionDate(graduationStudentRecord.getProgramCompletionDate() != null?
                        EducGradStudentApiUtils.getProgramCompletionDate(graduationStudentRecord.getProgramCompletionDate()) : null)
                .studentGrade(graduationStudentRecord.getStudentGrade())
                .studentStatus(graduationStudentRecord.getStudentStatus())
                .honoursStanding(graduationStudentRecord.getHonoursStanding())
                .updateUser(graduationStudentRecord.getUpdateUser())
                .build();
    }

    static Date toLastDayOfMonth(Date date) {
        if(date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            return cal.getTime();
        }
        return null;
    }

}
