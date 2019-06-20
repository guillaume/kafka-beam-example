package org.apache.beam.sdk.io.kafka;

import clojure.lang.Compiler;
import clojure.lang.IFn;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;

public final class CustomFunction_ extends SimpleFunction<KV<String, Long>, String> {

  private final Object form;

  public CustomFunction_(Object form) {
    this.form = form;
  }

  @Override
  public String apply(KV<String, Long> input) {
    final IFn fn = (IFn) Compiler.eval(this.form);
    return (String) fn.invoke(input);
  }

}