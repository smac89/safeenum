package com.github.smac89.process;

import com.github.smac89.safeenum.SafeEnum;
import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// https://theholyjava.wordpress.com/2011/09/07/practical-introduction-into-code-injection-with-aspectj-javassist-and-java-proxy/
// http://hannesdorfmann.com/annotation-processing/annotationprocessing101
@AutoService(Processor.class)
public class SafeEnumProcessor extends AbstractProcessor {
    private Messager messager;
    private Filer filer;
    private Elements elementUtil;
    private Map<String, List<TypeElement>> enumsByPackage = new LinkedHashMap<String, List<TypeElement>>();

    private static final String ENUM_ASPECT_NAME_FORMAT = "EnumSafe%sInjector";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
        filer = processingEnv.getFiler();
        elementUtil = processingEnv.getElementUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton(SafeEnum.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(SafeEnum.class)) {
            if (element.getKind() != ElementKind.ENUM) {
                error(element, "Only enums can be annotated with @{0}",
                      SafeEnum.class.getSimpleName());
                return true;
            }

            if (!isValidSafeEnum(element)) {
                error(element, "The enum does not contain a valid safe type.");
                return true;
            }

            String packageName = elementUtil.getPackageOf(element).getQualifiedName().toString();
            List<TypeElement> elements = enumsByPackage.get(packageName);
            if (elements == null) {
                elements = new ArrayList<TypeElement>();
                enumsByPackage.put(packageName, elements);
            }
            elements.add(TypeElement.class.cast(element));
        }

        for (Map.Entry<String, List<TypeElement>> entry : enumsByPackage.entrySet()) {
            String packageName = entry.getKey();
            List<TypeElement> packageSafeEnums = entry.getValue();
            try {
                writeSafeEnumAspect(packageName, packageSafeEnums);
            } catch (IOException ioe) {
                error(packageSafeEnums.get(0), ioe.getMessage());
            }
        }

        enumsByPackage.clear();
        return true;
    }

    private boolean isValidSafeEnum(Element enumClass) {
        String safeName = enumClass.getAnnotation(SafeEnum.class).safeName();
        if (safeName.isEmpty()) {
            safeName = "UNKNOWN";
        }

        for (Element element: enumClass.getEnclosedElements()) {
            if (element.getKind() == ElementKind.ENUM_CONSTANT) {
                String enumName = element.getSimpleName().toString();
                if (enumName.equals(safeName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void writeSafeEnumAspect(String packageName, List<TypeElement> enumsInPackage) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (TypeElement enumType: enumsInPackage) {
            builder.append(enumType.getSimpleName().charAt(0));
        }
        String className = String.format(ENUM_ASPECT_NAME_FORMAT, builder.toString());

        JavaFileObject aspectFile = filer.createSourceFile(packageName.isEmpty() ? className : packageName + "." + className,
                                                           enumsInPackage.toArray(new TypeElement[0]));
        PrintWriter writer = new PrintWriter(aspectFile.openWriter());

        String qualifiedClassName = packageName.isEmpty() ? className : packageName + "." + className;
        try {
            if (!packageName.isEmpty()) {
                writer.printf("package %s;\n\n",packageName);
            }

            writer.println("@org.aspectj.lang.annotation.Aspect");
            writer.printf("public class %s {\n", className);

            for (TypeElement enumType: enumsInPackage) {
                writer.println();
                writeElementAspect(qualifiedClassName, enumType, writer);
            }

            writer.println("}");
        } finally {
            writer.close();
        }
    }

    private void error(Element element, String message, Object...args) {
        messager.printMessage(Diagnostic.Kind.ERROR, MessageFormat.format(message, args), element);
    }

    // https://blog.espenberntsen.net/2010/03/20/aspectj-cheat-sheet/
    // https://blog.jayway.com/2015/09/08/defining-pointcuts-by-annotations/
    private void writeElementAspect(String className, TypeElement enumType, PrintWriter writer) {
        SafeEnum safeEnum = enumType.getAnnotation(SafeEnum.class);
        String defaultSafeValue = safeEnum.safeName().isEmpty() ? "UNKNOWN" : safeEnum.safeName();

        String qualifiedName = enumType.getQualifiedName().toString();
        String qualifiedMethodName = enumType.getQualifiedName().toString().replace('.', '_');

        writer.printf("  @org.aspectj.lang.annotation.Pointcut(\"!within(%s) && " +
                              "call(* %s.valueOf(String)) && args(name)\")",
                      className, qualifiedName);
        writer.println();

        String pointCutName = String.format("call%sValueOf", qualifiedMethodName);
        writer.printf("  public void %s(String name) { }\n\n", pointCutName);

        writer.printf("  @org.aspectj.lang.annotation.Around(\"%s(name)\")\n", pointCutName);
        writer.printf("  public %s safe%sValueOf(String name) {\n", qualifiedName, qualifiedMethodName);
        writer.println("    try {");
        writer.printf("      return %s.valueOf(name);", enumType.getQualifiedName());
        writer.println("    }");
        writer.println("    catch(java.lang.IllegalArgumentException ile) {");
        writer.printf("      return %s.%s;\n", enumType.getQualifiedName(), defaultSafeValue);
        writer.println("    }");

        writer.println("  }");
    }
}
