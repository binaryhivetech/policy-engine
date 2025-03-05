package org.binaryhive.policyengine.exception;

import lombok.experimental.StandardException;

@StandardException
public class PolicyEvaluationException extends RuntimeException {
  public PolicyEvaluationException(String message) {
    super(message);
  }
}
