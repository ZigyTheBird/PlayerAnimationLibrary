package com.zigythebird.playeranim.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.zigythebird.playeranim.animation.PlayerAnimResources;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class AnimationArgumentProvider implements SuggestionProvider<CommandSourceStack> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        List<String> suggestions = new LinkedList<>();
        for (ResourceLocation animation : PlayerAnimResources.getAnimations().keySet()) {
            suggestions.add(animation.toString()); // TODO by names
        }
        return SharedSuggestionProvider.suggest(suggestions.toArray(String[]::new), builder);
    }
}
