package com.simibubi.create.foundation.ponder;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import com.simibubi.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.simibubi.create.content.contraptions.glue.SuperGlueItem;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.RotationIndicatorParticleData;
import com.simibubi.create.content.kinetics.belt.BeltBlockEntity;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.crafter.ConnectedInputHandler;
import com.simibubi.create.content.kinetics.crafter.MechanicalCrafterBlockEntity;
import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.simibubi.create.content.logistics.funnel.FunnelBlockEntity;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.trains.display.FlapDisplayBlockEntity;
import com.simibubi.create.content.trains.signal.SignalBlockEntity;
import com.simibubi.create.content.trains.station.StationBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.ponder.element.BeltItemElement;
import com.simibubi.create.foundation.ponder.element.ExpandedParrotElement;
import com.simibubi.create.foundation.ponder.instruction.AnimateBlockEntityInstruction;

import net.createmod.catnip.utility.FunctionalHelper;
import net.createmod.catnip.utility.NBTHelper;
import net.createmod.catnip.utility.VecHelper;
import net.createmod.ponder.foundation.ElementLink;
import net.createmod.ponder.foundation.PonderScene;
import net.createmod.ponder.foundation.PonderWorld;
import net.createmod.ponder.foundation.SceneBuilder;
import net.createmod.ponder.foundation.Selection;
import net.createmod.ponder.foundation.element.ParrotElement;
import net.createmod.ponder.foundation.element.WorldSectionElement;
import net.createmod.ponder.foundation.instruction.CreateParrotInstruction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class CreateSceneBuilder extends SceneBuilder {

	public final EffectInstructions effects;
	public final WorldInstructions world;
	public final SpecialInstructions special;

	public CreateSceneBuilder(SceneBuilder baseSceneBuilder) {
		this(baseSceneBuilder.getScene());
	}

	private CreateSceneBuilder(PonderScene ponderScene) {
		super(ponderScene);
		effects = new EffectInstructions();
		world = new WorldInstructions();
		special = new SpecialInstructions();
	}

	public class EffectInstructions extends SceneBuilder.EffectInstructions {

		public void superGlue(BlockPos pos, Direction side, boolean fullBlock) {
			addInstruction(scene -> SuperGlueItem.spawnParticles(scene.getWorld(), pos, side, fullBlock));
		}

		private void rotationIndicator(BlockPos pos, boolean direction, BlockPos displayPos) {
			addInstruction(scene -> {
				BlockState blockState = scene.getWorld().getBlockState(pos);
				BlockEntity blockEntity = scene.getWorld().getBlockEntity(pos);

				if (!(blockState.getBlock() instanceof KineticBlock kb))
					return;
				if (!(blockEntity instanceof KineticBlockEntity kbe))
					return;

				Direction.Axis rotationAxis = kb.getRotationAxis(blockState);

				float speed = kbe.getTheoreticalSpeed();
				IRotate.SpeedLevel speedLevel = IRotate.SpeedLevel.of(speed);
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


	}

	public class WorldInstructions extends SceneBuilder.WorldInstructions {

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

		public void createItemOnBeltLike(BlockPos location, Direction insertionSide, ItemStack stack) {
			addInstruction(scene -> {
				PonderWorld world = scene.getWorld();
				BlockEntity blockEntity = world.getBlockEntity(location);
				if (!(blockEntity instanceof SmartBlockEntity beltBlockEntity))
					return;
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
				if (!(blockEntity instanceof BeltBlockEntity beltBlockEntity))
					return;

				DirectBeltInputBehaviour behaviour = beltBlockEntity.getBehaviour(DirectBeltInputBehaviour.TYPE);
				behaviour.handleInsertion(stack, insertionSide.getOpposite(), false);

				BeltBlockEntity controllerBE = beltBlockEntity.getControllerBE();
				if (controllerBE != null)
					controllerBE.tick();

				com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour transporter =
						beltBlockEntity.getBehaviour(com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TYPE);
				transporter.handleProcessingOnAllItems(tis -> {
					BeltItemElement tracker = new BeltItemElement(tis);
					scene.addElement(tracker);
					scene.linkElement(tracker, link);
					return com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult.doNothing();
				});
			});
			flapFunnel(beltLocation.above(), true);
			return link;
		}

		public void removeItemsFromBelt(BlockPos beltLocation) {
			addInstruction(scene -> {
				PonderWorld world = scene.getWorld();
				BlockEntity blockEntity = world.getBlockEntity(beltLocation);
				if (!(blockEntity instanceof SmartBlockEntity beltBlockEntity))
					return;
				TransportedItemStackHandlerBehaviour transporter =
						beltBlockEntity.getBehaviour(TransportedItemStackHandlerBehaviour.TYPE);
				if (transporter == null)
					return;
				transporter.handleCenteredProcessingOnAllItems(.52f, tis -> TransportedItemStackHandlerBehaviour.TransportedResult.removeItem());
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
			modifyBlockEntityNBT(getScene().getSceneBuildingUtil().select.position(position), StationBlockEntity.class,
					c -> c.putBoolean("ForceFlag", trainPresent));
		}

		public void conductorBlaze(BlockPos position, boolean conductor) {
			modifyBlockEntityNBT(getScene().getSceneBuildingUtil().select.position(position), BlazeBurnerBlockEntity.class,
					c -> c.putBoolean("TrainHat", conductor));
		}

		public void changeSignalState(BlockPos position, SignalBlockEntity.SignalState state) {
			modifyBlockEntityNBT(getScene().getSceneBuildingUtil().select.position(position), SignalBlockEntity.class,
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

	public class SpecialInstructions extends SceneBuilder.SpecialInstructions {

		@Override
		public ElementLink<ParrotElement> createBirb(Vec3 location, Supplier<? extends ParrotElement.ParrotPose> pose) {
			ElementLink<ParrotElement> link = new ElementLink<>(ParrotElement.class);
			ParrotElement parrot = ExpandedParrotElement.create(location, pose);
			addInstruction(new CreateParrotInstruction(10, Direction.DOWN, parrot));
			addInstruction(scene -> scene.linkElement(parrot, link));
			return link;
		}

		public ElementLink<ParrotElement> birbOnTurntable(BlockPos pos) {
			return createBirb(VecHelper.getCenterOf(pos), () -> new ParrotSpinOnComponentPose(pos));
		}

		public ElementLink<ParrotElement> birbOnSpinnyShaft(BlockPos pos) {
			return createBirb(VecHelper.getCenterOf(pos)
					.add(0, 0.5, 0), () -> new ParrotSpinOnComponentPose(pos));
		}

		public void conductorBirb(ElementLink<ParrotElement> birb, boolean conductor) {
			addInstruction(scene -> scene.resolveOptional(birb)
					.map(FunctionalHelper.filterAndCast(ExpandedParrotElement.class))
					.ifPresent(expandedBirb -> expandedBirb.setConductor(conductor)));
		}

		public static class ParrotSpinOnComponentPose extends ParrotElement.ParrotPose {
			private final BlockPos componentPos;

			public ParrotSpinOnComponentPose(BlockPos componentPos) {
				this.componentPos = componentPos;
			}

			@Override
			protected void tick(PonderScene scene, Parrot entity, Vec3 location) {
				BlockEntity blockEntity = scene.getWorld().getBlockEntity(componentPos);
				if (!(blockEntity instanceof KineticBlockEntity))
					return;
				float rpm = ((KineticBlockEntity) blockEntity).getSpeed();
				entity.yRotO = entity.getYRot();
				entity.setYRot(entity.getYRot() + (rpm * .3f));
			}
		}
	}

}
