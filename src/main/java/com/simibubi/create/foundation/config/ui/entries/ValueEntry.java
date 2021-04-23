package com.simibubi.create.foundation.config.ui.entries;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.ArrayUtils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.ui.ConfigButton;
import com.simibubi.create.foundation.config.ui.ConfigScreen;
import com.simibubi.create.foundation.config.ui.ConfigScreenList;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.ponder.ui.PonderButton;

import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

public class ValueEntry<T> extends ConfigScreenList.LabeledEntry {

	protected static final IFormattableTextComponent modComponent = new StringTextComponent("* ").formatted(TextFormatting.BOLD, TextFormatting.DARK_BLUE).append(StringTextComponent.EMPTY.copy().formatted(TextFormatting.RESET));
	protected static final int resetWidth = 28;//including 6px offset on either side
	public static final Pattern unitPattern = Pattern.compile("\\[(in .*)]");
	//public static DelegatedStencilElement.ElementRenderer idle = (ms, w, h) -> UIRenderHelper.angledGradient(ms, 0, 0, h / 2, h, w, ConfigButton.Palette.button_idle_1, ConfigButton.Palette.button_idle_2);

	protected ForgeConfigSpec.ConfigValue<T> value;
	protected ForgeConfigSpec.ValueSpec spec;
	protected PonderButton resetButton;
	protected boolean editable = true;
	protected String unit = null;
	protected String path;

	public ValueEntry(String label, ForgeConfigSpec.ConfigValue<T> value, ForgeConfigSpec.ValueSpec spec) {
		super(label);
		this.value = value;
		this.spec = spec;
		this.path = String.join(".", value.getPath());

		resetButton = new PonderButton(0, 0, (_$, _$$) -> {
			setValue((T) spec.getDefault());
			this.onReset();
		}, resetWidth - 12, 16)
				.showing(AllIcons.I_CONFIG_RESET.asStencil()/*.withElementRenderer(idle)*/);
		resetButton.fade(1);

		listeners.add(resetButton);

		List<String> path = value.getPath();
		labelTooltip.add(new StringTextComponent(path.get(path.size()-1)).formatted(TextFormatting.GRAY));
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
			commentLines = ArrayUtils.remove(commentLines, i);
			break;
		}
		//add comment to tooltip
		labelTooltip.addAll(Arrays.stream(commentLines).map(StringTextComponent::new).collect(Collectors.toList()));
	}

	@Override
	protected void setEditable(boolean b) {
		editable = b;
		resetButton.active = editable && !isCurrentValueDefault();
	}

	@Override
	public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		if (isCurrentValueChanged()) {
			IFormattableTextComponent original = label.getComponent();
			IFormattableTextComponent changed = modComponent.copy().append(original);
			label.withText(changed);
			super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);
			label.withText(original);
		} else {
			super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);
		}

		resetButton.x = x + width - resetWidth + 6;
		resetButton.y = y + 15;
		resetButton.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult);
	}

	public void setValue(@Nonnull T value) {
		if (value.equals(this.value.get())) {
			ConfigScreen.changes.remove(path);
			onValueChange(value);
			return;
		}

		ConfigScreen.changes.put(path, value);
		onValueChange(value);
	}

	@Nonnull
	public T getValue() {
		//noinspection unchecked
		return (T) ConfigScreen.changes.getOrDefault(path, this.value.get());
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
	}

	protected void bumpCog() {bumpCog(10f);}
	protected void bumpCog(float force) {
		if (list != null && list instanceof ConfigScreenList)
			((ConfigScreenList) list).bumpCog(force);
	}
}
