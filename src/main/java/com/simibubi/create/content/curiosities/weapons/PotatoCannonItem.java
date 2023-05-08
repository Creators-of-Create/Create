package com.simibubi.create.content.curiosities.weapons;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.curiosities.armor.BacktankUtil;
import com.simibubi.create.content.curiosities.zapper.ShootableGadgetItemMethods;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.CustomArmPoseItem;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Components;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

public class PotatoCannonItem extends ProjectileWeaponItem implements CustomArmPoseItem {

	public static ItemStack CLIENT_CURRENT_AMMO = ItemStack.EMPTY;
	public static final int MAX_DAMAGE = 100;

	public PotatoCannonItem(Properties properties) {
		super(properties.defaultDurability(MAX_DAMAGE));
	}

	@Override
	public boolean canAttackBlock(BlockState state, Level world, BlockPos pos, Player player) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		if (enchantment == Enchantments.POWER_ARROWS)
			return true;
		if (enchantment == Enchantments.PUNCH_ARROWS)
			return true;
		if (enchantment == Enchantments.FLAMING_ARROWS)
			return true;
		if (enchantment == Enchantments.MOB_LOOTING)
			return true;
		if (enchantment == AllEnchantments.POTATO_RECOVERY.get())
			return true;
		return super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return BacktankUtil.isBarVisible(stack, maxUses());
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return BacktankUtil.getBarWidth(stack, maxUses());
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return BacktankUtil.getBarColor(stack, maxUses());
	}

	private int maxUses() {
		return AllConfigs.server().curiosities.maxPotatoCannonShots.get();
	}

	public boolean isCannon(ItemStack stack) {
		return stack.getItem() instanceof PotatoCannonItem;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		return findAmmoInInventory(world, player, stack).map(itemStack -> {

			if (ShootableGadgetItemMethods.shouldSwap(player, stack, hand, this::isCannon))
				return InteractionResultHolder.fail(stack);

			if (world.isClientSide) {
				CreateClient.POTATO_CANNON_RENDER_HANDLER.dontAnimateItem(hand);
				return InteractionResultHolder.success(stack);
			}

			Vec3 barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, hand == InteractionHand.MAIN_HAND,
				new Vec3(.75f, -0.15f, 1.5f));
			Vec3 correction =
				ShootableGadgetItemMethods.getGunBarrelVec(player, hand == InteractionHand.MAIN_HAND, new Vec3(-.05f, 0, 0))
					.subtract(player.position()
						.add(0, player.getEyeHeight(), 0));

			PotatoCannonProjectileType projectileType = PotatoProjectileTypeManager.getTypeForStack(itemStack)
				.orElse(BuiltinPotatoProjectileTypes.FALLBACK);
			Vec3 lookVec = player.getLookAngle();
			Vec3 motion = lookVec.add(correction)
				.normalize()
				.scale(2)
				.scale(projectileType.getVelocityMultiplier());

			float soundPitch = projectileType.getSoundPitch() + (Create.RANDOM.nextFloat() - .5f) / 4f;

			boolean spray = projectileType.getSplit() > 1;
			Vec3 sprayBase = VecHelper.rotate(new Vec3(0, 0.1, 0), 360 * Create.RANDOM.nextFloat(), Axis.Z);
			float sprayChange = 360f / projectileType.getSplit();

			for (int i = 0; i < projectileType.getSplit(); i++) {
				PotatoProjectileEntity projectile = AllEntityTypes.POTATO_PROJECTILE.create(world);
				projectile.setItem(itemStack);
				projectile.setEnchantmentEffectsFromCannon(stack);

				Vec3 splitMotion = motion;
				if (spray) {
					float imperfection = 40 * (Create.RANDOM.nextFloat() - 0.5f);
					Vec3 sprayOffset = VecHelper.rotate(sprayBase, i * sprayChange + imperfection, Axis.Z);
					splitMotion = splitMotion.add(VecHelper.lookAt(sprayOffset, motion));
				}

				if (i != 0)
					projectile.recoveryChance = 0;

				projectile.setPos(barrelPos.x, barrelPos.y, barrelPos.z);
				projectile.setDeltaMovement(splitMotion);
				projectile.setOwner(player);
				world.addFreshEntity(projectile);
			}

			if (!player.isCreative()) {
				itemStack.shrink(1);
				if (itemStack.isEmpty())
					player.getInventory().removeItem(itemStack);
			}

			if (!BacktankUtil.canAbsorbDamage(player, maxUses()))
				stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

			Integer cooldown =
				findAmmoInInventory(world, player, stack).flatMap(PotatoProjectileTypeManager::getTypeForStack)
					.map(PotatoCannonProjectileType::getReloadTicks)
					.orElse(10);

			ShootableGadgetItemMethods.applyCooldown(player, stack, hand, this::isCannon, cooldown);
			ShootableGadgetItemMethods.sendPackets(player,
				b -> new PotatoCannonPacket(barrelPos, lookVec.normalize(), itemStack, hand, soundPitch, b));
			return InteractionResultHolder.success(stack);
		})
			.orElse(InteractionResultHolder.pass(stack));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || newStack.getItem() != oldStack.getItem();
	}

	private Optional<ItemStack> findAmmoInInventory(Level world, Player player, ItemStack held) {
		ItemStack findAmmo = player.getProjectile(held);
		return PotatoProjectileTypeManager.getTypeForStack(findAmmo)
			.map($ -> findAmmo);
	}

	@OnlyIn(Dist.CLIENT)
	public static Optional<ItemStack> getAmmoforPreview(ItemStack cannon) {
		if (AnimationTickHolder.getTicks() % 3 != 0)
			return Optional.of(CLIENT_CURRENT_AMMO)
				.filter(stack -> !stack.isEmpty());

		LocalPlayer player = Minecraft.getInstance().player;
		CLIENT_CURRENT_AMMO = ItemStack.EMPTY;
		if (player == null)
			return Optional.empty();
		ItemStack findAmmo = player.getProjectile(cannon);
		Optional<ItemStack> found = PotatoProjectileTypeManager.getTypeForStack(findAmmo)
			.map($ -> findAmmo);
		found.ifPresent(stack -> CLIENT_CURRENT_AMMO = stack);
		return found;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flag) {
		int power = stack.getEnchantmentLevel(Enchantments.POWER_ARROWS);
		int punch = stack.getEnchantmentLevel(Enchantments.PUNCH_ARROWS);
		final float additionalDamageMult = 1 + power * .2f;
		final float additionalKnockback = punch * .5f;

		getAmmoforPreview(stack).ifPresent(ammo -> {
			String _attack = "potato_cannon.ammo.attack_damage";
			String _reload = "potato_cannon.ammo.reload_ticks";
			String _knockback = "potato_cannon.ammo.knockback";

			tooltip.add(Components.immutableEmpty());
			tooltip.add(Components.translatable(ammo.getDescriptionId()).append(Components.literal(":"))
				.withStyle(ChatFormatting.GRAY));
			PotatoCannonProjectileType type = PotatoProjectileTypeManager.getTypeForStack(ammo)
				.get();
			MutableComponent spacing = Components.literal(" ");
			ChatFormatting green = ChatFormatting.GREEN;
			ChatFormatting darkGreen = ChatFormatting.DARK_GREEN;

			float damageF = type.getDamage() * additionalDamageMult;
			MutableComponent damage = Components.literal(
				damageF == Mth.floor(damageF) ? "" + Mth.floor(damageF) : "" + damageF);
			MutableComponent reloadTicks = Components.literal("" + type.getReloadTicks());
			MutableComponent knockback =
				Components.literal("" + (type.getKnockback() + additionalKnockback));

			damage = damage.withStyle(additionalDamageMult > 1 ? green : darkGreen);
			knockback = knockback.withStyle(additionalKnockback > 0 ? green : darkGreen);
			reloadTicks = reloadTicks.withStyle(darkGreen);

			tooltip.add(spacing.plainCopy()
				.append(Lang.translateDirect(_attack, damage)
					.withStyle(darkGreen)));
			tooltip.add(spacing.plainCopy()
				.append(Lang.translateDirect(_reload, reloadTicks)
					.withStyle(darkGreen)));
			tooltip.add(spacing.plainCopy()
				.append(Lang.translateDirect(_knockback, knockback)
					.withStyle(darkGreen)));
		});
		super.appendHoverText(stack, world, tooltip, flag);
	}

	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return stack -> PotatoProjectileTypeManager.getTypeForStack(stack)
			.isPresent();
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		return true;
	}

	@Override
	public UseAnim getUseAnimation(ItemStack stack) {
		return UseAnim.NONE;
	}

	@Override
	@Nullable
	public ArmPose getArmPose(ItemStack stack, AbstractClientPlayer player, InteractionHand hand) {
		if (!player.swinging) {
			return ArmPose.CROSSBOW_HOLD;
		}
		return null;
	}

	@Override
	public int getDefaultProjectileRange() {
		return 15;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IClientItemExtensions> consumer) {
		consumer.accept(SimpleCustomRenderer.create(this, new PotatoCannonItemRenderer()));
	}

}
