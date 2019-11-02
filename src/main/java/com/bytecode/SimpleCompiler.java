package com.bytecode;


import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
 * SimpleCompiler : run time compiler used by PlanckDB high level frameworks;
 */
class SimpleCompiler {

    private class PlanckDBFileObject extends SimpleJavaFileObject {

        private final CharSequence source;

        PlanckDBFileObject(String className, Kind kind, CharSequence source) throws URISyntaxException {
            super(new URI(null, null, className.replace('.', '/') + "." + (kind == Kind.CLASS ? "class" : "java"), null), kind);
            this.source = source;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            return source == null ? super.getCharContent(ignoreEncodingErrors) : source;
        }

        @Override
        public OutputStream openOutputStream() throws IOException {
            return out;
        }

    }

    private class MyManager extends ForwardingJavaFileManager<StandardJavaFileManager> {

        public MyManager(JavaCompiler compiler) {
            super(compiler.getStandardFileManager(null, null, null));
        }

        @Override
        public javax.tools.JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            try {
                return new PlanckDBFileObject(className, kind, null);
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

    }

    static byte[] compile(String className, CharSequence source) {
        return new SimpleCompiler(className, source).compile();
    }

    private final String className;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    private final CharSequence source;

    private SimpleCompiler(String className, CharSequence source) {
        this.className = className;
        this.source = source;
    }

    private byte[] compile() {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (MyManager manager = new MyManager(compiler)) {
            Boolean success = compiler.getTask(null, manager, null, null, null, Arrays.asList(new PlanckDBFileObject(className, JavaFileObject.Kind.SOURCE, source))).call();
            if (!success) {
                throw new Error("fail to compile class " + className);
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Could not compile " + className, e);
        }
        // do nothing ....
    }

}
