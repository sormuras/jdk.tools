package jdk.tools;

import java.nio.file.Path;

/** Knows how to create a tool finder by getting external tool assets into a local folder. */
public interface ToolInstaller {
  ToolFinder install(Path folder, String version) throws Exception;

  default String namespace() {
    return Internal.computeNamespace(getClass());
  }

  String name();
}
