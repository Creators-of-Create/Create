package com.simibubi.create.content.curiosities.weapons;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import com.simibubi.create.content.curiosities.zapper.ShootableGadgetItemMethods;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.item.render.SimpleCustomRenderer;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.IItemRenderProperties;

public class PotatoCannonItem extends ProjectileWeaponItem {

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
		if (enchantment == Enchantments.PIERCING)
			return true;
		if (enchantment == AllEnchantments.POTATO_RECOVERY.get())
			return true;
		if (enchantment == AllEnchantments.MULTIPLITATO.get())
			return true;
		return super.canApplyAtEnchantingTable(stack, enchantment);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
	}

	@Override
	public boolean isBarVisible(ItemStack stack) {
		return BackTankUtil.isBarVisible(stack, maxUses());
	}

	@Override
	public int getBarWidth(ItemStack stack) {
		return BackTankUtil.getBarWidth(stack, maxUses());
	}

	@Override
	public int getBarColor(ItemStack stack) {
		return BackTankUtil.getBarColor(stack, maxUses());
	}

	private int maxUses() {
		return AllConfigs.SERVER.curiosities.maxPotatoCannonShots.get();
	}

	public boolean isCannon(ItemStack stack) {
		return stack.getItem() instanceof PotatoCannonItem;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		final float EXTRA_SHOT_BASE = 25;
		return findAmmoInInventory(world, player, stack).map(itemStack -> {

			PotatoCannonProjectileType projectileType = PotatoProjectileTypeManager.getTypeForStack(itemStack)
					.orElse(BuiltinPotatoProjectileTypes.FALLBACK);

			if (ShootableGadgetItemMethods.shouldSwap(player, stack, hand, this::isCannon))
				return InteractionResultHolder.fail(stack);

			if (world.isClientSide) {
				CreateClient.POTATO_CANNON_RENDER_HANDLER.dontAnimateItem(hand);
				return InteractionResultHolder.success(stack);
			}

			int extraShot = EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.MULTIPLITATO.get(), stack);

			if (extraShot > 3) // If it's 100%
				extraShot = -1;
			if (extraShot > 0)
				if (Create.RANDOM.nextInt(100)+1 < EXTRA_SHOT_BASE * extraShot)
					extraShot = -1;
				else
					extraShot = 0;

			for (int i = extraShot; i < projectileType.getMaxFire(); i++) {
			if (!projectileType.onlyCostOnce() || projectileType.onlyCostOnce() && i == 0)
				if (!player.isCreative() && itemStack.getCount() < projectileType.getCost())
					return InteractionResultHolder.fail(stack);

			Vec3 barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, hand == InteractionHand.MAIN_HAND,
				new Vec3(.75f, -0.15f, 1.5f));
			Vec3 correction =
				ShootableGadgetItemMethods.getGunBarrelVec(player, hand == InteractionHand.MAIN_HAND, new Vec3(-.05f, 0, 0))
					.subtract(player.position()
						.add(0, player.getEyeHeight(), 0));

			Vec3 lookVec = player.getLookAngle();
			Vec3 motion = lookVec.add(correction)
				.normalize()
				.scale(projectileType.getVelocityMultiplier());

			float soundPitch = projectileType.getSoundPitch() + (Create.RANDOM.nextFloat() - .5f) / 4f;

			//boolean spray = projectileType.getSpray() > 1; No longer needed!
			Vec3 sprayBase = VecHelper.rotate(new Vec3(0, 0.1, 0), 360, Axis.Z); // Base Velocity
			float sprayChange = 360f / projectileType.getSpray();

			for (int j = 0; j < projectileType.getSpray(); j++) {
				PotatoProjectileEntity projectile = AllEntityTypes.POTATO_PROJECTILE.create(world);
				projectile.setItem(itemStack);
				projectile.setEnchantmentEffectsFromCannon(stack);
				float accuracy;
				Vec3 sprayMotion = motion;

				if (j != 0) { // The first shot takes the base accuracy, the other shots takes the spread accuracy
					projectile.recoveryChance = 0;
					accuracy = projectileType.getSprayAccuracy();
				} else if (i < 0) { // If the shot is a bonus shot, give the base accuracy and make it un recoverable (avoid dupes)
					projectile.recoveryChance = 0;
					accuracy = projectileType.getAccuracy();
				}
				else
					accuracy = projectileType.getAccuracy();

				accuracy = Math.max(accuracy, 0f); // Don't allow value bellow 0
				accuracy = Math.min(accuracy, 100f); // Don't allow value above 100
				accuracy -= 100f;

				float imperfection = (40 * accuracy) * (Create.RANDOM.nextFloat() - (0.5f * accuracy));
				Vec3 offset = VecHelper.rotate(sprayBase, i * sprayChange + imperfection, Axis.Z);
				sprayMotion = sprayMotion.add(VecHelper.lookAt(offset, motion));

				projectile.setPos(barrelPos.x, barrelPos.y, barrelPos.z);
				projectile.setDeltaMovement(sprayMotion);
				projectile.setOwner(player);
				world.addFreshEntity(projectile);
			}

			if (!player.isCreative() && projectileType.getCost() > 0) { // If creative or unlimited ammo, don't remove items
				if (!projectileType.onlyCostOnce() && i != -1 || projectileType.onlyCostOnce() && i == 0) // If it cost only once, calculate the ammo at the beginning, else calculate every shot
					itemStack.shrink(projectileType.getCost()); // Remove the correct amount of ammo
				if (itemStack.isEmpty()) // Is the stack empty?
					player.getInventory().removeItem(itemStack); // Delete the item
			}

			if (!BackTankUtil.canAbsorbDamage(player, maxUses()))
				stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

			Integer cooldown = findAmmoInInventory(world, player, stack).flatMap(PotatoProjectileTypeManager::getTypeForStack)
					.map(PotatoCannonProjectileType::getReloadTicks)
					.orElse(10);

			ShootableGadgetItemMethods.applyCooldown(player, stack, hand, this::isCannon, cooldown);
			ShootableGadgetItemMethods.sendPackets(player,
				b -> new PotatoCannonPacket(barrelPos, lookVec.normalize(), itemStack, hand, soundPitch, b));

			}
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
		int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
		int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
		int piercing = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PIERCING, stack);
		int recover = EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.POTATO_RECOVERY.get(), stack);
		int extraShot = EnchantmentHelper.getItemEnchantmentLevel(AllEnchantments.MULTIPLITATO.get(), stack);
		final float additionalDamageMult = 1 + power * .2f;
		final float additionalKnockback = punch * .5f;

		getAmmoforPreview(stack).ifPresent(ammo -> {
			String _attack = "potato_cannon.ammo.attack_damage";
			String _reload = "potato_cannon.ammo.reload_ticks";
			String _knockback = "potato_cannon.ammo.knockback";
			String _piercing = "potato_cannon.ammo.piercing";
			String _minAmmo = "potato_cannon.ammo.min_ammo";
			String _maxAmmo = "potato_cannon.ammo.max_ammo";
			String _recover = "potato_cannon.ammo.recover";
			String _extraShot = "potato_cannon.ammo.extra_shot";
			String _unlimitedAmmo = "potato_cannon.ammo.unlimited_ammo";

			tooltip.add(new TextComponent(""));
			tooltip.add(new TranslatableComponent(ammo.getDescriptionId()).append(new TextComponent(":"))
				.withStyle(ChatFormatting.GRAY));
			PotatoCannonProjectileType type = PotatoProjectileTypeManager.getTypeForStack(ammo)
				.get();
			TextComponent spacing = new TextComponent(" ");
			ChatFormatting green = ChatFormatting.GREEN;
			ChatFormatting darkGreen = ChatFormatting.DARK_GREEN;
			ChatFormatting aqua = ChatFormatting.AQUA;

			float damageF = type.getDamage() * additionalDamageMult;
			MutableComponent damage = new TextComponent(
				damageF == Mth.floor(damageF) ? "" + Mth.floor(damageF) : "" + damageF);
			MutableComponent reloadTicks = new TextComponent("" + type.getReloadTicks());
			MutableComponent knockback =
				new TextComponent("" + (type.getKnockback() + additionalKnockback));
			MutableComponent pierce =
					new TextComponent("" + (type.getPiercing() + piercing));
			MutableComponent minAmmo =
					new TextComponent("" + (type.getCost()));
			MutableComponent maxAmmo =
					new TextComponent("" + (type.getMaxFire()*type.getCost()));
			MutableComponent extraChance;
			if (extraShot > 3)
				extraChance = new TextComponent("100%");
			else
				extraChance = new TextComponent("" + (extraShot*25f) + "%");
			MutableComponent recoverChance;
			if (recover > 6)
				recoverChance = new TextComponent("100%");
			else
				recoverChance = new TextComponent("" + (12.5f + recover*12.5f) + "%");

			damage = damage.withStyle(additionalDamageMult > 1 ? green : darkGreen);
			knockback = knockback.withStyle(additionalKnockback > 0 ? green : darkGreen);
			pierce = pierce.withStyle(piercing > 0 ? green : darkGreen);
			minAmmo = minAmmo.withStyle(green);
			maxAmmo = maxAmmo.withStyle(green);
			extraChance = extraChance.withStyle(extraShot > 3 ? aqua : green);
			recoverChance = recoverChance.withStyle(recover > 6 ? aqua : green);
			reloadTicks = reloadTicks.withStyle(darkGreen);

			tooltip.add(spacing.plainCopy()
				.append(Lang.translate(_attack, damage)
					.withStyle(darkGreen)));
			tooltip.add(spacing.plainCopy()
				.append(Lang.translate(_reload, reloadTicks)
					.withStyle(darkGreen)));
			tooltip.add(spacing.plainCopy()
				.append(Lang.translate(_knockback, knockback)
					.withStyle(darkGreen)));
			if (type.getCost()*type.getMaxFire() > 1 || type.getCost() > 1) // If it shoots more than once or if it cost more than one
				tooltip.add(spacing.plainCopy()
					.append(Lang.translate(_minAmmo, minAmmo)
						.withStyle(darkGreen)));
			if (type.getCost() == 0) // If it costs nothing
				tooltip.add(spacing.plainCopy()
						.append(Lang.translate(_unlimitedAmmo)
								.withStyle(darkGreen)));
			else if (type.getCost()*type.getMaxFire() > 1) // If it shoots more than once
				tooltip.add(spacing.plainCopy()
					.append(Lang.translate(_maxAmmo, maxAmmo)
						.withStyle(darkGreen)));
			if (recover > 0) // If it has at least one level of Potato Recover
				tooltip.add(spacing.plainCopy()
						.append(Lang.translate(_recover, recoverChance)
								.withStyle(darkGreen)));
			if (extraShot > 0) // If it has at least one level of Multiplitato
			tooltip.add(spacing.plainCopy()
					.append(Lang.translate(_extraShot, extraChance)
							.withStyle(darkGreen)));
			if (piercing + type.getPiercing() > 0) // If it has at least one point of piercing (either the item or the enchant)
				tooltip.add(spacing.plainCopy()
					.append(Lang.translate(_piercing, pierce)
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
	public int getDefaultProjectileRange() {
		return 15;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void initializeClient(Consumer<IItemRenderProperties> consumer) {
		consumer.accept(SimpleCustomRenderer.create(this, new PotatoCannonItemRenderer()));
	}

}
