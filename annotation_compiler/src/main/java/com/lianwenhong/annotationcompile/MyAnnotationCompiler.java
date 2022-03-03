package com.lianwenhong.annotationcompile;

import com.google.auto.service.AutoService;
import com.lianwenhong.annotation.BindView;
import com.lianwenhong.annotation.OnClick;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

@AutoService(Processor.class)
public class MyAnnotationCompiler extends AbstractProcessor {


    /**
     * 初始化
     *
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    /**
     * 开始解析注解
     *
     * @param set
     * @param roundEnvironment
     * @return
     */
    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        // 将每个注解以所属的类为维度进行归类，Map意味着每个TypeElement（类）下对应的所有的BindView和OnClick注解
        Map<TypeElement, Elements4Type> allElements = classifyElementWithType(roundEnvironment);

        // 使用JavaPoet方式注入.java文件
        SourceGenerator generator = SourceGenerator.getGenerator(GeneratorType.JAVAPOET_GENERATOR, processingEnv);
        // 使用字符串拼接方式注入.java文件
//        SourceGenerator generator = SourceGenerator.getGenerator(GeneratorType.STRING_GENERATOR, processingEnv);
        generator.process(roundEnvironment, allElements);
        return false;
    }

    /**
     * 对注解以类为维度进行归类
     *
     * @param roundEnvironment
     * @return
     */
    private Map<TypeElement, Elements4Type> classifyElementWithType(RoundEnvironment roundEnvironment) {
        Map<TypeElement, Elements4Type> allElements = new HashMap<>();

        Set<? extends Element> bindElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        Set<? extends Element> clickElements = roundEnvironment.getElementsAnnotatedWith(OnClick.class);

        for (Element viewElement : bindElements) {
            VariableElement temp = (VariableElement) viewElement;
            // 获取类节点
            TypeElement belongType = (TypeElement) temp.getEnclosingElement();
            if (!allElements.containsKey(belongType)) {
                Elements4Type item = new Elements4Type();
                item.addViewElement(temp);
                allElements.put(belongType, item);
            } else {
                Elements4Type item = allElements.get(belongType);
                item.addViewElement(temp);
            }
        }

        for (Element methodElement : clickElements) {
            ExecutableElement temp = (ExecutableElement) methodElement;
            // 获取类节点
            TypeElement belongType = (TypeElement) temp.getEnclosingElement();
            if (!allElements.containsKey(belongType)) {
                Elements4Type item = new Elements4Type();
                item.addMethodElement(temp);
                allElements.put(belongType, item);
            } else {
                Elements4Type item = allElements.get(belongType);
                item.addMethodElement(temp);
            }
        }

        return allElements;
    }


    /**
     * 注解解析器支持的java版本
     *
     * @return
     */
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return processingEnv.getSourceVersion();
    }

    /**
     * 注解解析器支持的注解类型
     *
     * @return
     */
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getCanonicalName());
        types.add(OnClick.class.getCanonicalName());
        return types;
    }

}