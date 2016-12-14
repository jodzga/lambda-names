package com.linkedin.util.lambda;

import static org.testng.Assert.assertFalse;

import java.util.Optional;

import org.testng.annotations.Test;

public class TestLambdaNames {

  static {
    LambdaNames.initialize();
  }


  @Test
  public void testNotALambda() {
    Object o = new Object();
    Optional<String> lambdaName = LambdaNames.getLambdaName(o);
    assertFalse(lambdaName.isPresent());
  }

  @Test(expectedExceptions={NullPointerException.class})
  public void testNull() {
    Optional<String> lambdaName = LambdaNames.getLambdaName(null);
    assertFalse(lambdaName.isPresent());
  }

}
