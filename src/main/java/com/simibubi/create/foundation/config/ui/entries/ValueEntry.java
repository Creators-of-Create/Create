package com.simibubi.create.foundation.config.ui.entries;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.foundation.config.ui.CConfigureConfigPacket;
import com.simibubi.create.foundation.config.ui.ConfigButton;
import com.simibubi.create.foundation.config.ui.ConfigScreenList;
import com.simibubi.create.foundation.gui.TextStencilElement;
import com.simibubi.create.foundation.gui.UIRenderHelper;
import com.simibubi.create.foundation.networking.AllPackets;
import com.simibubi.create.foundation.ponder.ui.PonderButton;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

public class ValueEntry<T> extends ConfigScreenList.LabeledEntry {

	protected static final int resetWidth = 28;//including 6px offset on either side
	public static final Pattern unitPattern = Pattern.compile("\\[(in .*)]");

	protected ForgeConfigSpec.ConfigValue<T> value;
	protected ForgeConfigSpec.ValueSpec spec;
	protected PonderButton resetButton;
	protected boolean editable = true;
	protected String unit = null;

	public ValueEntry(String label, ForgeConfigSpec.ConfigValue<T> value, ForgeConfigSpec.ValueSpec spec) {
		super(label);
		this.value = value;
		this.spec = spec;

		TextStencilElement text = new TextStencilElement(Minecraft.getInstance().fontRenderer, "R").centered(true, true);
		text.withElementRenderer((ms, width, height) -> UIRenderHelper.angledGradient(ms, 0 ,0, height/2, height, width, ConfigButton.Palette.button_idle_1, ConfigButton.Palette.button_idle_2));
		resetButton = new PonderButton(0, 0, (_$, _$$) -> {
			value.set((T) spec.getDefault());
			this.onReset();
		}, resetWidth - 12, 16).showingUnscaled(text);
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
		resetButton.active = editable && !value.get().equals(spec.getDefault());
	}

	@Override
	public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		resetButton.x = x + width - resetWidth + 6;
		resetButton.y = y + 15;
		resetButton.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult);
	}

	protected void onReset() {
		onValueChange();
	}

	protected void onValueChange() {
		resetButton.active = editable && !value.get().equals(spec.getDefault());

		if (!isForServer())
			return;

		String path = String.join(".", value.getPath());
		AllPackets.channel.sendToServer(new CConfigureConfigPacket<>(path, value.get()));
	}

	protected void bumpCog() {bumpCog(10f);}
	protected void bumpCog(float force) {
		if (list != null && list instanceof ConfigScreenList)
			((ConfigScreenList) list).bumpCog(force);
	}
}
