package com.simibubi.create.foundation.ponder;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.mojang.math.Vector3f;
import com.simibubi.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.simibubi.create.content.contraptions.glue.SuperGlueItem;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate.SpeedLevel;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotationIndicatorParticleData;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.simibubi.create.content.kinetics.crafter.ConnectedInputHandler;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.signal.SignalBlockEntity;
import com.simibubi.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.ponder.element.AnimatedSceneElement;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import com.simibubi.create.foundation.ponder.element.EntityElement;
import com.simibubi.create.foundation.ponder.element.InputWindowElement;
import com.simibubi.create.foundation.ponder.element.MinecartElement;
import com.simibubi.create.foundation.ponder.element.MinecartElement.MinecartConstructor;
import com.simibubi.create.foundation.ponder.element.ParrotElement;
import com.simibubi.create.foundation.ponder.element.ParrotElement.ParrotPose;
import com.simibubi.create.foundation.ponder.element.ParrotElement.SpinOnComponentPose;
import com.simibubi.create.foundation.ponder.element.TextWindowElement;
import com.simibubi.create.foundation.ponder.element.WorldSectionElement;
import com.simibubi.create.foundation.ponder.instruction.AnimateBlockEntityInstruction;
import com.simibubi.create.foundation.ponder.instruction.AnimateMinecartInstruction;
import com.simibubi.create.foundation.ponder.instruction.AnimateParrotInstruction;
import com.simibubi.create.foundation.ponder.instruction.AnimateWorldSectionInstruction;
import com.simibubi.create.foundation.ponder.instruction.BlockEntityDataInstruction;
import com.simibubi.create.foundation.ponder.instruction.ChaseAABBInstruction;
import com.simibubi.create.foundation.ponder.instruction.CreateMinecartInstruction;
import com.simibubi.create.foundation.ponder.instruction.CreateParrotInstruction;
import com.simibubi.create.foundation.ponder.instruction.DelayInstruction;
import com.simibubi.create.foundation.ponder.instruction.DisplayWorldSectionInstruction;
import com.simibubi.create.foundation.ponder.instruction.EmitParticlesInstruction;
import com.simibubi.create.foundation.ponder.instruction.EmitParticlesInstruction.Emitter;
import com.simibubi.create.foundation.ponder.instruction.FadeOutOfSceneInstruction;
import com.simibubi.create.foundation.ponder.instruction.HighlightValueBoxInstruction;
import com.simibubi.create.foundation.ponder.instruction.KeyframeInstruction;
import com.simibubi.create.foundation.ponder.instruction.LineInstruction;
import com.simibubi.create.foundation.ponder.instruction.MarkAsFinishedInstruction;
import com.simibubi.create.foundation.ponder.instruction.MovePoiInstruction;
import com.simibubi.create.foundation.ponder.instruction.OutlineSelectionInstruction;
import com.simibubi.create.foundation.ponder.instruction.PonderInstruction;
import com.simibubi.create.foundation.ponder.instruction.ReplaceBlocksInstruction;
import com.simibubi.create.foundation.ponder.instruction.RotateSceneInstruction;
import com.simibubi.create.foundation.ponder.instruction.ShowInputInstruction;
import com.simibubi.create.foundation.ponder.instruction.TextInstruction;
import com.simibubi.create.foundation.utility.Color;
import com.simibubi.create.foundation.utility.NBTHelper;
import com.simibubi.create.foundation.utility.VecHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

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
		scene.sceneId = new ResourceLocation(scene.getNamespace(), sceneId);
		PonderLocalization.registerSpecific(scene.sceneId, PonderScene.TITLE_KEY, title);
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
		scene.basePlateOffsetX = xOffset;
		scene.basePlateOffsetZ = zOffset;
		scene.basePlateSize = basePlateSize;
	}

	/**
	 * Use this in case you are not happy with the scale of the scene relative to
	 * the overlay
	 *
	 * @param factor {@literal >}1 will make the scene appear larger, smaller
	 *               otherwise
	 */
	public void scaleSceneView(float factor) {
		scene.scaleFactor = factor;
	}
	
	/**
	 * Use this to disable the base plates' shadow for this scene
	 */
	public void removeShadow() {
		scene.hidePlatformShadow = true;
	}

	/**
	 * Use this in case you are not happy with the vertical alignment of the scene
	 * relative to the overlay
	 *
	 * @param yOffset {@literal >}0 moves the scene up, down otherwise
	 */
	public void setSceneOffsetY(float yOffset) {
		scene.yOffset = yOffset;
	}

	/**
	 * Fade the layer of blocks into the scene ponder assumes to be the base plate
	 * of the schematic's structure. Makes for a nice opener
	 */
	public void showBasePlate() {
		world.showSection(scene.getSceneBuildingUtil().select.cuboid(
			new BlockPos(scene.getBasePlateOffsetX(), 0, scene.getBasePlateOffsetZ()),
			new Vec3i(scene.getBasePlateSize() - 1, 0, scene.getBasePlateSize() - 1)), Direction.UP);
	}

	/**
	 * Adds an instruction to the scene. It is recommended to only use this method
	 * if another method in this class or its subclasses does not already allow
	 * adding a certain instruction.
	 */
	public void addInstruction(PonderInstruction instruction) {
		scene.schedule.add(instruction);
	}

	/**
	 * Adds a simple instruction to the scene. It is recommended to only use this
	 * method if another method in this class or its subclasses does not already
	 * allow adding a certain instruction.
	 */
	public void addInstruction(Consumer<PonderScene> callback) {
		addInstruction(PonderInstruction.simple(callback));
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

	/**
	 * Adds a Key Frame at the end of the last delay() instruction for the users to
	 * skip to
	 */
	public void addKeyframe() {
		addInstruction(KeyframeInstruction.IMMEDIATE);
	}

	/**
	 * Adds a Key Frame a couple ticks after the last delay() instruction for the
	 * users to skip to
	 */
	public void addLazyKeyframe() {
		addInstruction(KeyframeInstruction.DELAYED);
	}

	public class EffectInstructions {

		public void emitParticles(Vec3 location, Emitter emitter, float amountPerCycle, int cycles) {
			addInstruction(new EmitParticlesInstruction(location, emitter, amountPerCycle, cycles));
		}

		public void superGlue(BlockPos pos, Direction side, boolean fullBlock) {
			addInstruction(scene -> SuperGlueItem.spawnParticles(scene.getWorld(), pos, side, fullBlock));
		}

				private void rotationIndicator(BlockPos pos, boolean direction, BlockPos displayPos) {
			addInstruction(scene -> {
				BlockState blockState = scene.getWorld()
					.getBlockState(pos);
				BlockEntity blockEntity = scene.getWorld()
					.getBlockEntity(pos);

				if (!(blockState.getBlock() instanceof KineticBlock))
					return;
				if (!(blockEntity instanceof KineticBlockEntity))
					return;

				KineticBlockEntity kbe = (KineticBlockEntity) blockEntity;
				KineticBlock kb = (KineticBlock) blockState.getBlock();
				Axis rotationAxis = kb.getRotationAxis(blockState);

				float speed = kbe.getTheoreticalSpeed();
				SpeedLevel speedLevel = SpeedLevel.of(speed);
				int color = direction ? speed > 0 ? 0xeb5e0b : 0x1687a7 : speedLevel.getColor();
				int particleSpeed = speedLevel.getParticleSpeed();
				particleSpeed *= Math.signum(speed);

				Vec3 location = VecHelper.getCenterOf(displayPos);
				RotationIndicatorParticleData particleData = new RotationIndicatorParticleData(color, particleSpeed,
					kb.getParticleInitialRadius(), kb.getParticleTargetRadius(), 20, rotationAxis.name()
						.charAt(0));

				for (int i = 0; i < 20; i++)
					scene.getWorld()
						.addParticle(particleData, location.x, location.y, location.z, 0, 0, 0);
			});
		}

		public void rotationSpeedIndicator(BlockPos pos) {
			rotationIndicator(pos, false, pos);
		}

		public void rotationDirectionIndicator(BlockPos pos) {
			rotationIndicator(pos, true, pos);
		}

		public void rotationSpeedIndicator(BlockPos pos, BlockPos displayPos) {
			rotationIndicator(pos, false, displayPos);
		}

		public void rotationDirectionIndicator(BlockPos pos, BlockPos displayPos) {
			rotationIndicator(pos, true, displayPos);
		}

		public void indicateRedstone(BlockPos pos) {
			createRedstoneParticles(pos, 0xFF0000, 10);
		}

		public void indicateSuccess(BlockPos pos) {
			createRedstoneParticles(pos, 0x80FFaa, 10);
		}

		public void createRedstoneParticles(BlockPos pos, int color, int amount) {
			Vector3f rgb = new Color(color).asVectorF();
			addInstruction(new EmitParticlesInstruction(VecHelper.getCenterOf(pos),
				Emitter.withinBlockSpace(new DustParticleOptions(rgb, 1), Vec3.ZERO), amount, 2));
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

		public void chaseBoundingBoxOutline(PonderPalette color, Object slot, AABB boundingBox, int duration) {
			addInstruction(new ChaseAABBInstruction(color, slot, boundingBox, duration));
		}

		public void showCenteredScrollInput(BlockPos pos, Direction side, int duration) {
			showFilterSlotInput(scene.getSceneBuildingUtil().vector.blockSurface(pos, side), side, duration);
		}

		public void showScrollInput(Vec3 location, Direction side, int duration) {
			Axis axis = side.getAxis();
			float s = 1 / 16f;
			float q = 1 / 4f;
			Vec3 expands = new Vec3(axis == Axis.X ? s : q, axis == Axis.Y ? s : q, axis == Axis.Z ? s : q);
			addInstruction(new HighlightValueBoxInstruction(location, expands, duration));
		}

		public void showRepeaterScrollInput(BlockPos pos, int duration) {
			showFilterSlotInput(scene.getSceneBuildingUtil().vector.blockSurface(pos, Direction.DOWN)
				.add(0, 3 / 16f, 0), Direction.UP, duration);
		}

		public void showFilterSlotInput(Vec3 location, Direction side, int duration) {
			location = location.add(Vec3.atLowerCornerOf(side.getNormal())
				.scale(-3 / 128f));
			Vec3 expands = VecHelper.axisAlingedPlaneOf(side)
				.scale(11 / 128f);
			addInstruction(new HighlightValueBoxInstruction(location, expands, duration));
		}

		public void showLine(PonderPalette color, Vec3 start, Vec3 end, int duration) {
			addInstruction(new LineInstruction(color, start, end, duration, false));
		}

		public void showBigLine(PonderPalette color, Vec3 start, Vec3 end, int duration) {
			addInstruction(new LineInstruction(color, start, end, duration, true));
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

		public ElementLink<ParrotElement> createBirb(Vec3 location, Supplier<? extends ParrotPose> pose) {
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

		public void conductorBirb(ElementLink<ParrotElement> birb, boolean conductor) {
			addInstruction(scene -> scene.resolve(birb)
				.setConductor(conductor));
		}

		public void movePointOfInterest(Vec3 location) {
			addInstruction(new MovePoiInstruction(location));
		}

		public void movePointOfInterest(BlockPos location) {
			movePointOfInterest(VecHelper.getCenterOf(location));
		}

		public void rotateParrot(ElementLink<ParrotElement> link, double xRotation, double yRotation, double zRotation,
			int duration) {
			addInstruction(AnimateParrotInstruction.rotate(link, new Vec3(xRotation, yRotation, zRotation), duration));
		}

		public void moveParrot(ElementLink<ParrotElement> link, Vec3 offset, int duration) {
			addInstruction(AnimateParrotInstruction.move(link, offset, duration));
		}

		public ElementLink<MinecartElement> createCart(Vec3 location, float angle, MinecartConstructor type) {
			ElementLink<MinecartElement> link = new ElementLink<>(MinecartElement.class);
			MinecartElement cart = new MinecartElement(location, angle, type);
			addInstruction(new CreateMinecartInstruction(10, Direction.DOWN, cart));
			addInstruction(scene -> scene.linkElement(cart, link));
			return link;
		}

		public void rotateCart(ElementLink<MinecartElement> link, float yRotation, int duration) {
			addInstruction(AnimateMinecartInstruction.rotate(link, yRotation, duration));
		}

		public void moveCart(ElementLink<MinecartElement> link, Vec3 offset, int duration) {
			addInstruction(AnimateMinecartInstruction.move(link, offset, duration));
		}

		public <T extends AnimatedSceneElement> void hideElement(ElementLink<T> link, Direction direction) {
			addInstruction(new FadeOutOfSceneInstruction<>(15, direction, link));
		}

	}

	public class WorldInstructions {

		public void incrementBlockBreakingProgress(BlockPos pos) {
			addInstruction(scene -> {
				PonderWorld world = scene.getWorld();
				int progress = world.getBlockBreakingProgressions()
					.getOrDefault(pos, -1) + 1;
				if (progress == 9) {
					world.addBlockDestroyEffects(pos, world.getBlockState(pos));
					world.destroyBlock(pos, false);
					world.setBlockBreakingProgress(pos, 0);
					scene.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
				} else
					world.setBlockBreakingProgress(pos, progress + 1);
			});
		}

		public void showSection(Selection selection, Direction fadeInDirection) {
			addInstruction(new DisplayWorldSectionInstruction(15, fadeInDirection, selection,
				Optional.of(scene::getBaseWorldSection)));
		}

		public void showSectionAndMerge(Selection selection, Direction fadeInDirection,
			ElementLink<WorldSectionElement> link) {
			addInstruction(new DisplayWorldSectionInstruction(15, fadeInDirection, selection,
				Optional.of(() -> scene.resolve(link))));
		}

		public void glueBlockOnto(BlockPos position, Direction fadeInDirection, ElementLink<WorldSectionElement> link) {
			addInstruction(new DisplayWorldSectionInstruction(15, fadeInDirection,
				scene.getSceneBuildingUtil().select.position(position), Optional.of(() -> scene.resolve(link)),
				position));
		}

		public ElementLink<WorldSectionElement> showIndependentSection(Selection selection, Direction fadeInDirection) {
			DisplayWorldSectionInstruction instruction =
				new DisplayWorldSectionInstruction(15, fadeInDirection, selection, Optional.empty());
			addInstruction(instruction);
			return instruction.createLink(scene);
		}

		public ElementLink<WorldSectionElement> showIndependentSection(Selection selection, Direction fadeInDirection, int duration) {
			DisplayWorldSectionInstruction instruction =
				new DisplayWorldSectionInstruction(duration, fadeInDirection, selection, Optional.empty());
			addInstruction(instruction);
			return instruction.createLink(scene);
		}

		public ElementLink<WorldSectionElement> showIndependentSectionImmediately(Selection selection) {
			DisplayWorldSectionInstruction instruction =
				new DisplayWorldSectionInstruction(0, Direction.DOWN, selection, Optional.empty());
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

		public void hideIndependentSection(ElementLink<WorldSectionElement> link, Direction fadeOutDirection, int duration) {
			addInstruction(new FadeOutOfSceneInstruction<>(duration, fadeOutDirection, link));
		}

		public void hideIndependentSectionImmediately(ElementLink<WorldSectionElement> link) {
			addInstruction(new FadeOutOfSceneInstruction<>(0, Direction.DOWN, link));
		}
		

		public void restoreBlocks(Selection selection) {
			addInstruction(scene -> scene.getWorld()
				.restoreBlocks(selection));
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
				AnimateWorldSectionInstruction.rotate(link, new Vec3(xRotation, yRotation, zRotation), duration));
		}

		public void configureCenterOfRotation(ElementLink<WorldSectionElement> link, Vec3 anchor) {
			addInstruction(scene -> scene.resolve(link)
				.setCenterOfRotation(anchor));
		}

		public void configureStabilization(ElementLink<WorldSectionElement> link, Vec3 anchor) {
			addInstruction(scene -> scene.resolve(link)
				.stabilizeRotation(anchor));
		}

		public void moveSection(ElementLink<WorldSectionElement> link, Vec3 offset, int duration) {
			addInstruction(AnimateWorldSectionInstruction.move(link, offset, duration));
		}

		public void rotateBearing(BlockPos pos, float angle, int duration) {
			addInstruction(AnimateBlockEntityInstruction.bearing(pos, angle, duration));
		}

		public void movePulley(BlockPos pos, float distance, int duration) {
			addInstruction(AnimateBlockEntityInstruction.pulley(pos, distance, duration));
		}

		public void animateBogey(BlockPos pos, float distance, int duration) {
			addInstruction(AnimateBlockEntityInstruction.bogey(pos, distance, duration + 1));
		}

		public void moveDeployer(BlockPos pos, float distance, int duration) {
			addInstruction(AnimateBlockEntityInstruction.deployer(pos, distance, duration));
		}

		public void setBlocks(Selection selection, BlockState state, boolean spawnParticles) {
			addInstruction(new ReplaceBlocksInstruction(selection, $ -> state, true, spawnParticles));
		}

		public void destroyBlock(BlockPos pos) {
			setBlock(pos, Blocks.AIR.defaultBlockState(), true);
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

		public void cycleBlockProperty(BlockPos pos, Property<?> property) {
			modifyBlocks(scene.getSceneBuildingUtil().select.position(pos),
				s -> s.hasProperty(property) ? s.cycle(property) : s, false);
		}

		public void modifyBlocks(Selection selection, UnaryOperator<BlockState> stateFunc, boolean spawnParticles) {
			addInstruction(new ReplaceBlocksInstruction(selection, stateFunc, false, spawnParticles));
		}

		public void toggleRedstonePower(Selection selection) {
			modifyBlocks(selection, s -> {
				if (s.hasProperty(BlockStateProperties.POWER))
					s = s.setValue(BlockStateProperties.POWER, s.getValue(BlockStateProperties.POWER) == 0 ? 15 : 0);
				if (s.hasProperty(BlockStateProperties.POWERED))
					s = s.cycle(BlockStateProperties.POWERED);
				if (s.hasProperty(RedstoneTorchBlock.LIT))
					s = s.cycle(RedstoneTorchBlock.LIT);
				return s;
			}, false);
		}

		public <T extends Entity> void modifyEntities(Class<T> entityClass, Consumer<T> entityCallBack) {
			addInstruction(scene -> scene.forEachWorldEntity(entityClass, entityCallBack));
		}

		public <T extends Entity> void modifyEntitiesInside(Class<T> entityClass, Selection area,
			Consumer<T> entityCallBack) {
			addInstruction(scene -> scene.forEachWorldEntity(entityClass, e -> {
				if (area.test(e.blockPosition()))
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

		public ElementLink<EntityElement> createEntity(Function<Level, Entity> factory) {
			ElementLink<EntityElement> link = new ElementLink<>(EntityElement.class, UUID.randomUUID());
			addInstruction(scene -> {
				PonderWorld world = scene.getWorld();
				Entity entity = factory.apply(world);
				EntityElement handle = new EntityElement(entity);
				scene.addElement(handle);
				scene.linkElement(handle, link);
				world.addFreshEntity(entity);
			});
			return link;
		}

		public ElementLink<EntityElement> createItemEntity(Vec3 location, Vec3 motion, ItemStack stack) {
			return createEntity(world -> {
				ItemEntity itemEntity = new ItemEntity(world, location.x, location.y, location.z, stack);
				itemEntity.setDeltaMovement(motion);
				return itemEntity;
			});
		}

		public void createItemOnBeltLike(BlockPos location, Direction insertionSide, ItemStack stack) {
			addInstruction(scene -> {
				PonderWorld world = scene.getWorld();
				BlockEntity blockEntity = world.getBlockEntity(location);
				if (!(blockEntity instanceof SmartBlockEntity))
					return;
				SmartBlockEntity beltBlockEntity = (SmartBlockEntity) blockEntity;
				DirectBeltInputBehaviour behaviour = beltBlockEntity.getBehaviour(DirectBeltInputBehaviour.TYPE);
				if (behaviour == null)
					return;
				behaviour.handleInsertion(stack, insertionSide.getOpposite(), false);
			});
			flapFunnel(location.above(), true);
		}

		public ElementLink<BeltItemElement> createItemOnBelt(BlockPos beltLocation, Direction insertionSide,
			ItemStack stack) {
			ElementLink<BeltItemElement> link = new ElementLink<>(BeltItemElement.class);
			addInstruction(scene -> {
				PonderWorld world = scene.getWorld();
				BlockEntity blockEntity = world.getBlockEntity(beltLocation);
				if (!(blockEntity instanceof BeltBlockEntity))
					return;

				BeltBlockEntity beltBlockEntity = (BeltBlockEntity) blockEntity;
				DirectBeltInputBehaviour behaviour = beltBlockEntity.getBehaviour(DirectBeltInputBehaviour.TYPE);
				behaviour.handleInsertion(stack, insertionSide.getOpposite(), false);

				BeltBlockEntity controllerBE = beltBlockEntity.getControllerBE();
				if (controllerBE != null)
					controllerBE.tick();

				TransportedItemStackHandlerBehaviour transporter =
					beltBlockEntity.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
				transporter.handleProcessingOnAllItems(tis -> {
					BeltItemElement tracker = new BeltItemElement(tis);
					scene.addElement(tracker);
					scene.linkElement(tracker, link);
					return TransportedResult.doNothing();
				});
			});
			flapFunnel(beltLocation.above(), true);
			return link;
		}

		public void removeItemsFromBelt(BlockPos beltLocation) {
			addInstruction(scene -> {
				PonderWorld world = scene.getWorld();
				BlockEntity blockEntity = world.getBlockEntity(beltLocation);
				if (!(blockEntity instanceof SmartBlockEntity))
					return;
				SmartBlockEntity beltBlockEntity = (SmartBlockEntity) blockEntity;
				TransportedItemStackHandlerBehaviour transporter =
					beltBlockEntity.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
				if (transporter == null)
					return;
				transporter.handleCenteredProcessingOnAllItems(.52f, tis -> TransportedResult.removeItem());
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
			modifyBlockEntityNBT(selection, SpeedGaugeBlockEntity.class, nbt -> {
				float newSpeed = speedFunc.apply(nbt.getFloat("Speed"));
				nbt.putFloat("Value", SpeedGaugeBlockEntity.getDialTarget(newSpeed));
			});
			modifyBlockEntityNBT(selection, KineticBlockEntity.class, nbt -> {
				nbt.putFloat("Speed", speedFunc.apply(nbt.getFloat("Speed")));
			});
		}

		public void propagatePipeChange(BlockPos pos) {
			modifyBlockEntity(pos, PumpBlockEntity.class, be -> be.onSpeedChanged(0));
		}

		public void setFilterData(Selection selection, Class<? extends BlockEntity> teType, ItemStack filter) {
			modifyBlockEntityNBT(selection, teType, nbt -> {
				nbt.put("Filter", filter.serializeNBT());
			});
		}

		public void modifyBlockEntityNBT(Selection selection, Class<? extends BlockEntity> beType,
			Consumer<CompoundTag> consumer) {
			modifyBlockEntityNBT(selection, beType, consumer, false);
		}

		public <T extends BlockEntity> void modifyBlockEntity(BlockPos position, Class<T> beType,
			Consumer<T> consumer) {
			addInstruction(scene -> {
				BlockEntity blockEntity = scene.getWorld()
					.getBlockEntity(position);
				if (beType.isInstance(blockEntity))
					consumer.accept(beType.cast(blockEntity));
			});
		}

		public void modifyBlockEntityNBT(Selection selection, Class<? extends BlockEntity> teType,
			Consumer<CompoundTag> consumer, boolean reDrawBlocks) {
			addInstruction(new BlockEntityDataInstruction(selection, teType, nbt -> {
				consumer.accept(nbt);
				return nbt;
			}, reDrawBlocks));
		}

		public void instructArm(BlockPos armLocation, ArmBlockEntity.Phase phase, ItemStack heldItem,
			int targetedPoint) {
			modifyBlockEntityNBT(scene.getSceneBuildingUtil().select.position(armLocation), ArmBlockEntity.class,
				compound -> {
					NBTHelper.writeEnum(compound, "Phase", phase);
					compound.put("HeldItem", heldItem.serializeNBT());
					compound.putInt("TargetPointIndex", targetedPoint);
					compound.putFloat("MovementProgress", 0);
				});
		}

		public void flapFunnel(BlockPos position, boolean outward) {
			modifyBlockEntity(position, FunnelBlockEntity.class, funnel -> funnel.flap(!outward));
		}

		public void setCraftingResult(BlockPos crafter, ItemStack output) {
			modifyBlockEntity(crafter, MechanicalCrafterBlockEntity.class, mct -> mct.setScriptedResult(output));
		}

		public void connectCrafterInvs(BlockPos position1, BlockPos position2) {
			addInstruction(s -> {
				ConnectedInputHandler.toggleConnection(s.getWorld(), position1, position2);
				s.forEach(WorldSectionElement.class, WorldSectionElement::queueRedraw);
			});
		}

		public void toggleControls(BlockPos position) {
			cycleBlockProperty(position, ControlsBlock.VIRTUAL);
		}

		public void animateTrainStation(BlockPos position, boolean trainPresent) {
			modifyBlockEntityNBT(scene.getSceneBuildingUtil().select.position(position), StationBlockEntity.class,
				c -> c.putBoolean("ForceFlag", trainPresent));
		}

		public void conductorBlaze(BlockPos position, boolean conductor) {
			modifyBlockEntityNBT(scene.getSceneBuildingUtil().select.position(position), BlazeBurnerBlockEntity.class,
				c -> c.putBoolean("TrainHat", conductor));
		}

		public void changeSignalState(BlockPos position, SignalState state) {
			modifyBlockEntityNBT(scene.getSceneBuildingUtil().select.position(position), SignalBlockEntity.class,
				c -> NBTHelper.writeEnum(c, "State", state));
		}

		public void setDisplayBoardText(BlockPos position, int line, Component text) {
			modifyBlockEntity(position, FlapDisplayBlockEntity.class,
				t -> t.applyTextManually(line, Component.Serializer.toJson(text)));
		}

		public void dyeDisplayBoard(BlockPos position, int line, DyeColor color) {
			modifyBlockEntity(position, FlapDisplayBlockEntity.class, t -> t.setColour(line, color));
		}

		public void flashDisplayLink(BlockPos position) {
			modifyBlockEntity(position, DisplayLinkBlockEntity.class,
				linkBlockEntity -> linkBlockEntity.glow.setValue(2));
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

}
