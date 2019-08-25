package com.simibubi.create.modules.schematics.block;

import java.util.List;

import com.simibubi.create.foundation.utility.ITooltip;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;
import com.simibubi.create.foundation.utility.TooltipHolder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

public class SchematicTableBlock extends HorizontalBlock implements ITooltip {

	private TooltipHolder info;

	public SchematicTableBlock() {
		super(Properties.from(Blocks.OAK_PLANKS));
		info = new TooltipHolder(this);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean isSolid(BlockState state) {
		return false;
	}

	@Override
	@OnlyIn(value = Dist.CLIENT)
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		info.addInformation(tooltip);
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {

		if (worldIn.isRemote) {
			return true;
		} else {
			SchematicTableTileEntity te = (SchematicTableTileEntity) worldIn.getTileEntity(pos);
			if (te != null)
				NetworkHooks.openGui((ServerPlayerEntity) player, te, te::sendToContainer);
			return true;
		}
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SchematicTableTileEntity();
	}

	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (worldIn.getTileEntity(pos) == null)
			return;

		SchematicTableTileEntity te = (SchematicTableTileEntity) worldIn.getTileEntity(pos);
		for (int slot = 0; slot < te.inventory.getSlots(); slot++) {
			InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(),
					te.inventory.getStackInSlot(slot));
		}

		if (state.hasTileEntity() && state.getBlock() != newState.getBlock()) {
			worldIn.removeTileEntity(pos);
		}

	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Blue;
		return new ItemDescription(color).withSummary("Writes saved Schematics onto an " + h("Empty Schematic", color))
				.withBehaviour("When given an Empty Schematic", "Uploads the chosen File from your Schematics Folder")
				.createTabs();
	}

}
