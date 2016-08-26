package com.linkedin.util.lambda;

import java.io.IOException;
import java.io.PrintWriter;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.testng.annotations.Test;

public class LambdaByteCodeAnalyzerTest {

  static {
    LambdaNames.initialize();
  }

  int x = 0;

  @Test
  public void testAgentLoaded() {

    //create some Lambda
    Runnable r = () -> System.out.println();
    System.out.println(LambdaNames.getLambdaName(r.getClass().getName()));

    r = this::testAgentLoaded;
    System.out.println(LambdaNames.getLambdaName(r.getClass().getName()));

    r = LambdaByteCodeAnalyzerTest::hello;
    System.out.println(LambdaNames.getLambdaName(r.getClass().getName()));

    r = () -> closure(x);
    System.out.println(LambdaNames.getLambdaName(r.getClass().getName()));

    r = () -> closure2();
    System.out.println(LambdaNames.getLambdaName(r.getClass().getName()));

    r = this::closure2;
    System.out.println(LambdaNames.getLambdaName(r.getClass().getName()));

    try {
      ClassReader reader = new ClassReader("com.linkedin.util.lambda.LambdaByteCodeAnalyzerTest");
      reader.accept(new TraceClassVisitor(new PrintWriter(System.out)), 0);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void hello() {
  }

  private void closure(int x) {
  }

  private void closure2() {
    System.out.println(x);
  }

}
