import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class DownloadThread extends Thread {
    private final HttpRequest.Builder requestTemplate;
    private final HttpClient downloader;
    private final int id;
    private final CountDownLatch latch;
    private long startLength, endLength;
    public DownloadThread(URI link, HttpRequest.Builder requestTemplate, HttpClient downloader, long startLength, long endLength, int id, CountDownLatch latch) {
        this.downloader = downloader;
        this.requestTemplate = requestTemplate;
        this.id = id;
        this.latch = latch;
        this.startLength = startLength;
        this.endLength = endLength;
    }

    public void savePart(InputStream content,  int id) throws IOException {
        byte[] buffer = new byte[10_000];
        var file = new FileOutputStream(String.format("S:\\download\\part%d.p", id));
        int length = 0;

        while (length != -1) {
            length = content.read(buffer);
            if (length != -1) {
                file.write(Arrays.copyOfRange(buffer, 0, length));
            }
        }
        file.close();
    }

    @Override
    public void run() {
        // Range: bytes=0-1023
        requestTemplate.setHeader("Range", String.format("bytes=%d-%d", startLength, endLength));
        try {
            var stream = downloader.send(requestTemplate.build(), HttpResponse.BodyHandlers.ofInputStream());
            var content = stream.body();
            savePart(content, id);
            System.out.printf("part%d.p is saved\n", id);
            content.close();
        } catch (Exception e) {
            System.out.printf("Number %d part is not downloadable\n", id);
            e.printStackTrace();
        }
        latch.countDown();
    }
}
