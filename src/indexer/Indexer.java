package indexer;


import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.ling.tokensregex.matcher.Match;
import util.Pair;

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

        int documentNumber = 0;
        indexTable = new HashMap<>();

        for (File file : files) {
            Map<String, WordOccurrencesInformation> wordsOccurrences = textAnalyzer.getWordsOccurrences(file);

            if (wordsOccurrences.isEmpty()) {
                continue;
            }

            ++documentNumber;

            for (String word : wordsOccurrences.keySet()) {
                indexTable.putIfAbsent(word, new ArrayList<>());
                wordsOccurrences.get(word).setDocID(documentNumber);
                indexTable.get(word).add(wordsOccurrences.get(word));
            }

            filesIDs.put(documentNumber, file.getAbsolutePath());
        }

        calculateWordsWeights(documentNumber);
        sortOccurrences(indexTable);

        saveIndex();
        saveDocsIDs();
    }

    public List<String> findDocuments(String query) {
        Map<String, WordOccurrencesInformation> wordsInfo = textAnalyzer.getWordsOccurrences(query);
        weightWordsInQuery(wordsInfo);

        Map<Integer, List<WordOccurrencesInformation>> wordsInDocuments = getWordsInDocuments(wordsInfo.keySet());

        /*double queryNormalizedLength = 0;

        for (WordOccurrencesInformation woi : wordsInfo.values()) {
            queryNormalizedLength += woi.getWordWeight() * woi.getWordWeight();
        }

        queryNormalizedLength = Math.sqrt(queryNormalizedLength);*/

        List<Pair<Integer, Double>> documentsRates = new ArrayList<>();

        for (Integer docID : wordsInDocuments.keySet()) {
            double documentScore = 0;
            //double documentNormalizedLength = 0;

            for (WordOccurrencesInformation woi : wordsInDocuments.get(docID)) {
                if (wordsInfo.containsKey(woi.getWord())) {
                    //double queryTermWeight = wordsInfo.get(woi.getWord()).getWordWeight();
                    double documentTermWeight = woi.getWordWeight();

                    //cosineMeasure += queryTermWeight * documentTermWeight;
                    documentScore += documentTermWeight;
                }

                //documentNormalizedLength += woi.getWordWeight() * woi.getWordWeight();
            }

            //documentNormalizedLength = Math.sqrt(documentNormalizedLength);

            //cosineMeasure /= documentNormalizedLength * queryNormalizedLength;

            documentsRates.add(new Pair<>(docID, documentScore));
        }

        Collections.sort(documentsRates, (pair1, pair2) -> pair2.getSecond().compareTo(pair1.getSecond()));

        return getFilesAddresses(documentsRates);
    }

    private void weightWordsInQuery(Map<String, WordOccurrencesInformation> wordsInformation) {
        for (String word : wordsInformation.keySet()) {
            if (indexTable.containsKey(word)) {
                List<WordOccurrencesInformation> woi = indexTable.get(word);

                double docsWithWord = woi.size();
                int documentsNumber = filesIDs.size();
                double idf = Math.log10(documentsNumber / docsWithWord);

                double wordWeight = wordsInformation.get(word).getLogFrequencyWeight() * idf;
                wordsInformation.get(word).setWordWeight(wordWeight);
            }
        }
    }

    private Map<Integer, List<WordOccurrencesInformation>> getWordsInDocuments(Set<String> queryWords) {
        Map<Integer, List<WordOccurrencesInformation>> wordsInDocuments = new HashMap<>();
        Set<WordOccurrencesInformation> wordsOccurrences = new HashSet<>();

        for (String word : queryWords) {
            if (indexTable.containsKey(word)) {
                wordsOccurrences.addAll(indexTable.get(word));
            }
        }

        for (WordOccurrencesInformation woi : wordsOccurrences) {
            wordsInDocuments.putIfAbsent(woi.getDocID(), new ArrayList<>());
            wordsInDocuments.get(woi.getDocID()).add(woi);
        }

        return wordsInDocuments;
    }

    private void calculateWordsWeights(int numberOfDocuments) {
        for (String word : indexTable.keySet()) {
            double docsWithWord = indexTable.get(word).size();
            double idf = Math.log10(numberOfDocuments / docsWithWord);

            for (WordOccurrencesInformation woi : indexTable.get(word)) {
                double tf_idf = woi.getLogFrequencyWeight() * idf;
                woi.setWordWeight(tf_idf);
            }
        }
    }

    private void sortOccurrences(Map<String, List<WordOccurrencesInformation>> index) {
        for (String word : index.keySet()) {
            Collections.sort(index.get(word), (woi1, woi2) -> woi1.getDocID().compareTo(woi2.getDocID()));
        }
    }

    private List<String> getFilesAddresses(List<Pair<Integer, Double>> documentsRates) {
        List<String> filesAddresses = new ArrayList<>();

        for (Pair<Integer, Double> documentRate : documentsRates) {
            filesAddresses.add(filesIDs.get(documentRate.getFirst()));
        }

        return filesAddresses;
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

            String word = parts[0].trim();
            List<WordOccurrencesInformation> occurrences = parseWordOccurrences(word, parts[1]);

            indexTable.put(word, occurrences);
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

    private List<WordOccurrencesInformation> parseWordOccurrences(String word, String s) {
        String[] wordOccurrencesInfo = s.split(";");
        List<WordOccurrencesInformation> occurrences = new ArrayList<>();

        for (String wordOccurrenceInfo : wordOccurrencesInfo) {
            WordOccurrencesInformation woi = WordOccurrencesInformation.parseString(wordOccurrenceInfo.trim());

            if (woi != null) {
                woi.setWord(word);
                occurrences.add(woi);
            }
        }

        return occurrences;
    }
}
