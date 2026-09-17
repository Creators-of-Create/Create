package com.simibubi.create.content.logistics.itemHatch;

import java.util.List;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.logistics.filter.FilterItem;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.filtering.FilteringBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ItemHatchBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation, Clearable {
	public FilteringBehaviour filtering;

	public ItemHatchBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
	}

	@Override
	public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
		behaviours.add(filtering = new FilteringBehaviour(this, new HatchFilterSlot()));
	}

	@Override
	public void clearContent() {
		filtering.setFilter(ItemStack.EMPTY);
	}

	@Override
	public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
		CreateLang.translate("tooltip.item_hatch.header")
			.forGoggles(tooltip);
		
		boolean hasFilter = addFilterTooltip(tooltip);
		if (!hasFilter) {
			tooltip.remove(0);
			return false;
		}
		return true;
	}

	private boolean addFilterTooltip(List<Component> tooltip) {
		// Get filter blocks and items
		ItemStack filterStack = filtering == null ? ItemStack.EMPTY : filtering.getFilter();
		// Verify if the item hatch has a filter
		if (filterStack.isEmpty())
			return false;

		tooltip.add(CommonComponents.EMPTY);
		List<Component> filterSummary;
		// If the filter is an item filter, use its summary, otherwise just show the item
		if (filterStack.getItem() instanceof FilterItem filterItem) {
			filterSummary = filterItem.makeSummary(filterStack);
		} else {
			CreateLang.translate("gui.filter.allow_item")
				.style(ChatFormatting.GOLD)
				.forGoggles(tooltip);
			filterSummary = List.of(Component.literal("- ").append(filterStack.getHoverName())
				.withStyle(ChatFormatting.GRAY));
		}
		// If the filter summary is not empty, add it to the tooltip
		if (!filterSummary.isEmpty()) {
			// Add the filter type (allow or deny) in the goggles tooltip format
			CreateLang.builder()
				.add(filterSummary.get(0))
				.forGoggles(tooltip);
			// Add the filter blocks and items in the goggles tooltip format
			for (int i = 1; i < filterSummary.size(); i++)
				CreateLang.builder()
					.add(filterSummary.get(i))
					.forGoggles(tooltip, 1);
		} else {
			CreateLang.translate("gui.filter.empty")
				.style(ChatFormatting.DARK_GRAY)
				.forGoggles(tooltip);
		}
		return true;
	}
}
