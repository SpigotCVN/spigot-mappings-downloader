package io.github.spigotcvn.smdownloader.io;

import java.io.IOException;

/**
 * Exception thrown when the HTTP response code is not in the 200 range.
 * This exception contains the HTTP response code, which can be retrieved using {@link #getErrorCode()}.
 * @see IOUtils#getDownloadinputStream(java.net.URL)
 */
public class HTTPNotOkException extends IOException {
    private int errorCode;

    public HTTPNotOkException(int errorCode) {
        super("HTTP response code was not OK: " + errorCode);
    }

    public int getErrorCode() {
        return errorCode;
    }
}
