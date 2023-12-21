import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Downloader {
    private HttpClient downloader;
    private InfoCollector info;
    private URI link;
    private HttpRequest.Builder tempPlateRequest;
    private ArrayList<DownloadThread> threads;
    private CountDownLatch latch;
    private final int maxLatch = 10;
    private String name;
    public Downloader(URI link, String name) throws IOException, InterruptedException {
        this.link = link;
        this.name = name;
        threads = new ArrayList<>();
        info = new InfoCollector(link);
        downloader = HttpClient.newHttpClient();
        tempPlateRequest = HttpRequest.newBuilder()
                .GET()
                .uri(link);

    }
    private void init(InfoCollector info) throws IOException, InterruptedException {
        latch = new CountDownLatch(info.getPartLengths().size());
    }
    public void setAll() throws IOException, InterruptedException {
        init(info);
        var list = info.getPartLengths();
        AtomicInteger id = new AtomicInteger();
        list.forEach(v -> {
            threads.add(new DownloadThread(link, tempPlateRequest, downloader, v[0], v[1], id.get(), latch));
            id.getAndIncrement();
        });
    }

    public void startAndWait() throws InterruptedException, IOException {
        threads.forEach(Thread::start);
        latch.await();
        cleanAndSave();
    }

    private void cleanAndSave() throws IOException {
        File[] files = new File("S:\\download").listFiles();
        var mainFile = new FileOutputStream(String.format("S:\\download\\%s", name));
        if (files == null) {
            throw new IOException("Files are not found in S:\\download");
        }
        for (File file : files) {
            this.pushContent(new FileInputStream(file), mainFile);
        }
        mainFile.close();
    }

    private void pushContent(FileInputStream source, FileOutputStream destination) throws IOException {
        byte[] buf = new byte[10_000];
        int length = 0;
        while (length != -1) {
            length = source.read(buf);
            if (length != -1) {
                destination.write(Arrays.copyOfRange(buf, 0, length));
            }
        }
    }

    public InfoCollector getInfo() {
        return info;
    }

}
