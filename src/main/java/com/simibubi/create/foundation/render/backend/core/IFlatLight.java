package com.simibubi.create.foundation.render.backend.core;

import com.simibubi.create.foundation.render.backend.instancing.InstanceData;

/**
 * An interface that implementors of {@link InstanceData} should also implement
 * if they wish to make use of Flywheel's provided light update methods.
 * <p>
 * This only covers flat lighting, smooth lighting is still TODO.
 *
 * @param <D> The name of the class that implements this interface.
 */
public interface IFlatLight<D extends InstanceData & IFlatLight<D>> {
	/**
	 * @param blockLight An integer in the range [0, 15] representing the
	 *                   amount of block light this instance should receive.
	 * @return <code>this</code>
	 */
	D setBlockLight(int blockLight);

	/**
	 * @param skyLight An integer in the range [0, 15] representing the
	 *                 amount of sky light this instance should receive.
	 * @return <code>this</code>
	 */
	D setSkyLight(int skyLight);
}
