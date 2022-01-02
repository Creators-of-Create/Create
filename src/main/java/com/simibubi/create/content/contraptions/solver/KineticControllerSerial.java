package com.simibubi.create.content.contraptions.solver;

import com.simibubi.create.content.contraptions.relays.advanced.SpeedControllerTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import java.util.Optional;

public enum KineticControllerSerial {

	SPEED_CONTROLLER_COG {
		class Controller implements IKineticController {
			private final KineticConnections connections;
			private float targetSpeed;
			private float generatedSpeed;

			public Controller(KineticConnections connections, float targetSpeed) {
				this.connections = connections;
				this.targetSpeed = targetSpeed;
			}

			@Override
			public KineticConnections getConnections() {
				return connections;
			}

			@Override
			public void onUpdate(Level level, KineticSolver solver, KineticNode node) {
				BlockPos below = node.getPos().below();
				if (level.getBlockEntity(below) instanceof SpeedControllerTileEntity se) {
					targetSpeed = se.getTargetSpeed();
				}

				Optional<KineticNode> seNode = solver.getNode(below);
				if (seNode.isPresent() && seNode.get().getTheoreticalSpeed() != 0) {
					generatedSpeed = targetSpeed;
				} else {
					generatedSpeed = 0;
				}
			}

			@Override
			public float getGeneratedSpeed() {
				return generatedSpeed;
			}

			@Override
			public CompoundTag save(CompoundTag tag) {
				tag.put("Connections", connections.save(new CompoundTag()));
				tag.putFloat("Target", targetSpeed);
				return tag;
			}
		}

		@Override
		public IKineticController init(KineticNode prev) {
			return new Controller(prev.getConnections(), 0);
		}

		@Override
		public IKineticController load(CompoundTag tag) {
			KineticConnections connections = KineticConnections.load(tag.getCompound("Connections"));
			float target = tag.getFloat("Target");
			return new Controller(connections, target);
		}
	};

	public abstract IKineticController init(KineticNode prev);
	public abstract IKineticController load(CompoundTag tag);

}
