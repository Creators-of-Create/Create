package com.simibubi.create.foundation.ponder.instructions;

import java.util.function.Supplier;

import com.simibubi.create.foundation.ponder.PonderScene;
import com.simibubi.create.foundation.ponder.Select;
import com.simibubi.create.foundation.ponder.elements.OutlinerElement;
import com.simibubi.create.foundation.ponder.elements.TextWindowElement;

import net.minecraft.util.math.Vec3d;

public class TextInstruction extends FadeInOutInstruction {

	private TextWindowElement element;
	private OutlinerElement outline;

	protected TextInstruction(int color, Supplier<String> text, int duration) {
		super(duration);
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
	protected void show(PonderScene scene) {
		scene.addElement(element);
		element.setVisible(true);
		if (outline != null) {
			scene.addElement(outline);
			outline.setFade(1);
			outline.setVisible(true);
		}
	}

	@Override
	protected void hide(PonderScene scene) {
		element.setVisible(false);
		if (outline != null) {
			outline.setFade(0);
			outline.setVisible(false);
		}
	}

	@Override
	protected void applyFade(PonderScene scene, float fade) {
		element.setFade(fade);
	}

}
