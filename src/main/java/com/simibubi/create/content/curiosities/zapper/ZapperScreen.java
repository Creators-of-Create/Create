package com.simibubi.create.content.curiosities.zapper;

import java.util.Vector;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.GuiGameElement;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public abstract class ZapperScreen extends AbstractSimiScreen {

	protected final ITextComponent patternSection = Lang.translate("gui.terrainzapper.patternSection");

	protected AllGuiTextures background;
	protected ItemStack zapper;
	protected Hand hand;

	protected float animationProgress;

	protected ITextComponent title;
	protected Vector<IconButton> patternButtons = new Vector<>(6);
	private IconButton confirmButton;
	protected int brightColor;
	protected int fontColor;

	protected PlacementPatterns currentPattern;

	public ZapperScreen(AllGuiTextures background, ItemStack zapper, Hand hand) {
		this.background = background;
		this.zapper = zapper;
		this.hand = hand;
		title = StringTextComponent.EMPTY;
		brightColor = 0xFEFEFE;
		fontColor = AllGuiTextures.FONT_COLOR;

		CompoundNBT nbt = zapper.getOrCreateTag();
		currentPattern = NBTHelper.readEnum(nbt, "Pattern", PlacementPatterns.class);
	}

	@Override
	protected void init() {
		setWindowSize(background.width, background.height);
		setWindowOffset(-10, 0);
		super.init();
		widgets.clear();

		animationProgress = 0;

		int x = guiLeft;
		int y = guiTop;

		confirmButton =
			new IconButton(x + background.width - 33, y + background.height - 24, AllIcons.I_CONFIRM);
		widgets.add(confirmButton);

		patternButtons.clear();
		for (int row = 0; row <= 1; row++) {
			for (int col = 0; col <= 2; col++) {
				int id = patternButtons.size();
				PlacementPatterns pattern = PlacementPatterns.values()[id];
				patternButtons
					.add(new IconButton(x + background.width - 76 + col * 18, y + 21 + row * 18, pattern.icon));
				patternButtons.get(id)
					.setToolTip(Lang.translate("gui.terrainzapper.pattern." + pattern.translationKey));
			}
		}

		patternButtons.get(currentPattern.ordinal()).active = false;

		widgets.addAll(patternButtons);
	}

	@Override
	protected void renderWindow(MatrixStack ms, int mouseX, int mouseY, float partialTicks) {
		int x = guiLeft;
		int y = guiTop;

		background.draw(ms, this, x, y);
		drawOnBackground(ms, x, y);

		renderBlock(ms, x, y);
		renderZapper(ms, x, y);
	}

	protected void drawOnBackground(MatrixStack ms, int x, int y) {
		font.draw(ms, title, x + 11, y + 4, 0x54214F);
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
		AllPackets.channel.sendToServer(packet);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		for (IconButton patternButton : patternButtons) {
			if (patternButton.isHovered()) {
				patternButtons.forEach(b -> b.active = true);
				patternButton.active = false;
				patternButton.playDownSound(minecraft.getSoundManager());
				currentPattern = PlacementPatterns.values()[patternButtons.indexOf(patternButton)];
			}
		}

		if (confirmButton.isHovered()) {
			onClose();
			return true;
		}

		return super.mouseClicked(x, y, button);
	}

	protected void renderZapper(MatrixStack ms, int x, int y) {
		GuiGameElement.of(zapper)
				.scale(4)
				.at(x + background.width, y + background.height - 48, -200)
				.render(ms);
	}

	protected void renderBlock(MatrixStack ms, int x, int y) {
		ms.pushPose();
		ms.translate(x + 32, y + 42, 120);
		ms.mulPose(new Vector3f(1f, 0, 0).rotationDegrees(-25f));
		ms.mulPose(new Vector3f(0, 1f, 0).rotationDegrees(-45f));
		ms.scale(20, 20, 20);

		BlockState state = Blocks.AIR.defaultBlockState();
		if (zapper.hasTag() && zapper.getTag()
			.contains("BlockUsed"))
			state = NBTUtil.readBlockState(zapper.getTag()
				.getCompound("BlockUsed"));

		GuiGameElement.of(state)
			.render(ms);
		ms.popPose();
	}

	protected abstract ConfigureZapperPacket getConfigurationPacket();

}
