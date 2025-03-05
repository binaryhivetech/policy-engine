package org.binaryhive.policyengine.evaluation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.binaryhive.policyengine.model.Policy;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Getter
public abstract class EvaluationContext {

  private final String action;
  private final Map<String, String> context;

  abstract List<Policy> policies();
}
