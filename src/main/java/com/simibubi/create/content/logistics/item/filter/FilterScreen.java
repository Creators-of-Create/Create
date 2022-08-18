package com.simibubi.create.content.logistics.item.filter;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.gui.widget.Indicator;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;

public class FilterScreen extends AbstractFilterScreen<FilterContainer> {

	private static final String PREFIX = "gui.filter.";

	private Component allowN = Lang.translateDirect(PREFIX + "allow_list");
	private Component allowDESC = Lang.translateDirect(PREFIX + "allow_list.description");
	private Component denyN = Lang.translateDirect(PREFIX + "deny_list");
	private Component denyDESC = Lang.translateDirect(PREFIX + "deny_list.description");

	private Component respectDataN = Lang.translateDirect(PREFIX + "respect_data");
	private Component respectDataDESC = Lang.translateDirect(PREFIX + "respect_data.description");
	private Component ignoreDataN = Lang.translateDirect(PREFIX + "ignore_data");
	private Component ignoreDataDESC = Lang.translateDirect(PREFIX + "ignore_data.description");

	private IconButton whitelist, blacklist;
	private IconButton respectNBT, ignoreNBT;
	private Indicator whitelistIndicator, blacklistIndicator;
	private Indicator respectNBTIndicator, ignoreNBTIndicator;

	public FilterScreen(FilterContainer container, Inventory inv, Component title) {
		super(container, inv, title, AllGuiTextures.FILTER);
	}

	@Override
	protected void init() {
		setWindowOffset(-11, 5);
		super.init();

		int x = leftPos;
		int y = topPos;

		blacklist = new IconButton(x + 18, y + 73, AllIcons.I_BLACKLIST);
		blacklist.withCallback(() -> {
			menu.blacklist = true;
			sendOptionUpdate(Option.BLACKLIST);
		});
		blacklist.setToolTip(denyN);
		whitelist = new IconButton(x + 36, y + 73, AllIcons.I_WHITELIST);
		whitelist.withCallback(() -> {
			menu.blacklist = false;
			sendOptionUpdate(Option.WHITELIST);
		});
		whitelist.setToolTip(allowN);
		blacklistIndicator = new Indicator(x + 18, y + 67, Components.immutableEmpty());
		whitelistIndicator = new Indicator(x + 36, y + 67, Components.immutableEmpty());
		addRenderableWidgets(blacklist, whitelist, blacklistIndicator, whitelistIndicator);

		respectNBT = new IconButton(x + 60, y + 73, AllIcons.I_RESPECT_NBT);
		respectNBT.withCallback(() -> {
			menu.respectNBT = true;
			sendOptionUpdate(Option.RESPECT_DATA);
		});
		respectNBT.setToolTip(respectDataN);
		ignoreNBT = new IconButton(x + 78, y + 73, AllIcons.I_IGNORE_NBT);
		ignoreNBT.withCallback(() -> {
			menu.respectNBT = false;
			sendOptionUpdate(Option.IGNORE_DATA);
		});
		ignoreNBT.setToolTip(ignoreDataN);
		respectNBTIndicator = new Indicator(x + 60, y + 67, Components.immutableEmpty());
		ignoreNBTIndicator = new Indicator(x + 78, y + 67, Components.immutableEmpty());
		addRenderableWidgets(respectNBT, ignoreNBT, respectNBTIndicator, ignoreNBTIndicator);

		handleIndicators();
	}

	@Override
	protected List<IconButton> getTooltipButtons() {
		return Arrays.asList(blacklist, whitelist, respectNBT, ignoreNBT);
	}

	@Override
	protected List<MutableComponent> getTooltipDescriptions() {
		return Arrays.asList(denyDESC.plainCopy(), allowDESC.plainCopy(), respectDataDESC.plainCopy(), ignoreDataDESC.plainCopy());
	}

	@Override
	protected List<Indicator> getIndicators() {
		return Arrays.asList(blacklistIndicator, whitelistIndicator, respectNBTIndicator, ignoreNBTIndicator);
	}

	@Override
	protected boolean isButtonEnabled(IconButton button) {
		if (button == blacklist)
			return !menu.blacklist;
		if (button == whitelist)
			return menu.blacklist;
		if (button == respectNBT)
			return !menu.respectNBT;
		if (button == ignoreNBT)
			return menu.respectNBT;
		return true;
	}

	@Override
	protected boolean isIndicatorOn(Indicator indicator) {
		if (indicator == blacklistIndicator)
			return menu.blacklist;
		if (indicator == whitelistIndicator)
			return !menu.blacklist;
		if (indicator == respectNBTIndicator)
			return menu.respectNBT;
		if (indicator == ignoreNBTIndicator)
			return !menu.respectNBT;
		return false;
	}

}
