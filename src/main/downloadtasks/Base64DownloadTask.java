package main.downloadtasks;

import java.io.*;
import java.util.Base64;

public class Base64DownloadTask extends DownloadTask {

    private String base64;
    private int bufferSize = 1024;

    @Override
    public DownloadTask copyTask() {
        return new Base64DownloadTask(this.base64, this.localFile);
    }

    public Base64DownloadTask(String base64, File localFile) {
        this.base64 = base64;
        this.localFile = localFile;
        setDownloadedAt();
        addStateChangeListener();
    };

    @Override
    protected File call() throws Exception {
        base64 = base64.split("base64,")[1];
        // if(base64.startsWith("9j")) base64 = base64.split("9j/")[1];
        System.out.println("working magic on : " + base64);
        byte[] data = Base64.getDecoder().decode(base64);

        OutputStream localFileStream = null;
        // InputStream remoteContentStream = new ByteArrayInputStream(data);
        try {
            // long fileSize = data.length;
            if(localFile.exists())localFile.delete();
            File dir = localFile.getParentFile();
            dir.mkdirs();

            localFileStream = new FileOutputStream(localFile);

            /*
            byte[] buffer = new byte[bufferSize];
            int sizeOfChunk;
            int amountComplete = 0;
            while ((sizeOfChunk = remoteContentStream.read(buffer)) != -1)
            {
                localFileStream.write(buffer, 0, sizeOfChunk);
                amountComplete += sizeOfChunk;
                updateProgress(amountComplete, fileSize);
            }
            */
            localFileStream.write(data);
            return localFile;
        } finally {
          //  remoteContentStream.close();
            if (localFileStream != null) {
                localFileStream.close();
            }
        }
    }
}
