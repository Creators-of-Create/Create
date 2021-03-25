package com.simibubi.create.foundation.render.backend.instancing;

/**
 * An interface giving {@link TileEntityInstance}s a hook to have a function called at
 * the end of every tick. By implementing {@link ITickableInstance}, a {@link TileEntityInstance}
 * can update frequently, but not every frame.
 * <br> There are a few cases in which this should be considered over {@link IDynamicInstance}:
 * <ul>
 *     <li>
 *         You'd like to change something about the instance every now and then.
 *         eg. adding or removing parts, snapping to a different rotation.
 *     </li>
 *     <li>
 *         Your TileEntity does animate, but the animation doesn't have
 *         to be smooth, in which case this could be an optimization.
 *     </li>
 * </ul>
 */
public interface ITickableInstance {

	/**
	 * Called every tick.
	 */
	void tick();
}
