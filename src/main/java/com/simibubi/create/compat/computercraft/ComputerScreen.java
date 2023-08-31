package com.simibubi.create.compat.computercraft;

import com.simibubi.create.compat.Mods;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;
import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.gui.widget.AbstractSimiWidget;
import net.createmod.catnip.gui.widget.ElementWidget;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ComputerScreen extends AbstractSimiScreen {

	private final AllGuiTextures background = AllGuiTextures.COMPUTER;

	private final Supplier<Component> displayTitle;
	private final RenderWindowFunction additional;
	private final Screen previousScreen;
	private final Supplier<Boolean> hasAttachedComputer;

	private AbstractSimiWidget computerWidget;
	private IconButton confirmButton;

	public ComputerScreen(Component title, @Nullable RenderWindowFunction additional, Screen previousScreen, Supplier<Boolean> hasAttachedComputer) {
		this(title, () -> title, additional, previousScreen, hasAttachedComputer);
	}

	public ComputerScreen(Component title, Supplier<Component> displayTitle, @Nullable RenderWindowFunction additional, Screen previousScreen, Supplier<Boolean> hasAttachedComputer) {
		super(title);
		this.displayTitle = displayTitle;
		this.additional = additional;
		this.previousScreen = previousScreen;
		this.hasAttachedComputer = hasAttachedComputer;
	}

	@Override
	public void tick() {
		if (!hasAttachedComputer.get())
			minecraft.setScreen(previousScreen);

		super.tick();
	}

	@Override
	protected void init() {
		setWindowSize(background.getWidth(), background.getHeight());
		super.init();

		int x = guiLeft;
		int y = guiTop;

		Mods.COMPUTERCRAFT.executeIfInstalled(() -> () -> {
			computerWidget = new ElementWidget(x + 33, y + 38)
					.showingElement(GuiGameElement.of(Mods.COMPUTERCRAFT.getBlock("computer_advanced")));
			computerWidget.getToolTip().add(CreateLang.translate("gui.attached_computer.hint").component());
			addRenderableWidget(computerWidget);
		});

		confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(this::onClose);
		addRenderableWidget(confirmButton);
	}



	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(graphics, x, y);

		graphics.drawString(font, displayTitle.get(),
			Math.round(x + background.getWidth() / 2.0F - font.width(displayTitle.get()) / 2.0F), y + 4, 0x442000, false);
		graphics.drawWordWrap(font, CreateLang.translate("gui.attached_computer.controlled")
			.component(), x + 55, y + 32, 111, 0x7A7A7A);

		if (additional != null)
			additional.render(graphics, mouseX, mouseY, partialTicks, x, y, background);
	}

	@FunctionalInterface
	public interface RenderWindowFunction {

		void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks, int guiLeft, int guiTop, AllGuiTextures background);

	}

}
