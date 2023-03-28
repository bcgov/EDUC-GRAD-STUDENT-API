package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.CertificateType;
import ca.bc.gov.educ.api.gradstudent.model.dto.NonGradReason;
import ca.bc.gov.educ.api.gradstudent.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Component
public class ReportGradStudentTransformer {

    @Autowired
    ModelMapper modelMapper;

    public ReportGradStudentData transformToDTO (ReportGradStudentDataEntity entity) {
    	return modelMapper.map(entity, ReportGradStudentData.class);
    }

    public ReportGradStudentData transformToDTO ( Optional<ReportGradStudentDataEntity> studentStatusEntity ) {
        ReportGradStudentDataEntity entity = new ReportGradStudentDataEntity();
        if (studentStatusEntity.isPresent())
            entity = studentStatusEntity.get();

        return modelMapper.map(entity, ReportGradStudentData.class);
    }

	@SneakyThrows
    public List<ReportGradStudentData> transformToDTO (Iterable<ReportGradStudentDataEntity> studentStatusEntities ) {
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TypeFactory typeFactory = objectMapper.getTypeFactory();
		List<ReportGradStudentData> result = new ArrayList<>();
        for (ReportGradStudentDataEntity entity : studentStatusEntities) {
            ReportGradStudentData data = modelMapper.map(entity, ReportGradStudentData.class);
            if(StringUtils.isNotBlank(entity.getCertificateTypeCodes())) {
                List<CertificateType> types = objectMapper.readValue(entity.getCertificateTypeCodes(), typeFactory.constructCollectionType(List.class, CertificateType.class));
                if(!types.isEmpty()) {
                    data.setCertificateTypes(types);
                }
            }
            if(StringUtils.isNotBlank(entity.getNonGradReasons())) {
                List<NonGradReason> reasons = objectMapper.readValue(entity.getNonGradReasons(), typeFactory.constructCollectionType(List.class, NonGradReason.class));
                if(!reasons.isEmpty()) {
                    data.setNonGradReasons(reasons);
                }
            }
            result.add(data);
        }
        return result;
    }

}
