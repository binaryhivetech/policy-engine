package org.binaryhive.policyengine.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.binaryhive.policyengine.condition.Condition;

@Getter
@Setter
public class Policy {
  private String policyId;
  private String description;
  private List<Condition<?>> conditions;
  private Effect effect;
}
