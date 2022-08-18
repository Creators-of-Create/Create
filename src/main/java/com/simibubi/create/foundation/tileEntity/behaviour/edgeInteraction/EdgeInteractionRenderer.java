package com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction;

import java.util.List;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
		EdgeInteractionBehaviour behaviour = TileEntityBehaviour.get(world, pos, EdgeInteractionBehaviour.TYPE);
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
			double distance = Vec3.atLowerCornerOf(direction.getNormal()).subtract(target.getLocation()
				.subtract(center))
				.length();
			if (distance > bestDistance)
				continue;
			bestDistance = distance;
			closestEdge = direction;
		}

		AABB bb = EdgeInteractionHandler.getBB(pos, closestEdge);
		boolean hit = bb.contains(target.getLocation());

		ValueBox box = new ValueBox(Components.immutableEmpty(), bb.move(-pos.getX(), -pos.getY(), -pos.getZ()), pos);
		Vec3 textOffset = Vec3.ZERO;

		boolean positive = closestEdge.getAxisDirection() == AxisDirection.POSITIVE;
		if (positive) {
			if (face.getAxis()
				.isHorizontal()) {
				if (closestEdge.getAxis()
					.isVertical())
					textOffset = textOffset.add(0, -128, 0);
				else
					textOffset = textOffset.add(-128, 0, 0);
			} else
				textOffset = textOffset.add(-128, 0, 0);
		}

		box.offsetLabel(textOffset)
				.withColors(0x7A6A2C, 0xB79D64)
				.passive(!hit);

		CreateClient.OUTLINER.showValueBox("edge", box)
				.lineWidth(1 / 64f)
				.withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
				.highlightFace(face);

	}

}
