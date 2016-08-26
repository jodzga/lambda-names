package com.linkedin.util.lambda;

import java.util.Optional;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;

class NameGenClassVisitor extends ClassVisitor {

  private static final Pattern LAMBDA_NAME_PATTERN = Pattern.compile("^.*\\$\\$Lambda\\$\\d+$");
  private String _inferredOperation;
  private Optional<SourcePointer> _sourcePointer;
  private String _className;

  public NameGenClassVisitor(int api) {
    super(api);
  }

  private boolean isALambdaClassByName(String name) {
    return LAMBDA_NAME_PATTERN.matcher(name).matches();
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces);
    _className = name.replace('/', '.');
    if (isALambdaClassByName(name)) {
      _sourcePointer = SourcePointer.get();
    }
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    return new NameGenMethodVisitor(api, super.visitMethod(access, name, desc, signature, exceptions),
        this::setInferredOperation, _sourcePointer);
  }

  private void setInferredOperation(String inferredOperation) {
    _inferredOperation = inferredOperation;
  }

  public LambdaName getLambdaName() {
    String sourcePointer = _sourcePointer.map(SourcePointer::toString).orElse(null);
    return new LambdaName(_className, _inferredOperation, sourcePointer);
  }
}
