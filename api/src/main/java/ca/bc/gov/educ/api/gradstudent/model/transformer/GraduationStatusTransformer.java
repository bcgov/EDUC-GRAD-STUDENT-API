package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordView;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.repository.ReportGradStudentDataRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.JsonTransformer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class GraduationStatusTransformer {

    private static final Logger logger = LoggerFactory.getLogger(GraduationStatusTransformer.class);

    private static final String JSON_PARSING_ERROR = "Parsing Error: {}";

    @Autowired
    ModelMapper modelMapper;
    
    @Autowired
    GradValidation validation;

    @Autowired
    JsonTransformer jsonTransformer;

    @Autowired
    ReportGradStudentDataRepository reportGradStudentDataRepository;

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
            GraduationData existingData = (GraduationData)jsonTransformer.unmarshall(ent.getStudentGradData(), GraduationData.class);
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

    public List<GraduationStudentRecord> tToDForBatchView(List<GraduationStudentRecordView> gradStatusEntities) {
        List<GraduationStudentRecord> gradStatusList = new ArrayList<>();
        Map<UUID, ReportGradStudentDataEntity> reportGradStudentDataMap = convertGraduationStudentRecordViewToReportGradStudentDataMap(gradStatusEntities);
        for (GraduationStudentRecordView gradStatusEntity : gradStatusEntities) {
            GraduationStudentRecord gradStatus = modelMapper.map(gradStatusEntity, GraduationStudentRecord.class);
            gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.formatDate(gradStatusEntity.getProgramCompletionDate(), "yyyy/MM"));
            populatePenAndLegalNamesAndNonGradReasons(gradStatus, reportGradStudentDataMap);
            logger.debug("GraduationStudentRecord {} with trax program completion date {}", gradStatus.getPen(), gradStatus.getProgramCompletionDate());
            gradStatus.setStudentCitizenship(gradStatusEntity.getStudentCitizenship());
            gradStatus.setStudentGradData(null);
            gradStatus.setCreateDate((gradStatusEntity.getCreateDate()));
            gradStatus.setUpdateDate((gradStatusEntity.getUpdateDate()));
            gradStatusList.add(gradStatus);
        }
        return gradStatusList;
    }

    public List<UUID> tToDForAmalgamation(List<GraduationStudentRecordView> gradStatusEntities, String type) {
        List<GraduationStudentRecord> results = new ArrayList<>();
        Map<UUID, ReportGradStudentDataEntity> reportGradStudentDataMap = convertGraduationStudentRecordViewToReportGradStudentDataMap(gradStatusEntities);
        for (GraduationStudentRecordView gradStatusEntity : gradStatusEntities) {
            GraduationStudentRecord gradStatus = modelMapper.map(gradStatusEntity, GraduationStudentRecord.class);
            gradStatus.setProgramCompletionDate(EducGradStudentApiUtils.parseTraxDate(gradStatusEntity.getProgramCompletionDate() != null ? gradStatusEntity.getProgramCompletionDate().toString():null));
            populatePenAndLegalNamesAndNonGradReasons(gradStatus, reportGradStudentDataMap);
            if(gradStatus.getStudentProjectedGradData() != null) {
                ProjectedRunClob existingData = (ProjectedRunClob)jsonTransformer.unmarshall(gradStatus.getStudentProjectedGradData(), ProjectedRunClob.class);
                if((!existingData.isGraduated() && type.equalsIgnoreCase("TVRNONGRAD")) || (existingData.isGraduated() && type.equalsIgnoreCase("TVRGRAD"))) {
                    results.add(gradStatus);
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

    private Map<UUID, ReportGradStudentDataEntity> convertGraduationStudentRecordViewToReportGradStudentDataMap(List<GraduationStudentRecordView> gradStatusEntities) {
        List<UUID> uuids = gradStatusEntities.stream().map(GraduationStudentRecordView::getStudentID).toList();
        List<ReportGradStudentDataEntity> reportGradStudentData = reportGradStudentDataRepository.findReportGradStudentDataEntityByGraduationStudentRecordIdInOrderByMincodeAscSchoolNameAscLastNameAsc(uuids);
        return reportGradStudentData.stream().collect(Collectors.toMap(ReportGradStudentDataEntity::getGraduationStudentRecordId, Function.identity()));
    }

    private void populatePenAndLegalNamesAndNonGradReasons(GraduationStudentRecord gradStatus, Map<UUID, ReportGradStudentDataEntity> reportGradStudentDataMap) {
        ReportGradStudentDataEntity existingData = reportGradStudentDataMap.get(gradStatus.getStudentID());
        if (existingData != null) {
            gradStatus.setPen(existingData.getPen());
            gradStatus.setLegalFirstName(existingData.getFirstName());
            gradStatus.setLegalMiddleNames(existingData.getMiddleName());
            gradStatus.setLegalLastName(existingData.getLastName());
            if (StringUtils.isNotBlank(existingData.getNonGradReasons())) {
                TypeFactory typeFactory = jsonTransformer.getTypeFactory();
                List<GradRequirement> nonGradReasons = (List<GradRequirement>) jsonTransformer.unmarshall(existingData.getNonGradReasons(), typeFactory.constructCollectionType(List.class, GradRequirement.class));
                gradStatus.setNonGradReasons(nonGradReasons);
            }
        }
    }
}
