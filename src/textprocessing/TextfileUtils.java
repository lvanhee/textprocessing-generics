package textprocessing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TextfileUtils {

	public static Map<File,String> getAllFilesFromFolder(String string) {
		return Arrays.asList(Paths.get(string).toFile().listFiles()).stream()
				.collect(Collectors.toMap(Function.identity(), x->{
					try {
						return Files.readString(x.toPath());
					} catch (IOException e) {
						e.printStackTrace();
						throw new Error();
					}
				}));
	}

}
