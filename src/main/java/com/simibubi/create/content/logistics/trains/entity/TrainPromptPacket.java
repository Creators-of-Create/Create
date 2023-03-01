package com.simibubi.create.content.logistics.trains.entity;

import com.simibubi.create.content.contraptions.components.structureMovement.interaction.controls.TrainHUD;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent.Context;

public class TrainPromptPacket extends SimplePacketBase {

	private Component text;
	private boolean shadow;

	public TrainPromptPacket(Component text, boolean shadow) {
		this.text = text;
		this.shadow = shadow;
	}

	public TrainPromptPacket(FriendlyByteBuf buffer) {
		text = buffer.readComponent();
		shadow = buffer.readBoolean();
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeComponent(text);
		buffer.writeBoolean(shadow);
	}

	@Override
	public boolean handle(Context context) {
		context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::apply));
		return true;
	}

	@OnlyIn(Dist.CLIENT)
	public void apply() {
		TrainHUD.currentPrompt = text;
		TrainHUD.currentPromptShadow = shadow;
		TrainHUD.promptKeepAlive = 30;
	}

}
