package io.github.spigotcvn.smdownloader.io;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class IOUtils {
    /**
     * Deletes a directory and all its contents.
     * @param file the directory to delete
     */
    public static void deleteDirectory(File file) {
        if(file.isDirectory()) {
            for(File f : file.listFiles()) {
                deleteDirectory(f);
            }
        }
        file.delete();
    }

    /**
     * Reads the contents of a file into a string.
     * @param file the file to read
     * @return the contents of the file as a string
     */
    public static String readFromFile(File file) {
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

    /**
     * Opens an input stream from a URL.
     * Will throw an {@link HTTPNotOkException} if the response code is not in the 200 range.
     * @param downloadUrl the URL to open the input stream from
     * @return the input stream
     * @throws IOException if an I/O error occurs
     * @throws HTTPNotOkException if the response code is not in the 200 range
     */
    public static InputStream getDownloadinputStream(URL downloadUrl) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) downloadUrl.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        // check whether it's in the 200 range
        if(responseCode < 200 || responseCode >= 300) {
            throw new HTTPNotOkException(responseCode);
        }

        return connection.getInputStream();
    }

    /**
     * Downloads a file from an input stream to a file.
     * @param input the input stream to download from
     * @param result the file to download to
     */
    public static void downloadFile(InputStream input, File result) {
        try(OutputStream output = new FileOutputStream(result)) {
            downloadFile(input, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Redirects an input stream to an output stream.
     * Called downloadFile because it's used to download files here.
     * @param input the input stream to read from
     * @param output the output stream to write to
     */
    public static void downloadFile(InputStream input, OutputStream output) {
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
