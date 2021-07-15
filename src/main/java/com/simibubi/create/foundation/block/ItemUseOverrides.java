package com.simibubi.create.foundation.block;

import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ItemUseOverrides {

	private static final Set<ResourceLocation> OVERRIDES = new HashSet<>();

	public static void addBlock(Block block) {
		OVERRIDES.add(block.getRegistryName());
	}

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		if (AllItems.WRENCH.isIn(event.getItemStack()))
			return;

		BlockState state = event.getWorld()
				.getBlockState(event.getPos());
		ResourceLocation id = state.getBlock()
				.getRegistryName();

		if (!OVERRIDES.contains(id))
			return;

		BlockRayTraceResult blockTrace =
				new BlockRayTraceResult(VecHelper.getCenterOf(event.getPos()), event.getFace(), event.getPos(), true);
		ActionResultType result = state.use(event.getWorld(), event.getPlayer(), event.getHand(), blockTrace);

		if (!result.consumesAction())
			return;

		event.setCanceled(true);
		event.setCancellationResult(result);
	}

}
