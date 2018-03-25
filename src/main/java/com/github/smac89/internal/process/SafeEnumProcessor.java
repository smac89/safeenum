package com.github.smac89.internal.process;

import com.github.smac89.safeenum.SafeEnum;
import com.github.smac89.safeenum.SafeName;
import com.github.smac89.internal.Constants;
import com.google.auto.service.AutoService;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.Set;

// https://theholyjava.wordpress.com/2011/09/07/practical-introduction-into-code-injection-with-aspectj-javassist-and-java-proxy/
// http://hannesdorfmann.com/annotation-processing/annotationprocessing101
@AutoService(Processor.class)
public class SafeEnumProcessor extends AbstractProcessor {
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        messager = processingEnv.getMessager();
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

            if (!hasSafeName(element)) {
                error(element, "The enum does not contain a valid safe name (@SafeName) constant.");
                return true;
            }
        }
        return true;
    }

    private static boolean hasSafeName(Element enumClass) {
        for (Element element: enumClass.getEnclosedElements()) {
            if (element.getKind() == ElementKind.ENUM_CONSTANT) {
                SafeName defaultName = element.getAnnotation(SafeName.class);
                if (defaultName != null) {
                    return true;
                }
                String enumName = element.getSimpleName().toString();
                if (enumName.equals(Constants.SAFE_NAME_DEFAULT)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void error(Element element, String message, Object...args) {
        messager.printMessage(Diagnostic.Kind.ERROR, MessageFormat.format(message, args), element);
    }
}
