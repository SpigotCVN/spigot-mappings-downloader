package io.github.cvn.spigotmappingsdownloader;

public class Main {
    public static void main(String[] args) {
        String version = "1.12.2";

        MappingsDownloader mappinger = new MappingsDownloader(version);
        mappinger.getVersionData();
    }
}
