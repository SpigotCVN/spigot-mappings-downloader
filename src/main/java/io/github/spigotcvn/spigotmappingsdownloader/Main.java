package io.github.spigotcvn.spigotmappingsdownloader;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        String version = "1.12.2";

        SpigotMappingsDownloader mappinger = new SpigotMappingsDownloader(version);
        System.out.println(mappinger.getVersionData().toString());

        MappingFile[] files = mappinger.downloadMappings(false);
        for(MappingFile file : files) {
            if(!file.getFile().exists()) continue;
            System.out.println(file.getFileType());
            System.out.println(file.getType());
            System.out.println(file.getFile().getName());
            System.out.println();
        }
    }
}
