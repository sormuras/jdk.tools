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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;
import java.util.spi.ToolProvider;
import java.util.stream.Stream;
import jdk.tools.internal.CompositeToolFinder;
import jdk.tools.internal.DefaultTask;
import jdk.tools.internal.DefaultTool;
import jdk.tools.internal.DefaultToolFinder;
import jdk.tools.internal.EmptyToolFinder;

/** Package-private helper containing utility methods and accessors for internal implementations. */
class Internal {
  static String computeNamespace(Object object) {
    var type = object instanceof Class<?> c ? c : object.getClass();
    var module = type.getModule();
    return module.isNamed() ? module.getName() : type.getPackageName();
  }

  static boolean matchesToolName(Tool tool, String string) {
    String name = tool.name();
    return name.equals(string) || name.startsWith(string + '@');
  }

  static Tool newTool(ToolProvider provider) {
    return newTool(computeNamespace(provider), provider);
  }

  static Tool newTool(String namespace, ToolProvider provider) {
    return new DefaultTool(namespace, provider.name(), provider);
  }

  static Task newTask(String namespace, String name, String... args) {
    return newTask(namespace, name, Task.ARGUMENT_DELIMITER, List.of(args));
  }

  // args = ["jar", "--version", <delimiter>, "javac", "--version", ...]
  static Task newTask(String namespace, String name, String delimiter, List<String> args) {
    if (args.isEmpty()) return new DefaultTask(namespace, name, List.of());
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
    return new DefaultTask(namespace, name, List.copyOf(commands));
  }

  static Task newTask(String namespace, String name, Command first, Command... more) {
    if (more.length == 0) return new DefaultTask(namespace, name, List.of(first));
    var commands = Stream.concat(Stream.of(first), Stream.of(more)).toList();
    return new DefaultTask(namespace, name, commands);
  }

  static ToolFinder newToolFinder(String... tools) {
    if (tools.length == 0) return emptyToolFinder();
    if (tools.length == 1) return Tool.of(tools[0]);
    return new DefaultToolFinder(Stream.of(tools).map(Tool::of).toList());
  }

  static ToolFinder newToolFinder(Tool... tools) {
    if (tools.length == 0) return emptyToolFinder();
    if (tools.length == 1) return tools[0];
    return new DefaultToolFinder(List.of(tools));
  }

  static ToolFinder newToolFinder(List<? extends Tool> tools) {
    if (tools.isEmpty()) return emptyToolFinder();
    if (tools.size() == 1) return tools.get(0);
    return new DefaultToolFinder(List.copyOf(tools));
  }

  static ToolFinder newToolFinder(ModuleLayer layer, Predicate<Module> include) {
    return ToolFinder.compose(
        newToolFinderOfTasks(layer, include),
        newToolFinderOfFinders(ServiceLoader.load(layer, ToolFinder.class), include),
        newToolFinderOfProviders(ServiceLoader.load(layer, ToolProvider.class), include));
  }

  static ToolFinder newToolFinderOfFinders(
      ServiceLoader<ToolFinder> loader, Predicate<Module> include) {
    var finders =
        loader.stream()
            .filter(provider -> include.test(provider.type().getModule()))
            .map(ServiceLoader.Provider::get)
            .toList();
    return ToolFinder.compose(finders);
  }

  static ToolFinder newToolFinderOfProviders(
      ServiceLoader<ToolProvider> loader, Predicate<Module> include) {
    var tools =
        loader.stream()
            .filter(provider -> include.test(provider.type().getModule()))
            .map(ServiceLoader.Provider::get)
            .map(Tool::of)
            .toList();
    return ToolFinder.of(tools);
  }

  static ToolFinder newToolFinderOfTasks(ModuleLayer layer, Predicate<Module> include) {
    if (layer.modules().isEmpty()) return emptyToolFinder();
    var modules = layer.modules().stream().filter(include).toList();
    if (modules.isEmpty()) return emptyToolFinder();
    var tasks = new ArrayList<Task>();
    for (var module : modules) {
      for (var annotation : module.getAnnotationsByType(Task.Of.class)) {
        var namespace = annotation.namespace();
        var task =
            newTask(
                namespace.equals(Task.MODULE_NAME) ? module.getName() : namespace,
                annotation.name(),
                annotation.delimiter(),
                List.of(annotation.args()));
        tasks.add(task);
      }
    }
    return ToolFinder.of(tasks);
  }

  static ToolFinder composeToolFinder(ToolFinder... finders) {
    if (finders.length == 0) return emptyToolFinder();
    if (finders.length == 1) return finders[0];
    return new CompositeToolFinder(List.of(finders));
  }

  static ToolFinder composeToolFinder(List<ToolFinder> finders) {
    if (finders.isEmpty()) return emptyToolFinder();
    if (finders.size() == 1) return finders.get(0);
    return new CompositeToolFinder(List.copyOf(finders));
  }

  static ToolFinder emptyToolFinder() {
    return EmptyToolFinder.INSTANCE;
  }

  private Internal() {}
}
