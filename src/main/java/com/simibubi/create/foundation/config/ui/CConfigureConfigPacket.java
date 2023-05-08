package com.simibubi.create.foundation.config.ui;

import java.util.Objects;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.networking.SimplePacketBase;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.network.NetworkEvent;

public class CConfigureConfigPacket<T> extends SimplePacketBase {

	private String modID;
	private String path;
	private String value;

	public CConfigureConfigPacket(String modID, String path, T value) {
		this.modID = Objects.requireNonNull(modID);
		this.path = path;
		this.value = serialize(value);
	}

	public CConfigureConfigPacket(FriendlyByteBuf buffer) {
		this.modID = buffer.readUtf(32767);
		this.path = buffer.readUtf(32767);
		this.value = buffer.readUtf(32767);
	}

	@Override
	public void write(FriendlyByteBuf buffer) {
		buffer.writeUtf(modID);
		buffer.writeUtf(path);
		buffer.writeUtf(value);
	}

	@Override
	public boolean handle(NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			try {
				ServerPlayer sender = context.getSender();
				if (sender == null || !sender.hasPermissions(2))
					return;

				ForgeConfigSpec spec = ConfigHelper.findForgeConfigSpecFor(ModConfig.Type.SERVER, modID);
				if (spec == null)
					return;
				ForgeConfigSpec.ValueSpec valueSpec = spec.getRaw(path);
				ForgeConfigSpec.ConfigValue<T> configValue = spec.getValues().get(path);

				T v = (T) deserialize(configValue.get(), value);
				if (!valueSpec.test(v))
					return;

				configValue.set(v);
			} catch (Exception e) {
				Create.LOGGER.warn("Unable to handle ConfigureConfig Packet. ", e);
			}
		});
		return true;
	}

	public String serialize(T value) {
		if (value instanceof Boolean)
			return Boolean.toString((Boolean) value);
		if (value instanceof Enum<?>)
			return ((Enum<?>) value).name();
		if (value instanceof Integer)
			return Integer.toString((Integer) value);
		if (value instanceof Float)
			return Float.toString((Float) value);
		if (value instanceof Double)
			return Double.toString((Double) value);

		throw new IllegalArgumentException("unknown type " + value + ": " + value.getClass().getSimpleName());
	}

	public static Object deserialize(Object type, String sValue) {
		if (type instanceof Boolean)
			return Boolean.parseBoolean(sValue);
		if (type instanceof Enum<?>)
			return Enum.valueOf(((Enum<?>) type).getClass(), sValue);
		if (type instanceof Integer)
			return Integer.parseInt(sValue);
		if (type instanceof Float)
			return Float.parseFloat(sValue);
		if (type instanceof Double)
			return Double.parseDouble(sValue);

		throw new IllegalArgumentException("unknown type " + type + ": " + type.getClass().getSimpleName());
	}
}
