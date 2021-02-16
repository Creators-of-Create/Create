package com.simibubi.create.foundation.ponder.instructions;

import java.util.function.Supplier;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.Select;
import com.simibubi.create.foundation.ponder.elements.OutlinerElement;
import com.simibubi.create.foundation.ponder.elements.TextWindowElement;

import net.minecraft.util.math.Vec3d;

public class TextInstruction extends TickingInstruction {

	private TextWindowElement element;
	private OutlinerElement outline;
	private static final int fadeTime = 5;

	protected TextInstruction(int color, Supplier<String> text, int duration) {
		super(false, duration + 2 * fadeTime);
	}

	public TextInstruction(int color, Supplier<String> text, int duration, Select selection) {
		this(color, text, duration);
		element = new TextWindowElement(text).pointAt(selection.getCenter());
		element.colored(color);
		outline = new OutlinerElement(o -> selection.makeOutline(o)
			.lineWidth(1 / 16f)
			.colored(color));
	}

	public TextInstruction(int color, Supplier<String> text, int duration, Vec3d position) {
		this(color, text, duration);
		element = new TextWindowElement(text).pointAt(position);
		element.colored(color);
	}

	public TextInstruction(int color, Supplier<String> text, int duration, int y) {
		this(color, text, duration);
		element = new TextWindowElement(text).setY(y);
		element.colored(color);
	}

	@Override
	protected void firstTick(PonderScene scene) {
		super.firstTick(scene);
		scene.addElement(element);
		element.setVisible(true);
		element.setFade(0);
		if (outline != null) {
			scene.addElement(outline);
			outline.setFade(1);
			outline.setVisible(true);
		}
	}

	@Override
	public void tick(PonderScene scene) {
		super.tick(scene);
		int elapsed = totalTicks - remainingTicks;

		if (elapsed < fadeTime) {
			float fade = (elapsed / (float) fadeTime);
			element.setFade(fade * fade);

		} else if (remainingTicks < fadeTime) {
			float fade = (remainingTicks / (float) fadeTime);
			element.setFade(fade * fade);

		} else
			element.setFade(1);

		if (remainingTicks == 0) {
			element.setFade(0);
			element.setFade(0);
			element.setVisible(false);
			if (outline != null) {
				outline.setFade(0);
				outline.setVisible(false);
			}
		}

	}

}
