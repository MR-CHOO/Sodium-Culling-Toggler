package com.shadowculling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Reads {@code assets/sodiumcoreshadersupport/versions.json} from the
 * highest-priority enabled resourcepack and updates {@link CullingConfig}
 * whenever resources reload.
 *
 * <p>Recognized top-level boolean fields (either is optional; default false):
 * <ul>
 *   <li>{@code "disable-frustum-culling"} — short-circuits Sodium's
 *       {@code Viewport.isBoxVisible} to always return {@code true}.</li>
 *   <li>{@code "disable-backface-culling"} — forces Sodium's
 *       {@code DefaultChunkRenderer.fillCommandBuffer} down the
 *       {@code ModelQuadFacing.ALL} branch (equivalent to turning
 *       "Use Block Face Culling" off in Sodium's settings).</li>
 * </ul>
 *
 * <p>The file lives in the {@code sodiumcoreshadersupport} namespace so a
 * single resourcepack can configure both this mod and
 * <a href="https://github.com/lni-dev/SodiumCoreShaderSupport">SodiumCoreShaderSupport</a>
 * from one manifest.
 */
public final class CullingConfigLoader implements ClientModInitializer {

    private static final Identifier LISTENER_ID =
        Identifier.of("sodium-frustum-test", "culling_config");
    private static final Identifier VERSIONS_JSON =
        Identifier.of("sodiumcoreshadersupport", "versions.json");

    @Override
    public void onInitializeClient() {
        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
            .registerReloadListener(new SimpleSynchronousResourceReloadListener() {
                @Override
                public Identifier getFabricId() {
                    return LISTENER_ID;
                }

                @Override
                public void reload(ResourceManager manager) {
                    reloadConfig(manager);
                }
            });
    }

    private static void reloadConfig(ResourceManager manager) {
        // Reset to defaults — if no pack defines the file, the mod is a no-op.
        boolean frustum = false;
        boolean backface = false;

        var resource = manager.getResource(VERSIONS_JSON);
        if (resource.isPresent()) {
            try (var reader = new InputStreamReader(
                    resource.get().getInputStream(), StandardCharsets.UTF_8)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (parsed.isJsonObject()) {
                    JsonObject obj = parsed.getAsJsonObject();
                    frustum = readBool(obj, "disable-frustum-culling", false);
                    backface = readBool(obj, "disable-backface-culling", false);
                }
            } catch (Exception e) {
                System.err.println("[sodium-frustum-test] Failed to read "
                    + VERSIONS_JSON + ": " + e);
            }
        }

        CullingConfig.disableFrustumCulling = frustum;
        CullingConfig.disableBackfaceCulling = backface;
    }

    private static boolean readBool(JsonObject obj, String key, boolean fallback) {
        JsonElement el = obj.get(key);
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isBoolean()) {
            return el.getAsBoolean();
        }
        return fallback;
    }
}
