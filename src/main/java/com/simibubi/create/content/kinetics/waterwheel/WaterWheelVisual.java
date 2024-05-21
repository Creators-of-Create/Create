package com.simibubi.create.content.kinetics.waterwheel;

import java.util.function.Consumer;

import com.jozufozu.flywheel.api.instance.Instance;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.ModelCache;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import com.simibubi.create.foundation.render.AllInstanceTypes;
import com.simibubi.create.foundation.render.CachedBufferer;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;

public class WaterWheelVisual<T extends WaterWheelBlockEntity> extends KineticBlockEntityVisual<T> {
	private static final ModelCache<WaterWheelModelKey> MODEL_CACHE = new ModelCache<>(WaterWheelVisual::createModel);

	protected final boolean large;
	protected BlockState lastMaterial;
	protected RotatingInstance rotatingModel;

	public WaterWheelVisual(VisualizationContext context, T blockEntity, boolean large) {
		super(context, blockEntity);
		this.large = large;
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelVisual<T> standard(VisualizationContext context, T blockEntity) {
		return new WaterWheelVisual<>(context, blockEntity, false);
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelVisual<T> large(VisualizationContext context, T blockEntity) {
		return new WaterWheelVisual<>(context, blockEntity, true);
	}

	@Override
	public void init(float pt) {
		setupInstance();
		super.init(pt);
	}

	private void setupInstance() {
		lastMaterial = blockEntity.material;
		rotatingModel = instancerProvider.instancer(AllInstanceTypes.ROTATING, MODEL_CACHE.get(new WaterWheelModelKey(large, blockState, blockEntity.material)))
				.createInstance();
		setup(rotatingModel);
	}

	@Override
	public void update(float pt) {
		if (lastMaterial != blockEntity.material) {
			rotatingModel.delete();
			setupInstance();
		}

		updateRotation(rotatingModel);
	}

	@Override
	public void updateLight() {
		relight(pos, rotatingModel);
	}

	@Override
	protected void _delete() {
		rotatingModel.delete();
	}

	@Override
	public void collectCrumblingInstances(Consumer<Instance> consumer) {
		consumer.accept(rotatingModel);
	}

	private static Model createModel(WaterWheelModelKey key) {
		BakedModel model = WaterWheelRenderer.generateModel(key);
		BlockState state = key.state();
		Direction dir;
		if (key.large()) {
			dir = Direction.fromAxisAndDirection(state.getValue(LargeWaterWheelBlock.AXIS), AxisDirection.POSITIVE);
		} else {
			dir = state.getValue(WaterWheelBlock.FACING);
		}
		PoseStack transform = CachedBufferer.rotateToFaceVertical(dir).get();
		return BakedModelBuilder.create(model)
				.poseStack(transform)
				.build();
	}
}
