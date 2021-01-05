package ca.bc.gov.educ.api.gradstudent.util;

import org.springframework.http.HttpHeaders;

public class EducGradStudentApiUtils {

	public static HttpHeaders getHeaders (String accessToken)
    {
		HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.setBearerAuth(accessToken);
        return httpHeaders;
    }
}
