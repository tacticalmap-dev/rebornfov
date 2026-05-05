package com.flowingsun.rebornfov.data;

import com.flowingsun.rebornfov.RebornFovMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RebornFovSavedData extends SavedData {
    private final Map<String, Map<String, TeleportTarget>> teamTargets = new HashMap<>();

    public static RebornFovSavedData get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(RebornFovSavedData::load, RebornFovSavedData::new, RebornFovMod.MOD_ID + "_data");
    }

    public void putTarget(TeleportTarget target) {
        teamTargets.computeIfAbsent(target.teamId(), key -> new LinkedHashMap<>()).put(target.id(), target);
        setDirty();
    }

    public void removeTarget(String teamId, String targetId) {
        Map<String, TeleportTarget> targets = teamTargets.get(teamId);
        if (targets != null && targets.remove(targetId) != null) {
            setDirty();
        }
    }

    public void removeTargetFromAllTeams(String targetId) {
        boolean changed = false;
        for (Map<String, TeleportTarget> targets : teamTargets.values()) {
            if (targets.remove(targetId) != null) {
                changed = true;
            }
        }
        if (changed) {
            setDirty();
        }
    }

    public List<TeleportTarget> getTargets(String teamId) {
        return new ArrayList<>(teamTargets.getOrDefault(teamId, Map.of()).values());
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag teamsTag = new ListTag();
        for (Map.Entry<String, Map<String, TeleportTarget>> entry : teamTargets.entrySet()) {
            CompoundTag teamTag = new CompoundTag();
            teamTag.putString("teamId", entry.getKey());
            ListTag targetsTag = new ListTag();
            for (TeleportTarget target : entry.getValue().values()) {
                targetsTag.add(target.save());
            }
            teamTag.put("targets", targetsTag);
            teamsTag.add(teamTag);
        }
        tag.put("teams", teamsTag);
        return tag;
    }

    public static RebornFovSavedData load(CompoundTag tag) {
        RebornFovSavedData data = new RebornFovSavedData();
        ListTag teamsTag = tag.getList("teams", Tag.TAG_COMPOUND);
        for (Tag teamEntry : teamsTag) {
            CompoundTag teamTag = (CompoundTag) teamEntry;
            String teamId = teamTag.getString("teamId");
            Map<String, TeleportTarget> targets = data.teamTargets.computeIfAbsent(teamId, key -> new LinkedHashMap<>());
            ListTag targetsTag = teamTag.getList("targets", Tag.TAG_COMPOUND);
            for (Tag targetEntry : targetsTag) {
                TeleportTarget target = TeleportTarget.load((CompoundTag) targetEntry);
                targets.put(target.id(), target);
            }
        }
        return data;
    }
}
