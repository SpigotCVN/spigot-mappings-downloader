package io.github.spigotcvn.spigotmappingsdownloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.spigotcvn.spigotmappingsdownloader.json.BuildDataInfo;
import io.github.spigotcvn.spigotmappingsdownloader.json.VersionData;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class SpigotMappingsDownloader {
    private static final String VERSIONS_URL = "https://hub.spigotmc.org/versions/%s.json";
    public static final String BUILDDATA_REPO = "https://hub.spigotmc.org/stash/scm/spigot/builddata.git";

    private String rev;
    private VersionData versionInfo;
    private Git gitClient;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    public SpigotMappingsDownloader(String rev) {
        this.rev = rev;
        this.versionInfo = null;
        this.gitClient = null;
    }

    public VersionData getVersionData() {
        if(versionInfo == null) {
            URL downloadUrl;
            try {
                downloadUrl = new URL(String.format(VERSIONS_URL, rev));
            } catch(MalformedURLException ignored) {
                throw new IllegalArgumentException("Invalid version: " + rev);
            }

            StringBuilder downloadedInfo = new StringBuilder();
            try(InputStream inputStream = downloadUrl.openStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while((bytesRead = inputStream.read(buffer)) != -1) {
                    downloadedInfo.append(new String(buffer).substring(0, bytesRead));
                }
            } catch(IOException e) {
                e.printStackTrace();
            }

            versionInfo = gson.fromJson(downloadedInfo.toString(), VersionData.class);
        }
        return versionInfo;
    }

    public MappingFile[] downloadMappings(boolean deleteIfExists) {
        VersionData versionData = getVersionData();
        if(versionData == null) {
            return null;
        }

        File buildDataDir = new File("./builddata-" + rev);
        File mappingsDir = new File(buildDataDir, "mappings");

        if(deleteIfExists) {
            if(buildDataDir.exists() && buildDataDir.isDirectory())
                deleteFiles(buildDataDir);
        } else {
            if(buildDataDir.exists() && buildDataDir.isDirectory()) {
                String infoData = readFromFile(new File(buildDataDir, "info.json"));

                BuildDataInfo buildDataInfo = gson.fromJson(infoData, BuildDataInfo.class);
                return new MappingFile[] {
                    new MappingFile(MappingFile.MappingType.CLASS, new File(mappingsDir, buildDataInfo.getClassMappings())),
                    new MappingFile(MappingFile.MappingType.MEMBERS, new File(mappingsDir, buildDataInfo.getMemberMappings())),
                    new MappingFile(MappingFile.MappingType.PACKAGE, new File(mappingsDir, buildDataInfo.getPackageMappings()))
                };
            }
        }

        if(gitClient == null) {
            try {
                this.gitClient = Git.cloneRepository()
                        .setURI(BUILDDATA_REPO)
                        .setDirectory(buildDataDir)
                        .call();
            } catch (GitAPIException e) {
                e.printStackTrace();
                return null;
            }
        }

        String buildDataRevHash = versionData.getRefs().getBuildData();
        try {
            gitClient.fetch()
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/origin/*"))
                    .call();
            gitClient.checkout()
                    .setName(buildDataRevHash)
                    .call();
        } catch(Exception e) {
            e.printStackTrace();
        }

        File infoFile = new File(buildDataDir, "info.json");
        if(!infoFile.exists()) {
            return null;
        }

        String infoData = readFromFile(infoFile);

        BuildDataInfo buildDataInfo = gson.fromJson(infoData, BuildDataInfo.class);

        MappingFile[] mappings = new MappingFile[] {
            new MappingFile(MappingFile.MappingFileType.CSRG, MappingFile.MappingType.CLASS, new File(mappingsDir, buildDataInfo.getClassMappings())),              
            new MappingFile(MappingFile.MappingFileType.CSRG, MappingFile.MappingType.MEMBERS, new File(
mappingsDir, buildDataInfo.getMemberMappings())),
            new MappingFile(MappingFile.MappingFileType.CSRG, MappingFile.MappingType.PACKAGE, new File(mappingsDir, buildDataInfo.getPackageMappings()))
        };
        return mappings;
    }

    private void deleteFiles(File file) {
        if(file.isDirectory()) {
            for(File f : file.listFiles()) {
                deleteFiles(f);
            }
        }
        file.delete();
    }

    private String readFromFile(File file) {
        String infoData;
        try(BufferedInputStream infoStream = new BufferedInputStream(new FileInputStream(file))) {
            infoData = new String(infoStream.readAllBytes());
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        return infoData;
    }
}
