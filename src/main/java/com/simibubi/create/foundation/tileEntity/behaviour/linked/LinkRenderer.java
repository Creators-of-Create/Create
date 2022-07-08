package com.simibubi.create.foundation.tileEntity.behaviour.linked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class LinkRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		HitResult target = mc.hitResult;
		if (target == null || !(target instanceof BlockHitResult))
			return;

		BlockHitResult result = (BlockHitResult) target;
		ClientLevel world = mc.level;
		BlockPos pos = result.getBlockPos();

		LinkBehaviour behaviour = TileEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		Component freq1 = Lang.translateDirect("logistics.firstFrequency");
		Component freq2 = Lang.translateDirect("logistics.secondFrequency");

		for (boolean first : Iterate.trueAndFalse) {
			AABB bb = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(.25f);
			Component label = first ? freq1 : freq2;
			boolean hit = behaviour.testHit(first, target.getLocation());
			ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;

			ValueBox box = new ValueBox(label, bb, pos).withColors(0x601F18, 0xB73C2D)
					.offsetLabel(behaviour.textShift)
					.passive(!hit);
			CreateClient.OUTLINER.showValueBox(Pair.of(Boolean.valueOf(first), pos), box.transform(transform))
					.lineWidth(1 / 64f)
					.withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
					.highlightFace(result.getDirection());
		}
	}

	public static void renderOnTileEntity(SmartTileEntity te, float partialTicks, PoseStack ms,
		MultiBufferSource buffer, int light, int overlay) {

		if (te == null || te.isRemoved())
			return;
		
		Entity cameraEntity = Minecraft.getInstance().cameraEntity;
		float max = AllConfigs.CLIENT.filterItemRenderDistance.getF();
		if (!te.isVirtual() && cameraEntity != null && cameraEntity.position()
			.distanceToSqr(VecHelper.getCenterOf(te.getBlockPos())) > (max * max))
			return;

		LinkBehaviour behaviour = te.getBehaviour(LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		for (boolean first : Iterate.trueAndFalse) {
			ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;
			ItemStack stack = first ? behaviour.frequencyFirst.getStack() : behaviour.frequencyLast.getStack();

			ms.pushPose();
			transform.transform(te.getBlockState(), ms);
			ValueBoxRenderer.renderItemIntoValueBox(stack, ms, buffer, light, overlay);
			ms.popPose();
		}

	}

}
