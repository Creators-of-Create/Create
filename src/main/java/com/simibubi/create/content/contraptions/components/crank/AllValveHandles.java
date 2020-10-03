package com.simibubi.create.content.contraptions.components.crank;

import com.simibubi.create.AllTags;
import com.simibubi.create.foundation.config.StressConfigDefaults;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.item.DyeColor;

import java.util.ArrayList;
import java.util.List;

public class AllValveHandles {
	private static final List<String> types = new ArrayList<>();
	static {
		for (DyeColor color : DyeColor.values())
			types.add(color.getName());
		types.add("copper");
	}
	public static final BlockEntry<?>[] variants = new BlockEntry[types.size()];

	public static void register(CreateRegistrate registrate) {
		for (int i = 0; i < variants.length; i++) {
			String type = types.get(i);
			variants[i] = registrate.block(type + "_valve_handle", properties -> new ValveHandleBlock(properties, type.equals("copper")))
				.initialProperties(SharedProperties::softMetal)
				.blockstate((c, p) -> p.directionalBlock(c.get(), p.models()
					.withExistingParent(type + "_valve_handle", p.modLoc("block/valve_handle"))
					.texture("3", p.modLoc("block/valve_handle/valve_handle_" + type))))
				.transform(StressConfigDefaults.setCapacity(32.0))
				.tag(AllTags.AllBlockTags.BRITTLE.tag)
				.simpleItem()
				.register();
		}
	}
}