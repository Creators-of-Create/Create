package com.simibubi.create.content.contraptions.components.actors;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.utility.BlockHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SeatBlock extends Block {

	protected final DyeColor color;
	protected final boolean inCreativeTab;

	public SeatBlock(Properties properties, DyeColor color, boolean inCreativeTab) {
		super(properties);
		this.color = color;
		this.inCreativeTab = inCreativeTab;
	}

	@Override
	public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> p_149666_2_) {
		if (group != ItemGroup.TAB_SEARCH && !inCreativeTab)
			return;
		super.fillItemCategory(group, p_149666_2_);
	}

	@Override
	public void fallOn(World p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
		super.fallOn(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_ * 0.5F);
	}

	@Override
	public void updateEntityAfterFallOn(IBlockReader reader, Entity entity) {
		BlockPos pos = entity.blockPosition();
		if (entity instanceof PlayerEntity || !(entity instanceof LivingEntity) || !canBePickedUp(entity) || isSeatOccupied(entity.level, pos)) {
			super.updateEntityAfterFallOn(reader, entity);
			return;
		}
		if (reader.getBlockState(pos)
			.getBlock() != this)
			return;
		sitDown(entity.level, pos, entity);
	}

	@Override
	public PathNodeType getAiPathNodeType(BlockState state, IBlockReader world, BlockPos pos,
		@Nullable MobEntity entity) {
		return PathNodeType.RAIL;
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, IBlockReader p_220053_2_, BlockPos p_220053_3_,
		ISelectionContext p_220053_4_) {
		return AllShapes.SEAT;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState p_220071_1_, IBlockReader p_220071_2_, BlockPos p_220071_3_,
		ISelectionContext p_220071_4_) {
		return AllShapes.SEAT_COLLISION;
	}

	@Override
	public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
		BlockRayTraceResult p_225533_6_) {
		if (player.isShiftKeyDown())
			return ActionResultType.PASS;

		ItemStack heldItem = player.getItemInHand(hand);
		DyeColor color = DyeColor.getColor(heldItem);
		if (color != null && color != this.color) {
			if (world.isClientSide)
				return ActionResultType.SUCCESS;
			BlockState newState = BlockHelper.copyProperties(state, AllBlocks.SEATS.get(color).getDefaultState());
			world.setBlockAndUpdate(pos, newState);
			return ActionResultType.sidedSuccess(world.isClientSide);
		}

		List<SeatEntity> seats = world.getEntitiesOfClass(SeatEntity.class, new AxisAlignedBB(pos));
		if (!seats.isEmpty()) {
			SeatEntity seatEntity = seats.get(0);
			List<Entity> passengers = seatEntity.getPassengers();
			if (!passengers.isEmpty() && passengers.get(0) instanceof PlayerEntity)
				return ActionResultType.PASS;
			if (!world.isClientSide) {
				seatEntity.ejectPassengers();
				player.startRiding(seatEntity);
			}
			return ActionResultType.SUCCESS;
		}

		if (world.isClientSide)
			return ActionResultType.SUCCESS;
		sitDown(world, pos, player);
		return ActionResultType.SUCCESS;
	}

	public static boolean isSeatOccupied(World world, BlockPos pos) {
		return !world.getEntitiesOfClass(SeatEntity.class, new AxisAlignedBB(pos))
			.isEmpty();
	}

	public static boolean canBePickedUp(Entity passenger) {
		return !(passenger instanceof PlayerEntity) && (passenger instanceof LivingEntity);
	}

	public static void sitDown(World world, BlockPos pos, Entity entity) {
		if (world.isClientSide)
			return;
		SeatEntity seat = new SeatEntity(world, pos);
		seat.setPosRaw(pos.getX() + .5f, pos.getY(), pos.getZ() + .5f);
		world.addFreshEntity(seat);
		entity.startRiding(seat, true);
	}

	public DyeColor getColor() {
		return color;
	}

	@Override
	public boolean isPathfindable(BlockState state, IBlockReader reader, BlockPos pos, PathType type) {
		return false;
	}

}
