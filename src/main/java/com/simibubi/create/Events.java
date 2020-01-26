package com.simibubi.create;

import java.util.Arrays;

import com.simibubi.create.modules.curiosities.partialWindows.WindowInABlockTileEntity;

import net.minecraft.block.BlockState;
import net.minecraft.block.FourWayBlock;
import net.minecraft.block.WallBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

@EventBusSubscriber
public class Events {

	@SubscribeEvent
	public static void onTick(ServerTickEvent event) {
		if (event.phase == Phase.END)
			return;

		Create.tick();
	}

	@SubscribeEvent
	public static void onClose(FMLServerStoppingEvent event) {
		Create.shutdown();
	}

	@SubscribeEvent
	public static void onLoadWorld(WorldEvent.Load event) {
		IWorld world = event.getWorld();
		Create.redstoneLinkNetworkHandler.onLoadWorld(world);
		Create.torquePropagator.onLoadWorld(world);
//		Create.logisticalNetworkHandler.onLoadWorld(world);
	}

	@SubscribeEvent
	public static void onUnloadWorld(WorldEvent.Unload event) {
		IWorld world = event.getWorld();
		Create.redstoneLinkNetworkHandler.onUnloadWorld(world);
		Create.torquePropagator.onUnloadWorld(world);
//		Create.logisticalNetworkHandler.onUnloadWorld(world);
	}

	@SubscribeEvent
	public static void onRightClickBlock(RightClickBlock event) {
		if (event.getUseItem() == Result.DENY)
			return;
		if (event.getEntityLiving().isSneaking())
			return;
		if (!event.getPlayer().isAllowEdit())
			return;
		if (!CreateConfig.parameters.allowGlassPanesInPartialBlocks.get())
			return;

		ItemStack stack = event.getItemStack();
		if (stack.isEmpty())
			return;
		if (!stack.getItem().isIn(Tags.Items.GLASS_PANES))
			return;
		if (!(stack.getItem() instanceof BlockItem))
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
			wte.setWindowBlock(((BlockItem) stack.getItem()).getBlock().getDefaultState());
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
