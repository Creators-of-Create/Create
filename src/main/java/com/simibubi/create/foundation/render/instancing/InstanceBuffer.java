package com.simibubi.create.foundation.render.instancing;


import com.mojang.blaze3d.platform.GlStateManager;
import com.simibubi.create.foundation.render.RenderWork;
import com.simibubi.create.foundation.render.SafeDirectBuffer;
import com.simibubi.create.foundation.render.TemplateBuffer;
import net.minecraft.client.renderer.BufferBuilder;
import org.lwjgl.opengl.*;

import java.util.ArrayList;
import java.util.function.Consumer;

import static com.simibubi.create.foundation.render.instancing.VertexAttribute.*;

public abstract class InstanceBuffer<D extends InstanceData> extends TemplateBuffer {
    public static final VertexFormat FORMAT = new VertexFormat(POSITION, NORMAL, UV);

    protected int vao, ebo, invariantVBO, instanceVBO, instanceCount;

    protected final ArrayList<D> data = new ArrayList<>();
    protected boolean rebuffer = false;
    protected boolean shouldBuild = true;

    public InstanceBuffer(BufferBuilder buf) {
        super(buf);
        setup();
    }

    private void setup() {
        int stride = FORMAT.getStride();

        int invariantSize = vertexCount * stride;

        vao = GL30.glGenVertexArrays();
        ebo = GlStateManager.genBuffers();
        invariantVBO = GlStateManager.genBuffers();
        instanceVBO = GlStateManager.genBuffers();

        try (SafeDirectBuffer constant = new SafeDirectBuffer(invariantSize)) {
            constant.order(template.order());
            constant.limit(invariantSize);

            for (int i = 0; i < vertexCount; i++) {
                constant.putFloat(getX(template, i));
                constant.putFloat(getY(template, i));
                constant.putFloat(getZ(template, i));

                constant.put(getNX(template, i));
                constant.put(getNY(template, i));
                constant.put(getNZ(template, i));

                constant.putFloat(getU(template, i));
                constant.putFloat(getV(template, i));
            }
            constant.rewind();

            GL30.glBindVertexArray(vao);

            GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, invariantVBO);
            GlStateManager.bufferData(GL15.GL_ARRAY_BUFFER, constant.getBacking(), GL15.GL_STATIC_DRAW);

            buildEBO(ebo);

            FORMAT.informAttributes(0);
        } catch (Exception e) {
            delete();
        }

        GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, 0);
        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        // Deselect (bind to 0) the VAO
        GL30.glBindVertexArray(0);
    }

    protected abstract VertexFormat getInstanceFormat();

    public int numInstances() {
        return instanceCount + data.size();
    }

    public boolean isEmpty() {
        return numInstances() == 0;
    }

    public void clearInstanceData() {
        instanceCount = 0;
        shouldBuild = true;
    }

    public void markDirty() {
        rebuffer = true;
    }

    public void delete() {
        RenderWork.enqueue(() -> {
            GL15.glDeleteBuffers(invariantVBO);
            GL15.glDeleteBuffers(instanceVBO);
            GL15.glDeleteBuffers(ebo);
            GL30.glDeleteVertexArrays(vao);
            vao = 0;
            ebo = 0;
            invariantVBO = 0;
            instanceVBO = 0;
        });
    }

    protected abstract D newInstance();

    public void setupInstance(Consumer<D> setup) {
        if (!shouldBuild) return;

        D instanceData = newInstance();
        setup.accept(instanceData);

        data.add(instanceData);
    }

    public void render() {
        if (vao == 0) return;

        GL30.glBindVertexArray(vao);
        finishBuffering();

        int numAttributes = getInstanceFormat().getNumAttributes() + FORMAT.getNumAttributes();
        for (int i = 0; i <= numAttributes; i++) {
            GL40.glEnableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);

        GL40.glDrawElementsInstanced(GL11.GL_QUADS, vertexCount, GL11.GL_UNSIGNED_SHORT, 0, instanceCount);

        for (int i = 0; i <= numAttributes; i++) {
            GL40.glDisableVertexAttribArray(i);
        }

        GlStateManager.bindBuffers(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    private void finishBuffering() {
        if (!rebuffer || isEmpty()) return;

        instanceCount = data.size();

        VertexFormat instanceFormat = getInstanceFormat();

        int instanceSize = instanceCount * instanceFormat.getStride();

        try (SafeDirectBuffer buffer = new SafeDirectBuffer(instanceSize)) {
            buffer.order(template.order());
            buffer.limit(instanceSize);

            data.forEach(instanceData -> instanceData.write(buffer));
            buffer.rewind();

            GlStateManager.bindBuffers(GL15.GL_ARRAY_BUFFER, instanceVBO);
            GlStateManager.bufferData(GL15.GL_ARRAY_BUFFER, buffer.getBacking(), GL15.GL_STATIC_DRAW);

            int staticAttributes = FORMAT.getNumAttributes();
            instanceFormat.informAttributes(staticAttributes);

            for (int i = 0; i < instanceFormat.getNumAttributes(); i++) {
                GL33.glVertexAttribDivisor(i + staticAttributes, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Deselect (bind to 0) the VBO
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        shouldBuild = false;
        rebuffer = false;
        data.clear();
    }
}
