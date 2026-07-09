package com.simibubi.create.content.equipment.armor;

import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.generators.RegistrateItemModelGenerator;

import net.minecraft.world.item.Item;

public class TrimmableArmorModelGenerator {
	public static <T extends BaseArmorItem> void generate(DataGenContext<Item, T> c, RegistrateItemModelGenerator p) {
		p.generated(c);
	}
}
