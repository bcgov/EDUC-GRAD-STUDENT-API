package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationData;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecordDistribution;
import ca.bc.gov.educ.api.gradstudent.model.dto.ProjectedRunClob;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.*;


@Component
public class GraduationStatusTransformer {

    private static final Logger logger = LoggerFactory.getLogger(GraduationStatusTransformer.class);

    private static final String JSON_PARSING_ERROR = "Parsing Error: {}";

    @Autowired
    ModelMapper modelMapper;
    
    @Autowired
    GradValidation validation;

    public GraduationStudentRecord transformToDTOWithModifiedProgramCompletionDate(GraduationStudentRecordEntity gradStatusEntity) {
        GraduationStudentRecord gradStatus = modelMapper.map(gradStatusEntity, GraduationStudentRecord.class);
        gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(gradStatusEntity.getProgramCompletionDate() != null ?
                EducGradStudentApiUtils.formatDate(gradStatusEntity.getProgramCompletionDate()) : null));
        return gradStatus;
    }

    public GraduationStudentRecord transformToDTO(GraduationStudentRecordEntity gradStatusEntity) {
        GraduationStudentRecord gradStatus = modelMapper.map(gradStatusEntity, GraduationStudentRecord.class);
        gradStatus.setProgramCompletionDate(gradStatusEntity.getProgramCompletionDate() != null ?
                EducGradStudentApiUtils.formatDate(gradStatusEntity.getProgramCompletionDate()) : null);
        return gradStatus;
    }

    public GraduationStudentRecord transformToDTOWithModifiedProgramCompletionDate(Optional<GraduationStudentRecordEntity> gradStatusEntity ) {
        GraduationStudentRecordEntity cae = new GraduationStudentRecordEntity();
        if (gradStatusEntity.isPresent())
            cae = gradStatusEntity.get();

        GraduationStudentRecord gradStatus = modelMapper.map(cae, GraduationStudentRecord.class);
        gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(gradStatus.getProgramCompletionDate() != null ? gradStatus.getProgramCompletionDate():null));
        return gradStatus;
    }

    public List<GraduationStudentRecord> transformToDTOWithModifiedProgramCompletionDate(Iterable<GraduationStudentRecordEntity> gradStatusEntities ) {
        List<GraduationStudentRecord> gradStatusList = new ArrayList<>();
        for (GraduationStudentRecordEntity gradStatusEntity : gradStatusEntities) {
            GraduationStudentRecord gradStatus = modelMapper.map(gradStatusEntity, GraduationStudentRecord.class);
            gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(gradStatusEntity.getProgramCompletionDate() != null ? gradStatusEntity.getProgramCompletionDate().toString():null));
            gradStatusList.add(gradStatus);
        }
        return gradStatusList;
    }

    public List<GraduationStudentRecord> transformToDTORecalculate (Iterable<GraduationStudentRecordEntity> gradStatusEntities ) {
        List<GraduationStudentRecord> gradStatusList = new ArrayList<>();
        for (GraduationStudentRecordEntity gradStatusEntity : gradStatusEntities) {
            GraduationStudentRecord gradStatus = modelMapper.map(gradStatusEntity, GraduationStudentRecord.class);
            gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(gradStatusEntity.getProgramCompletionDate() != null ? gradStatusEntity.getProgramCompletionDate().toString():null));
            gradStatus.setStudentGradData(null);
            gradStatus.setStudentProjectedGradData(null);
            gradStatusList.add(gradStatus);
        }
        return gradStatusList;
    }

    public GraduationStudentRecordEntity transformToEntity(GraduationStudentRecord gradStatus) {
        GraduationStudentRecordEntity gradStatusEntity = modelMapper.map(gradStatus, GraduationStudentRecordEntity.class);
        Date programCompletionDate = null;
        try {
            if(gradStatus.getProgramCompletionDate() != null) {
                String pDate = gradStatus.getProgramCompletionDate();
                if(gradStatus.getProgramCompletionDate().length() <= 7) {
                    pDate = EducGradStudentApiUtils.parsingTraxDate(gradStatus.getProgramCompletionDate());
                }
                programCompletionDate= Date.valueOf(pDate);
            }
        }catch(Exception e) {
            validation.addErrorAndStop("Invalid Date");
        }
        gradStatusEntity.setProgramCompletionDate(programCompletionDate);
        return gradStatusEntity;
    }

    public GraduationStudentRecordDistribution tToDForDistribution(GraduationStudentRecordEntity gradStatusEntity) {
        GraduationStudentRecordDistribution distObj = new GraduationStudentRecordDistribution();
        GraduationStudentRecord ent = modelMapper.map(gradStatusEntity, GraduationStudentRecord.class);
        distObj.setProgram(ent.getProgram());
        distObj.setHonoursStanding(ent.getHonoursStanding());
        distObj.setSchoolOfRecord(ent.getSchoolOfRecord());
        distObj.setProgramCompletionDate(EducGradStudentApiUtils.parseDateFromString(gradStatusEntity.getProgramCompletionDate() != null ? gradStatusEntity.getProgramCompletionDate().toString():null));
        distObj.setStudentID(ent.getStudentID());
        distObj.setStudentCitizenship(ent.getStudentCitizenship());
        if(ent.getStudentGradData() != null) {
            GraduationData existingData = null;
            try {
                existingData = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(ent.getStudentGradData(), GraduationData.class);
            } catch (JsonProcessingException e) {
                logger.error(JSON_PARSING_ERROR, e.getMessage());
            }
            if(existingData != null) {
                distObj.setPen(existingData.getGradStudent().getPen());
                distObj.setLegalFirstName(existingData.getGradStudent().getLegalFirstName());
                distObj.setLegalMiddleNames(existingData.getGradStudent().getLegalMiddleNames());
                distObj.setLegalLastName(existingData.getGradStudent().getLegalLastName());
                distObj.setNonGradReasons(existingData.getNonGradReasons());
                distObj.setStudentGrade(existingData.getGradStudent().getStudentGrade());
            }
        }
        return distObj;
    }

    public List<GraduationStudentRecord> tToDForBatch(Iterable<GraduationStudentRecordEntity> gradStatusEntities) {
        List<GraduationStudentRecord> gradStatusList = new ArrayList<>();
        for (GraduationStudentRecordEntity gradStatusEntity : gradStatusEntities) {
            GraduationStudentRecord gradStatus = modelMapper.map(gradStatusEntity, GraduationStudentRecord.class);
            logger.debug("GraduationStudentRecordEntity {} with database program completion date {}", gradStatusEntity.getPen(), gradStatusEntity.getProgramCompletionDate());
            gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStatusEntity.getProgramCompletionDate(), "yyyy/MM"));
            logger.debug("GraduationStudentRecord {} with trax program completion date {}", gradStatus.getPen(), gradStatus.getProgramCompletionDate());
            populatePenAndLegalNamesAndNonGradReasons(gradStatus);
            gradStatus.setStudentCitizenship(gradStatusEntity.getStudentCitizenship());
            gradStatus.setStudentGradData(null);
            gradStatus.setCreateDate((gradStatusEntity.getCreateDate()));
            gradStatus.setUpdateDate((gradStatusEntity.getUpdateDate()));
            gradStatusList.add(gradStatus);
        }
        return gradStatusList;
    }

    public List<UUID> tToDForAmalgamation(Iterable<GraduationStudentRecordEntity> gradStatusEntities, String type) {
        List<GraduationStudentRecord> results = new ArrayList<>();
        for (GraduationStudentRecordEntity gradStatusEntity : gradStatusEntities) {
            GraduationStudentRecord gradStatus = modelMapper.map(gradStatusEntity, GraduationStudentRecord.class);
            gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(gradStatusEntity.getProgramCompletionDate() != null ? gradStatusEntity.getProgramCompletionDate().toString():null));
            populatePenAndLegalNamesAndNonGradReasons(gradStatus);
            if(gradStatus.getStudentProjectedGradData() != null) {
                try {
                    ProjectedRunClob existingData = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(gradStatus.getStudentProjectedGradData(), ProjectedRunClob.class);
                    if((!existingData.isGraduated() && type.equalsIgnoreCase("TVRNONGRAD")) || (existingData.isGraduated() && type.equalsIgnoreCase("TVRGRAD"))) {
                        results.add(gradStatus);
                    }
                } catch (JsonProcessingException e) {
                    logger.error(JSON_PARSING_ERROR ,e.getMessage());
                }
            }

        }
        if (results.isEmpty()) {
            return new ArrayList<>();
        }

        // sort by names
        results.sort(Comparator.comparing(GraduationStudentRecord::getLegalLastName, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(GraduationStudentRecord::getLegalFirstName, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(GraduationStudentRecord::getLegalMiddleNames, Comparator.nullsLast(Comparator.naturalOrder())));
        return results.stream().map(GraduationStudentRecord::getStudentID).toList();
    }

    private void populatePenAndLegalNamesAndNonGradReasons(GraduationStudentRecord gradStatus) {
        if(gradStatus.getStudentGradData() != null) {
            GraduationData existingData = null;
            try {
                existingData = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).readValue(gradStatus.getStudentGradData(), GraduationData.class);
            } catch (JsonProcessingException e) {
                logger.error(JSON_PARSING_ERROR, e.getMessage());
            }
            if (existingData != null) {
                gradStatus.setPen(existingData.getGradStudent().getPen());
                gradStatus.setLegalFirstName(existingData.getGradStudent().getLegalFirstName());
                gradStatus.setLegalMiddleNames(existingData.getGradStudent().getLegalMiddleNames());
                gradStatus.setLegalLastName(existingData.getGradStudent().getLegalLastName());
                gradStatus.setNonGradReasons(existingData.getNonGradReasons());
            }
        }
    }
}
