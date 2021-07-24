package com.simibubi.create.content.contraptions.components.actors;

import static net.minecraft.block.HorizontalBlock.FACING;

import com.simibubi.create.content.contraptions.components.actors.PloughBlock.PloughFakePlayer;
import com.simibubi.create.content.contraptions.components.structureMovement.MovementContext;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BubbleColumnBlock;
import net.minecraft.block.FarmlandBlock;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class PloughMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion, context.state.getValue(FACING)
			.getOpposite());
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);
		World world = context.world;
		if (world.isClientSide)
			return;
		BlockPos below = pos.below();
		if (!world.isLoaded(below))
			return;

		Vector3d vec = VecHelper.getCenterOf(pos);
		PloughFakePlayer player = getPlayer(context);

		if (player == null)
			return;

		BlockRayTraceResult ray = world
			.clip(new RayTraceContext(vec, vec.add(0, -1, 0), BlockMode.OUTLINE, FluidMode.NONE, player));
		if (ray.getType() != Type.BLOCK)
			return;

		ItemUseContext ctx = new ItemUseContext(player, Hand.MAIN_HAND, ray);
		new ItemStack(Items.DIAMOND_HOE).useOn(ctx);
	}

	@Override
	public Vector3d getActiveAreaOffset(MovementContext context) {
		return Vector3d.atLowerCornerOf(context.state.getValue(FACING)
			.getNormal()).scale(.45);
	}

	@Override
	protected boolean throwsEntities() {
		return true;
	}

	@Override
	public boolean canBreak(World world, BlockPos breakingPos, BlockState state) {
		if (world.getBlockState(breakingPos.below())
			.getBlock() instanceof FarmlandBlock)
			return false;
		if (state.getBlock() instanceof FlowingFluidBlock)
			return false;
		if (state.getBlock() instanceof BubbleColumnBlock)
			return false;
		return state.getCollisionShape(world, breakingPos)
			.isEmpty();
	}

	@Override
	protected void onBlockBroken(MovementContext context, BlockPos pos, BlockState brokenState) {
		super.onBlockBroken(context, pos, brokenState);

		if (brokenState.getBlock() == Blocks.SNOW && context.world instanceof ServerWorld) {
			ServerWorld world = (ServerWorld) context.world;
			brokenState.getDrops(new LootContext.Builder(world).withParameter(LootParameters.BLOCK_STATE, brokenState)
				.withParameter(LootParameters.ORIGIN, Vector3d.atCenterOf(pos))
				.withParameter(LootParameters.THIS_ENTITY, getPlayer(context))
				.withParameter(LootParameters.TOOL, new ItemStack(Items.IRON_SHOVEL)))
				.forEach(s -> dropItem(context, s));
		}
	}

	@Override
	public void stopMoving(MovementContext context) {
		super.stopMoving(context);
		if (context.temporaryData instanceof PloughFakePlayer)
			((PloughFakePlayer) context.temporaryData).remove();
	}

	private PloughFakePlayer getPlayer(MovementContext context) {
		if (!(context.temporaryData instanceof PloughFakePlayer) && context.world != null) {
			PloughFakePlayer player = new PloughFakePlayer((ServerWorld) context.world);
			player.setItemInHand(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_HOE));
			context.temporaryData = player;
		}
		return (PloughFakePlayer) context.temporaryData;
	}

}
