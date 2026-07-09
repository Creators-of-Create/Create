package com.simibubi.create.content.equipment.blueprint;

import org.joml.Matrix3f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.equipment.blueprint.BlueprintEntity.BlueprintSection;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import net.createmod.catnip.api.client.render.CachedBuffers;
import net.createmod.catnip.api.client.render.SuperByteBuffer;
import net.createmod.catnip.api.data.Couple;
import net.minecraft.client.Minecraft;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;

public class BlueprintRenderer extends EntityRenderer<BlueprintEntity, EntityRenderState> {

	public BlueprintRenderer(EntityRendererProvider.Context context) {
		super(context);
	}

	public void render(BlueprintEntity entity, float yaw, float pt, PoseStack ms, MultiBufferSource buffer,
		int light) {
		// TODO 26.2: Port blueprint rendering to EntityRenderer#submit.
	}

	@Override
	public EntityRenderState createRenderState() {
		return new EntityRenderState();
	}
}
