package io.github.spigotcvn.smdownloader;

import io.github.spigotcvn.smdownloader.mappings.MappingFile;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        OptionParser parser = new OptionParser();
        parser.allowsUnrecognizedOptions();

        parser.accepts("help").forHelp();
        parser.accepts("version").withRequiredArg().required();
        parser.accepts("spigot");
        parser.accepts("mojang");
        parser.accepts("generate-combined");
        parser.accepts("dir").withRequiredArg().defaultsTo("builddata-{rev}");

        OptionSet options = parser.parse(args);
        if(options.has("help")) {
            System.out.println("Usage: java -jar SpigotMappingDownloader.jar [options]");
            System.out.println("Options:");
            System.out.println("  --help               Show this help message");
            System.out.println("  --version <version>  The version of the mappings to download");
            System.out.println("  --spigot             Download Spigot mappings");
            System.out.println("  --mojang             Download Mojang mappings, will error if the version is not available");
            System.out.println("  --generate-combined  Generate combined spigot mappings");
            System.out.println("  --dir <dir>          The directory to download the mappings to");
            System.out.println("                       You can use {rev} to insert the version");
            System.out.println("                       Default: builddata-{rev}");
            System.exit(0);
        }

        String version = (String) options.valueOf("version");
        String dir = ((String) options.valueOf("dir")).replaceAll("\\{rev}", version);

        boolean downloadSpigotMappings = options.has("spigot");
        boolean downloadMojangMappings = options.has("mojang");
        boolean generateCombined = options.has("generate-combined");

        SpigotMappingsDownloader mappinger = new SpigotMappingsDownloader(new File(dir), version);
        if(downloadSpigotMappings) {
            System.out.println("Downloading Spigot mappings for version " + version);

            if(!mappinger.isVersionValid()) {
                throw new IllegalArgumentException("Invalid version: " + version);
            }

            for (MappingFile file : mappinger.downloadMappings(false)) {
                if (!file.getFile().exists()) continue;
                System.out.println(file.getFileType());
                System.out.println(file.getType());
                System.out.println(file.getFile().getName());
                System.out.println();
            }
        }

        if(downloadMojangMappings) {
            System.out.println("Downloading Mojang mappings for version " + version);

            if(!mappinger.hasMojangMappings()) {
                throw new IllegalArgumentException("Mojang mappings are not available for version: " + version);
            }

            MappingFile mojmaps = mappinger.downloadMojangMappings(false);
            if(mojmaps != null) {
                System.out.println(mojmaps.getFileType());
                System.out.println(mojmaps.getType());
                System.out.println(mojmaps.getFile().getName());
            } else {
                System.out.println("Mojmaps is null");
            }
        }
        System.out.println();

        if(generateCombined) {
            System.out.println("Generating combined mappings for version " + version);
            MappingFile combined = mappinger.generateCombinedMappings(false);
            if(combined != null) {
                System.out.println(combined.getFileType());
                System.out.println(combined.getType());
                System.out.println(combined.getFile().getName());
            } else {
                System.out.println("Combined is null");
            }
        }
        System.out.println();
    }
}
