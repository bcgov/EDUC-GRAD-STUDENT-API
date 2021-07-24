package ca.bc.gov.educ.api.gradstudent.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;

@Data
@MappedSuperclass
public class BaseEntity {
	@Column(name = "CREATE_USER", nullable = true)
    private String createUser;
	
	@Column(name = "CREATE_DATE", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
	
	@Column(name = "UPDATE_USER", nullable = true)
    private String updateUser;
	
	@Column(name = "UPDATE_DATE", nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	private Date updateDate;
	
	@PrePersist
	protected void onCreate() {
		this.updateUser = "API_STUDENT_GRADUATION";
		this.createUser = "API_STUDENT_GRADUATION";
		this.createDate = new Date(System.currentTimeMillis());
		this.createDate = new Date(System.currentTimeMillis());

	}

	@PreUpdate
	protected void onPersist() {
		this.updateDate = new Date(System.currentTimeMillis());
		this.updateUser = "API_STUDENT_GRADUATION";
		if (StringUtils.isBlank(createUser)) {
			createUser = "API_STUDENT_GRADUATION";
		}
		if (this.createDate == null) {
			this.createDate = new Date(System.currentTimeMillis());
		}
	}
}
