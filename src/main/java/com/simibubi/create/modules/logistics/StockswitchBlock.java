package com.simibubi.create.modules.logistics;

import java.util.List;

import com.simibubi.create.foundation.gui.ScreenOpener;
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
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
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
import net.minecraftforge.fml.DistExecutor;

public class StockswitchBlock extends HorizontalBlock implements ITooltip {

	public static final IntegerProperty INDICATOR = IntegerProperty.create("indicator", 0, 6);
	private TooltipHolder info;

	public StockswitchBlock() {
		super(Properties.from(Blocks.ANDESITE));
		info = new TooltipHolder(this);
	}

	@Override
	public boolean isSolid(BlockState state) {
		return false;
	}

	@Override
	public void addInformation(ItemStack stack, IBlockReader worldIn, List<ITextComponent> tooltip,
			ITooltipFlag flagIn) {
		info.addInformation(tooltip);
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING, INDICATOR);
		super.fillStateContainer(builder);
	}

	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn,
			BlockRayTraceResult hit) {
		DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
			displayScreen((StockswitchTileEntity) worldIn.getTileEntity(pos));
		});
		return true;
	}

	@OnlyIn(value = Dist.CLIENT)
	protected void displayScreen(StockswitchTileEntity te) {
		ScreenOpener.open(new StockswitchScreen(te));
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		BlockState state = getDefaultState();

		if (context.getFace().getAxis().isHorizontal()) {
			state = state.with(HORIZONTAL_FACING, context.getFace().getOpposite());
		} else {
			state = state.with(HORIZONTAL_FACING, context.getPlacementHorizontalFacing());
		}

		return state;
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new StockswitchTileEntity();
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
