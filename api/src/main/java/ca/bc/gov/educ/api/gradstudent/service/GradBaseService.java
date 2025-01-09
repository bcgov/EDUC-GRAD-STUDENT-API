package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.SchoolClob;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.District;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class GradBaseService {
    // Student Status
    public static final String STUDENT_STATUS_ARCHIVED = "ARC";
    public static final String STUDENT_STATUS_MERGED = "MER";
    public static final String STUDENT_STATUS_TERMINATED = "TER";


    protected abstract WebClient getWebClient();

    protected abstract EducGradStudentApiConstants getConstants();

    protected void validateStudentStatusAndResetBatchFlags(GraduationStudentRecordEntity gradEntity) {
        String currentStudentStatus = gradEntity.getStudentStatus();
        // GRAD2-2934
        // 1. If a student in GRAD is ARC/TER then do not set TVR flag when their other data changes
        // 2. If a student in GRAD is changed to ARC/TER then set TVR flag to NULL
        if (STUDENT_STATUS_ARCHIVED.equalsIgnoreCase(currentStudentStatus) || STUDENT_STATUS_TERMINATED.equalsIgnoreCase(currentStudentStatus)) {
            gradEntity.setRecalculateProjectedGrad(null);
        }
        // GRAD2-2922 & GRAD2-2950
        // 1. If a student in GRAD is MER then do not set Transcript & TVR flags  when their other data changes
        // 2. If a student in GRAD is changed to MER then set Transcript & TVR flags to NULL
        if (STUDENT_STATUS_MERGED.equalsIgnoreCase(currentStudentStatus)) {
            gradEntity.setRecalculateGradStatus(null);
            gradEntity.setRecalculateProjectedGrad(null);
        }
    }

    protected List<School> getSchoolsByDistricts(List<UUID> districtIds, String accessToken) {
        List<School> results = new ArrayList<>();
        for (UUID districtId : districtIds) {
            results.addAll(getSchoolsByDistrictId(districtId, accessToken));
        }
        return results;
    }

    protected List<School> getSchoolsByDistrictId(UUID districtId, String accessToken) {
        return getWebClient().get()
                .uri(String.format(getConstants().getSchoolsByDistrictIdUrl(), districtId))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                })
                .retrieve().bodyToMono(new ParameterizedTypeReference<List<School>>() {
                }).block();
    }

    protected School getSchool(UUID schoolId, String accessToken) {
        return getWebClient().get()
                .uri(String.format(getConstants().getSchoolBySchoolIdUrl(), schoolId))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                })
                .retrieve()
                .bodyToMono(School.class)
                .block();
    }

    protected SchoolClob getSchoolClob(UUID schoolId, String accessToken) {
        return getWebClient().get()
                .uri(String.format(getConstants().getSchoolClobBySchoolIdUrl(), schoolId))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                })
                .retrieve()
                .bodyToMono(SchoolClob.class)
                .block();
    }

    protected District getDistrict(UUID districtId, String accessToken) {
        return getWebClient().get()
                .uri(String.format(getConstants().getDistrictByDistrictIdUrl(), districtId))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                })
                .retrieve()
                .bodyToMono(District.class)
                .block();
    }

}
