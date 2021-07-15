package com.simibubi.create.content.contraptions.fluids.potion;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.simibubi.create.content.contraptions.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.foundation.fluid.FluidHelper;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.Pair;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.Tuple;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

public class PotionFluidHandler {

	public static Pair<FluidStack, ItemStack> emptyPotion(ItemStack stack, boolean simulate) {
		FluidStack fluid = getFluidFromPotionItem(stack);
		if (!simulate)
			stack.shrink(1);
		return Pair.of(fluid, new ItemStack(Items.GLASS_BOTTLE));
	}

	public static FluidIngredient potionIngredient(Potion potion, int amount) {
		return FluidIngredient.fromFluidStack(FluidHelper.copyStackWithAmount(PotionFluidHandler
			.getFluidFromPotionItem(PotionUtils.setPotion(new ItemStack(Items.POTION), potion)), amount));
	}

	public static FluidStack getFluidFromPotionItem(ItemStack stack) {
		Potion potion = PotionUtils.getPotion(stack);
		List<EffectInstance> list = PotionUtils.getCustomEffects(stack);
		FluidStack fluid = PotionFluid.withEffects(250, potion, list);
		BottleType bottleTypeFromItem = bottleTypeFromItem(stack);
		if (potion == Potions.WATER && list.isEmpty() && bottleTypeFromItem == BottleType.REGULAR)
			return new FluidStack(Fluids.WATER, fluid.getAmount());
		NBTHelper.writeEnum(fluid.getOrCreateTag(), "Bottle", bottleTypeFromItem);
		return fluid;
	}

	public static BottleType bottleTypeFromItem(ItemStack stack) {
		Item item = stack.getItem();
		if (item == Items.LINGERING_POTION)
			return BottleType.LINGERING;
		if (item == Items.SPLASH_POTION)
			return BottleType.SPLASH;
		return BottleType.REGULAR;
	}

	public static IItemProvider itemFromBottleType(BottleType type) {
		switch (type) {
		case LINGERING:
			return Items.LINGERING_POTION;
		case SPLASH:
			return Items.SPLASH_POTION;
		case REGULAR:
		default:
			return Items.POTION;
		}
	}

	public static int getRequiredAmountForFilledBottle(ItemStack stack, FluidStack availableFluid) {
		return 250;
	}

	public static ItemStack fillBottle(ItemStack stack, FluidStack availableFluid) {
		CompoundNBT tag = availableFluid.getOrCreateTag();
		ItemStack potionStack = new ItemStack(itemFromBottleType(NBTHelper.readEnum(tag, "Bottle", BottleType.class)));
		PotionUtils.setPotion(potionStack, PotionUtils.getPotion(tag));
		PotionUtils.setCustomEffects(potionStack, PotionUtils.getCustomEffects(tag));
		return potionStack;
	}

	// Modified version of PotionUtils#addPotionTooltip
	@OnlyIn(Dist.CLIENT)
	public static void addPotionTooltip(FluidStack fs, List<ITextComponent> tooltip, float p_185182_2_) {
		List<EffectInstance> list = PotionUtils.getAllEffects(fs.getOrCreateTag());
		List<Tuple<String, AttributeModifier>> list1 = Lists.newArrayList();
		if (list.isEmpty()) {
			tooltip.add((new TranslationTextComponent("effect.none")).withStyle(TextFormatting.GRAY));
		} else {
			for (EffectInstance effectinstance : list) {
				TranslationTextComponent textcomponent = new TranslationTextComponent(effectinstance.getDescriptionId());
				Effect effect = effectinstance.getEffect();
				Map<Attribute, AttributeModifier> map = effect.getAttributeModifiers();
				if (!map.isEmpty()) {
					for (Entry<Attribute, AttributeModifier> entry : map.entrySet()) {
						AttributeModifier attributemodifier = entry.getValue();
						AttributeModifier attributemodifier1 = new AttributeModifier(attributemodifier.getName(),
							effect.getAttributeModifierValue(effectinstance.getAmplifier(), attributemodifier),
							attributemodifier.getOperation());
						list1.add(new Tuple<>(
							entry.getKey().getDescriptionId(),
							attributemodifier1));
					}
				}

				if (effectinstance.getAmplifier() > 0) {
					textcomponent.append(" ")
						.append(new TranslationTextComponent("potion.potency." + effectinstance.getAmplifier()).getString());
				}

				if (effectinstance.getDuration() > 20) {
					textcomponent.append(" (")
						.append(EffectUtils.formatDuration(effectinstance, p_185182_2_))
						.append(")");
				}

				tooltip.add(textcomponent.withStyle(effect.getCategory()
					.getTooltipFormatting()));
			}
		}

		if (!list1.isEmpty()) {
			tooltip.add(new StringTextComponent(""));
			tooltip.add((new TranslationTextComponent("potion.whenDrank")).withStyle(TextFormatting.DARK_PURPLE));

			for (Tuple<String, AttributeModifier> tuple : list1) {
				AttributeModifier attributemodifier2 = tuple.getB();
				double d0 = attributemodifier2.getAmount();
				double d1;
				if (attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_BASE
					&& attributemodifier2.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
					d1 = attributemodifier2.getAmount();
				} else {
					d1 = attributemodifier2.getAmount() * 100.0D;
				}

				if (d0 > 0.0D) {
					tooltip.add((new TranslationTextComponent(
						"attribute.modifier.plus." + attributemodifier2.getOperation()
							.toValue(),
						ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
						new TranslationTextComponent(tuple.getA())))
							.withStyle(TextFormatting.BLUE));
				} else if (d0 < 0.0D) {
					d1 = d1 * -1.0D;
					tooltip.add((new TranslationTextComponent(
						"attribute.modifier.take." + attributemodifier2.getOperation()
							.toValue(),
						ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(d1),
						new TranslationTextComponent(tuple.getA())))
							.withStyle(TextFormatting.RED));
				}
			}
		}

	}

}
