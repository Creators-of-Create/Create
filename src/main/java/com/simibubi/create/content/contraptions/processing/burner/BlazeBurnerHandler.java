package com.simibubi.create.content.contraptions.processing.burner;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.contraptions.processing.burner.BlazeBurnerTileEntity.FuelType;

import net.minecraft.block.BlockState;
import net.minecraft.entity.projectile.EggEntity;
import net.minecraft.entity.projectile.PotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BlazeBurnerHandler {

	@SubscribeEvent
	public static void onThrowableImpact(ProjectileImpactEvent.Throwable event) {
		thrownEggsGetEatenByBurner(event);
		splashExtinguishesBurner(event);
	}

	public static void thrownEggsGetEatenByBurner(ProjectileImpactEvent.Throwable event) {
		if (!(event.getThrowable() instanceof EggEntity))
			return;

		if (event.getRayTraceResult()
			.getType() != RayTraceResult.Type.BLOCK)
			return;

		TileEntity tile = event.getThrowable().world.getTileEntity(new BlockPos(event.getRayTraceResult()
			.getHitVec()));
		if (!(tile instanceof BlazeBurnerTileEntity)) {
			return;
		}

		event.setCanceled(true);
		event.getThrowable()
			.setMotion(Vector3d.ZERO);
		event.getThrowable()
			.remove();

		World world = event.getThrowable().world;
		if (world.isRemote)
			return;
		
		BlazeBurnerTileEntity heater = (BlazeBurnerTileEntity) tile;
		if (heater.activeFuel != FuelType.SPECIAL) {
			heater.activeFuel = FuelType.NORMAL;
			heater.remainingBurnTime =
				MathHelper.clamp(heater.remainingBurnTime + 80, 0, BlazeBurnerTileEntity.maxHeatCapacity);
			heater.updateBlockState();
			heater.notifyUpdate();
		}
		
		AllSoundEvents.BLAZE_MUNCH.playOnServer(world, heater.getPos());
	}

	public static void splashExtinguishesBurner(ProjectileImpactEvent.Throwable event) {
		if (event.getThrowable().world.isRemote)
			return;

		if (!(event.getThrowable() instanceof PotionEntity))
			return;
		PotionEntity entity = (PotionEntity) event.getThrowable();

		if (event.getRayTraceResult()
			.getType() != RayTraceResult.Type.BLOCK)
			return;

		ItemStack stack = entity.getItem();
		Potion potion = PotionUtils.getPotionFromItem(stack);
		if (potion == Potions.WATER && PotionUtils.getEffectsFromStack(stack).isEmpty()) {
			BlockRayTraceResult result = (BlockRayTraceResult) event.getRayTraceResult();
			World world = entity.world;
			Direction face = result.getFace();
			BlockPos pos = result.getPos().offset(face);

			extinguishLitBurners(world, pos, face);
			extinguishLitBurners(world, pos.offset(face.getOpposite()), face);

			for (Direction face1 : Direction.Plane.HORIZONTAL) {
				extinguishLitBurners(world, pos.offset(face1), face1);
			}
		}
	}

	private static void extinguishLitBurners(World world, BlockPos pos, Direction direction) {
		BlockState state = world.getBlockState(pos);
		if (AllBlocks.LIT_BLAZE_BURNER.has(state)) {
			world.playSound(null, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F, 2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
			world.setBlockState(pos, AllBlocks.BLAZE_BURNER.getDefaultState());
		}
	}

}
