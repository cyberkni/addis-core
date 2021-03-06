package org.drugis.addis.trialverse.model;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by daan on 3/26/14.
 */
@Embeddable
public class MeasurementKey implements Serializable {
  @Column(name = "study")
  Long studyId;
  @Column(name = "variable")
  Long variableId;
  @Column(name = "measurement_moment")
  Long measurementMomentId;
  @Column(name = "arm")
  Long armId;
  @Column(name = "attribute")
  @Type(type = "org.drugis.addis.trialverse.model.MeasurementAttributeUserType")
  MeasurementAttribute measurementAttribute;

  public MeasurementKey() {
  }

  public MeasurementKey(Long studyId, Long variableId, Long measurementMomentId, Long armId, MeasurementAttribute measurementAttribute) {
    this.studyId = studyId;
    this.variableId = variableId;
    this.measurementMomentId = measurementMomentId;
    this.armId = armId;
    this.measurementAttribute = measurementAttribute;
  }

  public Long getStudyId() {
    return studyId;
  }

  public void setStudyId(Long studyId) {
    this.studyId = studyId;
  }

  public Long getVariableId() {
    return variableId;
  }

  public void setVariableId(Long variableId) {
    this.variableId = variableId;
  }

  public Long getMeasurementMomentId() {
    return measurementMomentId;
  }

  public void setMeasurementMomentId(Long measurementMomentId) {
    this.measurementMomentId = measurementMomentId;
  }

  public Long getArmId() {
    return armId;
  }

  public void setArmId(Long armId) {
    this.armId = armId;
  }

  public MeasurementAttribute getMeasurementAttribute() {
    return measurementAttribute;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MeasurementKey that = (MeasurementKey) o;

    if (!armId.equals(that.armId)) return false;
    if (measurementAttribute != that.measurementAttribute) return false;
    if (!measurementMomentId.equals(that.measurementMomentId)) return false;
    if (!studyId.equals(that.studyId)) return false;
    if (!variableId.equals(that.variableId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = studyId.hashCode();
    result = 31 * result + variableId.hashCode();
    result = 31 * result + measurementMomentId.hashCode();
    result = 31 * result + armId.hashCode();
    result = 31 * result + measurementAttribute.hashCode();
    return result;
  }
}
