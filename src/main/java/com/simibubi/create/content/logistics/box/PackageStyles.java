package com.simibubi.create.content.logistics.box;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Unmodifiable;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.Create;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class PackageStyles {
	public record PackageStyle(String type, int width, int height, float riggingOffset, boolean rare) {
		public Identifier getItemId() {
			String size = "_" + width + "x" + height;
			String id = type + "_package" + (rare ? "" : size);
			return Create.asResource(id);
		}

		public Identifier getRiggingModel() {
			String size = width + "x" + height;
			return Create.asResource("item/package/rigging_" + size);
		}
	};

	/**
	 * This is used for registration, if you insert into this list, you will be registering items under Create's namespace.
	 * Instead, you should handle registration yourself
	 * and use the PackageItem class so your packages end up in the correct lists.
	 */
	@Internal
	@Unmodifiable
	public static final List<PackageStyle> STYLES = ImmutableList.of(
		new PackageStyle("cardboard", 12, 12, 23f, false),
		new PackageStyle("cardboard", 10, 12, 22f, false),
		new PackageStyle("cardboard", 10, 8, 18f, false),
		new PackageStyle("cardboard", 12, 10, 21f, false),

		rare("creeper"),
		rare("darcy"),
		rare("evan"),
		rare("jinx"),
		rare("kryppers"),
		rare("simi"),
		rare("starlotte"),
		rare("thunder"),
		rare("up"),
		rare("vector")
	);

	public static final List<PackageItem> ALL_BOXES = new ArrayList<>();
	public static final List<PackageItem> STANDARD_BOXES = new ArrayList<>();
	public static final List<PackageItem> RARE_BOXES = new ArrayList<>();

	private static final Random STYLE_PICKER = new Random();
	private static final int RARE_CHANCE = 7500; // addons, have mercy

	public static ItemStack getRandomBox() {
		List<PackageItem> pool = STYLE_PICKER.nextInt(RARE_CHANCE) == 0 ? RARE_BOXES : STANDARD_BOXES;
		return new ItemStack(pool.get(STYLE_PICKER.nextInt(pool.size())));
	}

	public static ItemStack getDefaultBox() {
		return new ItemStack(ALL_BOXES.get(0));
	}

	private static PackageStyle rare(String name) {
		return new PackageStyle("rare_" + name, 12, 10, 21f, true);
	}
}
