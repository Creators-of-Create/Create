package com.simibubi.create.foundation.config.ui.entries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.ui.ConfigAnnotations;
import com.simibubi.create.foundation.config.ui.ConfigHelper;
import com.simibubi.create.foundation.config.ui.ConfigScreen;
import com.simibubi.create.foundation.config.ui.ConfigScreenList;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.widgets.BoxWidget;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

public class ValueEntry<T> extends ConfigScreenList.LabeledEntry {

	protected static final int resetWidth = 28;//including 6px offset on either side

	protected ForgeConfigSpec.ConfigValue<T> value;
	protected ForgeConfigSpec.ValueSpec spec;
	protected BoxWidget resetButton;
	protected boolean editable = true;

	public ValueEntry(String label, ForgeConfigSpec.ConfigValue<T> value, ForgeConfigSpec.ValueSpec spec) {
		super(label);
		this.value = value;
		this.spec = spec;
		this.path = String.join(".", value.getPath());

		resetButton = new BoxWidget(0, 0, resetWidth - 12, 16)
				.showingElement(AllIcons.I_CONFIG_RESET.asStencil())
				.withCallback(() -> {
					setValue((T) spec.getDefault());
					this.onReset();
				});
		resetButton.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.gradientFactory.apply(resetButton)));

		listeners.add(resetButton);

		List<String> path = value.getPath();
		labelTooltip.add(new StringTextComponent(label).withStyle(TextFormatting.WHITE));
		String comment = spec.getComment();
		if (comment == null || comment.isEmpty())
			return;

		List<String> commentLines = new ArrayList<>(Arrays.asList(comment.split("\n")));


		Pair<String, Map<String, String>> metadata = ConfigHelper.readMetadataFromComment(commentLines);
		if (metadata.getFirst() != null) {
			unit = metadata.getFirst();
		}
		if (metadata.getSecond() != null && !metadata.getSecond().isEmpty()) {
			annotations.putAll(metadata.getSecond());
		}
		// add comment to tooltip
		labelTooltip.addAll(commentLines.stream()
				.filter(Predicates.not(s -> s.startsWith("Range")))
				.map(StringTextComponent::new)
				.flatMap(stc -> TooltipHelper.cutTextComponent(stc, TextFormatting.GRAY, TextFormatting.GRAY)
						.stream())
				.collect(Collectors.toList()));

		if (annotations.containsKey(ConfigAnnotations.RequiresRelog.TRUE.getName()))
			labelTooltip.addAll(TooltipHelper.cutTextComponent(new StringTextComponent("Changing this value will require a _relog_ to take full effect"), TextFormatting.GRAY, TextFormatting.GOLD));

		if (annotations.containsKey(ConfigAnnotations.RequiresRestart.CLIENT.getName()))
			labelTooltip.addAll(TooltipHelper.cutTextComponent(new StringTextComponent("Changing this value will require a _restart_ to take full effect"), TextFormatting.GRAY, TextFormatting.RED));

		labelTooltip.add(new StringTextComponent(ConfigScreen.modID + ":" + path.get(path.size() - 1)).withStyle(TextFormatting.DARK_GRAY));
	}

	@Override
	protected void setEditable(boolean b) {
		editable = b;
		resetButton.active = editable && !isCurrentValueDefault();
		resetButton.animateGradientFromState();
	}

	@Override
	public void tick() {
		super.tick();
		resetButton.tick();
	}

	@Override
	public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		resetButton.x = x + width - resetWidth + 6;
		resetButton.y = y + 10;
		resetButton.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult) + 30;
	}

	public void setValue(@Nonnull T value) {
		ConfigHelper.setValue(path, this.value, value, annotations);
		onValueChange(value);
	}

	@Nonnull
	public T getValue() {
		return ConfigHelper.getValue(path, this.value);
	}

	protected boolean isCurrentValueDefault() {
		return spec.getDefault().equals(getValue());
	}

	public void onReset() {
		onValueChange(getValue());
	}

	public void onValueChange() {
		onValueChange(getValue());
	}
	public void onValueChange(T newValue) {
		resetButton.active = editable && !isCurrentValueDefault();
		resetButton.animateGradientFromState();
	}

	protected void bumpCog() {bumpCog(10f);}
	protected void bumpCog(float force) {
		if (list != null && list instanceof ConfigScreenList)
			((ConfigScreenList) list).bumpCog(force);
	}
}
