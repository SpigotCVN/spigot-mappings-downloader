package io.github.cvn.spigotmappingsdownloader;

import java.net.MalformedURLException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class MappingsDownloader {
    private static final String VERSIONS_URL = "https://hub.spigotmc.org/versions/%s.json";
    public static final String BUILDDATA_REPO = "https://hub.spigotmc.org/stash/scm/spigot/builddata.git";

    private String rev;
    private VersionData versionInfo;

    public MappingsDownloader(String rev) {
        this.rev = rev;
        this.versionInfo = null;
    }

    public VersionData getVersionData() {
        if(versionInfo == null) {
            URL downloadUrl;
            try {
                downloadUrl = new URL(String.format(VERSIONS_URL, rev));
            } catch(MalformedURLException ignored) {
                return null;
            }

            String downloadedInfo = "";
            try(InputStream inputStream = downloadUrl.openStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;

                while((bytesRead = inputStream.read(buffer)) != -1) {
                    downloadedInfo += new String(buffer).substring(0, bytesRead);
                }
            } catch(IOException e) {
                e.printStackTrace();
            }
            System.out.println(downloadedInfo);
        }
        return versionInfo;
    }
}
