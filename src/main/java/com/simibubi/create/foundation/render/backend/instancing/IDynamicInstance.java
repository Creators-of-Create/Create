package com.simibubi.create.foundation.render.backend.instancing;

/**
 * An interface giving {@link TileEntityInstance}s a hook to have a function called at
 * the start of a frame. By implementing {@link IDynamicInstance}, a {@link TileEntityInstance}
 * can animate its models in ways that could not be easily achieved by shader attribute
 * parameterization.
 *
 * <br><br> If your goal is offloading work to shaders, but you're unsure exactly how you need
 * to parameterize the instances, you're encouraged to implement this for prototyping.
 */
public interface IDynamicInstance {
    /**
     * Called every frame.
     */
    void beginFrame();
}
