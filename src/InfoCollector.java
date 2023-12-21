import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;

public class InfoCollector {
    private final HttpRequest infoRequest;
    private ArrayList<Integer[]> partLengths;
    private int chunkSize = 10_00_00_000;
    private final HttpClient downloader;
    private final int MAX_THREAD = 20;
    public InfoCollector(URI link) {
        partLengths = new ArrayList<>();
        downloader = HttpClient.newHttpClient();
        infoRequest = HttpRequest.newBuilder()
                .HEAD()
                .uri(link)
                .build();
    }
    public int fileLength() throws IOException, InterruptedException {
        var stream = downloader.send(infoRequest, HttpResponse.BodyHandlers.ofInputStream());
        var length = stream.headers().firstValue("Content-Length");
        return length.map(Integer::parseInt).orElse(-1);
    }

    public ArrayList<Integer[]> getPartLengths() throws IOException, InterruptedException {
        var length = this.fileLength();

        var size = length / MAX_THREAD;
        if (size > chunkSize) {
            chunkSize = size;
        }

        var remainLength = length;
        var start = 0;
        var end = chunkSize;
        while (remainLength > chunkSize) {
            partLengths.add(new Integer[]{start, end});
            start = end+1;
            end = end + chunkSize;
            remainLength -= chunkSize;
        }
        partLengths.add(new Integer[]{length-remainLength+1, length});
        return partLengths;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
}
