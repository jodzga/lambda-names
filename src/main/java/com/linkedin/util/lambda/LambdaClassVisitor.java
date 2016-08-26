package com.linkedin.util.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Visits Class containing generated lambda method.
 */
class LambdaClassVisitor extends ClassVisitor {

  private final String _methodName;
  private final String _methodDesc;
  private final Consumer<String> _inferredOperationConsumer;

  private String _lambdaName;

  public LambdaClassVisitor(int api, String name, String desc, Consumer<String> inferredOperationConsumer) {
    super(api);
    _methodName = name;
    _methodDesc = desc;
    _inferredOperationConsumer = inferredOperationConsumer;
  }

  public String getName() {
    return _lambdaName;
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    if (name.equals(_methodName) && desc.equals(_methodDesc)) {
      LambdaMethodVisitor visitor = new LambdaMethodVisitor(api, mv);
      visitor._state = State.WITH_HIDDEN_ANNOTATION;
      return visitor;
    } else {
      return mv;
    }
  }

  private class LambdaMethodVisitor extends NameGenMethodVisitor {

    private List<String> _localVariables = new ArrayList<>();

    public LambdaMethodVisitor(int api, MethodVisitor mv) {
      super(api, mv, _inferredOperationConsumer, Optional.empty());
    }

    @Override
    public void visitEnd() {
      switch (_state) {
        case METHOD_REF:
          _lambdaName = lambdaLHS() + " -> " + _name;
          break;
        default:
          _lambdaName = lambdaLHS() + " -> {...}";
          break;
      }
      super.visitEnd();
    }

    private String lambdaLHS() {
      int numOfParameters = Type.getArgumentTypes(_methodDesc).length;
      if (numOfParameters == 1) {
        return _localVariables.get(0);
      } else {
        StringJoiner sj = new StringJoiner(", ", "(", ")");
        for (int i = 0; i < numOfParameters; i++) {
          sj.add(_localVariables.get(i));
        }
        return sj.toString();
      }
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
      if (!name.equals("this")) {
        _localVariables.add(name);
      }
    }

  }

}