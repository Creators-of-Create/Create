package com.simibubi.create.content.contraptions.relays.belt.item;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.relays.elementary.ShaftBlock;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class BeltConnectorHandler {

	private static Random r = new Random();

	public static void tick() {
		PlayerEntity player = Minecraft.getInstance().player;
		World world = Minecraft.getInstance().level;

		if (player == null || world == null)
			return;
		if (Minecraft.getInstance().screen != null)
			return;

		for (Hand hand : Hand.values()) {
			ItemStack heldItem = player.getItemInHand(hand);

			if (!AllItems.BELT_CONNECTOR.isIn(heldItem))
				continue;
			if (!heldItem.hasTag())
				continue;

			CompoundNBT tag = heldItem.getTag();
			if (!tag.contains("FirstPulley"))
				continue;

			BlockPos first = NBTUtil.readBlockPos(tag.getCompound("FirstPulley"));

			if (!world.getBlockState(first).hasProperty(BlockStateProperties.AXIS))
				continue;
			Axis axis = world.getBlockState(first)
				.getValue(BlockStateProperties.AXIS);

			RayTraceResult rayTrace = Minecraft.getInstance().hitResult;
			if (rayTrace == null || !(rayTrace instanceof BlockRayTraceResult)) {
				if (r.nextInt(50) == 0) {
					world.addParticle(new RedstoneParticleData(.3f, .9f, .5f, 1),
						first.getX() + .5f + randomOffset(.25f), first.getY() + .5f + randomOffset(.25f),
						first.getZ() + .5f + randomOffset(.25f), 0, 0, 0);
				}
				return;
			}

			BlockPos selected = ((BlockRayTraceResult) rayTrace).getBlockPos();

			if (world.getBlockState(selected)
				.getMaterial()
				.isReplaceable())
				return;
			if (!ShaftBlock.isShaft(world.getBlockState(selected)))
				selected = selected.relative(((BlockRayTraceResult) rayTrace).getDirection());
			if (!selected.closerThan(first, AllConfigs.SERVER.kinetics.maxBeltLength.get()))
				return;

			boolean canConnect =
				BeltConnectorItem.validateAxis(world, selected) && BeltConnectorItem.canConnect(world, first, selected);

			Vector3d start = Vector3d.atLowerCornerOf(first);
			Vector3d end = Vector3d.atLowerCornerOf(selected);
			Vector3d actualDiff = end.subtract(start);
			end = end.subtract(axis.choose(actualDiff.x, 0, 0), axis.choose(0, actualDiff.y, 0),
				axis.choose(0, 0, actualDiff.z));
			Vector3d diff = end.subtract(start);

			double x = Math.abs(diff.x);
			double y = Math.abs(diff.y);
			double z = Math.abs(diff.z);
			float length = (float) Math.max(x, Math.max(y, z));
			Vector3d step = diff.normalize();

			int sames = ((x == y) ? 1 : 0) + ((y == z) ? 1 : 0) + ((z == x) ? 1 : 0);
			if (sames == 0) {
				List<Vector3d> validDiffs = new LinkedList<>();
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++)
						for (int k = -1; k <= 1; k++) {
							if (axis.choose(i, j, k) != 0)
								continue;
							if (axis == Axis.Y && i != 0 && k != 0)
								continue;
							if (i == 0 && j == 0 && k == 0)
								continue;
							validDiffs.add(new Vector3d(i, j, k));
						}
				int closestIndex = 0;
				float closest = Float.MAX_VALUE;
				for (Vector3d validDiff : validDiffs) {
					double distanceTo = step.distanceTo(validDiff);
					if (distanceTo < closest) {
						closest = (float) distanceTo;
						closestIndex = validDiffs.indexOf(validDiff);
					}
				}
				step = validDiffs.get(closestIndex);
			}

			if (axis == Axis.Y && step.x != 0 && step.z != 0)
				return;

			step = new Vector3d(Math.signum(step.x), Math.signum(step.y), Math.signum(step.z));
			for (float f = 0; f < length; f += .0625f) {
				Vector3d position = start.add(step.scale(f));
				if (r.nextInt(10) == 0) {
					world.addParticle(new RedstoneParticleData(canConnect ? .3f : .9f, canConnect ? .9f : .3f, .5f, 1),
						position.x + .5f, position.y + .5f, position.z + .5f, 0, 0, 0);
				}
			}

			return;
		}
	}

	private static float randomOffset(float range) {
		return (r.nextFloat() - .5f) * 2 * range;
	}

}
