import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        StringBuilder codeBuilder = new StringBuilder();
        List<String> lines = Files.readAllLines(Paths.get(new File("program.java").toURI()));
        for (int i = 0; i < lines.size(); i++) {
            if (i != 0) codeBuilder.append('\n');
            codeBuilder.append(lines.get(i));
        }
        String code = codeBuilder.toString();
        System.out.println(Preprocessor.preprocess(code));
    }
}
