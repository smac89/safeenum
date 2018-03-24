package com.github.smac89.weave;

import org.aspectj.weaver.loadtime.WeavingURLClassLoader;
import org.aspectj.weaver.tools.WeavingAdaptor;
import org.junit.BeforeClass;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public abstract class TestWithAspectWeaving {
    private static final WeavingURLClassLoader weaver = new WeavingURLClassLoader(ClassLoader.getSystemClassLoader());
    private static final WeavingAdaptor adaptor = new WeavingAdaptor(weaver);

    @BeforeClass
    public static void weaveAspects() {
//        System.out.println("Finding all aspects!!");
//        Aj aj = new Aj();
//        aj.initialize();
//        aj.preProcess();
//        System.out.println(aj.getNamespace(weaver));
//        System.out.println(aj.generatedClassesExist(weaver));
//        try {
//            byte[] classBytes = loadClassFile("com.github.smac89.safeenum.EnumSafeTInjector");
//            byte[] aspectBytes = adaptor.weaveClass("com.github.smac89.safeenum.EnumSafeTInjector", classBytes);
//            weaver.acceptClass("com.github.smac89.safeenum.EnumSafeTInjector", classBytes, aspectBytes);
//        } catch (IOException e) {
//            e.printStackTrace(System.out);
//        }
    }

    private static byte[] loadClassFile(String qualifiedClassName) {
        String file = qualifiedNameToClassFile(qualifiedClassName);
        InputStream stream = weaver.getResourceAsStream(file);

        if (stream == null) {
            return new byte[]{};
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            byte[] data = new byte[4096];
            for (int nRead = stream.read(data, 0, data.length); nRead > 0;
                 nRead = stream.read(data, 0, data.length)) {
                buffer.write(data, 0, nRead);
            }
        } catch (IOException e) { }

        return buffer.toByteArray();
    }

    private static String qualifiedNameToClassFile(String qualifiedName) {
        return qualifiedName.replace('.', File.separatorChar) + ".class";
    }
}
