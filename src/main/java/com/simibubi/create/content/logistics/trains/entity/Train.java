package com.simibubi.create.content.logistics.trains.entity;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import com.simibubi.create.Create;
import com.simibubi.create.CreateClient;
import com.simibubi.create.content.logistics.trains.TrackGraph;
import com.simibubi.create.content.logistics.trains.entity.MovingPoint.ITrackSelector;
import com.simibubi.create.content.logistics.trains.management.GlobalStation;
import com.simibubi.create.content.logistics.trains.management.ScheduleRuntime;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class Train {

	public static final double acceleration = 0.005f;
	public static final double topSpeed = 1.2f;
	
	public double speed = 0;
	public double targetSpeed = 0;

	public UUID id;
	public TrackGraph graph;
	public Navigation navigation;
	public GlobalStation currentStation;
	public ScheduleRuntime runtime;
	public TrainIconType icon;
	public Component name;

	public boolean doubleEnded;
	public List<Carriage> carriages;
	public List<Integer> carriageSpacing;

	double[] stress;

	public Train(UUID id, TrackGraph graph, List<Carriage> carriages, List<Integer> carriageSpacing) {
		this.id = id;
		this.graph = graph;
		this.carriages = carriages;
		this.carriageSpacing = carriageSpacing;
		this.icon = TrainIconType.getDefault();
		this.stress = new double[carriageSpacing.size()];
		this.name = Lang.translate("train.unnamed");

		carriages.forEach(c -> {
			c.setTrain(this);
			Create.RAILWAYS.carriageById.put(c.id, c);
		});

		doubleEnded = carriages.size() > 1 && carriages.get(carriages.size() - 1).contraption.hasControls();
		navigation = new Navigation(this, graph);
		runtime = new ScheduleRuntime(this);
	}

	public void tick(Level level) {
		runtime.tick(level);
		navigation.tick(level);

		double distance = speed;
		Carriage previousCarriage = null;

		for (int i = 0; i < carriages.size(); i++) {
			Carriage carriage = carriages.get(i);
			if (previousCarriage != null) {
				int target = carriageSpacing.get(i - 1);
				Vec3 leadingAnchor = carriage.leadingBogey().anchorPosition;
				Vec3 trailingAnchor = previousCarriage.trailingBogey().anchorPosition;
				double actual = leadingAnchor.distanceTo(trailingAnchor);
				stress[i - 1] = target - actual;
			}
			previousCarriage = carriage;
		}

		// positive stress: carriages should move apart
		// negative stress: carriages should move closer

		boolean approachingStation = navigation.distanceToDestination < 5;
		double leadingModifier = approachingStation ? -0.75d : -0.5d;
		double trailingModifier = approachingStation ? 0d : 0.125d;

		MovingPoint previous = null;
		for (int i = 0; i < carriages.size(); i++) {
			double leadingStress = i == 0 ? 0 : stress[i - 1] * leadingModifier;
			double trailingStress = i == stress.length ? 0 : stress[i] * trailingModifier;

			Carriage carriage = carriages.get(i);
			MovingPoint toFollow = previous;
			Function<MovingPoint, ITrackSelector> control =
				previous == null ? navigation::control : mp -> mp.follow(toFollow);
			double actualDistance = carriage.travel(level, distance + leadingStress + trailingStress, control);

			if (i == 0)
				distance = actualDistance;
			previous = carriage.getTrailingPoint();
		}

		if (navigation.destination != null) {
			navigation.distanceToDestination -= distance;
		}
	}

	public boolean disassemble(Direction assemblyDirection, BlockPos pos) {
		for (Carriage carriage : carriages) {
			CarriageContraptionEntity entity = carriage.entity.get();
			if (entity == null)
				return false;
			if (!Mth.equal(entity.pitch, 0))
				return false;
			if (!Mth.equal(((entity.yaw % 90) + 360) % 90, 0))
				return false;
		}

		int offset = 1;
		for (int i = 0; i < carriages.size(); i++) {
			
			Carriage carriage = carriages.get(i);
			CarriageContraptionEntity entity = carriage.entity.get();
			if (entity == null)
				return false;
			
			entity.setPos(Vec3.atLowerCornerOf(pos.relative(assemblyDirection, offset)));
			entity.disassemble();
			Create.RAILWAYS.carriageById.remove(carriage.id);
			CreateClient.RAILWAYS.carriageById.remove(carriage.id);

			offset += carriage.bogeySpacing;
			if (i < carriageSpacing.size())
				offset += carriageSpacing.get(i);
		}

		if (currentStation != null)
			currentStation.cancelReservation(this);

		Create.RAILWAYS.trains.remove(id);
		CreateClient.RAILWAYS.trains.remove(id);
		return true;
	}

	public int getTotalLength() {
		int length = 0;
		for (int i = 0; i < carriages.size(); i++) {
			length += carriages.get(i).bogeySpacing;
			if (i < carriageSpacing.size())
				length += carriageSpacing.get(i);
		}
		return length;
	}

	public void leave() {
		currentStation.trainDeparted(this);
		currentStation = null;
	}

	public void arriveAt(GlobalStation station) {
		currentStation = station;
		runtime.destinationReached();
	}

}
