package com.lrz.processor;

import com.google.auto.service.AutoService;
import com.lrz.multi.Interface.IMultiClassData;
import com.lrz.multi.Interface.IMultiData;
import com.lrz.multi.MultiArrayList;
import com.lrz.multi.MultiData;
import com.lrz.multi.MultiDataManager;
import com.lrz.multi.MultiDataUtil;
import com.lrz.multi.MultiHashMap;
import com.lrz.multi.MultiHashSet;
import com.lrz.multi.MultiLinkedList;
import com.lrz.multi.MultiTreeMap;
import com.lrz.multi.MultiTreeSet;
import com.lrz.multi.annotation.Get;
import com.lrz.multi.annotation.Table;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.sun.jdi.ClassType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
            if (element.getKind() == ElementKind.INTERFACE || element.getKind() == ElementKind.CLASS) {
                Table table = element.getAnnotation(Table.class);
                TypeElement typeElement = (TypeElement) element;
                List<? extends Element> members = elementUtils.getAllMembers(typeElement);
                //字段名和字段实体
                HashMap<String, FieldSpec> fieldSpecHashMap = new HashMap<>();
                //字段名和默认值
                HashMap<String, Object> fieldValues = new HashMap<>();
                ArrayList<MethodSpec> methodSpecs = new ArrayList<>();
                //set方法集合
                HashMap<String, MethodSpec[]> methodSpecsGetAndSet = new HashMap<>();
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
                        boolean isDataClass = false;
                        if (get != null && get.name() != null) {
                            if (method.getReturnType().getKind() != TypeKind.NULL && method.getReturnType().getKind() != TypeKind.NONE && method.getReturnType().getKind() != TypeKind.VOID) {
                                //判断如果是null 则从缓存中加载
                                String type = method.getReturnType().toString().split("<")[0];

                                for (Element el : elements) {
                                    if (el instanceof TypeElement) {
                                        if (((TypeElement) el).getQualifiedName().toString().equals(type)) {
                                            isDataClass = true;
                                            break;
                                        }
                                    }
                                }
                                if (isDataClass) {
                                    builder.addCode("return " + "$T.DATA.get(" + type + ".class);", MultiData.class);
                                } else {
                                    builder.addStatement("return " + get.name());
                                }
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
                            if (!isDataClass) {
                                fieldSpecHashMap.put(get.name(), buildField(ClassName.get(method.getReturnType()), get.name(), value));
                                fieldValues.put(get.name(), value);
                            }
                        }

                        List<VariableElement> params = (List<VariableElement>) method.getParameters();
                        if (set != null && set.name() != null && !params.isEmpty()) {
                            VariableElement variableElement = params.get(0);
//                            fieldSpecHashMap.put(set.name(), buildField(ClassName.get(variableElement.asType()), set.name()));
                            //设置set方法的方法体
                            builder.addParameter(ClassName.get(variableElement.asType()), variableElement.getSimpleName().toString());

                            String type = variableElement.asType().toString().split("<")[0];
                            TypeElement dataElement = null;
                            for (Element el : elements) {
                                if (el instanceof TypeElement) {
                                    if (((TypeElement) el).getQualifiedName().toString().equals(type)) {
                                        isDataClass = true;
                                        dataElement = (TypeElement) el;
                                        break;
                                    }
                                }
                            }
                            // 参数类型如果是集合，则尝试给其寻找自定义实现的子类
                            Class sonClass = null;
                            String addMethod = null;//操作子类添加的方法。例如：putAll,addAll;
                            //添加保存到磁盘的方法
                            if (dataElement != null && dataElement.getKind() == ElementKind.CLASS) {
                                //获取该类下所有方法
                                List<? extends Element> mem = elementUtils.getAllMembers(dataElement);
                                HashMap<String, ExecutableElement> setMethods = new HashMap<>();
                                HashMap<String, ExecutableElement> getMethods = new HashMap<>();
                                for (Element m : mem) {
                                    if (m.getKind() == ElementKind.METHOD) {
                                        com.lrz.multi.annotation.Set setM = ((ExecutableElement) m).getAnnotation(com.lrz.multi.annotation.Set.class);
                                        if (setM != null) {
                                            setMethods.put(setM.name(), (ExecutableElement) m);
                                        }

                                        Get getM = ((ExecutableElement) m).getAnnotation(com.lrz.multi.annotation.Get.class);
                                        if (getM != null) {
                                            getMethods.put(getM.name(), (ExecutableElement) m);
                                        }
                                    }
                                }
                                // 遍历添加方法操作
                                builder.addCode("if (" +
                                        variableElement.getSimpleName().toString() + " == null" +
                                        "){\n" +
                                        variableElement.getSimpleName().toString() + " = new " + type + "Imp();" +
                                        "\n}");
                                for (Map.Entry<String, ExecutableElement> entry : getMethods.entrySet()) {
                                    ExecutableElement setMethod = setMethods.get(entry.getKey());
                                    if (setMethod != null) {
                                        builder.addCode("$T.DATA.get(" +
                                                type + ".class" +
                                                ")." + setMethod.getSimpleName() + "(" +
                                                variableElement.getSimpleName().toString() + "." + entry.getValue() +
                                                ");", MultiData.class);
                                    }
                                }
                            } else if (!isBasic(type)) {
                                String value = null;
                                if (isDataClass) {
                                    value = "new " + type + "Imp()";
                                } else {
                                    try {
                                        Class typeClass = Class.forName(type);
                                        if (typeClass == String.class) {
                                            value = "\"\"";
                                        } else if (!typeClass.isInterface() && !java.lang.reflect.Modifier.isAbstract(typeClass.getModifiers())) {
                                            if (typeClass == HashMap.class) {
                                                sonClass = MultiHashMap.class;
                                                addMethod = "putAll";
                                            } else if (typeClass == ArrayList.class) {
                                                sonClass = MultiArrayList.class;
                                                addMethod = "addAll";
                                            } else if (typeClass == LinkedList.class) {
                                                sonClass = MultiLinkedList.class;
                                                addMethod = "addAll";
                                            } else if (typeClass == HashSet.class) {
                                                sonClass = MultiHashSet.class;
                                                addMethod = "addAll";
                                            } else if (typeClass == TreeSet.class) {
                                                sonClass = MultiTreeSet.class;
                                                addMethod = "addAll";
                                            } else if (typeClass == TreeMap.class) {
                                                sonClass = MultiTreeMap.class;
                                                addMethod = "putAll";
                                            }
                                            if (sonClass != null) {
                                                value = "new " + sonClass.getName() + "(\"" + table.name() + "\"," + "\"" + set.name() + "\")";
                                            } else {
                                                value = "new " + typeClass.getName() + "()";
                                            }

                                        } else if (List.class.isAssignableFrom(typeClass)) {
                                            sonClass = MultiArrayList.class;
                                            addMethod = "addAll";
                                            value = "new " + MultiArrayList.class.getName() + "(\"" + table.name() + "\"," + "\"" + set.name() + "\")";
                                        } else if (Map.class.isAssignableFrom(typeClass)) {
                                            sonClass = MultiHashMap.class;
                                            addMethod = "putAll";
                                            value = "new " + MultiHashMap.class.getName() + "(\"" + table.name() + "\"," + "\"" + set.name() + "\")";
                                        } else if (Set.class.isAssignableFrom(typeClass)) {
                                            sonClass = MultiTreeSet.class;
                                            addMethod = "addAll";
                                            value = "new " + MultiTreeSet.class.getName() + "(\"" + table.name() + "\"," + "\"" + set.name() + "\")";
                                        }
                                    } catch (Exception e1) {
                                        if (e1 instanceof ClassNotFoundException) {
                                            //说明是自定义的class
                                            value = "new " + type + "()";
                                        } else {
                                            e1.printStackTrace();
                                        }
                                    }
                                }
                                builder.addCode("if (" + variableElement.getSimpleName().toString() + " == " + "this." + set.name() + "){ return;}");
                                builder.addCode("if (" +
                                        variableElement.getSimpleName().toString() + " == null" +
                                        "){\n" +
                                        variableElement.getSimpleName().toString() + " = " + value + ";" +
                                        "\n}\n");
                            }
                            if (!isDataClass) {
                                if (sonClass != null) {
                                    builder.addStatement("this." + set.name() + ".clear()");
                                    builder.addStatement("this." + set.name() + "." + addMethod + "(" + params.get(0).getSimpleName().toString() + ")");
                                } else {
                                    builder.addStatement("this." + set.name() + " = " + params.get(0).getSimpleName().toString());
                                }
                            }
                            if (!isDataClass && sonClass == null) {
                                builder.addCode("$T.MANAGER.getInnerDataListener().onSave($S,$S," + variableElement.getSimpleName().toString() + ");"
                                        , MultiDataManager.class,
                                        table.name(), set.name());
                            }
                        }
                        MethodSpec m = builder.build();
                        String key = null;
                        if (get != null) {
                            key = get.name();
                            methodSpecs.add(m);
                        } else if (set != null) {
                            key = set.name();
                            methodSpecs.add(m);
                        }
                        if (key != null) {
                            if (!methodSpecsGetAndSet.containsKey(key)) {
                                MethodSpec[] ms = new MethodSpec[2];
                                methodSpecsGetAndSet.put(key, ms);
                            }
                            if (get != null) {
                                methodSpecsGetAndSet.get(key)[0] = m;
                            } else {
                                methodSpecsGetAndSet.get(key)[1] = m;
                            }
                        }

                    }
                }
                TypeSpec.Builder typeSpec = TypeSpec.classBuilder(element.getSimpleName() + "Imp")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(IMultiData.class);
                if (element.getKind() == ElementKind.CLASS) {
                    typeSpec.superclass(ClassName.get(typeElement));
                    typeSpec.addSuperinterface(ParameterizedTypeName.get(ClassName.get(IMultiClassData.class), ClassName.get(typeElement)));
                    CodeBlock.Builder initCode = CodeBlock.builder();
                    for (Map.Entry<String, Object> fieldValue : fieldValues.entrySet()) {
                        initCode.addStatement(fieldValue.getKey() + " = " + fieldValue.getValue());
                    }
                    typeSpec.addInitializerBlock(initCode.build());

                    MethodSpec.Builder saveBySuper = MethodSpec.methodBuilder("saveByObj")
                            .addModifiers(Modifier.PUBLIC)
                            .addAnnotation(Override.class)
                            .addParameter(ClassName.get(typeElement), "instance")
                            .returns(TypeName.VOID);
                    for (Map.Entry<String, MethodSpec[]> entry : methodSpecsGetAndSet.entrySet()) {
                        saveBySuper.addCode(entry.getValue()[1].name + "(instance." + entry.getValue()[0].name + "());");
                    }

                    methodSpecs.add(saveBySuper.build());
                } else {
                    typeSpec.addSuperinterface(ClassName.get(typeElement));
                    for (FieldSpec fieldSpec : fieldSpecHashMap.values()) {
                        typeSpec.addField(fieldSpec);
                    }
                }
                MethodSpec.Builder saveMulti = MethodSpec.methodBuilder("saveMulti")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .returns(TypeName.VOID);
                MethodSpec.Builder loadMulti = MethodSpec.methodBuilder("loadMulti")
                        .addModifiers(Modifier.PUBLIC)
                        .addAnnotation(Override.class)
                        .addParameter(boolean.class, "force")
                        .returns(TypeName.VOID);
                for (Map.Entry<String, FieldSpec> fieldSpecs : fieldSpecHashMap.entrySet()) {
                    saveMulti.addCode("$T.MANAGER.getInnerDataListener().onSave($S,$S," + fieldSpecs.getValue().name + ");"
                            , MultiDataManager.class,
                            table.name(), fieldSpecs.getKey());
                    String[] typeAndPar = fieldSpecs.getValue().type.toString().split("<");
                    String type = typeAndPar[0];
                    //范型的描述
                    String par = "";
                    if (typeAndPar.length > 1) {
                        par = "<" + fieldSpecs.getValue().type.toString().split("<")[1];
                    }
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
                        loadMulti.addCode("if (force) {\n");
                        loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S,new " + type + "Imp()" + ");"
                                , MultiDataManager.class,
                                table.name(), fieldSpecs.getKey());
                        loadMulti.addCode("\n}\n");
                    } else if (isBasic(type)) {
                        loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S," + fieldSpecs.getValue().name + ");"
                                , MultiDataManager.class,
                                table.name(), fieldSpecs.getKey());
                    } else {
                        String addOrPut = ".addAll(";
                        try {
                            Class typeClass = Class.forName(type);
                            if (typeClass == String.class) {
                                loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S," + fieldSpecs.getValue().name + "==null?\"\":" + fieldSpecs.getValue().name + ");"
                                        , MultiDataManager.class,
                                        table.name(), fieldSpecs.getKey());
                            } else if (!typeClass.isInterface() && !java.lang.reflect.Modifier.isAbstract(typeClass.getModifiers())) {
                                Class sonClass = typeClass;
                                if (typeClass == HashMap.class) {
                                    sonClass = MultiHashMap.class;
                                    addOrPut = ".putAll(";
                                } else if (typeClass == ArrayList.class) {
                                    sonClass = MultiArrayList.class;
                                } else if (typeClass == LinkedList.class) {
                                    sonClass = MultiLinkedList.class;
                                } else if (typeClass == HashSet.class) {
                                    sonClass = MultiHashSet.class;
                                } else if (typeClass == TreeSet.class) {
                                    sonClass = MultiTreeSet.class;
                                } else if (typeClass == TreeMap.class) {
                                    sonClass = MultiTreeMap.class;
                                    addOrPut = ".putAll(";
                                }
                                if (sonClass == typeClass) {
                                    loadMulti.addCode(fieldSpecs.getValue().name + " = $T.MANAGER.getInnerDataListener().onLoad($S,$S," + "new $T()" + ");"
                                            , MultiDataManager.class,
                                            table.name(), fieldSpecs.getKey(), sonClass);
                                } else {
                                    if (sonClass == MultiHashMap.class || sonClass == MultiTreeMap.class) {
                                        par = par.replace("java.lang.String,", "");
                                    }
                                    loadMulti.addCode(fieldSpecs.getValue().name + " = new $T($S,$S);", sonClass, table.name(), fieldSpecs.getKey());

                                    loadMulti.addCode(fieldSpecs.getValue().name + addOrPut + "$T.MANAGER.getInnerDataListener().onLoad($S,$S," + "new $T" + par + "($S,$S){}" + "));"
                                            , MultiDataManager.class,
                                            table.name(), fieldSpecs.getKey(), sonClass, table.name(), fieldSpecs.getKey());
                                }


                            } else if (List.class.isAssignableFrom(typeClass)) {
                                loadMulti.addCode(fieldSpecs.getValue().name + " = new $T($S,$S);", MultiArrayList.class, table.name(), fieldSpecs.getKey());
                                loadMulti.addCode(fieldSpecs.getValue().name + addOrPut + "$T.MANAGER.getInnerDataListener().onLoad($S,$S,new $T" + par + "($S,$S){}));"
                                        , MultiDataManager.class,
                                        table.name(), fieldSpecs.getKey(), MultiArrayList.class, table.name(), fieldSpecs.getKey());
                            } else if (Map.class.isAssignableFrom(typeClass)) {
                                addOrPut = ".putAll(";
                                loadMulti.addCode(fieldSpecs.getValue().name + " = new $T($S,$S);", MultiHashMap.class, table.name(), fieldSpecs.getKey());
                                loadMulti.addCode(fieldSpecs.getValue().name + addOrPut + "$T.MANAGER.getInnerDataListener().onLoad($S,$S,new $T" + par.replace("java.lang.String,", "") + "($S,$S){}));"
                                        , MultiDataManager.class,
                                        table.name(), fieldSpecs.getKey(), MultiHashMap.class, table.name(), fieldSpecs.getKey());
                            } else if (Set.class.isAssignableFrom(typeClass)) {
                                loadMulti.addCode(fieldSpecs.getValue().name + " = new $T($S,$S);", MultiTreeSet.class, table.name(), fieldSpecs.getKey());
                                loadMulti.addCode(fieldSpecs.getValue().name + addOrPut + "$T.MANAGER.getInnerDataListener().onLoad($S,$S,new $T" + par + "($S,$S){}));"
                                        , MultiDataManager.class,
                                        table.name(), fieldSpecs.getKey(), MultiTreeSet.class, table.name(), fieldSpecs.getKey());
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
            System.out.println("===生成类映射====》" + entry.getValue());
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