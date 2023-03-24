package com.simibubi.create.content.logistics.block.verticalvault;

import com.simibubi.create.AllTileEntities;
import com.simibubi.create.api.connectivity.ConnectivityHandler;

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

public class VerticalItemVaultItem extends BlockItem {

	public VerticalItemVaultItem(Block p_i48527_1_, Properties p_i48527_2_) {
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
			nbt.remove("Length");
			nbt.remove("Size");
			nbt.remove("Controller");
			nbt.remove("LastKnownPos");
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

		if (!VerticalItemVaultBlock.isVault(placedOnState))
			return;
		VerticalItemVaultTileEntity vvaultat = ConnectivityHandler.partAt(AllTileEntities.VERTICAL_ITEM_VAULT.get(), world, placedOnPos);
		if (vvaultat == null)
			return;
		VerticalItemVaultTileEntity controllerTE = vvaultat.getControllerTE();
		if (controllerTE == null)
			return;

		int width = controllerTE.getWidth();
		if (width == 1)
			return;

		int vvaultsToPlace = 0;
		BlockPos startPos = face == Direction.DOWN ? controllerTE.getBlockPos()
				.below()
				: controllerTE.getBlockPos()
				.above(controllerTE.getHeight());

		if (startPos.getY() != pos.getY())
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (VerticalItemVaultBlock.isVault(blockState))
					continue;
				if (!blockState.getMaterial()
						.isReplaceable())
					return;
				vvaultsToPlace++;
			}
		}

		if (!player.isCreative() && stack.getCount() < vvaultsToPlace)
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = startPos.offset(xOffset, 0, zOffset);
				BlockState blockState = world.getBlockState(offsetPos);
				if (VerticalItemVaultBlock.isVault(blockState))
					continue;
				BlockPlaceContext context = BlockPlaceContext.at(ctx, offsetPos, face);
				player.getPersistentData()
						.putBoolean("SilenceVaultSound", true);
				super.place(context);
				player.getPersistentData()
						.remove("SilenceVaultSound");
			}
		}
	}

}
