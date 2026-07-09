package com.simibubi.create.content.logistics.box;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllPartialModels;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.createmod.catnip.api.math.AngleHelper;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class PackageRenderer extends EntityRenderer<PackageEntity, EntityRenderState> {

	public PackageRenderer(Context pContext) {
		super(pContext);
		shadowRadius = 0.5f;
	}

	public void render(PackageEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer, int light) {
		// TODO 26.2: Port package rendering to EntityRenderer#submit.
	}

	public static void renderBox(Entity entity, float yaw, PoseStack ms, MultiBufferSource buffer, int light,
		PartialModel model) {
		if (model == null)
			return;
		SuperByteBuffer sbb = CachedBuffers.partial(model, Blocks.AIR.defaultBlockState());
		sbb.translate(-.5, 0, -.5)
			.rotateCentered(-AngleHelper.rad(yaw + 90), Direction.UP)
			.light(light)
			.nudge(entity.getId());
		sbb.renderInto(ms, buffer.getBuffer(com.simibubi.create.foundation.render.LegacyRenderTypes.solid()));
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}
}
