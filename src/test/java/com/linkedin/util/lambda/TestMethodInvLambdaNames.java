package com.linkedin.util.lambda;

import static org.testng.Assert.assertTrue;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

public class TestMethodInvLambdaNames {

  static {
    LambdaNames.initialize();
  }

  static TestMethodInvLambdaNames staticField = new TestMethodInvLambdaNames();
  TestMethodInvLambdaNames field = this;

  private TestMethodInvLambdaNames noParamMethod() {
    return this;
  }

  private static TestMethodInvLambdaNames noParamStaticMethod() {
    return staticField;
  }


  private TestMethodInvLambdaNames paramMethod(long x, String y) {
    return this;
  }

  private static TestMethodInvLambdaNames paramStaticMethod(long x, String y) {
    return staticField;
  }

  String virtualFunction(String s) {
    return s;
  }

  String virtualFunction2(String s, String z) {
    return s;
  }

  String virtualCallable() {
    return "";
  }

  void virtualConsumer(String s) {
  }


  static String staticFunction(String s) {
    return s;
  }

  static String staticCallable() {
    return "";
  }

  static void staticConsumer(String s) {
  }


  private Optional<String> getLambdaNameForFunction(Function<String, String> f) {
    return LambdaNames.getLambdaName(f);
  }

  private Optional<String> getLambdaNameForCallable(Callable<String> c) {
    return LambdaNames.getLambdaName(c);
  }

  private Optional<String> getLambdaNameForConsumer(Consumer<String> c) {
    return LambdaNames.getLambdaName(c);
  }

  @Test
  public void testStaticFunction() {
    Optional<String> lambdaName = getLambdaNameForFunction(s -> TestMethodInvLambdaNames.staticFunction(s));
    assertTrue(lambdaName.isPresent());
    assertNameMatch("s -> TestMethodInvLambdaNames.staticFunction(_)", "testStaticFunction", lambdaName.get());
  }

  @Test
  public void testStaticCallable() {
    Optional<String> lambdaName = getLambdaNameForCallable(() -> TestMethodInvLambdaNames.staticCallable());
    assertTrue(lambdaName.isPresent());
    assertNameMatch("() -> TestMethodInvLambdaNames.staticCallable()", "testStaticCallable", lambdaName.get());
  }

  @Test
  public void testStaticConsumer() {
    Optional<String> lambdaName = getLambdaNameForConsumer(s -> TestMethodInvLambdaNames.staticConsumer(s));
    assertTrue(lambdaName.isPresent());
    assertNameMatch("s -> TestMethodInvLambdaNames.staticConsumer(_)", "testStaticConsumer", lambdaName.get());
  }

  @Test
  public void testVirtualFunctionOnThis() {
    Optional<String> lambdaName = getLambdaNameForFunction(s -> virtualFunction(s));
    assertTrue(lambdaName.isPresent());
    assertNameMatch("s -> virtualFunction(_)", "testVirtualFunctionOnThis", lambdaName.get());
  }


  //TODO different types of captures - local variable, field, static field, computed function
  @Test
  public void testVirtualFunctionWithCaptureOnThis() {
    Optional<String> lambdaName = getLambdaNameForFunction(s -> virtualFunction2(s, ""));
    assertTrue(lambdaName.isPresent());
    System.out.println(lambdaName.get());
    assertNameMatch("s -> virtualFunction(_,_)", "testVirtualFunctionOnThis", lambdaName.get());
  }


  @Test
  public void testVirtualCallableOnThis() {
    Optional<String> lambdaName = getLambdaNameForCallable(() -> virtualCallable());
    assertTrue(lambdaName.isPresent());
    assertNameMatch("() -> virtualCallable()", "testVirtualCallableOnThis", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnThis() {
    Optional<String> lambdaName = getLambdaNameForConsumer(s -> virtualConsumer(s));
    assertTrue(lambdaName.isPresent());
    assertNameMatch("s -> virtualConsumer(_)", "testVirtualConsumerOnThis", lambdaName.get());
  }

  @Test
  public void testVirtualFunctionOnStaticField() {
    Optional<String> lambdaName = getLambdaNameForFunction(s -> staticField.virtualFunction(s));
    assertTrue(lambdaName.isPresent());
    System.out.println(lambdaName.get());
    assertNameMatch("s -> staticField.virtualFunction(_)", "testVirtualFunctionOnStaticField", lambdaName.get());
  }

  @Test
  public void testVirtualCallableOnStaticField() {
    Optional<String> lambdaName = getLambdaNameForCallable(() -> staticField.virtualCallable());
    assertTrue(lambdaName.isPresent());
    assertNameMatch("() -> staticField.virtualCallable()", "testVirtualCallableOnStaticField", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnStaticField() {
    Optional<String> lambdaName = getLambdaNameForConsumer(s -> staticField.virtualConsumer(s));
    assertTrue(lambdaName.isPresent());
    assertNameMatch("s -> staticField.virtualConsumer(_)", "testVirtualConsumerOnStaticField", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnStaticFieldChained() {
    Optional<String> lambdaName = getLambdaNameForConsumer(s -> System.out.println(s));
    assertTrue(lambdaName.isPresent());
    assertNameMatch("s -> out.println(_)", "testVirtualConsumerOnStaticFieldChained", lambdaName.get());
  }


  @Test
  public void testVirtualFunctionOnField() {
    Optional<String> lambdaName = getLambdaNameForFunction(s -> field.virtualFunction(s));
    assertTrue(lambdaName.isPresent());
    assertNameMatch("s -> field.virtualFunction(_)", "testVirtualFunctionOnField", lambdaName.get());
  }


  @Test
  public void testVirtualCallableOnField() {
    Optional<String> lambdaName = getLambdaNameForCallable(() -> field.virtualCallable());
    assertTrue(lambdaName.isPresent());
    assertNameMatch("() -> field.virtualCallable()", "testVirtualCallableOnField", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnField() {
    Optional<String> lambdaName = getLambdaNameForConsumer(s -> field.virtualConsumer(s));
    assertTrue(lambdaName.isPresent());
    assertNameMatch("s -> field.virtualConsumer(_)", "testVirtualConsumerOnField", lambdaName.get());
  }

  @Test
  public void testVirtualFunctionOnVar() {
    TestMethodInvLambdaNames localVar = noParamMethod();
    Optional<String> lambdaName = getLambdaNameForFunction(s -> localVar.virtualFunction(s));
    assertTrue(lambdaName.isPresent());
    System.out.println(lambdaName.get());
    assertNameMatch("s -> localVar.virtualFunction(_)", "testVirtualFunctionOnVar", lambdaName.get());
  }

//  @Test
//  public void testVirtualCallableOnVar() {
//    TestMethodInvLambdaNames localVar = noParamMethod();
//    Optional<String> lambdaName = getLambdaNameForCallable(localVar::virtualCallable);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("localVar::virtualCallable", "testVirtualCallableOnVar", lambdaName.get());
//  }
//
//  @Test
//  public void testVirtualConsumerOnVar() {
//    TestMethodInvLambdaNames localVar = noParamMethod();
//    Optional<String> lambdaName = getLambdaNameForConsumer(localVar::virtualConsumer);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("localVar::virtualConsumer", "testVirtualConsumerOnVar", lambdaName.get());
//  }
//
//  @Test
//  public void testVirtualFunctionOnNoParamMethod() {
//    Optional<String> lambdaName = getLambdaNameForFunction(noParamMethod()::virtualFunction);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("noParamMethod()::virtualFunction", "testVirtualFunctionOnNoParamMethod", lambdaName.get());
//  }
//
//  @Test
//  public void testVirtualCallableOnNoParamMethod() {
//    Optional<String> lambdaName = getLambdaNameForCallable(noParamMethod()::virtualCallable);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("noParamMethod()::virtualCallable", "testVirtualCallableOnNoParamMethod", lambdaName.get());
//  }
//
//  @Test
//  public void testVirtualConsumerOnNoParamMethod() {
//    Optional<String> lambdaName = getLambdaNameForConsumer(noParamMethod()::virtualConsumer);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("noParamMethod()::virtualConsumer", "testVirtualConsumerOnNoParamMethod", lambdaName.get());
//  }
//
//  @Test
//  public void testVirtualFunctionOnNoParamStaticMethod() {
//    Optional<String> lambdaName = getLambdaNameForFunction(noParamStaticMethod()::virtualFunction);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("noParamStaticMethod()::virtualFunction", "testVirtualFunctionOnNoParamStaticMethod", lambdaName.get());
//  }
//
//  @Test
//  public void testVirtualCallableOnNoParamStaticMethod() {
//    Optional<String> lambdaName = getLambdaNameForCallable(noParamStaticMethod()::virtualCallable);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("noParamStaticMethod()::virtualCallable", "testVirtualCallableOnNoParamStaticMethod", lambdaName.get());
//  }
//
//  @Test
//  public void testVirtualConsumerOnNoParamStaticMethod() {
//    Optional<String> lambdaName = getLambdaNameForConsumer(noParamStaticMethod()::virtualConsumer);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("noParamStaticMethod()::virtualConsumer", "testVirtualConsumerOnNoParamStaticMethod", lambdaName.get());
//  }
//
//  //-------
//
//  @Test
//  public void testVirtualFunctionOnParamStaticMethod() {
//    Optional<String> lambdaName = getLambdaNameForFunction(paramStaticMethod(0, "")::virtualFunction);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("paramStaticMethod(_,_)::virtualFunction", "testVirtualFunctionOnParamStaticMethod", lambdaName.get());
//  }
//
//  @Test
//  public void testVirtualCallableOnParamStaticMethod() {
//    Optional<String> lambdaName = getLambdaNameForCallable(paramStaticMethod(Long.MAX_VALUE, "")::virtualCallable);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("paramStaticMethod(_,_)::virtualCallable", "testVirtualCallableOnParamStaticMethod", lambdaName.get());
//  }
//
//  @Test
//  public void testVirtualConsumerOnParamStaticMethod() {
//    Optional<String> lambdaName = getLambdaNameForConsumer(paramStaticMethod(Long.MAX_VALUE, "")::virtualConsumer);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("paramStaticMethod(_,_)::virtualConsumer", "testVirtualConsumerOnParamStaticMethod", lambdaName.get());
//  }
//
//
//  @Test
//  public void testVirtualFunctionOnParamMethod() {
//    Optional<String> lambdaName = getLambdaNameForFunction(paramMethod(0, "")::virtualFunction);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("paramMethod(_,_)::virtualFunction", "testVirtualFunctionOnParamMethod", lambdaName.get());
//  }
//
//  @Test
//  public void testVirtualCallableOnParamMethod() {
//    Optional<String> lambdaName = getLambdaNameForCallable(paramMethod(Long.MAX_VALUE, "")::virtualCallable);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("paramMethod(_,_)::virtualCallable", "testVirtualCallableOnParamMethod", lambdaName.get());
//  }
//
//  @Test
//  public void testVirtualConsumerOnParamMethod() {
//    Optional<String> lambdaName = getLambdaNameForConsumer(paramMethod(Long.MAX_VALUE, "")::virtualConsumer);
//    assertTrue(lambdaName.isPresent());
//    assertNameMatch("paramMethod(_,_)::virtualConsumer", "testVirtualConsumerOnParamMethod", lambdaName.get());
//  }

  private void assertNameMatch(String name,String methodName, String lambdaName) {
    String className = TestMethodInvLambdaNames.class.getSimpleName();
    if (name == null) {
      Pattern p = Pattern.compile(methodName + "\\(" + className + ":\\d+\\)");
      Matcher m = p.matcher(Pattern.quote(name));
      assertTrue(m.matches());
    } else {
      Pattern p = Pattern.compile(Pattern.quote(name) + " " + methodName + "\\(" + className + ":\\d+\\)");
      Matcher m = p.matcher(lambdaName);
      assertTrue(m.matches());
    }
  }

}
