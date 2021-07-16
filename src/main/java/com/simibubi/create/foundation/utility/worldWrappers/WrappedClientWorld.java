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
		super(mc.getConnection(), mc.level.getLevelData(), world.dimension(), world.dimensionType(), mc.getConnection().serverChunkRadius, world.getProfilerSupplier(), mc.levelRenderer, world.isDebug(), world.getBiomeManager().biomeZoomSeed);
		this.world = world;
	}

	public static WrappedClientWorld of(World world) {
		return new WrappedClientWorld(world);
	}

	@Override
	public boolean hasChunkAt(BlockPos pos) {
		return world.hasChunkAt(pos);
	}
	
	@Override
	public boolean isLoaded(BlockPos pos) {
		return world.isLoaded(pos);
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
	public int getBrightness(LightType type, BlockPos pos) {
		return world.getBrightness(type, pos);
	}

	@Override
	public int getLightEmission(BlockPos pos) {
		return world.getLightEmission(pos);
	}

	@Override
	public FluidState getFluidState(BlockPos pos) {
		return world.getFluidState(pos);
	}

	@Nullable
	@Override
	public  <T extends LivingEntity> T getNearestEntity(List<? extends T> pEntities, EntityPredicate pPredicate, @Nullable LivingEntity pTarget, double pX, double pY, double pZ) {
		return world.getNearestEntity(pEntities, pPredicate, pTarget, pX, pY, pZ);
	}

	@Override
	public int getBlockTint(BlockPos pBlockPosIn, ColorResolver pColorResolverIn) {
		return world.getBlockTint(pBlockPosIn, pColorResolverIn);
	}

	// FIXME: Emissive Lighting might not light stuff properly


	@Override
	public void addParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
		world.addParticle(pParticleData, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
	}

	@Override
	public void addParticle(IParticleData pParticleData, boolean pForceAlwaysRender, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
		world.addParticle(pParticleData, pForceAlwaysRender, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
	}

	@Override
	public void addAlwaysVisibleParticle(IParticleData pParticleData, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
		world.addAlwaysVisibleParticle(pParticleData, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
	}

	@Override
	public void addAlwaysVisibleParticle(IParticleData pParticleData, boolean pIgnoreRange, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
		world.addAlwaysVisibleParticle(pParticleData, pIgnoreRange, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
	}

	@Override
	public void playLocalSound(double pX, double pY, double pZ, SoundEvent pSoundIn, SoundCategory pCategory, float pVolume, float pPitch, boolean pDistanceDelay) {
		world.playLocalSound(pX, pY, pZ, pSoundIn,pCategory, pVolume, pPitch, pDistanceDelay);
	}

	@Override
	public void playSound(@Nullable PlayerEntity pPlayer, double pX, double pY, double pZ, SoundEvent pSoundIn, SoundCategory pCategory, float pVolume, float pPitch) {
		world.playSound(pPlayer, pX, pY, pZ, pSoundIn, pCategory, pVolume, pPitch);
	}

	@Nullable
	@Override
	public TileEntity getBlockEntity(BlockPos pPos) {
		return world.getBlockEntity(pPos);
	}
	public World getWrappedWorld() {
		return world;
	}
}
