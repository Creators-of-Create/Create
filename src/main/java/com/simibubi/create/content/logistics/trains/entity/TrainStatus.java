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
	
	public void successfulNavigation() {
		if (!navigation)
			return;
		displayInformation("Navigation succeeded", true);
		navigation = false;
	}

	public void failedMigration() {
		if (track)
			return;
		displayInformation("Tracks are missing beneath the Train", false);
		track = true;
	}

	public void endOfTrack() {
		if (track)
			return;
		displayInformation("A Carriage has reached the end of its Track.", false);
		track = true;
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
	}

}
