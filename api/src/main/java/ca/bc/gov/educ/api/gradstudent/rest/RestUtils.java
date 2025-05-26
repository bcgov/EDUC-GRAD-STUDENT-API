package ca.bc.gov.educ.api.gradstudent.rest;

import ca.bc.gov.educ.api.gradstudent.constant.Topics;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.exception.GradStudentAPIRuntimeException;
import ca.bc.gov.educ.api.gradstudent.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.gradstudent.messaging.MessagePublisher;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventType;
import ca.bc.gov.educ.api.gradstudent.model.dto.LetterGrade;
import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1.CoregCoursesRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.GraduationProgramCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1.OptionalProgramCode;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is used for REST calls
 *
 * @author Marco Villeneuve
 */
@Component
@Slf4j
public class RestUtils {
  public static final String NATS_TIMEOUT = "Either NATS timed out or the response is null , correlationID :: ";
  private static final String CONTENT_TYPE = "Content-Type";
  public static final String NO_RESPONSE_RECEIVED_WITHIN_TIMEOUT_FOR_CORRELATION_ID = "No response received within timeout for correlation ID ";
  private final MessagePublisher messagePublisher;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final WebClient webClient;
  private final ReadWriteLock optionalProgramLock = new ReentrantReadWriteLock();
  private final ReadWriteLock letterGradeLock = new ReentrantReadWriteLock();
  private final ReadWriteLock gradProgramLock = new ReentrantReadWriteLock();
  private final Map<String, LetterGrade> letterGradeMap = new ConcurrentHashMap<>();
  private final Map<String, OptionalProgramCode> optionalProgramCodesMap = new ConcurrentHashMap<>();
  private final Map<String, GraduationProgramCode> gradProgramCodeMap = new ConcurrentHashMap<>();
  final EducGradStudentApiConstants constants;

  @Autowired
  public RestUtils(final MessagePublisher messagePublisher, WebClient webClient, EducGradStudentApiConstants constants) {
    this.messagePublisher = messagePublisher;
    this.webClient = webClient;
      this.constants = constants;
  }

  private List<OptionalProgramCode> getOptionalPrograms() {
    log.info("Calling Grad api to load optional programs to memory");
    return this.webClient.get()
            .uri(constants.getGradProgramUrl() + "/optionalprograms")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(OptionalProgramCode.class)
            .collectList()
            .block();
  }

  private List<GraduationProgramCode> getGraduationProgramCodes() {
    log.info("Calling Grad api to load graduation program codes to memory");
    return this.webClient.get()
            .uri(constants.getGradProgramUrl() + "/programs")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(GraduationProgramCode.class)
            .collectList()
            .block();
  }

  public void populateGradProgramCodesMap() {
    val writeLock = this.gradProgramLock.writeLock();
    try {
      writeLock.lock();
      for (val program : this.getGraduationProgramCodes()) {
        program.setEffectiveDate(!StringUtils.isBlank(program.getEffectiveDate()) ? LocalDateTime.parse(program.getEffectiveDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toString() : null);
        program.setExpiryDate(!StringUtils.isBlank(program.getExpiryDate()) ? LocalDateTime.parse(program.getExpiryDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toString() : null);
        this.gradProgramCodeMap.put(program.getProgramCode(), program);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache grad program codes {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} grad program codes to memory", this.gradProgramCodeMap.values().size());
    log.debug(this.gradProgramCodeMap.values().toString());
  }

  public List<GraduationProgramCode> getGraduationProgramCodeList(boolean activeOnly) {
    if (this.gradProgramCodeMap.isEmpty()) {
      log.info("Graduation Program Code map is empty reloading them");
      this.populateGradProgramCodesMap();
    }
    if(activeOnly){
      return this.gradProgramCodeMap.values().stream().filter(code -> StringUtils.isBlank(code.getExpiryDate()) || LocalDateTime.parse(code.getExpiryDate()).isAfter(LocalDateTime.now())).toList();
    }

    return this.gradProgramCodeMap.values().stream().toList();
  }

  public void populateOptionalProgramsMap() {
    val writeLock = this.optionalProgramLock.writeLock();
    try {
      writeLock.lock();
      for (val program : this.getOptionalPrograms()) {
        this.optionalProgramCodesMap.put(program.getOptProgramCode(), program);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache optional program {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} optional programs to memory", this.optionalProgramCodesMap.values().size());
  }

  public void populateLetterGradeMap() {
    val writeLock = this.letterGradeLock.writeLock();
    try {
      writeLock.lock();
      for (val grade : this.getLetterGrades()) {
        this.letterGradeMap.put(grade.getGrade(), grade);
      }
    } catch (Exception ex) {
      log.error("Unable to load map cache letter grade {}", ex);
    } finally {
      writeLock.unlock();
    }
    log.info("Loaded  {} letter grades to memory", this.letterGradeMap.values().size());
  }

  private List<LetterGrade> getLetterGrades() {
    log.info("Calling Grad student graduation api to load grades to memory");
    return this.webClient.get()
            .uri(this.constants.getLetterGradesUrl() + "/lettergrade")
            .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(LetterGrade.class)
            .collectList()
            .block();
  }

  public List<LetterGrade> getLetterGradeList() {
    if (this.letterGradeMap.isEmpty()) {
      log.info("Letter Grade map is empty reloading them");
      this.populateLetterGradeMap();
    }
    return this.letterGradeMap.values().stream().toList();
  }

  public List<OptionalProgramCode> getOptionalProgramCodeList() {
    if (this.optionalProgramCodesMap.isEmpty()) {
      log.info("Optional Program Code map is empty reloading them");
      this.populateOptionalProgramsMap();
    }
    return this.optionalProgramCodesMap.values().stream().toList();
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class, EntityNotFoundException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public Student getStudentByPEN(UUID correlationID, String assignedPEN) {
    try {
      final TypeReference<Event> refEvent = new TypeReference<>() {};
      final TypeReference<Student> refPenMatchResult = new TypeReference<>() {};
      Object event = Event.builder().sagaId(correlationID).eventType(EventType.GET_STUDENT).eventPayload(assignedPEN).build();
      val responseMessage = this.messagePublisher.requestMessage(Topics.STUDENT_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event)).completeOnTimeout(null, 120, TimeUnit.SECONDS).get();
      if (responseMessage != null) {
        byte[] data = responseMessage.getData();
        if (data == null || data.length == 0) {
          log.debug("Empty response data for getStudentByPEN; treating as student not found for PEN: {}", assignedPEN);
          throw new EntityNotFoundException(Student.class);
        }

        log.debug("Response message for getStudentByPen: {}", responseMessage);
        Event responseEvent = objectMapper.readValue(responseMessage.getData(), refEvent);

        if (EventOutcome.STUDENT_NOT_FOUND.equals(responseEvent.getEventOutcome())) {
          log.info("Student not found for PEN: {}", assignedPEN);
          throw new EntityNotFoundException(Student.class);
        }

        return objectMapper.readValue(responseMessage.getData(), refPenMatchResult);
      } else {
        throw new GradStudentAPIRuntimeException(NATS_TIMEOUT + correlationID);
      }

    } catch (EntityNotFoundException ex) {
      log.debug("Entity Not Found occurred calling GET STUDENT service :: {}", ex.getMessage());
      throw ex;
    } catch (final Exception ex) {
      log.error("Error occurred calling GET STUDENT service :: {}", ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradStudentAPIRuntimeException(NATS_TIMEOUT + correlationID + ex.getMessage());
    }
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {EntityNotFoundException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public CoregCoursesRecord getCoursesByExternalID(UUID correlationID, String externalID) {
    try {
      final TypeReference<CoregCoursesRecord> refCourseInformation = new TypeReference<>() {};

      Event event = Event.builder()
              .sagaId(correlationID)
              .eventType(EventType.GET_COURSE_FROM_EXTERNAL_ID)
              .eventPayload(externalID)
              .replyTo("coreg-response-topic")
              .build();

      val responseMessage = this.messagePublisher
              .requestMessage(Topics.COREG_API_TOPIC.toString(), JsonUtil.getJsonBytesFromObject(event))
              .completeOnTimeout(null, 120, TimeUnit.SECONDS)
              .get();

      if (responseMessage == null) {
        throw new GradStudentAPIRuntimeException(NO_RESPONSE_RECEIVED_WITHIN_TIMEOUT_FOR_CORRELATION_ID + correlationID);
      }

      byte[] responseData = responseMessage.getData();
      if (responseData.length == 0) {
        log.debug("No course information found for externalID {}", externalID);
        throw new EntityNotFoundException(CoregCoursesRecord.class);
      }

      log.debug("Received response from NATS: {}", new String(responseData, StandardCharsets.UTF_8));
      return objectMapper.readValue(responseData, refCourseInformation);

    } catch (EntityNotFoundException ex) {
      log.debug("EntityNotFoundException occurred calling GET_COURSE_FROM_EXTERNAL_ID service :: {}", ex.getMessage());
      throw new EntityNotFoundException();
    } catch (final Exception ex) {
      log.error("Error occurred calling GET_COURSE_FROM_EXTERNAL_ID service :: {}", ex.getMessage());
      Thread.currentThread().interrupt();
      throw new GradStudentAPIRuntimeException(NATS_TIMEOUT + correlationID);
    }
  }

}

