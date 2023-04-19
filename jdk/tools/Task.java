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
import java.util.List;

/** A tool operator extension for running an ordered collection of command instances. */
@FunctionalInterface
public interface Task extends ToolOperator {
  static Task of(String namespace, String name, Command first, Command... more) {
    return Internal.newTask(namespace, name, first, more);
  }

  // args = ["jar", "--version", "+", "javac", "--version", ...]
  static Task of(String namespace, String name, String... args) {
    return Internal.newTask(namespace, name, args);
  }

  // args = ["jar", "--version", "+", "javac", "--version", ...]
  static Task of(String namespace, String name, String delimiter, List<String> args) {
    return Internal.newTask(namespace, name, delimiter, args);
  }

  String ARGUMENT_DELIMITER = "+";

  String MODULE_NAME = "*";

  List<Command> commands();

  default boolean parallel() {
    return false;
  }

  @Override
  default int run(ToolRunner runner, PrintWriter out, PrintWriter err, String... args) {
    var commands = commands();
    if (parallel()) commands.stream().parallel().forEach(runner::run);
    else for (var command : commands) runner.run(command);
    return 0;
  }

  /** An annotation used to attach named command sequences to a module descriptor. */
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.MODULE)
  @Repeatable(Of.Container.class)
  @interface Of {
    String namespace() default MODULE_NAME;

    String name();

    String delimiter() default ARGUMENT_DELIMITER;

    String[] args();

    /** Repeatable annotation collector. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.MODULE)
    @interface Container {
      Of[] value();
    }
  }
}
