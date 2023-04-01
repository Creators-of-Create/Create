package com.simibubi.create.content.logistics.trains.entity;

import com.jozufozu.flywheel.api.Material;
import com.jozufozu.flywheel.api.MaterialManager;
import com.jozufozu.flywheel.core.Materials;
import com.jozufozu.flywheel.core.materials.model.ModelData;
import com.jozufozu.flywheel.util.AnimationTickHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.AllBlockPartials;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.content.logistics.trains.BogeyRenderer;
import com.simibubi.create.foundation.utility.AngleHelper;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import java.util.EnumMap;
import java.util.Map;

public abstract class BogeyInstance {

	public final CarriageBogey bogey;
	public final BogeyRenderer renderer;
	private final BogeyRenderer.BogeySize size;

	public BogeyInstance(CarriageBogey bogey, BogeyRenderer renderer, BogeyRenderer.BogeySize size,
						 MaterialManager materialManager) {
		this.bogey = bogey;
		this.renderer = renderer;
		this.size = size;

		renderer.initialiseContraptionModelData(materialManager, size);
	}

	public abstract BogeyInstanceFactory getInstanceFactory();

	protected void hiddenFrame() {
		beginFrame(0, null);
	}

	public void beginFrame(float wheelAngle, PoseStack ms) {
		if (ms == null) {
			renderer.emptyTransforms();
			return;
		}

		renderer.render(new CompoundTag(), wheelAngle, ms, this.size);
	};

	public void updateLight(BlockAndTintGetter world, CarriageContraptionEntity entity) {
		var lightPos = new BlockPos(getLightPos(entity));
		renderer.updateLight(world.getBrightness(LightLayer.BLOCK, lightPos),
				world.getBrightness(LightLayer.SKY, lightPos));
	}

	private Vec3 getLightPos(CarriageContraptionEntity entity) {
		return bogey.getAnchorPosition() != null ? bogey.getAnchorPosition()
				: entity.getLightProbePosition(AnimationTickHolder.getPartialTicks());
	}

	@FunctionalInterface
	interface BogeyInstanceFactory {
		BogeyInstance create(CarriageBogey bogey, BogeyRenderer.BogeySize size,
							 MaterialManager materialManager);
	}
}
