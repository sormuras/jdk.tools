package jdk.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.nio.file.Path;

/** Knows how to create a tool finder by getting external tool assets into a local folder. */
public interface ToolInstaller {
  ToolFinder install(Path folder, String version) throws Exception;

  default String namespace() {
    return Internal.computeNamespace(getClass());
  }

  String name();

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.MODULE)
  @Repeatable(Setup.Container.class)
  @interface Setup {
    Class<? extends ToolInstaller> service();

    String version();

    /** Repeatable annotation collector. */
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.MODULE)
    @interface Container {
      Setup[] value();
    }
  }
}
