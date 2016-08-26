package com.linkedin.util.lambda;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class LambdaNamesAgent {

  private static final Logger LOGGER = LoggerFactory.getLogger(LambdaNamesAgent.class);
  private static final AtomicBoolean _initialized = new AtomicBoolean(false);

  public static void agentmain(String agentArgs, Instrumentation instrumentation) {
    if (_initialized.compareAndSet(false, true)) {
      LOGGER.info("Loading " + LambdaNamesAgent.class.getName());
      instrumentation.addTransformer(new Analyzer());
    }
  }

  private static class Analyzer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
        ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
      if (className == null && loader != null) {
        //TODO verify this logic in various settings
        analyze(classfileBuffer);
      }
      return classfileBuffer;
    }

    private void analyze(byte[] byteCode) {
      ClassReader reader = new ClassReader(byteCode);
      NameGenClassVisitor cv = new NameGenClassVisitor(Opcodes.ASM5);
//      reader.accept(new TraceClassVisitor(cv, new PrintWriter(System.out)), 0);
      reader.accept(cv, 0);
      LambdaName lambdaName = cv.getLambdaName();
      lambdaName.getName().ifPresent(name -> LambdaNames.add(lambdaName.getClassName(), name));
    }
  }

}
