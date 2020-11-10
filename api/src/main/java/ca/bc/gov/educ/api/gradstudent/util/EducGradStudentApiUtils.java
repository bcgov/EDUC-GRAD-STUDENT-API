package ca.bc.gov.educ.api.gradstudent.util;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.HttpHeaders;

public class EducGradStudentApiUtils {

	public static HttpHeaders getHeaders (String username, String secret)
    {
        String basicAuth = Base64.encodeBase64String((username + ":" + secret).getBytes());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Authorization", "Basic " + basicAuth);
        return httpHeaders;
    }
}
