package game.util;

import java.io.File;
import java.nio.file.Paths;

/**
 * Utility class for file operations.
 */
public class FileUtil {
    private static final String BASE_DIR = getProjectDir();

    private static String getProjectDir() {
        String currentDir = System.getProperty("user.dir");
        if (currentDir.endsWith("bin")) {
            return Paths.get(currentDir).getParent().toString();
        }
        return currentDir;
    }

    public static String getProjectPath(String relativePath) {
        return Paths.get(BASE_DIR, relativePath).toString();
    }

    public static void ensureDirectoryExists(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }
}