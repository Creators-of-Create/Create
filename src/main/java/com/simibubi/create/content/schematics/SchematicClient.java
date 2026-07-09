package com.simibubi.create.content.schematics;

import com.simibubi.create.content.schematics.client.SchematicEditScreen;

import net.createmod.catnip.api.client.gui.ScreenOpener;

public class SchematicClient {

	public static void displayBlueprintScreen() {
		ScreenOpener.open(new SchematicEditScreen());
	}

}
