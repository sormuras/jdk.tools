package jdk.tools.internal;

import jdk.tools.ToolFinder;
import jdk.tools.ToolPrinter;
import jdk.tools.ToolRunner;
import jdk.tools.ToolRunner.Context;

public record DefaultToolRunner(ToolFinder finder, ToolPrinter printer)
    implements ToolRunner, Context {
  @Override
  public Context context() {
    return this;
  }
}
