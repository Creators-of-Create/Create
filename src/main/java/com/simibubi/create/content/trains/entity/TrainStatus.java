package com.simibubi.create.content.trains.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.google.common.collect.Streams;
import com.simibubi.create.foundation.utility.CreateLang;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class TrainStatus {

	Train train;

	public boolean navigation;
	public boolean track;
	public boolean conductor;

	List<StatusMessage> queued = new ArrayList<>();

	public TrainStatus(Train train) {
		this.train = train;
	}

	public void failedNavigation() {
		if (navigation)
			return;
		displayInformation("no_path", false);
		navigation = true;
	}

	public void failedNavigationNoTarget(String filter) {
		if (navigation)
			return;
		displayInformation("no_match", false, filter);
		navigation = true;
	}

	public void failedPackageNoTarget(String address) {
		if (navigation)
			return;
		displayInformation("no_package_target", false, address);
		navigation = true;
	}

	public void successfulNavigation() {
		if (!navigation)
			return;
		displayInformation("navigation_success", true);
		navigation = false;
	}

	public void foundConductor() {
		if (!conductor)
			return;
		displayInformation("found_driver", true);
		conductor = false;
	}

	public void missingConductor() {
		if (conductor)
			return;
		displayInformation("missing_driver", false);
		conductor = true;
	}

	public void missingCorrectConductor() {
		if (conductor)
			return;
		displayInformation("opposite_driver", false);
		conductor = true;
	}

	public void manualControls() {
		displayInformation("paused_for_manual", true);
	}

	public void failedMigration() {
		if (track)
			return;
		displayInformation("track_missing", false);
		track = true;
	}

	public void highStress() {
		if (track)
			return;
		displayInformation("coupling_stress", false);
		track = true;
	}

	public void doublePortal() {
		if (track)
			return;
		displayInformation("double_portal", false);
		track = true;
	}

	public void endOfTrack() {
		if (track)
			return;
		displayInformation("end_of_track", false);
		track = true;
	}

	public void crash() {
		Component component =
			Component.literal(" - ").withStyle(ChatFormatting.GRAY)
			.append(
				CreateLang.translateDirect("train.status.collision").withStyle(st -> st.withColor(0xFFD3B4))
			);
		List<ResourceKey<Level>> presentDimensions = train.getPresentDimensions();
		Stream<Component> locationComponents = presentDimensions.stream().map(key ->
                {
                    return Component.literal(" - ").withStyle(ChatFormatting.GRAY)
                        .append(
                            CreateLang.translateDirect(
                                "train.status.collision.where",
								key.location().toString(),
                                    train.getPositionInDimension(key).get().toShortString()
                                ).withStyle(style -> style.withColor(0xFFD3B4))
                        );
                }
		);
		addMessage(new StatusMessage(Streams.concat(Stream.of(component), locationComponents).toArray(Component[]::new)));

	}

	public void successfulMigration() {
		if (!track)
			return;
		displayInformation("back_on_track", true);
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
			player.displayClientMessage(CreateLang.translateDirect("train.status", train.name)
				.withStyle(ChatFormatting.GOLD), false);
			queued.forEach(message -> message.displayToPlayer(player));
		}
		queued.clear();
	}

	public void displayInformation(String key, boolean itsAGoodThing, Object... args) {
        MutableComponent component = Component.literal(" - ").withStyle(ChatFormatting.GRAY)
			.append(CreateLang.translateDirect("train.status." + key, args)
				.withStyle(st -> st.withColor(itsAGoodThing ? 0xD5ECC2 : 0xFFD3B4)));
		addMessage(new StatusMessage(component));
	}

	public void addMessage(StatusMessage message) {
		queued.add(message);

		if (queued.size() > 3)
			queued.remove(0);
	}

	public void newSchedule() {
		navigation = false;
		conductor = false;
	}

	public record StatusMessage(Component... messages) {
		public void displayToPlayer(Player player) {
			Arrays.stream(messages).forEach(messages -> player.displayClientMessage(messages, false));
		}

	};

}
