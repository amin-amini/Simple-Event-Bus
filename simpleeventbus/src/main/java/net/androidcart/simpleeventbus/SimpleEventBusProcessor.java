package net.androidcart.simpleeventbus;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.TypeToken;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;


import net.androidcart.simpleeventbusschema.SimpleEventBusSchema;
import net.androidcart.simpleeventbus.SimpleEventBusObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import io.reactivex.functions.Consumer;

public class SimpleEventBusProcessor extends AbstractProcessor {

    ProcessingEnvironment pe;
    private Filer filer;
    private Messager messager;
    private Elements elements;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        pe = processingEnv;
        filer = pe.getFiler();
        messager = pe.getMessager();
        elements = pe.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        for (Element element : roundEnvironment.getElementsAnnotatedWith(SimpleEventBusSchema.class)) {

            if (element.getKind() != ElementKind.INTERFACE) {
                messager.printMessage(Diagnostic.Kind.WARNING, "SimpleEventBusSchema must be an interface");
                continue;
            }

            TypeElement typeElement = (TypeElement) element;
            String packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();
            SimpleEventBusSchema annot = typeElement.getAnnotation(SimpleEventBusSchema.class);
            String busClassName = annot.value();


            TypeName eventBusTN = ClassName.get(packageName, busClassName).withoutAnnotations();

            TypeName objectTN = TypeName.get(SimpleEventBusObject.class).withoutAnnotations();
            ClassName consumerTN = ClassName.get(Consumer.class).withoutAnnotations();

            ClassName rxBusCN = ClassName.get("net.androidcart.androidutils.eventbus", "RxBus");
            TypeName rxBusTN = ParameterizedTypeName.get(rxBusCN, objectTN);

            TypeSpec.Builder eventBusClass = TypeSpec
                    .classBuilder( busClassName )
                    .addModifiers(Modifier.PUBLIC);

            eventBusClass.addField(rxBusTN, "bus");

            eventBusClass.addField(eventBusTN, "instance", Modifier.STATIC);

            MethodSpec getInstanceFunc = MethodSpec
                    .methodBuilder("getInstance")
                    .addStatement("if(instance==null){instance = new "+busClassName+"();} return instance")
                    .returns(eventBusTN)
                    .addModifiers(Modifier.PUBLIC)
                    .addModifiers(Modifier.STATIC)
                    .build();
            eventBusClass.addMethod(getInstanceFunc);

            MethodSpec eventBusConstructor = MethodSpec
                    .constructorBuilder()
                    .addModifiers(Modifier.PRIVATE)
                    .addStatement("bus = new RxBus<>()")
                    .build();
            eventBusClass.addMethod(eventBusConstructor);

            MethodSpec busSubscribe = MethodSpec
                    .methodBuilder("subscribe")
                    .addParameter(ParameterizedTypeName.get(consumerTN, objectTN), "action")
                    .addStatement("bus.subscribe(action)")
                    .addModifiers(Modifier.PUBLIC).build();
            eventBusClass.addMethod(busSubscribe);

            MethodSpec busUnregister = MethodSpec
                    .methodBuilder("unregister")
                    .addParameter(ParameterizedTypeName.get(consumerTN, objectTN), "action")
                    .addStatement("bus.unregister(action)")
                    .addModifiers(Modifier.PUBLIC).build();
            eventBusClass.addMethod(busUnregister);






            String CallbackMethodsName = busClassName + "CallbackMethods";
            TypeSpec.Builder callbackMethods = TypeSpec
                    .classBuilder( CallbackMethodsName )
                    .addModifiers(Modifier.PUBLIC);

            String mapperEnum = busClassName + "Key";
            TypeSpec.Builder mapperEnumMethods = TypeSpec
                    .enumBuilder( mapperEnum )
                    .addModifiers(Modifier.PUBLIC);
            ClassName mapperEnumMethod = ClassName.get(packageName, mapperEnum);
            TypeName mapperEnumMethodType = mapperEnumMethod.withoutAnnotations();


            MethodSpec doBroadcast = MethodSpec
                    .methodBuilder("doBroadcast")
                    .addParameter(mapperEnumMethodType, "key")
                    .addParameter(Object.class, "obj")
                    .addStatement("bus.publish(new SimpleEventBusObject(key, obj))")
                    .addModifiers(Modifier.PUBLIC).build();
            eventBusClass.addMethod(doBroadcast);

            String CallbackMapperName = busClassName + "CallbackMapper";
            ClassName CallbackMethodsMethod = ClassName.get(packageName, CallbackMethodsName);
            TypeName CallbackMethodsMethodType = CallbackMethodsMethod.withoutAnnotations();

            TypeSpec.Builder Mapper = TypeSpec
                    .classBuilder(CallbackMapperName )
                    .addModifiers(Modifier.PUBLIC)
                    .addField(FieldSpec.builder(CallbackMethodsMethodType, "methods").addModifiers(Modifier.PRIVATE).build());

            Mapper.addSuperinterface( new TypeToken<Consumer<SimpleEventBusObject>>(){}.getType() );


            MethodSpec mapperConstructor = MethodSpec
                    .constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(CallbackMethodsMethodType, "methods")
                    .addStatement("this.methods = methods")
                    .build();
            Mapper.addMethod(mapperConstructor);

            MethodSpec.Builder mapperAccept = MethodSpec
                    .methodBuilder("accept")
                    .addParameter(SimpleEventBusObject.class, "res")
                    .addException(Exception.class)
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC);


            mapperAccept.addStatement("Object[] arr = (Object[]) res.object");
            mapperAccept.beginControlFlow("switch ( ("+mapperEnum+")res.key )");


            for ( Element key : typeElement.getEnclosedElements() ) {
                if (key.getKind() != ElementKind.METHOD){
                    continue;
                }
                String keyName = key.getSimpleName().toString();

                if ( keyName.length()>1 ) {
                    keyName = keyName.substring(0, 1).toUpperCase() + keyName.substring(1);
                } else {
                    keyName = keyName.toUpperCase();
                }

                ExecutableElement eMethod = (ExecutableElement) key;

                List<String> paramsNames = new ArrayList<>();
                List<TypeName> paramsType = new ArrayList<>();
                for (VariableElement ve : eMethod.getParameters() ){
                    paramsType.add(TypeName.get(ve.asType()));
                    paramsNames.add(ve.getSimpleName().toString());
                }

                MethodSpec.Builder callbackMethodBuilder = MethodSpec
                        .methodBuilder("on" + keyName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class);
                        //.addParameter(Object.class, "object");
                for (int i=0 ; i<paramsType.size() ; i++){
                    callbackMethodBuilder.addParameter(paramsType.get(i) , paramsNames.get(i));
                }
                callbackMethods.addMethod(callbackMethodBuilder.build());


                mapperEnumMethods.addEnumConstant(keyName);



                MethodSpec.Builder eventBusMethodBuilder = MethodSpec
                        .methodBuilder("broadcast" + keyName)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(void.class);
                eventBusMethodBuilder.addStatement("Object[] object = new Object["+paramsType.size()+"] ");
                for (int i=0 ; i<paramsType.size() ; i++){
                    eventBusMethodBuilder.addParameter(paramsType.get(i) , paramsNames.get(i));
                    eventBusMethodBuilder.addStatement("object["+i+"] = " + paramsNames.get(i));
                }
                eventBusMethodBuilder.addStatement(String.format("doBroadcast(%s.%s, object)",
                    mapperEnumMethodType.withoutAnnotations().toString(),
                    keyName
                ));

                eventBusClass.addMethod(eventBusMethodBuilder.build());



                mapperAccept
                        .addCode("case "+keyName+":\n");



                List<String> paramsNamesConcatenated = new ArrayList<>();
                for(int i=0 ; i<paramsType.size() ; i++){
                    paramsNamesConcatenated.add( "(" + paramsType.get(i).toString() + ") arr["+i+"]");
                }
                mapperAccept.addStatement("methods.on"+keyName+"("+String.join(",", paramsNamesConcatenated )+")")
                        .addStatement("break")
                ;
            }

            mapperAccept.endControlFlow();
            Mapper.addMethod(mapperAccept.build());





            try {
                JavaFile.builder(packageName, callbackMethods.build())
                        .build()
                        .writeTo(filer);
                JavaFile.builder(packageName, eventBusClass.build())
                        .build()
                        .writeTo(filer);
                JavaFile.builder(packageName, Mapper.build())
                        .build()
                        .writeTo(filer);
                JavaFile.builder(packageName, mapperEnumMethods.build())
                        .build()
                        .writeTo(filer);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return true;
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(SimpleEventBusSchema.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }
}

