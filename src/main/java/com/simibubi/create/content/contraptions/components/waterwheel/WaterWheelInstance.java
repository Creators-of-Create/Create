package com.simibubi.create.content.contraptions.components.waterwheel;

import com.jozufozu.flywheel.api.Instancer;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.model.BlockModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.base.CutoutRotatingInstance;
import com.simibubi.create.content.contraptions.base.flwdata.RotatingData;
import com.simibubi.create.foundation.render.CachedBufferer;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class WaterWheelInstance<T extends WaterWheelBlockEntity> extends CutoutRotatingInstance<T> {
	protected final boolean large;
	protected final WaterWheelModelKey key;

	public WaterWheelInstance(MaterialManager materialManager, T blockEntity, boolean large) {
		super(materialManager, blockEntity);
		this.large = large;
		key = new WaterWheelModelKey(large, getRenderedBlockState(), blockEntity.material);
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelInstance<T> standard(MaterialManager materialManager, T blockEntity) {
		return new WaterWheelInstance<>(materialManager, blockEntity, false);
	}

	public static <T extends WaterWheelBlockEntity> WaterWheelInstance<T> large(MaterialManager materialManager, T blockEntity) {
		return new WaterWheelInstance<>(materialManager, blockEntity, true);
	}

	@Override
	public boolean shouldReset() {
		return super.shouldReset() || key.material() != blockEntity.material;
	}

	@Override
	protected Instancer<RotatingData> getModel() {
		return getRotatingMaterial().model(key, () -> {
			BakedModel model = WaterWheelRenderer.generateModel(key);
			BlockState state = key.state();
			// TODO waterwheels
			Direction dir;
			if (key.large()) {
				dir = Direction.fromAxisAndDirection(state.getValue(LargeWaterWheelBlock.AXIS), AxisDirection.POSITIVE);
			} else {
				dir = state.getValue(WaterWheelBlock.FACING);
			}
			PoseStack transform = CachedBufferer.rotateToFaceVertical(dir).get();
			return new BlockModel(model, Blocks.AIR.defaultBlockState(), transform);
		});
	}
}
