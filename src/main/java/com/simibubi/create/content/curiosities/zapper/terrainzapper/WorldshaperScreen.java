package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.List;
import java.util.Vector;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.curiosities.zapper.ConfigureZapperPacket;
import com.simibubi.create.content.curiosities.zapper.ZapperScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Indicator.State;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.gui.widgets.SelectionScrollInput;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants;

public class WorldshaperScreen extends ZapperScreen {

	protected final ITextComponent placementSection = Lang.translate("gui.terrainzapper.placement");
	protected final ITextComponent toolSection = Lang.translate("gui.terrainzapper.tool");
	protected final List<ITextComponent> brushOptions =
		Lang.translatedOptions("gui.terrainzapper.brush", "cuboid", "sphere", "cylinder", "surface", "cluster");

	protected Vector<IconButton> toolButtons;
	protected Vector<IconButton> placementButtons;

	protected ScrollInput brushInput;
	protected Label brushLabel;
	protected Vector<ScrollInput> brushParams = new Vector<>(3);
	protected Vector<Label> brushParamLabels = new Vector<>(3);
	protected IconButton followDiagonals;
	protected IconButton acrossMaterials;
	protected Indicator followDiagonalsIndicator;
	protected Indicator acrossMaterialsIndicator;

	protected TerrainBrushes currentBrush;
	protected int[] currentBrushParams = new int[] { 1, 1, 1 };
	protected boolean currentFollowDiagonals;
	protected boolean currentAcrossMaterials;
	protected TerrainTools currentTool;
	protected PlacementOptions currentPlacement;

	public WorldshaperScreen(ItemStack zapper, Hand hand) {
		super(AllGuiTextures.TERRAINZAPPER, zapper, hand);
		fontColor = 0x767676;
		title = zapper.getHoverName();

		CompoundNBT nbt = zapper.getOrCreateTag();
		currentBrush = NBTHelper.readEnum(nbt, "Brush", TerrainBrushes.class);
		if (nbt.contains("BrushParams", Constants.NBT.TAG_COMPOUND)) {
			BlockPos paramsData = NBTUtil.readBlockPos(nbt.getCompound("BrushParams"));
			currentBrushParams[0] = paramsData.getX();
			currentBrushParams[1] = paramsData.getY();
			currentBrushParams[2] = paramsData.getZ();
			if (currentBrushParams[1] == 0) {
				currentFollowDiagonals = true;
			}
			if (currentBrushParams[2] == 0) {
				currentAcrossMaterials = true;
			}
		}
		currentTool = NBTHelper.readEnum(nbt, "Tool", TerrainTools.class);
		currentPlacement = NBTHelper.readEnum(nbt, "Placement", PlacementOptions.class);
	}

	@Override
	protected void init() {
		super.init();

		int x = guiLeft;
		int y = guiTop;

		brushLabel = new Label(x + 61, y + 25, StringTextComponent.EMPTY).withShadow();
		brushInput = new SelectionScrollInput(x + 56, y + 20, 77, 18).forOptions(brushOptions)
			.titled(Lang.translate("gui.terrainzapper.brush"))
			.writingTo(brushLabel)
			.calling(brushIndex -> {
				currentBrush = TerrainBrushes.values()[brushIndex];
				initBrushParams(x, y);
			});

		brushInput.setState(currentBrush.ordinal());

		widgets.add(brushLabel);
		widgets.add(brushInput);

		initBrushParams(x, y);
	}

	protected void initBrushParams(int x, int y) {
		Brush currentBrush = this.currentBrush.get();

		// Brush Params

		widgets.removeAll(brushParamLabels);
		widgets.removeAll(brushParams);

		brushParamLabels.clear();
		brushParams.clear();

		for (int index = 0; index < 3; index++) {
			Label label = new Label(x + 65 + 20 * index, y + 45, StringTextComponent.EMPTY).withShadow();

			final int finalIndex = index;
			ScrollInput input = new ScrollInput(x + 56 + 20 * index, y + 40, 18, 18)
				.withRange(currentBrush.getMin(index), currentBrush.getMax(index) + 1)
				.writingTo(label)
				.titled(currentBrush.getParamLabel(index)
					.plainCopy())
				.calling(state -> {
					currentBrushParams[finalIndex] = state;
					label.x = x + 65 + 20 * finalIndex - font.width(label.text) / 2;
				});
			input.setState(currentBrushParams[index]);
			input.onChanged();

			if (index >= currentBrush.amtParams) {
				input.visible = false;
				label.visible = false;
				input.active = false;
			}

			brushParamLabels.add(label);
			brushParams.add(input);
		}

		widgets.addAll(brushParamLabels);
		widgets.addAll(brushParams);

		// Connectivity Options

		if (followDiagonals != null) {
			widgets.remove(followDiagonals);
			widgets.remove(followDiagonalsIndicator);
			widgets.remove(acrossMaterials);
			widgets.remove(acrossMaterialsIndicator);
			followDiagonals = null;
			followDiagonalsIndicator = null;
			acrossMaterials = null;
			acrossMaterialsIndicator = null;
		}

		if (currentBrush.hasConnectivityOptions()) {
			int x1 = x + 7 + 4 * 18;
			int y1 = y + 79;
			followDiagonalsIndicator = new Indicator(x1, y1 - 6, StringTextComponent.EMPTY);
			followDiagonals = new IconButton(x1, y1, AllIcons.I_FOLLOW_DIAGONAL);
			x1 += 18;
			acrossMaterialsIndicator = new Indicator(x1, y1 - 6, StringTextComponent.EMPTY);
			acrossMaterials = new IconButton(x1, y1, AllIcons.I_FOLLOW_MATERIAL);

			followDiagonals.setToolTip(Lang.translate("gui.terrainzapper.searchDiagonal"));
			acrossMaterials.setToolTip(Lang.translate("gui.terrainzapper.searchFuzzy"));
			widgets.add(followDiagonals);
			widgets.add(followDiagonalsIndicator);
			widgets.add(acrossMaterials);
			widgets.add(acrossMaterialsIndicator);
			if (currentFollowDiagonals)
				followDiagonalsIndicator.state = State.ON;
			if (currentAcrossMaterials)
				acrossMaterialsIndicator.state = State.ON;
		}

		// Tools

		if (toolButtons != null)
			widgets.removeAll(toolButtons);

		TerrainTools[] toolValues = currentBrush.getSupportedTools();
		toolButtons = new Vector<>(toolValues.length);
		for (int id = 0; id < toolValues.length; id++) {
			TerrainTools tool = toolValues[id];
			toolButtons.add(new IconButton(x + 7 + id * 18, y + 79, tool.icon));
			toolButtons.get(id)
				.setToolTip(Lang.translate("gui.terrainzapper.tool." + tool.translationKey));
		}

		int toolIndex = -1;
		for (int i = 0; i < toolValues.length; i++)
			if (currentTool == toolValues[i])
				toolIndex = i;
		if (toolIndex == -1) {
			currentTool = toolValues[0];
			toolIndex = 0;
		}
		toolButtons.get(toolIndex).active = false;

		widgets.addAll(toolButtons);

		// Placement Options

		if (placementButtons != null)
			widgets.removeAll(placementButtons);

		if (currentBrush.hasPlacementOptions()) {
			PlacementOptions[] placementValues = PlacementOptions.values();
			placementButtons = new Vector<>(placementValues.length);
			for (int id = 0; id < placementValues.length; id++) {
				PlacementOptions option = placementValues[id];
				placementButtons.add(new IconButton(x + 136 + id * 18, y + 79, option.icon));
				placementButtons.get(id)
					.setToolTip(Lang.translate("gui.terrainzapper.placement." + option.translationKey));
			}

			placementButtons.get(currentPlacement.ordinal()).active = false;

			widgets.addAll(placementButtons);
		}
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		TerrainTools[] supportedTools = currentBrush.get()
			.getSupportedTools();
		for (IconButton toolButton : toolButtons) {
			if (toolButton.isHovered()) {
				toolButtons.forEach(b -> b.active = true);
				toolButton.active = false;
				toolButton.playDownSound(minecraft.getSoundManager());
				currentTool = supportedTools[toolButtons.indexOf(toolButton)];
			}
		}

		if (placementButtons != null) {
			for (IconButton placementButton : placementButtons) {
				if (placementButton.isHovered()) {
					placementButtons.forEach(b -> b.active = true);
					placementButton.active = false;
					placementButton.playDownSound(minecraft.getSoundManager());
					currentPlacement = PlacementOptions.values()[placementButtons.indexOf(placementButton)];
				}
			}
		}

		if (followDiagonals != null && followDiagonals.isHovered()) {
			followDiagonalsIndicator.state = followDiagonalsIndicator.state == State.OFF ? State.ON : State.OFF;
			currentFollowDiagonals = !currentFollowDiagonals;
		}
		if (acrossMaterials != null && acrossMaterials.isHovered()) {
			acrossMaterialsIndicator.state = acrossMaterialsIndicator.state == State.OFF ? State.ON : State.OFF;
			currentAcrossMaterials = !currentAcrossMaterials;
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void drawOnBackground(MatrixStack matrixStack, int x, int y) {
		super.drawOnBackground(matrixStack, x, y);

		Brush currentBrush = this.currentBrush.get();
		for (int index = 2; index >= currentBrush.amtParams; index--)
			AllGuiTextures.TERRAINZAPPER_INACTIVE_PARAM.draw(matrixStack, x + 56 + 20 * index, y + 40);

		font.draw(matrixStack, toolSection, x + 7, y + 69, fontColor);
		if (currentBrush.hasPlacementOptions())
			font.draw(matrixStack, placementSection, x + 136, y + 69, fontColor);
	}

	@Override
	protected ConfigureZapperPacket getConfigurationPacket() {
		int brushParamX = currentBrushParams[0];
		int brushParamY = followDiagonalsIndicator != null ? followDiagonalsIndicator.state == State.ON ? 0 : 1
			: currentBrushParams[1];
		int brushParamZ = acrossMaterialsIndicator != null ? acrossMaterialsIndicator.state == State.ON ? 0 : 1
			: currentBrushParams[2];
		return new ConfigureWorldshaperPacket(hand, currentPattern, currentBrush, brushParamX, brushParamY, brushParamZ, currentTool, currentPlacement);
	}

}
