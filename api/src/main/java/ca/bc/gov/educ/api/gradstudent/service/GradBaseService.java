package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.FieldName;
import ca.bc.gov.educ.api.gradstudent.constant.FieldType;
import ca.bc.gov.educ.api.gradstudent.model.dto.OngoingUpdateFieldDTO;
import ca.bc.gov.educ.api.gradstudent.model.dto.SchoolClob;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.District;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class GradBaseService {
    // Student Status
    public static final String STUDENT_STATUS_ARCHIVED = "ARC";
    public static final String STUDENT_STATUS_MERGED = "MER";
    public static final String STUDENT_STATUS_TERMINATED = "TER";

    // NULL Value
    public static final String NULL_VALUE = "NULL"; // NULL String => Nullify (set to NULL)


    protected abstract WebClient getWebClient();

    protected abstract EducGradStudentApiConstants getConstants();

    protected void validateStudentStatusAndResetBatchFlags(GraduationStudentRecordEntity gradEntity) {
        String studentStatus = gradEntity.getStudentStatus();
        // GRAD2-2922 & GRAD2-2950
        // 1. If a student in GRAD is MER then do not set Transcript & TVR flags  when their other data changes
        // 2. If a student in GRAD is changed to MER then set Transcript & TVR flags to NULL
        if (STUDENT_STATUS_MERGED.equalsIgnoreCase(studentStatus)) {
            gradEntity.setRecalculateGradStatus(null);
            gradEntity.setRecalculateProjectedGrad(null);
        }
    }

    protected void validateStudentStatusAndResetBatchFlags(GraduationStudentRecordEntity gradEntity, Map<FieldName, OngoingUpdateFieldDTO> updateFieldsMap) {
        String studentStatus = null;
        if (updateFieldsMap.containsKey(FieldName.STUDENT_STATUS)) {
            OngoingUpdateFieldDTO studentStatusFieldDTO = updateFieldsMap.get(FieldName.STUDENT_STATUS);
            studentStatus = getStringValue(studentStatusFieldDTO.getValue());
        }
        if (studentStatus == null) {
            studentStatus = gradEntity.getStudentStatus();
        }
        // GRAD2-2922 & GRAD2-2950
        // 1. If a student in GRAD is MER then do not set Transcript & TVR flags  when their other data changes
        // 2. If a student in GRAD is changed to MER then set Transcript & TVR flags to NULL
        if (STUDENT_STATUS_MERGED.equalsIgnoreCase(studentStatus)) {
            addUpdateFieldIntoMap(updateFieldsMap, FieldName.RECALC_GRAD_ALG, FieldType.STRING, NULL_VALUE);
            addUpdateFieldIntoMap(updateFieldsMap, FieldName.RECALC_TVR, FieldType.STRING, NULL_VALUE);
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
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve().bodyToMono(new ParameterizedTypeReference<List<School>>() {
                }).block();
    }

    protected List<School> getSchoolsBySchoolCategoryCodes(List<String> schoolCategoryCodes, String accessToken) {
        return getWebClient().get()
                .uri(String.format(getConstants().getSchoolsByCategoryCodeUrl(), String.join(",", schoolCategoryCodes)))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<School>>() {
                }).block();
    }

    protected School getSchool(UUID schoolId, String accessToken) {
        return getWebClient().get()
                .uri(String.format(getConstants().getSchoolBySchoolIdUrl(), schoolId))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(School.class)
                .block();
    }

    protected SchoolClob getSchoolClob(UUID schoolId, String accessToken) {
        return getWebClient().get()
                .uri(String.format(getConstants().getSchoolClobBySchoolIdUrl(), schoolId))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(SchoolClob.class)
                .block();
    }

    protected District getDistrict(UUID districtId, String accessToken) {
        return getWebClient().get()
                .uri(String.format(getConstants().getDistrictByDistrictIdUrl(), districtId))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(District.class)
                .block();
    }

    protected void addUpdateFieldIntoMap(Map<FieldName, OngoingUpdateFieldDTO> updateFieldsMap, FieldName fieldName, FieldType type, Object value) {
        OngoingUpdateFieldDTO newFieldDTO = OngoingUpdateFieldDTO.builder()
                .type(type).name(fieldName).value(value)
                .build();
        addUpdateFieldIntoMap(updateFieldsMap, newFieldDTO);
    }

    protected void addUpdateFieldIntoMap(Map<FieldName, OngoingUpdateFieldDTO> updateFieldsMap, OngoingUpdateFieldDTO updateFieldDTO) {
        if (updateFieldsMap.containsKey(updateFieldDTO.getName())) {
            OngoingUpdateFieldDTO updateField = updateFieldsMap.get(updateFieldDTO.getName());
            updateField.setValue(updateFieldDTO.getValue());
        } else {
            updateFieldsMap.put(updateFieldDTO.getName(), updateFieldDTO);
        }
    }

    protected String getStringValue(Object value) {
        if (value instanceof String str) {
            return NULL_VALUE.equalsIgnoreCase(str) ? null : str;
        }
        return null;
    }

    protected UUID getGuidValue(Object value) {
        String strGuid = getStringValue(value);
        return strGuid != null? UUID.fromString(strGuid) : null;
    }

    protected String getUsername() {
        String username = ThreadLocalStateUtil.getCurrentUser();
        if (StringUtils.isBlank(username)) {
            username = EducGradStudentApiConstants.DEFAULT_UPDATED_BY;
        }
        return username;
    }

}
