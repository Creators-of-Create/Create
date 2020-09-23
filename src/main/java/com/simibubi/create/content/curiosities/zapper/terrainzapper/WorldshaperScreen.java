package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.List;
import java.util.Vector;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.curiosities.zapper.ZapperScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.widgets.IconButton;
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

public class WorldshaperScreen extends ZapperScreen {

	protected final ITextComponent placementSection = Lang.translate("gui.terrainzapper.placement");
	protected final ITextComponent toolSection = Lang.translate("gui.terrainzapper.tool");
	protected final List<ITextComponent> brushOptions =
		Lang.translatedOptions("gui.terrainzapper.brush", "cuboid", "sphere", "cylinder");

	protected Vector<IconButton> toolButtons;
	protected Vector<IconButton> placementButtons;

	protected ScrollInput brushInput;
	protected Label brushLabel;
	protected Vector<ScrollInput> brushParams;
	protected Vector<Label> brushParamLabels;
	private int i;
	private int j;
	private CompoundNBT nbt;

	public WorldshaperScreen(ItemStack zapper, boolean offhand) {
		super(AllGuiTextures.TERRAINZAPPER, zapper, offhand);
		brightColor = 0xDFF6FF;
		fontColor = 0x436B77;
		title = Lang.translate("gui.terrainzapper.title");
		nbt = zapper.getOrCreateTag();
	}

	@Override
	protected void init() {
		super.init();

		i = guiLeft - 20;
		j = guiTop;

		brushLabel = new Label(i + 58, j + 28, "").withShadow();
		brushInput = new SelectionScrollInput(i + 55, j + 25, 78, 14).forOptions(brushOptions)
			.titled(Lang.translate("gui.terrainzapper.brush"))
			.writingTo(brushLabel)
			.calling(this::brushChanged);
		if (nbt.contains("Brush"))
			brushInput.setState(NBTHelper.readEnum(nbt, "Brush", TerrainBrushes.class)
				.ordinal());

		widgets.add(brushLabel);
		widgets.add(brushInput);
		initBrushParams();

		toolButtons = new Vector<>(6);
		TerrainTools[] toolValues = TerrainTools.values();
		for (int id = 0; id < toolValues.length; id++) {
			TerrainTools tool = toolValues[id];
			toolButtons.add(new IconButton(i + 8 + id * 18, j + 76, tool.icon));
			toolButtons.get(id)
				.setToolTip(Lang.translate("gui.terrainzapper.tool." + tool.translationKey));
		}

		if (nbt.contains("Tool"))
			toolButtons.get(NBTHelper.readEnum(nbt, "Tool", TerrainTools.class)
				.ordinal()).active = false;
		widgets.addAll(toolButtons);

		placementButtons = new Vector<>(3);
		PlacementOptions[] placementValues = PlacementOptions.values();
		for (int id = 0; id < placementValues.length; id++) {
			PlacementOptions option = placementValues[id];
			placementButtons.add(new IconButton(i + 147 + id * 18, j + 76, option.icon));
			placementButtons.get(id)
				.setToolTip(Lang.translate("gui.terrainzapper.placement." + option.translationKey));
		}

		if (nbt.contains("Placement"))
			placementButtons.get(NBTHelper.readEnum(nbt, "Placement", PlacementOptions.class)
				.ordinal()).active = false;
		widgets.addAll(placementButtons);

	}

	public void initBrushParams() {
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

			Label label = new Label(i + 62 + 18 * index, j + 46, "").withShadow();
			brushParamLabels.add(label);
			int indexFinal = index;
			ScrollInput input = new ScrollInput(i + 55 + 18 * index, j + 43, 14, 14)
				.withRange(currentBrush.getMin(index), currentBrush.getMax(index) + 1)
				.writingTo(label)
				.titled(currentBrush.getParamLabel(index).copy())
				.calling(state -> {
					label.x = i + 62 + 18 * indexFinal - textRenderer.getWidth(label.text) / 2;
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
	}

	private void brushChanged(int brushIndex) {
		initBrushParams();
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		CompoundNBT nbt = zapper.getTag();

		for (IconButton placementButton : placementButtons) {
			if (placementButton.isHovered()) {
				placementButtons.forEach(b -> b.active = true);
				placementButton.active = false;
				placementButton.playDownSound(Minecraft.getInstance()
					.getSoundHandler());
				nbt.putString("Placement", PlacementOptions.values()[placementButtons.indexOf(placementButton)].name());
			}
		}

		for (IconButton toolButton : toolButtons) {
			if (toolButton.isHovered()) {
				toolButtons.forEach(b -> b.active = true);
				toolButton.active = false;
				toolButton.playDownSound(Minecraft.getInstance()
					.getSoundHandler());
				nbt.putString("Tool", TerrainTools.values()[toolButtons.indexOf(toolButton)].name());
			}
		}

		return super.mouseClicked(x, y, button);
	}

	@Override
	protected void drawOnBackground(MatrixStack matrixStack, int i, int j) {
		super.drawOnBackground(matrixStack, i, j);

		Brush currentBrush = TerrainBrushes.values()[brushInput.getState()].get();
		for (int index = 2; index >= currentBrush.amtParams; index--) {
			AllGuiTextures.TERRAINZAPPER_INACTIVE_PARAM.draw(matrixStack, i + 55 + index * 18, j + 43);
		}

		textRenderer.draw(matrixStack, toolSection, i + 8, j + 64, fontColor);
		textRenderer.draw(matrixStack, placementSection, i + 148, j + 64, fontColor);
	}

	@Override
	protected void writeAdditionalOptions(CompoundNBT nbt) {
		super.writeAdditionalOptions(nbt);
		NBTHelper.writeEnum(nbt, "Brush", TerrainBrushes.values()[brushInput.getState()]);
		nbt.put("BrushParams", NBTUtil.writeBlockPos(new BlockPos(brushParams.get(0)
			.getState(),
			brushParams.get(1)
				.getState(),
			brushParams.get(2)
				.getState())));
	}

}
