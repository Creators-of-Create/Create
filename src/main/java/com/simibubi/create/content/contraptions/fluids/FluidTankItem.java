package com.simibubi.create.content.contraptions.fluids;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class FluidTankItem extends BlockItem {

	public FluidTankItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public ActionResultType tryPlace(BlockItemUseContext ctx) {
		ActionResultType initialResult = super.tryPlace(ctx);
		if (initialResult != ActionResultType.SUCCESS)
			return initialResult;
		tryMultiPlace(ctx);
		return initialResult;
	}

	private void tryMultiPlace(BlockItemUseContext ctx) {
		PlayerEntity player = ctx.getPlayer();
		if (player == null)
			return;
		if (player.isSneaking())
			return;
		Direction face = ctx.getFace();
		if (!face.getAxis()
			.isVertical())
			return;
		ItemStack stack = ctx.getItem();
		World world = ctx.getWorld();
		BlockPos pos = ctx.getPos();
		BlockPos placedOnPos = pos.offset(face.getOpposite());
		BlockState placedOnState = world.getBlockState(placedOnPos);

		if (!FluidTankBlock.isTank(placedOnState))
			return;
		FluidTankTileEntity tankAt = FluidTankConnectivityHandler.tankAt(world, placedOnPos);
		if (tankAt == null)
			return;
		FluidTankTileEntity controllerTE = tankAt.getControllerTE();
		if (controllerTE == null)
			return;

		int width = controllerTE.width;
		if (width == 1)
			return;

		int tanksToPlace = 0;
		BlockPos startPos = face == Direction.DOWN ? controllerTE.getPos()
			.down()
			: controllerTE.getPos()
				.up(controllerTE.height);
			
		if (startPos.getY() != pos.getY())
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.add(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (FluidTankBlock.isTank(blockState))
					continue;
				if (!blockState.getMaterial()
					.isReplaceable())
					return;
				tanksToPlace++;
			}
		}

		if (!player.isCreative() && stack.getCount() < tanksToPlace)
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.add(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (FluidTankBlock.isTank(blockState))
					continue;
				BlockItemUseContext context = BlockItemUseContext.func_221536_a(ctx, offsetPos, face);
				player.getPersistentData().putBoolean("SilenceTankSound", true);
				super.tryPlace(context);
				player.getPersistentData().remove("SilenceTankSound");
			}
		}
	}

}
