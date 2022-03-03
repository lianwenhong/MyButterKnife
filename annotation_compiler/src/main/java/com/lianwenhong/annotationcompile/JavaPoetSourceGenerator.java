package com.lianwenhong.annotationcompile;

import com.lianwenhong.annotation.BindView;
import com.lianwenhong.annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * class           相关类介绍说明
 * JavaFile        用于构造输出包含一个顶级类的Java文件
 * TypeSpec        生成类，接口，或者枚举
 * MethodSpec      生成构造函数或方法
 * FieldSpec       生成成员变量或字段
 * ParameterSpec   用来创建参数
 * AnnotationSpec  用来创建注解
 * <p>
 * 常用api:
 * addStatement()                           方法负责分号和换行
 * beginControlFlow() + endControlFlow()    需要一起使用，提供换行符和缩进。
 * addCode()                                以字符串的形式添加内
 * returns                                  添加返回值类型
 * constructorBuilder()                     生成构造器函数
 * addAnnotation                            添加注解
 * addSuperinterface                        给类添加实现的接口
 * superclass                               给类添加继承的父类
 * ClassName.bestGuess（“类全名称”）          返回ClassName对象，这里的类全名称表示的类必须要存在，会自动导入相应的包
 * ClassName.get(“包名”，”类名”)              返回ClassName对象，不检查该类是否存在
 * TypeSpec.interfaceBuilder(“HelloWorld”)  生成一个HelloWorld接口
 * MethodSpec.constructorBuilder()          构造器
 * addTypeVariable(TypeVariableName.get(“T”, typeClassName))    会给生成的类加上泛型
 */
public class JavaPoetSourceGenerator extends SourceGenerator {
    public JavaPoetSourceGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public void process(RoundEnvironment roundEnvironment, Map<TypeElement, Elements4Type> allElements) {
        printLog("MyButterKnife开始解析注解");

        if (allElements.size() <= 0) {
            return;
        }

        for (TypeElement oneType : allElements.keySet()) {
            String activityName = oneType.getSimpleName().toString();
            // 需要注入的类的类名
            String clzName = activityName + "$$ViewBinder";
            // 获取包名
            String pkgName = getPkgName(oneType);

            Elements4Type oneTypeElements = allElements.get(oneType);

            MethodSpec.Builder constructorMethod = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(oneType.asType()), "activity")
                    .addStatement("bindView(activity)");

            //MethodSpec  生成构造函数或方法    生成bindView得方法
            MethodSpec.Builder bindViewBuilder = MethodSpec.methodBuilder("bindView")
                    .addModifiers(Modifier.PUBLIC)//public  static
                    .addParameter(ClassName.get(oneType.asType()), "activity") //MainActivity activity
                    .returns(TypeName.VOID);//返回值  void


            for (VariableElement bindElem : oneTypeElements.getViewElements()) {
                //activity.textView = (android.widget.TextView)activity.findViewById(2131165325);
                String findStatement = String.format("activity.%s = (%s)activity.findViewById(%d)", bindElem.getSimpleName(), bindElem.asType(), bindElem.getAnnotation(BindView.class).value());
                bindViewBuilder.addStatement(findStatement);
            }

            for (ExecutableElement clickElem : oneTypeElements.getMethodElements()) {
                for (int resId : clickElem.getAnnotation(OnClick.class).value()) {
                    String clickStatement = String.format("activity.findViewById(%d).setOnClickListener(new android.view.View.OnClickListener() {public void onClick(android.view.View v) {" +
                            "activity.click(v);" +
                            "}})", resId);
                    bindViewBuilder.addStatement(clickStatement);
                }
            }

            //TypeSpec 生成类，接口，或者枚举   生成BindMainActivity类
            TypeSpec typeSpec = TypeSpec.classBuilder(clzName)
//                    .addSuperinterface(TypeVariableName.get("android.view.View.OnClickListener"))
//                    .superclass()//extends MainActivity
                    .addModifiers(Modifier.PUBLIC)//public final
                    .addMethod(constructorMethod.build())//添加构造方法
                    .addMethod(bindViewBuilder.build())//添加上面得方法
                    .build();
            //用于构造输出包含一个顶级类的Java文件
            JavaFile file = JavaFile.builder(pkgName, typeSpec).build();
            try {
                file.writeTo(processingEnv.getFiler());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
