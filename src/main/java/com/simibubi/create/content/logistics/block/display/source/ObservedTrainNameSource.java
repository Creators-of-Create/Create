package com.simibubi.create.content.logistics.block.display.source;

import java.util.UUID;

import com.simibubi.create.Create;
import com.simibubi.create.content.logistics.block.display.DisplayLinkContext;
import com.simibubi.create.content.logistics.block.display.target.DisplayTargetStats;
import com.simibubi.create.content.logistics.trains.entity.Train;
import com.simibubi.create.content.logistics.trains.management.edgePoint.observer.TrackObserver;
import com.simibubi.create.content.logistics.trains.management.edgePoint.observer.TrackObserverBlockEntity;

import net.minecraft.network.chat.MutableComponent;

public class ObservedTrainNameSource extends SingleLineDisplaySource {

	@Override
	protected MutableComponent provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
		if (!(context.getSourceBlockEntity() instanceof TrackObserverBlockEntity observerTE))
			return EMPTY_LINE;
		TrackObserver observer = observerTE.getObserver();
		if (observer == null)
			return EMPTY_LINE;
		UUID currentTrain = observer.getCurrentTrain();
		if (currentTrain == null)
			return EMPTY_LINE;
		Train train = Create.RAILWAYS.trains.get(currentTrain);
		if (train == null)
			return EMPTY_LINE;
		return train.name.copy();
	}
	
	@Override
	public int getPassiveRefreshTicks() {
		return 400;
	}
	
	@Override
	protected String getTranslationKey() {
		return "observed_train_name";
	}

	@Override
	protected boolean allowsLabeling(DisplayLinkContext context) {
		return true;
	}

}
