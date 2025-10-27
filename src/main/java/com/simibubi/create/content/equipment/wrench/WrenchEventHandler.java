package com.simibubi.create.content.equipment.wrench;

import com.simibubi.create.AllItems;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags.Items;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class WrenchEventHandler {

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void useOwnWrenchLogicForCreateBlocks(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		ItemStack itemStack = event.getItemStack();

		if (event.isCanceled())
			return;
		if (event.getLevel() == null)
			return;
		if (player == null || !player.mayBuild())
			return;
		if (itemStack.isEmpty())
			return;
		if (AllItems.WRENCH.isIn(itemStack))
			return;
		if (!itemStack.is(Items.TOOLS_WRENCH))
			return;

		BlockState state = event.getLevel()
			.getBlockState(event.getPos());
		Block block = state.getBlock();

		if (!(block instanceof IWrenchable actor))
			return;

		BlockHitResult hitVec = event.getHitVec();
		UseOnContext context = new UseOnContext(player, event.getHand(), hitVec);

		InteractionResult result =
			player.isShiftKeyDown() ? actor.onSneakWrenched(state, context) : actor.onWrenched(state, context);
		event.setCanceled(true);
		event.setCancellationResult(result);
	}

}
