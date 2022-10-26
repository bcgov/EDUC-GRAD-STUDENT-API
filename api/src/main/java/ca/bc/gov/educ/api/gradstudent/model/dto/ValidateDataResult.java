package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ValidateDataResult implements Serializable {

    private String recalculateGradStatus = "N";
    private String recalculateProgectedGrad = "N";

    public void recalculateAll() {
        this.recalculateGradStatus = "Y";
        this.recalculateProgectedGrad = "Y";
    }

    public boolean hasDataChanged() {
        return "Y".equalsIgnoreCase(this.recalculateGradStatus) || "Y".equalsIgnoreCase(this.recalculateProgectedGrad);
    }
}
