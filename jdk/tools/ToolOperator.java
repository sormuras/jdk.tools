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
import java.util.Set;
import java.util.spi.ToolProvider;

/** A tool provider extension capable of running other tools. */
@FunctionalInterface
public interface ToolOperator extends Tool, ToolProvider {
  @Override
  default String name() {
    return Tool.super.name();
  }

  @Override
  default ToolProvider provider() {
    return this;
  }

  /** {@return names of required tools or an empty set indicating any tool name} */
  default Set<String> requires() {
    return Set.of();
  }

  @Override
  default int run(PrintWriter out, PrintWriter err, String... args) {
    throw new AssertionError();
  }

  int run(ToolRunner runner, PrintWriter out, PrintWriter err, String... args);
}
