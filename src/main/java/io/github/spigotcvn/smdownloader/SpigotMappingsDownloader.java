package io.github.spigotcvn.smdownloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.spigotcvn.smdownloader.json.BuildDataInfo;
import io.github.spigotcvn.smdownloader.json.VersionData;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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

    public VersionData getVersionData() throws IllegalArgumentException {
        if(versionInfo == null) {
            URL downloadUrl;
            try {
                downloadUrl = new URL(String.format(VERSIONS_URL, rev));
            } catch(MalformedURLException ignored) {
                throw new IllegalArgumentException("Invalid version: " + rev);
            }

            ByteArrayOutputStream downloadedInfoStream = new ByteArrayOutputStream();
            try(InputStream inputStream = getVersionDownloadInputStream(downloadUrl)) {
                downloadFile(inputStream, downloadedInfoStream);
            } catch(IOException e) {
                e.printStackTrace();
            }

            versionInfo = gson.fromJson(downloadedInfoStream.toString(), VersionData.class);
        }
        return versionInfo;
    }

    public List<MappingFile> downloadMappings(boolean deleteIfExists) {
        VersionData versionData = getVersionData();
        if(versionData == null) {
            return null;
        }

        File buildDataDir = new File("./builddata-" + rev);
        File mappingsDir = new File(buildDataDir, "mappings");

        if(deleteIfExists) {
            if(buildDataDir.exists() && buildDataDir.isDirectory()) {
                deleteFiles(buildDataDir);
            }
        }

        if(!buildDataDir.exists()) {
            pullBuildDataGit(buildDataDir, versionData.getRefs().getBuildData());
        }

        File infoFile = new File(buildDataDir, "info.json");
        if(!infoFile.exists()) {
            return null;
        }

        String infoData = readFromFile(infoFile);

        BuildDataInfo buildDataInfo = gson.fromJson(infoData, BuildDataInfo.class);
        List<MappingFile> mappingFiles = new ArrayList<>();
        if(buildDataInfo.getClassMappings() != null) {
            mappingFiles.add(new MappingFile(MappingFile.MappingType.CLASS,
                    new File(mappingsDir, buildDataInfo.getClassMappings())));
        }
        if(buildDataInfo.getMemberMappings() != null) {
            mappingFiles.add(new MappingFile(MappingFile.MappingType.MEMBERS,
                    new File(mappingsDir, buildDataInfo.getMemberMappings())));
        }
        if(buildDataInfo.getPackageMappings() != null) {
            mappingFiles.add(new MappingFile(MappingFile.MappingType.PACKAGE,
                    new File(mappingsDir, buildDataInfo.getPackageMappings())));
        }
        return mappingFiles;
    }

    public MappingFile downloadMojangMappings(boolean deleteRepoIfExists) {
        VersionData versionData = getVersionData();
        if(versionData == null) {
            return null;
        }

        File buildDataDir = new File("./builddata-" + rev);
        File infoFile = new File(buildDataDir, "info.json");
        if(!infoFile.exists()) {
            return null;
        }

        if(deleteRepoIfExists) {
            if (buildDataDir.exists() && buildDataDir.isDirectory()) {
                deleteFiles(buildDataDir);
            }
        }

        if(!buildDataDir.exists()) {
            pullBuildDataGit(buildDataDir, versionData.getRefs().getBuildData());
        }

        BuildDataInfo buildDataInfo = gson.fromJson(readFromFile(infoFile), BuildDataInfo.class);
        if(buildDataInfo.getMappingsUrl() == null) throw new IllegalArgumentException("No mojang mappings available for version " + rev);

        File mojmaps = new File(buildDataDir, "mojmaps.txt");
        URL url;
        try {
            url = new URL(buildDataInfo.getMappingsUrl());
            downloadFile(url.openStream(), mojmaps);

            return new MappingFile(
                    MappingFile.MappingFileType.PROGUARD,
                    MappingFile.MappingType.COMBINED,
                    mojmaps
            );
        } catch (IOException e) {
            System.out.println("Was unable to download mojmaps");
            e.printStackTrace();
        }
        return null;
    }

    public void pullBuildDataGit(File buildDataDir, String revHash) {
        if(gitClient == null) {
            try {
                this.gitClient = Git.cloneRepository()
                        .setURI(BUILDDATA_REPO)
                        .setDirectory(buildDataDir)
                        .call();
            } catch (GitAPIException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            gitClient.fetch()
                    .setRemote("origin")
                    .setRefSpecs(new RefSpec("+refs/heads/*:refs/remotes/origin/*"))
                    .call();
            gitClient.checkout()
                    .setName(revHash)
                    .call();
        } catch(Exception e) {
            e.printStackTrace();
        }
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
            byte[] buffer = new byte[infoStream.available()];
            infoStream.read(buffer);
            infoData = new String(buffer);
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
        return infoData;
    }

    private InputStream getVersionDownloadInputStream(URL downloadUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if(responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
            throw new IllegalArgumentException("Invalid version: " + rev);
        } else if(responseCode != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download version info: " + responseCode);
        }

        return connection.getInputStream();
    }

    private void downloadFile(InputStream input, File result) {
        try(OutputStream output = new FileOutputStream(result)) {
            downloadFile(input, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void downloadFile(InputStream input, OutputStream output) {
        try {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
