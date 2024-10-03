package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;

public abstract class GradBaseService {
    // Student Status
    public static final String STUDENT_STATUS_ARCHIVED = "ARC";
    public static final String STUDENT_STATUS_MERGED = "MER";
    public static final String STUDENT_STATUS_TERMINATED = "TER";

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
}
