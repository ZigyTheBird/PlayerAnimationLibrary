/*
 * MIT License
 *
 * Copyright (c) 2022 KosmX
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.zigythebird.playeranim.mixin;

import com.zigythebird.playeranim.accessors.IAnimatedPlayer;
import com.zigythebird.playeranim.animation.PlayerAnimManager;
import com.zigythebird.playeranim.animation.PlayerAnimationProcessor;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.AnimationProcessor;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
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
    private final AnimationProcessor playerAnimLib$animationProcessor = new PlayerAnimationProcessor((AbstractClientPlayer) (Object) this);

    @Unique
    private PlayerAnimManager playerAnimLib$createAnimationStack() {
        PlayerAnimManager manager = new PlayerAnimManager((AbstractClientPlayer)(Object)this);
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.prepareAnimations((AbstractClientPlayer)(Object) this, manager, playerAnimLib$modAnimationData);
        PlayerAnimationAccess.REGISTER_ANIMATION_EVENT.invoker().registerAnimation((AbstractClientPlayer)(Object) this, manager);
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

    @Inject(method = "tick", at = @At("TAIL"))
    private void tick(CallbackInfo ci) {
        this.playerAnimLib$animationProcessor.handleAnimations(0, true);
    }

    @Override
    public AnimationProcessor playerAnimLib$getAnimProcessor() {
        return this.playerAnimLib$animationProcessor;
    }
}
