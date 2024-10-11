package com.simibubi.create.content.logistics.vault;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.api.connectivity.ConnectivityHandler;
import com.simibubi.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
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

public class ItemVaultItem extends BlockItem {

	public ItemVaultItem(Block p_i48527_1_, Properties p_i48527_2_) {
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
		ItemStack stack = ctx.getItemInHand();
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockPos placedOnPos = pos.relative(face.getOpposite());
		BlockState placedOnState = world.getBlockState(placedOnPos);

		if (!ItemVaultBlock.isVault(placedOnState))
			return;
		if (SymmetryWandItem.presentInHotbar(player))
			return;
		ItemVaultBlockEntity tankAt = ConnectivityHandler.partAt(AllBlockEntityTypes.ITEM_VAULT.get(), world, placedOnPos);
		if (tankAt == null)
			return;
		ItemVaultBlockEntity controllerBE = tankAt.getControllerBE();
		if (controllerBE == null)
			return;

		int width = controllerBE.radius;
		if (width == 1)
			return;

		int tanksToPlace = 0;
		Axis vaultBlockAxis = ItemVaultBlock.getVaultBlockAxis(placedOnState);
		if (vaultBlockAxis == null)
			return;
		if (face.getAxis() != vaultBlockAxis)
			return;

		Direction vaultFacing = Direction.fromAxisAndDirection(vaultBlockAxis, AxisDirection.POSITIVE);
		BlockPos startPos = face == vaultFacing.getOpposite() ? controllerBE.getBlockPos()
			.relative(vaultFacing.getOpposite())
			: controllerBE.getBlockPos()
				.relative(vaultFacing, controllerBE.length);

		if (VecHelper.getCoordinate(startPos, vaultBlockAxis) != VecHelper.getCoordinate(pos, vaultBlockAxis))
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = vaultBlockAxis == Axis.X ? startPos.offset(0, xOffset, zOffset)
					: startPos.offset(xOffset, zOffset, 0);
				BlockState blockState = world.getBlockState(offsetPos);
				if (ItemVaultBlock.isVault(blockState))
					continue;
				if (!blockState.canBeReplaced())
					return;
				tanksToPlace++;
			}
		}

		if (!player.isCreative() && stack.getCount() < tanksToPlace)
			return;

		for (int xOffset = 0; xOffset < width; xOffset++) {
			for (int zOffset = 0; zOffset < width; zOffset++) {
				BlockPos offsetPos = vaultBlockAxis == Axis.X ? startPos.offset(0, xOffset, zOffset)
					: startPos.offset(xOffset, zOffset, 0);
				BlockState blockState = world.getBlockState(offsetPos);
				if (ItemVaultBlock.isVault(blockState))
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
