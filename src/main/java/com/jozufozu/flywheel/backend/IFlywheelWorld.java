package com.jozufozu.flywheel.backend;

/**
 * A marker interface custom worlds can override to indicate
 * that tiles inside the world should render with Flywheel.
 *
 * <code>Minecraft.getInstance().world</code> is special cased and will support Flywheel by default.
 */
public interface IFlywheelWorld {
	default boolean supportsFlywheel() {
		return true;
	}
}
