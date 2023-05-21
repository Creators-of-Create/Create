package com.simibubi.create.content.redstone.link.controller;

import java.util.function.Consumer;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.simibubi.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.utility.Couple;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.NetworkHooks;

public class LinkedControllerItem extends Item implements MenuProvider {

	public LinkedControllerItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext ctx) {
		Player player = ctx.getPlayer();
		if (player == null)
			return InteractionResult.PASS;
		Level world = ctx.getLevel();
		BlockPos pos = ctx.getClickedPos();
		BlockState hitState = world.getBlockState(pos);

		if (player.mayBuild()) {
			if (player.isShiftKeyDown()) {
				if (AllBlocks.LECTERN_CONTROLLER.has(hitState)) {
					if (!world.isClientSide)
						AllBlocks.LECTERN_CONTROLLER.get().withBlockEntityDo(world, pos, be ->
								be.swapControllers(stack, player, ctx.getHand(), hitState));
					return InteractionResult.SUCCESS;
				}
			} else {
				if (AllBlocks.REDSTONE_LINK.has(hitState)) {
					if (world.isClientSide)
						DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> this.toggleBindMode(ctx.getClickedPos()));
					player.getCooldowns()
							.addCooldown(this, 2);
					return InteractionResult.SUCCESS;
				}

				if (hitState.is(Blocks.LECTERN) && !hitState.getValue(LecternBlock.HAS_BOOK)) {
					if (!world.isClientSide) {
						ItemStack lecternStack = player.isCreative() ? stack.copy() : stack.split(1);
						AllBlocks.LECTERN_CONTROLLER.get().replaceLectern(hitState, world, pos, lecternStack);
					}
					return InteractionResult.SUCCESS;
				}

				if (AllBlocks.LECTERN_CONTROLLER.has(hitState))
					return InteractionResult.PASS;
			}
		}

		return use(world, player, ctx.getHand()).getResult();
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack heldItem = player.getItemInHand(hand);

		if (player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
			if (!world.isClientSide && player instanceof ServerPlayer && player.mayBuild())
				NetworkHooks.openGui((ServerPlayer) player, this, buf -> {
					buf.writeItem(heldItem);
				});
			return InteractionResultHolder.success(heldItem);
		}

		if (!player.isShiftKeyDown()) {
			if (world.isClientSide)
				DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::toggleActive);
			player.getCooldowns()
				.addCooldown(this, 2);
		}

		return InteractionResultHolder.pass(heldItem);
	}

	@OnlyIn(Dist.CLIENT)
	private void toggleBindMode(BlockPos pos) {
		LinkedControllerClientHandler.toggleBindMode(pos);
	}

	@OnlyIn(Dist.CLIENT)
	private void toggleActive() {
		LinkedControllerClientHandler.toggle();
	}

	public static ItemStackHandler getFrequencyItems(ItemStack stack) {
		ItemStackHandler newInv = new ItemStackHandler(12);
		if (AllItems.LINKED_CONTROLLER.get() != stack.getItem())
			throw new IllegalArgumentException("Cannot get frequency items from non-controller: " + stack);
		CompoundTag invNBT = stack.getOrCreateTagElement("Items");
		if (!invNBT.isEmpty())
			newInv.deserializeNBT(invNBT);
		return newInv;
	}

	public static Couple<RedstoneLinkNetworkHandler.Frequency> toFrequency(ItemStack controller, int slot) {
		ItemStackHandler frequencyItems = getFrequencyItems(controller);
		return Couple.create(Frequency.of(frequencyItems.getStackInSlot(slot * 2)),
			Frequency.of(frequencyItems.getStackInSlot(slot * 2 + 1)));
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
		ItemStack heldItem = player.getMainHandItem();
		return LinkedControllerMenu.create(id, inv, heldItem);
	}

	@Override
	public Component getDisplayName() {
		return getDescription();
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(SimpleCustomRenderer.create(this, new LinkedControllerItemRenderer()));
	}

}
