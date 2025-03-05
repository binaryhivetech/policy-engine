package org.binaryhive.policyengine.condition;

import java.util.Objects;
import lombok.Getter;
import org.binaryhive.policyengine.model.Attribute;
import org.binaryhive.policyengine.model.Operator;

@Getter
public class ValueCondition<T> implements Condition<T> {
  private final Attribute<T> attribute;
  private final Operator operator;
  private final T value;

  public ValueCondition(Attribute<T> attribute, Operator operator, T value) {
    if (operator == Operator.IN || operator == Operator.NOT_IN) {
      throw new IllegalArgumentException("Use ListCondition for IN/NOT_IN operators");
    }
    this.attribute = attribute;
    this.operator = operator;
    this.value = value;
  }

  public boolean evaluate(T contextValue) {
    if (contextValue == null && value != null) {
      return operator == Operator.NOT_EQUALS;
    }

    return switch (operator) {
      case EQUALS -> Objects.equals(contextValue, value);
      case NOT_EQUALS -> !Objects.equals(contextValue, value);
      case GREATER_THAN -> {
        if (contextValue instanceof Comparable && value instanceof Comparable) {
          yield ((Comparable<T>) contextValue).compareTo(value) > 0;
        }
        throw new IllegalArgumentException("Cannot compare with GREATER_THAN");
      }
      case LESS_THAN -> {
        if (contextValue instanceof Comparable && value instanceof Comparable) {
          yield ((Comparable<T>) contextValue).compareTo(value) < 0;
        }
        throw new IllegalArgumentException("Cannot compare with LESS_THAN");
      }
      default -> throw new IllegalArgumentException("Use ListCondition for IN/NOT_IN operators");
    };
  }
}
