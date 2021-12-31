package com.simibubi.create.content.contraptions.wrench;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.AllTags;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.event.entity.player.AttackEntityEvent;

public class WrenchItem extends Item {

	public WrenchItem(Properties properties) {
		super(properties);
	}

	@Nonnull
	@Override
	public InteractionResult useOn(UseOnContext context) {
		Player player = context.getPlayer();
		if (player == null || !player.mayBuild())
			return super.useOn(context);

		BlockState state = context.getLevel()
			.getBlockState(context.getClickedPos());
		Block block = state.getBlock();

		if (!(block instanceof IWrenchable)) {
			if (canWrenchPickup(state))
				return onItemUseOnOther(context);
			return super.useOn(context);
		}

		IWrenchable actor = (IWrenchable) block;
		if (player.isShiftKeyDown())
			return actor.onSneakWrenched(state, context);

		InteractionResult result = actor.onWrenched(state, context);
		if (result == InteractionResult.SUCCESS && AllConfigs.CLIENT.sounds.enableWrenchRatchet.get())
			AllSoundEvents.WRENCH_RATCHET.playFrom(player, 0.5f, 1f);
		return result;
	}

	private boolean canWrenchPickup(BlockState state) {
		return AllTags.AllBlockTags.WRENCH_PICKUP.matches(state);
	}

	private InteractionResult onItemUseOnOther(UseOnContext context) {
		Player player = context.getPlayer();
		Level world = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState state = world.getBlockState(pos);
		if (!(world instanceof ServerLevel))
			return InteractionResult.SUCCESS;
		if (player != null && !player.isCreative())
			Block.getDrops(state, (ServerLevel) world, pos, world.getBlockEntity(pos), player, context.getItemInHand())
				.forEach(itemStack -> player.getInventory().placeItemBackInInventory(itemStack));
		state.spawnAfterBreak((ServerLevel) world, pos, ItemStack.EMPTY);
		world.destroyBlock(pos, false);
		AllSoundEvents.WRENCH_REMOVE.playOnServer(world, pos, 1, Create.RANDOM.nextFloat() * .5f + .5f);
		return InteractionResult.SUCCESS;
	}

	public static void wrenchInstaKillsMinecarts(AttackEntityEvent event) {
		Entity target = event.getTarget();
		if (!(target instanceof AbstractMinecart))
			return;
		Player player = event.getPlayer();
		ItemStack heldItem = player.getMainHandItem();
		if (!AllItems.WRENCH.isIn(heldItem))
			return;
		if (player.isCreative())
			return;
		AbstractMinecart minecart = (AbstractMinecart) target;
		minecart.hurt(DamageSource.playerAttack(player), 100);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(SimpleCustomRenderer.create(this, new WrenchItemRenderer()));
	}

}
