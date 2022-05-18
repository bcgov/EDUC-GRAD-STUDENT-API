package ca.bc.gov.educ.api.gradstudent.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * The type JWT util.
 */
public class JwtUtil {

  private JwtUtil() {
  }

  /**
   * Gets username string from object.
   *
   * @param jwt the JWT
   * @return the username string from jwt
   */
  public static String getUsername(Jwt jwt) {
    return (String) jwt.getClaims().get("preferred_username");
  }

  /**
   * Gets email string from object.
   *
   * @param jwt the JWT
   * @return the username string from jwt
   */
  public static String getEmail(Jwt jwt) {
    return (String) jwt.getClaims().get("email");
  }

  /**
   * Gets name string from object.
   *
   * @param jwt the JWT
   * @return the username string from jwt
   */
  public static String getName(Jwt jwt) {
    StringBuilder sb = new StringBuilder();
    String givenName = (String) jwt.getClaims().get("given_name");
    if (StringUtils.isNotBlank(givenName)) {
      sb.append(givenName.charAt(0));
    }
    String familyName = (String) jwt.getClaims().get("family_name");
    sb.append(familyName);

    if (StringUtils.isBlank(sb.toString())) {
      sb.append(getUsername(jwt));
    }
    return sb.toString();
  }
}