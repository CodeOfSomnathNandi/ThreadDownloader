import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;

public class InfoCollector {
    private final HttpRequest infoRequest;
    private ArrayList<Long[]> partLengths;
    private long chunkSize;
    private final HttpClient downloader;
    private final long MAX_THREAD = 20;
    public InfoCollector(URI link) {
        partLengths = new ArrayList<>();
        downloader = HttpClient.newHttpClient();
        infoRequest = HttpRequest.newBuilder()
                .HEAD()
                .uri(link)
                .build();
    }
    public long fileLength() throws IOException, InterruptedException {
        var stream = downloader.send(infoRequest, HttpResponse.BodyHandlers.ofInputStream());
        var length = stream.headers().firstValue("Content-Length");
        return length.map(Long::parseLong).orElse(-1L);
    }

    public ArrayList<Long[]> getPartLengths() throws IOException, InterruptedException {
        var length = this.fileLength();
        chunkSize = length / MAX_THREAD;

        for (int i = 0; i < MAX_THREAD-1; i++) {
            var start = chunkSize * i;
            var end  = chunkSize * (i+1);
            partLengths.add(new Long[]{start, end});
        }

        System.out.println("Length: " + partLengths.size());
        partLengths.add(new Long[]{chunkSize * (MAX_THREAD - 1), length});
        return partLengths;
    }


    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }
}
