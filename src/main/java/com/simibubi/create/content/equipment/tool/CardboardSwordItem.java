package com.simibubi.create.content.equipment.tool;

import java.util.function.Consumer;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.createmod.catnip.platform.CatnipServices;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber
public class CardboardSwordItem extends SwordItem {

	public CardboardSwordItem(Properties pProperties) {
		super(AllToolMaterials.CARDBOARD, pProperties);
	}

	@Override
	public boolean supportsEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
		return enchantment.getKey() == Enchantments.KNOCKBACK;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		ItemEnchantments enchants = book.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
		for (Holder<Enchantment> enchantment : enchants.keySet()) {
			if (enchantment.getKey() != Enchantments.KNOCKBACK)
				return false;
		}
		return true;
	}

	@SubscribeEvent
	public static void cardboardSwordsMakeNoiseOnClick(PlayerInteractEvent.LeftClickBlock event) {
		ItemStack itemStack = event.getItemStack();
		if (!AllItems.CARDBOARD_SWORD.isIn(itemStack))
			return;
		if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START)
			return;
		if (event.getSide() == LogicalSide.CLIENT)
			AllSoundEvents.CARDBOARD_SWORD.playAt(event.getLevel(), event.getPos(), 0.5f, 1.85f, false);
		else
			AllSoundEvents.CARDBOARD_SWORD.play(event.getLevel(), event.getEntity(), event.getPos(), 0.5f, 1.85f);
	}

	// We set priority to highest just so we catch this before anyone does anything else
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void cardboardSwordsCannotHurtYou(AttackEntityEvent event) {
		Player attacker = event.getEntity();
		if (!(event.getTarget() instanceof LivingEntity target) || target.getType().is(EntityTypeTags.ARTHROPOD))
			return;
		ItemStack stack = attacker.getItemInHand(InteractionHand.MAIN_HAND);
		if (!(AllItems.CARDBOARD_SWORD.isIn(stack)))
			return;

		AllSoundEvents.CARDBOARD_SWORD.playFrom(attacker, 0.75f, 1.85f);

		event.setCanceled(true);

		// Reference player.attack()
		// This section replicates knockback behaviour without hurting the target

		float knockbackStrength = (float) (attacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK) + 2);
		if (attacker.level() instanceof ServerLevel serverLevel)
			knockbackStrength = EnchantmentHelper.modifyKnockback(serverLevel, stack, target, serverLevel.damageSources().playerAttack(attacker), knockbackStrength);
		if (attacker.isSprinting() && attacker.getAttackStrengthScale(0.5f) > 0.9f)
			++knockbackStrength;

		if (knockbackStrength <= 0)
			return;

		float yRot = attacker.getYRot();
		knockback(target, knockbackStrength, yRot);

		boolean targetIsPlayer = target instanceof Player;
		MobCategory targetType = target.getClassification(false);

		if (target instanceof ServerPlayer sp)
			CatnipServices.NETWORK.sendToClient(sp, new KnockbackPacket(yRot, (float) knockbackStrength));

		if ((targetType == MobCategory.MISC || targetType == MobCategory.CREATURE) && !targetIsPlayer)
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9, true, false, false));

		attacker.setDeltaMovement(attacker.getDeltaMovement()
			.multiply(0.6D, 1.0D, 0.6D));
		attacker.setSprinting(false);
	}

	public static void knockback(LivingEntity target, double knockbackStrength, float yRot) {
		target.stopRiding();
		target.knockback(knockbackStrength * 0.5F, Mth.sin(yRot * Mth.DEG_TO_RAD), -Mth.cos(yRot * Mth.DEG_TO_RAD));
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(SimpleCustomRenderer.create(this, new CardboardSwordItemRenderer()));
	}
}
