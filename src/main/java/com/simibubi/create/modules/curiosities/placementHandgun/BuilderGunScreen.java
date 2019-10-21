package com.simibubi.create.modules.curiosities.placementHandgun;

import java.util.Collections;
import java.util.Vector;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.AllPackets;
import com.simibubi.create.ScreenResources;
import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Indicator.State;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.model.data.EmptyModelData;

@SuppressWarnings("deprecation")
public class BuilderGunScreen extends AbstractSimiScreen {

	private ItemStack item;
	private boolean offhand;
	private float animationProgress;

	private final String title = Lang.translate("gui.blockzapper.title");
	private final String patternSection = Lang.translate("gui.blockzapper.patternSection");
	private final String needsUpgradedAmplifier = Lang.translate("gui.blockzapper.needsUpgradedAmplifier");

	private IconButton replaceModeButton;
	private Indicator replaceModeIndicator;
	private IconButton spreadDiagonallyButton;
	private Indicator spreadDiagonallyIndicator;
	private IconButton spreadMaterialButton;
	private Indicator spreadMaterialIndicator;

	private ScrollInput spreadRangeInput;
	private Label spreadRangeLabel;

	private Vector<IconButton> patternButtons;

	public BuilderGunScreen(ItemStack handgun, boolean offhand) {
		super();
		item = handgun;
		this.offhand = offhand;
	}

	@Override
	protected void init() {
		animationProgress = 0;
		setWindowSize(ScreenResources.PLACEMENT_GUN.width + 40, ScreenResources.PLACEMENT_GUN.height);
		super.init();
		int i = guiLeft - 20;
		int j = guiTop;

		CompoundNBT nbt = item.getOrCreateTag();

		widgets.clear();
		replaceModeIndicator = new Indicator(i + 51, j + 36, "");
		replaceModeButton = new IconButton(i + 51, j + 41, ScreenResources.ICON_REPLACE_SOLID);
		if (nbt.contains("Replace") && nbt.getBoolean("Replace"))
			replaceModeIndicator.state = State.ON;
		replaceModeButton.setToolTip(Lang.translate("gui.blockzapper.replaceMode"));

		spreadDiagonallyIndicator = new Indicator(i + 74, j + 36, "");
		spreadDiagonallyButton = new IconButton(i + 74, j + 41, ScreenResources.ICON_FOLLOW_DIAGONAL);
		if (nbt.contains("SearchDiagonal") && nbt.getBoolean("SearchDiagonal"))
			spreadDiagonallyIndicator.state = State.ON;
		spreadDiagonallyButton.setToolTip(Lang.translate("gui.blockzapper.searchDiagonal"));

		spreadMaterialIndicator = new Indicator(i + 92, j + 36, "");
		spreadMaterialButton = new IconButton(i + 92, j + 41, ScreenResources.ICON_FOLLOW_MATERIAL);
		if (nbt.contains("SearchFuzzy") && nbt.getBoolean("SearchFuzzy"))
			spreadMaterialIndicator.state = State.ON;
		spreadMaterialButton.setToolTip(Lang.translate("gui.blockzapper.searchFuzzy"));

		spreadRangeLabel = new Label(i + 119, j + 46, "").withShadow().withSuffix("m");
		spreadRangeInput = new ScrollInput(i + 115, j + 43, 22, 14).withRange(1, BuilderGunItem.getMaxAoe(item))
				.setState(1).titled(Lang.translate("gui.blockzapper.range")).writingTo(spreadRangeLabel);

		if (nbt.contains("SearchDistance"))
			spreadRangeInput.setState(nbt.getInt("SearchDistance"));
		if (BuilderGunItem.getMaxAoe(item) == 2)
			spreadRangeInput.getToolTip().add(1, TextFormatting.RED + needsUpgradedAmplifier);

		Collections.addAll(widgets, replaceModeButton, replaceModeIndicator, spreadDiagonallyButton,
				spreadDiagonallyIndicator, spreadMaterialButton, spreadMaterialIndicator, spreadRangeLabel,
				spreadRangeInput);

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
	public boolean mouseClicked(double x, double y, int button) {
		CompoundNBT nbt = item.getTag();

		if (replaceModeButton.isHovered()) {
			boolean mode = nbt.contains("Replace") && nbt.getBoolean("Replace");
			mode = !mode;
			replaceModeIndicator.state = mode ? State.ON : State.OFF;
			nbt.putBoolean("Replace", mode);
		}

		if (spreadDiagonallyButton.isHovered()) {
			boolean mode = nbt.contains("SearchDiagonal") && nbt.getBoolean("SearchDiagonal");
			mode = !mode;
			spreadDiagonallyIndicator.state = mode ? State.ON : State.OFF;
			nbt.putBoolean("SearchDiagonal", mode);
		}

		if (spreadMaterialButton.isHovered()) {
			boolean mode = nbt.contains("SearchFuzzy") && nbt.getBoolean("SearchFuzzy");
			mode = !mode;
			spreadMaterialIndicator.state = mode ? State.ON : State.OFF;
			nbt.putBoolean("SearchFuzzy", mode);
		}

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

	@Override
	public void onClose() {
		CompoundNBT nbt = item.getTag();
		nbt.putInt("SearchDistance", spreadRangeInput.getState());
		AllPackets.channel.sendToServer(new NbtPacket(item, offhand ? -2 : -1));
		super.onClose();
	}

	@Override
	public void tick() {
		super.tick();
		animationProgress += 5;
	}

	@Override
	protected void renderWindow(int mouseX, int mouseY, float partialTicks) {
		int i = guiLeft - 20;
		int j = guiTop;
		ScreenResources.PLACEMENT_GUN.draw(this, i, j);

		font.drawStringWithShadow(title, i + 8, j + 10, 0xCCDDFF);
		font.drawString(patternSection, i + 148, j + 11, ScreenResources.FONT_COLOR);

		minecraft.getTextureManager().bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableBlend();

		renderBlock();

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

		IBakedModel model = itemRenderer.getModelWithOverrides(item);
		model.handlePerspective(TransformType.FIXED);
		itemRenderer.renderItem(item, model);

		GlStateManager.disableAlphaTest();
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();

		GlStateManager.popMatrix();
		GlStateManager.popAttributes();
	}

	private void renderBlock() {
		GlStateManager.pushMatrix();
		BufferBuilder buffer = Tessellator.getInstance().getBuffer();
		buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		GlStateManager.translated(guiLeft + 1.7f, guiTop - 49, 120);
		GlStateManager.rotatef(-30f, .5f, .9f, -.1f);
		GlStateManager.scaled(20, -20, 20);

		BlockState state = Blocks.BEACON.getDefaultState();
		if (item.hasTag() && item.getTag().contains("BlockUsed"))
			state = NBTUtil.readBlockState(item.getTag().getCompound("BlockUsed"));

		minecraft.getBlockRendererDispatcher().renderBlock(state, new BlockPos(0, -5, 0), minecraft.world, buffer,
				minecraft.world.rand, EmptyModelData.INSTANCE);

		Tessellator.getInstance().draw();
		GlStateManager.popMatrix();
	}

}
