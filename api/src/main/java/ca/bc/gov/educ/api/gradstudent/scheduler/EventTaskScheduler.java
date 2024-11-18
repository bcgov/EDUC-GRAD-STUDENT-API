package ca.bc.gov.educ.api.gradstudent.scheduler;

import ca.bc.gov.educ.api.gradstudent.constant.SagaStatusEnum;
import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEntity;
import ca.bc.gov.educ.api.gradstudent.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.gradstudent.repository.SagaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Slf4j
@Component
public class EventTaskScheduler {

  @Getter(PRIVATE)
  private final Map<String, Orchestrator> sagaOrchestrators = new HashMap<>();
  @Getter(PRIVATE)
  private final SagaRepository sagaRepository;
  @Setter
  private List<String> statusFilters;

  private static final ObjectMapper mapper = new ObjectMapper();

  public EventTaskScheduler(final SagaRepository sagaRepository, final List<Orchestrator> orchestrators) {
    this.sagaRepository = sagaRepository;
    orchestrators.forEach(orchestrator -> this.sagaOrchestrators.put(orchestrator.getSagaName(), orchestrator));
    log.info("'{}' Saga Orchestrators are loaded.", String.join(",", this.sagaOrchestrators.keySet()));
  }

  @Scheduled(cron = "1 * * * * *")
  @SchedulerLock(name = "REPLAY_UNCOMPLETED_SAGAS",
      lockAtLeastFor = "PT50S", lockAtMostFor = "PT55S")
  public void findAndProcessUncompletedSagas() {
    final List<SagaEntity> sagas = this.getSagaRepository().findAllByStatusIn(this.getStatusFilters());
    if (!sagas.isEmpty()) {
      this.processUncompletedSagas(sagas);
    }
  }

  private void processUncompletedSagas(final List<SagaEntity> sagas) {
    for (val saga : sagas) {
      if (saga.getCreateDate().isBefore(LocalDateTime.now().minusMinutes(1))
          && this.getSagaOrchestrators().containsKey(saga.getSagaName())) {
        try {
          this.setRetryCountAndLog(saga);
          this.getSagaOrchestrators().get(saga.getSagaName()).replaySaga(saga);
        } catch (final InterruptedException ex) {
          Thread.currentThread().interrupt();
          log.error("InterruptedException while findAndProcessPendingSagaEvents :: for saga :: {} :: {}", saga, ex);
        } catch (final Exception e) {
          log.error("Exception while findAndProcessPendingSagaEvents :: for saga :: {} :: {}", saga, e);
        }
      }
    }
  }

  public List<String> getStatusFilters() {
    if (this.statusFilters != null && !this.statusFilters.isEmpty()) {
      return this.statusFilters;
    } else {
      final var statuses = new ArrayList<String>();
      statuses.add(SagaStatusEnum.IN_PROGRESS.toString());
      statuses.add(SagaStatusEnum.STARTED.toString());
      return statuses;
    }
  }

  private void setRetryCountAndLog(final SagaEntity saga) {
    Integer retryCount = saga.getRetryCount();
    if (retryCount == null || retryCount == 0) {
      retryCount = 1;
    } else {
      retryCount += 1;
    }
    saga.setRetryCount(retryCount);
    this.getSagaRepository().save(saga);
    logSagaRetry(saga);
  }

  private static void logSagaRetry(final SagaEntity saga) {
    final Map<String, Object> retrySagaMap = new HashMap<>();
    try {
      retrySagaMap.put("sagaName", saga.getSagaName());
      retrySagaMap.put("sagaId", saga.getSagaId());
      retrySagaMap.put("retryCount", saga.getRetryCount());
      MDC.putCloseable("sagaRetry", mapper.writeValueAsString(retrySagaMap));
      log.info("Saga is being retried.");
      MDC.clear();
    } catch (final Exception ex) {
      log.error("Exception ", ex);
    }
  }
}
