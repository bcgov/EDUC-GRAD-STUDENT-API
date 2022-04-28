package ca.bc.gov.educ.api.gradstudent.config;

import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.LogHelper;
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;

@Component
@Slf4j
public class RequestInterceptor implements AsyncHandlerInterceptor {

	@Autowired
	GradValidation validation;

	@Autowired
	EducGradStudentApiConstants constants;

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		// for async this is called twice so need a check to avoid setting twice.
		if (request.getAttribute("startTime") == null) {
			final long startTime = Instant.now().toEpochMilli();
			request.setAttribute("startTime", startTime);
		}
		validation.clear();
		// correlationID
		val correlationID = request.getHeader(EducGradStudentApiConstants.CORRELATION_ID);
		if (correlationID != null) {
			ThreadLocalStateUtil.setCorrelationID(correlationID);
		}

		// username
		JwtAuthenticationToken authenticationToken = (JwtAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
		Jwt jwt = (Jwt) authenticationToken.getCredentials();
		String email = (String) jwt.getClaims().get("email");
		if (email != null) {
			ThreadLocalStateUtil.setCurrentUser(email);
		}
		return true;
	}

	/**
	 * After completion.
	 *
	 * @param request  the request
	 * @param response the response
	 * @param handler  the handler
	 * @param ex       the ex
	 */
	@Override
	public void afterCompletion(@NonNull final HttpServletRequest request, final HttpServletResponse response, @NonNull final Object handler, final Exception ex) {
		LogHelper.logServerHttpReqResponseDetails(request, response, constants.isSplunkLogHelperEnabled());
		val correlationID = request.getHeader(EducGradStudentApiConstants.CORRELATION_ID);
		if (correlationID != null) {
			response.setHeader(EducGradStudentApiConstants.CORRELATION_ID, request.getHeader(EducGradStudentApiConstants.CORRELATION_ID));
		}
		// reset
		ThreadLocalStateUtil.setCorrelationID(null);
		ThreadLocalStateUtil.setCurrentUser(null);
	}
}
