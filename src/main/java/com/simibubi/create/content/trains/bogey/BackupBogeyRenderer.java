package com.simibubi.create.content.trains.bogey;

import com.jozufozu.flywheel.api.MaterialManager;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.trains.entity.CarriageBogey;

import net.minecraft.nbt.CompoundTag;

public class BackupBogeyRenderer extends BogeyRenderer.CommonRenderer {
	public static BackupBogeyRenderer INSTANCE = new BackupBogeyRenderer();

	@Override
	public void render(CompoundTag bogeyData, float wheelAngle, PoseStack ms, int light, VertexConsumer vb, boolean inContraption) {

	}

	@Override
	public void initialiseContraptionModelData(MaterialManager materialManager, CarriageBogey carriageBogey) {

	}
}
