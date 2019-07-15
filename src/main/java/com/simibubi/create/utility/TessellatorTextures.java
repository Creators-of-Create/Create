package com.simibubi.create.utility;

import com.simibubi.create.Create;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public enum TessellatorTextures {

    Room("inner.png"),
    RoomTransparent("inner_transparent.png"),
    SelectedRoom("inner_selected.png"),
    SuperSelectedRoom("inner_super_selected.png"),
    Selection("select.png"),
    Exporter("exporter.png"),
    Trim("trim.png");

    private ResourceLocation location;

    private TessellatorTextures(String filename) {
        location = new ResourceLocation(Create.ID,
                "textures/block/marker/" + filename);
    }

    public void bind() {
        Minecraft.getInstance().getTextureManager().bindTexture(location);
    }

}