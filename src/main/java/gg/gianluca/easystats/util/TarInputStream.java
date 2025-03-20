package gg.gianluca.easystats.util;

import java.io.*;

public class TarInputStream extends InputStream {
    private final InputStream in;
    @SuppressWarnings("unused")
    private long bytesRead = 0;
    private TarEntry currentEntry;
    private long currentFileSize = 0;
    private long bytesReadInFile = 0;

    public TarInputStream(InputStream in) {
        this.in = in;
    }

    public TarEntry getNextEntry() throws IOException {
        if (currentEntry != null) {
            long bytesToSkip = currentFileSize - bytesReadInFile;
            // Skip to the end of the current file plus padding
            while (bytesToSkip > 0) {
                long skipped = in.skip(bytesToSkip);
                if (skipped <= 0) break;
                bytesToSkip -= skipped;
                bytesRead += skipped;
            }
            // Skip padding bytes
            long padding = (512 - (currentFileSize % 512)) % 512;
            while (padding > 0) {
                long skipped = in.skip(padding);
                if (skipped <= 0) break;
                padding -= skipped;
                bytesRead += skipped;
            }
        }

        byte[] header = new byte[512];
        int read = readFully(header);
        if (read == -1) return null;
        
        // Check for end of archive (empty header)
        boolean empty = true;
        for (byte b : header) {
            if (b != 0) {
                empty = false;
                break;
            }
        }
        if (empty) return null;

        String name = new String(header, 0, 100).trim();
        String size = new String(header, 124, 12).trim();
        currentFileSize = Long.parseLong(size, 8);
        bytesReadInFile = 0;
        currentEntry = new TarEntry(name);
        return currentEntry;
    }

    @Override
    public int read() throws IOException {
        if (bytesReadInFile >= currentFileSize) return -1;
        int b = in.read();
        if (b != -1) {
            bytesRead++;
            bytesReadInFile++;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (bytesReadInFile >= currentFileSize) return -1;
        len = (int) Math.min(len, currentFileSize - bytesReadInFile);
        int read = in.read(b, off, len);
        if (read != -1) {
            bytesRead += read;
            bytesReadInFile += read;
        }
        return read;
    }

    private int readFully(byte[] buffer) throws IOException {
        int read = 0;
        while (read < buffer.length) {
            int r = in.read(buffer, read, buffer.length - read);
            if (r == -1) {
                return read == 0 ? -1 : read;
            }
            read += r;
            bytesRead += r;
        }
        return read;
    }

    @Override
    public void close() throws IOException {
        in.close();
    }
} 