package com.simibubi.create.content.redstone.link;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxRenderer;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.createmod.catnip.api.data.Iterate;
import net.createmod.catnip.api.math.VecHelper;
import net.createmod.catnip.api.client.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.createmod.catnip.api.client.render.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
		if (target == null || !(target instanceof BlockHitResult result))
			return;

		ClientLevel world = mc.level;
		BlockPos pos = result.getBlockPos();

		LinkBehaviour behaviour = BlockEntityBehaviour.get(world, pos, LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		Component freq1 = CreateLang.translateDirect("logistics.firstFrequency");
		Component freq2 = CreateLang.translateDirect("logistics.secondFrequency");

		for (boolean first : Iterate.trueAndFalse) {
			AABB bb = new AABB(Vec3.ZERO, Vec3.ZERO).inflate(.25f);
			Component label = first ? freq1 : freq2;
			boolean hit = behaviour.testHit(first, target.getLocation());
			ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;

			ValueBox box = new ValueBox(label, bb, pos).passive(!hit);
			boolean empty = behaviour.getNetworkKey()
				.get(first)
				.getStack()
				.isEmpty();

			if (!empty)
				box.wideOutline();

			Outliner.getInstance().showOutline(Pair.of(Boolean.valueOf(first), pos), box.transform(transform))
				.highlightFace(result.getDirection());

			if (!hit)
				continue;

			List<MutableComponent> tip = new ArrayList<>();
			tip.add(label.copy());
			tip.add(
				CreateLang.translateDirect(empty ? "logistics.filter.click_to_set" : "logistics.filter.click_to_replace"));
			CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
		}
	}

	public static void renderOnBlockEntity(SmartBlockEntity be, float partialTicks, PoseStack ms,
										   MultiBufferSource buffer, int light, int overlay) {

		if (be == null || be.isRemoved())
			return;

		Entity cameraEntity = Minecraft.getInstance().getCameraEntity();
		float max = AllConfigs.client().filterItemRenderDistance.getF();
		if (!be.isVirtual() && cameraEntity != null && cameraEntity.position()
			.distanceToSqr(VecHelper.getCenterOf(be.getBlockPos())) > (max * max))
			return;

		LinkBehaviour behaviour = be.getBehaviour(LinkBehaviour.TYPE);
		if (behaviour == null)
			return;

		for (boolean first : Iterate.trueAndFalse) {
			ValueBoxTransform transform = first ? behaviour.firstSlot : behaviour.secondSlot;
			ItemStack stack = first ? behaviour.frequencyFirst.getStack() : behaviour.frequencyLast.getStack();

			ms.pushPose();
			transform.transform(be.getLevel(), be.getBlockPos(), be.getBlockState(), ms);
			ValueBoxRenderer.renderItemIntoValueBox(stack, ms, buffer, light, overlay);
			ms.popPose();
		}

	}

}
