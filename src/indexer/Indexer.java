package indexer;


import main.Constants;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by artem on 13.06.16.
 */
public class Indexer {
    private DocumentAnalyzer documentAnalyzer;
    private HashMap<String, List<WordOccurrencesInformation>> indexTable;

    public Indexer() {
        documentAnalyzer = new DocumentAnalyzer();
        indexTable = new HashMap<>();
    }

    public void index(File directory) {
        if (!directory.exists() || !directory.isDirectory())
            return;

        File[] files = directory.listFiles();

        int docID = 0;
        indexTable = new HashMap<>();

        for (File file : files) {
            try {
                HashMap<String, ArrayList<Integer>> wordsPositions = documentAnalyzer.getWordsOccurrences(file);

                for (String word : wordsPositions.keySet()) {
                    indexTable.putIfAbsent(word, new ArrayList<>());
                    indexTable.get(word).add(new WordOccurrencesInformation(docID, wordsPositions.get(word)));
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
        File indexFile = new File(Constants.INDEX_ADDRESS);
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(indexFile, false));

            for (String word : indexTable.keySet()) {
                writer.write(word + ": ");

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
}
