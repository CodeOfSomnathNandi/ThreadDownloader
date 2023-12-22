import java.io.IOException;
import java.net.URI;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException, IOException {
        var link = URI.create("https://download-cdn.jetbrains.com/go/goland-2023.3.2.exe");

        var downloader = new Downloader(link, "code.exe");
        downloader.getInfo().setChunkSize(1_000_000);
        System.out.println("File size: " + downloader.getInfo().fileLength());

        downloader.setAll();
        downloader.startAndWait();
    }
}
