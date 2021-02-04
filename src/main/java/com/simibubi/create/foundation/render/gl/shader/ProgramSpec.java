package com.simibubi.create.foundation.render.gl.shader;

import com.simibubi.create.Create;
import net.minecraft.util.ResourceLocation;

public class ProgramSpec<P extends GlProgram> {

    public final ResourceLocation name;
    public final ResourceLocation vert;
    public final ResourceLocation frag;

    public final ShaderConstants defines;

    public final GlProgram.ProgramFactory<P> factory;

    public ProgramSpec(String name, ResourceLocation vert, ResourceLocation frag, GlProgram.ProgramFactory<P> factory) {
        this(name, vert, frag, factory, null);
    }

    public ProgramSpec(String name, ResourceLocation vert, ResourceLocation frag, GlProgram.ProgramFactory<P> factory, ShaderConstants defines) {
        this.name = new ResourceLocation(Create.ID, name);
        this.vert = vert;
        this.frag = frag;
        this.defines = defines;

        this.factory = factory;
    }

    public ResourceLocation getVert() {
        return vert;
    }

    public ResourceLocation getFrag() {
        return frag;
    }

}
