package com.simibubi.create.content.logistics.filter;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.logistics.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;

public class FilterScreen extends AbstractFilterScreen<FilterMenu> {

	private static final String PREFIX = "gui.filter.";

	private Component allowN = CreateLang.translateDirect(PREFIX + "allow_list");
	private Component allowDESC = CreateLang.translateDirect(PREFIX + "allow_list.description");
	private Component denyN = CreateLang.translateDirect(PREFIX + "deny_list");
	private Component denyDESC = CreateLang.translateDirect(PREFIX + "deny_list.description");

	private Component respectDataN = CreateLang.translateDirect(PREFIX + "respect_data");
	private Component respectDataDESC = CreateLang.translateDirect(PREFIX + "respect_data.description");
	private Component ignoreDataN = CreateLang.translateDirect(PREFIX + "ignore_data");
	private Component ignoreDataDESC = CreateLang.translateDirect(PREFIX + "ignore_data.description");

	private IconButton whitelist, blacklist;
	private IconButton respectNBT, ignoreNBT;

	public FilterScreen(FilterMenu menu, Inventory inv, Component title) {
		super(menu, inv, title, AllGuiTextures.FILTER);
	}

	@Override
	protected void init() {
		setWindowOffset(-11, 5);
		super.init();

		int x = leftPos;
		int y = topPos;

		blacklist = new IconButton(x + 18, y + 75, AllIcons.I_BLACKLIST);
		blacklist.withCallback(() -> {
			menu.blacklist = true;
			sendOptionUpdate(Option.BLACKLIST);
		});
		blacklist.setToolTip(denyN);
		whitelist = new IconButton(x + 36, y + 75, AllIcons.I_WHITELIST);
		whitelist.withCallback(() -> {
			menu.blacklist = false;
			sendOptionUpdate(Option.WHITELIST);
		});
		whitelist.setToolTip(allowN);
		addRenderableWidgets(blacklist, whitelist);

		respectNBT = new IconButton(x + 60, y + 75, AllIcons.I_RESPECT_NBT);
		respectNBT.withCallback(() -> {
			menu.respectNBT = true;
			sendOptionUpdate(Option.RESPECT_DATA);
		});
		respectNBT.setToolTip(respectDataN);
		ignoreNBT = new IconButton(x + 78, y + 75, AllIcons.I_IGNORE_NBT);
		ignoreNBT.withCallback(() -> {
			menu.respectNBT = false;
			sendOptionUpdate(Option.IGNORE_DATA);
		});
		ignoreNBT.setToolTip(ignoreDataN);
		addRenderableWidgets(respectNBT, ignoreNBT);

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
	protected int getTitleColor() {
		return 0x303030;
	}
}
