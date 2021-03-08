package com.simibubi.create.foundation.ponder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.foundation.renderState.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;

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
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class PonderWorld extends SchematicWorld {

	protected Map<BlockPos, BlockState> originalBlocks;
	protected Map<BlockPos, TileEntity> originalTileEntities;
	protected List<Entity> originalEntities;

	protected PonderWorldParticles particles;
	private final Map<ResourceLocation, IParticleFactory<?>> particleFactories;

	int overrideLight;
	Selection mask;

	public PonderWorld(BlockPos anchor, World original) {
		super(anchor, original);
		originalBlocks = new HashMap<>();
		originalTileEntities = new HashMap<>();
		originalEntities = new ArrayList<>();
		particles = new PonderWorldParticles(this);

		// ParticleManager.factories - ATs don't seem to like this one
		particleFactories = ObfuscationReflectionHelper.getPrivateValue(ParticleManager.class,
			Minecraft.getInstance().particles, "field_178932_g");
	}

	public void createBackup() {
		originalBlocks.clear();
		originalTileEntities.clear();
		blocks.forEach((k, v) -> originalBlocks.put(k, v));
		tileEntities.forEach((k, v) -> originalTileEntities.put(k, TileEntity.create(v.write(new CompoundNBT()))));
		entities.forEach(e -> EntityType.loadEntityUnchecked(e.serializeNBT(), this)
			.ifPresent(originalEntities::add));
	}

	public void restore() {
		entities.clear();
		blocks.clear();
		tileEntities.clear();
		renderedTileEntities.clear();
		originalBlocks.forEach((k, v) -> blocks.put(k, v));
		originalTileEntities.forEach((k, v) -> {
			TileEntity te = TileEntity.create(v.write(new CompoundNBT()));
			te.setLocation(this, te.getPos());
			tileEntities.put(k, te);
			renderedTileEntities.add(te);
		});
		originalEntities.forEach(e -> EntityType.loadEntityUnchecked(e.serializeNBT(), this)
			.ifPresent(entities::add));
		particles.clearEffects();
		fixVirtualTileEntities();
	}

	public void pushFakeLight(int light) {
		this.overrideLight = light;
	}

	public void popLight() {
		this.overrideLight = -1;
	}

	@Override
	public int getLightLevel(LightType p_226658_1_, BlockPos p_226658_2_) {
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
			return Blocks.AIR.getDefaultState();
		return super.getBlockState(globalPos);
	}

	@Override // For particle collision
	public IBlockReader getExistingChunk(int p_225522_1_, int p_225522_2_) {
		return this;
	}

	public void renderEntities(MatrixStack ms, SuperRenderTypeBuffer buffer, ActiveRenderInfo ari, float pt) {
		Vec3d vec3d = ari.getProjectedView();
		double d0 = vec3d.getX();
		double d1 = vec3d.getY();
		double d2 = vec3d.getZ();

		for (Entity entity : entities) {
			if (entity.ticksExisted == 0) {
				entity.lastTickPosX = entity.getX();
				entity.lastTickPosY = entity.getY();
				entity.lastTickPosZ = entity.getZ();
			}
			renderEntity(entity, d0, d1, d2, pt, ms, buffer);
		}

		buffer.draw(RenderType.getEntitySolid(PlayerContainer.BLOCK_ATLAS_TEXTURE));
		buffer.draw(RenderType.getEntityCutout(PlayerContainer.BLOCK_ATLAS_TEXTURE));
		buffer.draw(RenderType.getEntityCutoutNoCull(PlayerContainer.BLOCK_ATLAS_TEXTURE));
		buffer.draw(RenderType.getEntitySmoothCutout(PlayerContainer.BLOCK_ATLAS_TEXTURE));
	}

	private void renderEntity(Entity entity, double x, double y, double z, float pt, MatrixStack ms,
		IRenderTypeBuffer buffer) {
		double d0 = MathHelper.lerp((double) pt, entity.lastTickPosX, entity.getX());
		double d1 = MathHelper.lerp((double) pt, entity.lastTickPosY, entity.getY());
		double d2 = MathHelper.lerp((double) pt, entity.lastTickPosZ, entity.getZ());
		float f = MathHelper.lerp(pt, entity.prevRotationYaw, entity.rotationYaw);
		EntityRendererManager renderManager = Minecraft.getInstance()
			.getRenderManager();
		int light = renderManager.getRenderer(entity)
			.getLight(entity, pt);
		renderManager.render(entity, d0 - x, d1 - y, d2 - z, f, pt, ms, buffer, light);
	}

	public void renderParticles(MatrixStack ms, IRenderTypeBuffer buffer, ActiveRenderInfo ari, float pt) {
		particles.renderParticles(ms, buffer, ari, pt);
	}

	public void tick() {
		particles.tick();

		for (Iterator<Entity> iterator = entities.iterator(); iterator.hasNext();) {
			Entity entity = iterator.next();

			entity.ticksExisted++;
			entity.lastTickPosX = entity.getX();
			entity.lastTickPosY = entity.getY();
			entity.lastTickPosZ = entity.getZ();
			entity.tick();

			if (!entity.isAlive())
				iterator.remove();
		}
	}

	@Override
	public void addParticle(IParticleData data, double x, double y, double z, double mx, double my, double mz) {
		addParticle(makeParticle(data, x, y, z, mx, my, mz));
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <T extends IParticleData> Particle makeParticle(T data, double x, double y, double z, double mx, double my,
		double mz) {
		ResourceLocation key = ForgeRegistries.PARTICLE_TYPES.getKey(data.getType());
		IParticleFactory<T> iparticlefactory = (IParticleFactory<T>) particleFactories.get(key);
		return iparticlefactory == null ? null : iparticlefactory.makeParticle(data, this, x, y, z, mx, my, mz);
	}

	public void addParticle(Particle p) {
		if (p != null)
			particles.addParticle(p);
	}

	public void fixVirtualTileEntities() {
		for (TileEntity tileEntity : tileEntities.values()) {
			if (!(tileEntity instanceof SmartTileEntity))
				continue;
			SmartTileEntity smartTileEntity = (SmartTileEntity) tileEntity;
			smartTileEntity.markVirtual();

			if (!(smartTileEntity instanceof BeltTileEntity))
				continue;
			BeltTileEntity beltTileEntity = (BeltTileEntity) smartTileEntity;
			if (!beltTileEntity.isController())
				continue;
			BlockPos controllerPos = tileEntity.getPos();
			for (BlockPos blockPos : BeltBlock.getBeltChain(this, controllerPos)) {
				TileEntity tileEntity2 = getTileEntity(blockPos);
				if (!(tileEntity2 instanceof BeltTileEntity))
					continue;
				BeltTileEntity belt2 = (BeltTileEntity) tileEntity2;
				belt2.setController(controllerPos);
			}
		}
	}

	public void addBlockDestroyEffects(BlockPos pos, BlockState state) {
		VoxelShape voxelshape = state.getShape(this, pos);
		if (voxelshape.isEmpty())
			return;

		AxisAlignedBB bb = voxelshape.getBoundingBox();
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

}
