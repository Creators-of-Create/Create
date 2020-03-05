package com.simibubi.create.modules.curiosities.zapper;

import java.util.Vector;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllPackets;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.packet.NbtPacket;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.data.EmptyModelData;

@SuppressWarnings("deprecation")
public class ZapperScreen extends AbstractSimiScreen {

	protected ItemStack zapper;
	protected boolean offhand;
	protected float animationProgress;
	protected ScreenResources background;

	protected final String patternSection = Lang.translate("gui.blockzapper.patternSection");

	protected String title;
	protected Vector<IconButton> patternButtons;
	protected int brightColor;
	protected int fontColor;

	public ZapperScreen(ScreenResources background, ItemStack zapper, boolean offhand) {
		super();
		this.background = background;
		this.zapper = zapper;
		this.offhand = offhand;
		title = "";
		brightColor = 0xCCDDFF;
		fontColor = ScreenResources.FONT_COLOR;
	}

	@Override
	protected void init() {
		animationProgress = 0;
		setWindowSize(background.width + 40, background.height);
		super.init();
		widgets.clear();

		int i = guiLeft - 20;
		int j = guiTop;
		CompoundNBT nbt = zapper.getOrCreateTag();

		patternButtons = new Vector<>(6);
		for (int row = 0; row <= 1; row++) {
			for (int col = 0; col <= 2; col++) {
				int id = patternButtons.size();
				PlacementPatterns pattern = PlacementPatterns.values()[id];
				patternButtons.add(new IconButton(i + 147 + col * 18, j + 23 + row * 18, pattern.icon));
				patternButtons.get(id).setToolTip(Lang.translate("gui.blockzapper.pattern." + pattern.translationKey));
			}
		}

		if (nbt.contains("Pattern"))
			patternButtons.get(PlacementPatterns.valueOf(nbt.getString("Pattern")).ordinal()).active = false;

		widgets.addAll(patternButtons);
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		int i = guiLeft - 20;
		int j = guiTop;

		background.draw(this, i, j);
		drawOnBackground(i, j);

		minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableBlend();

		renderBlock();
		renderZapper();
	}

	protected void drawOnBackground(int i, int j) {
		font.drawStringWithShadow(title, i + 8, j + 10, brightColor);
		font.drawString(patternSection, i + 148, j + 11, fontColor);
	}

	@Override
	public void tick() {
		super.tick();
		animationProgress += 5;
	}

	@Override
	public void onClose() {
		CompoundNBT nbt = zapper.getTag();
		writeAdditionalOptions(nbt);
		AllPackets.channel.sendToServer(new NbtPacket(zapper, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND));
		super.onClose();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		CompoundNBT nbt = zapper.getTag();

		for (IconButton patternButton : patternButtons) {
			if (patternButton.isHovered()) {
				patternButtons.forEach(b -> b.active = true);
				patternButton.active = false;
				patternButton.playDownSound(Minecraft.getInstance().getSoundHandler());
				nbt.putString("Pattern", PlacementPatterns.values()[patternButtons.indexOf(patternButton)].name());
			}
		}

		return super.mouseClicked(x, y, button);
	}

	protected void renderZapper() {
		GlStateManager.pushLightingAttributes();
		GlStateManager.pushMatrix();

		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableBlend();
		GlStateManager.enableRescaleNormal();
		GlStateManager.enableAlphaTest();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);

		GlStateManager.translated((this.width - this.sWidth) / 2 + 260, this.height / 2 - this.sHeight / 4, 100);
		GlStateManager.rotatef(90 + 0.2f * animationProgress, 0, 1, 0);
		GlStateManager.rotatef(-40, .8f, 0, -.0f);
		GlStateManager.scaled(100, -100, 100);

		IBakedModel model = itemRenderer.getModelWithOverrides(zapper);
		model.handlePerspective(TransformType.FIXED);
		itemRenderer.renderItem(zapper, model);

		GlStateManager.disableAlphaTest();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();

		GlStateManager.popMatrix();
		GlStateManager.popAttributes();
	}

	protected void renderBlock() {
		GlStateManager.pushMatrix();
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		GlStateManager.translated(guiLeft + 1.7f, guiTop - 49, 120);
		GlStateManager.rotatef(-30f, .5f, .9f, -.1f);
		GlStateManager.scaled(20, -20, 20);

		BlockState state = Blocks.AIR.getDefaultState();
		if (zapper.hasTag() && zapper.getTag().contains("BlockUsed"))
			state = NBTUtil.readBlockState(zapper.getTag().getCompound("BlockUsed"));

		minecraft.getBlockRendererDispatcher().renderBlock(state, new BlockPos(0, -5, 0), minecraft.world, buffer,
				minecraft.world.rand, EmptyModelData.INSTANCE);

		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

	protected void writeAdditionalOptions(CompoundNBT nbt) {
	}

}
