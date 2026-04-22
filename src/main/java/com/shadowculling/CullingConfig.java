package com.shadowculling;

/**
 * Holds the current culling-toggle state. Values are refreshed whenever
 * resources reload (game start, F3+T, resourcepack enable/disable) by
 * {@link CullingConfigLoader}. Both default to {@code false} so the mod is
 * a no-op unless an enabled resourcepack opts in.
 */
public final class CullingConfig {
    public static volatile boolean disableFrustumCulling = false;
    public static volatile boolean disableBackfaceCulling = false;

    private CullingConfig() {}
}
