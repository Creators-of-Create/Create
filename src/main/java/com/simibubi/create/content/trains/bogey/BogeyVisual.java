package com.simibubi.create.content.trains.bogey;

import java.util.Optional;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.trains.entity.CarriageBogey;
import com.simibubi.create.content.trains.entity.CarriageContraptionEntity;
import com.simibubi.create.foundation.utility.AnimationTickHolder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public final class BogeyVisual {
	private final BogeySizes.BogeySize size;
	private final BogeyStyle style;

	public final CarriageBogey bogey;
	public final BogeyRenderer renderer;
	public final Optional<BogeyRenderer.CommonRenderer> commonRenderer;

	public BogeyVisual(CarriageBogey bogey, BogeyStyle style, BogeySizes.BogeySize size,
		VisualizationContext context) {
		this.bogey = bogey;
		this.size = size;
		this.style = style;

		this.renderer = this.style.createRendererInstance(this.size);
		this.commonRenderer = this.style.getNewCommonRenderInstance();

		commonRenderer.ifPresent(bogeyRenderer -> bogeyRenderer.initialiseContraptionModelData(context, bogey));
		renderer.initialiseContraptionModelData(context, bogey);
	}

	public void beginFrame(float wheelAngle, PoseStack ms) {
		if (ms == null) {
			renderer.emptyTransforms();
			return;
		}

		commonRenderer.ifPresent(bogeyRenderer -> bogeyRenderer.render(bogey.bogeyData, wheelAngle, ms));
		renderer.render(bogey.bogeyData, wheelAngle, ms);
	}

	public void updateLight(BlockAndTintGetter world, CarriageContraptionEntity entity) {
		var lightPos = BlockPos.containing(getLightPos(entity));
		commonRenderer
			.ifPresent(bogeyRenderer -> bogeyRenderer.updateLight(world.getBrightness(LightLayer.BLOCK, lightPos),
				world.getBrightness(LightLayer.SKY, lightPos)));
		renderer.updateLight(world.getBrightness(LightLayer.BLOCK, lightPos),
			world.getBrightness(LightLayer.SKY, lightPos));
	}

	private Vec3 getLightPos(CarriageContraptionEntity entity) {
		return bogey.getAnchorPosition() != null ? bogey.getAnchorPosition()
			: entity.getLightProbePosition(AnimationTickHolder.getPartialTicks());
	}

	@FunctionalInterface
	interface BogeyVisualFactory {
		BogeyVisual create(CarriageBogey bogey, BogeySizes.BogeySize size, VisualizationContext context);
	}
}
