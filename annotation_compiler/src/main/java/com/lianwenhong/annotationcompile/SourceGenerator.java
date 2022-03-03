package com.lianwenhong.annotationcompile;

import java.util.Map;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public abstract class SourceGenerator {

    protected ProcessingEnvironment processingEnv;

    protected SourceGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public static SourceGenerator getGenerator(GeneratorType type, ProcessingEnvironment processingEnv) {
        switch (type) {
            case STRING_GENERATOR:
                return new StringSourceGenerator(processingEnv);
            case JAVAPOET_GENERATOR:
            default:
                return new JavaPoetSourceGenerator(processingEnv);
        }
    }

    public abstract void process(RoundEnvironment roundEnvironment, Map<TypeElement, Elements4Type> allElements);

    /**
     * 日志打印
     *
     * @param msg
     */
    public void printLog(String msg) {
        Messager messager = processingEnv.getMessager();
        messager.printMessage(Diagnostic.Kind.NOTE, msg);
    }

    /**
     * 获取类的包名
     *
     * @param typeElement
     * @return
     */
    public String getPkgName(Element typeElement) {
        PackageElement packageOf = processingEnv.getElementUtils().getPackageOf(typeElement);
        return packageOf.getQualifiedName().toString();
    }
}
