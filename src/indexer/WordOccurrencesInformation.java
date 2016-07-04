package indexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by artem on 13.06.16.
 */
public class WordOccurrencesInformation {
    private String word;
    private Integer docID;
    private Double logFrequencyWeight;
    private Double wordWeight;
    private List<Integer> positions;

    public WordOccurrencesInformation() {
        word = "";
        docID = -1;
        logFrequencyWeight = 0.0;
        wordWeight = 0.0;
        positions = new ArrayList<>();
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getWord() { return this.word; }

    public Integer getDocID() {
        return docID;
    }

    public void setDocID(Integer docID) {
        this.docID = docID;
    }

    public Double getWordWeight() {
        return wordWeight;
    }

    public void setWordWeight(Double weight) {
        this.wordWeight = weight;
    }

    public void setLogFrequencyWeight(Double logWeight) { this.logFrequencyWeight = logWeight; }

    public int getTermFrequency() { return this.positions.size(); }

    public Double getLogFrequencyWeight() { return this.logFrequencyWeight; }

    public List<Integer> getPositions() {
        return positions;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }

    public void addPosition(int position) {
        this.positions.add(position);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WordOccurrencesInformation)) return false;

        WordOccurrencesInformation that = (WordOccurrencesInformation) o;

        if (!word.equals(that.word)) return false;
        return docID.equals(that.docID);

    }

    @Override
    public int hashCode() {
        int result = word.hashCode();
        result = 31 * result + docID.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("(" + docID + ", " + wordWeight + "):");

        for (Integer position : positions) {
            s.append(" " + position);
        }

        return s.toString();
    }

    public static WordOccurrencesInformation parseString(String s) {
        if (s.isEmpty())
            return null;

        String[] info = s.split("[(,):]");
        final int parts = 3;

        if (info.length < parts) {
            throw new IllegalArgumentException("WordOccurrencesInformation: wrong string input for parsing");
        }

        Integer docID = Integer.parseInt(info[1].trim());
        Double weight = Double.parseDouble(info[2].trim());

        String[] stringPositions = info[4].split(" ");
        List<Integer> positions = new ArrayList<>();

        for (String stringPosition : stringPositions) {
            if (!stringPosition.isEmpty()) {
                positions.add(Integer.parseInt(stringPosition.trim()));
            }
        }

        WordOccurrencesInformation woi = new WordOccurrencesInformation();

        woi.setDocID(docID);
        woi.setWordWeight(weight);
        woi.setPositions(positions);

        return woi;
    }
}
