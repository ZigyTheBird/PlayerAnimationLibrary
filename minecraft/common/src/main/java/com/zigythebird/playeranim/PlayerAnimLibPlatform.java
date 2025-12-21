package com.zigythebird.playeranim;

import java.util.ServiceLoader;
import java.util.stream.Stream;

@SuppressWarnings("unused") // api
public interface PlayerAnimLibPlatform {
    PlayerAnimLibPlatform INSTANCE = loadServices(PlayerAnimLibPlatform.class).findAny().orElseThrow();

    boolean isModLoaded(String id);

    static <T> Stream<T> loadServices(Class<T> serviceClass) {
        ModuleLayer layer = serviceClass.getModule().getLayer(); // NeoForge compat?
        ServiceLoader<T> loader = layer == null ? ServiceLoader.load(serviceClass,
                serviceClass.getClassLoader()
        ) : ServiceLoader.load(layer, serviceClass);
        return loader.stream().map(ServiceLoader.Provider::get);
    }
}
