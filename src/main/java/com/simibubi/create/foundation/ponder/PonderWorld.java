package com.simibubi.create.foundation.ponder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.contraptions.relays.belt.BeltBlock;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.schematics.SchematicWorld;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.render.SuperRenderTypeBuffer;
import com.simibubi.create.foundation.tileEntity.IMultiTileContainer;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.utility.worldWrappers.WrappedClientWorld;
import io.github.fabricators_of_create.porting_lib.mixin.client.accessor.ParticleEngineAccessor;
import io.github.fabricators_of_create.porting_lib.util.NBTSerializer;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PonderWorld extends SchematicWorld {

	public PonderScene scene;

	protected Map<BlockPos, BlockState> originalBlocks;
	protected Map<BlockPos, BlockEntity> originalTileEntities;
	protected Map<BlockPos, Integer> blockBreakingProgressions;
	protected List<Entity> originalEntities;
	private Supplier<ClientLevel> asClientWorld = Suppliers.memoize(() -> WrappedClientWorld.of(this));

	protected PonderWorldParticles particles;
//	private final Map<ResourceLocation, ParticleProvider<?>> particleFactories;
	private final Int2ObjectMap<ParticleProvider<?>> particleFactories;

	int overrideLight;
	Selection mask;

	public PonderWorld(BlockPos anchor, Level original) {
		super(anchor, original);
		originalBlocks = new HashMap<>();
		originalTileEntities = new HashMap<>();
		blockBreakingProgressions = new HashMap<>();
		originalEntities = new ArrayList<>();
		particles = new PonderWorldParticles(this);

		// ParticleManager.factories - ATs don't seem to like this one
		particleFactories = ((ParticleEngineAccessor) Minecraft.getInstance().particleEngine).port_lib$getProviders();
//		particleFactories = ObfuscationReflectionHelper.getPrivateValue(ParticleEngine.class,
//			Minecraft.getInstance().particleEngine, "f_107293_"); // providers
	}

	public void createBackup() {
		originalBlocks.clear();
		originalTileEntities.clear();
		blocks.forEach((k, v) -> originalBlocks.put(k, v));
		tileEntities.forEach(
			(k, v) -> originalTileEntities.put(k, BlockEntity.loadStatic(k, blocks.get(k), v.saveWithFullMetadata())));
		entities.forEach(e -> EntityType.create(NBTSerializer.serializeNBT(e), this)
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
			BlockEntity te = BlockEntity.loadStatic(k, originalBlocks.get(k), v.saveWithFullMetadata());
			onTEadded(te, te.getBlockPos());
			tileEntities.put(k, te);
			renderedTileEntities.add(te);
		});
		originalEntities.forEach(e -> EntityType.create(NBTSerializer.serializeNBT(e), this)
			.ifPresent(entities::add));
		particles.clearEffects();
		fixControllerTileEntities();
	}

	public void restoreBlocks(Selection selection) {
		selection.forEach(p -> {
			if (originalBlocks.containsKey(p))
				blocks.put(p, originalBlocks.get(p));
			if (originalTileEntities.containsKey(p)) {
				BlockEntity te = BlockEntity.loadStatic(p, originalBlocks.get(p), originalTileEntities.get(p)
					.saveWithFullMetadata());
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
	public int getBrightness(LightLayer p_226658_1_, BlockPos p_226658_2_) {
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
	public BlockGetter getChunkForCollisions(int p_225522_1_, int p_225522_2_) {
		return this;
	}

	public void renderEntities(PoseStack ms, SuperRenderTypeBuffer buffer, Camera ari, float pt) {
		Vec3 Vector3d = ari.getPosition();
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

		buffer.draw(RenderType.entitySolid(InventoryMenu.BLOCK_ATLAS));
		buffer.draw(RenderType.entityCutout(InventoryMenu.BLOCK_ATLAS));
		buffer.draw(RenderType.entityCutoutNoCull(InventoryMenu.BLOCK_ATLAS));
		buffer.draw(RenderType.entitySmoothCutout(InventoryMenu.BLOCK_ATLAS));
	}

	private void renderEntity(Entity entity, double x, double y, double z, float pt, PoseStack ms,
		MultiBufferSource buffer) {
		double d0 = Mth.lerp((double) pt, entity.xOld, entity.getX());
		double d1 = Mth.lerp((double) pt, entity.yOld, entity.getY());
		double d2 = Mth.lerp((double) pt, entity.zOld, entity.getZ());
		float f = Mth.lerp(pt, entity.yRotO, entity.getYRot());
		EntityRenderDispatcher renderManager = Minecraft.getInstance()
			.getEntityRenderDispatcher();
		int light = renderManager.getRenderer(entity)
			.getPackedLightCoords(entity, pt);
		renderManager.render(entity, d0 - x, d1 - y, d2 - z, f, pt, ms, buffer, light);
	}

	public void renderParticles(PoseStack ms, MultiBufferSource buffer, Camera ari, float pt) {
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
				entity.discard();

			if (!entity.isAlive())
				iterator.remove();
		}
	}

	@Override
	public void addParticle(ParticleOptions data, double x, double y, double z, double mx, double my, double mz) {
		addParticle(makeParticle(data, x, y, z, mx, my, mz));
	}

	@Override
	public void addAlwaysVisibleParticle(ParticleOptions data, double x, double y, double z, double mx, double my, double mz) {
		addParticle(data, x, y, z, mx, my, mz);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	private <T extends ParticleOptions> Particle makeParticle(T data, double x, double y, double z, double mx, double my,
		double mz) {
		int key = Registry.PARTICLE_TYPE.getId(data.getType());
		ParticleProvider<T> iparticlefactory = (ParticleProvider<T>) particleFactories.get(key);
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
	protected void onTEadded(BlockEntity tileEntity, BlockPos pos) {
		super.onTEadded(tileEntity, pos);
		if (!(tileEntity instanceof SmartTileEntity))
			return;
		SmartTileEntity smartTileEntity = (SmartTileEntity) tileEntity;
		smartTileEntity.markVirtual();
	}

	public void fixControllerTileEntities() {
		for (BlockEntity tileEntity : tileEntities.values()) {

			if (tileEntity instanceof BeltTileEntity) {
				BeltTileEntity beltTileEntity = (BeltTileEntity) tileEntity;
				if (!beltTileEntity.isController())
					continue;
				BlockPos controllerPos = tileEntity.getBlockPos();
				for (BlockPos blockPos : BeltBlock.getBeltChain(this, controllerPos)) {
					BlockEntity tileEntity2 = getBlockEntity(blockPos);
					if (!(tileEntity2 instanceof BeltTileEntity))
						continue;
					BeltTileEntity belt2 = (BeltTileEntity) tileEntity2;
					belt2.setController(controllerPos);
				}
			}

			if (tileEntity instanceof IMultiTileContainer) {
				IMultiTileContainer multiTile = (IMultiTileContainer) tileEntity;
				BlockPos lastKnown = multiTile.getLastKnownPos();
				BlockPos current = tileEntity.getBlockPos();
				if (lastKnown == null || current == null)
					continue;
				if (multiTile.isController())
					continue;
				if (!lastKnown.equals(current)) {
					BlockPos newControllerPos = multiTile.getController()
						.offset(current.subtract(lastKnown));
					multiTile.setController(newControllerPos);
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

		AABB bb = voxelshape.bounds();
		double d1 = Math.min(1.0D, bb.maxX - bb.minX);
		double d2 = Math.min(1.0D, bb.maxY - bb.minY);
		double d3 = Math.min(1.0D, bb.maxZ - bb.minZ);
		int i = Math.max(2, Mth.ceil(d1 / 0.25D));
		int j = Math.max(2, Mth.ceil(d2 / 0.25D));
		int k = Math.max(2, Mth.ceil(d3 / 0.25D));

		for (int l = 0; l < i; ++l) {
			for (int i1 = 0; i1 < j; ++i1) {
				for (int j1 = 0; j1 < k; ++j1) {
					double d4 = (l + 0.5D) / i;
					double d5 = (i1 + 0.5D) / j;
					double d6 = (j1 + 0.5D) / k;
					double d7 = d4 * d1 + bb.minX;
					double d8 = d5 * d2 + bb.minY;
					double d9 = d6 * d3 + bb.minZ;
					addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + d7, pos.getY() + d8,
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

	// In case another mod (read: Lithium) has overwritten noCollision and would break PonderWorlds, force vanilla behavior in PonderWorlds
	@Override
	public boolean noCollision(@Nullable Entity entity, AABB collisionBox) {
		// Vanilla copy
		Iterator var3 = this.getBlockCollisions(entity, collisionBox).iterator();

		while(var3.hasNext()) {
			VoxelShape voxelShape = (VoxelShape)var3.next();
			if (!voxelShape.isEmpty()) {
				return false;
			}
		}

		if (!this.getEntityCollisions(entity, collisionBox).isEmpty()) {
			return false;
		} else if (entity == null) {
			return true;
		} else {
			VoxelShape voxelShape2 = this.borderCollision(entity, collisionBox);
			return voxelShape2 == null || !Shapes.joinIsNotEmpty(voxelShape2, Shapes.create(collisionBox), BooleanOp.AND);
		}
	}

	VoxelShape borderCollision(Entity entity, AABB aABB) {
		WorldBorder worldBorder = this.getWorldBorder();
		return worldBorder.isInsideCloseToBorder(entity, aABB) ? worldBorder.getCollisionShape() : null;
	}
}
