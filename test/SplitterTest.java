import org.junit.Test;
import static org.junit.Assert.*;
import Splitter.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class SplitterTest {
    @Test
    public void SplitTest() {
        try {
            Splitter first = new Splitter(new String[]{"-l", "3", "-o", "out", "resource/line.txt"});
            assertEquals(first.splitLine(), Arrays.asList("123\r\n321\r\nkjkj", "lili\r\n444"));
            first = new Splitter(new String[]{"-c", "2", "-o", "out", "resource/line.txt"});
            assertEquals(first.splitChar(), Arrays.asList("12", "33", "21", "kj", "kj", "li", "li", "44", "4"));
            first = new Splitter(new String[]{"-n", "2", "-o", "out", "resource/line.txt"});
            assertEquals(Files.readAllLines(Paths.get("outaa")), Arrays.asList("123", "321", "kjk"));
            assertEquals(Files.readAllLines(Paths.get("outab")), Arrays.asList("j", "lili", "444"));
            first.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
