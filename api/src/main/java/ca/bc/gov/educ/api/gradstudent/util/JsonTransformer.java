package ca.bc.gov.educ.api.gradstudent.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TimeZone;

@Component
public class JsonTransformer implements Transformer {

    private static final Logger log = LoggerFactory.getLogger(JsonTransformer.class);

    @Autowired
    ObjectMapper objectMapper;

    @PostConstruct
    void initMapper() {
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDate.class, new GradLocalDateSerializer());
        simpleModule.addSerializer(LocalDateTime.class, new GradLocalDateTimeSerializer());
        simpleModule.addDeserializer(LocalDate.class, new GradLocalDateDeserializer());
        simpleModule.addDeserializer(LocalDateTime.class, new GradLocalDateTimeDeserializer());
        objectMapper
                .findAndRegisterModules()
                .registerModule(simpleModule)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(SerializationFeature.INDENT_OUTPUT)
                .enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
                .enable(JsonGenerator.Feature.ESCAPE_NON_ASCII)
                .setTimeZone(TimeZone.getDefault())
        //        .enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS)
        ;
    }

    @Override
    public Object unmarshall(byte[] input, Class<?> clazz) {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = objectMapper.readValue(input, clazz);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        log.debug("Time taken for unmarshalling response from bytes to {} is {} ms", clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    public Object unmarshallWithWrapper(String input, Class<?> clazz) {
        final ObjectReader reader = objectMapper.readerFor(clazz);
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = reader
                    .with(DeserializationFeature.UNWRAP_ROOT_VALUE)
                    .with(DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY)
                    .readValue(input);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        log.debug("Time taken for unmarshalling response from String to {} is {} ms", clazz.getSimpleName(), (System.currentTimeMillis() - start));
        return result;
    }

    public String marshallWithWrapper(Object input) {
        ObjectWriter prettyPrinter = objectMapper.writer();
        String result = null;
        try {
            result = prettyPrinter
                    .with(SerializationFeature.WRAP_ROOT_VALUE)
                    .writeValueAsString(input);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }

        return result;
    }

    @Override
    public Object unmarshall(String input, Class<?> clazz) {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = objectMapper.readValue(input, clazz);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        log.debug("Time taken for unmarshalling response from String to {} is {} ms", clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public Object unmarshall(String input, CollectionType collectionType) {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = objectMapper.readValue(input, collectionType);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage(), e);
        }
        log.debug("Time taken for unmarshalling response from String to {} is {} ms", collectionType.getRawClass().getName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public Object unmarshall(InputStream input, Class<?> clazz) {
        Object result = null;
        long start = System.currentTimeMillis();
        try {
            result = objectMapper.readValue(input, clazz);
        } catch (IOException e) {
            log.error("Unable to unmarshall object {}", clazz.getSimpleName());
        }
        log.debug("Time taken for unmarshalling response from stream to {} is {} ms", clazz.getName(), (System.currentTimeMillis() - start));
        return result;
    }

    @Override
    public String marshall(Object input) {
        ObjectWriter prettyPrinter = objectMapper.writerWithDefaultPrettyPrinter();
        String result = null;
        try {
            result = prettyPrinter.writeValueAsString(input);
        } catch (IOException e) {
            log.error("Unable to marshall object {}", input.toString());
        }
        return result;
    }

    @Override
    public TypeFactory getTypeFactory() {
        return objectMapper.getTypeFactory();
    }

    @Override
    public String getAccept() {
        return "application/json";
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    public <T> T convertValue(Object fromValue, TypeReference<T> toValueTypeRef) throws IllegalArgumentException {
        return objectMapper.convertValue(fromValue, toValueTypeRef);
    }
}
