package com.simibubi.create.foundation.utility.fabric;

import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class FabricTags {
	private static final String CREATE = "create";
	private static final String TIC = "tconstruct";

	// blocks
	public static final Tag.Named<Block> WINDMILL_SAILS = c("windmill_sails", TagFactory.BLOCK);
	public static final Tag.Named<Block> FAN_HEATERS = c("fan_heaters", TagFactory.BLOCK);
	public static final Tag.Named<Block> WINDOWABLE = c("windowable", TagFactory.BLOCK);
	public static final Tag.Named<Block> NON_MOVABLE = c("non_movable", TagFactory.BLOCK);
	public static final Tag.Named<Block> BRITTLE = c("brittle", TagFactory.BLOCK);
	public static final Tag.Named<Block> BLOCKS$SEATS = c("seats", TagFactory.BLOCK);
	public static final Tag.Named<Block> SAILS = c("sails", TagFactory.BLOCK);
	public static final Tag.Named<Block> BLOCKS$VALVE_HANDLES = c("valve_handles", TagFactory.BLOCK);
	public static final Tag.Named<Block> FAN_TRANSPARENT = c("fan_transparent", TagFactory.BLOCK);
	public static final Tag.Named<Block> SAFE_NBT = c("safe_nbt", TagFactory.BLOCK);

	// items
	public static final Tag.Named<Item> CRUSHED_ORES = c("crushed_ores", TagFactory.ITEM);
	public static final Tag.Named<Item> ITEMS$SEATS = c("seats", TagFactory.ITEM);
	public static final Tag.Named<Item> ITEMS$VALVE_HANDLES = c("valve_handles", TagFactory.ITEM);
	public static final Tag.Named<Item> UPRIGHT_ON_BELT = c("upright_on_belt", TagFactory.ITEM);
	public static final Tag.Named<Item> CREATE_INGOTS = c("create_ingots", TagFactory.ITEM);

	// Don't use these tags as they are here since more than one of common tags exists for them
//	public static final Tag.Named<Item> STICK_OTHER = TagFactory.ITEM.create(new ResourceLocation("c:wooden_rods"));
//	public static final Tag.Named<Item> STONE_OTHER = TagFactory.ITEM.create(new ResourceLocation("c:stones"));

	// TIC compat
	public static final Tag.Named<Block> SLIMY_LOGS = t("slimy_logs", TagFactory.BLOCK);
	public static final Tag.Named<Item> SLIMEBALLS = t("slime_balls", TagFactory.ITEM);

	private static <T> Tag.Named<T> c(String id, TagFactory<T> factory) {
		return factory.create(new ResourceLocation(CREATE, id));
	}

	private static <T> Tag.Named<T> t(String id, TagFactory<T> factory) {
		return factory.create(new ResourceLocation(TIC, id));
	}
}
