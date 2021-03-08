package com.simibubi.create.foundation.render.backend.instancing;

/**
 * A marker interface custom worlds can override to indicate
 * that tiles inside the world should render with Flywheel.
 *
 * <code>Minecraft.getInstance().world</code> will always support Flywheel.
 */
public interface IFlywheelWorld {
    default boolean supportsFlywheel() {
        return true;
    }
}
