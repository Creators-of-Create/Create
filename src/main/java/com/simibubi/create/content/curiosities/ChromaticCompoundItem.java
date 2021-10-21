package com.simibubi.create.content.curiosities;

import java.util.Random;

import org.apache.commons.lang3.mutable.MutableBoolean;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.relays.belt.transport.TransportedItemStack;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CRecipes;
import com.simibubi.create.foundation.tileEntity.TileEntityBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.BeaconTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

public class ChromaticCompoundItem extends Item {

	public ChromaticCompoundItem(Properties properties) {
		super(properties);
	}

	@Override
	public boolean shouldOverrideMultiplayerNbt() {
		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		int light = stack.getOrCreateTag()
			.getInt("CollectingLight");
		return 1 - light / (float) AllConfigs.SERVER.recipes.lightSourceCountForRefinedRadiance.get();
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		int light = stack.getOrCreateTag()
			.getInt("CollectingLight");
		return light > 0;
	}

	@Override
	public int getRGBDurabilityForDisplay(ItemStack stack) {
		return Color.mixColors(0x413c69, 0xFFFFFF, (float) (1 - getDurabilityForDisplay(stack)));
	}

	@Override
	public int getItemStackLimit(ItemStack stack) {
		return showDurabilityBar(stack) ? 1 : 16;
	}

	@Override
	public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
		double y = entity.getY();
		double yMotion = entity.getDeltaMovement().y;
		World world = entity.level;
		CompoundNBT data = entity.getPersistentData();
		CompoundNBT itemData = entity.getItem()
			.getOrCreateTag();

		Vector3d positionVec = entity.position();
		CRecipes config = AllConfigs.SERVER.recipes;
		if (world.isClientSide) {
			int light = itemData.getInt("CollectingLight");
			if (random.nextInt(config.lightSourceCountForRefinedRadiance.get() + 20) < light) {
				Vector3d start = VecHelper.offsetRandomly(positionVec, random, 3);
				Vector3d motion = positionVec.subtract(start)
					.normalize()
					.scale(.2f);
				world.addParticle(ParticleTypes.END_ROD, start.x, start.y, start.z, motion.x, motion.y, motion.z);
			}
			return false;
		}

		// Convert to Shadow steel if in void
		if (y < 0 && y - yMotion < -10 && config.enableShadowSteelRecipe.get()) {
			ItemStack newStack = AllItems.SHADOW_STEEL.asStack();
			newStack.setCount(stack.getCount());
			data.putBoolean("JustCreated", true);
			entity.setItem(newStack);
		}

		if (!config.enableRefinedRadianceRecipe.get())
			return false;

		// Convert to Refined Radiance if eaten enough light sources
		if (itemData.getInt("CollectingLight") >= config.lightSourceCountForRefinedRadiance.get()) {
			ItemStack newStack = AllItems.REFINED_RADIANCE.asStack();
			ItemEntity newEntity = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), newStack);
			newEntity.setDeltaMovement(entity.getDeltaMovement());
			newEntity.getPersistentData()
				.putBoolean("JustCreated", true);
			itemData.remove("CollectingLight");
			world.addFreshEntity(newEntity);

			stack.split(1);
			entity.setItem(stack);
			if (stack.isEmpty())
				entity.remove();
			return false;
		}

		// Is inside beacon beam?
		boolean isOverBeacon = false;
		int entityX = MathHelper.floor(entity.getX());
		int entityZ = MathHelper.floor(entity.getZ());
		int localWorldHeight = world.getHeight(Heightmap.Type.WORLD_SURFACE, entityX, entityZ);

		BlockPos.Mutable testPos =
			new BlockPos.Mutable(entityX, Math.min(MathHelper.floor(entity.getY()), localWorldHeight), entityZ);

		while (testPos.getY() > 0) {
			testPos.move(Direction.DOWN);
			BlockState state = world.getBlockState(testPos);
			if (state.getLightBlock(world, testPos) >= 15 && state.getBlock() != Blocks.BEDROCK)
				break;
			if (state.getBlock() == Blocks.BEACON) {
				TileEntity te = world.getBlockEntity(testPos);

				if (!(te instanceof BeaconTileEntity))
					break;

				BeaconTileEntity bte = (BeaconTileEntity) te;

				if (bte.getLevels() != 0 && !bte.beamSections.isEmpty())
					isOverBeacon = true;

				break;
			}
		}

		if (isOverBeacon) {
			ItemStack newStack = AllItems.REFINED_RADIANCE.asStack();
			newStack.setCount(stack.getCount());
			data.putBoolean("JustCreated", true);
			entity.setItem(newStack);
			return false;
		}

		// Find a light source and eat it.
		Random r = world.random;
		int range = 3;
		float rate = 1 / 2f;
		if (r.nextFloat() > rate)
			return false;

		BlockPos randomOffset = new BlockPos(VecHelper.offsetRandomly(positionVec, r, range));
		BlockState state = world.getBlockState(randomOffset);

		TransportedItemStackHandlerBehaviour behaviour =
			TileEntityBehaviour.get(world, randomOffset, TransportedItemStackHandlerBehaviour.TYPE);

		// Find a placed light source
		if (behaviour == null) {
			if (checkLight(stack, entity, world, itemData, positionVec, randomOffset, state))
				world.destroyBlock(randomOffset, false);
			return false;
		}

		// Find a light source from a depot/belt (chunk rebuild safe)
		MutableBoolean success = new MutableBoolean(false);
		behaviour.handleProcessingOnAllItems(ts -> {

			ItemStack heldStack = ts.stack;
			if (!(heldStack.getItem() instanceof BlockItem))
				return TransportedResult.doNothing();

			BlockItem blockItem = (BlockItem) heldStack.getItem();
			if (blockItem.getBlock() == null)
				return TransportedResult.doNothing();

			BlockState stateToCheck = blockItem.getBlock()
				.defaultBlockState();

			if (!success.getValue()
				&& checkLight(stack, entity, world, itemData, positionVec, randomOffset, stateToCheck)) {
				success.setTrue();
				if (ts.stack.getCount() == 1)
					return TransportedResult.removeItem();
				TransportedItemStack left = ts.copy();
				left.stack.shrink(1);
				return TransportedResult.convertTo(left);
			}

			return TransportedResult.doNothing();

		});
		return false;
	}

	public boolean checkLight(ItemStack stack, ItemEntity entity, World world, CompoundNBT itemData,
		Vector3d positionVec, BlockPos randomOffset, BlockState state) {
		if (state.getLightValue(world, randomOffset) == 0)
			return false;
		if (state.getDestroySpeed(world, randomOffset) == -1)
			return false;
		if (state.getBlock() == Blocks.BEACON)
			return false;

		RayTraceContext context = new RayTraceContext(positionVec.add(new Vector3d(0, 0.5, 0)),
			VecHelper.getCenterOf(randomOffset), BlockMode.COLLIDER, FluidMode.NONE, entity);
		if (!randomOffset.equals(world.clip(context)
			.getBlockPos()))
			return false;

		ItemStack newStack = stack.split(1);
		newStack.getOrCreateTag()
			.putInt("CollectingLight", itemData.getInt("CollectingLight") + 1);
		ItemEntity newEntity = new ItemEntity(world, entity.getX(), entity.getY(), entity.getZ(), newStack);
		newEntity.setDeltaMovement(entity.getDeltaMovement());
		newEntity.setDefaultPickUpDelay();
		world.addFreshEntity(newEntity);
		entity.lifespan = 6000;
		if (stack.isEmpty())
			entity.remove();
		return true;
	}

}
