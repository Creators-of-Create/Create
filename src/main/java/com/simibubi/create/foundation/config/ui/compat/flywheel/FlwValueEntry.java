package com.simibubi.create.foundation.config.ui.compat.flywheel;

import com.google.common.base.Predicates;
import com.jozufozu.flywheel.config.Option;
import com.simibubi.create.foundation.config.ui.ConfigAnnotations;
import com.simibubi.create.foundation.config.ui.ConfigHelper;
import com.simibubi.create.foundation.config.ui.ConfigScreen;
import com.simibubi.create.foundation.config.ui.entries.ValueEntry;

import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.DelegatedStencilElement;
import com.simibubi.create.foundation.gui.widget.BoxWidget;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TextComponent;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FlwValueEntry<T> extends ValueEntry<T> {

	protected Option<T> option;

	public FlwValueEntry(String label, Option option) {
		super(label);
		this.option = option;
		this.path = String.join(".", option.getKey());

		resetButton = new BoxWidget(0, 0, resetWidth - 12, 16)
				.showingElement(AllIcons.I_CONFIG_RESET.asStencil())
				.withCallback(() -> {
					setValue((T) option.get());
					this.onReset();
				});
		resetButton.modifyElement(e -> ((DelegatedStencilElement) e).withElementRenderer(BoxWidget.gradientFactory.apply(resetButton)));

		listeners.add(resetButton);

		String path = option.getKey();
		labelTooltip.add(new TextComponent(label).withStyle(ChatFormatting.WHITE));
		String comment = null;//option.getComment();
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
				.map(TextComponent::new)
				.flatMap(stc -> TooltipHelper.cutTextComponent(stc, ChatFormatting.GRAY, ChatFormatting.GRAY)
						.stream())
				.collect(Collectors.toList()));

		if (annotations.containsKey(ConfigAnnotations.RequiresRelog.TRUE.getName()))
			labelTooltip.addAll(TooltipHelper.cutTextComponent(new TextComponent("Changing this value will require a _relog_ to take full effect"), ChatFormatting.GRAY, ChatFormatting.GOLD));

		if (annotations.containsKey(ConfigAnnotations.RequiresRestart.CLIENT.getName()))
			labelTooltip.addAll(TooltipHelper.cutTextComponent(new TextComponent("Changing this value will require a _restart_ to take full effect"), ChatFormatting.GRAY, ChatFormatting.RED));

		labelTooltip.add(new TextComponent(ConfigScreen.modID + ":" + path).withStyle(ChatFormatting.DARK_GRAY));
	}

	@Override
	public void setValue(@NotNull T value) {
		option.set(value);
		onValueChange(value);
	}

	@NotNull
	@Override
	public T getValue() {
		return option.get();
	}

	@Override
	protected boolean isCurrentValueDefault() {
		return false;
	}
}
