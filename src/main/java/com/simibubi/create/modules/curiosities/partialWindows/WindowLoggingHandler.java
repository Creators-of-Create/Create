package com.simibubi.create.modules.curiosities.partialWindows;

import java.util.Arrays;

import com.simibubi.create.AllBlockTags;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.config.AllConfigs;

import net.minecraft.block.BlockState;
import net.minecraft.block.FourWayBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;

@EventBusSubscriber(bus = Bus.FORGE)
public class WindowLoggingHandler {

	@SubscribeEvent
	public static void rightClickPartialBlockWithPaneMakesItWindowLogged(RightClickBlock event) {
		if (event.getUseItem() == Result.DENY)
			return;
		if (event.getEntityLiving().isSneaking())
			return;
		if (!event.getPlayer().isAllowEdit())
			return;
		if (!AllConfigs.SERVER.curiosities.allowGlassPanesInPartialBlocks.get())
			return;

		ItemStack stack = event.getItemStack();
		if (stack.isEmpty())
			return;
		if (!(stack.getItem() instanceof BlockItem))
			return;
		BlockItem item = (BlockItem) stack.getItem();
		if (!item.isIn(Tags.Items.GLASS_PANES)
				&& (item.getBlock() == null || !item.getBlock().isIn(Tags.Blocks.GLASS_PANES)))
			return;

		BlockPos pos = event.getPos();
		World world = event.getWorld();
		BlockState blockState = world.getBlockState(pos);
		if (!AllBlockTags.WINDOWABLE.matches(blockState))
			return;
		if (AllBlocks.WINDOW_IN_A_BLOCK.typeOf(blockState))
			return;

		BlockState defaultState = AllBlocks.WINDOW_IN_A_BLOCK.get().getDefaultState();
		world.setBlockState(pos, defaultState);
		TileEntity te = world.getTileEntity(pos);
		if (te != null && te instanceof WindowInABlockTileEntity) {
			WindowInABlockTileEntity wte = (WindowInABlockTileEntity) te;
			wte.setWindowBlock(item.getBlock().getDefaultState());
			wte.updateWindowConnections();

			if (blockState.getBlock() instanceof FourWayBlock) {
				for (BooleanProperty side : Arrays.asList(FourWayBlock.EAST, FourWayBlock.NORTH, FourWayBlock.SOUTH,
						FourWayBlock.WEST))
					blockState = blockState.with(side, false);
			}
			if (blockState.getBlock() instanceof WallBlock)
				blockState = blockState.with(WallBlock.UP, true);

			wte.setPartialBlock(blockState);
			wte.requestModelDataUpdate();

			if (!event.getPlayer().isCreative())
				stack.shrink(1);
			event.getPlayer().swingArm(event.getHand());
		}

		event.setCanceled(true);
	}

}
