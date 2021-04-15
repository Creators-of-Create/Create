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
import com.simibubi.create.foundation.networking.AllPackets;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeConfigSpec;

public class ValueEntry<T> extends ConfigScreenList.LabeledEntry {

	protected static final int resetWidth = 24;//including 2px offset on either side
	public static final Pattern unitPattern = Pattern.compile("\\[(in .*)]");

	protected ForgeConfigSpec.ConfigValue<T> value;
	protected ForgeConfigSpec.ValueSpec spec;
	protected ConfigButton reset;
	protected boolean editable = true;
	protected String unit = null;

	public ValueEntry(String label, ForgeConfigSpec.ConfigValue<T> value, ForgeConfigSpec.ValueSpec spec) {
		super(label);
		this.value = value;
		this.spec = spec;

		TextStencilElement text = new TextStencilElement(Minecraft.getInstance().fontRenderer, "R").centered(true, true);
		reset = ConfigButton.createFromStencilElement(0, 0, text)
				.withBounds(resetWidth - 4, 20)
				.withCallback(() -> {
					value.set((T) spec.getDefault());
					this.onReset();
				});

		listeners.add(reset);
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
		reset.active = editable && !value.get().equals(spec.getDefault());
		reset.animateGradientFromState();
	}

	@Override
	public void tick() {
		reset.tick();
	}

	@Override
	public void render(MatrixStack ms, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean p_230432_9_, float partialTicks) {
		super.render(ms, index, y, x, width, height, mouseX, mouseY, p_230432_9_, partialTicks);

		reset.x = x + width - resetWidth + 2;
		reset.y = y + 15;
		reset.render(ms, mouseX, mouseY, partialTicks);
	}

	@Override
	protected int getLabelWidth(int totalWidth) {
		return (int) (totalWidth * labelWidthMult);
	}

	protected void onReset() {
		onValueChange();
	}

	protected void onValueChange() {
		reset.active = editable && !value.get().equals(spec.getDefault());
		reset.animateGradientFromState();

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
