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

import java.util.ServiceLoader;
import java.util.spi.ToolProvider;

/** A runner of tools providing default implementations. */
@FunctionalInterface
public interface ToolRunner {
  /** Run-time relevant components used by all tool runner implementations. */
  interface Context {
    ToolFinder finder();

    ToolPrinter printer();
  }

  Context context();

  default void run(Command command) {
    run(command.tool(), command.toArray());
  }

  default void run(String tool, String... args) {
    var finder = context().finder();
    var found = finder.find(tool);
    if (found.isEmpty()) throw new ToolNotFoundException(tool);
    run(found.get(), args);
  }

  default void run(Tool tool, String... args) {
    run(context().printer(), tool, args);
  }

  default void run(ToolPrinter printer, Tool tool, String... args) {
    var event = Internal.newToolRunEvent(tool);

    event.args = String.join(" ", args);

    printer.debug("| " + event.name + " " + event.args);
    event.begin();
    try {
      var out = Internal.newStringPrintWriter(printer.out());
      var err = Internal.newStringPrintWriter(printer.err());
      var provider = tool.provider();
      var loader = provider.getClass().getClassLoader();
      Thread.currentThread().setContextClassLoader(loader);
      event.code =
          provider instanceof ToolOperator operator
              ? operator.run(this, out, err, args)
              : provider.run(out, err, args);
      event.end();
      if (out.checkError()) System.err.println("The normal output stream had troubles");
      if (err.checkError()) System.err.println("The errors output stream had troubles");
      event.out = out.toString().strip();
      event.err = err.toString().strip();
      if (event.code == 0) return;
    } finally {
      event.commit();
    }
    var message = "Tool %s returned exit code: %d".formatted(tool.toNamespaceAndName(), event.code);
    throw new RuntimeException(message);
  }

  static ToolRunner ofSystem() {
    var loader = ServiceLoader.load(ToolProvider.class);
    var finder = Internal.newToolFinder(loader, __ -> true);
    return ToolRunner.of(finder);
  }

  static ToolRunner of(ToolFinder finder) {
    return ToolRunner.of(finder, ToolPrinter.ofSystem());
  }

  static ToolRunner of(ToolFinder finder, ToolPrinter printer) {
    return Internal.newToolRunner(finder, printer);
  }
}
