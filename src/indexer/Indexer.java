package indexer;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by artem on 13.06.16.
 */
public class Indexer {
    private final static String INDEX_ADDRESS = "./files/index.txt";
    private final static String FILES_IDs_ADDRESS = "./files/filesIDs.txt";

    private TextAnalyzer textAnalyzer;
    private Map<String, List<WordOccurrencesInformation>> indexTable;
    private Map<Integer, String> filesIDs;

    public Indexer() {
        textAnalyzer = new TextAnalyzer();

        if (!loadIndexTable()) {
            indexTable = new HashMap<>();
        }

        if (!loadFilesIDs()) {
            filesIDs = new HashMap<>();
        }
    }

    public void index(File directory) {
        if (!directory.exists() || !directory.isDirectory())
            return;

        File[] files = directory.listFiles();

        int docID = 0;
        indexTable = new HashMap<>();

        for (File file : files) {
            try {
                HashMap<String, List<Integer>> wordsPositions = textAnalyzer.getWordsOccurrences(file);

                for (String word : wordsPositions.keySet()) {
                    indexTable.putIfAbsent(word, new ArrayList<>());
                    indexTable.get(word).add(new WordOccurrencesInformation(docID,
                            wordsPositions.get(word).size(), wordsPositions.get(word)));
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            filesIDs.put(docID, file.getAbsolutePath());

            ++docID;
        }

        sortOccurrences(indexTable);

        saveIndex();
        saveDocsIDs();
    }

    public List<String> findDocuments(String query) {
        HashMap<String, List<Integer>> words = textAnalyzer.getWordsOccurrences(query);
        Set<String> filesAddresses = new HashSet<>();

        for (String word : words.keySet()) {
            if (indexTable.containsKey(word)) {
                List<WordOccurrencesInformation> occurrences = indexTable.get(word);

                for (WordOccurrencesInformation woi : occurrences) {
                    filesAddresses.add(filesIDs.get(woi.getDocID()));
                }
            }
        }

        return new ArrayList<>(filesAddresses);
    }

    private void sortOccurrences(Map<String, List<WordOccurrencesInformation>> index) {
        for (String word : index.keySet()) {
            Collections.sort(index.get(word), (woi1, woi2) -> woi1.getDocID().compareTo(woi2.getDocID()));
        }
    }

    private void saveDocsIDs() {
        File indexFile = new File(FILES_IDs_ADDRESS);
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(indexFile, false));

            for (Integer id : filesIDs.keySet()) {
                writer.write(id + "#" + filesIDs.get(id));
                writer.write("\n");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void saveIndex() {
        File indexFile = new File(INDEX_ADDRESS);
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(indexFile, false));

            for (String word : indexTable.keySet()) {
                writer.write(word + "#");

                for (WordOccurrencesInformation woi : indexTable.get(word)) {
                    writer.write(woi + ";");
                }

                writer.write("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean loadIndexTable() {
        String content = null;
        indexTable = new HashMap<>();

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(INDEX_ADDRESS));
            content = new String(encoded, Charset.defaultCharset());
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        if (content.isEmpty()) {
            return false;
        }

        String[] wordsInfo = content.split("\n");

        for (String wordInfo : wordsInfo) {
            String[] parts = wordInfo.split("#");
            List<WordOccurrencesInformation> occurrences = parseWordOccurrences(parts[1]);

            indexTable.put(parts[0].trim(), occurrences);
        }

        return true;
    }

    private boolean loadFilesIDs() {
        String content = null;
        filesIDs = new HashMap<>();

        try {
            byte[] encoded = Files.readAllBytes(Paths.get(FILES_IDs_ADDRESS));
            content = new String(encoded, Charset.defaultCharset());
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }

        if (content.isEmpty()) {
            return false;
        }

        String[] docsInfo = content.split("\n");

        for (String docInfo : docsInfo) {
            String[] parts = docInfo.split("#");

            if (parts.length < 2) {
                continue;
            }

            Integer id = Integer.parseInt(parts[0].trim());
            String fileAddress = parts[1].trim();

            filesIDs.put(id, fileAddress);
        }

        return true;
    }

    private List<WordOccurrencesInformation> parseWordOccurrences(String s) {
        String[] wordOccurrencesInfo = s.split(";");
        List<WordOccurrencesInformation> occurrences = new ArrayList<>();

        for (String wordOccurrenceInfo : wordOccurrencesInfo) {
            WordOccurrencesInformation woi = WordOccurrencesInformation.parseString(wordOccurrenceInfo.trim());

            if (woi != null) {
                occurrences.add(woi);
            }
        }

        return occurrences;
    }
}
