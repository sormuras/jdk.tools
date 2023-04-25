package jdk.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

/** This class consists exclusively of static methods that operate on tools. */
public class Tools {
  public static Map<String, List<Tool>> toSimpleNameMap(List<Tool> tools) {
    if (tools.isEmpty()) return Map.of();
    var tree = new TreeMap<String, List<Tool>>();
    for (var tool : tools) {
      var name = tool.name();
      var key = name.contains("@") ? name.substring(0, name.indexOf('@')) : name;
      tree.computeIfAbsent(key, __ -> new ArrayList<>()).add(tool);
    }
    return tree;
  }

  public static String toTextBlock(List<Tool> tools) {
    var lines = new StringJoiner("\n");
    for (var entry : Tools.toSimpleNameMap(tools).entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();
      var first = value.get(0);
      lines.add("%21s = %s".formatted(key, first.toNamespaceAndName()));
      value.stream().skip(1).forEach(tool -> lines.add("   -> " + tool.toNamespaceAndName()));
    }
    return lines.toString();
  }

  private Tools() {}
}
