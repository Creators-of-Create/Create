package com.simibubi.create.content.kinetics.belt.item;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.joml.Vector3f;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.simpleRelays.ShaftBlock;
import com.simibubi.create.infrastructure.config.AllConfigs;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class BeltConnectorHandler {

	private static Random r = new Random();

	public static void tick() {
		Player player = Minecraft.getInstance().player;
		Level world = Minecraft.getInstance().level;

		if (player == null || world == null)
			return;
		if (Minecraft.getInstance().screen != null)
			return;

		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack heldItem = player.getItemInHand(hand);

			if (!AllItems.BELT_CONNECTOR.isIn(heldItem))
				continue;
			if (!heldItem.hasTag())
				continue;

			CompoundTag tag = heldItem.getTag();
			if (!tag.contains("FirstPulley"))
				continue;

			BlockPos first = NbtUtils.readBlockPos(tag.getCompound("FirstPulley"));

			if (!world.getBlockState(first)
				.hasProperty(BlockStateProperties.AXIS))
				continue;
			Axis axis = world.getBlockState(first)
				.getValue(BlockStateProperties.AXIS);

			HitResult rayTrace = Minecraft.getInstance().hitResult;
			if (rayTrace == null || !(rayTrace instanceof BlockHitResult)) {
				if (r.nextInt(50) == 0) {
					world.addParticle(new DustParticleOptions(new Vector3f(.3f, .9f, .5f), 1),
						first.getX() + .5f + randomOffset(.25f), first.getY() + .5f + randomOffset(.25f),
						first.getZ() + .5f + randomOffset(.25f), 0, 0, 0);
				}
				return;
			}

			BlockPos selected = ((BlockHitResult) rayTrace).getBlockPos();

			if (world.getBlockState(selected)
				.canBeReplaced())
				return;
			if (!ShaftBlock.isShaft(world.getBlockState(selected)))
				selected = selected.relative(((BlockHitResult) rayTrace).getDirection());
			if (!selected.closerThan(first, AllConfigs.server().kinetics.maxBeltLength.get()))
				return;

			boolean canConnect =
				BeltConnectorItem.validateAxis(world, selected) && BeltConnectorItem.canConnect(world, first, selected);

			Vec3 start = Vec3.atLowerCornerOf(first);
			Vec3 end = Vec3.atLowerCornerOf(selected);
			Vec3 actualDiff = end.subtract(start);
			end = end.subtract(axis.choose(actualDiff.x, 0, 0), axis.choose(0, actualDiff.y, 0),
				axis.choose(0, 0, actualDiff.z));
			Vec3 diff = end.subtract(start);

			double x = Math.abs(diff.x);
			double y = Math.abs(diff.y);
			double z = Math.abs(diff.z);
			float length = (float) Math.max(x, Math.max(y, z));
			Vec3 step = diff.normalize();

			int sames = ((x == y) ? 1 : 0) + ((y == z) ? 1 : 0) + ((z == x) ? 1 : 0);
			if (sames == 0) {
				List<Vec3> validDiffs = new LinkedList<>();
				for (int i = -1; i <= 1; i++)
					for (int j = -1; j <= 1; j++)
						for (int k = -1; k <= 1; k++) {
							if (axis.choose(i, j, k) != 0)
								continue;
							if (axis == Axis.Y && i != 0 && k != 0)
								continue;
							if (i == 0 && j == 0 && k == 0)
								continue;
							validDiffs.add(new Vec3(i, j, k));
						}
				int closestIndex = 0;
				float closest = Float.MAX_VALUE;
				for (Vec3 validDiff : validDiffs) {
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

			step = new Vec3(Math.signum(step.x), Math.signum(step.y), Math.signum(step.z));
			for (float f = 0; f < length; f += .0625f) {
				Vec3 position = start.add(step.scale(f));
				if (r.nextInt(10) == 0) {
					world.addParticle(
						new DustParticleOptions(new Vector3f(canConnect ? .3f : .9f, canConnect ? .9f : .3f, .5f), 1),
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
