package com.simibubi.create.content.logistics.item.filter.attribute.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

import com.simibubi.create.content.logistics.item.filter.attribute.AllItemAttributeTypes;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.simibubi.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.simibubi.create.foundation.utility.RegisteredObjects;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FireworkRocketItem;
import net.minecraft.world.item.FireworkStarItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ColorAttribute implements ItemAttribute {
	private DyeColor color;

	public ColorAttribute(DyeColor color) {
		this.color = color;
	}

	private static Collection<DyeColor> findMatchingDyeColors(ItemStack stack) {
		CompoundTag nbt = stack.getTag();

		DyeColor color = DyeColor.getColor(stack);
		if (color != null)
			return Collections.singletonList(color);

		Set<DyeColor> colors = new HashSet<>();
		if (stack.getItem() instanceof FireworkRocketItem && nbt != null) {
			ListTag listnbt = nbt.getCompound("Fireworks").getList("Explosions", 10);
			for (int i = 0; i < listnbt.size(); i++) {
				colors.addAll(getFireworkStarColors(listnbt.getCompound(i)));
			}
		}

		if (stack.getItem() instanceof FireworkStarItem && nbt != null) {
			colors.addAll(getFireworkStarColors(nbt.getCompound("Explosion")));
		}

		Arrays.stream(DyeColor.values()).filter(c -> RegisteredObjects.getKeyOrThrow(stack.getItem()).getPath().startsWith(c.getName() + "_")).forEach(colors::add);

		return colors;
	}

	private static Collection<DyeColor> getFireworkStarColors(CompoundTag compound) {
		Set<DyeColor> colors = new HashSet<>();
		Arrays.stream(compound.getIntArray("Colors")).mapToObj(DyeColor::byFireworkColor).forEach(colors::add);
		Arrays.stream(compound.getIntArray("FadeColors")).mapToObj(DyeColor::byFireworkColor).forEach(colors::add);
		return colors;
	}

	@Override
	public boolean appliesTo(ItemStack itemStack, Level level) {
		return findMatchingDyeColors(itemStack).stream().anyMatch(color::equals);
	}

	@Override
	public String getTranslationKey() {
		return "color";
	}

	@Override
	public Object[] getTranslationParameters() {
		return new Object[]{I18n.get("color.minecraft." + color.getName())};
	}

	@Override
	public ItemAttributeType getType() {
		return AllItemAttributeTypes.HAS_COLOR.get();
	}

	@Override
	public void save(CompoundTag nbt) {
		nbt.putInt("id", color.getId());
	}

	@Override
	public void load(CompoundTag nbt) {
		if (nbt.contains("id")) {
			color = DyeColor.byId(nbt.getInt("id"));
		}
	}

	public static class Type implements ItemAttributeType {
		@Override
		public @NotNull ItemAttribute createAttribute() {
			return new ColorAttribute(DyeColor.PURPLE);
		}

		@Override
		public List<ItemAttribute> getAllAttributes(ItemStack stack, Level level) {
			List<ItemAttribute> list = new ArrayList<>();

			for (DyeColor color : ColorAttribute.findMatchingDyeColors(stack)) {
				list.add(new ColorAttribute(color));
			}

			return list;
		}
	}
}
