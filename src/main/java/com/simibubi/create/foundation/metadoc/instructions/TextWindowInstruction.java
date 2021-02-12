package com.simibubi.create.foundation.metadoc.instructions;

import java.util.function.Supplier;

import com.simibubi.create.foundation.metadoc.MetaDocScene;
import com.simibubi.create.foundation.metadoc.elements.TextWindowElement;

import net.minecraft.util.math.Vec3d;

public class TextWindowInstruction extends TickingInstruction {

	private TextWindowElement element;
	private int fadeTime;

	public TextWindowInstruction(Supplier<String> text, int fadeTime, int duration, Vec3d position) {
		super(false, duration + 2 * fadeTime);
		this.fadeTime = fadeTime;
		element = new TextWindowElement(text).moveTo(position);
	}

	@Override
	protected void firstTick(MetaDocScene scene) {
		super.firstTick(scene);
		scene.addElement(element);
		element.setVisible(true);
		element.setFade(0);
	}

	@Override
	public void tick(MetaDocScene scene) {
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
		}
	}

}
