package com.zigythebird.playeranimcore.molang;

import com.zigythebird.playeranimcore.easing.EasingType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import team.unnamed.mocha.runtime.binding.Binding;
import team.unnamed.mocha.runtime.value.ObjectProperty;
import team.unnamed.mocha.runtime.value.ObjectValue;
import team.unnamed.mocha.runtime.value.Value;
import team.unnamed.mocha.util.CaseInsensitiveStringHashMap;

import java.util.Locale;
import java.util.Map;

@Binding("math")
public class MochaMathExtensions implements ObjectValue {
    private final Map<String, ObjectProperty> entries = new CaseInsensitiveStringHashMap<>();

    @Nullable
    private final ObjectValue mochaMath;

    public MochaMathExtensions(@Nullable ObjectProperty property) {
        this((ObjectValue) property.value());
    }

    public MochaMathExtensions(@Nullable ObjectValue mochaMath) {
        this.mochaMath = mochaMath;

        for (EasingType type : EasingType.values()) {
            String name = type.name().toLowerCase(Locale.ROOT);
            if (!name.startsWith("ease_")) continue;
            setFunction(name, type);
        }
    }

    @Override
    public boolean set(@NotNull String name, @Nullable Value value) {
        return this.entries.put(name, ObjectProperty.property(value, false)) == null;
    }

    @Override
    public @Nullable ObjectProperty getProperty(final @NotNull String name) {
        ObjectProperty extension = this.entries.get(name);
        if (extension == null && this.mochaMath != null) {
            return this.mochaMath.getProperty(name);
        }
        return extension;
    }
}
