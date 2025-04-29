package ca.bc.gov.educ.api.gradstudent.model.mapper;

import org.apache.commons.lang3.StringUtils;

public class StringMapper {

    /**
     * Map string.
     *
     * @param value the value
     * @return the string
     */
    public String map(String value) {
        if (StringUtils.isNotEmpty(value)) {
            return value.trim();
        }
        return value;
    }
}

