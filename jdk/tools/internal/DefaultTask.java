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

package jdk.tools.internal;

import java.util.List;
import jdk.tools.Command;
import jdk.tools.Task;

/**
 * Represents an ordered collection of command instances.
 *
 * @param namespace the namespace of this task
 * @param name the name of this task
 * @param commands the list of command instances to execute
 */
public record DefaultTask(String namespace, String name, List<Command> commands) implements Task {
  public DefaultTask {
    if (namespace == null) throw new IllegalArgumentException("namespace must not be null");
    if (name.isBlank()) throw new IllegalArgumentException("name must not be blank");
    if (commands == null) throw new IllegalArgumentException("commands must not be null");
  }
}
