package com.linkedin.util.lambda;

enum State {
  INIT,
  WITH_HIDDEN_ANNOTATION,
  FIRST_LOADS_AND_GETFIELDS,
  METHOD_REF,
  BLOCK,
  UNKNOWN
}