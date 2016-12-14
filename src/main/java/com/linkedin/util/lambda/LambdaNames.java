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
    int slashIndex = lambdaClassName.lastIndexOf('/');
    if (slashIndex > 0) {
      String name = lambdaClassName.substring(0, slashIndex);
      return Optional.ofNullable(_names.get(name));
    } else {
      return Optional.empty();
    }
  }

  static void add(String lambdaClassName, String name) {
    _names.put(lambdaClassName, name);
  }

  public static void initialize() {
    AgentLoader.loadAgentClass(LambdaNamesAgent.class.getName(), null);
  }

}
