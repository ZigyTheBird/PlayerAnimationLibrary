package com.zigythebird.playeranim.animation;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zigythebird.playeranim.PlayerAnimLibMod;
import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.bindings.PlatformModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.Material;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.MaterialBaker;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MinecraftModel implements PlatformModel, ResolvedModel, MaterialBaker, ModelBaker, ModelBaker.Interner {
    private static final AtomicInteger TEXTURE_COUNTER = new AtomicInteger(0);

    private final Map<Identifier, TextureAtlasSprite> sprites = new HashMap<>();

    private final BlockModel model;
    @Nullable
    private final RenderType renderType;
    @Nullable
    private BlockModelPart bakedModel;

    public MinecraftModel(JsonObject obj) {
        obj.remove("display");
        obj.remove("gui_light");

        Identifier primaryTextureId = null;

        if (obj.has("textures") && obj.get("textures").isJsonObject()) {
            JsonObject textures = obj.getAsJsonObject("textures").deepCopy();
            obj.add("textures", textures);

            for (Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                String value = entry.getValue().getAsString();
                if (value.startsWith("#")) continue;

                Identifier texId = PlayerAnimLibMod.id("dynamic/texture_" + TEXTURE_COUNTER.getAndIncrement());

                try {
                    RenderSystem.assertOnRenderThread();

                    NativeImage image = NativeImage.read(Base64.getDecoder().decode(value));

                    int w = image.getWidth();
                    int h = image.getHeight();

                    DynamicTexture dynTex = new DynamicTexture(texId::toString, image);
                    Minecraft.getInstance().getTextureManager().register(texId, dynTex);

                    SpriteContents contents = new SpriteContents(texId, new FrameSize(w, h), image);
                    this.sprites.put(texId, new TextureAtlasSprite(texId, contents, w, h, 0, 0, 0));
                } catch (Exception e) {
                    PlayerAnimLib.LOGGER.error("Failed to load texture for bone model", e);
                }

                entry.setValue(new JsonPrimitive(texId.toString()));
                if (primaryTextureId == null) primaryTextureId = texId;
            }
        }

        this.model = BlockModel.GSON.fromJson(obj, BlockModel.class);
        this.renderType = primaryTextureId != null ? RenderTypes.entityCutoutCull(primaryTextureId) : null;
    }

    @Nullable
    public BlockModelPart getBakedModel() {
        if (this.bakedModel == null && !this.sprites.isEmpty()) {
            try {
                this.bakedModel = bake();
            } catch (Exception e) {
                PlayerAnimLib.LOGGER.error("Failed to bake custom bone model", e);
            }
        }
        return this.bakedModel;
    }

    @Nullable
    public RenderType getRenderType() {
        return this.renderType;
    }

    @Override
    public void invalidate() {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        for (Identifier texId : this.sprites.keySet()) {
            textureManager.release(texId);
        }
        this.sprites.clear();
        this.bakedModel = null;
    }

    private @NonNull BlockModelPart bake() {
        TextureSlots slots = getTopTextureSlots();

        Material.Baked particle = resolveParticleMaterial(slots, this);
        QuadCollection geometry = bakeTopGeometry(slots, this, BlockModelRotation.IDENTITY);

        boolean translucent = false;
        for (BakedQuad quad : geometry.getAll()) {
            if (quad.spriteInfo().layer().translucent()) {
                translucent = true;
                break;
            }
        }

        return new SimpleModelWrapper(geometry, getTopAmbientOcclusion(), particle, translucent);
    }

    @Override
    public @NonNull UnbakedModel wrapped() {
        return this.model;
    }

    @Override
    public @Nullable ResolvedModel parent() {
        return null;
    }

    @Override
    public @NonNull String debugName() {
        return "playeranim:custom_bone";
    }

    @Override
    public Material.@NonNull Baked get(Material material, @NonNull ModelDebugName name) {
        TextureAtlasSprite sprite = this.sprites.get(material.sprite());
        if (sprite == null && !this.sprites.isEmpty()) {
            PlayerAnimLib.LOGGER.warn("Unknown texture: {} in {}", material.sprite(), name.debugName());
            sprite = this.sprites.values().iterator().next();
        }
        return new Material.Baked(sprite, material.forceTranslucent());
    }

    @Override
    public Material.@NonNull Baked reportMissingReference(@NonNull String reference, @NonNull ModelDebugName name) {
        TextureAtlasSprite sprite = this.sprites.isEmpty() ? null : this.sprites.values().iterator().next();
        return new Material.Baked(sprite, false);
    }

    @Override
    public @NonNull ResolvedModel getModel(@NonNull Identifier location) {
        return this;
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
}
