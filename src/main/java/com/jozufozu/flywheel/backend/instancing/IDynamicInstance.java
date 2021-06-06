package com.jozufozu.flywheel.backend.instancing;

import com.jozufozu.flywheel.backend.instancing.tile.TileEntityInstance;

/**
 * An interface giving {@link TileEntityInstance}s a hook to have a function called at
 * the start of a frame. By implementing {@link IDynamicInstance}, a {@link TileEntityInstance}
 * can animate its models in ways that could not be easily achieved by shader attribute
 * parameterization.
 *
 * <br><br> If your goal is offloading work to shaders, but you're unsure exactly how you need
 * to parameterize the instances, you're encouraged to implement this for prototyping.
 */
public interface IDynamicInstance extends IInstance {
	/**
	 * Called every frame.
	 */
	void beginFrame();

	/**
	 * As a further optimization, dynamic instances that are far away are ticked less often.
	 * This behavior can be disabled by returning false.
	 *
	 * <br> You might want to opt out of this if you want your animations to remain smooth
	 * even when far away from the camera. It is recommended to keep this as is, however.
	 *
	 * @return <code>true</code> if your instance should be slow ticked.
	 */
	default boolean decreaseFramerateWithDistance() {
		return true;
	}
}
