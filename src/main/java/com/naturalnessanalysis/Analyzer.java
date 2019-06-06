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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import com.naturalness.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class Analyzer {
    private Map<Sequence<String>, String> testSequenceMap;
    private int depth;
    private double probaOfUnknown;
    final DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

    public Analyzer(int depth, double probaOfUnknown) {
        depth = depth;
        probaOfUnknown = probaOfUnknown;
        testSequenceMap = new HashMap<>();
    }

    public void recordSequenceFromLogFile(File logFile ) throws ParserConfigurationException, SAXException, IOException {
        final Document doc = dbFactory.newDocumentBuilder().parse(logFile);
        final Sequence<String> sequence = new Sequence();
        final String ACTION_TAG_NAME = "action";
        NodeList actionElementList = doc.getElementsByTagName(ACTION_TAG_NAME);
        for (int i=0 ; i < actionElementList.getLength() ; i++) {
            Node actionNode = actionElementList.item(i);
            if (actionNode.getNodeType() == Node.ELEMENT_NODE) {
                Element actionElement = (Element) actionNode;
                String hash = hashActionElement(actionElement);
                sequence.append(new Event<String>(hash));
            }
        }
        String name = logFile.getParentFile().getName().replaceAll("\\_xml$", "");
        testSequenceMap.put(sequence, name);
    }

    public String getSequenceName(Sequence<String> sequence) {
        return testSequenceMap.get(sequence);
    }

    public RankAnalysisResult rankAnalysis() {
        SequenceSuite<String> sequenceList = new SequenceSuite(new ArrayList<>(testSequenceMap.keySet()));
        return new RankAnalysisResult(sequenceList.rank(), testSequenceMap);
    }

    public SequenceAnalysis sequenceAnalysis() {
        Map<Sequence<String>, NaturalnessModel<String>> modelMap = new HashMap<>();

        for (Sequence<String> sequence : testSequenceMap.keySet()) {
            NaturalnessModel<String> model = new NaturalnessModel<>(depth, probaOfUnknown);
            model.learn(sequence);
            modelMap.put(sequence, model);
        }

        return new SequenceAnalysis(modelMap, testSequenceMap);
    }

    public GlobalAnalysisResult globalAnalysis() {
        NaturalnessModel<String> model = new NaturalnessModel<>(depth, probaOfUnknown);
        for (Sequence<String> sequence : testSequenceMap.keySet()) {
            model.learn(sequence);
        }
        return new GlobalAnalysisResult(model);
    }

    private String hashActionElement(Element actionElement) {
        final String TYPE_ATTRIBUTE = "type";
        String type = actionElement.getAttribute(TYPE_ATTRIBUTE);

        final String VALUE_TAG_NAME = "value";
        String value = "";
        NodeList valueNodeList = actionElement.getElementsByTagName(VALUE_TAG_NAME);
        if (valueNodeList.getLength() > 0 ) {
            Node firstValue = valueNodeList.item(0);
            value = firstValue.getTextContent();
        }

        final String CHANNEL_TAG_NAME = "channel";
        String channel = "";
        NodeList channelNodeList = actionElement.getElementsByTagName(CHANNEL_TAG_NAME);
        if (channelNodeList.getLength() > 0 ) {
            Node firstChannel = channelNodeList.item(0);
            final String CHANNEL_NAME_ATTRIBUTE = "name";
            channel = ((Element)firstChannel).getAttribute(CHANNEL_NAME_ATTRIBUTE);
        }

        final String ELEMENT_TAG_NAME = "element";
        String element = "";
        NodeList elementNodeList = actionElement.getElementsByTagName(ELEMENT_TAG_NAME);
        if (elementNodeList.getLength() > 0 ) {
            Node firstElement = elementNodeList.item(0);
            final String ELEMENT_TAG_ATTRIBUTE = "tag";
            element = ((Element)firstElement).getAttribute(ELEMENT_TAG_ATTRIBUTE);
        }

        final String CRITERIAS_TAG_NAME = "criterias";
        String criterias = "";
        NodeList criteriasNodeList = actionElement.getElementsByTagName(CRITERIAS_TAG_NAME);
        if (criteriasNodeList.getLength() > 0 ) {
            Node firstCriterias = valueNodeList.item(0);
            criterias = firstCriterias.getTextContent();
        }

        return type + value + channel + element + criterias;
    }
}