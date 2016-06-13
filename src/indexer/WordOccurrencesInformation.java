package indexer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by artem on 13.06.16.
 */
public class WordOccurrencesInformation {
    private Integer docID;
    private Integer frequency;
    private List<Integer> positions;

    public WordOccurrencesInformation() {
        docID = null;
        frequency = null;
        positions = null;
    }

    public WordOccurrencesInformation(int docID, List<Integer> positions) {
        this.docID = docID;
        this.frequency = positions.size();
        this.positions = positions;
    }

    public Integer getDocID() {
        return docID;
    }

    public void setDocID(Integer docID) {
        this.docID = docID;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
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
        StringBuilder s = new StringBuilder("[" + docID + ", " + frequency + "]:");

        for (Integer position : positions) {
            s.append(" " + position);
        }

        return s.toString();
    }
}
