package com.simibubi.create.content.contraptions.components.actors;

import java.util.List;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.utility.BlockHelper;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.NonNullList;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

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
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> p_149666_2_) {
		if (group != CreativeModeTab.TAB_SEARCH && !inCreativeTab)
			return;
		super.fillItemCategory(group, p_149666_2_);
	}

	@Override
	public void fallOn(Level p_180658_1_, BlockPos p_180658_2_, Entity p_180658_3_, float p_180658_4_) {
		super.fallOn(p_180658_1_, p_180658_2_, p_180658_3_, p_180658_4_ * 0.5F);
	}

	@Override
	public void updateEntityAfterFallOn(BlockGetter reader, Entity entity) {
		BlockPos pos = entity.blockPosition();
		if (entity instanceof Player || !(entity instanceof LivingEntity) || !canBePickedUp(entity) || isSeatOccupied(entity.level, pos)) {
			super.updateEntityAfterFallOn(reader, entity);
			return;
		}
		if (reader.getBlockState(pos)
			.getBlock() != this)
			return;
		sitDown(entity.level, pos, entity);
	}

	@Override
	public BlockPathTypes getAiPathNodeType(BlockState state, BlockGetter world, BlockPos pos,
		@Nullable Mob entity) {
		return BlockPathTypes.RAIL;
	}

	@Override
	public VoxelShape getShape(BlockState p_220053_1_, BlockGetter p_220053_2_, BlockPos p_220053_3_,
		CollisionContext p_220053_4_) {
		return AllShapes.SEAT;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState p_220071_1_, BlockGetter p_220071_2_, BlockPos p_220071_3_,
		CollisionContext p_220071_4_) {
		return AllShapes.SEAT_COLLISION;
	}

	@Override
	public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand,
		BlockHitResult p_225533_6_) {
		if (player.isShiftKeyDown())
			return InteractionResult.PASS;

		ItemStack heldItem = player.getItemInHand(hand);
		DyeColor color = DyeColor.getColor(heldItem);
		if (color != null && color != this.color) {
			if (world.isClientSide)
				return InteractionResult.SUCCESS;
			BlockState newState = BlockHelper.copyProperties(state, AllBlocks.SEATS.get(color).getDefaultState());
			world.setBlockAndUpdate(pos, newState);
			return InteractionResult.sidedSuccess(world.isClientSide);
		}

		List<SeatEntity> seats = world.getEntitiesOfClass(SeatEntity.class, new AABB(pos));
		if (!seats.isEmpty()) {
			SeatEntity seatEntity = seats.get(0);
			List<Entity> passengers = seatEntity.getPassengers();
			if (!passengers.isEmpty() && passengers.get(0) instanceof Player)
				return InteractionResult.PASS;
			if (!world.isClientSide) {
				seatEntity.ejectPassengers();
				player.startRiding(seatEntity);
			}
			return InteractionResult.SUCCESS;
		}

		if (world.isClientSide)
			return InteractionResult.SUCCESS;
		sitDown(world, pos, player);
		return InteractionResult.SUCCESS;
	}

	public static boolean isSeatOccupied(Level world, BlockPos pos) {
		return !world.getEntitiesOfClass(SeatEntity.class, new AABB(pos))
			.isEmpty();
	}

	public static boolean canBePickedUp(Entity passenger) {
		return !(passenger instanceof Player) && (passenger instanceof LivingEntity);
	}

	public static void sitDown(Level world, BlockPos pos, Entity entity) {
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
	public boolean isPathfindable(BlockState state, BlockGetter reader, BlockPos pos, PathComputationType type) {
		return false;
	}

}
