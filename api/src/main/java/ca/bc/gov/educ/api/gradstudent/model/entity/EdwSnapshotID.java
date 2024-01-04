package ca.bc.gov.educ.api.gradstudent.model.entity;

import java.io.Serializable;
import java.util.Objects;

public class EdwSnapshotID implements Serializable {
    private Long gradYear;
    private String pen;

    public EdwSnapshotID() {

    }
    public EdwSnapshotID(Long gradYear, String pen) {
        this.gradYear = gradYear;
        this.pen = pen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdwSnapshotID that = (EdwSnapshotID) o;
        return Objects.equals(gradYear, that.gradYear) && Objects.equals(pen, that.pen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gradYear, pen);
    }
}
