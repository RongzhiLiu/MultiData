package com.lrz.processor;

import com.google.auto.service.AutoService;
import com.lrz.multi.Interface.IMultiData;
import com.lrz.multi.MultiDataManager;
import com.lrz.multi.MultiDataUtil;
import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Table;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;

@AutoService(Processor.class)
public class AnnotationProcessor extends AbstractProcessor {
    private Elements elementUtils;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return processTable(annotations, roundEnv);
    }

    private boolean processTable(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Table.class);
        if (elements.size() == 0) return false;

        HashMap<TypeElement, String> impClass = new HashMap<>();
        for (Element element : elements) {
            if (element.getKind() == ElementKind.INTERFACE) {
                Table table = element.getAnnotation(Table.class);
                TypeElement typeElement = (TypeElement) element;
                List<? extends Element> members = typeElement.getEnclosedElements();//elementUtils.getAllMembers(typeElement);
                HashMap<String, FieldSpec> fieldSpecHashMap = new HashMap<>();
                ArrayList<MethodSpec> methodSpecs = new ArrayList<>();
                for (Element e : members) {
                    if (e.getKind() == ElementKind.METHOD) {
                        ExecutableElement method = (ExecutableElement) e;
                        Get get = method.getAnnotation(Get.class);
                        com.lrz.multi.annotation.Set set = method.getAnnotation(com.lrz.multi.annotation.Set.class);
                        //实现方法
                        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                                .addModifiers(Modifier.PUBLIC)
                                .addAnnotation(Override.class)
                                .returns(ClassName.get(method.getReturnType()));
                        /**
                         * 通过方法的注解扫描变量
                         */
                        if (get != null && get.name() != null) {
                            if (method.getReturnType().getKind() != TypeKind.NULL && method.getReturnType().getKind() != TypeKind.NONE && method.getReturnType().getKind() != TypeKind.VOID) {
                                builder.addStatement("return " + get.name());
                            }
                            Object value = null;
                            if (method.getReturnType().toString().equals(boolean.class.getName()) || method.getReturnType().toString().equals(Boolean.class.getName())) {
                                value = get.defaultBoolean();
                            } else if (method.getReturnType().toString().equals(int.class.getName()) || method.getReturnType().toString().equals(Integer.class.getName())) {
                                value = get.defaultInt();
                            } else if (method.getReturnType().toString().equals(float.class.getName()) || method.getReturnType().toString().equals(Float.class.getName())) {
                                value = get.defaultFloat();
                            } else if (method.getReturnType().toString().equals(double.class.getName()) || method.getReturnType().toString().equals(Double.class.getName())) {
                                value = get.defaultDouble();
                            } else if (method.getReturnType().toString().equals(long.class.getName()) || method.getReturnType().toString().equals(Long.class.getName())) {
                                value = get.defaultLong();
                            } else if (method.getReturnType().toString().equals(String.class.getName())) {
                                value = '"' + get.defaultString() + '"';
                            }
                            fieldSpecHashMap.put(get.name(), buildField(ClassName.get(method.getReturnType()), get.name(), value));
                        }

                        List<VariableElement> params = (List<VariableElement>) method.getParameters();
                        if (set != null && set.name() != null && !params.isEmpty()) {
                            VariableElement variableElement = params.get(0);
//                            fieldSpecHashMap.put(set.name(), buildField(ClassName.get(variableElement.asType()), set.name()));
                            //设置set方法的方法体
                            builder.addParameter(ClassName.get(variableElement.asType()), variableElement.getSimpleName().toString());
                            builder.addStatement("this." + set.name() + " = " + params.get(0).getSimpleName().toString());
                            //添加保存到磁盘的方法
                            builder.addCode("$T.MANAGER.getInnerDataListener().onSave($S,$S," + variableElement.getSimpleName().toString() + ");"
                                    , MultiDataManager.class,
                                    table.name(), set.name());
                        }
                        methodSpecs.add(builder.build());
                    }
                }
                TypeSpec.Builder typeSpec = TypeSpec.classBuilder(element.getSimpleName() + "Imp")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(IMultiData.class)
//                        .addStaticBlock(CodeBlock.builder().addStatement("int i = 1").build())
//                        .addInitializerBlock(CodeBlock.builder().addStatement("int i = 1").build())
                        .addSuperinterface(ClassName.get(typeElement));
                for (FieldSpec fieldSpec : fieldSpecHashMap.values()) {
                    typeSpec.addField(fieldSpec);
                }
                MethodSpec.Builder saveMulti = MethodSpec.methodBuilder("saveMulti")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(TypeName.VOID);
                MethodSpec.Builder loadMulti = MethodSpec.methodBuilder("loadMulti")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(TypeName.VOID);
                for (Map.Entry<String, FieldSpec> fieldSpecs : fieldSpecHashMap.entrySet()) {
                    saveMulti.addCode("$T.MANAGER.getInnerDataListener().onSave($S,$S," + fieldSpecs.getValue().name + ");"
                            , MultiDataManager.class,
                            table.name(), fieldSpecs.getKey());
                    String type = fieldSpecs.getValue().type.toString().split("<")[0];
                    boolean isDataClass = false;
                    for (Element el : elements) {
                        if (el instanceof TypeElement) {
                            if (((TypeElement) el).getQualifiedName().toString().equals(type)) {
                                isDataClass = true;
                                break;
                            }
                        }
                    }
                    if (isDataClass) {
                        loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S,new " + type + "Imp()" + ");"
                                , MultiDataManager.class,
                                table.name(), fieldSpecs.getKey());
                    } else if (isBasic(type)) {
                        loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S," + fieldSpecs.getValue().name + ");"
                                , MultiDataManager.class,
                                table.name(), fieldSpecs.getKey());
                    } else {
                        try {
                            Class typeClass = Class.forName(type);
                            if (typeClass == String.class) {
                                loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S," + fieldSpecs.getValue().name + ");"
                                        , MultiDataManager.class,
                                        table.name(), fieldSpecs.getKey());
                            } else if (!typeClass.isInterface() && !java.lang.reflect.Modifier.isAbstract(typeClass.getModifiers())) {
                                loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S," + "new $T()" + ");"
                                        , MultiDataManager.class,
                                        table.name(), fieldSpecs.getKey(), typeClass);
                            } else if (List.class.isAssignableFrom(typeClass)) {
                                loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S,new $T<>());"
                                        , MultiDataManager.class,
                                        table.name(), fieldSpecs.getKey(), ArrayList.class);
                            } else if (Map.class.isAssignableFrom(typeClass)) {
                                loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S,new $T<>());"
                                        , MultiDataManager.class,
                                        table.name(), fieldSpecs.getKey(), HashMap.class);
                            } else if (Set.class.isAssignableFrom(typeClass)) {
                                loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S,new $T<>());"
                                        , MultiDataManager.class,
                                        table.name(), fieldSpecs.getKey(), HashSet.class);
                            }
                        } catch (Exception e) {
                            if (e instanceof ClassNotFoundException) {
                                //说明是自定义的class
                                loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S,new " + type + "()" + ");"
                                        , MultiDataManager.class,
                                        table.name(), fieldSpecs.getKey());
                            } else {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                MethodSpec.Builder tableName = MethodSpec.methodBuilder("tableName")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        .addCode("return $S;", table.name());

                MethodSpec.Builder isLazy = MethodSpec.methodBuilder("isLazy")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(boolean.class)
                        .addCode("return " + table.lazy() + ";");

                MethodSpec.Builder toString = MethodSpec.methodBuilder("toString")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(String.class)
                        .addCode("return $T.GSON.toJson(this);", MultiDataUtil.class);

                methodSpecs.add(saveMulti.build());
                methodSpecs.add(loadMulti.build());
                methodSpecs.add(tableName.build());
                methodSpecs.add(isLazy.build());
                methodSpecs.add(toString.build());

                for (MethodSpec methodSpec : methodSpecs) {
                    typeSpec.addMethod(methodSpec);
                }

                JavaFile javaFile = JavaFile.builder(getPackageName(typeElement), typeSpec.build())
                        .build();
                impClass.put(typeElement, getPackageName(typeElement) + "." + element.getSimpleName() + "Imp");
                try {
                    javaFile.writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        CodeBlock.Builder builder = CodeBlock.builder();
        for (Map.Entry<TypeElement, String> entry : impClass.entrySet()) {
            builder.addStatement("CLASSES.put(" + entry.getKey().getQualifiedName() + ".class" + "," + entry.getValue() + ".class)");
        }

        //将注册的类全部写到常量中
        FieldSpec fc = FieldSpec.builder(HashMap.class, "CLASSES", Modifier.PUBLIC, Modifier.FINAL, Modifier.STATIC).initializer("new HashMap()").build();
        TypeSpec.Builder typeSpec = TypeSpec.classBuilder("MultiConstants")
                .addField(fc)
                .addStaticBlock(builder.build())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        JavaFile constants = JavaFile.builder("com.lrz.multi.Interface", typeSpec.build())
                .build();
        try {
            constants.writeTo(processingEnv.getFiler());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private FieldSpec buildField(TypeName clazz, String name, Object defaultValue) {
        System.out.println("-----defaultValue-" + defaultValue);
        return FieldSpec.builder(clazz, name, Modifier.PUBLIC).initializer(defaultValue + "").build();

    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(Table.class.getName());
        set.add(com.lrz.multi.annotation.Set.class.getName());
        set.add(Get.class.getName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        elementUtils = processingEnv.getElementUtils();
    }

    private String getPackageName(TypeElement type) {
        return elementUtils.getPackageOf(type).getQualifiedName().toString();
    }


    public static boolean isBasic(String c) {
        return int.class.getName().equals(c) || Integer.class.getName().equals(c)
                || boolean.class.getName().equals(c) || Boolean.class.getName().equals(c)
                || float.class.getName().equals(c) || Float.class.getName().equals(c)
                || double.class.getName().equals(c) || Double.class.getName().equals(c)
                || long.class.getName().equals(c) || Long.class.getName().equals(c);
    }
}