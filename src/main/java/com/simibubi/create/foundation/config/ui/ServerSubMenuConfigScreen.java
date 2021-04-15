package com.simibubi.create.foundation.config.ui;

import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerSubMenuConfigScreen extends SubMenuConfigScreen {

	protected PonderButton missingPermissions = null;

	public ServerSubMenuConfigScreen(Screen parent, ForgeConfigSpec configSpec) {
		super(parent, configSpec);
	}

	public ServerSubMenuConfigScreen(Screen parent, ForgeConfigSpec configSpec, UnmodifiableConfig configGroup) {
		super(parent, configSpec, configGroup);
	}

	@Override
	protected void init() {
		super.init();

		list.isForServer = true;

		if (client != null && client.player != null && client.player.hasPermissionLevel(2))
			return;

		list.children().forEach(e -> e.setEditable(false));

		int col1 = 0xff_f78888;
		int col2 = 0xff_cc2020;

		missingPermissions = new PonderButton(width - 30, height - 50, () -> {})
				.showing(new DelegatedStencilElement()
						.withStencilRenderer((ms, w, h) -> AllIcons.I_MTD_CLOSE.draw(ms, 0, 0))
						.withElementRenderer((ms, w, h) -> UIRenderHelper.angledGradient(ms, 90, 8, 0, 16, 16, col1, col2))
				).customColors(col1, col2);
		missingPermissions.fade(1);
		missingPermissions.getToolTip().add(new StringTextComponent("Locked").formatted(TextFormatting.BOLD));
		missingPermissions.getToolTip().addAll(TooltipHelper.cutStringTextComponent("You don't have enough permissions to edit the server config. You can still look at the current values here though.", TextFormatting.GRAY, TextFormatting.GRAY));

		widgets.add(missingPermissions);
	}
}
