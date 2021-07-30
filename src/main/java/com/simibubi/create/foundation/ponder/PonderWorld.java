package com.simibubi.create.foundation.ponder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.fluids.tank.FluidTankTileEntity;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class PonderWorld extends SchematicWorld {

	public PonderScene scene;

	protected Map<BlockPos, BlockState> originalBlocks;
	protected Map<BlockPos, TileEntity> originalTileEntities;
	protected Map<BlockPos, Integer> blockBreakingProgressions;
	protected List<Entity> originalEntities;
	private LazyValue<ClientWorld> asClientWorld = new LazyValue<>(() -> WrappedClientWorld.of(this));

	protected PonderWorldParticles particles;
	private final Map<ResourceLocation, IParticleFactory<?>> particleFactories;

	int overrideLight;
	Selection mask;

	public PonderWorld(BlockPos anchor, World original) {
		super(anchor, original);
		originalBlocks = new HashMap<>();
		originalTileEntities = new HashMap<>();
		blockBreakingProgressions = new HashMap<>();
		originalEntities = new ArrayList<>();
		particles = new PonderWorldParticles(this);

		// ParticleManager.factories - ATs don't seem to like this one
		particleFactories = ObfuscationReflectionHelper.getPrivateValue(ParticleManager.class,
			Minecraft.getInstance().particleEngine, "field_178932_g");
	}

	public void createBackup() {
		originalBlocks.clear();
		originalTileEntities.clear();
		blocks.forEach((k, v) -> originalBlocks.put(k, v));
		tileEntities.forEach(
			(k, v) -> originalTileEntities.put(k, TileEntity.loadStatic(blocks.get(k), v.save(new CompoundNBT()))));
		entities.forEach(e -> EntityType.create(e.serializeNBT(), this)
			.ifPresent(originalEntities::add));
	}

	public void restore() {
		entities.clear();
		blocks.clear();
		tileEntities.clear();
		blockBreakingProgressions.clear();
		renderedTileEntities.clear();
		originalBlocks.forEach((k, v) -> blocks.put(k, v));
		originalTileEntities.forEach((k, v) -> {
			TileEntity te = TileEntity.loadStatic(originalBlocks.get(k), v.save(new CompoundNBT()));
			onTEadded(te, te.getBlockPos());
			tileEntities.put(k, te);
			renderedTileEntities.add(te);
		});
		originalEntities.forEach(e -> EntityType.create(e.serializeNBT(), this)
			.ifPresent(entities::add));
		particles.clearEffects();
		fixControllerTileEntities();
	}

	public void restoreBlocks(Selection selection) {
		selection.forEach(p -> {
			if (originalBlocks.containsKey(p))
				blocks.put(p, originalBlocks.get(p));
			if (originalTileEntities.containsKey(p)) {
				TileEntity te = TileEntity.loadStatic(originalBlocks.get(p), originalTileEntities.get(p)
					.save(new CompoundNBT()));
				onTEadded(te, te.getBlockPos());
				tileEntities.put(p, te);
			}
		});
		redraw();
	}

	private void redraw() {
		if (scene != null)
			scene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
	}

	public void pushFakeLight(int light) {
		this.overrideLight = light;
	}

	public void popLight() {
		this.overrideLight = -1;
	}

	@Override
	public int getBrightness(LightType p_226658_1_, BlockPos p_226658_2_) {
		return overrideLight == -1 ? 15 : overrideLight;
	}

	public void setMask(Selection mask) {
		this.mask = mask;
	}

	public void clearMask() {
		this.mask = null;
	}

	@Override
	public BlockState getBlockState(BlockPos globalPos) {
		if (mask != null && !mask.test(globalPos.subtract(anchor)))
			return Blocks.AIR.defaultBlockState();
		return super.getBlockState(globalPos);
	}

	@Override // For particle collision
	public IBlockReader getChunkForCollisions(int p_225522_1_, int p_225522_2_) {
		return this;
	}

	public void renderEntities(MatrixStack ms, SuperRenderTypeBuffer buffer, ActiveRenderInfo ari, float pt) {
		Vector3d Vector3d = ari.getPosition();
		double d0 = Vector3d.x();
		double d1 = Vector3d.y();
		double d2 = Vector3d.z();

		for (Entity entity : entities) {
			if (entity.tickCount == 0) {
				entity.xOld = entity.getX();
				entity.yOld = entity.getY();
				entity.zOld = entity.getZ();
			}
			renderEntity(entity, d0, d1, d2, pt, ms, buffer);
		}

		buffer.draw(RenderType.entitySolid(PlayerContainer.BLOCK_ATLAS));
		buffer.draw(RenderType.entityCutout(PlayerContainer.BLOCK_ATLAS));
		buffer.draw(RenderType.entityCutoutNoCull(PlayerContainer.BLOCK_ATLAS));
		buffer.draw(RenderType.entitySmoothCutout(PlayerContainer.BLOCK_ATLAS));
	}

	private void renderEntity(Entity entity, double x, double y, double z, float pt, MatrixStack ms,
		IRenderTypeBuffer buffer) {
		double d0 = MathHelper.lerp((double) pt, entity.xOld, entity.getX());
		double d1 = MathHelper.lerp((double) pt, entity.yOld, entity.getY());
		double d2 = MathHelper.lerp((double) pt, entity.zOld, entity.getZ());
		float f = MathHelper.lerp(pt, entity.yRotO, entity.yRot);
		EntityRendererManager renderManager = Minecraft.getInstance()
			.getEntityRenderDispatcher();
		int light = renderManager.getRenderer(entity)
			.getPackedLightCoords(entity, pt);
		renderManager.render(entity, d0 - x, d1 - y, d2 - z, f, pt, ms, buffer, light);
	}

	public void renderParticles(MatrixStack ms, IRenderTypeBuffer buffer, ActiveRenderInfo ari, float pt) {
		particles.renderParticles(ms, buffer, ari, pt);
	}

	public void tick() {
		particles.tick();

		for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();

			entity.tickCount++;
			entity.xOld = entity.getX();
			entity.yOld = entity.getY();
			entity.zOld = entity.getZ();
			entity.tick();

			if (entity.getY() <= -.5f)
				entity.remove();

			if (!entity.isAlive())
				iterator.remove();
		}
	}

	@Override
	public void addParticle(IParticleData data, double x, double y, double z, double mx, double my, double mz) {
		addParticle(makeParticle(data, x, y, z, mx, my, mz));
	}

	@Override
	public void addAlwaysVisibleParticle(IParticleData data, double x, double y, double z, double mx, double my, double mz) {
		addParticle(data, x, y, z, mx, my, mz);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <T extends IParticleData> Particle makeParticle(T data, double x, double y, double z, double mx, double my,
		double mz) {
		ResourceLocation key = ForgeRegistries.PARTICLE_TYPES.getKey(data.getType());
		IParticleFactory<T> iparticlefactory = (IParticleFactory<T>) particleFactories.get(key);
		return iparticlefactory == null ? null
			: iparticlefactory.createParticle(data, asClientWorld.get(), x, y, z, mx, my, mz);
	}

	@Override
	public boolean setBlock(BlockPos pos, BlockState arg1, int arg2) {
		return super.setBlock(pos, arg1, arg2);
	}

	public void addParticle(Particle p) {
		if (p != null)
			particles.addParticle(p);
	}

	@Override
	protected void onTEadded(TileEntity tileEntity, BlockPos pos) {
		super.onTEadded(tileEntity, pos);
		if (!(tileEntity instanceof SmartTileEntity))
			return;
		SmartTileEntity smartTileEntity = (SmartTileEntity) tileEntity;
		smartTileEntity.markVirtual();
	}

	public void fixControllerTileEntities() {
		for (TileEntity tileEntity : tileEntities.values()) {
			if (tileEntity instanceof BeltTileEntity) {
				BeltTileEntity beltTileEntity = (BeltTileEntity) tileEntity;
				if (!beltTileEntity.isController())
					continue;
				BlockPos controllerPos = tileEntity.getBlockPos();
				for (BlockPos blockPos : BeltBlock.getBeltChain(this, controllerPos)) {
					TileEntity tileEntity2 = getBlockEntity(blockPos);
					if (!(tileEntity2 instanceof BeltTileEntity))
						continue;
					BeltTileEntity belt2 = (BeltTileEntity) tileEntity2;
					belt2.setController(controllerPos);
				}
			}
			if (tileEntity instanceof FluidTankTileEntity) {
				FluidTankTileEntity fluidTankTileEntity = (FluidTankTileEntity) tileEntity;
				BlockPos lastKnown = fluidTankTileEntity.getLastKnownPos();
				BlockPos current = fluidTankTileEntity.getBlockPos();
				if (lastKnown == null || current == null)
					continue;
				if (fluidTankTileEntity.isController())
					continue;
				if (!lastKnown.equals(current)) {
					BlockPos newControllerPos = fluidTankTileEntity.getController()
						.offset(current.subtract(lastKnown));
					fluidTankTileEntity.setController(newControllerPos);
				}
			}
		}
	}

	public void setBlockBreakingProgress(BlockPos pos, int damage) {
		if (damage == 0)
			blockBreakingProgressions.remove(pos);
		else
			blockBreakingProgressions.put(pos, damage - 1);
	}

	public Map<BlockPos, Integer> getBlockBreakingProgressions() {
		return blockBreakingProgressions;
	}

	public void addBlockDestroyEffects(BlockPos pos, BlockState state) {
		VoxelShape voxelshape = state.getShape(this, pos);
		if (voxelshape.isEmpty())
			return;

		AxisAlignedBB bb = voxelshape.bounds();
		double d1 = Math.min(1.0D, bb.maxX - bb.minX);
		double d2 = Math.min(1.0D, bb.maxY - bb.minY);
		double d3 = Math.min(1.0D, bb.maxZ - bb.minZ);
		int i = Math.max(2, MathHelper.ceil(d1 / 0.25D));
		int j = Math.max(2, MathHelper.ceil(d2 / 0.25D));
		int k = Math.max(2, MathHelper.ceil(d3 / 0.25D));

		for (int l = 0; l < i; ++l) {
			for (int i1 = 0; i1 < j; ++i1) {
				for (int j1 = 0; j1 < k; ++j1) {
					double d4 = (l + 0.5D) / i;
					double d5 = (i1 + 0.5D) / j;
					double d6 = (j1 + 0.5D) / k;
					double d7 = d4 * d1 + bb.minX;
					double d8 = d5 * d2 + bb.minY;
					double d9 = d6 * d3 + bb.minZ;
					addParticle(new BlockParticleData(ParticleTypes.BLOCK, state), pos.getX() + d7, pos.getY() + d8,
						pos.getZ() + d9, d4 - 0.5D, d5 - 0.5D, d6 - 0.5D);
				}
			}
		}
	}

	@Override
	protected BlockState processBlockStateForPrinting(BlockState state) {
		return state;
	}

	@Override
	public boolean hasChunkAt(BlockPos pos) {
		return true; // fix particle lighting
	}

	@Override
	public boolean hasChunk(int x, int y) {
		return true; // fix particle lighting
	}

	@Override
	public boolean isLoaded(BlockPos pos) {
		return true; // fix particle lighting
	}

	@Override
	public boolean hasNearbyAlivePlayer(double p_217358_1_, double p_217358_3_, double p_217358_5_, double p_217358_7_) {
		return true; // always enable spawner animations
	}
}
