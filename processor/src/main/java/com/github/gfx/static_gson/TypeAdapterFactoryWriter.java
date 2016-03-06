package com.github.gfx.static_gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;

import javax.lang.model.element.Modifier;

public class TypeAdapterFactoryWriter {

    private final StaticGsonContext context;

    private final ModelDefinition model;

    private final ParameterizedTypeName typeToken;

    private final ParameterizedTypeName typeAdapter;

    private final TypeVariableName t;

    public TypeAdapterFactoryWriter(StaticGsonContext context, ModelDefinition model) {
        this.context = context;
        this.model = model;
        t = TypeVariableName.get("T");
        typeAdapter = ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), t);
        typeToken = ParameterizedTypeName.get(ClassName.get(TypeToken.class), t);

    }

    String getPackageName() {
        return model.modelType.packageName();
    }

    String getTypeAdapterClassName() {
        return model.modelType.simpleName() + "_TypeAdapterFactory";
    }

    TypeSpec buildTypeSpec() {
        TypeSpec.Builder type = TypeSpec.classBuilder(getTypeAdapterClassName());
        type.addJavadoc("Generated by {@code $T}\n", StaticGsonProcessor.class);
        type.addAnnotation(Types.Keep);
        type.addModifiers(Modifier.PUBLIC);
        type.addSuperinterface(TypeAdapterFactory.class);

        type.addMethod(buildCreateMethod());

        return type.build();
    }

    /**
     * @return The entity of {@code @Override public <T> TypeAdapter<T> create(final Gson gson,
     * TypeToken<T> typeToken)}
     */
    private MethodSpec buildCreateMethod() {
        MethodSpec.Builder method = MethodSpec.methodBuilder("create");

        return method.addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addTypeVariable(t)
                .returns(typeAdapter)
                .addParameter(Gson.class, "gson", Modifier.FINAL)
                .addParameter(typeToken, "typeToken", Modifier.FINAL)
                .addStatement("return ($T) $L", typeAdapter, buildTypeAdapter())
                .build();
    }

    private TypeSpec buildTypeAdapter() {
        return TypeSpec.anonymousClassBuilder("")
                .superclass(ParameterizedTypeName.get(typeAdapter.rawType, model.modelType))
                .addMethod(buildWriteMethod())
                .addMethod(buildReadMethod())
                .build();
    }

    /**
     * @return {@code public void write(JsonWriter out, T value) throws IOException}
     */
    private MethodSpec buildWriteMethod() {
        MethodSpec.Builder method = MethodSpec.methodBuilder("write")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addException(IOException.class)
                .addParameter(JsonWriter.class, "writer")
                .addParameter(model.modelType, "value");

        method.addStatement("writer.beginObject()");
        for (FieldDefinition field : model.getFields()) {
            method.addCode(field.buildWriteBlock("value", "writer"));
        }
        method.addStatement("writer.endObject()");

        return method.build();
    }

    /**
     * @return {@code public T read(JsonReader in) throws IOException }
     */
    private MethodSpec buildReadMethod() {
        MethodSpec.Builder method = MethodSpec.methodBuilder("read")
                .addAnnotation(Annotations.suppressWarnings("unchecked"))
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(model.modelType)
                .addException(IOException.class)
                .addParameter(JsonReader.class, "reader");

        method.addStatement("$T object = new $T()", model.modelType, model.modelType);

        method.addStatement("reader.beginObject()");
        method.beginControlFlow("while (reader.hasNext())");
        method.beginControlFlow("switch (reader.nextName())");
        for (FieldDefinition field : model.getFields()) {
            for (String name : field.getSerializedNameCandidates()) {
                method.addCode("case $S:\n", name);
            }
            method.addCode(field.buildReadCodeBlock("object", "reader"));
            method.addStatement("break");
        }
        method.addStatement("default: break");
        method.endControlFlow(); // switch
        method.endControlFlow(); // while
        method.addStatement("reader.endObject()");

        method.addStatement("return ($T) object", model.modelType);

        return method.build();
    }

    private JavaFile buildJavaFile() {
        return JavaFile.builder(getPackageName(), buildTypeSpec())
                .skipJavaLangImports(true)
                .build();
    }

    public void write() {
        try {
            buildJavaFile().writeTo(context.processingEnv.getFiler());
        } catch (IOException e) {
            throw new ProcessingException(e);
        }
    }
}
