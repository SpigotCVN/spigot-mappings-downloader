package io.github.spigotcvn.smdownloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.spigotcvn.smdownloader.io.HTTPNotOkException;
import io.github.spigotcvn.smdownloader.io.IOUtils;
import io.github.spigotcvn.smdownloader.json.BuildDataInfo;
import io.github.spigotcvn.smdownloader.json.VersionData;
import io.github.spigotcvn.smdownloader.mappings.MapUtil;
import io.github.spigotcvn.smdownloader.mappings.MappingFile;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.RefSpec;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SpigotMappingsDownloader implements AutoCloseable {
    private static final String VERSIONS_URL = "https://hub.spigotmc.org/versions/%s.json";
    private static final String BUILDDATA_REPO = "https://hub.spigotmc.org/stash/scm/spigot/builddata.git";

    private File buildDataDir;
    private String rev;
    private VersionData versionInfo;
    private Git gitClient;
    private String repo;
    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    /**
     * Creates a new SpigotMappingsDownloader object.
     * If you want to define a custom buildDataDir, use {@link #SpigotMappingsDownloader(File, String)}.
     * @param rev The version to download mappings for.
     */
    public SpigotMappingsDownloader(String rev) {
        this(null, rev, BUILDDATA_REPO);
    }

    /**
     * Creates a new SpigotMappingsDownloader object.
     * @param buildDataDir The directory to clone the builddata git repository to.
     *                     This is also the directory inside which all operations will be performed.
     *                     Defaults to ./builddata-{rev}
     * @param rev The version to download mappings for.
     */
    public SpigotMappingsDownloader(File buildDataDir, String rev) {
        this(buildDataDir, rev, BUILDDATA_REPO);
    }

    /**
     * Creates a new SpigotMappingsDownloader object.
     * @param buildDataDir The directory to clone the builddata git repository to.
     *                     This is also the directory inside which all operations will be performed.
     *                     Defaults to ./builddata-{rev}
     * @param rev The version to download mappings for.
     * @param repo The URL of the builddata git repository.
     */
    public SpigotMappingsDownloader(File buildDataDir, String rev, String repo) {
        this.buildDataDir = buildDataDir;
        if(buildDataDir == null) {
            this.buildDataDir = new File("builddata-" + rev);
        }
        this.rev = rev;
        this.versionInfo = null;
        this.gitClient = null;
        this.repo = repo;
    }

    /**
     * Checks if the specified version is valid.
     * @return True if the version is valid, false otherwise.
     */
    public boolean isVersionValid() {
        try {
            URL downloadUrl = new URL(String.format(VERSIONS_URL, rev));
            HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
            connection.setRequestMethod("GET");

            return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch(IOException e) {
            return false;
        }
    }

    /**
     * Checks if the specified version has mojang mappings.
     * @return True if the version has mojang mappings, false otherwise.
     */
    public boolean hasMojangMappings() {
        VersionData versionData = getVersionData();
        if(versionData == null) {
            return false;
        }

        File infoFile = new File(buildDataDir, "info.json");
        if(!infoFile.exists()) {
            return false;
        }

        BuildDataInfo buildDataInfo = gson.fromJson(IOUtils.readFromFile(infoFile), BuildDataInfo.class);
        return buildDataInfo.getMappingsUrl() != null;
    }

    /**
     * Downloads the version data for the specified version.
     * @return The version data object for the specified version.
     * @throws IllegalArgumentException If the version is invalid.#
     */
    public VersionData getVersionData() throws IllegalArgumentException {
        if(versionInfo == null) {
            URL downloadUrl;
            try {
                downloadUrl = new URL(String.format(VERSIONS_URL, rev));
            } catch(MalformedURLException ignored) {
                throw new IllegalArgumentException("Invalid version: " + rev);
            }

            ByteArrayOutputStream downloadedInfoStream = new ByteArrayOutputStream();
            try(InputStream inputStream = IOUtils.getDownloadinputStream(downloadUrl)) {
                IOUtils.downloadFile(inputStream, downloadedInfoStream);
            } catch(IOException e) {
                if(e instanceof HTTPNotOkException) {
                    HTTPNotOkException httpException = (HTTPNotOkException) e;
                    if(httpException.getErrorCode() == HttpURLConnection.HTTP_NOT_FOUND)
                        throw new IllegalArgumentException("Invalid version: " + rev);
                }
                e.printStackTrace();
                return null;
            }

            versionInfo = gson.fromJson(downloadedInfoStream.toString(), VersionData.class);
        }
        return versionInfo;
    }

    /**
     * Downloads spigot mappings for the specified version.
     * That is achieved by cloning the builddata git repository and checking out the specified version.
     * @param deleteIfExists If true, it will delete the existing builddata directory
     *                       and clone the repository again.
     * @return A list of mapping files for the specified version.
     */
    public List<MappingFile> downloadMappings(boolean deleteIfExists) {
        VersionData versionData = getVersionData();
        if(versionData == null) {
            return null;
        }

        File mappingsDir = new File(buildDataDir, "mappings");

        if(deleteIfExists) {
            if(buildDataDir.exists() && buildDataDir.isDirectory()) {
                IOUtils.deleteDirectory(buildDataDir);
            }
        }

        if(!buildDataDir.exists()) {
            pullBuildDataGit(versionData.getRefs().getBuildData());
        }

        File infoFile = new File(buildDataDir, "info.json");
        if(!infoFile.exists()) {
            return null;
        }

        String infoData = IOUtils.readFromFile(infoFile);

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

    /**
     * Downloads mojang mappings for the specified version.
     * That is achieved by cloning the builddata git repository and checking out the specified version.
     * The info.json file in the builddata git repo has the needed mappings url for that version.
     * @param deleteRepoIfExists If true, it will delete the existing builddata directory
     *                           and clone the repository again.
     * @return The downloaded mojang mapping file.
     */
    public MappingFile downloadMojangMappings(boolean deleteRepoIfExists) {
        VersionData versionData = getVersionData();
        if(versionData == null) {
            return null;
        }

        File infoFile = new File(buildDataDir, "info.json");
        if(!infoFile.exists()) {
            return null;
        }

        if(deleteRepoIfExists) {
            if (buildDataDir.exists() && buildDataDir.isDirectory()) {
                IOUtils.deleteDirectory(buildDataDir);
            }
        }

        if(!buildDataDir.exists()) {
            pullBuildDataGit(versionData.getRefs().getBuildData());
        }

        BuildDataInfo buildDataInfo = gson.fromJson(IOUtils.readFromFile(infoFile), BuildDataInfo.class);
        if(buildDataInfo.getMappingsUrl() == null) throw new IllegalArgumentException("No mojang mappings available for version " + rev);

        File mojmaps = new File(buildDataDir, "mojmaps.txt");
        URL url;
        try {
            url = new URL(buildDataInfo.getMappingsUrl());
            IOUtils.downloadFile(url.openStream(), mojmaps);

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

    /**
     * Generates combined mappings for the specified version.
     * Combined mappings are mappings that contain both class and member mappings.
     * Usually used for remapping.
     * @param deleteRepoIfExists If true, it will delete the existing builddata directory
     * @return The generated combined mapping file.
     */
    public MappingFile generateCombinedMappings(boolean deleteRepoIfExists) {
        VersionData versionData = getVersionData();
        if(versionData == null) {
            return null;
        }

        MapUtil mapUtil = new MapUtil();

        List<MappingFile> mappings = downloadMappings(deleteRepoIfExists);
        if(mappings == null) {
            return null;
        }

        MappingFile classMappings = mappings.stream()
                .filter(m -> m.getType() == MappingFile.MappingType.CLASS)
                .findFirst()
                .orElse(null);
        if(classMappings == null) {
            return null;
        }
        try {
            mapUtil.loadBuk(classMappings.getFile());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        MappingFile memberMappings = mappings.stream()
                .filter(m -> m.getType() == MappingFile.MappingType.MEMBERS)
                .findFirst()
                .orElse(null);
        if(memberMappings == null && hasMojangMappings()) {
            memberMappings = generateMemberMappings(mapUtil, deleteRepoIfExists);
        }
        if(memberMappings == null) {
            return null;
        }

        if(hasMojangMappings()) {
            generateFieldMappings(mapUtil, deleteRepoIfExists);
        }

        File combinedMappings = new File(buildDataDir, "spigot-" + rev + "-combined.csrg");

        try {
            mapUtil.makeCombinedMaps(combinedMappings, memberMappings.getFile());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new MappingFile(
                MappingFile.MappingType.COMBINED,
                combinedMappings
        );
    }

    /**
     * Generates member mappings for the specified version.
     * Member mappings are not shipped with the spigot mappings from some version, so we have to generate them.
     * @param deleteRepoIfExists If true, it will delete the existing builddata directory
     * @return The generated member mapping file.
     */
    public MappingFile generateMemberMappings(boolean deleteRepoIfExists) {
        MapUtil mapUtil = new MapUtil();
        List<MappingFile> mappings = downloadMappings(deleteRepoIfExists);
        if(mappings == null) {
            return null;
        }

        MappingFile classMappings = mappings.stream()
                .filter(m -> m.getType() == MappingFile.MappingType.CLASS)
                .findFirst()
                .orElse(null);
        if(classMappings == null) {
            return null;
        }

        return generateMemberMappings(mapUtil, deleteRepoIfExists);
    }

    /**
     * Generates field mappings for the specified version.
     * Field mappings are and have never been shipped with the spigot mappings, so we have to generate them.
     * @param deleteRepoIfExists If true, it will delete the existing builddata directory
     * @return The generated field mapping file.
     */
    public MappingFile generateFieldMappings(boolean deleteRepoIfExists) {
        MapUtil mapUtil = new MapUtil();
        List<MappingFile> mappings = downloadMappings(deleteRepoIfExists);
        if(mappings == null) {
            return null;
        }

        MappingFile classMappings = mappings.stream()
                .filter(m -> m.getType() == MappingFile.MappingType.CLASS)
                .findFirst()
                .orElse(null);
        if(classMappings == null) {
            return null;
        }

        try {
            mapUtil.loadBuk(classMappings.getFile());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return generateFieldMappings(mapUtil, deleteRepoIfExists);
    }

    /**
     * Generates member mappings for the specified version.
     * Member mappings are not shipped with the spigot mappings from some version, so we have to generate them.
     * @param mapUtil The map util object to use for generating the mappings,
     *                it is expected to have the class mappings loaded.
     * @param deleteRepoIfExists If true, it will delete the existing builddata directory
     * @return The generated member mapping file.
     */
    public MappingFile generateMemberMappings(MapUtil mapUtil, boolean deleteRepoIfExists) {
        VersionData versionData = getVersionData();
        if(versionData == null) {
            return null;
        }

        MappingFile mojmaps = downloadMojangMappings(deleteRepoIfExists);
        if(mojmaps == null) {
            return null;
        }

        List<MappingFile> mappings = downloadMappings(deleteRepoIfExists);
        if(mappings == null) {
            return null;
        }

        MappingFile memberMappings = mappings.stream()
                .filter(m -> m.getType() == MappingFile.MappingType.MEMBERS)
                .findFirst()
                .orElse(null);
        if(memberMappings == null) {
            try {
                File members = new File(buildDataDir, "spigot-" + rev + "-members.csrg");
                mapUtil.makeFieldMaps(mojmaps.getFile(), members, true);
                memberMappings = new MappingFile(
                        MappingFile.MappingType.MEMBERS,
                        members
                );
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Caught exception");
                return null;
            }
        }

        return memberMappings;
    }

    /**
     * Generates field mappings for the specified version.
     * Field mappings are and have never been shipped with the spigot mappings, so we have to generate them.
     * @param mapUtil The map util object to use for generating the mappings,
     *                it is expected to have the class mappings loaded.
     * @param deleteRepoIfExists If true, it will delete the existing builddata directory
     * @return The generated field mapping file.
     */
    public MappingFile generateFieldMappings(MapUtil mapUtil, boolean deleteRepoIfExists) {
        VersionData versionData = getVersionData();
        if(versionData == null) {
            return null;
        }

        List<MappingFile> mappings = downloadMappings(deleteRepoIfExists);
        if(mappings == null) {
            return null;
        }
        MappingFile mojmaps = downloadMojangMappings(deleteRepoIfExists);
        if(mojmaps == null) {
            return null;
        }

        File fieldMappings = new File(buildDataDir, "spigot-" + rev + "-fields.csrg");
        try {
            mapUtil.makeFieldMaps(mojmaps.getFile(), fieldMappings, false);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return new MappingFile(
                MappingFile.MappingType.FIELDS,
                fieldMappings
        );
    }

    /**
     * Clones the builddata git repository and checks out the specified revision hash.
     * The repository is cloned to the directory specified in the constructor.
     * @param revHash The revision hash to checkout.
     */
    public void pullBuildDataGit(String revHash) {
        if(gitClient == null) {
            try {
                this.gitClient = Git.cloneRepository()
                        .setURI(repo)
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

    public File getBuildDataDir() {
        return buildDataDir;
    }

    public String getRev() {
        return rev;
    }

    @Override
    public void close() throws Exception {
        if(gitClient != null) {
            gitClient.close();
        }
    }
}
