package com.naturalnessanalysis;

import com.naturalness.NaturalnessModel;
import com.naturalness.Sequence;

import java.util.Map;

public class SequenceAnalysis extends AnalysisResult {
    private Map<Sequence<String>, NaturalnessModel<String>> sequenceModelMap;
    private Map<Sequence<String>, String> testSequenceMap;

    public SequenceAnalysis(Map<Sequence<String>, NaturalnessModel<String>> sequenceModelMap, Map<Sequence<String>, String> testSequenceMap) {
        super("Sequence Analysis");
        this.sequenceModelMap = sequenceModelMap;
        this.testSequenceMap = testSequenceMap;
    }


    @Override
    public String toCSV() {
        String csv = "Analysis \n";
        csv += "Analysis Name , "+analysisName+"\n\n";
        csv += "Test Name, size, occurence\n";
        for (Sequence<String> sequence : sequenceModelMap.keySet()) {
            NaturalnessModel<String> model = sequenceModelMap.get(sequence);
            String name = testSequenceMap.get(sequence);
            csv += name+","+model.size()+","+model.occurence()+"\n";
        }
        return csv;
    }
}
