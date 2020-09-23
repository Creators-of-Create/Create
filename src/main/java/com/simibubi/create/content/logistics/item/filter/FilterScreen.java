package com.simibubi.create.content.logistics.item.filter;

import java.util.Arrays;
import java.util.List;

import com.simibubi.create.content.logistics.item.filter.FilterScreenPacket.Option;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class FilterScreen extends AbstractFilterScreen<FilterContainer> {

	private static final String PREFIX = "gui.filter.";

	private ITextComponent whitelistN = Lang.translate(PREFIX + "whitelist");
	private ITextComponent whitelistDESC = Lang.translate(PREFIX + "whitelist.description");
	private ITextComponent blacklistN = Lang.translate(PREFIX + "blacklist");
	private ITextComponent blacklistDESC = Lang.translate(PREFIX + "blacklist.description");

	private ITextComponent respectDataN = Lang.translate(PREFIX + "respect_data");
	private ITextComponent respectDataDESC = Lang.translate(PREFIX + "respect_data.description");
	private ITextComponent ignoreDataN = Lang.translate(PREFIX + "ignore_data");
	private ITextComponent ignoreDataDESC = Lang.translate(PREFIX + "ignore_data.description");

	private IconButton whitelist, blacklist;
	private IconButton respectNBT, ignoreNBT;
	private Indicator whitelistIndicator, blacklistIndicator;
	private Indicator respectNBTIndicator, ignoreNBTIndicator;

	public FilterScreen(FilterContainer container, PlayerInventory inv, ITextComponent title) {
		super(container, inv, title, AllGuiTextures.FILTER);
	}

	@Override
	protected void init() {
		super.init();
		int x = guiLeft;
		int y = guiTop;

		blacklist = new IconButton(x + 58, y + 72, AllIcons.I_BLACKLIST);
		blacklist.setToolTip(blacklistN);
		whitelist = new IconButton(x + 76, y + 72, AllIcons.I_WHITELIST);
		whitelist.setToolTip(whitelistN);
		blacklistIndicator = new Indicator(x + 58, y + 67, StringTextComponent.EMPTY);
		whitelistIndicator = new Indicator(x + 76, y + 67, StringTextComponent.EMPTY);
		widgets.addAll(Arrays.asList(blacklist, whitelist, blacklistIndicator, whitelistIndicator));

		respectNBT = new IconButton(x + 98, y + 72, AllIcons.I_RESPECT_NBT);
		respectNBT.setToolTip(respectDataN);
		ignoreNBT = new IconButton(x + 116, y + 72, AllIcons.I_IGNORE_NBT);
		ignoreNBT.setToolTip(ignoreDataN);
		respectNBTIndicator = new Indicator(x + 98, y + 67, StringTextComponent.EMPTY);
		ignoreNBTIndicator = new Indicator(x + 116, y + 67, StringTextComponent.EMPTY);
		widgets.addAll(Arrays.asList(respectNBT, ignoreNBT, respectNBTIndicator, ignoreNBTIndicator));
		handleIndicators();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		boolean mouseClicked = super.mouseClicked(x, y, button);

		if (button != 0)
			return mouseClicked;

		if (blacklist.isHovered()) {
			container.blacklist = true;
			sendOptionUpdate(Option.BLACKLIST);
			return true;
		}

		if (whitelist.isHovered()) {
			container.blacklist = false;
			sendOptionUpdate(Option.WHITELIST);
			return true;
		}

		if (respectNBT.isHovered()) {
			container.respectNBT = true;
			sendOptionUpdate(Option.RESPECT_DATA);
			return true;
		}

		if (ignoreNBT.isHovered()) {
			container.respectNBT = false;
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
	protected List<IFormattableTextComponent> getTooltipDescriptions() {
		return Arrays.asList(blacklistDESC.copy(), whitelistDESC.copy(), respectDataDESC.copy(), ignoreDataDESC.copy());
	}

	@Override
	protected boolean isButtonEnabled(IconButton button) {
		if (button == blacklist)
			return !container.blacklist;
		if (button == whitelist)
			return container.blacklist;
		if (button == respectNBT)
			return !container.respectNBT;
		if (button == ignoreNBT)
			return container.respectNBT;
		return true;
	}

	@Override
	protected boolean isIndicatorOn(Indicator indicator) {
		if (indicator == blacklistIndicator)
			return container.blacklist;
		if (indicator == whitelistIndicator)
			return !container.blacklist;
		if (indicator == respectNBTIndicator)
			return container.respectNBT;
		if (indicator == ignoreNBTIndicator)
			return !container.respectNBT;
		return false;
	}

}
