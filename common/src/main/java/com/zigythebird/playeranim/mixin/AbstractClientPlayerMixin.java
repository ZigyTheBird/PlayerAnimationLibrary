package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.ModInit;
import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.AnimationProcessor;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.animation.layered.IAnimation;
import com.zigythebird.playeranim.api.AnimationRegisterEvent;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin implements IAnimatedPlayer {
    @Unique
    private final Map<ResourceLocation, IAnimation> playerAnimLib$modAnimationData = new HashMap<>();
    @Unique
    private final PlayerAnimManager playerAnimLib$animationManager = playerAnimLib$createAnimationStack();
    @Unique
    private final AnimationProcessor playerAnimLib$animationProcessor = new AnimationProcessor((AbstractClientPlayer) (Object) this);

    @Unique
    private PlayerAnimManager playerAnimLib$createAnimationStack() {
        PlayerAnimManager manager = new PlayerAnimManager((AbstractClientPlayer)(Object)this);
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.prepareAnimations((AbstractClientPlayer)(Object) this, manager, playerAnimLib$modAnimationData);
        ModInit.EVENT_BUS.post(new AnimationRegisterEvent((AbstractClientPlayer)(Object) this, manager));
        return manager;
    }

    @Override
    public PlayerAnimManager playerAnimLib$getAnimManager() {
        return playerAnimLib$animationManager;
    }

    @Override
    public IAnimation playerAnimLib$getAnimation(ResourceLocation id) {
        if (playerAnimLib$modAnimationData.containsKey(id)) return playerAnimLib$modAnimationData.get(id);
        return null;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        this.playerAnimLib$animationProcessor.handleAnimations(1, true);
    }

    @Override
    public AnimationProcessor playerAnimLib$getAnimProcessor() {
        return this.playerAnimLib$animationProcessor;
    }
}
