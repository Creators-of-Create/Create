package com.simibubi.create.content.equipment.potatoCannon;

import java.util.function.Consumer;

import com.simibubi.create.CreateClient;
import com.simibubi.create.api.equipment.potatoCannon.PotatoCannonProjectileType;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class PotatoCannonClient {

	public static void dontAnimateItem(InteractionHand hand) {
		CreateClient.POTATO_CANNON_RENDER_HANDLER.dontAnimateItem(hand);
	}

	public static boolean addToTooltip(ItemStack stack, TooltipContext context, Consumer<Component> tooltip) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return false;

		PotatoCannonItem.Ammo ammo = PotatoCannonItem.getAmmo(player, stack);
		if (ammo == null)
			return false;

		HolderLookup.Provider registries = context.registries();
		if (registries == null)
			return true;

		ItemStack ammoStack = ammo.stack();
		PotatoCannonProjectileType type = ammo.type();
		HolderLookup<Enchantment> lookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
		int power = stack.getEnchantmentLevel(lookup.getOrThrow(Enchantments.POWER));
		int punch = stack.getEnchantmentLevel(lookup.getOrThrow(Enchantments.PUNCH));
		final float additionalDamageMult = 1 + power * .2f;
		final float additionalKnockback = punch * .5f;

		String _attack = "potato_cannon.ammo.attack_damage";
		String _reload = "potato_cannon.ammo.reload_ticks";
		String _knockback = "potato_cannon.ammo.knockback";

		tooltip.accept(CommonComponents.EMPTY);
		tooltip.accept(ammoStack.getHoverName()
			.copy()
			.append(Component.literal(":"))
			.withStyle(ChatFormatting.GRAY));
		MutableComponent spacing = CommonComponents.space();
		ChatFormatting green = ChatFormatting.GREEN;
		ChatFormatting darkGreen = ChatFormatting.DARK_GREEN;

		float damageF = type.damage() * additionalDamageMult;
		MutableComponent damage = Component.literal(damageF == Mth.floor(damageF) ? "" + Mth.floor(damageF) : "" + damageF);
		MutableComponent reloadTicks = Component.literal("" + type.reloadTicks());
		MutableComponent knockback = Component.literal("" + (type.knockback() + additionalKnockback));

		damage = damage.withStyle(additionalDamageMult > 1 ? green : darkGreen);
		knockback = knockback.withStyle(additionalKnockback > 0 ? green : darkGreen);
		reloadTicks = reloadTicks.withStyle(darkGreen);

		tooltip.accept(spacing.plainCopy()
			.append(CreateLang.translateDirect(_attack, damage)
				.withStyle(darkGreen)));
		tooltip.accept(spacing.plainCopy()
			.append(CreateLang.translateDirect(_reload, reloadTicks)
				.withStyle(darkGreen)));
		tooltip.accept(spacing.plainCopy()
			.append(CreateLang.translateDirect(_knockback, knockback)
				.withStyle(darkGreen)));
		return true;
	}

	public static void initializeClient(PotatoCannonItem item, Consumer<IClientItemExtensions> consumer) {
		consumer.accept(SimpleCustomRenderer.create(item, new PotatoCannonItemRenderer()));
	}

}
