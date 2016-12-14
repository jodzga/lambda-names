package com.linkedin.util.lambda;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

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
      return new MethodNode(api, access, name, desc, signature, exceptions) {
        @SuppressWarnings("unchecked")
        @Override
        public void visitEnd() {
          visitor._instructions = instructions;
          visitor._localVars = localVariables;
          accept(visitor);
        }
      };
    } else {
      return mv;
    }
  }

  //visits synthetic method created by a lambda expression
  private class LambdaMethodVisitor extends NameGenMethodVisitor {

    private List<String> _localVariables = new ArrayList<>();
    private InsnList _instructions;
    private List<LocalVariableNode> _localVars;

    public LambdaMethodVisitor(int api, MethodVisitor mv) {
      super(api, mv, _inferredOperationConsumer, Optional.empty());
    }

    @Override
    public void visitEnd() {
      switch (_state) {
        case METHOD_REF:
          if (_opcode == Opcodes.INVOKESTATIC) {
            _lambdaName = lambdaLHS(_methodDesc) + " -> " + Util.extractSimpleName(_owner, "/")  + "." + methodInvName();
          } else if (_opcode == Opcodes.INVOKEVIRTUAL) {
            String methodInvName = methodInvName();
            //TODO lhs static vs dynamic - parameters
            //find owners depending on number of parameters

            _lambdaName = lambdaLHS(_desc) + " -> " +
                MethodInvOwnerFinder.findOwner(_instructions, _localVars,
                    Type.getMethodType(_desc).getArgumentTypes().length + 1)
                  .map(owner -> owner + "." + methodInvName)
                  .orElse(methodInvName);
          } else {
            //TODO
            _lambdaName = lambdaLHS(_methodDesc) + " -> {...}";
          }
          break;
        default:
          //TODO
          _lambdaName = lambdaLHS(_methodDesc) + " -> {...}";
          break;
      }
      super.visitEnd();
    }

    private String methodInvName() {
      Type methodType = Type.getMethodType(_desc);
      int argSize = methodType.getArgumentTypes().length;
      StringJoiner sj = new StringJoiner(",", "(", ")");
      for (int i = 0; i < argSize; i++) {
        sj.add("_");
      }
      return _name + sj.toString();
    }

    private String lambdaLHS(String desc) {
      int numOfParameters = Type.getArgumentTypes(desc).length;
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