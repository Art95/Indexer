package searchengine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by artem on 12.06.16.
 */
public class SearchEngine {

    public SearchEngine() {

    }

    public List<File> findByName(String name) {
        List<File> result = new ArrayList<>();
        File[] roots = File.listRoots();

        for (File root : roots) {
            System.out.println(root.getAbsolutePath());
            result.addAll(findByNameIn(root.getAbsolutePath(), name));
        }

        return result;
    }

    public List<File> findByNameIn(String location, String name) {
        List<File> results = new ArrayList<>();
        File root = new File(location);
        File[] files = root.listFiles();

        if (files == null) {
            return results;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                results.addAll(findByNameIn(file.getAbsolutePath(), name));
            } else {
                if (file.getName().equals(name)) {
                    results.add(file);
                }
            }
        }

        return results;
    }

}
