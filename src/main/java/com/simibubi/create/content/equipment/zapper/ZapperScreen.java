package com.simibubi.create.content.equipment.zapper;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllDataComponents;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.gui.AbstractSimiScreen;
import net.createmod.catnip.gui.element.GuiGameElement;
import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ZapperScreen extends AbstractSimiScreen {

	protected final Component patternSection = CreateLang.translateDirect("gui.terrainzapper.patternSection");

	protected AllGuiTextures background;
	protected ItemStack zapper;
	protected InteractionHand hand;

	protected float animationProgress;

	protected Component title;
	protected List<IconButton> patternButtons = new ArrayList<>(6);
	private IconButton confirmButton;
	protected int brightColor;
	protected int fontColor;

	protected PlacementPatterns currentPattern;

	public ZapperScreen(AllGuiTextures background, ItemStack zapper, InteractionHand hand) {
		this.background = background;
		this.zapper = zapper;
		this.hand = hand;
		title = CommonComponents.EMPTY;
		brightColor = 0xFEFEFE;
		fontColor = AllGuiTextures.FONT_COLOR;

		currentPattern = zapper.getOrDefault(AllDataComponents.PLACEMENT_PATTERN, PlacementPatterns.Solid);
	}

	@Override
	protected void init() {
		setWindowSize(background.getWidth(), background.getHeight());
		setWindowOffset(-10, 0);
		super.init();

		animationProgress = 0;

		int x = guiLeft;
		int y = guiTop;

		confirmButton =
			new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
		confirmButton.withCallback(this::onClose);
		addRenderableWidget(confirmButton);

		patternButtons.clear();
		for (int row = 0; row <= 1; row++) {
			for (int col = 0; col <= 2; col++) {
				int id = patternButtons.size();
				PlacementPatterns pattern = PlacementPatterns.values()[id];
				IconButton patternButton = new IconButton(x + background.getWidth() - 76 + col * 18, y + 21 + row * 18, pattern.icon);
				patternButton.withCallback(() -> {
					patternButtons.forEach(b -> b.green = false);
					patternButton.green = true;
					currentPattern = pattern;
				});
				patternButton.setToolTip(CreateLang.translateDirect("gui.terrainzapper.pattern." + pattern.translationKey));
				patternButtons.add(patternButton);
			}
		}

		patternButtons.get(currentPattern.ordinal()).green = true;

		addRenderableWidgets(patternButtons);
	}

	@Override
	protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.render(graphics, x, y);
		drawOnBackground(graphics, x, y);

		renderBlock(graphics, x, y);
		renderZapper(graphics, x, y);
	}

	protected void drawOnBackground(GuiGraphics graphics, int x, int y) {
		graphics.drawString(font, title, x + (background.getWidth() - font.width(title)) / 2, y + 4, 0x54214F, false);
	}

	@Override
	public void tick() {
		super.tick();
		animationProgress += 5;
	}

	@Override
	public void removed() {
		ConfigureZapperPacket packet = getConfigurationPacket();
		packet.configureZapper(zapper);
		CatnipServices.NETWORK.sendToServer(packet);
	}

	protected void renderZapper(GuiGraphics graphics, int x, int y) {
		GuiGameElement.of(zapper)
			.scale(4)
			.at(x + background.getWidth(), y + background.getHeight() - 48, -200)
			.render(graphics);
	}

	protected void renderBlock(GuiGraphics graphics, int x, int y) {
		PoseStack ms = graphics.pose();
		ms.pushPose();
		ms.translate(x + 32, y + 42, 120);
		ms.mulPose(Axis.XP.rotationDegrees(-25f));
		ms.mulPose(Axis.YP.rotationDegrees(-45f));
		ms.scale(20, 20, 20);

		BlockState state = zapper.getOrDefault(AllDataComponents.SHAPER_BLOCK_USED, Blocks.AIR.defaultBlockState());;

		GuiGameElement.of(state)
			.render(graphics);
		ms.popPose();
	}

	protected abstract ConfigureZapperPacket getConfigurationPacket();

}
