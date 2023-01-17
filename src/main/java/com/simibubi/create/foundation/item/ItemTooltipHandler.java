package com.simibubi.create.foundation.item;

import static net.minecraft.ChatFormatting.DARK_GRAY;
import static net.minecraft.ChatFormatting.GRAY;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.mojang.bridge.game.Language;
import com.simibubi.create.Create;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.content.contraptions.components.steam.SteamEngineBlock;
import com.simibubi.create.content.contraptions.goggles.GogglesItem;
import com.simibubi.create.foundation.block.BlockStressValues;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CKinetics;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Couple;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.LangBuilder;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;

public class ItemTooltipHandler {
	private static final String ITEM_PREFIX = "item." + Create.ID;
	private static final String BLOCK_PREFIX = "block." + Create.ID;

	private static final Map<Item, Function<ItemStack, String>> TOOLTIP_REFERRALS = new IdentityHashMap<>();
	private static final Map<String, ItemDescription> TOOLTIP_CACHE = new HashMap<>();
	private static Language cachedLanguage;

	public static void referTo(ItemLike item, Function<ItemStack, String> func) {
		TOOLTIP_REFERRALS.put(item.asItem(), func);
	}

	public static void referTo(ItemLike item, Supplier<? extends ItemLike> itemWithTooltip) {
		referTo(item, stack -> itemWithTooltip.get()
			.asItem()
			.getDescriptionId());
	}

	public static void referTo(ItemLike item, String string) {
		referTo(item, stack -> string);
	}

	public static void addToTooltip(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		List<Component> tooltip = event.getToolTip();

		String translationKey = stack.getDescriptionId();
		if (translationKey.startsWith(ITEM_PREFIX) || translationKey.startsWith(BLOCK_PREFIX)) {
			ItemDescription desc = getOrCreateTooltip(stack);
			if (desc != null) {
				List<Component> descTooltip = new ArrayList<>();
				desc.addInformation(descTooltip);
				tooltip.addAll(1, descTooltip);
			}
		}

		if (stack.getItem() instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();
			if (block instanceof IRotate || block instanceof SteamEngineBlock) {
				List<Component> kineticStats = getKineticStats(block, event.getPlayer());
				if (!kineticStats.isEmpty()) {
					tooltip.add(Components.immutableEmpty());
					tooltip.addAll(kineticStats);
				}
			}
		}
	}

	private static void checkLocale() {
		Language currentLanguage = Minecraft.getInstance()
			.getLanguageManager()
			.getSelected();
		if (cachedLanguage != currentLanguage) {
			cachedLanguage = currentLanguage;
			TOOLTIP_CACHE.clear();
		}
	}

	public static String getTooltipTranslationKey(ItemStack stack) {
		Item item = stack.getItem();
		if (TOOLTIP_REFERRALS.containsKey(item)) {
			return TOOLTIP_REFERRALS.get(item)
				.apply(stack) + ".tooltip";
		}
		return stack.getDescriptionId() + ".tooltip";
	}

	@Nullable
	public static ItemDescription getOrCreateTooltip(ItemStack stack) {
		checkLocale();

		String key = getTooltipTranslationKey(stack);
		ItemDescription desc = TOOLTIP_CACHE.get(key);

		if (desc == null) {
			// TODO 0.5.1: Decide on colors and defer creation to registered factory/type based on key or item
			desc = ItemDescription.create(ItemDescription.Palette.GRAY, key);
			TOOLTIP_CACHE.put(key, desc);
		}

		if (desc == ItemDescription.MISSING) {
			return null;
		}

		return desc;
	}

	public static List<Component> getKineticStats(Block block, Player player) {
		List<Component> list = new ArrayList<>();

		CKinetics config = AllConfigs.SERVER.kinetics;
		LangBuilder rpmUnit = Lang.translate("generic.unit.rpm");
		LangBuilder suUnit = Lang.translate("generic.unit.stress");

		boolean hasGoggles = GogglesItem.isWearingGoggles(player);

		boolean showStressImpact;
		if (block instanceof IRotate) {
			showStressImpact = !((IRotate) block).hideStressImpact();
		} else {
			showStressImpact = true;
		}

		boolean hasStressImpact =
			StressImpact.isEnabled() && showStressImpact && BlockStressValues.getImpact(block) > 0;
		boolean hasStressCapacity = StressImpact.isEnabled() && BlockStressValues.hasCapacity(block);

		if (hasStressImpact) {
			Lang.translate("tooltip.stressImpact")
				.style(GRAY)
				.addTo(list);

			double impact = BlockStressValues.getImpact(block);
			StressImpact impactId = impact >= config.highStressImpact.get() ? StressImpact.HIGH
				: (impact >= config.mediumStressImpact.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			LangBuilder builder = Lang.builder()
				.add(Lang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1))
					.style(impactId.getAbsoluteColor()));

			if (hasGoggles) {
				builder.add(Lang.number(impact))
					.text("x ")
					.add(rpmUnit)
					.addTo(list);
			} else
				builder.translate("tooltip.stressImpact." + Lang.asId(impactId.name()))
					.addTo(list);
		}

		if (hasStressCapacity) {
			Lang.translate("tooltip.capacityProvided")
				.style(GRAY)
				.addTo(list);

			double capacity = BlockStressValues.getCapacity(block);
			Couple<Integer> generatedRPM = BlockStressValues.getGeneratedRPM(block);

			StressImpact impactId = capacity >= config.highCapacity.get() ? StressImpact.HIGH
				: (capacity >= config.mediumCapacity.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			StressImpact opposite = StressImpact.values()[StressImpact.values().length - 2 - impactId.ordinal()];
			LangBuilder builder = Lang.builder()
				.add(Lang.text(TooltipHelper.makeProgressBar(3, impactId.ordinal() + 1))
					.style(opposite.getAbsoluteColor()));

			if (hasGoggles) {
				builder.add(Lang.number(capacity))
					.text("x ")
					.add(rpmUnit)
					.addTo(list);

				if (generatedRPM != null) {
					LangBuilder amount = Lang.number(capacity * generatedRPM.getSecond())
						.add(suUnit);
					Lang.text(" -> ")
						.add(!generatedRPM.getFirst()
							.equals(generatedRPM.getSecond()) ? Lang.translate("tooltip.up_to", amount) : amount)
						.style(DARK_GRAY)
						.addTo(list);
				}
			} else
				builder.translate("tooltip.capacityProvided." + Lang.asId(impactId.name()))
					.addTo(list);
		}

		return list;
	}
}
