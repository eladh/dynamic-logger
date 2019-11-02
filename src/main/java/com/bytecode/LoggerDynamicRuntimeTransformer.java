package com.bytecode;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class LoggerDynamicRuntimeTransformer implements ClassFileTransformer {

    private final String classToModify;
    private final String methodName;
    private final String logToInject;
    private final int lineToInjectInto;

    LoggerDynamicRuntimeTransformer(String classToModify, String methodName, String logToInject, int lineToInjectInto) {
        this.classToModify = classToModify;
        this.methodName = methodName;
        this.logToInject = logToInject;
        this.lineToInjectInto = lineToInjectInto;
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        byte[] bytesToReturn = classfileBuffer;
        if (className.replaceAll("/", ".").equals(classToModify)) {
            try {
                ClassPool classPool = ClassPool.getDefault();
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                CtMethod[] methods = ctClass.getDeclaredMethods();
                for (CtMethod method : methods) {
                    if (method.getName().equals(methodName)) {
                        method.insertAt(lineToInjectInto, logToInject);
                    }
                }
                bytesToReturn = ctClass.toBytecode();
                ctClass.detach();
            } catch (Throwable ex) {
                System.out.println("Error occurred while transforming class. " + ex.getMessage());
            }
        }
        return bytesToReturn;
    }

    private byte[] recompile(String className) {
        String source = "package com.bytecode;\n" +
                "\n" +
                "public class Example {\n" +
                "    public void doJob() {\n" +
                "        System.out.println(\"default code extended\");\n" +
                "    }\n" +
                "}\n";
        return SimpleCompiler.compile(className, source);
    }
}
