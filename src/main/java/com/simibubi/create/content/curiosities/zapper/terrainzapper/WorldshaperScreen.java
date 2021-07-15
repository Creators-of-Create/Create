package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.List;
import java.util.Vector;

import com.mojang.blaze3d.matrix.MatrixStack;
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

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class WorldshaperScreen extends ZapperScreen {

	protected final ITextComponent placementSection = Lang.translate("gui.terrainzapper.placement");
	protected final ITextComponent toolSection = Lang.translate("gui.terrainzapper.tool");
	protected final List<ITextComponent> brushOptions =
		Lang.translatedOptions("gui.terrainzapper.brush", "cuboid", "sphere", "cylinder", "surface", "cluster");

	protected Vector<IconButton> toolButtons;
	protected Vector<IconButton> placementButtons;

	protected ScrollInput brushInput;
	protected Label brushLabel;
	protected Vector<ScrollInput> brushParams;
	protected Vector<Label> brushParamLabels;
	protected IconButton followDiagonals;
	protected IconButton acrossMaterials;
	protected Indicator followDiagonalsIndicator;
	protected Indicator acrossMaterialsIndicator;

	private CompoundNBT nbt;

	public WorldshaperScreen(ItemStack zapper, boolean offhand) {
		super(AllGuiTextures.TERRAINZAPPER, zapper, offhand);
		fontColor = 0x767676;
		title = zapper.getHoverName();
		nbt = zapper.getOrCreateTag();
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
			.calling(brushIndex -> initBrushParams(x, y));
		if (nbt.contains("Brush"))
			brushInput.setState(NBTHelper.readEnum(nbt, "Brush", TerrainBrushes.class)
				.ordinal());

		widgets.add(brushLabel);
		widgets.add(brushInput);
		initBrushParams(x, y);
	}

	public void initBrushParams(int x, int y) {
		if (brushParams != null) {
			nbt.put("BrushParams", NBTUtil.writeBlockPos(new BlockPos(brushParams.get(0)
				.getState(),
				brushParams.get(1)
					.getState(),
				brushParams.get(2)
					.getState())));

			widgets.removeAll(brushParamLabels);
			widgets.removeAll(brushParams);
		}

		brushParamLabels = new Vector<>(3);
		brushParams = new Vector<>(3);
		BlockPos data = NBTUtil.readBlockPos(nbt.getCompound("BrushParams"));
		int[] params = new int[] { data.getX(), data.getY(), data.getZ() };
		Brush currentBrush = TerrainBrushes.values()[brushInput.getState()].get();
		for (int index = 0; index < 3; index++) {

			Label label = new Label(x + 65 + 20 * index, y + 45, StringTextComponent.EMPTY).withShadow();
			brushParamLabels.add(label);
			int indexFinal = index;
			ScrollInput input = new ScrollInput(x + 56 + 20 * index, y + 40, 18, 18)
				.withRange(currentBrush.getMin(index), currentBrush.getMax(index) + 1)
				.writingTo(label)
				.titled(currentBrush.getParamLabel(index)
					.plainCopy())
				.calling(state -> {
					label.x = x + 65 + 20 * indexFinal - font.width(label.text) / 2;
				});
			input.setState(params[index]);
			input.onChanged();
			if (index >= currentBrush.amtParams) {
				input.visible = false;
				label.visible = false;
				input.active = false;
			}

			brushParams.add(input);
		}

		widgets.addAll(brushParamLabels);
		widgets.addAll(brushParams);

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
			if (params[1] == 0)
				followDiagonalsIndicator.state = State.ON;
			if (params[2] == 0)
				acrossMaterialsIndicator.state = State.ON;
		}

		// TOOLS

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

		if (!nbt.contains("Tool"))
			NBTHelper.writeEnum(nbt, "Tool", toolValues[0]);
		int index = -1;
		TerrainTools tool = NBTHelper.readEnum(nbt, "Tool", TerrainTools.class);
		for (int i = 0; i < toolValues.length; i++)
			if (tool == toolValues[i])
				index = i;
		if (index == -1) {
			NBTHelper.writeEnum(nbt, "Tool", toolValues[0]);
			index = 0;
		}

		toolButtons.get(index).active = false;
		widgets.addAll(toolButtons);

		if (placementButtons != null)
			widgets.removeAll(placementButtons);
		if (!currentBrush.hasPlacementOptions())
			return;

		PlacementOptions[] placementValues = PlacementOptions.values();
		placementButtons = new Vector<>(placementValues.length);
		for (int id = 0; id < placementValues.length; id++) {
			PlacementOptions option = placementValues[id];
			placementButtons.add(new IconButton(x + 136 + id * 18, y + 79, option.icon));
			placementButtons.get(id)
				.setToolTip(Lang.translate("gui.terrainzapper.placement." + option.translationKey));
		}

		if (!nbt.contains("Placement"))
			NBTHelper.writeEnum(nbt, "Placement", placementValues[0]);
		int optionIndex = NBTHelper.readEnum(nbt, "Placement", PlacementOptions.class)
			.ordinal();
		if (optionIndex >= placementValues.length) {
			NBTHelper.writeEnum(nbt, "Placement", placementValues[0]);
			optionIndex = 0;
		}
		placementButtons.get(optionIndex).active = false;
		widgets.addAll(placementButtons);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		CompoundNBT nbt = zapper.getTag();
		TerrainBrushes brush = TerrainBrushes.values()[brushInput.getState()];
		TerrainTools[] supportedTools = brush.get()
			.getSupportedTools();

		if (placementButtons != null) {
			for (IconButton placementButton : placementButtons) {
				if (placementButton.isHovered()) {
					placementButtons.forEach(b -> b.active = true);
					placementButton.active = false;
					placementButton.playDownSound(Minecraft.getInstance()
						.getSoundManager());
					nbt.putString("Placement",
						PlacementOptions.values()[placementButtons.indexOf(placementButton)].name());
				}
			}
		}

		for (IconButton toolButton : toolButtons) {
			if (toolButton.isHovered()) {
				toolButtons.forEach(b -> b.active = true);
				toolButton.active = false;
				toolButton.playDownSound(Minecraft.getInstance()
					.getSoundManager());
				nbt.putString("Tool", supportedTools[toolButtons.indexOf(toolButton)].name());
			}
		}

		if (followDiagonals != null && followDiagonals.isHovered())
			followDiagonalsIndicator.state = followDiagonalsIndicator.state == State.OFF ? State.ON : State.OFF;
		if (acrossMaterials != null && acrossMaterials.isHovered())
			acrossMaterialsIndicator.state = acrossMaterialsIndicator.state == State.OFF ? State.ON : State.OFF;

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void drawOnBackground(MatrixStack matrixStack, int x, int y) {
		super.drawOnBackground(matrixStack, x, y);

		Brush currentBrush = TerrainBrushes.values()[brushInput.getState()].get();
		for (int index = 2; index >= currentBrush.amtParams; index--)
			AllGuiTextures.TERRAINZAPPER_INACTIVE_PARAM.draw(matrixStack, x + 56 + 20 * index, y + 40);

		font.draw(matrixStack, toolSection, x + 7, y + 69, fontColor);
		if (currentBrush.hasPlacementOptions())
			font.draw(matrixStack, placementSection, x + 136, y + 69, fontColor);
	}

	@Override
	protected void writeAdditionalOptions(CompoundNBT nbt) {
		super.writeAdditionalOptions(nbt);
		TerrainBrushes brush = TerrainBrushes.values()[brushInput.getState()];
		int param1 = brushParams.get(0)
			.getState();
		int param2 = followDiagonalsIndicator != null ? followDiagonalsIndicator.state == State.ON ? 0 : 1
			: brushParams.get(1)
				.getState();
		int param3 = acrossMaterialsIndicator != null ? acrossMaterialsIndicator.state == State.ON ? 0 : 1
			: brushParams.get(2)
				.getState();

		NBTHelper.writeEnum(nbt, "Brush", brush);
		nbt.put("BrushParams", NBTUtil.writeBlockPos(new BlockPos(param1, param2, param3)));
	}

}
