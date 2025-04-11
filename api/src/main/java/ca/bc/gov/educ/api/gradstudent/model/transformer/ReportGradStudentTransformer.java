package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.CertificateType;
import ca.bc.gov.educ.api.gradstudent.model.dto.NonGradReason;
import ca.bc.gov.educ.api.gradstudent.model.dto.ReportGradStudentData;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.util.JsonTransformer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Component
public class ReportGradStudentTransformer {

    private static final Logger logger = LoggerFactory.getLogger(ReportGradStudentTransformer.class);

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    JsonTransformer jsonTransformer;

    public ReportGradStudentData transformToDTO (ReportGradStudentDataEntity entity) {
    	return modelMapper.map(entity, ReportGradStudentData.class);
    }

    public ReportGradStudentData transformToDTO ( Optional<ReportGradStudentDataEntity> studentStatusEntity ) {
        ReportGradStudentDataEntity entity = new ReportGradStudentDataEntity();
        if (studentStatusEntity.isPresent())
            entity = studentStatusEntity.get();

        return modelMapper.map(entity, ReportGradStudentData.class);
    }

    public List<ReportGradStudentData> transformToDTO (Iterable<ReportGradStudentDataEntity> studentStatusEntities ) {
        TypeFactory typeFactory = jsonTransformer.getTypeFactory();
		List<ReportGradStudentData> result = new ArrayList<>();
        for (ReportGradStudentDataEntity entity : studentStatusEntities) {
            ReportGradStudentData data = modelMapper.map(entity, ReportGradStudentData.class);
            try {
                if(StringUtils.isNotBlank(entity.getDistrictId())) {
                    data.setDistrictId(UUID.fromString(entity.getDistrictId()));
                }
            } catch (IllegalArgumentException e) {
                logger.warn("Invalid UUID for district ID: {} for student with ID {}", entity.getDistrictId(), entity.getGraduationStudentRecordId());
            }
            if (StringUtils.isNotBlank(entity.getCertificateTypeCodes())) {
                List<CertificateType> types = (List<CertificateType>)jsonTransformer.unmarshall(entity.getCertificateTypeCodes(), typeFactory.constructCollectionType(List.class, CertificateType.class));
                if (!types.isEmpty()) {
                    data.setCertificateTypes(types);
                }
            }
            if (StringUtils.isNotBlank(entity.getNonGradReasons())) {
                List<NonGradReason> reasons = (List<NonGradReason>)jsonTransformer.unmarshall(entity.getNonGradReasons(), typeFactory.constructCollectionType(List.class, NonGradReason.class));
                if (!reasons.isEmpty()) {
                    data.setNonGradReasons(reasons);
                }
            }
            result.add(data);
        }
        return result;
    }

}
