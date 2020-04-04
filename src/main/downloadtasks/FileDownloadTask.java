package main.downloadtasks;


import main.HttpCManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileDownloadTask extends DownloadTask {


    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private HttpClient httpClient;
    private String remoteUrl;
    private int bufferSize;

    public FileDownloadTask(String remoteUrl, File localFile, String taskID)
    {
        this(HttpCManager.getClient(), remoteUrl, localFile, taskID, DEFAULT_BUFFER_SIZE);
        setDownloadedAt();
    }

    public FileDownloadTask(HttpClient httpClient, String remoteUrl, File localFile, String taskID, int bufferSize)
    {
        this.httpClient = httpClient;
        this.remoteUrl = remoteUrl;
        this.localFile = localFile;
        this.bufferSize = bufferSize;
        this.taskID = taskID;
        addStateChangeListener();

    }

    public String getRemoteUrl() {
        return remoteUrl;
    }
    protected File call() throws Exception
    {

        HttpGet httpGet = new HttpGet(this.remoteUrl);
        HttpResponse response = httpClient.execute(httpGet);
        InputStream remoteContentStream = response.getEntity().getContent();
        OutputStream localFileStream = null;
        try
        {
            long fileSize = response.getEntity().getContentLength();
            File dir = localFile.getParentFile();
            dir.mkdirs();

            localFileStream = new FileOutputStream(localFile);
            byte[] buffer = new byte[bufferSize];
            int sizeOfChunk;
            int amountComplete = 0;
            while ((sizeOfChunk = remoteContentStream.read(buffer)) != -1)
            {
                localFileStream.write(buffer, 0, sizeOfChunk);
                amountComplete += sizeOfChunk;
                updateProgress(amountComplete, fileSize);
                }
            return localFile;
        }
        finally
        {
            remoteContentStream.close();
            if (localFileStream != null)
            {
                localFileStream.close();
            }
        }
    }

}
