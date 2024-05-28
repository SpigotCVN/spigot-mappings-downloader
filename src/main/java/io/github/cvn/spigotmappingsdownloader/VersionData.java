package io.github.cvn.spigotmappingsdownloader;

import com.google.gson.annotations.SerializedName;

public class VersionData {
    private String name;
    private String description;
    private RefsData refs;
    private int toolsVersion;
    private int[] javaVersions;

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

    public static class RefsData {
        @SerializedName("BuildData")
        private String buildData;
        @SerializedName("Bukkit")
        private String bukkit;
        @SerializedName("CraftBukkit")
        private String craftBukkit;
        @SerializedName("Spigot")
        private String spigot;

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
    }
}
