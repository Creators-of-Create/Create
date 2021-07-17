package com.simibubi.create.foundation.config.ui.entries;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Predicates;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.ui.ConfigHelper;
import com.simibubi.create.foundation.config.ui.ConfigScreen;
import com.simibubi.create.foundation.config.ui.ConfigScreenList;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.widgets.BoxWidget;
import com.simibubi.create.foundation.item.TooltipHelper;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

public class ValueEntry<T> extends ConfigScreenList.LabeledEntry {

	protected static final IFormattableTextComponent modComponent = new StringTextComponent("* ").withStyle(TextFormatting.BOLD, TextFormatting.DARK_BLUE).append(StringTextComponent.EMPTY.plainCopy().withStyle(TextFormatting.RESET));
	protected static final int resetWidth = 28;//including 6px offset on either side
	public static final Pattern unitPattern = Pattern.compile("\\[(in .*)]");

	protected ForgeConfigSpec.ConfigValue<T> value;
	protected ForgeConfigSpec.ValueSpec spec;
	protected BoxWidget resetButton;
	protected boolean editable = true;
	protected String path;

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
		String[] commentLines = comment.split("\n");
		//find unit in the comment
		for (int i = 0; i < commentLines.length; i++) {
			if (commentLines[i].isEmpty()) {
				commentLines = ArrayUtils.remove(commentLines, i);
				i--;
				continue;
			}

			Matcher matcher = unitPattern.matcher(commentLines[i]);
			if (!matcher.matches())
				continue;

			String u = matcher.group(1);
			if (u.equals("in Revolutions per Minute"))
				u = "in RPM";
			if (u.equals("in Stress Units"))
				u = "in SU";
			unit = u;
		}
		// add comment to tooltip
		labelTooltip.addAll(Arrays.stream(commentLines)
				.filter(Predicates.not(s -> s.startsWith("Range")))
				.map(StringTextComponent::new)
				.flatMap(stc -> TooltipHelper.cutTextComponent(stc, TextFormatting.GRAY, TextFormatting.GRAY)
						.stream())
				.collect(Collectors.toList()));
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
	public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean pIsMouseOver, float partialTicks) {
		if (isCurrentValueChanged()) {
			IFormattableTextComponent original = label.getComponent();
			IFormattableTextComponent changed = modComponent.plainCopy().append(original);
			label.withText(changed);
			super.render(ms, index, y, x, width, height, mouseX, mouseY, pIsMouseOver, partialTicks);
			label.withText(original);
		} else {
			super.render(ms, index, y, x, width, height, mouseX, mouseY, pIsMouseOver, partialTicks);
		}

		resetButton.x = x + width - resetWidth + 6;
		resetButton.y = y + 10;
		resetButton.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult) + 30;
	}

	public void setValue(@Nonnull T value) {
		ConfigHelper.setValue(path, this.value, value);
		onValueChange(value);
	}

	@Nonnull
	public T getValue() {
		return ConfigHelper.getValue(path, this.value);
	}

	protected boolean isCurrentValueChanged() {
		return ConfigScreen.changes.containsKey(path);
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
