package com.simibubi.create.content.curiosities.zapper.terrainzapper;

import java.util.List;
import java.util.Vector;

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

public class WorldshaperScreen extends ZapperScreen {

	protected final String placementSection = Lang.translate("gui.terrainzapper.placement");
	protected final String toolSection = Lang.translate("gui.terrainzapper.tool");
	protected final List<String> brushOptions =
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
		fontColor = 0x767676;
		title = Lang.translate("gui.terrainzapper.title");
		nbt = zapper.getOrCreateTag();
	}

	@Override
	protected void init() {
		super.init();

		i = guiLeft - 20;
		j = guiTop;

		brushLabel = new Label(i + 61, j + 23, "").withShadow();
		brushInput = new SelectionScrollInput(i + 56, j + 18, 77, 18).forOptions(brushOptions)
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
			toolButtons.add(new IconButton(i + 7 + id * 18, j + 77, tool.icon));
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
			placementButtons.add(new IconButton(i + 136 + id * 18, j + 77, option.icon));
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

			Label label = new Label(i + 65 + 20 * index, j + 43, "").withShadow();
			brushParamLabels.add(label);
			int indexFinal = index;
			ScrollInput input = new ScrollInput(i + 56 + 20 * index, j + 38, 18, 18)
				.withRange(currentBrush.getMin(index), currentBrush.getMax(index) + 1)
				.writingTo(label)
				.titled(currentBrush.getParamLabel(index))
				.calling(state -> {
					label.x = i + 65 + 20 * indexFinal - font.getStringWidth(label.text) / 2;
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
	protected void drawOnBackground(int i, int j) {
		super.drawOnBackground(i, j);

		Brush currentBrush = TerrainBrushes.values()[brushInput.getState()].get();
		for (int index = 2; index >= currentBrush.amtParams; index--) 
			AllGuiTextures.TERRAINZAPPER_INACTIVE_PARAM.draw(i + 56 + 20 * index, j + 38);

		font.drawString(toolSection, i + 7, j + 66, fontColor);
		font.drawString(placementSection, i + 136, j + 66, fontColor);
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
