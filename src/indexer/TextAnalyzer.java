package indexer;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * Created by artem on 13.06.16.
 */
public class TextAnalyzer {
    private StanfordCoreNLP pipeline;
    private Set<String> stopWords;

    private final static String STOP_WORDS_FILE = "./files/stopwords.txt";


    public TextAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        this.pipeline = new StanfordCoreNLP(props);

        try {
            loadStopWords();
        } catch (IOException ex) {
            System.out.println("Can't load stop words!");
        }
    }

    public Map<String, WordOccurrencesInformation> getWordsOccurrences(File document) {
        try {
            String text = readFile(document);
            return getWordsOccurrences(text);
        } catch (IOException ex) {
            ex.printStackTrace();
            return new HashMap<>();
        }
    }

    public Map<String, WordOccurrencesInformation> getWordsOccurrences(String text) {
        if (text.trim().isEmpty()) {
            return new HashMap<>();
        }

        Annotation document = new Annotation(text);
        this.pipeline.annotate(document);

        Map<String, WordOccurrencesInformation> wordsOccurrences = new HashMap<>();
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        int currentPosition = 0;
        int totalNumberOfWords = 0;

        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.LemmaAnnotation.class).trim().toLowerCase();

                if (!isStopSymbol(token)) {
                    wordsOccurrences.putIfAbsent(word, new WordOccurrencesInformation());
                    wordsOccurrences.get(word).setWord(word);
                    wordsOccurrences.get(word).addPosition(currentPosition);

                    ++totalNumberOfWords;
                }

                ++currentPosition;
            }
        }

        calculateWordsLogFrequencies(wordsOccurrences, totalNumberOfWords);

        return wordsOccurrences;
    }

    private void calculateWordsLogFrequencies(Map<String, WordOccurrencesInformation> occurrences, int totalNumberOfWords) {
        for (String word : occurrences.keySet()) {
            double wordFrequency = occurrences.get(word).getTermFrequency();
            double logFrequency = 1 + Math.log10(wordFrequency / totalNumberOfWords);

            occurrences.get(word).setLogFrequencyWeight(logFrequency);
        }
    }

    private String readFile(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        Scanner scan = new Scanner(in);
        StringBuilder text = new StringBuilder();

        while (scan.hasNextLine()) {
            text.append(scan.nextLine());
            text.append("\n");
        }

        return text.toString();
    }

    private void loadStopWords() throws IOException {
        stopWords = new HashSet<>();
        File stopWordsFile = new File(STOP_WORDS_FILE);
        InputStream in = new FileInputStream(stopWordsFile);
        Scanner scan = new Scanner(in);

        while (scan.hasNextLine()) {
            stopWords.add(scan.nextLine().trim());
        }
    }

    private boolean isStopSymbol(CoreLabel token) {
        String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
        return pos.equals(".") || pos.equals(",");
    }

}
