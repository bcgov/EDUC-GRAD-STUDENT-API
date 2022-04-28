package ca.bc.gov.educ.api.gradstudent.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;

import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import lombok.Data;

@Data
@MappedSuperclass
public class BaseEntity {
	@Column(name = "CREATE_USER", nullable = true)
    private String createUser;
	
	@Column(name = "CREATE_DATE", columnDefinition="datetime",nullable = false)
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-mm-dd hh:mm:ss")
    private Date createDate;
	
	
	@Column(name = "UPDATE_USER", nullable = false)
    private String updateUser;
	
	@Column(name = "UPDATE_DATE", columnDefinition="datetime",nullable = true)
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-mm-dd hh:mm:ss")
	private Date updateDate;
	
	@PrePersist
	protected void onCreate() {
		if (StringUtils.isBlank(createUser)) {
			this.createUser = ThreadLocalStateUtil.getCurrentUser();
			if (StringUtils.isBlank(createUser)) {
				this.createUser = EducGradStudentApiConstants.DEFAULT_CREATED_BY;
			}
		}		
		if (StringUtils.isBlank(updateUser)) {
			this.updateUser = ThreadLocalStateUtil.getCurrentUser();
			if (StringUtils.isBlank(updateUser)) {
				this.updateUser = EducGradStudentApiConstants.DEFAULT_UPDATED_BY;
			}
		}		
		this.createDate = new Date(System.currentTimeMillis());
		this.updateDate = new Date(System.currentTimeMillis());

	}

	@PreUpdate
	protected void onPersist() {
		this.updateDate = new Date();
		if (StringUtils.isBlank(updateUser)) {
			this.updateUser = ThreadLocalStateUtil.getCurrentUser();
			if (StringUtils.isBlank(updateUser)) {
				this.updateUser = EducGradStudentApiConstants.DEFAULT_UPDATED_BY;
			}
		}
		if (StringUtils.isBlank(createUser)) {
			this.createUser = ThreadLocalStateUtil.getCurrentUser();
			if (StringUtils.isBlank(createUser)) {
				this.createUser = EducGradStudentApiConstants.DEFAULT_CREATED_BY;
			}
		}
		if (this.createDate == null) {
			this.createDate = new Date();
		}
	}
}
