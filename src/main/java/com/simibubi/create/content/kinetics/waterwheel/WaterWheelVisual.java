package com.simibubi.create.content.kinetics.waterwheel;

import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.lib.model.ModelCache;
import com.jozufozu.flywheel.lib.model.baked.BakedModelBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.CutoutRotatingVisual;
import com.simibubi.create.foundation.render.CachedBufferer;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;

public class WaterWheelVisual<T extends WaterWheelBlockEntity> extends CutoutRotatingVisual<T> {
	private static final ModelCache<WaterWheelModelKey> MODEL_CACHE = new ModelCache<>(WaterWheelVisual::createModel);

	protected final boolean large;
	protected final WaterWheelModelKey key;

	public WaterWheelVisual(VisualizationContext context, T blockEntity, boolean large) {
		super(context, blockEntity);
		this.large = large;
        key = new WaterWheelModelKey(large, blockState, blockEntity.material);
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelVisual<T> standard(VisualizationContext context, T blockEntity) {
		return new WaterWheelVisual<>(context, blockEntity, false);
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelVisual<T> large(VisualizationContext context, T blockEntity) {
		return new WaterWheelVisual<>(context, blockEntity, true);
	}

	@Override
	public boolean shouldReset() {
		return super.shouldReset() || key.material() != blockEntity.material;
	}

	@Override
	protected Model model() {
		return MODEL_CACHE.get(key);
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
		return new BakedModelBuilder(model)
				.poseStack(transform)
				.build();
	}
}
