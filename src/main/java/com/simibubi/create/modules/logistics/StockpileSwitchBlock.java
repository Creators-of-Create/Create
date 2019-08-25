package com.simibubi.create.modules.logistics;

import java.util.List;

import com.simibubi.create.foundation.block.ProperDirectionalBlock;
import com.simibubi.create.foundation.utility.ITooltip;
import com.simibubi.create.foundation.utility.ItemDescription;
import com.simibubi.create.foundation.utility.ItemDescription.Palette;
import com.simibubi.create.foundation.utility.TooltipHolder;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IBlockReader;

public class StockpileSwitchBlock extends ProperDirectionalBlock implements ITooltip {

	private TooltipHolder info;

	public StockpileSwitchBlock() {
		super(Properties.from(Blocks.ANDESITE));
		info = new TooltipHolder(this);
	}

	@Override
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		info.addInformation(tooltip);
	}

	@Override
	public boolean hasTileEntity() {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new StockpileSwitchTileEntity();
	}

	@Override
	public ItemDescription getDescription() {
		Palette color = Palette.Yellow;
		return new ItemDescription(color)
				.withSummary("Toggles a Redstone signal based on the " + h("Storage Space", color)
						+ " in the attached Container.")
				.withBehaviour("When below Lower Limit", "Stops providing " + h("Redstone Power", color))
				.withBehaviour("When above Upper Limit",
						"Starts providing " + h("Redstone Power", color) + " until Lower Limit is reached again.")
				.withControl("When R-Clicked", "Opens the " + h("Configuration Screen", color)).createTabs();
	}

}
