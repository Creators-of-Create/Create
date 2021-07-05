package com.simibubi.create.content.curiosities.armor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import com.simibubi.create.foundation.config.AllConfigs;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CopperBacktankItem extends CopperArmorItem {

	public static final int DURABILITY_BAR = 0xefefef;
	public static final int RECHARGES_PER_TICK = 4;
	private BlockItem blockItem;

	public CopperBacktankItem(Properties p_i48534_3_, BlockItem blockItem) {
		super(EquipmentSlotType.CHEST, p_i48534_3_);
		this.blockItem = blockItem;
	}

	@Override
	public ActionResultType onItemUse(ItemUseContext p_195939_1_) {
		return blockItem.onItemUse(p_195939_1_);
	}

	@Override
	public boolean isDamageable() {
		return false;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return DURABILITY_BAR;
	}

	@Override
	public void fillItemGroup(ItemGroup p_150895_1_, NonNullList<ItemStack> p_150895_2_) {
		if (!isInGroup(p_150895_1_))
			return;

		ItemStack stack = new ItemStack(this);
		CompoundNBT nbt = new CompoundNBT();
		nbt.putInt("Air", AllConfigs.SERVER.curiosities.maxAirInBacktank.get());
		stack.setTag(nbt);
		p_150895_2_.add(stack);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		return 1 - MathHelper
			.clamp(getRemainingAir(stack) / ((float) AllConfigs.SERVER.curiosities.maxAirInBacktank.get()), 0, 1);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return true;
	}

	public static int getRemainingAir(ItemStack stack) {
		CompoundNBT orCreateTag = stack.getOrCreateTag();
		return orCreateTag.getInt("Air");
	}

	@SubscribeEvent
	public static void rechargePneumaticTools(TickEvent.PlayerTickEvent event) {
		PlayerEntity player = event.player;
		if (event.phase != TickEvent.Phase.START)
			return;
		if (event.side != LogicalSide.SERVER)
			return;
		if (player.isSpectator())
			return;
		ItemStack tankStack = BackTankUtil.get(player);
		if (tankStack.isEmpty())
			return;

		PlayerInventory inv = player.inventory;

		List<ItemStack> toCharge = Streams.concat(Stream.of(player.getHeldItemMainhand()), inv.offHandInventory.stream(),
			inv.armorInventory.stream(), inv.mainInventory.stream())
			.filter(s -> s.getItem() instanceof IBackTankRechargeable && s.isDamaged())
			.collect(Collectors.toList());

		int charges = RECHARGES_PER_TICK;
		for (ItemStack stack : toCharge) {
			while (stack.isDamaged()) {
				if (BackTankUtil.canAbsorbDamage(event.player, ((IBackTankRechargeable) stack.getItem()).maxUses())) {
					stack.setDamage(stack.getDamage() - 1);
					charges--;
					if (charges <= 0)
						return;
				} else {
					return;
				}
			}
		}

	}
}
