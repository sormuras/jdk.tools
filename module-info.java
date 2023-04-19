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
 *
 * <p>The basic building block of the Java Tooling API is the {@link java.util.spi.ToolProvider
 * ToolProvider} interface defined by module {@code java.base} in the {@code java.util.spi} package.
 * This ensures that all existing and future tools implementing that base interface are supported by
 * the Java Tooling API.
 *
 * <p>In module {@code jdk.tools}, the {@link jdk.tools.Tool Tool} interface describes a {@link
 * java.util.spi.ToolProvider ToolProvider} instance and binds it to an optional namespace and
 * non-empty name. By default, the namespace and name are computed by inspecting provider instance
 * properties. For example:
 *
 * <pre>{@snippet :
 *   var jar = Tool.of("jar");
 *
 *   assert jar.namespace().equals("jdk.jartool");
 *   assert jar.name().equals("jar");
 *   assert jar.provider() instanceof java.util.spi.ToolProvider;
 * }</pre>
 *
 * <p>An implementation of the {@link jdk.tools.ToolFinder ToolFinder} interface represents a
 * search-able sequence of tool instances. A tool can be searched for by passing its name to the
 * {@link jdk.tools.ToolFinder#find(String)} method. Use the list returned by the {@link
 * jdk.tools.ToolFinder#tools()} accessor to perform custom searches.
 *
 * <p>Note that the {@link jdk.tools.Tool Tool} interface extends the {@link jdk.tools.ToolFinder
 * ToolFinder} interface (containing itself as the only tool instance) in order to allow better
 * composability via the {@link jdk.tools.ToolFinder#compose(jdk.tools.ToolFinder...)} factory
 * method. Here are examples of commonly used factory methods:
 *
 * <ul>
 *   <li>{@link jdk.tools.ToolFinder#of(String...)} ...
 *   <li>{@link jdk.tools.ToolFinder#of(jdk.tools.Tool...)} ...
 * </ul>
 *
 * <p>An implementation of the {@link jdk.tools.ToolRunner ToolRunner} interface offers various
 * methods to execute instances of {@link jdk.tools.Tool Tool} using the linked {@link
 * java.util.spi.ToolProvider ToolProvider} object. It also provides run-time relevant components
 * and constants. Here is an exemplary usage pattern:
 *
 * <pre>{@snippet :
 *   var finder = ToolFinder.of("jar", "javac");
 *   var runner = ToolRunner.of(finder);
 *
 *   runner.run("jar", "--version");
 *   runner.run("javac", "--version");
 * }</pre>
 *
 * <p>The {@link jdk.tools.ToolOperator ToolOperator} interface is a {@link
 * java.util.spi.ToolProvider ToolProvider} extension implemented by classes that want to be capable
 * of running other tools. For example:
 *
 * <pre>{@snippet :
 * public record VersionPrinter(String name) implements ToolOperator {
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
 * }</pre>
 *
 * <p>{@link jdk.tools.Command Command} represents a tool call from the command-line that is
 * programmatically composable. For example, the tool call {@code javac --version} can be composed
 * via:
 *
 * <ul>
 *   <li>{@code Command.ofCommandLine("javac --version")}
 *   <li>{@code Command.ofCommand(List.of("javac", "--version")}
 *   <li>{@code Command.of("javac").with("--version")}
 *   <li>{@code Command.of("javac", "--version")}
 * </ul>
 *
 * <p>The {@link jdk.tools.Task Task} interface is tool operator extension for running an ordered
 * collection of command instances.
 *
 * <pre> {@snippet :
 *   var versions = Task.of("preset", "versions", Command.of("jar", "--version"), Command.of("javac", "--version"));
 *   var finder = ToolFinder.compose(ToolFinder.of("jar", "javac"), versions);
 *   var runner = ToolRunner.of(finder);
 *
 *   runner.run("versions");
 * } </pre>
 *
 * <p>The {@link jdk.tools.ToolMenu ToolMenu} interface supports creating subcommand hierarchies
 * using nested {@link jdk.tools.ToolFinder ToolFinder} instances.
 */
module jdk.tools {
  requires jdk.jfr;

  exports jdk.tools;

  uses java.util.spi.ToolProvider;
}
