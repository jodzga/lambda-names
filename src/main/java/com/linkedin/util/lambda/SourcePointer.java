package com.linkedin.util.lambda;

import java.util.Arrays;
import java.util.Optional;

class SourcePointer {

  final String _className;
  final String _methodName;
  final int _lineNumber;

  private SourcePointer(String className, String methodName, Integer lineNumber) {
    _className = className;
    _methodName = methodName;
    _lineNumber = lineNumber;
  }

  static Optional<SourcePointer> get() {
    return Arrays.stream(new Exception().getStackTrace()).filter(SourcePointer::notLambdaStuff).findFirst()
        .map(SourcePointer::sourcePointer);
  }

  private static boolean notLambdaStuff(StackTraceElement element) {
    return !(element.getClassName().startsWith("java.") || element.getClassName().startsWith("sun.")
        || element.getClassName().startsWith(NameGenClassVisitor.class.getName())
        || element.getClassName().startsWith(LambdaNamesAgent.class.getName())
        || element.getClassName().startsWith(SourcePointer.class.getName())
        || element.getClassName().startsWith("org.objectweb.asm."));
  }

  private static SourcePointer sourcePointer(StackTraceElement stackTraceElement) {
    return new SourcePointer(stackTraceElement.getClassName(), stackTraceElement.getMethodName(),
        stackTraceElement.getLineNumber());
  }

  @Override
  public String toString() {
    return _methodName + "(" + Util.extractSimpleName(_className, ".") + (_lineNumber > 0 ? ":" + _lineNumber : "")
        + ")";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_className == null) ? 0 : _className.hashCode());
    result = prime * result + _lineNumber;
    result = prime * result + ((_methodName == null) ? 0 : _methodName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SourcePointer other = (SourcePointer) obj;
    if (_className == null) {
      if (other._className != null)
        return false;
    } else if (!_className.equals(other._className))
      return false;
    if (_lineNumber != other._lineNumber)
      return false;
    if (_methodName == null) {
      if (other._methodName != null)
        return false;
    } else if (!_methodName.equals(other._methodName))
      return false;
    return true;
  }


}
