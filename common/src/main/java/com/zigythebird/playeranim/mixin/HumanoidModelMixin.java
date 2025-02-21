package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IMutableModel;
import com.zigythebird.playeranim.accessors.IUpperPartHelper;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelMixin<T extends HumanoidRenderState> extends EntityModel<T> implements IMutableModel {
    @Final
    @Shadow
    public ModelPart rightArm;
    @Final
    @Shadow
    public ModelPart leftArm;

    @Unique
    private PlayerAnimManager playerAnimLib$animation = null;

    private HumanoidModelMixin(Void v, ModelPart modelPart) {
        super(modelPart);
    }

    @Inject(method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;Ljava/util/function/Function;)V", at = @At("RETURN"))
    private void initBend(ModelPart modelPart, Function<ResourceLocation, RenderType> function, CallbackInfo ci){
        ((IUpperPartHelper)rightArm).playerAnimLib$setUpperPart(true);
        ((IUpperPartHelper)leftArm).playerAnimLib$setUpperPart(true);
        ((IUpperPartHelper)head).playerAnimLib$setUpperPart(true);
        ((IUpperPartHelper)hat).playerAnimLib$setUpperPart(true);
    }

    @Override
    public void playerAnimLib$setAnimation(@Nullable PlayerAnimManager emoteSupplier){
        this.playerAnimLib$animation = emoteSupplier;
    }

    @Override
    public @Nullable PlayerAnimManager playerAnimLib$getAnimation() {
        return this.playerAnimLib$animation;
    }

    @Inject(method = "copyPropertiesTo", at = @At("RETURN"))
    private void copyMutatedAttributes(HumanoidModel<T> bipedEntityModel, CallbackInfo ci){
        ((IMutableModel) bipedEntityModel).playerAnimLib$setAnimation(playerAnimLib$animation);
    }

    @Final
    @Shadow public ModelPart body;

    @Shadow @Final public ModelPart head;

    @Shadow @Final public ModelPart hat;
}
