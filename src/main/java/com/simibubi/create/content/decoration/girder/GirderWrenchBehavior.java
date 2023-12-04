package com.simibubi.create.content.decoration.girder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.CreateClient;
import com.simibubi.create.foundation.placement.IPlacementHelper;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.Iterate;
import com.simibubi.create.foundation.utility.Pair;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class GirderWrenchBehavior {

	@OnlyIn(Dist.CLIENT)
	public static void tick() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult result))
			return;

		ClientLevel world = mc.level;
		BlockPos pos = result.getBlockPos();
		Player player = mc.player;
		ItemStack heldItem = player.getMainHandItem();

		if (player.isShiftKeyDown())
			return;

		if (!AllBlocks.METAL_GIRDER.has(world.getBlockState(pos)))
			return;

		if (!AllItems.WRENCH.isIn(heldItem))
			return;

		Pair<Direction, Action> dirPair = getDirectionAndAction(result, world, pos);
		if (dirPair == null)
			return;

		Vec3 center = VecHelper.getCenterOf(pos);
		Vec3 edge = center.add(Vec3.atLowerCornerOf(dirPair.getFirst()
			.getNormal())
			.scale(0.4));
		Direction.Axis[] axes = Arrays.stream(Iterate.axes)
			.filter(axis -> axis != dirPair.getFirst()
				.getAxis())
			.toArray(Direction.Axis[]::new);

		double normalMultiplier = dirPair.getSecond() == Action.PAIR ? 4 : 1;
		Vec3 corner1 = edge
			.add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.POSITIVE)
				.getNormal())
				.scale(0.3))
			.add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.POSITIVE)
				.getNormal())
				.scale(0.3))
			.add(Vec3.atLowerCornerOf(dirPair.getFirst()
				.getNormal())
				.scale(0.1 * normalMultiplier));

		normalMultiplier = dirPair.getSecond() == Action.HORIZONTAL ? 9 : 2;
		Vec3 corner2 = edge
			.add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.NEGATIVE)
				.getNormal())
				.scale(0.3))
			.add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.NEGATIVE)
				.getNormal())
				.scale(0.3))
			.add(Vec3.atLowerCornerOf(dirPair.getFirst()
				.getOpposite()
				.getNormal())
				.scale(0.1 * normalMultiplier));

		CreateClient.OUTLINER.showAABB("girderWrench", new AABB(corner1, corner2))
			.lineWidth(1 / 32f)
			.colored(new Color(127, 127, 127));
	}

	@Nullable
	private static Pair<Direction, Action> getDirectionAndAction(BlockHitResult result, Level world, BlockPos pos) {
		List<Pair<Direction, Action>> validDirections = getValidDirections(world, pos);

		if (validDirections.isEmpty())
			return null;

		List<Direction> directions = IPlacementHelper.orderedByDistance(pos, result.getLocation(),
			validDirections.stream()
				.map(Pair::getFirst)
				.toList());

		if (directions.isEmpty())
			return null;

		Direction dir = directions.get(0);
		return validDirections.stream()
			.filter(pair -> pair.getFirst() == dir)
			.findFirst()
			.orElseGet(() -> Pair.of(dir, Action.SINGLE));
	}

	public static List<Pair<Direction, Action>> getValidDirections(BlockGetter level, BlockPos pos) {
		BlockState blockState = level.getBlockState(pos);

		if (!AllBlocks.METAL_GIRDER.has(blockState))
			return Collections.emptyList();

		return Arrays.stream(Iterate.directions)
			.<Pair<Direction, Action>>mapMulti((direction, consumer) -> {
				BlockState other = level.getBlockState(pos.relative(direction));

				if (!blockState.getValue(GirderBlock.X) && !blockState.getValue(GirderBlock.Z))
					return;

				// up and down
				if (direction.getAxis() == Direction.Axis.Y) {
					// no other girder in target dir
					if (!AllBlocks.METAL_GIRDER.has(other)) {
						if (!blockState.getValue(GirderBlock.X) ^ !blockState.getValue(GirderBlock.Z))
							consumer.accept(Pair.of(direction, Action.SINGLE));
						return;
					}
					// this girder is a pole or cross
					if (blockState.getValue(GirderBlock.X) == blockState.getValue(GirderBlock.Z))
						return;
					// other girder is a pole or cross
					if (other.getValue(GirderBlock.X) == other.getValue(GirderBlock.Z))
						return;
					// toggle up/down connection for both
					consumer.accept(Pair.of(direction, Action.PAIR));

					return;
				}

//					if (AllBlocks.METAL_GIRDER.has(other))
//						consumer.accept(Pair.of(direction, Action.HORIZONTAL));

			})
			.toList();
	}

	public static boolean handleClick(Level level, BlockPos pos, BlockState state, BlockHitResult result) {
		Pair<Direction, Action> dirPair = getDirectionAndAction(result, level, pos);
		if (dirPair == null)
			return false;
		if (level.isClientSide)
			return true;
		if (!state.getValue(GirderBlock.X) && !state.getValue(GirderBlock.Z))
			return false;

		Direction dir = dirPair.getFirst();

		BlockPos otherPos = pos.relative(dir);
		BlockState other = level.getBlockState(otherPos);

		if (dir == Direction.UP) {
			level.setBlock(pos, postProcess(state.cycle(GirderBlock.TOP)), 2 | 16);
			if (dirPair.getSecond() == Action.PAIR && AllBlocks.METAL_GIRDER.has(other))
				level.setBlock(otherPos, postProcess(other.cycle(GirderBlock.BOTTOM)), 2 | 16);
			return true;
		}

		if (dir == Direction.DOWN) {
			level.setBlock(pos, postProcess(state.cycle(GirderBlock.BOTTOM)), 2 | 16);
			if (dirPair.getSecond() == Action.PAIR && AllBlocks.METAL_GIRDER.has(other))
				level.setBlock(otherPos, postProcess(other.cycle(GirderBlock.TOP)), 2 | 16);
			return true;
		}

//		if (dirPair.getSecond() == Action.HORIZONTAL) {
//			BooleanProperty property = dir.getAxis() == Direction.Axis.X ? GirderBlock.X : GirderBlock.Z;
//			level.setBlock(pos, state.cycle(property), 2 | 16);
//
//			return true;
//		}

		return true;
	}

	private static BlockState postProcess(BlockState newState) {
		if (newState.getValue(GirderBlock.TOP) && newState.getValue(GirderBlock.BOTTOM))
			return newState;
		if (newState.getValue(GirderBlock.AXIS) != Axis.Y)
			return newState;
		return newState.setValue(GirderBlock.AXIS, newState.getValue(GirderBlock.X) ? Axis.X : Axis.Z);
	}

	private enum Action {
		SINGLE, PAIR, HORIZONTAL
	}

}
