package io.lsdconsulting.stub.plugin.handler;

import io.lsdconsulting.stub.plugin.model.ControllerModel;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;

@AllArgsConstructor
public class RestControllerAnnotationHandler {
    private final Elements elementUtils;

    public void handle(final Element element, final ControllerModel controllerModel) {
        controllerModel.setRootResource(element.getAnnotation(RestController.class).value());
        controllerModel.setStubClassName(element.getSimpleName().toString() + "Stub");
        controllerModel.setPackageName(elementUtils.getPackageOf(element).getQualifiedName().toString());
        controllerModel.setStubFullyQualifiedName(controllerModel.getPackageName() + "." + controllerModel.getStubClassName());
        controllerModel.setStubBaseFullyQualifiedName(controllerModel.getPackageName() + ".StubBase");
    }
}
