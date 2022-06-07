package com.simibubi.create.content.logistics.trains.entity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TrainStatus {

	Train train;

	boolean navigation;
	boolean track;
	boolean conductor;

	List<Component> queued = new ArrayList<>();

	public TrainStatus(Train train) {
		this.train = train;
	}
	
	public void failedNavigation() {
		if (navigation)
			return;
		displayInformation("Unable to find Path to next Scheduled destination", false);
		navigation = true;
	}
	
	public void failedNavigationNoTarget(String filter) {
		if (navigation)
			return;
		displayInformation("No Station on graph matches '" + filter + "'", false);
		navigation = true;
	}
	
	public void successfulNavigation() {
		if (!navigation)
			return;
		displayInformation("Navigation succeeded", true);
		navigation = false;
	}
	
	public void foundConductor() {
		if (!conductor)
			return;
		displayInformation("A new driver has been found", true);
		conductor = false;
	}
	
	public void missingConductor() {
		if (conductor)
			return;
		displayInformation("Driver has gone missing", false);
		conductor = true;
	}
	
	public void missingBackwardsConductor() { // missingCorrectConductor
		if (conductor)
			return;
		displayInformation("Path requires a driver facing the opposite direction", false);
		conductor = true;
	}
	
	public void manualControls() {
		displayInformation("Schedule paused for manual controls", true);
	}

	public void failedMigration() {
		if (track)
			return;
		displayInformation("Tracks are missing beneath the Train", false);
		track = true;
	}
	
	public void highStress() {
		if (track)
			return;
		displayInformation("Forced stop due to Stress on Couplings", false);
		track = true;
	}

	public void doublePortal() {
		if (track)
			return;
		displayInformation("A Carriage cannot enter a portal whilst leaving another.", false);
		track = true;
	}
	
	public void endOfTrack() {
		if (track)
			return;
		displayInformation("A Carriage has reached the end of its Track.", false);
		track = true;
	}
	
	public void crash() {
		displayInformation("Collision with other Train", false);
	}

	public void successfulMigration() {
		if (!track)
			return;
		displayInformation("Train is back on Track", true);
		track = false;
	}
	
	public void trackOK() {
		track = false;
	}

	public void tick(Level level) {
		if (queued.isEmpty())
			return;
		LivingEntity owner = train.getOwner(level);
		if (owner == null)
			return;
		if (owner instanceof Player player) {
			// TODO change to Lang.translate
			player.displayClientMessage(new TextComponent("<i> Information about Train: ").append(train.name)
				.withStyle(ChatFormatting.GOLD), false);
			queued.forEach(c -> player.displayClientMessage(c, false));
		}
		queued.clear();
	}

	public void displayInformation(String key, boolean itsAGoodThing, Object... args) {
		queued.add(new TextComponent(" - ").withStyle(ChatFormatting.GRAY)
			.append(new TextComponent(key).withStyle(st -> st.withColor(itsAGoodThing ? 0xD5ECC2 : 0xFFD3B4))));
		if (queued.size() > 3)
			queued.remove(0);
	}

	public void newSchedule() {
		navigation = false;
		conductor = false;
	}

}
