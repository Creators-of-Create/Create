package com.simibubi.create.foundation.blockEntity.behaviour.edgeInteraction;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.kinetics.crafter.CrafterHelper;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class EdgeInteractionRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		HitResult target = mc.hitResult;
		if (target == null || !(target instanceof BlockHitResult))
			return;

		BlockHitResult result = (BlockHitResult) target;
		ClientLevel world = mc.level;
		BlockPos pos = result.getBlockPos();
		Player player = mc.player;
		ItemStack heldItem = player.getMainHandItem();

		if (player.isShiftKeyDown())
			return;
		EdgeInteractionBehaviour behaviour = BlockEntityBehaviour.get(world, pos, EdgeInteractionBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (behaviour.requiredItem.orElse(heldItem.getItem()) != heldItem.getItem())
			return;

		Direction face = result.getDirection();
		List<Direction> connectiveSides = EdgeInteractionHandler.getConnectiveSides(world, pos, face, behaviour);
		if (connectiveSides.isEmpty())
			return;

		Direction closestEdge = connectiveSides.get(0);
		double bestDistance = Double.MAX_VALUE;
		Vec3 center = VecHelper.getCenterOf(pos);
		for (Direction direction : connectiveSides) {
			double distance = Vec3.atLowerCornerOf(direction.getNormal())
				.subtract(target.getLocation()
					.subtract(center))
				.length();
			if (distance > bestDistance)
				continue;
			bestDistance = distance;
			closestEdge = direction;
		}

		AABB bb = EdgeInteractionHandler.getBB(pos, closestEdge);
		boolean hit = bb.contains(target.getLocation());
		Vec3 offset = Vec3.atLowerCornerOf(closestEdge.getNormal())
			.scale(.5)
			.add(Vec3.atLowerCornerOf(face.getNormal())
				.scale(.469))
			.add(VecHelper.CENTER_OF_ORIGIN);

		ValueBox box = new ValueBox(Components.immutableEmpty(), bb, pos).passive(!hit)
			.transform(new EdgeValueBoxTransform(offset))
			.wideOutline();
		CreateClient.OUTLINER.showValueBox("edge", box)
			.highlightFace(face);

		if (!hit)
			return;

		List<MutableComponent> tip = new ArrayList<>();
		tip.add(Lang.translateDirect("logistics.crafter.connected"));
		tip.add(Lang.translateDirect(CrafterHelper.areCraftersConnected(world, pos, pos.relative(closestEdge))
			? "logistics.crafter.click_to_separate"
			: "logistics.crafter.click_to_merge"));
		CreateClient.VALUE_SETTINGS_HANDLER.showHoverTip(tip);
	}

	static class EdgeValueBoxTransform extends ValueBoxTransform.Sided {

		private Vec3 add;

		public EdgeValueBoxTransform(Vec3 add) {
			this.add = add;
		}

		@Override
		protected Vec3 getSouthLocation() {
			return Vec3.ZERO;
		}

		@Override
		public Vec3 getLocalOffset(BlockState state) {
			return add;
		}

		@Override
		public void rotate(BlockState state, PoseStack ms) {
			super.rotate(state, ms);
		}

	}

}
