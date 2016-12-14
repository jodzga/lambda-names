package com.linkedin.util.lambda;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Function;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * Given method name and line number this class visitor tries to retrieve name of
 * the owner of the method reference.
 */
class MethodRefOwnerFinder extends ClassVisitor {

  private final String _name;
  private final int _lineNr;

  private String _inferredOwner;

  public MethodRefOwnerFinder(int api, String name, int lineNr) {
    super(api);
    _name = name;
    _lineNr = lineNr;
  }

  public Optional<String> getInferredOwner() {
    return Optional.ofNullable(_inferredOwner);
  }

  private static Iterator<AbstractInsnNode> forwardIterator(AbstractInsnNode instruction) {
    return iterator(instruction, AbstractInsnNode::getNext);
  }

  private static Iterator<AbstractInsnNode> backwardIterator(AbstractInsnNode instruction) {
    return iterator(instruction, AbstractInsnNode::getPrevious);
  }

  private static Iterator<AbstractInsnNode> iterator(AbstractInsnNode instruction,
      Function<AbstractInsnNode, AbstractInsnNode> move) {
    return new Iterator<AbstractInsnNode>() {

      private AbstractInsnNode _current = null;
      private AbstractInsnNode _next = instruction;

      @Override
      public boolean hasNext() {
        if (_next != null) {
          return true;
        } else {
          _next = move.apply(_current);
          return (_next != null);
        }
      }

      @Override
      public AbstractInsnNode next() {
        if (hasNext()) {
          _current = _next;
          _next = null;
          return _current;
        } else {
          throw new NoSuchElementException();
        }
      }
    };
  }

  private static Optional<AbstractInsnNode> findLineNumber(AbstractInsnNode instr, int lineNr) {
    return Iterators.toStream(forwardIterator(instr))
        .filter(instruction -> (instruction instanceof LineNumberNode) && ((LineNumberNode)instruction).line == lineNr)
        .findFirst();
  }

  private static final int[] POTENTIAL_OP_PUSH_INSTRUCTIONS_ARR = new int[] {
      Opcodes.ALOAD,
      Opcodes.GETFIELD,
      Opcodes.GETSTATIC,
      Opcodes.INVOKEINTERFACE,
      Opcodes.INVOKESPECIAL,
      Opcodes.INVOKESTATIC,
      Opcodes.INVOKEVIRTUAL
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

  private static Optional<AbstractInsnNode> findInvokeDynamic(AbstractInsnNode instr) {
    return Iterators.toStream(Iterators.until(forwardIterator(instr.getNext()), ins -> (ins instanceof LineNumberNode)))
    .filter(instruction -> instruction.getOpcode() == Opcodes.INVOKEDYNAMIC)
    .findFirst();
  }

  private static Optional<AbstractInsnNode> findALoad(AbstractInsnNode instr) {
    return Iterators.toStream(Iterators.until(backwardIterator(instr), ins ->
      (ins instanceof LineNumberNode) || GIVE_UP_OP_PUSH_INSTRUCTIONS.contains(ins.getOpcode())))
    .filter(ins -> POTENTIAL_OP_PUSH_INSTRUCTIONS.contains(ins.getOpcode()))
    .findFirst();
  }

  private static Optional<String> toVariableName(AbstractInsnNode instr, MethodNode mnode) {
    if (instr instanceof VarInsnNode) {
      VarInsnNode varInstr = (VarInsnNode)instr;
      @SuppressWarnings("unchecked")
      List<LocalVariableNode> vars = mnode.localVariables;
      return Optional.of(vars.get(varInstr.var).name);
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

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    if (name.equals(_name)) {
      return new MethodNode(api, access, name, desc, signature, exceptions) {
        @Override
        public void visitEnd() {
          findLineNumber(instructions.getFirst(), _lineNr)
            .flatMap(MethodRefOwnerFinder::findInvokeDynamic)
            .flatMap(MethodRefOwnerFinder::findALoad)
            .flatMap(instr -> toVariableName(instr, this))
            .ifPresent(name -> {
              _inferredOwner = name;
            });
        }
      };
    } else {
      return super.visitMethod(access, name, desc, signature, exceptions);
    }
  }

}
