package com.linkedin.util.lambda;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.function.Consumer;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.util.TraceClassVisitor;

class NameGenMethodVisitor extends MethodVisitor {

  private final Consumer<String> _inferredOperationConsumer;
  protected final Optional<SourcePointer> _sourcePointer;

  protected State _state = State.INIT;
  protected String _name;
  protected String _owner;
  protected String _desc;
  protected int _opcode;

  public NameGenMethodVisitor(int api, MethodVisitor mv, Consumer<String> inferredOperationConsumer,
      Optional<SourcePointer> sourcePointer) {
    super(api, mv);
    _inferredOperationConsumer = inferredOperationConsumer;
    _sourcePointer = sourcePointer;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    if (desc.equals("Ljava/lang/invoke/LambdaForm$Hidden;")) {
      if (_state == State.INIT) {
        _state = State.WITH_HIDDEN_ANNOTATION;
      }
    }
    return super.visitAnnotation(desc, visible);
  }

  @Override
  public AnnotationVisitor visitAnnotationDefault() {
    _state = State.UNKNOWN;
    return super.visitAnnotationDefault();
  }

  protected Optional<String> findOwner(SourcePointer sp) {
    MethodRefOwnerFinder ownerFinder = new MethodRefOwnerFinder(api, sp._methodName, sp._lineNumber);
    try {
      ClassReader cr = new ClassReader(sp._className.replace('/', '.'));
//      cr.accept(new TraceClassVisitor(ownerFinder, new PrintWriter(System.out)), 0);
      cr.accept(ownerFinder, 0);
      return ownerFinder.getInferredOwner();
    } catch (IOException e) {
      return Optional.empty();
    }
  }

  @Override
  public void visitEnd() {
    switch (_state) {
      case METHOD_REF:
        if (_opcode == Opcodes.INVOKESTATIC) {
          _inferredOperationConsumer.accept(Util.extractSimpleName(_owner, "/") + "::" + _name);
        } else {
          Optional<String> fullName = _sourcePointer.flatMap(this::findOwner)
              .map(ownr -> ownr + "::" + _name);
          _inferredOperationConsumer.accept(fullName.orElse(_name));
        }
        break;
      case BLOCK:
        //analyze class that contains synthetic method created by lambda expression
        LambdaClassVisitor cv = new LambdaClassVisitor(api, _name, _desc, _inferredOperationConsumer);
        try {
          ClassReader cr = new ClassReader(_owner.replace('/', '.'));
//          cr.accept(new TraceClassVisitor(cv, new PrintWriter(System.out)), 0);
          cr.accept(cv, 0);
          _inferredOperationConsumer.accept(cv.getName());
        } catch (IOException e) {
        }
      default:
        break;
    }
    super.visitEnd();
  }

  @Override
  public void visitVarInsn(int opcode, int var) {
    if (opcode == Opcodes.ALOAD) {
      if (_state == State.WITH_HIDDEN_ANNOTATION) {
        _state = State.FIRST_LOADS_AND_GETFIELDS;
      }
    } else {
      _state = State.UNKNOWN;
    }
    super.visitVarInsn(opcode, var);
  }

  @Override
  public void visitInsn(int opcode) {
    if (opcode != Opcodes.IRETURN &&
        opcode != Opcodes.LRETURN &&
        opcode != Opcodes.FRETURN &&
        opcode != Opcodes.DRETURN &&
        opcode != Opcodes.ARETURN &&
        opcode != Opcodes.RETURN) {
      _state = State.UNKNOWN;
    }
    super.visitInsn(opcode);
  }

  @Override
  public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    if (_state == State.FIRST_LOADS_AND_GETFIELDS ||
        _state == State.WITH_HIDDEN_ANNOTATION) {

      switch (opcode) {
        case Opcodes.INVOKEVIRTUAL:
          handleMethodInvoke(owner, name, desc, opcode);
          break;
        case Opcodes.INVOKESPECIAL:
          handleMethodInvoke(owner, name, desc, opcode);
          break;
        case Opcodes.INVOKESTATIC:
          handleMethodInvoke(owner, name, desc, opcode);
          break;
        default:
          _state = State.UNKNOWN;
          break;
      }
    } else {
      _state = State.UNKNOWN;
    }
    super.visitMethodInsn(opcode, owner, name, desc, itf);
  }

  private void handleMethodInvoke(String owner, String name, String desc, int opcode) {
    _name = name;
    _owner = owner;
    _desc = desc;
    _opcode = opcode;
    if (name.startsWith("lambda$")) {
      _state = State.BLOCK;
    } else {
      _state = State.METHOD_REF;
    }
  }

  @Override
  public void visitFieldInsn(int opcode, String owner, String name, String desc) {
    if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
      if (_state == State.WITH_HIDDEN_ANNOTATION) {
        _state = State.FIRST_LOADS_AND_GETFIELDS;
      }
    } else {
      _state = State.UNKNOWN;
    }
    super.visitFieldInsn(opcode, owner, name, desc);
  }

  @Override
  public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
    _state = State.UNKNOWN;
    super.visitFrame(type, nLocal, local, nStack, stack);
  }

  @Override
  public void visitIincInsn(int var, int increment) {
    _state = State.UNKNOWN;
    super.visitIincInsn(var, increment);
  }

  @Override
  public void visitIntInsn(int opcode, int operand) {
    _state = State.UNKNOWN;
    super.visitIntInsn(opcode, operand);
  }

  @Override
  public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
    _state = State.UNKNOWN;
    super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
  }

  @Override
  public void visitJumpInsn(int opcode, Label label) {
    _state = State.UNKNOWN;
    super.visitJumpInsn(opcode, label);
  }

  @Override
  public void visitLdcInsn(Object cst) {
    _state = State.UNKNOWN;
    super.visitLdcInsn(cst);
  }

  @Override
  public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
    _state = State.UNKNOWN;
    super.visitLocalVariable(name, desc, signature, start, end, index);
  }

  @Override
  public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    _state = State.UNKNOWN;
    super.visitLookupSwitchInsn(dflt, keys, labels);
  }

  @Override
  public void visitMultiANewArrayInsn(String desc, int dims) {
    _state = State.UNKNOWN;
    super.visitMultiANewArrayInsn(desc, dims);
  }

  @Override
  public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
    _state = State.UNKNOWN;
    super.visitTableSwitchInsn(min, max, dflt, labels);
  }

  @Override
  public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
    _state = State.UNKNOWN;
    return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible);
  }

  @Override
  public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    _state = State.UNKNOWN;
    super.visitTryCatchBlock(start, end, handler, type);
  }
}