package com.zigythebird.playeranim.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zigythebird.playeranim.PlayerAnimLibMod;
import com.zigythebird.playeranimcore.bindings.PlatformModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class MinecraftModel implements PlatformModel, MaterialBaker, ModelBaker, ModelBaker.Interner, ModelDebugName {
    private static final AtomicInteger TEXTURE_COUNTER = new AtomicInteger(0);

    private final TextureAtlasSprite sprite;
    private final RenderType renderType;
    private final QuadCollection geometry;

    public MinecraftModel(@Nullable String texture, @NotNull JsonArray elements) throws IOException {
        this.sprite = loadAtlasSprite(texture);
        this.renderType = RenderTypes.entityCutoutCull(this.sprite.atlasLocation());

        List<BlockElement> blockElements = new ArrayList<>();
        for (JsonElement element : elements) {
            blockElements.add(BlockModel.GSON.fromJson(element, BlockElement.class));
        }
        this.geometry = SimpleUnbakedGeometry.bake(blockElements, TextureSlots.EMPTY, this, BlockModelRotation.IDENTITY, this);
    }

    @NotNull
    public QuadCollection getGeometry() {
        return this.geometry;
    }

    @NotNull
    public RenderType getRenderType() {
        return this.renderType;
    }

    @Override
    public void invalidate() {
        if (this.sprite != null) this.sprite.close();
    }

    @Override
    public @NonNull String debugName() {
        return "playeranim:custom_bone";
    }

    @Override
    public Material.@NonNull Baked get(Material material, @NonNull ModelDebugName name) {
        return new Material.Baked(this.sprite, material.forceTranslucent());
    }

    @Override
    public Material.@NonNull Baked reportMissingReference(@NonNull String reference, @NonNull ModelDebugName name) {
        return new Material.Baked(this.sprite, false);
    }

    @Override
    public @NonNull ResolvedModel getModel(@NonNull Identifier location) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull BlockModelPart missingBlockModelPart() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NonNull MaterialBaker materials() {
        return this;
    }

    @Override
    public @NonNull Interner interner() {
        return this;
    }

    @Override
    public @NonNull Vector3fc vector(@NonNull Vector3fc vector) {
        return vector;
    }

    @Override
    public BakedQuad.@NonNull SpriteInfo spriteInfo(BakedQuad.@NonNull SpriteInfo sprite) {
        return sprite;
    }

    @Override
    public <T> T compute(SharedOperationKey<T> key) {
        return key.compute(this);
    }

    private static TextureAtlasSprite loadAtlasSprite(String texture) throws IOException {
        Identifier texId = PlayerAnimLibMod.id("dynamic/texture_" + TEXTURE_COUNTER.getAndIncrement());

        RenderSystem.assertOnRenderThread();
        NativeImage image = NativeImage.read(Base64.getDecoder().decode(texture));

        DynamicTexture dynTex = new DynamicTexture(texId::toString, image);
        Minecraft.getInstance().getTextureManager().register(texId, dynTex);

        int w = image.getWidth();
        int h = image.getHeight();
        SpriteContents contents = new SpriteContents(texId, new FrameSize(w, h), image);
        return new TextureAtlasSprite(texId, contents, w, h, 0, 0, 0);
    }
}
