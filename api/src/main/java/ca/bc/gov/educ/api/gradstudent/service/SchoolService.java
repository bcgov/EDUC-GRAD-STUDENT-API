package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonTransformer;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SchoolService {
	EducGradStudentApiConstants educGradStudentApiConstants;
	RESTService restService;

	JsonTransformer jsonTransformer;
	@Autowired
	public SchoolService(EducGradStudentApiConstants educGradStudentApiConstants, RESTService restService, JsonTransformer jsonTransformer) {
		this.educGradStudentApiConstants = educGradStudentApiConstants;
		this.restService = restService;
		this.jsonTransformer = jsonTransformer;
	}

	public School getSchoolByMincode(String mincode) {
		if (mincode == null) return null;
		List<School> schools = this.restService.get(String.format(educGradStudentApiConstants.getSchoolsByMincodeUrl(), mincode), List.class, null);
		return (schools != null && !schools.isEmpty()) ? jsonTransformer.convertValue(schools.get(0), new TypeReference<School>() {}) : null;
	}

	public School getSchoolBySchoolId(UUID schoolId) {
		if (schoolId == null) return null;
		return this.restService.get(String.format(educGradStudentApiConstants.getSchoolBySchoolIdUrl(), schoolId), School.class, null);
	}
}
