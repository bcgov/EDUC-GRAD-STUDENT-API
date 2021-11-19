package ca.bc.gov.educ.api.gradstudent.model.dto;

public enum PenGradStudentStatusEnum {

    DEC("D"),
    CUR("A"),
    ARC("A"),
    MER("M"),
    TER("A");

    public final String label;

    private PenGradStudentStatusEnum(String label) {
        this.label = label;
    }
}
