package indexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by artem on 13.06.16.
 */
public class WordOccurrencesInformation {
    private Integer docID;
    private Double frequency;
    private List<Integer> positions;

    public WordOccurrencesInformation() {
        docID = null;
        frequency = null;
        positions = null;
    }

    public WordOccurrencesInformation(int docID, double frequency, List<Integer> positions) {
        this.docID = docID;
        this.frequency = frequency;
        this.positions = positions;
    }

    public Integer getDocID() {
        return docID;
    }

    public void setDocID(Integer docID) {
        this.docID = docID;
    }

    public Double getFrequency() {
        return frequency;
    }

    public void setFrequency(Double frequency) {
        this.frequency = frequency;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("(" + docID + ", " + frequency + "):");

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
        Double frequency = Double.parseDouble(info[2].trim());

        String[] stringPositions = info[4].split(" ");
        List<Integer> positions = new ArrayList<>();

        for (String stringPosition : stringPositions) {
            if (!stringPosition.isEmpty()) {
                positions.add(Integer.parseInt(stringPosition.trim()));
            }
        }

        WordOccurrencesInformation woi = new WordOccurrencesInformation(docID, frequency, positions);

        return woi;
    }
}
