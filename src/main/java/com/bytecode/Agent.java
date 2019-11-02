package com.bytecode;

import java.lang.instrument.Instrumentation;

public class Agent {
    static final String SEPARATOR = "@SEPARATOR@";

    public static void agentmain(String args, Instrumentation inst) {
        try {
            //className + SEPARATOR + methodName + SEPARATOR + logToInject + SEPARATOR + lineToInjectInto;
            String[] split = args.split(SEPARATOR);
            inst.addTransformer(new LoggerDynamicRuntimeTransformer(split[0], split[1], split[2], Integer.valueOf(split[3])), true);
            inst.retransformClasses(Class.forName(split[0]));
        } catch (Exception e) {
            System.out.println("Failed register transformer. " + e.getMessage());
        }
    }
}