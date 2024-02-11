package gltf.annotations;

import arc.func.*;
import arc.graphics.*;
import arc.math.geom.*;
import arc.struct.*;
import arc.util.*;
import arc.util.serialization.*;
import arc.util.serialization.Jval.*;
import com.squareup.javapoet.*;

import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.*;
import javax.tools.Diagnostic.*;
import java.io.*;
import java.util.*;

import static javax.lang.model.element.Modifier.*;

public class SpecProcessor implements Processor{
    public static final String packageName = "gltfrenzy.data";

    protected Elements elements;
    protected Filer filer;
    protected Messager messager;
    protected Types types;

    @Override
    public void init(ProcessingEnvironment processingEnv){
        elements = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        types = processingEnv.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv){
        for(var req : annotations){
            if(!types.isSameType(req.asType(), elements.getTypeElement(Spec.class.getName()).asType())) continue;
            for(var spec : roundEnv.getElementsAnnotatedWith(req)){
                var anno = spec.getAnnotation(Spec.class);
                var outType = ClassName.get(packageName, spec.getSimpleName().toString());

                if(spec.getKind() == ElementKind.ENUM){
                    var builder = TypeSpec
                        .enumBuilder(outType)
                        .addOriginatingElement(spec)
                        .addModifiers(PUBLIC);

                    var create = MethodSpec.methodBuilder("create")
                        .addModifiers(PUBLIC, STATIC)
                        .returns(outType)
                        .addParameter(ClassName.get(Jval.class), "json")
                        .addStatement("$T name = json.asString()", ClassName.get(String.class))
                        .beginControlFlow("return switch(name.toLowerCase())");

                    for(var field : spec.getEnclosedElements()){
                        if(field.getKind() != ElementKind.ENUM_CONSTANT) continue;

                        var name = field.getSimpleName().toString();
                        builder.addEnumConstant(name);

                        var alias = field.getAnnotation(Alias.class);
                        create.addStatement("case $S -> $T.$L", alias == null ? name : alias.value(), outType, name);
                    }

                    var type = builder
                        .addMethod(create
                            .addStatement("default -> throw new $T($S + name + $S)", ClassName.get(IllegalArgumentException.class), "Invalid discriminator `", "` for type `" + spec.getSimpleName() + "`.")
                            .endControlFlow("")
                        .build())
                        .build();

                    try{
                        JavaFile
                            .builder(packageName, type)
                            .indent("    ")
                            .skipJavaLangImports(true)
                            .build().writeTo(filer);
                    }catch(IOException e){
                        messager.printMessage(Kind.ERROR, "Failed to write implementation for '" + spec + "': " + Strings.getFinalMessage(e), spec);
                    }
                    continue;
                }

                var builder = TypeSpec.classBuilder(outType)
                    .addOriginatingElement(spec)
                    .addModifiers(PUBLIC, FINAL)
                    .addMethod(MethodSpec
                        .constructorBuilder()
                        .addModifiers(PRIVATE)
                        .addStatement("throw new $T()", ClassName.get(AssertionError.class))
                    .build())
                    .addField(FieldSpec
                        .builder(ClassName.get(Jval.class), "extensions", PUBLIC)
                        .addAnnotation(ClassName.get(Nullable.class))
                    .build())
                    .addField(FieldSpec
                        .builder(ClassName.get(Jval.class), "extras", PUBLIC)
                        .addAnnotation(ClassName.get(Nullable.class))
                    .build());

                boolean named = spec.getAnnotation(Named.class) != null;
                if(named){
                    builder.addField(FieldSpec
                        .builder(ClassName.get(String.class), "name", PUBLIC)
                        .addAnnotation(ClassName.get(Nullable.class))
                    .build());
                }

                var create = MethodSpec.methodBuilder("create")
                    .addModifiers(PUBLIC, STATIC)
                    .returns(outType)
                    .addParameter(ClassName.get(Jval.class), "json")
                    .addStatement("$T out = new $T()", outType, outType)
                    .addStatement("out.extensions = json.get($S)", "extensions")
                    .addStatement("out.extras = json.get($S)", "extras");

                if(named) create.addStatement("out.name = json.getString($S)", "name");
                for(var prop : anno.value()){
                    var name = prop.name();
                    var type = type(prop::type);
                    var def = prop.def();

                    var typeName = change(type);

                    var field = FieldSpec.builder(typeName, name, PUBLIC);
                    if(!prop.required() && def.value().isBlank() && type instanceof DeclaredType) field.addAnnotation(ClassName.get(Nullable.class));
                    builder.addField(field.build());

                    create
                        .beginControlFlow("$L:", name)
                        .addStatement("$T $L__data = json.get($S)", ClassName.get(Jval.class), name, name);

                    create.beginControlFlow("if($L__data == null)", name);
                    if(prop.required()){
                        create.addStatement("throw new $T($S)", ClassName.get(IllegalArgumentException.class), "Property `" + name + "` is required for `" + spec.getSimpleName() + "`.");
                    }else{
                        if(def.value().isBlank()){
                            type.accept(new SimpleTypeVisitor8<Void, Void>(){
                                @Override
                                protected Void defaultAction(TypeMirror t, Void unused){
                                    messager.printMessage(Kind.ERROR, "Invalid field type: `" + t + "`.", spec);
                                    return null;
                                }

                                @Override
                                public Void visitPrimitive(PrimitiveType t, Void unused){
                                    switch(t.getKind()){
                                        case BOOLEAN -> create.addStatement("out.$L = false", name);
                                        case BYTE, SHORT, INT, LONG -> create.addStatement("out.$L = -1", name);
                                        case CHAR -> create.addStatement("out.$L = '\\0'", name);
                                        case FLOAT -> create.addStatement("out.$L = $T.NaN", name, ClassName.get(Float.class));
                                        case DOUBLE -> create.addStatement("out.$L = $T.NaN", name, ClassName.get(Double.class));
                                        default -> throw new AssertionError();
                                    }
                                    return null;
                                }

                                @Override
                                public Void visitDeclared(DeclaredType t, Void unused){
                                    create.addStatement("out.$L = null", name);
                                    return null;
                                }

                                @Override
                                public Void visitArray(ArrayType t, Void unused){
                                    int depth = 0;
                                    TypeMirror root = t;
                                    for(TypeMirror count = t; count instanceof ArrayType array; count = array.getComponentType()){
                                        root = array.getComponentType();
                                        depth++;
                                    }

                                    create.addStatement("out.$L = new $T[0]$L", name, change(root), depth > 1 ? "[]".repeat(depth - 1) : "");
                                    return null;
                                }
                            }, null);
                        }else{
                            create
                                .addCode("out.$L = ", name)
                                .addStatement(def.value(), types(def::args).<Object>map(this::change).toArray());
                        }

                        create.addStatement("break $L", name);
                    }

                    create
                        .endControlFlow()
                        .addStatement("$T $L", typeName, name);

                    create(create, name, name + "__data", type, spec);
                    create
                        .addStatement("out.$L = $L", name, name)
                        .endControlFlow();
                }

                var type = builder.addMethod(create.addStatement("return out").build()).build();
                try{
                    JavaFile
                        .builder(packageName, type)
                        .indent("    ")
                        .skipJavaLangImports(true)
                        .build().writeTo(filer);
                }catch(IOException e){
                    messager.printMessage(Kind.ERROR, "Failed to write implementation for '" + spec + "': " + Strings.getFinalMessage(e), spec);
                }
            }

            break;
        }

        return true;
    }

    protected void create(MethodSpec.Builder create, String name, String json, TypeMirror type, Element spec){
        type.accept(new SimpleTypeVisitor8<Void, Void>(){
            @Override
            protected Void defaultAction(TypeMirror t, Void unused){
                messager.printMessage(Kind.ERROR, "Invalid field type: `" + t + "`.", spec);
                return null;
            }

            @Override
            public Void visitPrimitive(PrimitiveType t, Void unused){
                create.addCode("$L = ", name);
                switch(t.getKind()){
                    case BOOLEAN -> create.addStatement("$L.asBool()", json);
                    case BYTE, SHORT -> create.addStatement("($T)$L.asInt()", TypeName.get(t), json);
                    case INT -> create.addStatement("$L.asInt()", json);
                    case LONG -> create.addStatement("$L.asLong()", json);
                    case CHAR -> create.addStatement("$L.asString().charAt(0)", json);
                    case FLOAT -> create.addStatement("$L.asFloat()", json);
                    case DOUBLE -> create.addStatement("$L.asDouble()", json);
                    default -> throw new AssertionError();
                }
                return null;
            }

            @Override
            public Void visitDeclared(DeclaredType t, Void unused){
                if(types.isSameType(t, elements.getTypeElement(String.class.getName()).asType())){
                    create.addStatement("$L = $L.asString()", name, json);
                }else if(types.isSameType(t, elements.getTypeElement(Color.class.getName()).asType())){
                    create
                        .addStatement("$T $L__array = $L.asArray()", ClassName.get(JsonArray.class), name, json)
                        .addStatement(
                            "$L = new $T($L__array.get(0).asFloat(), $L__array.get(1).asFloat(), $L__array.get(2).asFloat(), $L__array.get(3).asFloat())",
                            name, ClassName.get(Color.class), name, name, name, name
                        );
                }else if(types.isSameType(t, elements.getTypeElement(Vec3.class.getName()).asType())){
                    create
                        .addStatement("$T $L__array = $L.asArray()", ClassName.get(JsonArray.class), name, json)
                        .addStatement(
                            "$L = new $T($L__array.get(0).asFloat(), $L__array.get(1).asFloat(), $L__array.get(2).asFloat())",
                            name, ClassName.get(Vec3.class), name, name, name
                        );
                }else if(types.isSameType(t, elements.getTypeElement(Mat3D.class.getName()).asType())){
                    create
                        .addStatement("$T $L__array = $L.asArray()", ClassName.get(JsonArray.class), name, json)
                        .addStatement("$L = new $T()", name, ClassName.get(Mat3D.class))
                        .beginControlFlow("for(int $L__i = 0; $L__i < 16; $L__i++)", name, name, name)
                            .addStatement("$L.val[$L__i] = $L__array.get($L__i).asFloat()", name, name, name, name)
                        .endControlFlow();
                }else if(types.isSameType(t, elements.getTypeElement(Quat.class.getName()).asType())){
                    create
                        .addStatement("$T $L__array = $L.asArray()", ClassName.get(JsonArray.class), name, json)
                        .addStatement(
                            "$L = new $T($L__array.get(0).asFloat(), $L__array.get(1).asFloat(), $L__array.get(2).asFloat(), $L__array.get(3).asFloat())",
                            name, ClassName.get(Quat.class), name, name, name, name
                        );
                }else if(types.isSameType(t, elements.getTypeElement(Jval.class.getName()).asType())){
                    create.addStatement("$L = $L", name, json);
                }else{
                    var type = change(t);
                    if(type.toString().startsWith("gltfrenzy.data")){
                        create.addStatement("$L = $T.create($L)", name, type, json);
                    }else{
                        return super.visitDeclared(t, unused);
                    }
                }

                return null;
            }

            @Override
            public Void visitArray(ArrayType t, Void unused){
                var comp = t.getComponentType();
                if(comp instanceof PrimitiveType p){
                    create
                        .addStatement("$T $L__array = $L.asArray()", ClassName.get(JsonArray.class), name, json)
                        .addStatement("$L = new $T[$L__array.size]", name, TypeName.get(p), name)
                        .beginControlFlow("for(int $L__i = 0, $L__len = $L__array.size; $L__i < $L__len; $L__i++)", name, name, name, name, name, name)
                            .addStatement("$T $L__item = $L__array.get($L__i)", ClassName.get(Jval.class), name, name, name)
                            .addStatement("$T $L__out", TypeName.get(p), name);

                    create(create, name + "__out", name + "__item", p, spec);
                    create
                            .addStatement("$L[$L__i] = $L__out", name, name, name)
                        .endControlFlow();

                    return null;
                }

                var compName = change(comp);
                create
                    .beginControlFlow("$L = $L.asArray().map($L__item ->", name, json, name)
                    .addStatement("$T $L__out", compName, name);

                create(create, name + "__out", name + "__item", comp, spec);
                create
                    .addStatement("return $L__out", name)
                    .endControlFlow(").toArray($T.class)", compName);

                return null;
            }
        }, null);
    }

    protected TypeName change(TypeMirror type){
        if(type instanceof ArrayType array){
            return ArrayTypeName.of(change(array.getComponentType()));
        }else if(type instanceof DeclaredType){
            var str = type.toString();
            return str.startsWith("gltfrenzy.spec")
                ? ClassName.bestGuess("gltfrenzy.data" + str.substring("gltfrenzy.spec".length()))
                : TypeName.get(type);
        }else{
            return TypeName.get(type);
        }
    }

    protected TypeMirror type(Prov<Class<?>> type){
        try{
            type.get();
        }catch(MirroredTypeException e){
            return e.getTypeMirror();
        }

        throw new IllegalArgumentException("`type()` is supposed to be parameterized with an annotation value method reference that returns a `Class<?>`.");
    }

    protected Seq<? extends TypeMirror> types(Prov<Class<?>[]> types){
        try{
            types.get();
        }catch(MirroredTypesException e){
            return Seq.with(e.getTypeMirrors());
        }

        throw new IllegalArgumentException("`types()` is supposed to be parameterized with an annotation value method reference that returns a `Class<?>[]`.");
    }

    @Override
    public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText){
        return Collections.emptyList();
    }

    @Override
    public Set<String> getSupportedOptions(){
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes(){
        return Set.of(Spec.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion(){
        return SourceVersion.RELEASE_17;
    }
}
