package com.simibubi.create.content.logistics.item.filter.attribute;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.simibubi.create.content.logistics.item.filter.ItemAttribute;

import net.minecraft.item.DyeColor;
import net.minecraft.item.FireworkRocketItem;
import net.minecraft.item.FireworkStarItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.text.TranslationTextComponent;

public class ColorAttribute implements ItemAttribute {
	public static final ColorAttribute EMPTY = new ColorAttribute(DyeColor.PURPLE);

	public final DyeColor color;

	public ColorAttribute(DyeColor color) {
		this.color = color;
	}

	@Override
	public boolean appliesTo(ItemStack itemStack) {
		return findMatchingDyeColors(itemStack).stream().anyMatch(color::equals);
	}

	@Override
	public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
		return findMatchingDyeColors(itemStack).stream().map(ColorAttribute::new).collect(Collectors.toList());
	}

	private Collection<DyeColor> findMatchingDyeColors(ItemStack stack) {
		CompoundNBT nbt = stack.getTag();

		DyeColor color = DyeColor.getColor(stack);
		if (color != null)
			return Collections.singletonList(color);

		Set<DyeColor> colors = new HashSet<>();
		if (stack.getItem() instanceof FireworkRocketItem && nbt != null) {
			ListNBT listnbt = nbt.getCompound("Fireworks").getList("Explosions", 10);
			for (int i = 0; i < listnbt.size(); i++) {
				colors.addAll(getFireworkStarColors(listnbt.getCompound(i)));
			}
		}

		if (stack.getItem() instanceof FireworkStarItem && nbt != null) {
			colors.addAll(getFireworkStarColors(nbt.getCompound("Explosion")));
		}

		Arrays.stream(DyeColor.values()).filter(c -> stack.getItem().getRegistryName().getPath().startsWith(c.getName() + "_")).forEach(colors::add);

		return colors;
	}

	private Collection<DyeColor> getFireworkStarColors(CompoundNBT compound) {
		Set<DyeColor> colors = new HashSet<>();
		Arrays.stream(compound.getIntArray("Colors")).mapToObj(DyeColor::byFireworkColor).forEach(colors::add);
		Arrays.stream(compound.getIntArray("FadeColors")).mapToObj(DyeColor::byFireworkColor).forEach(colors::add);
		return colors;
	}

	@Override
	public String getTranslationKey() {
		return "color";
	}

	@Override
	public Object[] getTranslationParameters() {
		return new Object[]{new TranslationTextComponent(color.getName()).getContents()};
	}

	@Override
	public void writeNBT(CompoundNBT nbt) {
		nbt.putInt("id", color.getId());
	}

	@Override
	public ItemAttribute readNBT(CompoundNBT nbt) {
		return nbt.contains("id") ?
			new ColorAttribute(DyeColor.byId(nbt.getInt("id")))
			: EMPTY;
	}
}
