package com.simibubi.create.content.contraptions.components.crusher;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.foundation.utility.Iterate;

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LootingLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber
public class CrushingWheelTileEntity extends KineticTileEntity {

	public static DamageSource damageSource = new DamageSource("create.crush").setDamageBypassesArmor()
			.setDifficultyScaled();

	public CrushingWheelTileEntity(TileEntityType<? extends CrushingWheelTileEntity> type) {
		super(type);
		setLazyTickRate(20);
	}

	@Override
	public void onSpeedChanged(float prevSpeed) {
		super.onSpeedChanged(prevSpeed);
		fixControllers();
	}

	public void fixControllers() {
		for (Direction d : Iterate.directions)
			((CrushingWheelBlock) getBlockState().getBlock()).updateControllers(getBlockState(), getWorld(), getPos(),
					d);
	}

	@Override
	public AxisAlignedBB makeRenderBoundingBox() {
		return new AxisAlignedBB(pos).grow(1);
	}

	@Override
	public void lazyTick() {
		super.lazyTick();
		fixControllers();
	}

	@SubscribeEvent
	public static void crushingIsFortunate(LootingLevelEvent event) {
		if (event.getDamageSource() != damageSource)
			return;
		event.setLootingLevel(2);		//This does not currently increase mob drops. It seems like this only works for damage done by an entity.
	}

	@SubscribeEvent
	public static void handleCrushedMobDrops(LivingDropsEvent event) {
		if (event.getSource() != CrushingWheelTileEntity.damageSource)
			return;
		Vec3d outSpeed = Vec3d.ZERO;
		for (ItemEntity outputItem : event.getDrops()) {
			outputItem.setMotion(outSpeed);
		}
	}

}
