package com.simibubi.create.modules.contraptions.components.actors;

import static net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING;

import com.simibubi.create.foundation.utility.VecHelper;
import com.simibubi.create.modules.contraptions.components.actors.PloughBlock.PloughFakePlayer;
import com.simibubi.create.modules.contraptions.components.contraptions.MovementContext;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class PloughMovementBehaviour extends BlockBreakingMovementBehaviour {

	@Override
	public boolean isActive(MovementContext context) {
		return !VecHelper.isVecPointingTowards(context.relativeMotion,
				context.state.get(HORIZONTAL_FACING).getOpposite());
	}

	@Override
	public void visitNewPosition(MovementContext context, BlockPos pos) {
		super.visitNewPosition(context, pos);

		World world = context.world;
		if (world.isRemote)
			return;
		BlockPos below = pos.down();
		if (!world.isBlockPresent(below))
			return;

		Vec3d vec = VecHelper.getCenterOf(pos);
		PloughFakePlayer player = getPlayer(context);

		if (player == null)
			return;

		BlockRayTraceResult ray = world
				.rayTraceBlocks(new RayTraceContext(vec, vec.add(0, -1, 0), BlockMode.OUTLINE, FluidMode.NONE, player));
		if (ray == null || ray.getType() != Type.BLOCK)
			return;

		ItemUseContext ctx = new ItemUseContext(player, Hand.MAIN_HAND, ray);
		new ItemStack(Items.DIAMOND_HOE).onItemUse(ctx);
	}

	@Override
	public Vec3d getActiveAreaOffset(MovementContext context) {
		return new Vec3d(context.state.get(HORIZONTAL_FACING).getDirectionVec()).scale(.45);
	}

	@Override
	protected boolean throwsEntities() {
		return true;
	}

	@Override
	public boolean canBreak(World world, BlockPos breakingPos, BlockState state) {
		return state.getCollisionShape(world, breakingPos).isEmpty();
	}

	@Override
	public void stopMoving(MovementContext context) {
		if (context.temporaryData instanceof PloughFakePlayer)
			((PloughFakePlayer) context.temporaryData).remove();
	}

	private PloughFakePlayer getPlayer(MovementContext context) {
		if (!(context.temporaryData instanceof PloughFakePlayer) && context.world instanceof ServerWorld) {
			PloughFakePlayer player = new PloughFakePlayer((ServerWorld) context.world);
			player.setHeldItem(Hand.MAIN_HAND, new ItemStack(Items.DIAMOND_HOE));
			context.temporaryData = player;
		}
		return (PloughFakePlayer) context.temporaryData;
	}

}
