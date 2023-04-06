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

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

/**
 * Represents a tool call from the command-line.
 *
 * <p>For example: a command {@code javac --version} can be composed via:
 *
 * <ul>
 *   <li>{@code Command.ofCommandLine("javac --version")}
 *   <li>{@code Command.ofCommand(List.of("javac", "--version")}
 *   <li>{@code Command.of("javac").with("--version")}
 *   <li>{@code Command.of("javac", "--version")}
 * </ul>
 *
 * @param tool the name of the tool to run
 * @param arguments the commands of arguments to pass to the tool being run
 */
public record Command(String tool, List<String> arguments) {
  public static Command of(String tool) {
    return new Command(tool);
  }

  public static Command of(String tool, Object... args) {
    if (args.length == 0) return new Command(tool);
    if (args.length == 1) return new Command(tool, trim(args[0]));
    if (args.length == 2) return new Command(tool, trim(args[0]), trim(args[1]));
    return new Command(tool).with(Stream.of(args));
  }

  // command = ["tool-name", "tool-args", ...]
  public static Command ofCommand(List<String> command) {
    var size = command.size();
    if (size == 0) throw new IllegalArgumentException("Empty command");
    var tool = command.get(0);
    if (size == 1) return new Command(tool);
    if (size == 2) return new Command(tool, trim(command.get(1)));
    if (size == 3) return new Command(tool, trim(command.get(1)), trim(command.get(2)));
    return new Command(tool).with(command.stream().skip(1).map(Command::trim));
  }

  // line = "tool-name [tool-args...]"
  public static Command ofCommandLine(String line) {
    return Command.ofCommand(List.of(trim(line).split("\\s+")));
  }

  private static String trim(Object object) {
    return object.toString().trim();
  }

  private Command(String tool, String... args) {
    this(tool, List.of(args));
  }

  public String[] toArray() {
    return arguments.toArray(String[]::new);
  }

  public String toCommandLine() {
    return toCommandLine(" ");
  }

  public String toCommandLine(String delimiter) {
    if (arguments.isEmpty()) return tool;
    if (arguments.size() == 1) return tool + delimiter + arguments.get(0);
    var joiner = new StringJoiner(delimiter).add(tool);
    arguments.forEach(joiner::add);
    return joiner.toString();
  }

  public Command with(Stream<?> objects) {
    var strings = objects.map(Command::trim);
    return new Command(tool, Stream.concat(arguments.stream(), strings).toList());
  }

  public Command with(String[] arguments) {
    return with(Stream.of(arguments));
  }

  public Command with(Object argument) {
    return with(Stream.of(argument));
  }

  public Command with(String key, Object value, Object... values) {
    var call = with(Stream.of(key, value));
    return values.length == 0 ? call : call.with(Stream.of(values));
  }

  public Command withFindFiles(String glob) {
    return withFindFiles(Path.of(""), glob);
  }

  public Command withFindFiles(Path start, String glob) {
    return withFindFiles(start, "glob", glob);
  }

  public Command withFindFiles(Path start, String syntax, String pattern) {
    var syntaxAndPattern = syntax + ':' + pattern;
    var matcher = start.getFileSystem().getPathMatcher(syntaxAndPattern);
    return withFindFiles(start, Integer.MAX_VALUE, matcher);
  }

  public Command withFindFiles(Path start, int maxDepth, PathMatcher matcher) {
    try (var files = Files.find(start, maxDepth, (p, a) -> matcher.matches(p))) {
      return with(files);
    } catch (Exception exception) {
      throw new RuntimeException("Find files failed in: " + start, exception);
    }
  }

  public Command withTweak(Tweak tweak) {
    return tweak.tweak(this);
  }

  public Command withTweak(int position, Tweak tweak) {
    var call = new Command(tool, List.of()).with(arguments.stream().limit(position));
    return tweak.tweak(call).with(arguments.stream().skip(position));
  }

  public Command withTweaks(Iterable<Tweak> tweaks) {
    var tweaked = this;
    for (var tweak : tweaks) tweaked = tweak.tweak(tweaked);
    return tweaked;
  }

  /** Represents a unary operation on a command producing a new command with other arguments. */
  @FunctionalInterface
  public interface Tweak {
    Command tweak(Command call);
  }
}
