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
public class DocumentAnalyzer {
    private StanfordCoreNLP pipeline;


    public DocumentAnalyzer() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
        this.pipeline = new StanfordCoreNLP(props);
    }

    public HashMap<String, ArrayList<Integer>> getWordsOccurrences(File document) throws IOException {
        String text = readFile(document);
        return getWordsOccurrences(text);
    }

    public HashMap<String, ArrayList<Integer>> getWordsOccurrences(String text) {
        Annotation document = new Annotation(text);
        this.pipeline.annotate(document);

        HashMap<String, ArrayList<Integer>> wordsOccurrences = new HashMap<>();

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        int currentPosition = 0;

        for (CoreMap sentence : sentences) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.lemma().toLowerCase();

                wordsOccurrences.putIfAbsent(word, new ArrayList<>());
                wordsOccurrences.get(word).add(currentPosition);

                ++currentPosition;
            }
        }

        return wordsOccurrences;
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

}
