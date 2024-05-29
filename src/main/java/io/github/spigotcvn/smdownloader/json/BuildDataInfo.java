package io.github.spigotcvn.smdownloader.json;

public class BuildDataInfo {
    private final String minecraftVersion;
    private final String serverUrl;
    private final String mappingsUrl;
    private final String minecraftHash;
    private final String accessTransforms;
    private final String classMappings;
    private final String memberMappings;
    private final String packageMappings;
    private final String classMapCommand;
    private final String memberMapCommand;
    private final String finalMapCommand;
    private final String decompileCommand;
    private final int toolsVersion;

    public BuildDataInfo(String minecraftVersion, String serverUrl, String mappingsUrl, String minecraftHash, String accessTransforms, String classMappings, String memberMappings, String packageMappings, String classMapCommand, String memberMapCommand, String finalMapCommand, String decompileCommand, int toolsVersion) {
        this.minecraftVersion = minecraftVersion;
        this.serverUrl = serverUrl;
        this.mappingsUrl = mappingsUrl;
        this.minecraftHash = minecraftHash;
        this.accessTransforms = accessTransforms;
        this.classMappings = classMappings;
        this.memberMappings = memberMappings;
        this.packageMappings = packageMappings;
        this.classMapCommand = classMapCommand;
        this.memberMapCommand = memberMapCommand;
        this.finalMapCommand = finalMapCommand;
        this.decompileCommand = decompileCommand;
        this.toolsVersion = toolsVersion;
    }

    public String getMinecraftVersion() {
        return minecraftVersion;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public String getMappingsUrl() {
        return mappingsUrl;
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

    public String getClassMapCommand() {
        return classMapCommand;
    }

    public String getMemberMapCommand() {
        return memberMapCommand;
    }

    public String getFinalMapCommand() {
        return finalMapCommand;
    }

    public String getDecompileCommand() {
        return decompileCommand;
    }

    public int getToolsVersion() {
        return toolsVersion;
    }
}
