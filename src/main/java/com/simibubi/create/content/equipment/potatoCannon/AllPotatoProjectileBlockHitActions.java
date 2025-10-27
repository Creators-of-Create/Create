package com.simibubi.create.content.equipment.potatoCannon;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.Create;
import com.simibubi.create.api.equipment.potatoCannon.PotatoProjectileBlockHitAction;
import com.simibubi.create.api.registry.CreateBuiltInRegistries;
import com.simibubi.create.foundation.mixin.accessor.FallingBlockEntityAccessor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.neoforge.common.SpecialPlantable;

public class AllPotatoProjectileBlockHitActions {

	static {
		register("plant_crop", PlantCrop.CODEC);
		register("place_block_on_ground", PlaceBlockOnGround.CODEC);
	}

	public static void init() {
	}

	private static void register(String name, MapCodec<? extends PotatoProjectileBlockHitAction> codec) {
		Registry.register(CreateBuiltInRegistries.POTATO_PROJECTILE_BLOCK_HIT_ACTION, Create.asResource(name), codec);
	}

	public record PlantCrop(Holder<Block> cropBlock) implements PotatoProjectileBlockHitAction {
		public static final MapCodec<PlantCrop> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(PlantCrop::cropBlock)
		).apply(instance, PlantCrop::new));

		public PlantCrop(Block cropBlock) {
			this(cropBlock.builtInRegistryHolder());
		}

		@Override
		public boolean execute(LevelAccessor level, ItemStack projectile, BlockHitResult ray) {
			if (level.isClientSide())
				return true;

			BlockPos hitPos = ray.getBlockPos();
			if (level instanceof Level l && !l.isLoaded(hitPos))
				return true;
			Direction face = ray.getDirection();
			if (face != Direction.UP)
				return false;
			BlockPos placePos = hitPos.relative(face);
			if (!level.getBlockState(placePos)
				.canBeReplaced())
				return false;
			if (!(cropBlock.value() instanceof SpecialPlantable specialPlantable))
				return false;
			if (specialPlantable.canPlacePlantAtPosition(projectile, level, placePos, null))
				specialPlantable.spawnPlantAtPosition(projectile, level, placePos, null);
			return true;
		}

		@Override
		public MapCodec<? extends PotatoProjectileBlockHitAction> codec() {
			return CODEC;
		}
	}

	public record PlaceBlockOnGround(Holder<Block> block) implements PotatoProjectileBlockHitAction {
		public static final MapCodec<PlaceBlockOnGround> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
			BuiltInRegistries.BLOCK.holderByNameCodec().fieldOf("block").forGetter(PlaceBlockOnGround::block)
		).apply(instance, PlaceBlockOnGround::new));

		public PlaceBlockOnGround(Block block) {
			this(block.builtInRegistryHolder());
		}

		@Override
		public boolean execute(LevelAccessor levelAccessor, ItemStack projectile, BlockHitResult ray) {
			if (levelAccessor.isClientSide())
				return true;

			BlockPos hitPos = ray.getBlockPos();
			if (levelAccessor instanceof Level l && !l.isLoaded(hitPos))
				return true;
			Direction face = ray.getDirection();
			BlockPos placePos = hitPos.relative(face);
			if (!levelAccessor.getBlockState(placePos)
				.canBeReplaced())
				return false;

			if (face == Direction.UP) {
				levelAccessor.setBlock(placePos, block.value()
					.defaultBlockState(), Block.UPDATE_ALL);
			} else if (levelAccessor instanceof Level level) {
				double y = ray.getLocation().y - 0.5;
				if (!level.isEmptyBlock(placePos.above()))
					y = Math.min(y, placePos.getY());
				if (!level.isEmptyBlock(placePos.below()))
					y = Math.max(y, placePos.getY());

				FallingBlockEntity falling = FallingBlockEntityAccessor.create$callInit(level, placePos.getX() + 0.5, y,
					placePos.getZ() + 0.5, block.value().defaultBlockState());
				falling.time = 1;
				level.addFreshEntity(falling);
			}

			return true;
		}

		@Override
		public MapCodec<? extends PotatoProjectileBlockHitAction> codec() {
			return CODEC;
		}
	}
}
