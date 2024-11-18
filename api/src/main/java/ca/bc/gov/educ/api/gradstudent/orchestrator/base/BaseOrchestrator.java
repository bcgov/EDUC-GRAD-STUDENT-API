package ca.bc.gov.educ.api.gradstudent.orchestrator.base;

import ca.bc.gov.educ.api.gradstudent.constant.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.messaging.MessagePublisher;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.NotificationEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEventStatesEntity;
import ca.bc.gov.educ.api.gradstudent.service.SagaService;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import static ca.bc.gov.educ.api.gradstudent.constant.EventOutcome.INITIATE_SUCCESS;
import static ca.bc.gov.educ.api.gradstudent.constant.EventOutcome.SAGA_COMPLETED;
import static ca.bc.gov.educ.api.gradstudent.constant.EventType.INITIATED;
import static ca.bc.gov.educ.api.gradstudent.constant.EventType.MARK_SAGA_COMPLETE;
import static ca.bc.gov.educ.api.gradstudent.constant.SagaStatusEnum.COMPLETED;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PUBLIC;

/**
 * The type Base orchestrator.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class BaseOrchestrator<T> implements EventHandler, Orchestrator {

  protected static final String SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT = "system is going to execute next event :: {} for current event {} and SAGA ID :: {}";
  protected static final String SELF = "SELF";
  protected final Class<T> clazz;
  protected final Map<EventType, List<SagaEventState<T>>> nextStepsToExecute = new LinkedHashMap<>();
  @Getter(PROTECTED)
  private final SagaService sagaService;
  @Getter(PROTECTED)
  private final MessagePublisher messagePublisher;
  @Getter(PUBLIC)
  private final String sagaName;
  @Getter(PUBLIC)
  private final String topicToSubscribe;
  @Setter(PROTECTED)
  protected boolean shouldSendNotificationEvent = true;

  protected BaseOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher,
                             final Class<T> clazz, final String sagaName,
                             final String topicToSubscribe) {
    this.sagaService = sagaService;
    this.messagePublisher = messagePublisher;
    this.clazz = clazz;
    this.sagaName = sagaName;
    this.topicToSubscribe = topicToSubscribe;
    this.populateStepsToExecuteMap();
  }

  protected List<SagaEventState<T>> createSingleCollectionEventState(final EventOutcome eventOutcome, final Predicate<T> nextStepPredicate, final EventType nextEventType, final SagaStep<T> stepToExecute) {
    final List<SagaEventState<T>> eventStates = new ArrayList<>();
    eventStates.add(this.buildSagaEventState(eventOutcome, nextStepPredicate, nextEventType, stepToExecute));
    return eventStates;
  }

  protected SagaEventState<T> buildSagaEventState(final EventOutcome eventOutcome, final Predicate<T> nextStepPredicate, final EventType nextEventType, final SagaStep<T> stepToExecute) {
    return SagaEventState.<T>builder().currentEventOutcome(eventOutcome).nextStepPredicate(nextStepPredicate).nextEventType(nextEventType).stepToExecute(stepToExecute).build();
  }

  protected BaseOrchestrator<T> registerStepToExecute(final EventType initEvent, final EventOutcome outcome, final Predicate<T> nextStepPredicate, final EventType nextEvent, final SagaStep<T> stepToExecute) {
    if (this.nextStepsToExecute.containsKey(initEvent)) {
      final List<SagaEventState<T>> states = this.nextStepsToExecute.get(initEvent);
      states.add(this.buildSagaEventState(outcome, nextStepPredicate, nextEvent, stepToExecute));
    } else {
      this.nextStepsToExecute.put(initEvent, this.createSingleCollectionEventState(outcome, nextStepPredicate, nextEvent, stepToExecute));
    }
    return this;
  }

  public BaseOrchestrator<T> step(final EventType currentEvent, final EventOutcome outcome, final EventType nextEvent, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(currentEvent, outcome, (T sagaData) -> true, nextEvent, stepToExecute);
  }

  public BaseOrchestrator<T> step(final EventType currentEvent, final EventOutcome outcome, final Predicate<T> nextStepPredicate, final EventType nextEvent, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(currentEvent, outcome, nextStepPredicate, nextEvent, stepToExecute);
  }

  public BaseOrchestrator<T> begin(final EventType nextEvent, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(INITIATED, INITIATE_SUCCESS, (T sagaData) -> true, nextEvent, stepToExecute);
  }

  public BaseOrchestrator<T> begin(final Predicate<T> nextStepPredicate, final EventType nextEvent, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(INITIATED, INITIATE_SUCCESS, nextStepPredicate, nextEvent, stepToExecute);
  }

  public void end(final EventType currentEvent, final EventOutcome outcome) {
    this.registerStepToExecute(currentEvent, outcome, (T sagaData) -> true, MARK_SAGA_COMPLETE, this::markSagaComplete);
  }

  public BaseOrchestrator<T> end(final EventType currentEvent, final EventOutcome outcome, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(currentEvent, outcome, (T sagaData) -> true, MARK_SAGA_COMPLETE, (Event event, SagaEntity saga, T sagaData) -> {
      stepToExecute.apply(event, saga, sagaData);
      this.markSagaComplete(event, saga, sagaData);
    });
  }

  public BaseOrchestrator<T> or() {
    return this;
  }

  public BaseOrchestrator<T> stepBuilder() {
    return this;
  }

  /**
   * this method will check if the event is not already processed. this could happen in SAGAs due to duplicate messages.
   * Application should be able to handle this.
   */
  protected boolean isNotProcessedEvent(final EventType currentEventType, final SagaEntity saga, final Set<EventType> eventTypes) {
    final EventType eventTypeInDB = EventType.valueOf(saga.getSagaState());
    final List<EventType> events = new LinkedList<>(eventTypes);
    final int dbEventIndex = events.indexOf(eventTypeInDB);
    final int currentEventIndex = events.indexOf(currentEventType);
    return currentEventIndex >= dbEventIndex;
  }

  protected SagaEventStatesEntity createEventState(@NotNull final SagaEntity saga, @NotNull final EventType eventType, @NotNull final EventOutcome eventOutcome, final String eventPayload) {
    final var user = this.sagaName.length() > 32 ? this.sagaName.substring(0, 32) : this.sagaName;
    return SagaEventStatesEntity.builder()
      .createDate(LocalDateTime.now())
      .createUser(user)
      .updateDate(LocalDateTime.now())
      .updateUser(user)
      .saga(saga)
      .sagaEventOutcome(eventOutcome.toString())
      .sagaEventState(eventType.toString())
      .sagaStepNumber(this.calculateStep(saga))
      .sagaEventResponse(StringUtils.isBlank(eventPayload) ? "NO-PAYLOAD-IN-RESPONSE" : eventPayload)
      .build();
  }

  protected void markSagaComplete(final Event event, final SagaEntity saga, final T sagaData) {
    this.markSagaComplete(event, saga, sagaData, "");
  }

  protected void markSagaComplete(final Event event, final SagaEntity saga, final T sagaData, final String payloadToSubscribers) {
    //Added to slow down complete write
    try {
      Thread.sleep(1000);
    }catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    
    log.trace("payload is {}", sagaData);
    if (this.shouldSendNotificationEvent) {
      final var finalEvent = new NotificationEvent();
      BeanUtils.copyProperties(event, finalEvent);
      finalEvent.setEventType(MARK_SAGA_COMPLETE);
      finalEvent.setEventOutcome(SAGA_COMPLETED);
      finalEvent.setSagaStatus(COMPLETED.toString());
      if(saga.getBatchId() != null) {
        finalEvent.setBatchId(String.valueOf(saga.getBatchId()));
      }
      finalEvent.setSagaName(this.getSagaName());
      finalEvent.setEventPayload(payloadToSubscribers);
      this.postMessageToTopic(this.getTopicToSubscribe(), finalEvent);
    }

    final SagaEventStatesEntity sagaEventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(COMPLETED.toString());
    saga.setStatus(COMPLETED.toString());
    saga.setUpdateDate(LocalDateTime.now());
    this.getSagaService().updateAttachedSagaWithEvents(saga, sagaEventStates);

  }

  private int calculateStep(final SagaEntity saga) {
    val sagaStates = this.getSagaService().findAllSagaStates(saga);
    return (sagaStates.size() + 1);
  }

  protected void postMessageToTopic(final String topicName, final Event nextEvent) {
    final var eventStringOptional = JsonUtil.getJsonString(nextEvent);
    if (eventStringOptional.isPresent()) {
      this.getMessagePublisher().dispatchMessage(topicName, eventStringOptional.get().getBytes());
    } else {
      log.error("event string is not present for  :: {} :: this should not have happened", nextEvent);
    }
  }

  protected Optional<SagaEventStatesEntity> findTheLastEventOccurred(final List<SagaEventStatesEntity> eventStates) {
    final int step = eventStates.stream().map(SagaEventStatesEntity::getSagaStepNumber).mapToInt(x -> x).max().orElse(0);
    return eventStates.stream().filter(element -> element.getSagaStepNumber() == step).findFirst();
  }

  /**
   * this method is called from the cron job , which will replay the saga process based on its current state.
   */
  @Override
  @Transactional
  @Async("sagaRetryTaskExecutor")
  public void replaySaga(final SagaEntity saga) throws IOException, InterruptedException, TimeoutException {
    final var eventStates = this.getSagaService().findAllSagaStates(saga);
    final var t = JsonUtil.getJsonObjectFromString(this.clazz, saga.getPayload());
    if (eventStates.isEmpty()) { //process did not start last time, lets start from beginning.
      this.replayFromBeginning(saga, t);
    } else {
      this.replayFromLastEvent(saga, eventStates, t);
    }
  }

  private void replayFromLastEvent(final SagaEntity saga, final List<SagaEventStatesEntity> eventStates, final T t) throws InterruptedException, TimeoutException, IOException {
    val sagaEventOptional = this.findTheLastEventOccurred(eventStates);
    if (sagaEventOptional.isPresent()) {
      val sagaEvent = sagaEventOptional.get();
      log.trace(sagaEventOptional.toString());
      final EventType currentEvent = EventType.valueOf(sagaEvent.getSagaEventState());
      final EventOutcome eventOutcome = EventOutcome.valueOf(sagaEvent.getSagaEventOutcome());
      final Event event = Event.builder()
        .eventOutcome(eventOutcome)
        .eventType(currentEvent)
        .eventPayload(sagaEvent.getSagaEventResponse())
        .build();
      this.findAndInvokeNextStep(saga, t, currentEvent, eventOutcome, event);
    }
  }

  private void findAndInvokeNextStep(final SagaEntity saga, final T t, final EventType currentEvent, final EventOutcome eventOutcome, final Event event) throws InterruptedException, TimeoutException, IOException {
    final Optional<SagaEventState<T>> sagaEventState = this.findNextSagaEventState(currentEvent, eventOutcome, t);
    if (sagaEventState.isPresent()) {
      log.trace(SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT, sagaEventState.get().getNextEventType(), event.toString(), saga.getSagaId());
      this.invokeNextEvent(event, saga, t, sagaEventState.get());
    }
  }

  private void replayFromBeginning(final SagaEntity saga, final T t) throws InterruptedException, TimeoutException, IOException {
    final Event event = Event.builder()
      .eventOutcome(INITIATE_SUCCESS)
      .eventType(INITIATED)
      .build();
    this.findAndInvokeNextStep(saga, t, INITIATED, INITIATE_SUCCESS, event);
  }

  @Override
  @Async("subscriberExecutor")
  @Transactional
  public void handleEvent(@NotNull final Event event) throws InterruptedException, IOException, TimeoutException {
    log.debug("Executing saga event {}", event);
    if (this.sagaEventExecutionNotRequired(event)) {
      log.trace("Execution is not required for this message returning EVENT is :: {}", event);
      return;
    }
    this.broadcastSagaInitiatedMessage(event);

    log.debug("About to find saga by ID with event :: {}", event);
    final var sagaOptional = this.getSagaService().findSagaById(event.getSagaId()); // system expects a saga record to be present here.
    if (sagaOptional.isPresent()) {
      val saga = sagaOptional.get();
      if (!COMPLETED.toString().equalsIgnoreCase(sagaOptional.get().getStatus())) {//possible duplicate message or force stop scenario check
        final T sagaData = JsonUtil.getJsonObjectFromString(this.clazz, saga.getPayload());
        final var sagaEventState = this.findNextSagaEventState(event.getEventType(), event.getEventOutcome(), sagaData);
        log.trace("found next event as {}", sagaEventState);
        if (sagaEventState.isPresent()) {
          this.process(event, saga, sagaData, sagaEventState.get());
        } else {
          log.error("This should not have happened, please check that both the saga api and all the participating apis are in sync in terms of events and their outcomes. {}", event); // more explicit error message,
        }
      } else {
        log.debug("Got message to process saga for saga ID :: {} but saga is already :: {}", saga.getSagaId(), saga.getStatus());
      }
    } else {
      log.error("Saga process without DB record is not expected. {}", event);
    }
  }

  @Override
  @Transactional(propagation = Propagation.MANDATORY)
  public void startSaga(@NotNull final SagaEntity saga) {
    try {
      log.debug("Starting saga with the following payload :: {}", saga);
      this.handleEvent(Event.builder()
        .eventType(INITIATED)
        .eventOutcome(INITIATE_SUCCESS)
        .sagaId(saga.getSagaId())
        .eventPayload(saga.getPayload())
        .build());
    } catch (final InterruptedException e) {
      log.error("InterruptedException while startSaga", e);
      Thread.currentThread().interrupt();
    } catch (final TimeoutException | IOException e) {
      log.error("Exception while startSaga", e);
    }
  }

  @Override
  @Transactional
  public SagaEntity createSaga(@NotNull final String payload, final String userName, final long batchId) {
    return this.sagaService.createSagaRecordInDB(this.sagaName, userName, payload, batchId);
  }

  @Transactional
  public List<SagaEntity> createSagas(final List<SagaEntity> sagaEntities) {
    return this.sagaService.createSagaRecordsInDB(sagaEntities);
  }

  /**
   * DONT DO ANYTHING the message was broad-casted for the frontend listeners, that a saga process has initiated, completed.
   *
   * @param event the event object received from queue.
   * @return true if this message need not be processed further.
   */
  private boolean sagaEventExecutionNotRequired(@NotNull final Event event) {
    return (event.getEventType() == INITIATED && event.getEventOutcome() == INITIATE_SUCCESS && SELF.equalsIgnoreCase(event.getReplyTo()))
      || event.getEventType() == MARK_SAGA_COMPLETE && event.getEventOutcome() == SAGA_COMPLETED;
  }

  private void broadcastSagaInitiatedMessage(@NotNull final Event event) {
    if (this.shouldSendNotificationEvent && event.getEventType() == INITIATED && event.getEventOutcome() == INITIATE_SUCCESS
      && !SELF.equalsIgnoreCase(event.getReplyTo())) {
      final var notificationEvent = new NotificationEvent();
      BeanUtils.copyProperties(event, notificationEvent);
      notificationEvent.setSagaStatus(INITIATED.toString());
      notificationEvent.setReplyTo(SELF);
      notificationEvent.setSagaName(this.getSagaName());
      this.postMessageToTopic(this.getTopicToSubscribe(), notificationEvent);
    }
  }

  protected Optional<SagaEventState<T>> findNextSagaEventState(final EventType currentEvent, final EventOutcome eventOutcome, final T sagaData) {
    val sagaEventStates = this.nextStepsToExecute.get(currentEvent);
    return sagaEventStates == null ? Optional.empty() : sagaEventStates.stream().filter(el ->
      el.getCurrentEventOutcome() == eventOutcome && el.nextStepPredicate.test(sagaData)
    ).findFirst();
  }

  protected void process(@NotNull final Event event, final SagaEntity saga, final T sagaData, final SagaEventState<T> sagaEventState) throws InterruptedException, TimeoutException, IOException {
    if (!saga.getSagaState().equalsIgnoreCase(COMPLETED.toString())
      && this.isNotProcessedEvent(event.getEventType(), saga, this.nextStepsToExecute.keySet())) {
      log.debug(SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT, sagaEventState.getNextEventType(), event, saga.getSagaId());
      this.invokeNextEvent(event, saga, sagaData, sagaEventState);
    } else {
      log.debug("Ignoring this message as we have already processed it or it is completed. {}", event.toString()); // it is expected to receive duplicate message in saga pattern, system should be designed to handle duplicates.
    }
  }

  protected void invokeNextEvent(final Event event, final SagaEntity saga, final T sagaData, final SagaEventState<T> sagaEventState) throws InterruptedException, TimeoutException, IOException {
    final SagaStep<T> stepToExecute = sagaEventState.getStepToExecute();
    stepToExecute.apply(event, saga, sagaData);
  }

  public abstract void populateStepsToExecuteMap();

}
