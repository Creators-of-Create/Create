package com.simibubi.create.foundation.utility.worldWrappers;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import mezz.jei.api.MethodsReturnNonnullByDefault;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.level.ColorResolver;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings("deprecation")
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class WrappedClientWorld extends ClientWorld {
	private static final Minecraft mc = Minecraft.getInstance();
	protected World world;

	private WrappedClientWorld(World world) {
		super(mc.getConnection(), mc.world.getWorldInfo(), world.getRegistryKey(), world.getDimension(), mc.getConnection().viewDistance, world.getProfilerSupplier(), mc.worldRenderer, world.isDebugWorld(), world.getBiomeAccess().seed);
		this.world = world;
	}

	public static WrappedClientWorld of(World world) {
		return new WrappedClientWorld(world);
	}

	@Override
	public boolean isBlockLoaded(BlockPos pos) {
		return world.isBlockLoaded(pos);
	}

	@Override
	public Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, AxisAlignedBB axisAlignedBB) {
		return world.getBlockCollisions(entity, axisAlignedBB);
	}

	@Override
	public Stream<VoxelShape> getBlockCollisions(@Nullable Entity entity, AxisAlignedBB axisAlignedBB, BiPredicate<BlockState, BlockPos> blockStateBlockPosBiPredicate) {
		return world.getBlockCollisions(entity, axisAlignedBB, blockStateBlockPosBiPredicate);
	}

	@Override
	public BlockState getBlockState(BlockPos pos) {
		return world.getBlockState(pos);
	}

	// FIXME: blockstate#getCollisionShape with WrappedClientWorld gives unreliable data (maybe)


	@Override
	public int getLightLevel(LightType type, BlockPos pos) {
		return world.getLightLevel(type, pos);
	}

	@Override
	public int getLightValue(BlockPos pos) {
		return world.getLightValue(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return world.getFluidState(pos);
	}

	@Nullable
	@Override
	public  <T extends LivingEntity> T getClosestEntity(List<? extends T> p_217361_1_, EntityPredicate p_217361_2_, @Nullable LivingEntity p_217361_3_, double p_217361_4_, double p_217361_6_, double p_217361_8_) {
		return world.getClosestEntity(p_217361_1_, p_217361_2_, p_217361_3_, p_217361_4_, p_217361_6_, p_217361_8_);
	}

	@Override
	public int getColor(BlockPos p_225525_1_, ColorResolver p_225525_2_) {
		return world.getColor(p_225525_1_, p_225525_2_);
	}

	// FIXME: Emissive Lighting might not light stuff properly


	@Override
	public void addParticle(IParticleData p_195594_1_, double p_195594_2_, double p_195594_4_, double p_195594_6_, double p_195594_8_, double p_195594_10_, double p_195594_12_) {
		world.addParticle(p_195594_1_, p_195594_2_, p_195594_4_, p_195594_6_, p_195594_8_, p_195594_10_, p_195594_12_);
	}

	@Override
	public void addParticle(IParticleData p_195590_1_, boolean p_195590_2_, double p_195590_3_, double p_195590_5_, double p_195590_7_, double p_195590_9_, double p_195590_11_, double p_195590_13_) {
		world.addParticle(p_195590_1_, p_195590_2_, p_195590_3_, p_195590_5_, p_195590_7_, p_195590_9_, p_195590_11_, p_195590_13_);
	}

	@Override
	public void addOptionalParticle(IParticleData p_195589_1_, double p_195589_2_, double p_195589_4_, double p_195589_6_, double p_195589_8_, double p_195589_10_, double p_195589_12_) {
		world.addOptionalParticle(p_195589_1_, p_195589_2_, p_195589_4_, p_195589_6_, p_195589_8_, p_195589_10_, p_195589_12_);
	}

	@Override
	public void addOptionalParticle(IParticleData p_217404_1_, boolean p_217404_2_, double p_217404_3_, double p_217404_5_, double p_217404_7_, double p_217404_9_, double p_217404_11_, double p_217404_13_) {
		world.addOptionalParticle(p_217404_1_, p_217404_2_, p_217404_3_, p_217404_5_, p_217404_7_, p_217404_9_, p_217404_11_, p_217404_13_);
	}

	@Override
	public void playSound(double p_184134_1_, double p_184134_3_, double p_184134_5_, SoundEvent p_184134_7_, SoundCategory p_184134_8_, float p_184134_9_, float p_184134_10_, boolean p_184134_11_) {
		world.playSound(p_184134_1_, p_184134_3_, p_184134_5_, p_184134_7_,p_184134_8_, p_184134_9_, p_184134_10_, p_184134_11_);
	}

	@Override
	public void playSound(@Nullable PlayerEntity p_184148_1_, double p_184148_2_, double p_184148_4_, double p_184148_6_, SoundEvent p_184148_8_, SoundCategory p_184148_9_, float p_184148_10_, float p_184148_11_) {
		world.playSound(p_184148_1_, p_184148_2_, p_184148_4_, p_184148_6_, p_184148_8_, p_184148_9_, p_184148_10_, p_184148_11_);
	}

	@Nullable
	@Override
	public TileEntity getTileEntity(BlockPos p_175625_1_) {
		return world.getTileEntity(p_175625_1_);
	}
	public World getWrappedWorld() {
		return world;
	}
}
