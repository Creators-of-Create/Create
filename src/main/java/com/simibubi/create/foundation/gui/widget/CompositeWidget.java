package com.simibubi.create.foundation.gui.widget;

import net.createmod.catnip.gui.TickableGuiEventListener;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A wrapper around multiple widgets, allowing them to be managed together.
 * <p>
 * Mostly based on (Abstract|Object)SelectionList.
 */
public class CompositeWidget extends AbstractContainerEventHandler implements NarratableEntry, Renderable, TickableGuiEventListener {
	private final List<GuiEventListener> children = new ArrayList<>();
	private final List<Renderable> renderables = new ArrayList<>();

	private GuiEventListener hovered;

	public <T extends GuiEventListener> T add(T child) {
		this.children.add(child);

		if (child instanceof Renderable renderable) {
			this.renderables.add(renderable);
		}

		return child;
	}

	public <T extends Renderable> T addRenderableOnly(T renderable) {
		this.renderables.add(renderable);
		return renderable;
	}

	public <T extends GuiEventListener> boolean remove(T child) {
		boolean removed = this.children.remove(child);

		if (child instanceof Renderable) {
			removed |= this.renderables.remove(child);
		}

		return removed;
	}

	public <T extends Renderable> boolean removeRenderableOnly(T renderable) {
		return this.renderables.remove(renderable);
	}

	public void clear() {
		this.children.clear();
		this.renderables.clear();
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return Collections.unmodifiableList(this.children);
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
		this.getChildAt(mouseX, mouseY).ifPresent(hovered -> this.hovered = hovered);

		for (Renderable renderable : this.renderables) {
			renderable.render(graphics, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public NarrationPriority narrationPriority() {
		if (this.getFocused() instanceof NarratableEntry) {
			return NarrationPriority.FOCUSED;
		} else if (this.hovered instanceof NarratableEntry) {
			return NarrationPriority.HOVERED;
		} else {
			return NarrationPriority.NONE;
		}
	}

	@Override
	public void updateNarration(NarrationElementOutput output) {
		if (this.hovered instanceof NarratableEntry narratable) {
			narratable.updateNarration(output);
		} else if (this.getFocused() instanceof NarratableEntry narratable) {
			narratable.updateNarration(output);
		}
	}

	@Override
	public void tick() {
		for (GuiEventListener child : this.children) {
			if (child instanceof TickableGuiEventListener tickable) {
				tickable.tick();
			}
		}
	}

	// these aren't implemented by ContainerEventHandler for some reason

	@Override
	public void mouseMoved(double mouseX, double mouseY) {
		for (GuiEventListener child : this.children) {
			child.mouseMoved(mouseX, mouseY);
		}
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return this.getChildAt(mouseX, mouseY).isPresent();
	}
}
