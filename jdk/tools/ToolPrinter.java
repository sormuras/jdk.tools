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
import java.lang.System.Logger.Level;

/** A wrapper for standard output streams. */
public record ToolPrinter(PrintWriter out, PrintWriter err, Level threshold) {
  public static ToolPrinter ofSystem() {
    var out = new PrintWriter(System.out, true);
    var err = new PrintWriter(System.err, true);
    return ToolPrinter.of(out, err);
  }

  public static ToolPrinter of(PrintWriter out, PrintWriter err) {
    return new ToolPrinter(out, err, Level.INFO);
  }

  public ToolPrinter withThreshold(Level threshold) {
    return new ToolPrinter(out, err, threshold);
  }

  public void debug(String message) {
    println(Level.DEBUG, message);
  }

  public void println(Level level, String message) {
    if (threshold == Level.OFF) return;
    if (threshold.getSeverity() > level.getSeverity()) return;
    var printer = level.getSeverity() >= Level.WARNING.getSeverity() ? err : out;
    printer.println(message);
  }
}
