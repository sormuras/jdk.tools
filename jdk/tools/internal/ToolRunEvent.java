package jdk.tools.internal;

import jdk.jfr.Category;
import jdk.jfr.Enabled;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;
import jdk.jfr.StackTrace;

@Category("JDK Tools")
@Enabled
@StackTrace(false)
@Label("Tool Run")
@Name("jdk.tools.ToolRun")
public final class ToolRunEvent extends Event {
  @Label("Tool Namespace")
  public String namespace;

  @Label("Tool Name")
  public String name;

  @Label("Tool Provider")
  public Class<?> provider;

  @Label("Tool Arguments")
  public String args;

  @Label("Exit Code")
  public int code;

  @Label("Output")
  public String out;

  @Label("Errors")
  public String err;
}
