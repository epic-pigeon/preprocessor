import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length == 1 || args.length == 2) {
            Path input = Paths.get(new File(args[0]).toURI());
            Path output = args.length == 1 ? null : Paths.get(new File(args[1]).toURI());
            StringBuilder codeBuilder = new StringBuilder();
            List<String> lines = Files.readAllLines(input, StandardCharsets.UTF_8);
            for (int i = 0; i < lines.size(); i++) {
                if (i != 0) codeBuilder.append('\n');
                codeBuilder.append(lines.get(i));
            }
            String code = codeBuilder.toString();
            String result = Preprocessor.preprocess(code);
            if (output == null) {
                System.out.println(result);
            } else {
                Files.write(output, Arrays.asList(result.split("\\r?\\n")), StandardCharsets.UTF_8);
            }
        } else {
            System.err.println("Usage: preprocessor.exe input_file [output_file]");
        }
    }
}
