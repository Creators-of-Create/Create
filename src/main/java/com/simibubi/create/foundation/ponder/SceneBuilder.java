package com.simibubi.create.foundation.ponder;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.components.structureMovement.glue.SuperGlueItem;
import com.simibubi.create.content.contraptions.particle.RotationIndicatorParticleData;
import com.simibubi.create.content.contraptions.relays.belt.BeltTileEntity;
import com.simibubi.create.content.contraptions.relays.gauge.SpeedGaugeTileEntity;
import com.simibubi.create.content.logistics.block.funnel.FunnelTileEntity;
import com.simibubi.create.foundation.ponder.content.PonderPalette;
import com.simibubi.create.foundation.ponder.elements.BeltItemElement;
import com.simibubi.create.foundation.ponder.elements.EntityElement;
import com.simibubi.create.foundation.ponder.elements.InputWindowElement;
import com.simibubi.create.foundation.ponder.elements.ParrotElement;
import com.simibubi.create.foundation.ponder.elements.ParrotElement.ParrotPose;
import com.simibubi.create.foundation.ponder.elements.ParrotElement.SpinOnComponentPose;
import com.simibubi.create.foundation.ponder.elements.TextWindowElement;
import com.simibubi.create.foundation.ponder.elements.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instructions.AnimateParrotInstruction;
import com.simibubi.create.foundation.ponder.instructions.AnimateTileEntityInstruction;
import com.simibubi.create.foundation.ponder.instructions.AnimateWorldSectionInstruction;
import com.simibubi.create.foundation.ponder.instructions.ChaseAABBInstruction;
import com.simibubi.create.foundation.ponder.instructions.CreateParrotInstruction;
import com.simibubi.create.foundation.ponder.instructions.DelayInstruction;
import com.simibubi.create.foundation.ponder.instructions.DisplayWorldSectionInstruction;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction;
import com.simibubi.create.foundation.ponder.instructions.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.ponder.instructions.FadeOutOfSceneInstruction;
import com.simibubi.create.foundation.ponder.instructions.LineInstruction;
import com.simibubi.create.foundation.ponder.instructions.MarkAsFinishedInstruction;
import com.simibubi.create.foundation.ponder.instructions.MovePoiInstruction;
import com.simibubi.create.foundation.ponder.instructions.OutlineSelectionInstruction;
import com.simibubi.create.foundation.ponder.instructions.ReplaceBlocksInstruction;
import com.simibubi.create.foundation.ponder.instructions.RotateSceneInstruction;
import com.simibubi.create.foundation.ponder.instructions.ShowInputInstruction;
import com.simibubi.create.foundation.ponder.instructions.TextInstruction;
import com.simibubi.create.foundation.ponder.instructions.TileEntityDataInstruction;
import com.simibubi.create.foundation.tileEntity.SmartTileEntity;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.foundation.tileEntity.behaviour.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.foundation.utility.ColorHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/**
 * Enqueue instructions to the schedule via this object's methods.
 */
public class SceneBuilder {

	/**
	 * Ponder's toolkit for showing information on top of the scene world, such as
	 * highlighted bounding boxes, texts, icons and keybindings.
	 */
	public final OverlayInstructions overlay;

	/**
	 * Instructions for manipulating the schematic and its currently visible areas.
	 * Allows to show, hide and modify blocks as the scene plays out.
	 */
	public final WorldInstructions world;

	/**
	 * Additional tools for debugging ponder and bypassing the facade
	 */
	public final DebugInstructions debug;

	/**
	 * Special effects to embellish and communicate with
	 */
	public final EffectInstructions effects;

	/**
	 * Random other instructions that might come in handy
	 */
	public final SpecialInstructions special;

	private final PonderScene scene;

	public SceneBuilder(PonderScene ponderScene) {
		scene = ponderScene;
		overlay = new OverlayInstructions();
		special = new SpecialInstructions();
		world = new WorldInstructions();
		debug = new DebugInstructions();
		effects = new EffectInstructions();
	}

	// General

	/**
	 * Assign a unique translation key, as well as the standard english translation
	 * for this scene's title using this method, anywhere inside the program
	 * function.
	 * 
	 * @param sceneId
	 * @param title
	 */
	public void title(String sceneId, String title) {
		scene.sceneId = sceneId;
		PonderLocalization.registerSpecific(sceneId, PonderScene.TITLE_KEY, title);
	}

	/**
	 * Communicates to the ponder UI which parts of the schematic make up the base
	 * horizontally. Use of this is encouraged whenever there are components outside
	 * the the base plate. <br>
	 * As a result, showBasePlate() will only show the configured size, and the
	 * scene's scaling inside the UI will be consistent with its base size.
	 * 
	 * @param xOffset       Block spaces between the base plate and the schematic
	 *                      boundary on the Western side.
	 * @param zOffset       Block spaces between the base plate and the schematic
	 *                      boundary on the Northern side.
	 * @param basePlateSize Length in blocks of the base plate itself. Ponder
	 *                      assumes it to be square
	 */
	public void configureBasePlate(int xOffset, int zOffset, int basePlateSize) {
		scene.offsetX = xOffset;
		scene.offsetZ = zOffset;
		scene.size = basePlateSize;
	}

	/**
	 * Fade the layer of blocks into the scene ponder assumes to be the base plate
	 * of the schematic's structure. Makes for a nice opener
	 */
	public void showBasePlate() {
		world.showSection(scene.getSceneBuildingUtil().select.cuboid(new BlockPos(scene.offsetX, 0, scene.offsetZ),
			new Vec3i(scene.size, 0, scene.size)), Direction.UP);
	}

	/**
	 * Before running the upcoming instructions, wait for a duration to let previous
	 * actions play out. <br>
	 * Idle does not stall any animations, only schedules a time gap between
	 * instructions.
	 * 
	 * @param ticks Duration to wait for
	 */
	public void idle(int ticks) {
		addInstruction(new DelayInstruction(ticks));
	}

	/**
	 * Before running the upcoming instructions, wait for a duration to let previous
	 * actions play out. <br>
	 * Idle does not stall any animations, only schedules a time gap between
	 * instructions.
	 * 
	 * @param seconds Duration to wait for
	 */
	public void idleSeconds(int seconds) {
		idle(seconds * 20);
	}

	/**
	 * Once the scene reaches this instruction in the timeline, mark it as
	 * "finished". This happens automatically when the end of a storyboard is
	 * reached, but can be desirable to do earlier, in order to bypass the wait for
	 * any residual text windows to time out. <br>
	 * So far this event only affects the "next scene" button in the UI to flash.
	 */
	public void markAsFinished() {
		addInstruction(new MarkAsFinishedInstruction());
	}

	/**
	 * Pans the scene's camera view around the vertical axis by the given amount
	 * 
	 * @param degrees
	 */
	public void rotateCameraY(float degrees) {
		addInstruction(new RotateSceneInstruction(0, degrees, true));
	}

	public class EffectInstructions {

		public void emitParticles(Vec3d location, Emitter emitter, float amountPerCycle, int cycles) {
			addInstruction(new EmitParticlesInstruction(location, emitter, amountPerCycle, cycles));
		}

		public void superGlue(BlockPos pos, Direction side, boolean fullBlock) {
			addInstruction(scene -> SuperGlueItem.spawnParticles(scene.world, pos, side, fullBlock));
		}

		private void rotationIndicator(BlockPos pos, boolean direction) {
			addInstruction(scene -> {
				BlockState blockState = scene.world.getBlockState(pos);
				TileEntity tileEntity = scene.world.getTileEntity(pos);

				if (!(blockState.getBlock() instanceof KineticBlock))
					return;
				if (!(tileEntity instanceof KineticTileEntity))
					return;

				KineticTileEntity kte = (KineticTileEntity) tileEntity;
				KineticBlock kb = (KineticBlock) blockState.getBlock();
				Axis rotationAxis = kb.getRotationAxis(blockState);

				float speed = kte.getTheoreticalSpeed();
				SpeedLevel speedLevel = SpeedLevel.of(speed);
				int color = direction ? speed > 0 ? 0xeb5e0b : 0x1687a7 : speedLevel.getColor();
				int particleSpeed = speedLevel.getParticleSpeed();
				particleSpeed *= Math.signum(speed);

				Vec3d location = VecHelper.getCenterOf(pos);
				RotationIndicatorParticleData particleData = new RotationIndicatorParticleData(color, particleSpeed,
					kb.getParticleInitialRadius(), kb.getParticleTargetRadius(), 20, rotationAxis.name()
						.charAt(0));

				for (int i = 0; i < 20; i++)
					scene.world.addParticle(particleData, location.x, location.y, location.z, 0, 0, 0);
			});
		}

		public void rotationSpeedIndicator(BlockPos pos) {
			rotationIndicator(pos, false);
		}

		public void rotationDirectionIndicator(BlockPos pos) {
			rotationIndicator(pos, true);
		}

		public void indicateRedstone(BlockPos pos) {
			createRedstoneParticles(pos, 0xFF0000, 10);
		}

		public void indicateSuccess(BlockPos pos) {
			createRedstoneParticles(pos, 0x80FFaa, 10);
		}

		public void createRedstoneParticles(BlockPos pos, int color, int amount) {
			Vec3d rgb = ColorHelper.getRGB(color);
			addInstruction(new EmitParticlesInstruction(VecHelper.getCenterOf(pos), Emitter.withinBlockSpace(
				new RedstoneParticleData((float) rgb.x, (float) rgb.y, (float) rgb.z, 1), Vec3d.ZERO), amount, 2));
		}

	}

	public class OverlayInstructions {

		public TextWindowElement.Builder showText(int duration) {
			TextWindowElement textWindowElement = new TextWindowElement();
			addInstruction(new TextInstruction(textWindowElement, duration));
			return textWindowElement.new Builder(scene);
		}

		public TextWindowElement.Builder showSelectionWithText(Selection selection, int duration) {
			TextWindowElement textWindowElement = new TextWindowElement();
			addInstruction(new TextInstruction(textWindowElement, duration, selection));
			return textWindowElement.new Builder(scene).pointAt(selection.getCenter());
		}

		public void showControls(InputWindowElement element, int duration) {
			addInstruction(new ShowInputInstruction(element.clone(), duration));
		}

		public void chaseBoundingBoxOutline(PonderPalette color, Object slot, AxisAlignedBB boundingBox, int duration) {
			addInstruction(new ChaseAABBInstruction(color, slot, boundingBox, duration));
		}

		public void showLine(PonderPalette color, Vec3d start, Vec3d end, int duration) {
			addInstruction(new LineInstruction(color, start, end, duration));
		}

		public void showOutline(PonderPalette color, Object slot, Selection selection, int duration) {
			addInstruction(new OutlineSelectionInstruction(color, slot, selection, duration));
		}

	}

	public class SpecialInstructions {

		public ElementLink<ParrotElement> birbOnTurntable(BlockPos pos) {
			return createBirb(VecHelper.getCenterOf(pos), () -> new SpinOnComponentPose(pos));
		}

		public ElementLink<ParrotElement> birbOnSpinnyShaft(BlockPos pos) {
			return createBirb(VecHelper.getCenterOf(pos)
				.add(0, 0.5, 0), () -> new SpinOnComponentPose(pos));
		}

		public ElementLink<ParrotElement> createBirb(Vec3d location, Supplier<? extends ParrotPose> pose) {
			ElementLink<ParrotElement> link = new ElementLink<>(ParrotElement.class);
			ParrotElement parrot = ParrotElement.create(location, pose);
			addInstruction(new CreateParrotInstruction(10, Direction.DOWN, parrot));
			addInstruction(scene -> scene.linkElement(parrot, link));
			return link;
		}

		public void changeBirbPose(ElementLink<ParrotElement> birb, Supplier<? extends ParrotPose> pose) {
			addInstruction(scene -> scene.resolve(birb)
				.setPose(pose.get()));
		}

		public void movePointOfInterest(Vec3d location) {
			addInstruction(new MovePoiInstruction(location));
		}

		public void movePointOfInterest(BlockPos location) {
			movePointOfInterest(VecHelper.getCenterOf(location));
		}

		public void rotateParrot(ElementLink<ParrotElement> link, double xRotation, double yRotation, double zRotation,
			int duration) {
			addInstruction(AnimateParrotInstruction.rotate(link, new Vec3d(xRotation, yRotation, zRotation), duration));
		}

		public void moveParrot(ElementLink<ParrotElement> link, Vec3d offset, int duration) {
			addInstruction(AnimateParrotInstruction.move(link, offset, duration));
		}

	}

	public class WorldInstructions {

		public void showSection(Selection selection, Direction fadeInDirection) {
			addInstruction(new DisplayWorldSectionInstruction(15, fadeInDirection, selection,
				Optional.of(scene::getBaseWorldSection)));
		}

		public void showSectionAndMerge(Selection selection, Direction fadeInDirection,
			ElementLink<WorldSectionElement> link) {
			addInstruction(new DisplayWorldSectionInstruction(15, fadeInDirection, selection,
				Optional.of(() -> scene.resolve(link))));
		}

		public ElementLink<WorldSectionElement> showIndependentSection(Selection selection, Direction fadeInDirection) {
			DisplayWorldSectionInstruction instruction =
				new DisplayWorldSectionInstruction(15, fadeInDirection, selection, Optional.empty());
			addInstruction(instruction);
			return instruction.createLink(scene);
		}

		public void hideSection(Selection selection, Direction fadeOutDirection) {
			WorldSectionElement worldSectionElement = new WorldSectionElement(selection);
			ElementLink<WorldSectionElement> elementLink = new ElementLink<>(WorldSectionElement.class);

			addInstruction(scene -> {
				scene.getBaseWorldSection()
					.erase(selection);
				scene.linkElement(worldSectionElement, elementLink);
				scene.addElement(worldSectionElement);
				worldSectionElement.queueRedraw();
			});

			hideIndependentSection(elementLink, fadeOutDirection);
		}

		public void hideIndependentSection(ElementLink<WorldSectionElement> link, Direction fadeOutDirection) {
			addInstruction(new FadeOutOfSceneInstruction<>(15, fadeOutDirection, link));
		}

		public ElementLink<WorldSectionElement> makeSectionIndependent(Selection selection) {
			WorldSectionElement worldSectionElement = new WorldSectionElement(selection);
			ElementLink<WorldSectionElement> elementLink = new ElementLink<>(WorldSectionElement.class);

			addInstruction(scene -> {
				scene.getBaseWorldSection()
					.erase(selection);
				scene.linkElement(worldSectionElement, elementLink);
				scene.addElement(worldSectionElement);
				worldSectionElement.queueRedraw();
				worldSectionElement.resetAnimatedTransform();
				worldSectionElement.setVisible(true);
				worldSectionElement.forceApplyFade(1);
			});

			return elementLink;
		}

		public void rotateSection(ElementLink<WorldSectionElement> link, double xRotation, double yRotation,
			double zRotation, int duration) {
			addInstruction(
				AnimateWorldSectionInstruction.rotate(link, new Vec3d(xRotation, yRotation, zRotation), duration));
		}

		public void configureCenterOfRotation(ElementLink<WorldSectionElement> link, Vec3d anchor) {
			addInstruction(scene -> scene.resolve(link)
				.setCenterOfRotation(anchor));
		}

		public void moveSection(ElementLink<WorldSectionElement> link, Vec3d offset, int duration) {
			addInstruction(AnimateWorldSectionInstruction.move(link, offset, duration));
		}

		public void rotateBearing(BlockPos pos, float angle, int duration) {
			addInstruction(AnimateTileEntityInstruction.bearing(pos, angle, duration));
		}

		public void movePulley(BlockPos pos, float distance, int duration) {
			addInstruction(AnimateTileEntityInstruction.pulley(pos, distance, duration));
		}

		public void setBlocks(Selection selection, BlockState state, boolean spawnParticles) {
			addInstruction(new ReplaceBlocksInstruction(selection, $ -> state, true, spawnParticles));
		}

		public void destroyBlock(BlockPos pos) {
			setBlock(pos, Blocks.AIR.getDefaultState(), true);
		}

		public void setBlock(BlockPos pos, BlockState state, boolean spawnParticles) {
			setBlocks(scene.getSceneBuildingUtil().select.position(pos), state, spawnParticles);
		}

		public void replaceBlocks(Selection selection, BlockState state, boolean spawnParticles) {
			modifyBlocks(selection, $ -> state, spawnParticles);
		}

		public void modifyBlock(BlockPos pos, UnaryOperator<BlockState> stateFunc, boolean spawnParticles) {
			modifyBlocks(scene.getSceneBuildingUtil().select.position(pos), stateFunc, spawnParticles);
		}

		public void modifyBlocks(Selection selection, UnaryOperator<BlockState> stateFunc, boolean spawnParticles) {
			addInstruction(new ReplaceBlocksInstruction(selection, stateFunc, false, spawnParticles));
		}

		public void toggleRedstonePower(Selection selection) {
			modifyBlocks(selection, s -> {
				if (s.has(BlockStateProperties.POWER_0_15))
					s = s.with(BlockStateProperties.POWER_0_15, s.get(BlockStateProperties.POWER_0_15) == 0 ? 15 : 0);
				if (s.has(BlockStateProperties.POWERED))
					s = s.cycle(BlockStateProperties.POWERED);
				return s;
			}, false);
		}

		public <T extends Entity> void modifyEntities(Class<T> entityClass, Consumer<T> entityCallBack) {
			addInstruction(scene -> scene.forEachWorldEntity(entityClass, entityCallBack));
		}

		public <T extends Entity> void modifyEntitiesInside(Class<T> entityClass, Selection area,
			Consumer<T> entityCallBack) {
			addInstruction(scene -> scene.forEachWorldEntity(entityClass, e -> {
				if (area.test(e.getPosition()))
					entityCallBack.accept(e);
			}));
		}

		public void modifyEntity(ElementLink<EntityElement> link, Consumer<Entity> entityCallBack) {
			addInstruction(scene -> {
				EntityElement resolve = scene.resolve(link);
				if (resolve != null)
					resolve.ifPresent(entityCallBack::accept);
			});
		}

		public ElementLink<EntityElement> createEntity(Function<World, Entity> factory) {
			ElementLink<EntityElement> link = new ElementLink<>(EntityElement.class, UUID.randomUUID());
			addInstruction(scene -> {
				PonderWorld world = scene.getWorld();
				Entity entity = factory.apply(world);
				EntityElement handle = new EntityElement(entity);
				scene.addElement(handle);
				scene.linkElement(handle, link);
				world.addEntity(entity);
			});
			return link;
		}

		public ElementLink<EntityElement> createItemEntity(Vec3d location, Vec3d motion, ItemStack stack) {
			return createEntity(world -> {
				ItemEntity itemEntity = new ItemEntity(world, location.x, location.y, location.z, stack);
				itemEntity.setMotion(motion);
				return itemEntity;
			});
		}

		public void createItemOnBeltLike(BlockPos location, Direction insertionSide, ItemStack stack) {
			addInstruction(scene -> {
				PonderWorld world = scene.getWorld();
				TileEntity tileEntity = world.getTileEntity(location);
				if (!(tileEntity instanceof SmartTileEntity))
					return;
				SmartTileEntity beltTileEntity = (SmartTileEntity) tileEntity;
				DirectBeltInputBehaviour behaviour = beltTileEntity.getBehaviour(DirectBeltInputBehaviour.TYPE);
				if (behaviour == null)
					return;
				behaviour.handleInsertion(stack, insertionSide.getOpposite(), false);
			});
			flapFunnels(scene.getSceneBuildingUtil().select.position(location.up()), true);
		}

		public ElementLink<BeltItemElement> createItemOnBelt(BlockPos beltLocation, Direction insertionSide,
			ItemStack stack) {
			ElementLink<BeltItemElement> link = new ElementLink<>(BeltItemElement.class);
			addInstruction(scene -> {
				PonderWorld world = scene.getWorld();
				TileEntity tileEntity = world.getTileEntity(beltLocation);
				if (!(tileEntity instanceof BeltTileEntity))
					return;

				BeltTileEntity beltTileEntity = (BeltTileEntity) tileEntity;
				DirectBeltInputBehaviour behaviour = beltTileEntity.getBehaviour(DirectBeltInputBehaviour.TYPE);
				behaviour.handleInsertion(stack, insertionSide.getOpposite(), false);

				BeltTileEntity controllerTE = beltTileEntity.getControllerTE();
				if (controllerTE != null)
					controllerTE.tick();

				TransportedItemStackHandlerBehaviour transporter =
					beltTileEntity.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
				transporter.handleProcessingOnAllItems(tis -> {
					BeltItemElement tracker = new BeltItemElement(tis);
					scene.addElement(tracker);
					scene.linkElement(tracker, link);
					return TransportedResult.doNothing();
				});

			});
			flapFunnels(scene.getSceneBuildingUtil().select.position(beltLocation.up()), true);
			return link;
		}

		public void removeItemsFromBelt(BlockPos beltLocation) {
			addInstruction(scene -> {
				PonderWorld world = scene.getWorld();
				TileEntity tileEntity = world.getTileEntity(beltLocation);
				if (!(tileEntity instanceof BeltTileEntity))
					return;
				BeltTileEntity beltTileEntity = (BeltTileEntity) tileEntity;
				TransportedItemStackHandlerBehaviour transporter =
					beltTileEntity.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
				transporter.handleProcessingOnAllItems(tis -> TransportedResult.removeItem());
			});
		}

		public void stallBeltItem(ElementLink<BeltItemElement> link, boolean stalled) {
			addInstruction(scene -> {
				BeltItemElement resolve = scene.resolve(link);
				if (resolve != null)
					resolve.ifPresent(tis -> tis.locked = stalled);
			});
		}

		public void changeBeltItemTo(ElementLink<BeltItemElement> link, ItemStack newStack) {
			addInstruction(scene -> {
				BeltItemElement resolve = scene.resolve(link);
				if (resolve != null)
					resolve.ifPresent(tis -> tis.stack = newStack);
			});
		}

		public void setKineticSpeed(Selection selection, float speed) {
			modifyKineticSpeed(selection, f -> speed);
		}

		public void multiplyKineticSpeed(Selection selection, float modifier) {
			modifyKineticSpeed(selection, f -> f * modifier);
		}

		public void modifyKineticSpeed(Selection selection, UnaryOperator<Float> speedFunc) {
			modifyTileNBT(selection, SpeedGaugeTileEntity.class, nbt -> {
				float newSpeed = speedFunc.apply(nbt.getFloat("Speed"));
				nbt.putFloat("Value", SpeedGaugeTileEntity.getDialTarget(newSpeed));
			});
			modifyTileNBT(selection, KineticTileEntity.class, nbt -> {
				nbt.putFloat("Speed", speedFunc.apply(nbt.getFloat("Speed")));
			});
		}

		public void setFilterData(Selection selection, Class<? extends TileEntity> teType, ItemStack filter) {
			modifyTileNBT(selection, teType, nbt -> {
				nbt.put("Filter", filter.serializeNBT());
			});
		}

		public void modifyTileNBT(Selection selection, Class<? extends TileEntity> teType,
			Consumer<CompoundNBT> consumer) {
			modifyTileNBT(selection, teType, consumer, false);
		}

		public void modifyTileNBT(Selection selection, Class<? extends TileEntity> teType,
			Consumer<CompoundNBT> consumer, boolean reDrawBlocks) {
			addInstruction(new TileEntityDataInstruction(selection, teType, nbt -> {
				consumer.accept(nbt);
				return nbt;
			}, reDrawBlocks));
		}

		public void flapFunnels(Selection selection, boolean outward) {
			addInstruction(new TileEntityDataInstruction(selection, FunnelTileEntity.class, nbt -> {
				nbt.putInt("Flap", outward ? -1 : 1);
				return nbt;
			}, false));
		}

	}

	public class DebugInstructions {

		public void debugSchematic() {
			addInstruction(
				scene -> scene.addElement(new WorldSectionElement(scene.getSceneBuildingUtil().select.everywhere())));
		}

		public void addInstructionInstance(PonderInstruction instruction) {
			addInstruction(instruction);
		}

		public void enqueueCallback(Consumer<PonderScene> callback) {
			addInstruction(callback);
		}

	}

	private void addInstruction(PonderInstruction instruction) {
		scene.schedule.add(instruction);
	}

	private void addInstruction(Consumer<PonderScene> callback) {
		scene.schedule.add(PonderInstruction.simple(callback));
	}

}