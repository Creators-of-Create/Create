package com.simibubi.create.content.redstone.displayLink.source;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;

import net.createmod.catnip.data.IntAttached;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FactoryGaugeDisplaySource extends ValueListDisplaySource {

	@Override
	protected Stream<IntAttached<MutableComponent>> provideEntries(DisplayLinkContext context, int maxRows) {
		List<FactoryPanelPosition> panels = context.blockEntity().factoryPanelSupport.getLinkedPanels();
		if (panels.isEmpty())
			return Stream.empty();
		return panels.stream()
			.map(fpp -> createEntry(context.level(), fpp))
//			.sorted(IntAttached.comparator())
			.filter(Objects::nonNull)
			.limit(maxRows);
	}

	@Nullable
	public IntAttached<MutableComponent> createEntry(Level level, FactoryPanelPosition pos) {
		FactoryPanelBehaviour panel = FactoryPanelBehaviour.at(level, pos);
		if (panel == null)
			return null;

		ItemStack filter = panel.getFilter();

		int demand = panel.getAmount() * (panel.upTo ? 1 : filter.getMaxStackSize());
		String s = " ";

		if (demand != 0) {
			int promised = panel.getPromised();
			if (panel.satisfied)
				s = "\u2714";
			else if (promised != 0)
				s = "\u2191";
			else
				s = "\u25aa";
		}

		return IntAttached.with(panel.getLevelInStorage(), Component.literal(s + " ")
			.withStyle(style -> style.withColor(panel.getIngredientStatusColor()))
			.append(filter.getHoverName()
				.plainCopy()
				.withStyle(ChatFormatting.RESET)));
	}

	@Override
	protected String getTranslationKey() {
		return "gauge_status";
	}

	@Override
	protected boolean valueFirst() {
		return true;
	}

}
