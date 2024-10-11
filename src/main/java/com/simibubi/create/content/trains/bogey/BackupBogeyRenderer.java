package com.simibubi.create.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.trains.entity.CarriageBogey;

import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import net.minecraft.nbt.CompoundTag;

public class BackupBogeyRenderer extends BogeyRenderer.CommonRenderer {
	public static BackupBogeyRenderer INSTANCE = new BackupBogeyRenderer();

	@Override
	public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb, boolean inContraption) {

	}

	@Override
	public void initialiseContraptionModelData(VisualizationContext context, CarriageBogey carriageBogey) {

	}
}
