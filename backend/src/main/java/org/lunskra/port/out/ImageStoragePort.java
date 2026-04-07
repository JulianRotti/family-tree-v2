package org.lunskra.port.out;

import java.io.InputStream;

public interface ImageStoragePort {

    /**
     * Uploads an image stream and returns the object key that identifies it in storage.
     *
     * @param stream      the image data
     * @param sizeInBytes the exact byte length of the stream, or -1 if unknown (will buffer in memory)
     * @return the object key to persist in the database
     */
    String uploadImage(InputStream stream, long sizeInBytes);

    /**
     * Generates a one-time presigned URL valid for 60 minutes.
     *
     * @param objectKey the key returned by {@link #uploadImage}
     * @return a time-limited URL the client can use to download the image
     */
    String generatePresignedUrl(String objectKey);

    /**
     * Deletes the stored image identified by the given object key.
     *
     * @param objectKey the key returned by {@link #uploadImage}
     */
    void deleteImage(String objectKey);
}
