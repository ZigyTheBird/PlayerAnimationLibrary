package com.zigythebird.playeranimcore.util;

import java.util.ServiceLoader;
import java.util.stream.Stream;

public class ServiceUtil {
    public static <T extends ActiveService> Stream<T> loadServices(Class<T> serviceClass) {
        ModuleLayer layer = serviceClass.getModule().getLayer(); // NeoForge compat?

        ServiceLoader<T> loader = layer == null ? ServiceLoader.load(serviceClass,
                serviceClass.getClassLoader()
        ) : ServiceLoader.load(layer, serviceClass);

        return loader.stream()
                .map(ServiceLoader.Provider::get)
                .filter(ActiveService::isActive);
    }

    public static <T extends ActiveService> T loadService(Class<T> serviceClass) {
        return ServiceUtil.loadServices(serviceClass).findAny().orElseThrow();
    }

    public interface ActiveService {
        boolean isActive();
    }
}
