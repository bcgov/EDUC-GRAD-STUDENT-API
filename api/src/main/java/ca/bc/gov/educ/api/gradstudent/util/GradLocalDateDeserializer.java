package ca.bc.gov.educ.api.gradstudent.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants.SECOND_DEFAULT_DATE_FORMAT;

public class GradLocalDateDeserializer extends StdDeserializer<LocalDate> {

    private static final Logger logger = LoggerFactory.getLogger(GradLocalDateDeserializer.class);

    public GradLocalDateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String fieldName = jsonParser.getParsingContext().getCurrentName();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;
        String dateAsString = jsonParser.getValueAsString();
        logger.debug("Deserialize LocalDate of {} and value {}", fieldName, dateAsString);
        //Fix date format as programCompletion date YYYY/MM
        if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() < 10 && dateAsString.contains("/")) {
            int year = StringUtils.substringBefore(dateAsString, "/").length();
            int slashCount = StringUtils.countMatches(dateAsString, "/");
            if(year == 4 && slashCount == 1) {
                dateAsString = dateAsString + "/01";
            }
            if(slashCount > 0) {
                formatter = DateTimeFormatter.ofPattern(SECOND_DEFAULT_DATE_FORMAT);
            }
            return LocalDate.parse(dateAsString, formatter);
        } else if(jsonParser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            long timestamp = jsonParser.getValueAsLong();
            return LocalDate.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        } else if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() == 10 && dateAsString.contains("-")) {
            return LocalDate.parse(dateAsString, formatter);
        } else if(StringUtils.isNotBlank(dateAsString) && dateAsString.length() == 10 && dateAsString.contains("/")) {
            formatter = DateTimeFormatter.ofPattern(SECOND_DEFAULT_DATE_FORMAT);
            return LocalDate.parse(dateAsString, formatter);
        } else if(StringUtils.isNotBlank(dateAsString)) {
            return LocalDate.parse(dateAsString, formatter);
        }
        return null;
    }
}
