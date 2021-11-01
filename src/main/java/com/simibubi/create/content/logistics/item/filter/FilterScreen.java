package com.simibubi.create.content.logistics.item.filter;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class FilterScreen extends AbstractFilterScreen<FilterContainer> {

	private static final String PREFIX = "gui.filter.";

	private Component allowN = Lang.translate(PREFIX + "allow_list");
	private Component allowDESC = Lang.translate(PREFIX + "allow_list.description");
	private Component denyN = Lang.translate(PREFIX + "deny_list");
	private Component denyDESC = Lang.translate(PREFIX + "deny_list.description");

	private Component respectDataN = Lang.translate(PREFIX + "respect_data");
	private Component respectDataDESC = Lang.translate(PREFIX + "respect_data.description");
	private Component ignoreDataN = Lang.translate(PREFIX + "ignore_data");
	private Component ignoreDataDESC = Lang.translate(PREFIX + "ignore_data.description");

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
		blacklist.setToolTip(denyN);
		whitelist = new IconButton(x + 36, y + 73, AllIcons.I_WHITELIST);
		whitelist.setToolTip(allowN);
		blacklistIndicator = new Indicator(x + 18, y + 67, TextComponent.EMPTY);
		whitelistIndicator = new Indicator(x + 36, y + 67, TextComponent.EMPTY);
		widgets.addAll(Arrays.asList(blacklist, whitelist, blacklistIndicator, whitelistIndicator));

		respectNBT = new IconButton(x + 60, y + 73, AllIcons.I_RESPECT_NBT);
		respectNBT.setToolTip(respectDataN);
		ignoreNBT = new IconButton(x + 78, y + 73, AllIcons.I_IGNORE_NBT);
		ignoreNBT.setToolTip(ignoreDataN);
		respectNBTIndicator = new Indicator(x + 60, y + 67, TextComponent.EMPTY);
		ignoreNBTIndicator = new Indicator(x + 78, y + 67, TextComponent.EMPTY);
		widgets.addAll(Arrays.asList(respectNBT, ignoreNBT, respectNBTIndicator, ignoreNBTIndicator));

		handleIndicators();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean mouseClicked = super.mouseClicked(x, y, button);

		if (button != 0)
			return mouseClicked;

		if (blacklist.isHovered()) {
			menu.blacklist = true;
			sendOptionUpdate(Option.BLACKLIST);
			return true;
		}

		if (whitelist.isHovered()) {
			menu.blacklist = false;
			sendOptionUpdate(Option.WHITELIST);
			return true;
		}

		if (respectNBT.isHovered()) {
			menu.respectNBT = true;
			sendOptionUpdate(Option.RESPECT_DATA);
			return true;
		}

		if (ignoreNBT.isHovered()) {
			menu.respectNBT = false;
			sendOptionUpdate(Option.IGNORE_DATA);
			return true;
		}

		return mouseClicked;
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
