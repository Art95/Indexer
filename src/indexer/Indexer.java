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

    private TextAnalyzer textAnalyzer;
    private HashMap<String, List<WordOccurrencesInformation>> indexTable;

    public Indexer() {
        textAnalyzer = new TextAnalyzer();

        if (!loadIndexTable()) {
            indexTable = new HashMap<>();
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
                HashMap<String, ArrayList<Integer>> wordsPositions = textAnalyzer.getWordsOccurrences(file);

                for (String word : wordsPositions.keySet()) {
                    indexTable.putIfAbsent(word, new ArrayList<>());
                    indexTable.get(word).add(new WordOccurrencesInformation(docID,
                            wordsPositions.get(word).size(), wordsPositions.get(word)));
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

            ++docID;
        }

        sortOccurrences(indexTable);
        saveIndex();
    }

    private void sortOccurrences(HashMap<String, List<WordOccurrencesInformation>> index) {
        for (String word : index.keySet()) {
            Collections.sort(index.get(word), (woi1, woi2) -> woi1.getDocID().compareTo(woi2.getDocID()));
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
