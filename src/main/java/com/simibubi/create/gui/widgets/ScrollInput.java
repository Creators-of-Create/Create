package com.simibubi.create.gui.widgets;

import java.util.function.Consumer;

import com.simibubi.create.utility.Keyboard;

import net.minecraft.util.text.TextFormatting;

public class ScrollInput extends AbstractSimiWidget {

	protected Consumer<Integer> onScroll;
	protected int state;
	protected String title = "Choose an option";
	protected Label displayLabel;

	protected int min, max;

	public ScrollInput(int xIn, int yIn, int widthIn, int heightIn) {
		super(xIn, yIn, widthIn, heightIn);
		state = 0;
		min = 0;
		max = 1;
	}
	
	public ScrollInput withRange(int min, int max) {
		this.min = min;
		this.max = max;
		return this;
	}

	public ScrollInput calling(Consumer<Integer> onScroll) {
		this.onScroll = onScroll;
		return this;
	}

	public ScrollInput titled(String title) {
		this.title = title;
		updateTooltip();
		return this;
	}

	public ScrollInput writingTo(Label label) {
		this.displayLabel = label;
		writeToLabel();
		return this;
	}

	public int getState() {
		return state;
	}

	public ScrollInput setState(int state) {
		this.state = state;
		clampState();
		updateTooltip();
		if (displayLabel != null)
			writeToLabel();
		return this;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if (!isHovered)
			return false;

		int priorState = state;
		int step = (int) Math.signum(delta) * (Keyboard.isKeyDown(Keyboard.LSHIFT) ? 5 : 1);

		state += step;
		clampState();
		
		if (priorState != state)
			onChanged();
		
		return priorState != state;
	}

	protected void clampState() {
		if (state >= max)
			state = max - 1;
		if (state < min)
			state = min;
	}
	
	protected void onChanged() {
		if (displayLabel != null)
			writeToLabel();
		if (onScroll != null)
			onScroll.accept(state);
		updateTooltip();
	}
	
	protected void writeToLabel() {
		displayLabel.text = "" + state;
	}

	protected void updateTooltip() {
		toolTip.clear();
		toolTip.add(TextFormatting.BLUE + title);
	}
	
//	public interface IScrollAction {
//		public void onScroll(int position);
//	}
//
//	public interface ICancelableScrollAction extends IScrollAction {
//		public void onScroll(int position);
//
//		public boolean canScroll(int position);
//	}
//
//	private int x, y, width, height;
//	private IScrollAction action;
//	public boolean enabled;
//	private Optional<List<String>> tooltipContent;
//	private int min, max;
//	private boolean limitless;
//	private boolean numeric;
//
//	public ScrollArea(List<String> options, IScrollAction action) {
//		this(0, options.size(), action);
//		this.tooltipContent = Optional.of(options);
//		updateTooltip();
//	}
//
//	public ScrollArea(int min, int max, IScrollAction action) {
//		this(action);
//		this.limitless = false;
//		this.min = min;
//		this.max = max;
//	}
//
//	public ScrollArea(IScrollAction action) {
//		this.enabled = true;
//		this.action = action;
//		this.tooltipContent = Optional.absent();
//		this.limitless = true;
//		this.numeric = false;
//	}
//
//	public void setBounds(int x, int y, int width, int height) {
//		this.x = x;
//		this.y = y;
//		this.width = width;
//		this.height = height;
//	}
//	
//	public void setState(int state) {
//		currentState = state;
//		updateTooltip();
//	}
//
//	public int getState() {
//		return currentState;
//	}
//
//	public boolean isHovered(double x, double y) {
//		return (x > this.x && x < this.x + this.width && y > this.y && y < this.y + this.height);
//	}
//
//	public void tryScroll(double mouseX, double mouseY, int amount) {
//		if (enabled && isHovered(mouseX, mouseY)) {
//			scroll(numeric? -amount : amount);
//		}
//	}
//	
//	public void setNumeric(boolean numeric) {
//		this.numeric = numeric;
//	}
//
//	private void scroll(int amount) {
//		if (enabled) {
//
//			if (limitless) {
//				if (!(action instanceof ICancelableScrollAction)
//						|| ((ICancelableScrollAction) action).canScroll(amount))
//					action.onScroll(amount);
//				return;
//			}
//
//			if (!(action instanceof ICancelableScrollAction)
//					|| ((ICancelableScrollAction) action).canScroll(currentState + amount)) {
//				currentState += amount;
//				if (currentState < min)
//					currentState = min;
//				if (currentState >= max)
//					currentState = max - 1;
//				updateTooltip();
//				action.onScroll(currentState);
//			}
//		}
//	}
//
//	public void draw(Screen screen, int mouseX, int mouseY) {
//		GlStateManager.pushLightingAttributes();
//		if (enabled && isHovered(mouseX, mouseY)) {
//			GlStateManager.pushMatrix();
//			GlStateManager.translated(mouseX, mouseY,0);
//			if (tooltipContent.isPresent())
//				screen.renderTooltip(getToolTip(), 0, 0);
//			else
//				screen.renderTooltip(TextFormatting.BLUE + title, 0, 0);
//			GlStateManager.popMatrix();
//		}
//
//		GlStateManager.popAttributes();
//	}
//
//	public List<String> getToolTip() {
//		return tooltip;
//	}
//
//	public void setTitle(String title) {
//		this.title = title;
//		updateTooltip();
//	}
//
//	private void updateTooltip() {
//		tooltip = new LinkedList<>();
//		tooltip.add(TextFormatting.BLUE + title);
//
//		if (tooltipContent.isPresent()) {
//			for (int i = min; i < max; i++) {
//				StringBuilder result = new StringBuilder();
//				if (i == currentState)
//					result.append(TextFormatting.WHITE).append("-> ").append(tooltipContent.get().get(i));
//				else
//					result.append(TextFormatting.GRAY).append("> ").append(tooltipContent.get().get(i));
//				tooltip.add(result.toString());
//			}
//
//		}
//	}
//
//	public boolean isNumeric() {
//		return numeric;
//	}

}
