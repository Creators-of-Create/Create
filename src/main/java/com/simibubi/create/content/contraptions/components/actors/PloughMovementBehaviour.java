package com.simibubi.create.content.contraptions.components.actors;

import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BubbleColumnBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class PloughMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.getValue(BlockStateProperties.FACING)
			.getOpposite());
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);
		Level world = context.world;
		if (world.isClientSide)
			return;
		BlockPos below = pos.below();
		if (!world.isLoaded(below))
			return;

		Vec3 vec = VecHelper.getCenterOf(pos);
		PloughBlock.PloughFakePlayer player = getPlayer(context);

		if (player == null)
			return;

		BlockHitResult ray = world
			.clip(new ClipContext(vec, vec.add(0, -1, 0), Block.OUTLINE, Fluid.NONE, player));
		if (ray.getType() != Type.BLOCK)
			return;

		UseOnContext ctx = new UseOnContext(player, InteractionHand.MAIN_HAND, ray);
		new ItemStack(Items.DIAMOND_HOE).useOn(ctx);
	}

	@Override
	public Vec3 getActiveAreaOffset(MovementContext context) {
		return Vec3.atLowerCornerOf(context.state.getValue(BlockStateProperties.FACING)
			.getNormal()).scale(.45);
	}

	@Override
	protected boolean throwsEntities() {
		return true;
	}

	@Override
	public boolean canBreak(Level world, BlockPos breakingPos, BlockState state) {
		if (world.getBlockState(breakingPos.below())
			.getBlock() instanceof FarmBlock)
			return false;
		if (state.getBlock() instanceof LiquidBlock)
			return false;
		if (state.getBlock() instanceof BubbleColumnBlock)
			return false;
		return state.getCollisionShape(world, breakingPos)
			.isEmpty();
	}

	@Override
	protected void onBlockBroken(MovementContext context, BlockPos pos, BlockState brokenState) {
		super.onBlockBroken(context, pos, brokenState);

		if (brokenState.getBlock() == Blocks.SNOW && context.world instanceof ServerLevel) {
			ServerLevel world = (ServerLevel) context.world;
			brokenState.getDrops(new LootContext.Builder(world).withParameter(LootContextParams.BLOCK_STATE, brokenState)
				.withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos))
				.withParameter(LootContextParams.THIS_ENTITY, getPlayer(context))
				.withParameter(LootContextParams.TOOL, new ItemStack(Items.IRON_SHOVEL)))
				.forEach(s -> dropItem(context, s));
		}
	}

	@Override
	public void stopMoving(MovementContext context) {
		super.stopMoving(context);
		if (context.temporaryData instanceof PloughBlock.PloughFakePlayer)
			((PloughBlock.PloughFakePlayer) context.temporaryData).remove(Entity.RemovalReason.DISCARDED);
	}

	private PloughBlock.PloughFakePlayer getPlayer(MovementContext context) {
		if (!(context.temporaryData instanceof PloughBlock.PloughFakePlayer) && context.world != null) {
			PloughBlock.PloughFakePlayer player = new PloughBlock.PloughFakePlayer((ServerLevel) context.world);
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.DIAMOND_HOE));
			context.temporaryData = player;
		}
		return (PloughBlock.PloughFakePlayer) context.temporaryData;
	}

}
