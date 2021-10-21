package com.simibubi.create.content.schematics.item;

import com.simibubi.create.foundation.utility.NBTHelper;

import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundNBT;

public class SchematicAndQuillItem extends Item {

	public SchematicAndQuillItem(Properties properties) {
		super(properties);
	}

	public static void replaceStructureVoidWithAir(CompoundNBT nbt) {
		String air = Blocks.AIR.getRegistryName()
			.toString();
		String structureVoid = Blocks.STRUCTURE_VOID.getRegistryName()
			.toString();

		NBTHelper.iterateCompoundList(nbt.getList("palette", 10), c -> {
			if (c.contains("Name") && c.getString("Name")
				.equals(structureVoid)) {
				c.putString("Name", air);
			}
		});
	}

}
