package com.simibubi.create.content.equipment.wrench;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllTags.AllItemTags;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class WrenchEventHandler {

	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void useOwnWrenchLogicForCreateBlocks(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getPlayer();
		ItemStack itemStack = event.getItemStack();

		if (event.isCanceled())
			return;
		if (event.getWorld() == null)
			return;
		if (player == null || !player.mayBuild())
			return;
		if (itemStack.isEmpty())
			return;
		if (AllItems.WRENCH.isIn(itemStack))
			return;
		if (!AllItemTags.WRENCH.matches(itemStack.getItem()))
			return;

		BlockState state = event.getWorld()
			.getBlockState(event.getPos());
		Block block = state.getBlock();

		if (!(block instanceof IWrenchable))
			return;

		BlockHitResult hitVec = event.getHitVec();
		UseOnContext context = new UseOnContext(player, event.getHand(), hitVec);
		IWrenchable actor = (IWrenchable) block;

		InteractionResult result =
			player.isShiftKeyDown() ? actor.onSneakWrenched(state, context) : actor.onWrenched(state, context);
		event.setCanceled(true);
		event.setCancellationResult(result);
	}

}
