package ca.bc.gov.educ.api.gradstudent.model.mapper;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;

import java.math.BigInteger;

@Mapper
public class BigIntegerMapper {

        public BigInteger map(String value) {
            if (StringUtils.isNotBlank(value)) {
                return new BigInteger(value);
            }
            return null;
        }
}

