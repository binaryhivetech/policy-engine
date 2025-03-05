package org.binaryhive.policyengine.exception;

import lombok.experimental.StandardException;

@StandardException
public class AttributeConversionException extends RuntimeException {
  public AttributeConversionException(String message) {
    super(message);
  }
}
