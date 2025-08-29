package ca.bc.gov.educ.api.gradstudent.service.event;

import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.service.event.choreographer.StudentMergeChoreographer;
import io.nats.client.Message;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class PenServicesEventHandlerDelegatorService {

    private final GraduationStatusService graduationStatusService;
    private final StudentMergeChoreographer studentMergeChoreographer;

    @Autowired
    public PenServicesEventHandlerDelegatorService(GraduationStatusService graduationStatusService, StudentMergeChoreographer studentMergeChoreographer) {
        this.graduationStatusService = graduationStatusService;
        this.studentMergeChoreographer = studentMergeChoreographer;
    }

    public void handleChoreographyEvent(@NonNull final ChoreographedEvent choreographedEvent, final Message message) {
        if(!this.graduationStatusService.eventExistsInDB(choreographedEvent).isPresent()) {
            final var persistedEvent = this.graduationStatusService.persistEventToDB(choreographedEvent);
            if(persistedEvent != null) {
                message.ack(); // acknowledge to Jet Stream that api got the message and it is now in DB.
                log.info("acknowledged to Jet Stream...");
                studentMergeChoreographer.handleEvent(persistedEvent);
            }
        }
        else {
            message.ack(); // acknowledge to Jet Stream that api got the message and it is already in DB.
            log.debug("Event with ID {} already exists in the database. No further action taken.", choreographedEvent.getEventID());
        }
    }

}
