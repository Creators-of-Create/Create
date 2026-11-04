package com.simibubi.create.foundation.block;

import java.util.HashSet;
import java.util.Set;

import com.simibubi.create.AllItems;
import com.simibubi.create.foundation.advancement.CreateAdvancement;
import com.simibubi.create.foundation.utility.BlockHelper;

import net.createmod.catnip.registry.RegisteredObjectsHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class ItemUseOverrides {

	private static final Set<ResourceLocation> OVERRIDES = new HashSet<>();
	private static final ResourceLocation WAX_ON = ResourceLocation.withDefaultNamespace("husbandry/wax_on");
	private static final ResourceLocation WAX_OFF = ResourceLocation.withDefaultNamespace("husbandry/wax_off");

	public static void addBlock(Block block) {
		OVERRIDES.add(RegisteredObjectsHelper.getKeyOrThrow(block));
	}

	@SubscribeEvent
	public static void onBlockActivated(PlayerInteractEvent.RightClickBlock event) {
		if (AllItems.WRENCH.isIn(event.getItemStack()))
			return;

		Level level = event.getLevel();
		BlockPos pos = event.getPos();
		Direction face = event.getFace();
		Player player = event.getEntity();
		InteractionHand hand = event.getHand();

		BlockState state = level.getBlockState(pos);
		ResourceLocation id = RegisteredObjectsHelper.getKeyOrThrow(state.getBlock());

		if (!OVERRIDES.contains(id))
			return;

		BlockHitResult blockTrace =
				new BlockHitResult(VecHelper.getCenterOf(pos), face, pos, true);
		InteractionResult result = BlockHelper.invokeUse(state, level, player, hand, blockTrace);

		if (!result.consumesAction())
			return;

		event.setCanceled(true);
		event.setCancellationResult(result);
	}

	@SubscribeEvent
	public static void onHoneycombUsed(PlayerInteractEvent.RightClickBlock event) {
		if (!(event.getItemStack().is(Items.HONEYCOMB)))
			return;

		// Advancement only exists on the server
		if (!(event.getEntity() instanceof ServerPlayer sp))
			return;
		
		BlockState state = event.getLevel().getBlockState(event.getPos());

		// Check if the Create copper block is waxable and award the "Wax On" advancement
		if (CopperRegistries.getWaxableView().containsKey(state.getBlockHolder())) {
			CreateAdvancement.awardVanilla(sp, WAX_ON);
		}
	}

	@SubscribeEvent
	public static void onAxeUsed(PlayerInteractEvent.RightClickBlock event) {
		if (!(event.getItemStack().canPerformAction(ItemAbilities.AXE_WAX_OFF)))
			return;

		// Advancement only exists on the server
		if (!(event.getEntity() instanceof ServerPlayer sp))
			return;
		
		BlockState state = event.getLevel().getBlockState(event.getPos());

		// Check if the Create copper block is waxed and award the "Wax Off" advancement
		if (CopperRegistries.getWaxableView().containsValue(state.getBlockHolder())) {
			CreateAdvancement.awardVanilla(sp, WAX_OFF);
		}
	}
}
