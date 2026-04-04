package com.flowingsun.rebornfov.team;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.Team;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class TeamResolver {
    public static String resolveTeamId(ServerPlayer player) {
        String ftbTeam = resolveFtbTeam(player);
        if (ftbTeam != null && !ftbTeam.isBlank()) {
            return ftbTeam;
        }

        Team scoreboardTeam = player.getTeam();
        if (scoreboardTeam != null) {
            return "scoreboard:" + scoreboardTeam.getName();
        }

        UUID uuid = player.getUUID();
        return "player:" + uuid;
    }

    private static String resolveFtbTeam(ServerPlayer player) {
        if (!ModList.get().isLoaded("ftbteams")) {
            return null;
        }

        String[] apiClasses = {
                "dev.ftb.mods.ftbteams.api.FTBTeamsAPI",
                "dev.ftb.mods.ftbteams.FTBTeamsAPI"
        };
        for (String apiClassName : apiClasses) {
            try {
                Class<?> apiClass = Class.forName(apiClassName);
                Object apiInstance = null;
                for (Method method : apiClass.getMethods()) {
                    if (method.getParameterCount() == 0 && apiClass.isAssignableFrom(method.getReturnType())) {
                        apiInstance = method.invoke(null);
                        break;
                    }
                }
                Object source = apiInstance != null ? apiInstance : apiClass;
                for (Method method : apiClass.getMethods()) {
                    if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == UUID.class) {
                        Object team = method.invoke(source, player.getUUID());
                        String resolved = extractTeamId(team);
                        if (resolved != null) {
                            return "ftb:" + resolved;
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static String extractTeamId(Object team) {
        if (team == null) {
            return null;
        }
        for (String methodName : Arrays.asList("getId", "getShortName", "getName")) {
            try {
                Method method = team.getClass().getMethod(methodName);
                Object value = method.invoke(team);
                if (value != null) {
                    return Objects.toString(value);
                }
            } catch (Exception ignored) {
            }
        }
        return team.toString();
    }
}
