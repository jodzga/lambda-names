package com.linkedin.util.lambda;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.ea.agentloader.AgentLoader;

public class LambdaNames {

  private static ConcurrentMap<String, String> _names = new ConcurrentHashMap<>();

  private LambdaNames() {
  }

  public static Optional<String> getLambdaName(Object lambda) {
    String lambdaClassName = lambda.getClass().getName();
    String name = lambdaClassName.substring(0, lambdaClassName.lastIndexOf('/'));
    return Optional.ofNullable(_names.get(name));
  }

  static void add(String lambdaClassName, String name) {
    _names.put(lambdaClassName, name);
  }

  public static void initialize() {
    AgentLoader.loadAgentClass(LambdaNamesAgent.class.getName(), null);
  }

}
