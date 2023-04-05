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
import java.util.Optional;

/** An ordered and searchable collection of tool descriptors. */
public interface ToolFinder {
  static ToolFinder of(String... tools) {
    return Internal.newToolFinder(tools);
  }

  static ToolFinder of(Tool... tools) {
    return Internal.newToolFinder(tools);
  }

  static ToolFinder of(List<Tool> tools) {
    return Internal.newToolFinder(tools);
  }

  static ToolFinder compose(ToolFinder... finders) {
    return Internal.composeToolFinder(finders);
  }

  static ToolFinder compose(List<ToolFinder> finders) {
    return Internal.composeToolFinder(finders);
  }

  static ToolFinder empty() {
    return Internal.emptyToolFinder();
  }

  List<Tool> tools();

  default Optional<Tool> find(String string) {
    var tools = tools().stream();
    // "tool[@suffix]"
    var slash = string.lastIndexOf('/');
    if (slash == -1)
      return tools.filter(tool -> Internal.matchesToolName(tool, string)).findFirst();
    // "path/to/tool[@suffix]"
    var namespace = string.substring(0, slash);
    var name = string.substring(slash + 1);
    return tools
        .filter(tool -> tool.namespace().equals(namespace)) // "path/to"
        .filter(tool -> Internal.matchesToolName(tool, name)) // "tool[@suffix]"
        .findFirst();
  }
}
