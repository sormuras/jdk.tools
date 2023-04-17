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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.spi.ToolProvider;
import java.util.stream.Stream;

/** A tool provider implementation running operating system programs. */
public record Program(String name, List<String> command) implements ToolProvider {
  public static Optional<ToolProvider> findJavaDevelopmentKitTool(String name, Object... args) {
    return findInFolder(name, Path.of(System.getProperty("java.home", ""), "bin"), args);
  }

  public static Optional<ToolProvider> findInFolder(String name, Path folder, Object... args) {
    if (!Files.isDirectory(folder)) return Optional.empty();
    var win = System.getProperty("os.name", "").toLowerCase().contains("win");
    var file = name + (win && !name.endsWith(".exe") ? ".exe" : "");
    return findExecutable(name, folder.resolve(file), args);
  }

  public static Optional<ToolProvider> findExecutable(String name, Path file, Object... args) {
    if (!Files.isExecutable(file)) return Optional.empty();
    var command = new ArrayList<String>();
    command.add(file.toString());
    command.addAll(Stream.of(args).map(Object::toString).toList());
    return Optional.of(new Program(name, List.copyOf(command)));
  }

  @Override
  public int run(PrintWriter out, PrintWriter err, String... arguments) {
    var builder = new ProcessBuilder(new ArrayList<>(command));
    builder.command().addAll(List.of(arguments));
    try {
      var process = builder.start();
      new Thread(new LinePrinter(process.getInputStream(), out), name + "-out").start();
      new Thread(new LinePrinter(process.getErrorStream(), err), name + "-err").start();
      return process.waitFor();
    } catch (InterruptedException exception) {
      return -1;
    } catch (Exception exception) {
      exception.printStackTrace(err);
      return 1;
    }
  }

  record LinePrinter(InputStream stream, PrintWriter writer) implements Runnable {
    @Override
    public void run() {
      new BufferedReader(new InputStreamReader(stream)).lines().forEach(writer::println);
    }
  }
}
