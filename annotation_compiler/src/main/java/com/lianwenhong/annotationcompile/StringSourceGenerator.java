package com.lianwenhong.annotationcompile;

import com.lianwenhong.annotation.BindView;
import com.lianwenhong.annotation.OnClick;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

public class StringSourceGenerator extends SourceGenerator {

    // 生成文件的对象
    Filer filer;

    public StringSourceGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
        //初始化生成文件的对象
        filer = processingEnv.getFiler();
    }

    @Override
    public void process(RoundEnvironment roundEnvironment, Map<TypeElement, Elements4Type> allElements) {
        printLog("MyButterKnife开始解析注解");

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
}
