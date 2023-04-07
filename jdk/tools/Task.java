/*
 * Copyright (c) 2023, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.tools;

import java.io.PrintWriter;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents named sequence of command instances.
 *
 * @param namespace the namespace of this task
 * @param name the name of this task
 * @param commands the list of command instances to execute
 */
public record Task(String namespace, String name, List<Command> commands) implements ToolOperator {
  public static final String ARGUMENT_DELIMITER = "+";
  public static final String MODULE_NAME = "*";

  // args = ["jar", "--version", "+", "javac", "--version", ...]
  public static Task of(String namespace, String name, String... args) {
    return Task.of(namespace, name, ARGUMENT_DELIMITER, List.of(args));
  }

  // args = ["jar", "--version", <delimiter>, "javac", "--version", ...]
  static Task of(String namespace, String name, String delimiter, List<String> args) {
    if (args.isEmpty()) return new Task(namespace, name, List.of());
    var arguments = new ArrayDeque<>(args);
    var elements = new ArrayList<String>();
    var commands = new ArrayList<Command>();
    while (true) {
      var empty = arguments.isEmpty();
      if (empty || arguments.peekFirst().equals(delimiter)) {
        commands.add(Command.of(elements.get(0)).with(elements.stream().skip(1)));
        elements.clear();
        if (empty) break;
        arguments.pop(); // consume delimiter
      }
      var element = arguments.pop(); // consume element
      elements.add(element.trim());
    }
    return new Task(namespace, name, List.copyOf(commands));
  }

  public Task {
    if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");
    if (commands == null) throw new IllegalArgumentException("commands must not be null");
  }

  @Override
  public int run(ToolRunner runner, PrintWriter out, PrintWriter err, String... args) {
    for (var command : commands) runner.run(command);
    return 0;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.MODULE)
  @Repeatable(Of.Annotations.class)
  @interface Of {
    String namespace() default MODULE_NAME;
    String name();
    String delimiter() default ARGUMENT_DELIMITER;
    String[] args();

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.MODULE)
    @interface Annotations {
      Of[] value();
    }
  }

  static List<Task> of(Module module) {
    return Stream.of(module.getAnnotationsByType(Of.class))
        .map(annotation -> Task.of(module, annotation))
        .toList();
  }

  static Task of(Module module, Of annotation) {
    var namespace = annotation.namespace();
    return Task.of(
        namespace.equals(MODULE_NAME) ? module.getName() : namespace,
        annotation.name(),
        annotation.delimiter(),
        List.of(annotation.args()));
  }
}
