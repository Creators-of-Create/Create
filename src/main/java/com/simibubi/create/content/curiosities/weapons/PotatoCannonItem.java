package com.simibubi.create.content.curiosities.weapons;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.simibubi.create.AllEnchantments;
import com.simibubi.create.AllEntityTypes;
import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.curiosities.armor.BackTankUtil;
import com.simibubi.create.content.curiosities.zapper.ShootableGadgetItemMethods;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.utility.AnimationTickHolder;
import com.simibubi.create.foundation.utility.Lang;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.ShootableItem;
import net.minecraft.item.UseAction;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PotatoCannonItem extends ShootableItem {

	public static ItemStack CLIENT_CURRENT_AMMO = ItemStack.EMPTY;
	public static final int MAX_DAMAGE = 100;

	public PotatoCannonItem(Properties p_i48487_1_) {
		super(p_i48487_1_);
	}

	@Override
	public boolean canAttackBlock(BlockState p_195938_1_, World p_195938_2_, BlockPos p_195938_3_,
		PlayerEntity p_195938_4_) {
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
	public ActionResultType useOn(ItemUseContext context) {
		return use(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return 1;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return BackTankUtil.getRGBDurabilityForDisplay(stack, maxUses());
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return BackTankUtil.getDurabilityForDisplay(stack, maxUses());
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return BackTankUtil.showDurabilityBar(stack, maxUses());
	}

	private int maxUses() {
		return AllConfigs.SERVER.curiosities.maxPotatoCannonShots.get();
	}

	@Override
	public boolean canBeDepleted() {
		return true;
	}

	public boolean isCannon(ItemStack stack) {
		return stack.getItem() instanceof PotatoCannonItem;
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		return MAX_DAMAGE;
	}

	@Override
	public ActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getItemInHand(hand);
		return findAmmoInInventory(world, player, stack).map(itemStack -> {

			if (ShootableGadgetItemMethods.shouldSwap(player, stack, hand, this::isCannon))
				return ActionResult.fail(stack);

			if (world.isClientSide) {
				CreateClient.POTATO_CANNON_RENDER_HANDLER.dontAnimateItem(hand);
				return ActionResult.success(stack);
			}

			Vector3d barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, hand == Hand.MAIN_HAND,
				new Vector3d(.75f, -0.15f, 1.5f));
			Vector3d correction =
				ShootableGadgetItemMethods.getGunBarrelVec(player, hand == Hand.MAIN_HAND, new Vector3d(-.05f, 0, 0))
					.subtract(player.position()
						.add(0, player.getEyeHeight(), 0));

			PotatoCannonProjectileTypes projectileType = PotatoCannonProjectileTypes.getProjectileTypeOf(itemStack)
				.orElse(PotatoCannonProjectileTypes.FALLBACK);
			Vector3d lookVec = player.getLookAngle();
			Vector3d motion = lookVec.add(correction)
				.normalize()
				.scale(projectileType.getVelocityMultiplier());

			float soundPitch = projectileType.getSoundPitch() + (Create.RANDOM.nextFloat() - .5f) / 4f;

			boolean spray = projectileType.getSplit() > 1;
			Vector3d sprayBase = VecHelper.rotate(new Vector3d(0, 0.1, 0), 360 * Create.RANDOM.nextFloat(), Axis.Z);
			float sprayChange = 360f / projectileType.getSplit();

			for (int i = 0; i < projectileType.getSplit(); i++) {
				PotatoProjectileEntity projectile = AllEntityTypes.POTATO_PROJECTILE.create(world);
				projectile.setItem(itemStack);
				projectile.setEnchantmentEffectsFromCannon(stack);

				Vector3d splitMotion = motion;
				if (spray) {
					float imperfection = 40 * (Create.RANDOM.nextFloat() - 0.5f);
					Vector3d sprayOffset = VecHelper.rotate(sprayBase, i * sprayChange + imperfection, Axis.Z);
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
					player.inventory.removeItem(itemStack);
			}

			if (!BackTankUtil.canAbsorbDamage(player, maxUses()))
				stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));

			Integer cooldown =
				findAmmoInInventory(world, player, stack).flatMap(PotatoCannonProjectileTypes::getProjectileTypeOf)
					.map(PotatoCannonProjectileTypes::getReloadTicks)
					.orElse(10);

			ShootableGadgetItemMethods.applyCooldown(player, stack, hand, this::isCannon, cooldown);
			ShootableGadgetItemMethods.sendPackets(player,
				b -> new PotatoCannonPacket(barrelPos, lookVec.normalize(), itemStack, hand, soundPitch, b));
			return ActionResult.success(stack);
		})
			.orElse(ActionResult.pass(stack));
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || newStack.getItem() != oldStack.getItem();
	}

	private Optional<ItemStack> findAmmoInInventory(World world, PlayerEntity player, ItemStack held) {
		ItemStack findAmmo = player.getProjectile(held);
		return PotatoCannonProjectileTypes.getProjectileTypeOf(findAmmo)
			.map($ -> findAmmo);
	}

	@OnlyIn(Dist.CLIENT)
	public static Optional<ItemStack> getAmmoforPreview(ItemStack cannon) {
		if (AnimationTickHolder.getTicks() % 3 != 0)
			return Optional.of(CLIENT_CURRENT_AMMO)
				.filter(stack -> !stack.isEmpty());

		ClientPlayerEntity player = Minecraft.getInstance().player;
		CLIENT_CURRENT_AMMO = ItemStack.EMPTY;
		if (player == null)
			return Optional.empty();
		ItemStack findAmmo = player.getProjectile(cannon);
		Optional<ItemStack> found = PotatoCannonProjectileTypes.getProjectileTypeOf(findAmmo)
			.map($ -> findAmmo);
		found.ifPresent(stack -> CLIENT_CURRENT_AMMO = stack);
		return found;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, stack);
		int punch = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, stack);
		final float additionalDamage = power * 2;
		final float additionalKnockback = punch * .5f;
		
		getAmmoforPreview(stack).ifPresent(ammo -> {
			String _attack = "potato_cannon.ammo.attack_damage";
			String _reload = "potato_cannon.ammo.reload_ticks";
			String _knockback = "potato_cannon.ammo.knockback";

			tooltip.add(new StringTextComponent(""));
			tooltip.add(new TranslationTextComponent(ammo.getDescriptionId()).append(new StringTextComponent(":"))
				.withStyle(TextFormatting.GRAY));
			PotatoCannonProjectileTypes type = PotatoCannonProjectileTypes.getProjectileTypeOf(ammo)
				.get();
			StringTextComponent spacing = new StringTextComponent(" ");
			TextFormatting green = TextFormatting.GREEN;
			TextFormatting darkGreen = TextFormatting.DARK_GREEN;

			float damageF = type.getDamage() + additionalDamage;
			IFormattableTextComponent damage = new StringTextComponent(
				damageF == MathHelper.floor(damageF) ? "" + MathHelper.floor(damageF) : "" + damageF);
			IFormattableTextComponent reloadTicks = new StringTextComponent("" + type.getReloadTicks());
			IFormattableTextComponent knockback =
				new StringTextComponent("" + (type.getKnockback() + additionalKnockback));

			damage = damage.withStyle(additionalDamage > 0 ? green : darkGreen);
			knockback = knockback.withStyle(additionalKnockback > 0 ? green : darkGreen);
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
		});
		super.appendHoverText(stack, world, tooltip, flag);
	}

	@Override
	public Predicate<ItemStack> getAllSupportedProjectiles() {
		return stack -> PotatoCannonProjectileTypes.getProjectileTypeOf(stack)
			.isPresent();
	}

	@Override
	public int getEnchantmentValue() {
		return 1;
	}

	@Override
	public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
		return true;
	}

	@Override
	public UseAction getUseAnimation(ItemStack stack) {
		return UseAction.NONE;
	}

	@Override
	public int getDefaultProjectileRange() {
		return 15;
	}

}
