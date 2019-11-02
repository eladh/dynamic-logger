package com.bytecode;


import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static com.bytecode.Agent.SEPARATOR;

public class DynamicLogger {

    private void injectLog(String className, String methodName, String logToInject, int lineToInjectInto) throws Exception {
        VirtualMachine vm = resolveVirtualMachine();
        try {
            String arg = className + SEPARATOR + methodName + SEPARATOR + logToInject + SEPARATOR + lineToInjectInto;
            loadAgent(vm, arg);
        } finally {
            vm.detach();
        }
    }

    private void loadAgent(VirtualMachine vm, String className) throws Exception {
        File temporaryAgentJar = createTemporaryAgentJar(Agent.class.getName(), null, true, true, false);
        vm.loadAgent(temporaryAgentJar.getAbsolutePath(), className);
    }

    private VirtualMachine resolveVirtualMachine() throws AttachNotSupportedException, IOException {
        String nameOfRunningVM = ManagementFactory.getRuntimeMXBean().getName();
        int p = nameOfRunningVM.indexOf('@');
        String pid = nameOfRunningVM.substring(0, p);
        return VirtualMachine.attach(pid);
    }

    private static File createTemporaryAgentJar(final String agentClass, final String bootClassPath,
                                                final boolean canRedefineClasses, final boolean canRetransformClasses,
                                                final boolean canSetNativeMethodPrefix) throws IOException {
        final File jarFile = File.createTempFile("javaagent." + agentClass, ".jar");
        jarFile.deleteOnExit();
        createAgentJar(new FileOutputStream(jarFile), agentClass, bootClassPath, canRedefineClasses,
                canRetransformClasses, canSetNativeMethodPrefix);
        return jarFile;
    }

    private static void createAgentJar(final OutputStream out, final String agentClass, final String bootClassPath,
                                       final boolean canRedefineClasses, final boolean canRetransformClasses,
                                       final boolean canSetNativeMethodPrefix) throws IOException {
        final Manifest man = new Manifest();
        man.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        man.getMainAttributes().putValue("Agent-Class", agentClass);
        if (bootClassPath != null) {
            man.getMainAttributes().putValue("Boot-Class-Path", bootClassPath);
        }
        man.getMainAttributes().putValue("Can-Redefine-Classes", Boolean.toString(canRedefineClasses));
        man.getMainAttributes().putValue("Can-Retransform-Classes", Boolean.toString(canRetransformClasses));
        man.getMainAttributes().putValue("Can-Set-Native-Method-Prefix", Boolean.toString(canSetNativeMethodPrefix));
        final JarOutputStream jarOut = new JarOutputStream(out, man);
        jarOut.flush();
        jarOut.close();
    }

    public static void main(String[] args) throws Exception {
        DynamicLogger dynamicLogger = new DynamicLogger();
        dynamicLogger.injectLog("Example", "doJob",
                "System.out.println(\"injected line!!!\");", 6);

        dynamicLogger.injectLog("Example", "doJob",
                "System.out.println(\"injected new line!!!\");", 6);
    }
}
