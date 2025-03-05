package org.binaryhive.policyengine.condition;

import org.binaryhive.policyengine.model.Attribute;
import org.binaryhive.policyengine.model.Operator;

public interface Condition<T> {
  boolean evaluate(T contextValue);

  Attribute<T> getAttribute();

  Operator getOperator();
}
