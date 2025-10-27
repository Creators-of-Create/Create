package com.simibubi.create.content.logistics.factoryBoard;

import java.util.UUID;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
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
				.displayClientMessage(CreateLang.translate("factory_panel.tune_before_placing")
					.component(), true);
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
			CompoundTag bet = stack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
			UUID frequency = UUID.randomUUID();

			for (PanelSlot slot : PanelSlot.values()) {
				CompoundTag panelTag = bet.getCompound(CreateLang.asId(slot.name()));
				if (panelTag.hasUUID("Freq"))
					frequency = panelTag.getUUID("Freq");
			}

			bet = new CompoundTag();
			bet.putUUID("Freq", frequency);

			BlockEntity.addEntityType(bet, ((IBE<?>) ((BlockItem) stack.getItem()).getBlock()).getBlockEntityType());
			stack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(bet));
		}

		return stack;
	}

}
