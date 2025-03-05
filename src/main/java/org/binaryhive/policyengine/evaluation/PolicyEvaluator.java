package org.binaryhive.policyengine.evaluation;

import java.util.*;
import lombok.extern.log4j.Log4j2;
import org.binaryhive.policyengine.condition.Condition;
import org.binaryhive.policyengine.model.Effect;
import org.binaryhive.policyengine.model.Policy;

@Log4j2
public final class PolicyEvaluator {
  public PolicyEvaluator() {}

  public PolicyEvaluationResult evaluate(Policy policy, Map<String, Object> context) {
    boolean allConditionsMet =
        policy.getConditions().stream()
            .allMatch(condition -> evaluateCondition(condition, context));
    if (allConditionsMet) {
      return policy.getEffect() == Effect.ALLOW
          ? PolicyEvaluationResult.ALLOW
          : PolicyEvaluationResult.DENY;
    } else {
      return PolicyEvaluationResult.NOT_APPLICABLE;
    }
  }

  private boolean evaluateCondition(Condition<?> condition, Map<String, Object> context) {
    var attributeName = condition.getAttribute().getName();
    var rawContextValue = context.get(attributeName);

    if (rawContextValue == null) {
      return evaluateWithNull(condition);
    }

    try {
      var convertedValue = condition.getAttribute().convert(rawContextValue);
      return evaluateTyped(condition, convertedValue);
    } catch (Exception e) {
      log.warn("Error evaluating condition: {}: {}", attributeName, e.getMessage());
      return false;
    }
  }

  @SuppressWarnings("unchecked")
  private <T> boolean evaluateTyped(Condition<?> condition, Object value) {
    Condition<T> typedCondition = (Condition<T>) condition;
    T typedValue = (T) value;
    return typedCondition.evaluate(typedValue);
  }

  private boolean evaluateWithNull(Condition<?> condition) {
    return condition.evaluate(null);
  }
}
