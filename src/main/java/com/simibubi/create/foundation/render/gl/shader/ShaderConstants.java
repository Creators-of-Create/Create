package com.simibubi.create.foundation.render.gl.shader;

import java.util.ArrayList;

public class ShaderConstants {

    private final ArrayList<String> defines;

    public ShaderConstants() {
        defines = new ArrayList<>();
    }

    public ShaderConstants define(String def) {
        defines.add(def);
        return this;
    }

    public ArrayList<String> getDefines() {
        return defines;
    }
}
