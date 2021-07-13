package ca.bc.gov.educ.api.gradstudent.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

@Data
@MappedSuperclass
public class BaseEntity {
	@Column(name = "CREATED_BY", nullable = true)
    private String createdBy;
	
	@Column(name = "CREATED_TIMESTAMP", nullable = true)
    private Date createdTimestamp;
	
	@Column(name = "UPDATED_BY", nullable = true)
    private String updatedBy;
	
	@Column(name = "UPDATED_TIMESTAMP", nullable = true)
    private Date updatedTimestamp;
	
	@PrePersist
	protected void onCreate() {
		if (StringUtils.isBlank(createdBy)) {
			this.createdBy = "GRADUATION";
		}		
		if (StringUtils.isBlank(updatedBy)) {
			this.updatedBy = "GRADUATION";
		}		
		this.createdTimestamp = new Date(System.currentTimeMillis());
		this.updatedTimestamp = new Date(System.currentTimeMillis());

	}

	@PreUpdate
	protected void onPersist() {
		this.updatedTimestamp = new Date(System.currentTimeMillis());
		if (StringUtils.isBlank(updatedBy)) {
			this.updatedBy = "GRADUATION";
		}
		if (StringUtils.isBlank(createdBy)) {
			this.createdBy = "GRADUATION";
		}
		if (this.createdTimestamp == null) {
			this.createdTimestamp = new Date(System.currentTimeMillis());
		}
	}
}
