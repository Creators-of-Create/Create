package com.jozufozu.flywheel.backend.instancing.tile;

import java.util.Arrays;
import java.util.stream.Stream;

import com.jozufozu.flywheel.backend.instancing.IDynamicInstance;
import com.jozufozu.flywheel.backend.instancing.IInstance;
import com.jozufozu.flywheel.backend.instancing.ITickableInstance;
import com.jozufozu.flywheel.backend.instancing.InstanceMaterial;
import com.jozufozu.flywheel.backend.instancing.MaterialManager;
import com.jozufozu.flywheel.backend.instancing.TileInstanceManager;
import com.jozufozu.flywheel.core.materials.IFlatLight;
import com.jozufozu.flywheel.core.materials.ModelData;
import com.jozufozu.flywheel.core.materials.OrientedData;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

/**
 * The layer between a {@link TileEntity} and the Flywheel backend.
 *
 * <br><br> {@link #updateLight()} is called after construction.
 *
 * <br><br> There are a few additional features that overriding classes can opt in to:
 * <ul>
 *     <li>{@link IDynamicInstance}</li>
 *     <li>{@link ITickableInstance}</li>
 * </ul>
 * See the interfaces' documentation for more information about each one.
 *
 * <br> Implementing one or more of these will give a {@link TileEntityInstance} access
 * to more interesting and regular points within a tick or a frame.
 *
 * @param <T> The type of {@link TileEntity} your class is an instance of.
 */
public abstract class TileEntityInstance<T extends TileEntity> implements IInstance {

	protected final MaterialManager<?> materialManager;
	protected final T tile;
	protected final World world;
	protected final BlockPos pos;
	protected final BlockPos instancePos;
	protected final BlockState blockState;

	public TileEntityInstance(MaterialManager<?> materialManager, T tile) {
		this.materialManager = materialManager;
		this.tile = tile;
		this.world = tile.getWorld();
		this.pos = tile.getPos();
		this.blockState = tile.getBlockState();
		this.instancePos = pos.subtract(materialManager.getOriginCoordinate());
	}

	/**
	 * Update instance data here. Good for when data doesn't change very often and when animations are GPU based.
	 * Don't query lighting data here, that's handled separately in {@link #updateLight()}.
	 *
	 * <br><br> If your animations are complex or more CPU driven, see {@link IDynamicInstance} or {@link ITickableInstance}.
	 */
	public void update() {
	}

	/**
	 * Called after construction and when a light update occurs in the world.
	 *
	 * <br> If your model needs it, update light here.
	 */
	public void updateLight() {
	}

	/**
	 * Free any acquired resources.
	 */
	public abstract void remove();

	/**
	 * Just before {@link #update()} would be called, <code>shouldReset()</code> is checked.
	 * If this function returns <code>true</code>, then this instance will be {@link #remove removed},
	 * and another instance will be constructed to replace it. This allows for more sane resource
	 * acquisition compared to trying to update everything within the lifetime of an instance.
	 *
	 * @return <code>true</code> if this instance should be discarded and refreshed.
	 */
	public boolean shouldReset() {
		return tile.getBlockState() != blockState;
	}

	/**
	 * In order to accommodate for floating point precision errors at high coordinates,
	 * {@link TileInstanceManager}s are allowed to arbitrarily adjust the origin, and
	 * shift the world matrix provided as a shader uniform accordingly.
	 *
	 * @return The {@link BlockPos position} of the {@link TileEntity} this instance
	 * represents should be rendered at to appear in the correct location.
	 */
	public BlockPos getInstancePosition() {
		return pos.subtract(materialManager.getOriginCoordinate());
	}

	@Override
	public BlockPos getWorldPosition() {
		return pos;
	}

	protected void relight(BlockPos pos, IFlatLight<?>... models) {
		relight(world.getLightLevel(LightType.BLOCK, pos), world.getLightLevel(LightType.SKY, pos), models);
	}

	protected <L extends IFlatLight<?>> void relight(BlockPos pos, Stream<L> models) {
		relight(world.getLightLevel(LightType.BLOCK, pos), world.getLightLevel(LightType.SKY, pos), models);
	}

	protected void relight(int block, int sky, IFlatLight<?>... models) {
		relight(block, sky, Arrays.stream(models));
	}

	protected <L extends IFlatLight<?>> void relight(int block, int sky, Stream<L> models) {
		models.forEach(model -> model.setBlockLight(block).setSkyLight(sky));
	}

	protected InstanceMaterial<ModelData> getTransformMaterial() {
		return materialManager.getTransformMaterial();
	}

	protected InstanceMaterial<OrientedData> getOrientedMaterial() {
		return materialManager.getOrientedMaterial();
	}
}
