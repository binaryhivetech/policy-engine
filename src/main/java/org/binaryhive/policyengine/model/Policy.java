package org.binaryhive.policyengine.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.binaryhive.policyengine.condition.Condition;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Policy {
  private String policyId;
  private String name;
  private String description;
  private List<Condition<?>> conditions;
  private Effect effect;
  
  /**
   * Constructor with essential fields.
   * 
   * @param policyId unique identifier for this policy
   * @param name display name for this policy
   * @param conditions list of conditions that must be satisfied
   * @param effect the effect to apply if conditions are met
   */
  public Policy(String policyId, String name, List<Condition<?>> conditions, Effect effect) {
    this.policyId = policyId;
    this.name = name;
    this.conditions = conditions;
    this.effect = effect;
  }
  
  /**
   * Returns the name if available, otherwise the policyId.
   * 
   * @return a human-readable identifier for this policy
   */
  public String getName() {
    return name != null && !name.isEmpty() ? name : policyId;
  }
}
