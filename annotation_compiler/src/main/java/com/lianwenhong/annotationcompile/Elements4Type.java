package com.lianwenhong.annotationcompile;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

public class Elements4Type {

    public List<VariableElement> viewElements = new ArrayList<>();
    public List<ExecutableElement> methodElements = new ArrayList<>();

    public List<VariableElement> getViewElements() {
        return viewElements;
    }

    public void addViewElement(VariableElement viewElement) {
        this.viewElements.add(viewElement);
    }

    public List<ExecutableElement> getMethodElements() {
        return methodElements;
    }

    public void addMethodElement(ExecutableElement methodElement) {
        this.methodElements.add(methodElement);
    }
}
