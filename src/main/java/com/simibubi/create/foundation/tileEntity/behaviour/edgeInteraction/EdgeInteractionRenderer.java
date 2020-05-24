package com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction;

import java.util.List;

import com.simibubi.create.AllSpecialTextures;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.ValueBox;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class EdgeInteractionRenderer {

	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		RayTraceResult target = mc.objectMouseOver;
		if (target == null || !(target instanceof BlockRayTraceResult))
			return;

		BlockRayTraceResult result = (BlockRayTraceResult) target;
		ClientWorld world = mc.world;
		BlockPos pos = result.getPos();
		PlayerEntity player = mc.player;
		ItemStack heldItem = player.getHeldItemMainhand();

		if (player.isSneaking())
			return;
		EdgeInteractionBehaviour behaviour = TileEntityBehaviour.get(world, pos, EdgeInteractionBehaviour.TYPE);
		if (behaviour == null)
			return;
		if (behaviour.requiredItem.orElse(heldItem.getItem()) != heldItem.getItem())
			return;

		Direction face = result.getFace();
		List<Direction> connectiveSides = EdgeInteractionHandler.getConnectiveSides(world, pos, face, behaviour);
		if (connectiveSides.isEmpty())
			return;

		Direction closestEdge = connectiveSides.get(0);
		double bestDistance = Double.MAX_VALUE;
		Vec3d center = VecHelper.getCenterOf(pos);
		for (Direction direction : connectiveSides) {
			double distance = new Vec3d(direction.getDirectionVec()).subtract(target.getHitVec()
				.subtract(center))
				.length();
			if (distance > bestDistance)
				continue;
			bestDistance = distance;
			closestEdge = direction;
		}

		AxisAlignedBB bb = EdgeInteractionHandler.getBB(pos, closestEdge);
		boolean hit = bb.contains(target.getHitVec());

		ValueBox box = new ValueBox("", bb.offset(-pos.getX(), -pos.getY(), -pos.getZ()), pos);
		Vec3d textOffset = Vec3d.ZERO;

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

		CreateClient.outliner.showValueBox("edge", box)
			.lineWidth(1 / 64f)
			.withFaceTexture(hit ? AllSpecialTextures.THIN_CHECKERED : null)
			.highlightFace(face);

	}

}
