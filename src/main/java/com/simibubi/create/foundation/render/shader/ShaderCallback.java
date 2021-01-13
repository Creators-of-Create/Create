package com.simibubi.create.foundation.render.shader;

/**
 * A Callback for when a shader is called. Used to define shader uniforms.
 */
@FunctionalInterface
public interface ShaderCallback {

    void call(int shader);
}
