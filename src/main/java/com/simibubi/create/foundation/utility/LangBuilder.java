package com.simibubi.create.foundation.utility;

import java.util.List;

import com.simibubi.create.compat.Mods;

import joptsimple.internal.Strings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class LangBuilder {

	String namespace;
	MutableComponent component;

	public LangBuilder(String namespace) {
		this.namespace = namespace;
	}

	public LangBuilder space() {
		return text(" ");
	}

	public LangBuilder newLine() {
		return text("\n");
	}

	/**
	 * Appends a localised component<br>
	 * To add an independently formatted localised component, use add() and a nested
	 * builder
	 *
	 * @param langKey
	 * @param args
	 * @return
	 */
	public LangBuilder translate(String langKey, Object... args) {
		return add(Components.translatable(namespace + "." + langKey, Lang.resolveBuilders(args)));
	}

	/**
	 * Appends a text component
	 *
	 * @param literalText
	 * @return
	 */
	public LangBuilder text(String literalText) {
		return add(Components.literal(literalText));
	}

	/**
	 * Appends a colored text component
	 *
	 * @param format
	 * @param literalText
	 * @return
	 */
	public LangBuilder text(ChatFormatting format, String literalText) {
		return add(Components.literal(literalText).withStyle(format));
	}

	/**
	 * Appends a colored text component
	 *
	 * @param color
	 * @param literalText
	 * @return
	 */
	public LangBuilder text(int color, String literalText) {
		return add(Components.literal(literalText).withStyle(s -> s.withColor(color)));
	}

	/**
	 * Appends the contents of another builder
	 *
	 * @param otherBuilder
	 * @return
	 */
	public LangBuilder add(LangBuilder otherBuilder) {
		return add(otherBuilder.component());
	}

	/**
	 * Appends a component
	 *
	 * @param customComponent
	 * @return
	 */
	public LangBuilder add(MutableComponent customComponent) {
		component = component == null ? customComponent : component.append(customComponent);
		return this;
	}

	//

	/**
	 * Applies the format to all added components
	 *
	 * @param format
	 * @return
	 */
	public LangBuilder style(ChatFormatting format) {
		assertComponent();
		component = component.withStyle(format);
		return this;
	}

	/**
	 * Applies the color to all added components
	 *
	 * @param color
	 * @return
	 */
	public LangBuilder color(int color) {
		assertComponent();
		component = component.withStyle(s -> s.withColor(color));
		return this;
	}

	//

	public MutableComponent component() {
		assertComponent();
		return component;
	}

	public String string() {
		return component().getString();
	}

	public String json() {
		return Component.Serializer.toJson(component());
	}

	public void sendStatus(Player player) {
		player.displayClientMessage(component(), true);
	}

	public void sendChat(Player player) {
		player.displayClientMessage(component(), false);
	}

	public void addTo(List<? super MutableComponent> tooltip) {
		tooltip.add(component());
	}

	public void forGoggles(List<? super MutableComponent> tooltip) {
		forGoggles(tooltip, 0);
	}

	public void forGoggles(List<? super MutableComponent> tooltip, int indents) {
		indents += getIndents();
		tooltip.add(Lang.builder()
			.text(Strings.repeat(' ', 4 + indents))
			.add(this)
			.component());
	}

	static int getIndents() {
		return Mods.MODERNUI.isLoaded() ? 5 : 0;
	}

	//

	private void assertComponent() {
		if (component == null)
			throw new IllegalStateException("No components were added to builder");
	}

}
