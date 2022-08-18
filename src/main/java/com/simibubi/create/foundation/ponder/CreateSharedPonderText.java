package com.simibubi.create.foundation.ponder;

import java.util.function.BiConsumer;

import com.simibubi.create.Create;

import net.createmod.ponder.foundation.content.SharedText;

public class CreateSharedPonderText {

	public static void loadClass() {
		SharedText.registerText(Create.ID, CreateSharedPonderText::createSharedText);
	}

	private static void createSharedText(BiConsumer<String, String> adder) {
		adder.accept("rpm8", "8 RPM");
		adder.accept("rpm16", "16 RPM");
		adder.accept("rpm16_source", "Source: 16 RPM");
		adder.accept("rpm32", "32 RPM");

		adder.accept("movement_anchors", "With the help of Super Glue, larger structures can be moved.");
		adder.accept("behaviour_modify_wrench", "This behaviour can be modified using a Wrench");
		adder.accept("storage_on_contraption", "Inventories attached to the Contraption will pick up their drops automatically");
	}

}
