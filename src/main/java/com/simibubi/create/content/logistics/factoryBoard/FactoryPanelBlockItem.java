package com.simibubi.create.content.logistics.factoryBoard;

import java.util.UUID;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.CreateLang;
import com.simibubi.create.foundation.utility.LegacyBlockEntityDataComponentBridge;
import com.simibubi.create.foundation.utility.LegacyNbtUtilsBridge;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class FactoryPanelBlockItem extends LogisticallyLinkedBlockItem {

	public FactoryPanelBlockItem(Block pBlock, Properties pProperties) {
		super(pBlock, pProperties);
	}

	@Override
	public InteractionResult place(BlockPlaceContext pContext) {
		ItemStack stack = pContext.getItemInHand();

		if (!isTuned(stack)) {
			AllSoundEvents.DENY.playOnServer(pContext.getLevel(), pContext.getClickedPos());
			pContext.getPlayer()
				.sendSystemMessage(CreateLang.translate("factory_panel.tune_before_placing")
					.component());
			return InteractionResult.FAIL;
		}

		return super.place(pContext);
	}

	@Override
	protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, Player player, ItemStack stack,
		BlockState state) {
		return super.updateCustomBlockEntityTag(pos, level, player, fixCtrlCopiedStack(stack), state);
	}

	public static ItemStack fixCtrlCopiedStack(ItemStack stack) {
		// Salvage frequency data from one of the panel slots
		if (isTuned(stack) && networkFromStack(stack) == null) {
			CompoundTag bet = LegacyBlockEntityDataComponentBridge.get(stack);
			UUID frequency = UUID.randomUUID();

			for (PanelSlot slot : PanelSlot.values()) {
				CompoundTag panelTag = bet.getCompoundOrEmpty(CreateLang.asId(slot.name()));
				if (panelTag.contains("Freq"))
					frequency = LegacyNbtUtilsBridge.loadUUID(panelTag.get("Freq"));
			}

			bet = new CompoundTag();
			bet.put("Freq", LegacyNbtUtilsBridge.createUUID(frequency));

			LegacyBlockEntityDataComponentBridge.set(stack, ((IBE<?>) ((BlockItem) stack.getItem()).getBlock()).getBlockEntityType(), bet);
		}

		return stack;
	}

}
