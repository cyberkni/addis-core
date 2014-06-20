package org.drugis.addis.analyses;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by daan on 10-6-14.
 */
@Entity
public class ArmExclusion implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  private Integer analysisId;

  private Long trialverseId;

  public ArmExclusion() {
  }

  public ArmExclusion(Integer analysisId, Long trialverseId) {
    this.analysisId = analysisId;
    this.trialverseId = trialverseId;
  }

  public Integer getId() {
    return id;
  }

  public Integer getAnalysisId() {
    return analysisId;
  }

  public Long getTrialverseId() {
    return trialverseId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ArmExclusion)) return false;

    ArmExclusion that = (ArmExclusion) o;

    if (!analysisId.equals(that.analysisId)) return false;
    if (id != null ? !id.equals(that.id) : that.id != null) return false;
    if (!trialverseId.equals(that.trialverseId)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + analysisId.hashCode();
    result = 31 * result + trialverseId.hashCode();
    return result;
  }
}