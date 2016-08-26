package com.linkedin.util.lambda;

import java.util.Optional;

class LambdaName {

  private final String _inferredOperation;
  private final String _sourcePointer;
  private final String _className;

  public LambdaName(String className, String inferredOperation, String sourcePointer) {
    _className = className;
    _inferredOperation = inferredOperation;
    _sourcePointer = sourcePointer;
  }

  public Optional<String> getName() {
    if (_inferredOperation == null && _sourcePointer == null) {
      return Optional.empty();
    } else {
      StringBuilder nameBuilder = new StringBuilder();
      if (_inferredOperation != null) {
        nameBuilder.append(_inferredOperation);
        if (_sourcePointer != null) {
          nameBuilder.append(" ");
          nameBuilder.append(_sourcePointer);
        }
      } else {
        nameBuilder.append(_sourcePointer);
      }
      return Optional.of(nameBuilder.toString());
    }
  }

  public String getClassName() {
    return _className;
  }

  @Override
  public String toString() {
    return _className + " => " + getName().orElse("");
  }
}
