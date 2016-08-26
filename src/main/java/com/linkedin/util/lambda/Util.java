package com.linkedin.util.lambda;

class Util {
  static String extractSimpleName(String fqcn, String separator) {
    if (fqcn.contains(separator)) {
      return fqcn.substring(fqcn.lastIndexOf(separator) + 1, fqcn.length());
    } else {
      return fqcn;
    }
  }
}
