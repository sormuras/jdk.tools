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

/**
 * Defines the Java Tooling API.
 * <p>
 * The basic building block of the Java Tooling API is {@link java.util.spi.ToolProvider} defined in
 * the {@code java.base} module. This ensures that all existing and future tools implementing that
 * base interface are supported by the Java Tooling API.
 * <p>
 * The {@link jdk.tools.Tool} interface of this module describes a {@link java.util.spi.ToolProvider
 * ToolProvider} instance and binds it to an optional namespace and non-empty name. By default, the
 * namespace and name are computed by inspecting provider instance properties. For example:
 * {@snippet
 *   jdk.tools.Tool jar = jdk.tools.Tool.of("jar");
 *   assert jar.namespace().equals("jdk.jartool");
 *   assert jar.name().equals("jar");
 *   assert jar.provider() instanceof java.util.spi.ToolProvider;
 * }
 * <p>
 * An implementation of the {@link jdk.tools.ToolFinder} interface represents a search-able sequence
 * of tool instances. A tool can be searched for by passing its name to the
 * {@link jdk.tools.ToolFinder#find(String)} method. Use the list returned by the
 * {@link jdk.tools.ToolFinder#tools()} accessor to perform custom searches.
 *
 * Note that the {@link jdk.tools.Tool} interface
 * extends the {@link jdk.tools.ToolFinder} interface (containing itself as the only tool instance)
 * in order to allow better composibility via the {@link jdk.tools.ToolFinder#compose(jdk.tools.ToolFinder...)}
 * factory method. Here are examples of commonly used factory methods:
 * <ul>
 * <li>{@link jdk.tools.ToolFinder#of(String...)} ...
 * <li>{@link jdk.tools.ToolFinder#of(jdk.tools.Tool...)} ...
 * <li>{@link jdk.tools.ToolFinder#of(ModuleLayer)} ...
 * </ul>
 * <p>
 * An implementation of the {@link jdk.tools.ToolRunner} interface offers various methods to run
 * instances of {@link jdk.tools.Tool}. It also provides run-time relevant components and constants.
 * Here is an exemplary usage pattern:
 * {@snippet
 *   var finder = ToolFinder.of("jar", "javac");
 *   var runner = ToolRunner.of(finder);
 *   runner.run("jar", "--version");
 *   runner.run("javac", "--version");
 * }
 * <p>
 * The {@link jdk.tools.ToolOperator} interface is a {@link java.util.spi.ToolProvider} extension
 * implemented by classes want to be capable of running other tools. For example:
 * {@snippet
 * public record VersionPrinter(String name) implements jdk.tools.ToolOperator {
 *   public VersionPrinter() { this("versions"); }
 *   public int run(ToolRunner runner, PrintWriter out, PrintWriter err, String... names) {
 *     if (names.length == 0) {
 *       runner.run("jar", "--version");
 *       runner.run("javac", "--version");
 *       runner.run("javadoc", "--version");
 *     }
 *     for (var name : names) runner.run(name, "--version");
 *     return 0;
 *   }
 * }
 * <p>
 * {@link jdk.tools.Command} represents a composable tool call from the command-line.
 * For example, the tool call {@code javac --version} can be composed via:
 * <ul>
 *   <li>{@code Command.ofCommandLine("javac --version")}
 *   <li>{@code Command.ofCommand(List.of("javac", "--version")}
 *   <li>{@code Command.of("javac").with("--version")}
 *   <li>{@code Command.of("javac", "--version")}
 * </ul>
 * Task -  tool operator extension for running an ordered collection of command instances.
 * <p>
 * The {@link jdk.tools.ToolMenu} interface supports creating subcommand hierarchies using nested
 * {@link jdk.tools.ToolFinder} instances.
 */
module jdk.tools {
  requires jdk.jfr;

  exports jdk.tools;

  uses java.util.spi.ToolProvider;
  uses jdk.tools.ToolFinder;
}
