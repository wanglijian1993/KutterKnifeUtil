package com.wlj.complier;


import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.wlj.libannotations.BindView;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
public class ButterknifeProcessor extends AbstractProcessor {

    private Filer mFiler;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mFiler=processingEnv.getFiler();
        mElementUtils=processingEnv.getElementUtils();
        System.out.println("----init---");
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        //process f方法代表的是，有注解就都会进来，但是这里面是一团乱码
        Set<? extends Element> elements=roundEnv.getElementsAnnotatedWith(BindView.class);

        Map<Element, List<Element>> elementMap=new LinkedHashMap();
//        for(Element element:elements){
//            Element enclosingElement = element.getEnclosingElement();
//            System.out.println("---------"+element.getSimpleName().toString()+" "+enclosingElement.getSimpleName().toString());
//        }

        for(Element element:elements){
            Element enclosingElement = element.getEnclosingElement();
            List<Element> viewBindElements = elementMap.get(enclosingElement.getSimpleName());
            if(viewBindElements==null){
                viewBindElements=new ArrayList<>();
                elementMap.put(enclosingElement,viewBindElements);
            }
            viewBindElements.add(element);
        }
        //生成代码
        for (Map.Entry<Element, List<Element>> entry:elementMap.entrySet() ){

            Element enclosingElment = entry.getKey();
            List<Element> viewBindElements=entry.getValue();
            String activityClassNameStr=enclosingElment.getSimpleName().toString();
            ClassName activityClassName=ClassName.bestGuess(activityClassNameStr);
            ClassName unbinderClassName=ClassName.get("com.wlj.butterknife","Unbinder");

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(activityClassNameStr + "_ViewBinding")
                    .addModifiers(Modifier.FINAL, Modifier.PUBLIC).addSuperinterface(unbinderClassName)
                    .addField(activityClassName, "target", Modifier.PRIVATE);

            // 实现 unbind 方法
            // android.support.annotation.CallSuper
            ClassName callSuperClassName = ClassName.get("androidx.annotation","CallSuper");
            MethodSpec.Builder unbindMethodBuilder = MethodSpec.methodBuilder("unbind")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC,Modifier.FINAL)
                    .addAnnotation(callSuperClassName);

            unbindMethodBuilder.addStatement("$T target = this.target",activityClassName);
            unbindMethodBuilder.addStatement("if (target == null) throw new IllegalStateException(\"Bindings already cleared.\");");

            // 构造函数
            MethodSpec.Builder constructorMethodBuilder = MethodSpec.constructorBuilder()
                    .addParameter(activityClassName,"target");
            // this.target = target;
            constructorMethodBuilder.addStatement("this.target = target");
            // findViewById 属性
            for (Element viewBindElement : viewBindElements) {
                // target.textView1 = Utils.findRequiredViewAsType(source, R.id.tv1, "field 'textView1'", TextView.class);
                // target.textView1 = Utils.findViewById(source, R.id.tv1);
                String filedName = viewBindElement.getSimpleName().toString();
                ClassName utilsClassName = ClassName.get("com.wlj.butterknife","Utils");
                int resId = viewBindElement.getAnnotation(BindView.class).value();
                constructorMethodBuilder.addStatement("target.$L = $T.findViewById(target, $L)",filedName,utilsClassName,resId);
                // target.textView1 = null;
                unbindMethodBuilder.addStatement("target.$L = null",filedName);
            }


            classBuilder.addMethod(unbindMethodBuilder.build());
            classBuilder.addMethod(constructorMethodBuilder.build());

            // 生成类，看下效果
            try {
                String packageName = mElementUtils.getPackageOf(enclosingElment).getQualifiedName().toString();

                JavaFile.builder(packageName,classBuilder.build())
                        .addFileComment("butterknife 自动生成")
                        .build().writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("翻车了！");
            }
        }
        return false;
    }

    // 1. 指定处理的版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    // 2. 给到需要处理的注解
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        // 需要解析的自定义注解 BindView  OnClick
        annotations.add(BindView.class);
        return annotations;
    }



}