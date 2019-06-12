/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package com.naturalnessanalysis;

import java.util.List;
import java.util.Map;

import com.naturalness.Ranking;
import com.naturalness.Sequence;

public class RankAnalysisResult extends AnalysisResult{

    private List<Ranking> rankingList;
    private Map<Sequence<String>, String> testSequenceMap;

    public RankAnalysisResult(List<Ranking> rankingList, Map<Sequence<String>, String> testSequenceMap) {
        super("RankAnalysis");
        this.rankingList = rankingList;
        this.testSequenceMap = testSequenceMap;
    }


    @Override
    public String toCSV() {
        String csv = "Analysis \n";
        csv += "Analysis Name , "+analysisName+"\n\n";
        csv += "Ranking\n";
        csv += "Test Name, Cross Entropy \n";
        for (Ranking rank : rankingList) {
            String name = testSequenceMap.get(rank.getSequence());
            double crossEntropy = rank.getCrossEntropy();
            csv += name+" , "+crossEntropy+"\n";
        }
        return csv;
    }
}
