package ca.bc.gov.educ.api.gradstudent.util;

import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.io.InputStream;

public interface Transformer {

    public Object unmarshall(byte[] input, Class<?> clazz);

    public Object unmarshall(String input, Class<?> clazz);

    public Object unmarshall(String input, CollectionType collectionType);

    public Object unmarshall(InputStream input, Class<?> clazz);

    public String marshall(Object input);

    public String getAccept();

    public String getContentType();

    public TypeFactory getTypeFactory();
}
