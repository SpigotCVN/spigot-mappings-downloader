package io.github.spigotcvn.spigotmappingsdownloader;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        String version = "1.12.2";

        SpigotMappingsDownloader mappinger = new SpigotMappingsDownloader(version);
        System.out.println(mappinger.getVersionData().toString());

        File[] files = mappinger.downloadMappings(false);
        for(File file : files) {
            if(!file.exists()) continue;
            System.out.println(file.getName());
        }
    }
}
