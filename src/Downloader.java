import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

public class Downloader {
    private final HttpClient downloader;
    private final InfoCollector info;
    private final URI link;
    private final HttpRequest.Builder tempPlateRequest;
    private final ArrayList<DownloadThread> threads;
    private CountDownLatch latch;
    private final String name;
    public Downloader(URI link, String name) {
        this.link = link;
        this.name = name;
        threads = new ArrayList<>();
        info = new InfoCollector(link);
        downloader = HttpClient.newHttpClient();
        tempPlateRequest = HttpRequest.newBuilder()
                .GET()
                .uri(link);

    }
    private ArrayList<Long[]> init(InfoCollector info) throws IOException, InterruptedException {
        var list = info.getPartLengths();
        latch = new CountDownLatch(list.size());
        return list;
    }
    public void setAll() throws IOException, InterruptedException {
        var list = init(info);
//        var list = info.getPartLengths();
        var id = 0;
        for (Long[] longs : list) {
            threads.add(new DownloadThread(link, tempPlateRequest, downloader, longs[0], longs[1], id, latch));
            id++;
        }
        System.out.println("Threads length: "+threads.size());
        System.out.println("Lists length: "+list.size());
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
        source.close();
    }

    public InfoCollector getInfo() {
        return info;
    }

}
