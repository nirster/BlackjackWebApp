/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server.engine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ws.blackjack.InvalidXML;
import ws.blackjack.InvalidXML_Exception;

/**
 *
 * @author shai
 */

public final class XmlLoader {
    private int computerPlayerCount;
    private List<String> humanPlayersNames;
    private String xmlGameName;

    public void parseXmlData(String xmlData) throws InvalidXML_Exception {
        humanPlayersNames = new ArrayList<>();

        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(xmlData.getBytes("utf-8"))));
            doc.getDocumentElement().normalize();
            
            this.xmlGameName = doc.getFirstChild().getAttributes().getNamedItem("name").getNodeValue();
            NodeList nList = doc.getElementsByTagName("humanPlayers");
            Node nNode = nList.item(0);

            for (int i = 1; i < nNode.getChildNodes().getLength(); i++) {
                if ("humanPlayer".equals(nNode.getChildNodes().item(1).getNodeName())) {
                    humanPlayersNames.add(nNode.getChildNodes().item(1).getTextContent().toString().trim());
                    
                }
            }

            nList = doc.getElementsByTagName("computerPlayers");
            
            if ("count".equals(nList.item(0).getChildNodes().item(1).getNodeName())) {
                computerPlayerCount = Integer.parseInt(nList.item(0).getChildNodes().item(1).getTextContent());
            }

        } catch (IOException | NumberFormatException | ParserConfigurationException | DOMException | SAXException e) {
            throw new InvalidXML_Exception("Error parsing XML file", new InvalidXML());
        }
    }

    public int getHumanPlayerCount() {
        return humanPlayersNames.size();
    }

    public int getComputerPlayerCount() {
        return computerPlayerCount;
    }

    public List<String> getHumanPlayersNames() {
        return humanPlayersNames;
    }

    public String getGameName() {
        return xmlGameName;
    }
}