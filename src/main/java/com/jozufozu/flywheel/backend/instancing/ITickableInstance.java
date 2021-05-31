package com.jozufozu.flywheel.backend.instancing;

/**
 * An interface giving {@link TileEntityInstance}s a hook to have a function called at
 * the end of every tick. By implementing {@link ITickableInstance}, a {@link TileEntityInstance}
 * can update frequently, but not every frame.
 * <br> There are a few cases in which this should be considered over {@link IDynamicInstance}:
 * <ul>
 *     <li>
 *         You'd like to change something about the instance every now and then.
 *         eg. adding or removing parts, snapping to a different rotation, etc.
 *     </li>
 *     <li>
 *         Your TileEntity does animate, but the animation doesn't have
 *         to be smooth, in which case this could be an optimization.
 *     </li>
 * </ul>
 */
public interface ITickableInstance extends IInstance {

	/**
	 * Called every tick.
	 */
	void tick();

	/**
	 * As a further optimization, tickable instances that are far away are ticked less often.
	 * This behavior can be disabled by returning false.
	 *
	 * <br> You might want to opt out of this if you want your animations to remain smooth
	 * even when far away from the camera. It is recommended to keep this as is, however.
	 *
	 * @return <code>true</code> if your instance should be slow ticked.
	 */
	default boolean decreaseTickRateWithDistance() {
		return true;
	}
}
