import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;

public class InfoCollector {
    private final HttpRequest infoRequest;
    private ArrayList<Integer[]> partLengths;
    private int chunkSize;
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
        chunkSize = length / MAX_THREAD;
        var remainLength = chunkSize;
        var start = 0;
        var end  = chunkSize;
        partLengths.add(new Integer[]{start, end});
        while (remainLength < length) {
            start += chunkSize;
            end += chunkSize;
            partLengths.add(new Integer[]{start, end});
            remainLength += chunkSize;
        }

        partLengths.add(new Integer[]{length-remainLength+1, length});
        partLengths.forEach(v->{
            System.out.println(Arrays.toString(v));
        });
        return partLengths;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
}
