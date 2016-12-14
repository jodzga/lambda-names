package com.linkedin.util.lambda;

import static com.linkedin.util.lambda.Iterators.backwardIterator;
import static com.linkedin.util.lambda.Iterators.forwardIterator;
import static com.linkedin.util.lambda.Iterators.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

class MethodInvOwnerFinder {

  private static final int[] POTENTIAL_OP_PUSH_INSTRUCTIONS_ARR = new int[] {
      Opcodes.ALOAD,
      Opcodes.GETFIELD,
      Opcodes.GETSTATIC,
      Opcodes.INVOKEINTERFACE,
      Opcodes.INVOKESPECIAL,
      Opcodes.INVOKESTATIC,
      Opcodes.INVOKEVIRTUAL
  };

  private static final int[] INVOKE_INSTRUCTIONS_ARR = new int[] {
      Opcodes.INVOKEINTERFACE,
      Opcodes.INVOKESPECIAL,
      Opcodes.INVOKESTATIC,
      Opcodes.INVOKEVIRTUAL
  };

  private static final int[] RETURN_INSTRUCTIONS_ARR = new int[] {
      Opcodes.IRETURN,
      Opcodes.LRETURN,
      Opcodes.FRETURN,
      Opcodes.DRETURN,
      Opcodes.ARETURN,
      Opcodes.RETURN
  };

  private static final int[] GIVE_UP_OP_PUSH_INSTRUCTIONS_ARR = new int[] {
      Opcodes.SWAP
  };

  private static Set<Integer> toSet(int[] array) {
    Set<Integer> set = new HashSet<>();
    for(int x: array) {
      set.add(x);
    }
    return set;
  }

  private static final Set<Integer> POTENTIAL_OP_PUSH_INSTRUCTIONS = toSet(POTENTIAL_OP_PUSH_INSTRUCTIONS_ARR);
  private static final Set<Integer> GIVE_UP_OP_PUSH_INSTRUCTIONS = toSet(GIVE_UP_OP_PUSH_INSTRUCTIONS_ARR);
  private static final Set<Integer> RETURN_INSTRUCTIONS = toSet(RETURN_INSTRUCTIONS_ARR);
  private static final Set<Integer> INVOKE_INSTRUCTIONS = toSet(INVOKE_INSTRUCTIONS_ARR);

  private static Optional<AbstractInsnNode> findReturn(AbstractInsnNode instr) {
    return toStream(forwardIterator(instr))
        .filter(instruction -> RETURN_INSTRUCTIONS.contains(instruction.getOpcode()))
        .findFirst();
  }

  private static Optional<AbstractInsnNode> findInvoke(AbstractInsnNode instr) {
    return toStream(backwardIterator(instr))
    .filter(instruction -> INVOKE_INSTRUCTIONS.contains(instruction.getOpcode()))
    .findFirst();
  }

  private static Optional<AbstractInsnNode> findALoad(AbstractInsnNode instr, int argSize) {
    if (argSize == 0) {
      return Optional.of(instr);
    } else {
      return toStream(until(backwardIterator(instr.getPrevious()), ins ->
      GIVE_UP_OP_PUSH_INSTRUCTIONS.contains(ins.getOpcode())))
   .filter(ins -> POTENTIAL_OP_PUSH_INSTRUCTIONS.contains(ins.getOpcode()))
   .findFirst()
   .flatMap(ins -> findALoad(ins, argSize -1));
    }
  }

  private static Optional<String> toVariableName(AbstractInsnNode instr, List<LocalVariableNode> localVars) {
    if (instr instanceof VarInsnNode) {
      VarInsnNode varInstr = (VarInsnNode)instr;
      return Optional.of(localVars.get(varInstr.var).name);
    } else if (instr instanceof FieldInsnNode) {
      FieldInsnNode fieldInstr = (FieldInsnNode)instr;
      return Optional.of(fieldInstr.name);
    } else if (instr instanceof MethodInsnNode) {
      MethodInsnNode methodInstr = (MethodInsnNode)instr;
      if (methodInstr.getOpcode() == Opcodes.INVOKESPECIAL && methodInstr.name.equals("<init>")) {
        return Optional.of("new " + Util.extractSimpleName(methodInstr.owner, "/") + "()");
      }
      Type methodType = Type.getMethodType(methodInstr.desc);
      int retSize = methodType.getArgumentsAndReturnSizes() & 0x03;
      if (retSize > 0) {
        int argSize = methodType.getArgumentTypes().length;
        StringJoiner sj = new StringJoiner(",", "(", ")");
        for (int i = 0; i < argSize; i++) {
          sj.add("_");
        }
        return Optional.of(methodInstr.name + sj.toString());
      } else  {
        //things got too complicated, retreat
        return Optional.empty();
      }
    }
    else {
      return Optional.empty();
    }
  }

  static Optional<String> findOwner(InsnList instructions, List<LocalVariableNode> localVars, int argSize) {
          return findReturn(instructions.getFirst())
            .flatMap(MethodInvOwnerFinder::findInvoke)
            .flatMap(ins -> MethodInvOwnerFinder.findALoad(ins, argSize))
            .flatMap(instr -> toVariableName(instr, localVars));
  }

}
