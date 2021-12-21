package com.simibubi.create.content.contraptions.fluids.tank;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.simibubi.create.lib.util.EntityHelper;
import com.simibubi.create.lib.transfer.fluid.FluidStack;

public class FluidTankItem extends BlockItem {

	public FluidTankItem(Block p_i48527_1_, Properties p_i48527_2_) {
		super(p_i48527_1_, p_i48527_2_);
	}

	@Override
	public InteractionResult place(BlockPlaceContext ctx) {
		InteractionResult initialResult = super.place(ctx);
		if (!initialResult.consumesAction())
			return initialResult;
		tryMultiPlace(ctx);
		return initialResult;
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos p_195943_1_, Level p_195943_2_, Player p_195943_3_,
		ItemStack p_195943_4_, BlockState p_195943_5_) {
		MinecraftServer minecraftserver = p_195943_2_.getServer();
		if (minecraftserver == null)
			return false;
		CompoundTag nbt = p_195943_4_.getTagElement("BlockEntityTag");
		if (nbt != null) {
			nbt.remove("Luminosity");
			nbt.remove("Size");
			nbt.remove("Height");
			nbt.remove("Controller");
			nbt.remove("LastKnownPos");
			if (nbt.contains("TankContent")) {
				FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt.getCompound("TankContent"));
				if (!fluid.isEmpty()) {
					fluid.setAmount(Math.min(FluidTankTileEntity.getCapacityMultiplier(), fluid.getAmount()));
					nbt.put("TankContent", fluid.writeToNBT(new CompoundTag()));
				}
			}
		}
		return super.updateCustomBlockEntityTag(p_195943_1_, p_195943_2_, p_195943_3_, p_195943_4_, p_195943_5_);
	}

	private void tryMultiPlace(BlockPlaceContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null)
			return;
		if (player.isShiftKeyDown())
			return;
		Direction face = ctx.getClickedFace();
		if (!face.getAxis()
			.isVertical())
			return;
		ItemStack stack = ctx.getItemInHand();
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockPos placedOnPos = pos.relative(face.getOpposite());
		BlockState placedOnState = world.getBlockState(placedOnPos);

		if (!FluidTankBlock.isTank(placedOnState))
			return;
		FluidTankTileEntity tankAt = FluidTankConnectivityHandler.anyTankAt(world, placedOnPos);
		if (tankAt == null)
			return;
		FluidTankTileEntity controllerTE = tankAt.getControllerTE();
		if (controllerTE == null)
			return;

		int width = controllerTE.width;
		if (width == 1)
			return;

		int tanksToPlace = 0;
		BlockPos startPos = face == Direction.DOWN ? controllerTE.getBlockPos()
			.below()
			: controllerTE.getBlockPos()
				.above(controllerTE.height);

		if (startPos.getY() != pos.getY())
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
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
				BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (FluidTankBlock.isTank(blockState))
					continue;
				BlockPlaceContext context = BlockPlaceContext.at(ctx, offsetPos, face);
				EntityHelper.getExtraCustomData(player)
					.putBoolean("SilenceTankSound", true);
				super.place(context);
				EntityHelper.getExtraCustomData(player)
					.remove("SilenceTankSound");
			}
		}
	}

}
