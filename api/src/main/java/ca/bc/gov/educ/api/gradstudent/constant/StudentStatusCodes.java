package ca.bc.gov.educ.api.gradstudent.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Getter
public enum StudentStatusCodes {
  CURRENT("CUR"),
  ARCHIVED("ARC"),
  DECEASED("DEC"),
  MERGED("MER"),
  TERMINATED("TER");

  private final String code;
  StudentStatusCodes(String code) {
    this.code = code;
  }

  public static Optional<StudentStatusCodes> findByValue(String value) {
    return Arrays.stream(values()).filter(e -> Objects.equals(e.code, value)).findFirst();
  }
}
