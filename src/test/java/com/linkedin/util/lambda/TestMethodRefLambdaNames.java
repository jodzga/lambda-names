package com.linkedin.util.lambda;

import static org.testng.Assert.assertTrue;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

public class TestMethodRefLambdaNames {

  static {
    LambdaNames.initialize();
  }

  static TestMethodRefLambdaNames staticField = new TestMethodRefLambdaNames();
  TestMethodRefLambdaNames field = this;

  private TestMethodRefLambdaNames noParamMethod() {
    return this;
  }

  private static TestMethodRefLambdaNames noParamStaticMethod() {
    return staticField;
  }


  private TestMethodRefLambdaNames paramMethod(long x, String y) {
    return this;
  }

  private static TestMethodRefLambdaNames paramStaticMethod(long x, String y) {
    return staticField;
  }

  String virtualFunction(String s) {
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
    Optional<String> lambdaName = getLambdaNameForFunction(TestMethodRefLambdaNames::staticFunction);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("TestMethodRefLambdaNames::staticFunction", "testStaticFunction", lambdaName.get());
  }

  @Test
  public void testStaticCallable() {
    Optional<String> lambdaName = getLambdaNameForCallable(TestMethodRefLambdaNames::staticCallable);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("TestMethodRefLambdaNames::staticCallable", "testStaticCallable", lambdaName.get());
  }

  @Test
  public void testStaticConsumer() {
    Optional<String> lambdaName = getLambdaNameForConsumer(TestMethodRefLambdaNames::staticConsumer);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("TestMethodRefLambdaNames::staticConsumer", "testStaticConsumer", lambdaName.get());
  }

  @Test
  public void testVirtualFunctionOnThis() {
    Optional<String> lambdaName = getLambdaNameForFunction(this::virtualFunction);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("this::virtualFunction", "testVirtualFunctionOnThis", lambdaName.get());
  }

  @Test
  public void testVirtualCallableOnThis() {
    Optional<String> lambdaName = getLambdaNameForCallable(this::virtualCallable);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("this::virtualCallable", "testVirtualCallableOnThis", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnThis() {
    Optional<String> lambdaName = getLambdaNameForConsumer(this::virtualConsumer);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("this::virtualConsumer", "testVirtualConsumerOnThis", lambdaName.get());
  }

  @Test
  public void testVirtualFunctionOnStaticField() {
    Optional<String> lambdaName = getLambdaNameForFunction(staticField::virtualFunction);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("staticField::virtualFunction", "testVirtualFunctionOnStaticField", lambdaName.get());
  }

  @Test
  public void testVirtualCallableOnStaticField() {
    Optional<String> lambdaName = getLambdaNameForCallable(staticField::virtualCallable);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("staticField::virtualCallable", "testVirtualCallableOnStaticField", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnStaticField() {
    Optional<String> lambdaName = getLambdaNameForConsumer(staticField::virtualConsumer);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("staticField::virtualConsumer", "testVirtualConsumerOnStaticField", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnStaticFieldChained() {
    Optional<String> lambdaName = getLambdaNameForConsumer(System.out::println);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("out::println", "testVirtualConsumerOnStaticFieldChained", lambdaName.get());
  }

  @Test
  public void testVirtualFunctionOnField() {
    Optional<String> lambdaName = getLambdaNameForFunction(field::virtualFunction);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("field::virtualFunction", "testVirtualFunctionOnField", lambdaName.get());
  }

  @Test
  public void testVirtualCallableOnField() {
    Optional<String> lambdaName = getLambdaNameForCallable(field::virtualCallable);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("field::virtualCallable", "testVirtualCallableOnField", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnField() {
    Optional<String> lambdaName = getLambdaNameForConsumer(field::virtualConsumer);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("field::virtualConsumer", "testVirtualConsumerOnField", lambdaName.get());
  }

  @Test
  public void testVirtualFunctionOnVar() {
    TestMethodRefLambdaNames localVar = noParamMethod();
    Optional<String> lambdaName = getLambdaNameForFunction(localVar::virtualFunction);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("localVar::virtualFunction", "testVirtualFunctionOnVar", lambdaName.get());
  }

  @Test
  public void testVirtualCallableOnVar() {
    TestMethodRefLambdaNames localVar = noParamMethod();
    Optional<String> lambdaName = getLambdaNameForCallable(localVar::virtualCallable);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("localVar::virtualCallable", "testVirtualCallableOnVar", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnVar() {
    TestMethodRefLambdaNames localVar = noParamMethod();
    Optional<String> lambdaName = getLambdaNameForConsumer(localVar::virtualConsumer);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("localVar::virtualConsumer", "testVirtualConsumerOnVar", lambdaName.get());
  }

  @Test
  public void testVirtualFunctionOnNoParamMethod() {
    Optional<String> lambdaName = getLambdaNameForFunction(noParamMethod()::virtualFunction);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("noParamMethod()::virtualFunction", "testVirtualFunctionOnNoParamMethod", lambdaName.get());
  }

  @Test
  public void testVirtualCallableOnNoParamMethod() {
    Optional<String> lambdaName = getLambdaNameForCallable(noParamMethod()::virtualCallable);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("noParamMethod()::virtualCallable", "testVirtualCallableOnNoParamMethod", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnNoParamMethod() {
    Optional<String> lambdaName = getLambdaNameForConsumer(noParamMethod()::virtualConsumer);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("noParamMethod()::virtualConsumer", "testVirtualConsumerOnNoParamMethod", lambdaName.get());
  }

  @Test
  public void testVirtualFunctionOnNoParamStaticMethod() {
    Optional<String> lambdaName = getLambdaNameForFunction(noParamStaticMethod()::virtualFunction);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("noParamStaticMethod()::virtualFunction", "testVirtualFunctionOnNoParamStaticMethod", lambdaName.get());
  }

  @Test
  public void testVirtualCallableOnNoParamStaticMethod() {
    Optional<String> lambdaName = getLambdaNameForCallable(noParamStaticMethod()::virtualCallable);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("noParamStaticMethod()::virtualCallable", "testVirtualCallableOnNoParamStaticMethod", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnNoParamStaticMethod() {
    Optional<String> lambdaName = getLambdaNameForConsumer(noParamStaticMethod()::virtualConsumer);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("noParamStaticMethod()::virtualConsumer", "testVirtualConsumerOnNoParamStaticMethod", lambdaName.get());
  }

  //-------

  @Test
  public void testVirtualFunctionOnParamStaticMethod() {
    Optional<String> lambdaName = getLambdaNameForFunction(paramStaticMethod(0, "")::virtualFunction);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("paramStaticMethod(_,_)::virtualFunction", "testVirtualFunctionOnParamStaticMethod", lambdaName.get());
  }

  @Test
  public void testVirtualCallableOnParamStaticMethod() {
    Optional<String> lambdaName = getLambdaNameForCallable(paramStaticMethod(Long.MAX_VALUE, "")::virtualCallable);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("paramStaticMethod(_,_)::virtualCallable", "testVirtualCallableOnParamStaticMethod", lambdaName.get());
  }

  @Test
  public void testVirtualConsumerOnParamStaticMethod() {
    Optional<String> lambdaName = getLambdaNameForConsumer(paramStaticMethod(Long.MAX_VALUE, "")::virtualConsumer);
    System.out.println(lambdaName);
    assertTrue(lambdaName.isPresent());
    assertNameMatch("paramStaticMethod(_,_)::virtualConsumer", "testVirtualConsumerOnParamStaticMethod", lambdaName.get());
  }





  private void assertNameMatch(String name,String methodName, String lambdaName) {
    String className = TestMethodRefLambdaNames.class.getSimpleName();
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
