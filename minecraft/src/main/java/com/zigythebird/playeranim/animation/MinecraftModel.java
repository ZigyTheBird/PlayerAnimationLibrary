package com.zigythebird.playeranim.animation;

import com.google.gson.JsonObject;
import com.zigythebird.playeranimcore.PlayerAnimLib;
import com.zigythebird.playeranimcore.bindings.PlatformModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.Material;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.MaterialBaker;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SpriteId;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;

public class MinecraftModel implements PlatformModel, ResolvedModel {
    private final BlockModel model;

    @Nullable
    private BlockModelPart bakedModel;
    private boolean bakingAttempted;

    public MinecraftModel(JsonObject obj) {
        this.model = BlockModel.GSON.fromJson(obj, BlockModel.class);
    }

    @Nullable
    public BlockModelPart getBakedModel() {
        if (!bakingAttempted) {
            bakingAttempted = true;
            try {
                bakedModel = bake();
            } catch (Exception e) {
                PlayerAnimLib.LOGGER.error("Failed to bake custom bone model", e);
            }
        }
        return bakedModel;
    }

    public void invalidate() {
        bakingAttempted = false;
        bakedModel = null;
    }

    private @NonNull BlockModelPart bake() {
        TextureAtlasSprite missingSprite = Minecraft.getInstance().getAtlasManager()
                .get(new SpriteId(TextureAtlas.LOCATION_BLOCKS, Identifier.withDefaultNamespace("missingno")));

        MaterialBaker materialBaker = new MaterialBaker() {
            @Override
            public Material.@NonNull Baked get(Material material, @NonNull ModelDebugName name) {
                SpriteId id = new SpriteId(TextureAtlas.LOCATION_BLOCKS, material.sprite());
                TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(id);
                return new Material.Baked(sprite, material.forceTranslucent());
            }

            @Override
            public Material.@NonNull Baked reportMissingReference(@NonNull String reference, @NonNull ModelDebugName name) {
                return new Material.Baked(missingSprite, false);
            }
        };

        ModelBaker baker = new ModelBaker() {
            @Override
            public @NonNull ResolvedModel getModel(@NonNull Identifier location) {
                return MinecraftModel.this;
            }

            @Override
            public @NonNull BlockModelPart missingBlockModelPart() {
                throw new UnsupportedOperationException();
            }

            @Override
            public @NonNull MaterialBaker materials() {
                return materialBaker;
            }

            @Override
            public @NonNull Interner interner() {
                return new Interner() {
                    @Override
                    public @NonNull Vector3fc vector(@NonNull Vector3fc vector) {
                        return vector;
                    }

                    @Override
                    public BakedQuad.@NonNull SpriteInfo spriteInfo(BakedQuad.@NonNull SpriteInfo sprite) {
                        return sprite;
                    }
                };
            }

            @Override
            public <T> T compute(SharedOperationKey<T> key) {
                return key.compute(this);
            }
        };

        TextureSlots slots = getTopTextureSlots();
        Material.Baked particle = resolveParticleMaterial(slots, baker);
        QuadCollection geometry = bakeTopGeometry(slots, baker, BlockModelRotation.IDENTITY);

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
}
