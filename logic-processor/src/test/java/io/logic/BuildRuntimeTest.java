package io.logic;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.immutables.metainf.internal.$processor$.$Processor;
import org.immutables.value.processor.Processor;
import org.junit.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * A collection of tests to verify the runtime is built correctly.
 *
 * @author Ian Caffey
 * @since 1.0
 */
public class BuildRuntimeTest {
    @Test
    public void testGenerateRuntime() {
        Compilation compilation = javac()
                .withProcessors(new LogicProcessor(), new Processor(), new $Processor())
                .withOptions("-A" + LogicProcessor.INCLUDE_RUNTIME_KEY + "=true")
                .compile(JavaFileObjects.forResource("io/logic/package-info.java"));
        assertThat(compilation).succeeded();
    }
}
