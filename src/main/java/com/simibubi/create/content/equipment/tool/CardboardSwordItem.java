package com.simibubi.create.content.equipment.tool;

import java.util.Map;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllPackets;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStack.TooltipPart;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.LeftClickBlock.Action;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.network.PacketDistributor;

@EventBusSubscriber
public class CardboardSwordItem extends SwordItem {

	public CardboardSwordItem(Properties pProperties) {
		super(AllToolMaterials.CARDBOARD, 3, 1f, pProperties);
	}

	@Override
	public int getBurnTime(ItemStack itemStack, @Nullable RecipeType<?> recipeType) {
		return 1000;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return enchantment == Enchantments.KNOCKBACK;
	}

	@Override
	public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
		Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(book);
		for (Enchantment enchantment : enchants.keySet()) {
			if (enchantment != Enchantments.KNOCKBACK)
				return false;
		}
		return true;
	}

	@Override
	public int getDefaultTooltipHideFlags(@NotNull ItemStack stack) {
		return TooltipPart.MODIFIERS.getMask();
	}

	@SubscribeEvent
	public static void cardboardSwordsMakeNoiseOnClick(LeftClickBlock event) {
		ItemStack itemStack = event.getItemStack();
		if (!AllItems.CARDBOARD_SWORD.isIn(itemStack))
			return;
		if (event.getAction() != Action.START)
			return;
		if (event.getSide() == LogicalSide.CLIENT)
			AllSoundEvents.CARDBOARD_SWORD.playAt(event.getLevel(), event.getPos(), 0.5f, 1.85f, false);
		else
			AllSoundEvents.CARDBOARD_SWORD.play(event.getLevel(), event.getEntity(), event.getPos(), 0.5f, 1.85f);
	}

	@SubscribeEvent
	public static void cardboardSwordsCannotHurtYou(LivingAttackEvent event) {
		Entity attacker = event.getSource()
			.getEntity();
		LivingEntity target = event.getEntity();
		if (target == null || target.getMobType() == MobType.ARTHROPOD)
			return;
		if (!(attacker instanceof LivingEntity livingAttacker
			&& AllItems.CARDBOARD_SWORD.isIn(livingAttacker.getItemInHand(InteractionHand.MAIN_HAND))))
			return;

		AllSoundEvents.CARDBOARD_SWORD.playFrom(attacker, 0.75f, 1.85f);

		event.setCanceled(true);

		// Reference player.attack()
		// This section replicates knockback behaviour without hurting the target

		double knockbackStrength = livingAttacker.getAttributeValue(Attributes.ATTACK_KNOCKBACK) + 2;
		knockbackStrength += EnchantmentHelper.getKnockbackBonus(livingAttacker);
		if (livingAttacker.isSprinting()
			&& (!(livingAttacker instanceof Player p) || p.getAttackStrengthScale(0.5f) > 0.9f))
			++knockbackStrength;

		if (knockbackStrength <= 0)
			return;

		float yRot = livingAttacker.getYRot();
		knockback(target, knockbackStrength, yRot);

		boolean targetIsPlayer = target instanceof Player;
		MobCategory targetType = target.getClassification(false);

		if (target instanceof ServerPlayer sp)
			AllPackets.getChannel()
				.send(PacketDistributor.PLAYER.with(() -> sp), new KnockbackPacket(yRot, (float) knockbackStrength));

		if ((targetType == MobCategory.MISC || targetType == MobCategory.CREATURE) && !targetIsPlayer)
			target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 9, true, false, false));

		livingAttacker.setDeltaMovement(livingAttacker.getDeltaMovement()
			.multiply(0.6D, 1.0D, 0.6D));
		livingAttacker.setSprinting(false);
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
