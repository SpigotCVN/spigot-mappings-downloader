package io.github.spigotcvn.spigotmappingsdownloader.json;

public class BuildDataInfo {
    private final String minecraftVersion;
    private final String serverUrl;
    private final String minecraftHash;
    private final String accessTransforms;
    private final String classMappings;
    private final String memberMappings;
    private final String packageMappings;
    private final String decompileCommand;

    public BuildDataInfo(String minecraftVersion, String serverUrl, String minecraftHash, String accessTransforms, String classMappings, String memberMappings, String packageMappings, String decompileCommand) {
        this.minecraftVersion = minecraftVersion;
        this.serverUrl = serverUrl;
        this.minecraftHash = minecraftHash;
        this.accessTransforms = accessTransforms;
        this.classMappings = classMappings;
        this.memberMappings = memberMappings;
        this.packageMappings = packageMappings;
        this.decompileCommand = decompileCommand;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getMinecraftHash() {
        return minecraftHash;
    }

    public String getAccessTransforms() {
        return accessTransforms;
    }

    public String getClassMappings() {
        return classMappings;
    }

    public String getMemberMappings() {
        return memberMappings;
    }

    public String getPackageMappings() {
        return packageMappings;
    }

    public String getDecompileCommand() {
        return decompileCommand;
    }
}
