package com.simibubi.create.foundation.render.gl.shader;

/**
 * A Callback for when a shader is called. Used to define shader uniforms.
 */
@FunctionalInterface
public interface ShaderCallback {

    void call(int shader);

    default ShaderCallback andThen(ShaderCallback other) {
        return i -> {
            call(i);
            other.call(i);
        };
    }
}
