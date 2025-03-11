package org.binaryhive.policyengine.evaluation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.binaryhive.policyengine.model.Policy;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public abstract class EvaluationContext {

  protected final String action;
  protected final Map<String, Object> context;

  abstract List<Policy> policies();
}
