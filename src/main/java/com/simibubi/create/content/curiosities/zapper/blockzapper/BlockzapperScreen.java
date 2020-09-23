package com.simibubi.create.content.curiosities.zapper.blockzapper;

import java.util.Collections;

import com.simibubi.create.content.curiosities.zapper.ZapperScreen;
import com.simibubi.create.foundation.gui.AllGuiTextures;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widgets.IconButton;
import com.simibubi.create.foundation.gui.widgets.Indicator;
import com.simibubi.create.foundation.gui.widgets.Indicator.State;
import com.simibubi.create.foundation.gui.widgets.Label;
import com.simibubi.create.foundation.gui.widgets.ScrollInput;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public class BlockzapperScreen extends ZapperScreen {

	private final ITextComponent needsUpgradedAmplifier = Lang.translate("gui.blockzapper.needsUpgradedAmplifier");

	private IconButton replaceModeButton;
	private Indicator replaceModeIndicator;
	private IconButton spreadDiagonallyButton;
	private Indicator spreadDiagonallyIndicator;
	private IconButton spreadMaterialButton;
	private Indicator spreadMaterialIndicator;

	private ScrollInput spreadRangeInput;
	private Label spreadRangeLabel;
	
	public BlockzapperScreen(ItemStack zapper, boolean offhand) {
		super(AllGuiTextures.BLOCKZAPPER, zapper, offhand);
		title = Lang.translate("gui.blockzapper.title");
	}

	@Override
	protected void init() {
		super.init();

		int i = guiLeft - 20;
		int j = guiTop;
		CompoundNBT nbt = zapper.getOrCreateTag();

		replaceModeIndicator = new Indicator(i + 51, j + 36, StringTextComponent.EMPTY);
		replaceModeButton = new IconButton(i + 51, j + 41, AllIcons.I_REPLACE_SOLID);
		if (nbt.contains("Replace") && nbt.getBoolean("Replace"))
			replaceModeIndicator.state = State.ON;
		replaceModeButton.setToolTip(Lang.translate("gui.blockzapper.replaceMode"));

		spreadDiagonallyIndicator = new Indicator(i + 74, j + 36, StringTextComponent.EMPTY);
		spreadDiagonallyButton = new IconButton(i + 74, j + 41, AllIcons.I_FOLLOW_DIAGONAL);
		if (nbt.contains("SearchDiagonal") && nbt.getBoolean("SearchDiagonal"))
			spreadDiagonallyIndicator.state = State.ON;
		spreadDiagonallyButton.setToolTip(Lang.translate("gui.blockzapper.searchDiagonal"));

		spreadMaterialIndicator = new Indicator(i + 92, j + 36, StringTextComponent.EMPTY);
		spreadMaterialButton = new IconButton(i + 92, j + 41, AllIcons.I_FOLLOW_MATERIAL);
		if (nbt.contains("SearchFuzzy") && nbt.getBoolean("SearchFuzzy"))
			spreadMaterialIndicator.state = State.ON;
		spreadMaterialButton.setToolTip(Lang.translate("gui.blockzapper.searchFuzzy"));

		spreadRangeLabel = new Label(i + 119, j + 46, "").withShadow().withSuffix("m");
		spreadRangeInput = new ScrollInput(i + 115, j + 43, 22, 14).withRange(1, BlockzapperItem.getMaxAoe(zapper))
				.setState(1).titled(Lang.translate("gui.blockzapper.range")).writingTo(spreadRangeLabel);

		if (nbt.contains("SearchDistance"))
			spreadRangeInput.setState(nbt.getInt("SearchDistance"));
		if (BlockzapperItem.getMaxAoe(zapper) == 2)
			spreadRangeInput.getToolTip().add(1,needsUpgradedAmplifier.copy().formatted(TextFormatting.RED));

		Collections.addAll(widgets, replaceModeButton, replaceModeIndicator, spreadDiagonallyButton,
				spreadDiagonallyIndicator, spreadMaterialButton, spreadMaterialIndicator, spreadRangeLabel,
				spreadRangeInput);
	}

	@Override
	public boolean mouseClicked(double x, double y, int button) {
		CompoundNBT nbt = zapper.getTag();

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

		return super.mouseClicked(x, y, button);
	}

	
	@Override
	protected void writeAdditionalOptions(CompoundNBT nbt) {
		nbt.putInt("SearchDistance", spreadRangeInput.getState());
	}

}
