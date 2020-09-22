package com.simibubi.create.foundation.tileEntity.behaviour.edgeInteraction;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.foundation.utility.RaycastHelper;

import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class EdgeInteractionHandler {

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		World world = event.getWorld();
		BlockPos pos = event.getPos();
		PlayerEntity player = event.getPlayer();
		Hand hand = event.getHand();
		ItemStack heldItem = player.getHeldItem(hand);

		if (player.isSneaking())
			return;
		EdgeInteractionBehaviour behaviour = TileEntityBehaviour.get(world, pos, EdgeInteractionBehaviour.TYPE);
		if (behaviour == null)
			return;
		BlockRayTraceResult ray = RaycastHelper.rayTraceRange(world, player, 10);
		if (ray == null)
			return;
		if (behaviour.requiredItem.orElse(heldItem.getItem()) != heldItem.getItem())
			return;

		Direction activatedDirection = getActivatedDirection(world, pos, ray.getFace(), ray.getHitVec(), behaviour);
		if (activatedDirection == null)
			return;

		if (event.getSide() != LogicalSide.CLIENT)
			behaviour.connectionCallback.apply(world, pos, pos.offset(activatedDirection));
		event.setCanceled(true);
		event.setCancellationResult(ActionResultType.SUCCESS);
		world.playSound(null, pos, SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, .25f, .1f);
	}

	public static List<Direction> getConnectiveSides(World world, BlockPos pos, Direction face,
		EdgeInteractionBehaviour behaviour) {
		List<Direction> sides = new ArrayList<>(6);
		if (BlockHelper.hasBlockSolidSide(world.getBlockState(pos.offset(face)), world, pos.offset(face), face.getOpposite()))
			return sides;

		for (Direction direction : Direction.values()) {
			if (direction.getAxis() == face.getAxis())
				continue;
			BlockPos neighbourPos = pos.offset(direction);
			if (BlockHelper.hasBlockSolidSide(world.getBlockState(neighbourPos.offset(face)), world, neighbourPos.offset(face),
				face.getOpposite()))
				continue;
			if (!behaviour.connectivityPredicate.test(world, pos, face, direction))
				continue;
			sides.add(direction);
		}

		return sides;
	}

	public static Direction getActivatedDirection(World world, BlockPos pos, Direction face, Vector3d hit,
		EdgeInteractionBehaviour behaviour) {
		for (Direction facing : getConnectiveSides(world, pos, face, behaviour)) {
			AxisAlignedBB bb = getBB(pos, facing);
			if (bb.contains(hit))
				return facing;
		}
		return null;
	}

	static AxisAlignedBB getBB(BlockPos pos, Direction direction) {
		AxisAlignedBB bb = new AxisAlignedBB(pos);
		Vector3i vec = direction.getDirectionVec();
		int x = vec.getX();
		int y = vec.getY();
		int z = vec.getZ();
		double margin = 12 / 16f;
		double absX = Math.abs(x) * margin;
		double absY = Math.abs(y) * margin;
		double absZ = Math.abs(z) * margin;

		bb = bb.contract(absX, absY, absZ);
		bb = bb.offset(absX / 2d, absY / 2d, absZ / 2d);
		bb = bb.offset(x / 2d, y / 2d, z / 2d);
		bb = bb.grow(1 / 256f);
		return bb;
	}

}
