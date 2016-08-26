package com.linkedin.util.lambda;

import static org.testng.Assert.assertTrue;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.annotations.Test;

public class FunctionLambdaNamesTest {

  static FunctionLambdaNamesTest obj = new FunctionLambdaNamesTest();

  static {
    LambdaNames.initialize();
  }

  String virtualMethod(String s) {
    return s;
  }

  String virtualMethodNoParams() {
    return "";
  }

  static String staticMethod(String s) {
    return s;
  }

  private Optional<String> getLambdaName(Function<String, String> f) {
    return LambdaNames.getLambdaName(f);
  }

  private Optional<String> getLambdaName(Callable<String> c) {
    return LambdaNames.getLambdaName(c);
  }

  @Test
  public void testStaticMethod() {
    Optional<String> lambdaName = getLambdaName(FunctionLambdaNamesTest::staticMethod);
    assertTrue(lambdaName.isPresent());
    System.out.println(lambdaName);
    //assertNameMatch("FunctionLambdaNamesTest::staticMethod", "testStaticMethod", lambdaName.get());
  }

  @Test
  public void testVirtualMethod() {
//    FunctionLambdaNamesTest obj = new FunctionLambdaNamesTest();
    Optional<String> lambdaName =
        getLambdaName(obj::virtualMethod);
    assertTrue(lambdaName.isPresent());
    //assertNameMatch("::virtualMethod", "testVirtualMethod", lambdaName.get());
    System.out.println(lambdaName);
  }

  @Test
  public void testVirtualMethodOnNewObject() {
//    FunctionLambdaNamesTest obj = new FunctionLambdaNamesTest();
    Optional<String> lambdaName =
        getLambdaName(new FunctionLambdaNamesTest()::virtualMethod);
    assertTrue(lambdaName.isPresent());
    //assertNameMatch("::virtualMethod", "testVirtualMethod", lambdaName.get());
    System.out.println(lambdaName);
  }

  @Test
  public void testVirtualMethodOnThis() {
    Optional<String> lambdaName = getLambdaName(this::virtualMethod);
    assertTrue(lambdaName.isPresent());
    //assertNameMatch("::virtualMethod", "testVirtualMethodOnThis", lambdaName.get());
    System.out.println(lambdaName);
  }

  @Test
  public void testMethodOnLambdaParam() {
    Optional<String> lambdaName = getLambdaName(str -> str.toLowerCase());
    assertTrue(lambdaName.isPresent());
    System.out.println(lambdaName);
  }

  @Test
  public void testBlock() {
    Optional<String> lambdaName = getLambdaName(str -> {
      if (str.length() > 0) {
        return str.trim();
      } else {
        return str;
      }
    });
    assertTrue(lambdaName.isPresent());
    System.out.println(lambdaName);
  }

  @Test
  public void testConstantCallable() {
    Optional<String> lambdaName = getLambdaName(() -> "hello");
    assertTrue(lambdaName.isPresent());
    System.out.println(lambdaName);
  }

  @Test
  public void testConstantVirtualMethodNoParams() {
    Optional<String> lambdaName = getLambdaName(() -> virtualMethodNoParams());
    assertTrue(lambdaName.isPresent());
    System.out.println(lambdaName);
  }

  @Test
  public void testConstantVirtualMethodParams() {
    Optional<String> lambdaName = getLambdaName(() -> virtualMethod(""));
    assertTrue(lambdaName.isPresent());
    System.out.println(lambdaName);
  }


  private void assertNameMatch(String name,String methodName, String lambdaName) {
    String className = FunctionLambdaNamesTest.class.getSimpleName();
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
