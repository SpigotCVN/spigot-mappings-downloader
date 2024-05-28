package io.github.cvn.spigotmappingsdownloader.json;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

public class VersionData {
    private final String name;
    private final String description;
    private final RefsData refs;
    private final int toolsVersion;
    private final int[] javaVersions;

    public VersionData(String name, String description, RefsData refs, int toolsVersion, int[] javaVersions) {
        this.name = name;
        this.description = description;
        this.refs = refs;
        this.toolsVersion = toolsVersion;
        this.javaVersions = javaVersions;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public RefsData getRefs() {
        return refs;
    }

    public int getToolsVersion() {
        return toolsVersion;
    }

    public int[] getJavaVersions() {
        return javaVersions;
    }

    @Override
    public String toString() {
        return "VersionData{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", refs=" + refs +
                ", toolsVersion=" + toolsVersion +
                ", javaVersions=" + Arrays.toString(javaVersions) +
                '}';
    }

    public static class RefsData {
        @SerializedName("BuildData")
        private final String buildData;
        @SerializedName("Bukkit")
        private final String bukkit;
        @SerializedName("CraftBukkit")
        private final String craftBukkit;
        @SerializedName("Spigot")
        private final String spigot;

        public RefsData(String buildData, String bukkit, String craftBukkit, String spigot) {
            this.buildData = buildData;
            this.bukkit = bukkit;
            this.craftBukkit = craftBukkit;
            this.spigot = spigot;
        }

        public String getBuildData() {
            return buildData; 
        }

        public String getBukkit() {
            return bukkit;
        }

        public String getCraftBukkit() {
            return craftBukkit;
        }

        public String getSpigot() {
            return spigot;
        }

        @Override
        public String toString() {
            return "RefsData{" +
                    "buildData='" + buildData + '\'' +
                    ", bukkit='" + bukkit + '\'' +
                    ", craftBukkit='" + craftBukkit + '\'' +
                    ", spigot='" + spigot + '\'' +
                    '}';
        }
    }
}
