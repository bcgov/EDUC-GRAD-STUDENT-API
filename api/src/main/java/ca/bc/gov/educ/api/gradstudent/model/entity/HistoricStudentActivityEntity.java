package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "HISTORIC_STUDENT_ACTIVITY")
public class HistoricStudentActivityEntity extends BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
        name = "UUID",
        strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "HISTORIC_STUDENT_ACTIVITY_ID", nullable = false)
    private UUID historicStudentActivityID;

    @Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID graduationStudentRecordID;

    @Column(name = "ACTIVITY_DATE", columnDefinition="datetime",nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-mm-dd hh:mm:ss")
    private LocalDateTime date;

    @Column(name = "TYPE", length = 3)
    private String type;

    @Column(name = "PROGRAM", length = 12)
    private String program;

    @Column(name = "USER_ID", length = 12)
    private String userID;

    @Column(name = "BATCH", length = 3)
    private String batch;

    @Column(name = "SEQ_NO", length = 4)
    private String seqNo;
}
