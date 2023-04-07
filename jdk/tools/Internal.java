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

import java.util.List;
import java.util.spi.ToolProvider;
import java.util.stream.Stream;
import jdk.tools.internal.CompositeToolFinder;
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

  static Task newTask(String namespace, String name, Command first, Command... more) {
    if (more.length == 0) return new Task(namespace, name, List.of(first));
    return new Task(namespace, name, Stream.concat(Stream.of(first), Stream.of(more)).toList());
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

  static ToolFinder newToolFinder(List<Tool> tools) {
    if (tools.isEmpty()) return emptyToolFinder();
    if (tools.size() == 1) return tools.get(0);
    return new DefaultToolFinder(List.copyOf(tools));
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
