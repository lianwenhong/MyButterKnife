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
    // 生成文件的对象
    Filer filer;

    /**
     * 初始化
     *
     * @param processingEnv
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        //初始化生成文件的对象
        filer = processingEnv.getFiler();
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

        printLog("MyButterKnife开始解析注解");

        // 将每个注解以所属的类为维度进行归类，Map意味着每个TypeElement（类）下对应的所有的BindView和OnClick注解
        Map<TypeElement, Elements4Type> allElements = classifyElementWithType(roundEnvironment);

        if (allElements.size() > 0) {
            for (TypeElement oneType : allElements.keySet()) {
                Elements4Type oneTypeElements = allElements.get(oneType);
                String activityName = oneType.getSimpleName().toString();
                // 需要注入的类的类名
                String clzName = activityName + "$$ViewBinder";
                // 获取包名
                String pkgName = getPkgName(oneType);

                // 生成java文件
                Writer writer = null;
                try {
                    JavaFileObject sourceFile = filer.createSourceFile(pkgName + "." + clzName);
                    writer = sourceFile.openWriter();
                    StringBuffer buffer = getSourceFileBuffer(pkgName, clzName, oneType, oneTypeElements);
                    writer.write(buffer.toString());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (writer != null)
                        try {
                            writer.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        }
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

        Set<? extends Element> viewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        Set<? extends Element> methodElements = roundEnvironment.getElementsAnnotatedWith(OnClick.class);

        for (Element viewElement : viewElements) {
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

        for (Element methodElement : methodElements) {
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
    private String getPkgName(Element typeElement) {
        PackageElement packageOf = processingEnv.getElementUtils().getPackageOf(typeElement);
        return packageOf.getQualifiedName().toString();
    }

    /**
     * 类拼装
     *
     * @param pkgName
     * @param clzName
     * @param typeElement
     * @param elements4Type
     * @return
     */
    private StringBuffer getSourceFileBuffer(String pkgName, String clzName, TypeElement typeElement, Elements4Type elements4Type) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("package ").append(pkgName).append(";\n");
        buffer.append("import android.view.View;\n");
        buffer.append("public class ").append(clzName).append("{\n");
        buffer.append("public ").append(clzName).append("(final ").append(typeElement.getQualifiedName()).append(" target").append("){\n");

        // 查找控件代码注入
        if (elements4Type != null && elements4Type.getViewElements() != null && elements4Type.getViewElements().size() > 0) {
            List<VariableElement> viewElements = elements4Type.getViewElements();
            for (VariableElement view : viewElements) {
                // 获取控件类型
                TypeMirror typeMirror = view.asType();
                // 获取控件名称
                String viewName = view.getSimpleName().toString();
                // 获取资源id
                int resId = view.getAnnotation(BindView.class).value();
                // 查找控件
                buffer.append("target.").append(viewName).append(" = (").append(typeMirror).append(")").append("target.findViewById(").append(resId).append(");\n");
            }
        }

        // 添加点击时间代码注入
        if (elements4Type != null && elements4Type.getMethodElements() != null && elements4Type.getMethodElements().size() > 0) {
            List<ExecutableElement> methodElements = elements4Type.getMethodElements();
            for (ExecutableElement method : methodElements) {
                // 获取方法名称
                String methodName = method.getSimpleName().toString();
                int[] resIds = method.getAnnotation(OnClick.class).value();
                for (int resId : resIds) {

                    buffer.append("target.findViewById(").append(resId).append(").setOnClickListener(new View.OnClickListener() {\n");
                    buffer.append("@Override\n");
                    buffer.append("public void onClick(View v){\n");
                    buffer.append("target.").append(methodName).append("(v);\n");
                    buffer.append("}\n});\n");
                }
            }
        }
        buffer.append("}\n}");
        return buffer;
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