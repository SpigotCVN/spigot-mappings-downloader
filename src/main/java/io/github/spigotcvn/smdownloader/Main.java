package io.github.spigotcvn.smdownloader;

import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Main {
    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.accepts("version").withRequiredArg().required();
        parser.accepts("spigot").withOptionalArg();
        parser.accepts("mojang").withOptionalArg();

        OptionSet options = parser.parse(args);
        String version = (String)options.valueOf("version");
        boolean generateSpigotMappings = options.has("spigot");
        boolean generateMojangMappings = options.has("mojang");

        SpigotMappingsDownloader mappinger = new SpigotMappingsDownloader(version);
        if(generateSpigotMappings) {
            System.out.println("Downloading Spigot mappings for version " + version);

            try {
                System.out.println(mappinger.getVersionData().toString());
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid version: " + version);
                return;
            }

            for (MappingFile file : mappinger.downloadMappings(false)) {
                if (!file.getFile().exists()) continue;
                System.out.println(file.getFileType());
                System.out.println(file.getType());
                System.out.println(file.getFile().getName());
                System.out.println();
            }
        }

        if(generateMojangMappings) {
            System.out.println("Downloading Mojang mappings for version " + version);
            MappingFile mojmaps = mappinger.downloadMojangMappings(false);
            if(mojmaps != null) {
                System.out.println(mojmaps.getFileType());
                System.out.println(mojmaps.getType());
                System.out.println(mojmaps.getFile().getName());
            } else {
                System.out.println("Mojmaps is null");
            }
        }
    }
}
