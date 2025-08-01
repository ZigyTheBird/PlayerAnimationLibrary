package com.zigythebird.playeranim;

import org.jetbrains.annotations.ApiStatus;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

@ApiStatus.Internal
public class ModMixinPlugin implements IMixinConfigPlugin {
    public static final boolean IS_BC_INSTALLED = PlayerAnimLibPlatform.isModLoaded("bendable_cuboids");

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return !IS_BC_INSTALLED || !mixinClassName.endsWith("_noBend");
    }

    // Boilerplate

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
