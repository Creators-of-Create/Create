package com.simibubi.create.foundation.block;

import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.utility.RegisteredObjects;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class ItemUseOverrides {

	private static final Set<ResourceLocation> OVERRIDES = new HashSet<>();

	public static void addBlock(Block block) {
		OVERRIDES.add(RegisteredObjects.getKeyOrThrow(block));
	}

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		if (AllItems.WRENCH.isIn(event.getItemStack()))
			return;

		BlockState state = event.getLevel()
				.getBlockState(event.getPos());
		ResourceLocation id = RegisteredObjects.getKeyOrThrow(state.getBlock());

		if (!OVERRIDES.contains(id))
			return;

		BlockHitResult blockTrace =
				new BlockHitResult(VecHelper.getCenterOf(event.getPos()), event.getFace(), event.getPos(), true);
		InteractionResult result = state.use(event.getLevel(), event.getEntity(), event.getHand(), blockTrace);

		if (!result.consumesAction())
			return;

		event.setCanceled(true);
		event.setCancellationResult(result);
	}

}
