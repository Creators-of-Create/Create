package com.simibubi.create.content.kinetics.waterwheel;

import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.api.model.Model;
import com.jozufozu.flywheel.api.visualization.VisualizationContext;
import com.jozufozu.flywheel.core.model.BlockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.kinetics.base.CutoutRotatingInstance;
import com.simibubi.create.foundation.render.CachedBufferer;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class WaterWheelInstance<T extends WaterWheelBlockEntity> extends CutoutRotatingInstance<T> {
	protected final boolean large;
	protected final WaterWheelModelKey key;

	public WaterWheelInstance(VisualizationContext materialManager, T blockEntity, boolean large) {
		super(materialManager, blockEntity);
		this.large = large;
		key = new WaterWheelModelKey(large, getRenderedBlockState(), blockEntity.material);
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelInstance<T> standard(VisualizationContext materialManager, T blockEntity) {
		return new WaterWheelInstance<>(materialManager, blockEntity, false);
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelInstance<T> large(VisualizationContext materialManager, T blockEntity) {
		return new WaterWheelInstance<>(materialManager, blockEntity, true);
	}

	@Override
	public boolean shouldReset() {
		return super.shouldReset() || key.material() != blockEntity.material;
	}

	@Override
	protected Model model() {
		return model(key, () -> {
			BakedModel model = WaterWheelRenderer.generateModel(key);
			BlockState state = key.state();
			Direction dir;
			if (key.large()) {
				dir = Direction.fromAxisAndDirection(state.getValue(LargeWaterWheelBlock.AXIS), AxisDirection.POSITIVE);
			} else {
				dir = state.getValue(WaterWheelBlock.FACING);
			}
			PoseStack transform = CachedBufferer.rotateToFaceVertical(dir).get();
			return BlockModel.of(model, Blocks.AIR.defaultBlockState(), transform);
		});
	}
}
