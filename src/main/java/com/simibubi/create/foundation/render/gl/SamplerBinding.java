package com.simibubi.create.foundation.render.gl;

public class SamplerBinding {

    private final SamplerType type;
    private final String variableName;
    private final int binding;

    public SamplerBinding(SamplerType type, String variableName, int binding) {
        this.type = type;
        this.variableName = variableName;
        this.binding = binding;
    }

    public SamplerType getType() {
        return type;
    }

    public String getVariableName() {
        return variableName;
    }

    public int getBinding() {
        return binding;
    }
}
