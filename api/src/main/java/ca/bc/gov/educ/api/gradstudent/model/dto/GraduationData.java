package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Data
@Component
@JsonIgnoreProperties
public class GraduationData {
    private GradSearchStudent gradStudent;
    private SchoolClob school;
    private String gradMessage;
    private List<GradRequirement> nonGradReasons;
    private boolean dualDogwood;
    private boolean isGraduated;
    private String latestSessionDate;

    @JsonIgnore
    public long getSessionDateMonthsIntervalNow() {
        if(StringUtils.isBlank(latestSessionDate)) {
            return Integer.MAX_VALUE;
        }
        return ChronoUnit.MONTHS.between(LocalDate.parse(latestSessionDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")), LocalDate.now());
    }
}
