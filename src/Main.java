import java.io.IOException;
import java.net.URI;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        var link = URI.create("https://vscode.download.prss.microsoft.com/dbazure/download/stable/0ee08df0cf4527e40edc9aa28f4b5bd38bbff2b2/VSCodeSetup-x64-1.85.1.exe");

        var downloader = new Downloader(link, "code.exe");
        downloader.getInfo().setChunkSize(1_000_000);
        System.out.println("file size"+ downloader.getInfo().fileLength());

        downloader.setAll();
        downloader.startAndWait();

    }
}