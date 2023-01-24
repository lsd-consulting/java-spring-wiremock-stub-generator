package io.lsdconsulting.stub.plugin;

import io.lsdconsulting.stub.plugin.handler.RestControllerAnnotationHandler;
import io.lsdconsulting.stub.plugin.model.ControllerModel;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import lombok.SneakyThrows;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.capitalize;

@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class ControllerProcessor extends AbstractProcessor {
    private RestControllerAnnotationHandler restControllerAnnotationHandler;

    private Messager messager;

    private final PebbleEngine engine = new PebbleEngine.Builder().build();
    private final PebbleTemplate stubTemplate = engine.getTemplate("templates/Stub.java");
    private final PebbleTemplate stubBaseTemplate = engine.getTemplate("templates/StubBase.java");

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        Elements elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
        restControllerAnnotationHandler = new RestControllerAnnotationHandler(elementUtils);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Controller.class.getCanonicalName(),
                RestController.class.getCanonicalName(),
                GetMapping.class.getCanonicalName(),
                PostMapping.class.getCanonicalName(),
                ResponseBody.class.getCanonicalName(),
                RequestBody.class.getCanonicalName(),
                RequestParam.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_11;
    }

    @Override
    @SneakyThrows
    public boolean process(Set<? extends TypeElement> annotations,
                           RoundEnvironment roundEnv) {

        final ControllerModel controllerModel = new ControllerModel();

        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            annotatedElements.forEach(element -> {
                if (element.getAnnotation(RestController.class) != null) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "Processing RestController annotation");
                    restControllerAnnotationHandler.handle(element, controllerModel);
                } else if (element.getAnnotation(GetMapping.class) != null) {
                    messager.printMessage(Diagnostic.Kind.NOTE, "Processing GetMapping annotation");
                    String[] path = element.getAnnotation(GetMapping.class).path();
                    String[] value = element.getAnnotation(GetMapping.class).value();
                    if (path.length > 0) {
                        controllerModel.setSubResource(path[0]);
                    } else if (value.length > 0) {
                        controllerModel.setSubResource(value[0]);
                    }

                    controllerModel.setMethodName(capitalize(element.getSimpleName().toString()));
                    controllerModel.setResponseType(element.asType().toString().replace("()", ""));

                } else {
                    messager.printMessage(Diagnostic.Kind.NOTE, "Unknown annotation");
                }
            });
        }

        if (!annotations.isEmpty()) {
            messager.printMessage(Diagnostic.Kind.NOTE, "Writing files for model:" + controllerModel);
            writeStubFile(controllerModel);
            writeStubBaseFile(controllerModel);
        }

        return true;
    }

    @SneakyThrows
    private void writeStubBaseFile(final ControllerModel controllerModel) {
        try {
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(controllerModel.getStubBaseFullyQualifiedName());
            String stubBasePathName = builderFile.toUri().getPath().replace("generated/sources/annotationProcessor/java/main", "generated-stub-sources");
            String directory = stubBasePathName.replace("StubBase.java", "");
            Files.createDirectories(Path.of(directory));
            Path path = Files.createFile(Path.of(stubBasePathName));
            try (PrintWriter writer = new PrintWriter(builderFile.openWriter())) {
                stubBaseTemplate.evaluate(writer, Map.of(
                        "packageName", controllerModel.getPackageName()
                ));
            }
            try (PrintWriter writer = new PrintWriter(path.toFile())) {
                stubBaseTemplate.evaluate(writer, Map.of(
                        "packageName", controllerModel.getPackageName()
                ));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @SneakyThrows
    private void writeStubFile(final ControllerModel controllerModel) {
        try {
            JavaFileObject builderFile = processingEnv.getFiler().createSourceFile(controllerModel.getStubFullyQualifiedName());
            String stubBasePathName = builderFile.toUri().getPath().replace("generated/sources/annotationProcessor/java/main", "generated-stub-sources");
            String directory = stubBasePathName.replace(controllerModel.getStubClassName() + ".java", "");
            Files.createDirectories(Path.of(directory));
            Path path = Files.createFile(Path.of(stubBasePathName));
            try (PrintWriter writer = new PrintWriter(builderFile.openWriter())) {
                stubTemplate.evaluate(writer, Map.of("model", controllerModel));
            }
            try (PrintWriter writer = new PrintWriter(path.toFile())) {
                stubTemplate.evaluate(writer, Map.of("model", controllerModel));
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
