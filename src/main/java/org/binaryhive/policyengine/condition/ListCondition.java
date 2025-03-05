package org.binaryhive.policyengine.condition;

import java.util.List;
import lombok.Getter;
import org.binaryhive.policyengine.model.Attribute;
import org.binaryhive.policyengine.model.Operator;

public class ListCondition<T> implements Condition<T> {
  private final Attribute<T> attribute;
  private final Operator operator;
  @Getter private final List<T> values;

  public ListCondition(Attribute<T> attribute, Operator operator, List<T> values) {
    // Validate operator type
    if (operator != Operator.IN && operator != Operator.NOT_IN) {
      throw new IllegalArgumentException("ListCondition only supports IN or NOT_IN operators");
    }
    this.attribute = attribute;
    this.operator = operator;
    this.values = values;
  }

  @Override
  public boolean evaluate(T contextValue) {
    if (contextValue == null) {
      return operator == Operator.NOT_IN;
    }

    boolean contains;

    // If contextValue is a List, check for intersection
    if (contextValue instanceof List<?>) {
      List<?> contextList = (List<?>) contextValue;
      contains = contextList.stream().anyMatch(values::contains);
    } else {
      // Standard check for a single value
      contains = values.contains(contextValue);
    }

    return operator == Operator.IN ? contains : !contains;
  }

  @Override
  public Attribute<T> getAttribute() {
    return attribute;
  }

  @Override
  public Operator getOperator() {
    return operator;
  }
}
