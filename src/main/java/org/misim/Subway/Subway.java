package org.misim.Subway;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

// 노드가 아니라 노선으로 변경해야 한다.
// 그게 아니라 인천 1호선은 1호선 끼리만 인접하게 한다. 양쪽으로.
// 그렇게 데이터 저장한 후에 환승 가능역만 인접한 것을 연결해주는 방식으로 하자. 그래서 환승할 때 이동 시간이 소요되게 하자.
class Node {
    String nodeId;
    double latitude;
    double longitude;

    // List<Link> adj;

    public Node(String nodeId) {
        this.nodeId = nodeId;
        // this.adj = new ArrayList<>();
    }

    public Node(String nodeId, double latitude, double longitude) {
        this.nodeId = nodeId;
        this.latitude = latitude;
        this.longitude = longitude;
        // this.adj = new ArrayList<>();
    }
}

class Graph {
    Map<String, Node> nodes;

    public Graph() {
        this.nodes = new HashMap<>(1100);
    }
}
public class Subway {
    public static void main(String[] args) {
        // 그래프 초기화 (예시로 인접 리스트 사용)
        Graph graph = new Graph();

        try {
            // 프로그램 실행 시작 시간 측정
            long startTime = System.currentTimeMillis();

            // XML 응답을 가져오는 코드
            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/SubwayInfoService/getKwrdFndSubwaySttnList"); /*URL*/
            urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=snY%2FE%2Fh1clc%2FQvfB6XfZVOMyJfyzGVBzOy%2Bs4F0UCeVuXqvBB1zu8Spjz2%2FF%2F%2BBSa8oxXfpYQ%2BQYvyDX1jwZ0w%3D%3D"); /*Service Key*/
            // urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
            urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("1100", "UTF-8")); /*한 페이지 결과 수*/
            urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*데이터 타입(xml, json)*/
            // urlBuilder.append("&" + URLEncoder.encode("subwayStationName","UTF-8") + "=" + URLEncoder.encode("신도림", "UTF-8")); /*지하철역명*/

            InputStream xmlStream = getXMLStream(urlBuilder.toString());
            // XML을 파싱하는 코드
            parseXML(xmlStream, graph);
            System.out.println("parseXMl done!");

            // 프로그램 실행 종료 시간 측정
            long endTime1 = System.currentTimeMillis();

            long executionTime1 = endTime1 - startTime;

            System.out.println("openapi 처리 시간: " + executionTime1 + " 밀리초");

            printGraph(graph);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static InputStream getXMLStream(String urlString) throws Exception {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        return connection.getInputStream();
    }


    private static void parseXML(InputStream xmlStream, Graph graph) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlStream);

        // 'item' 태그 아래의 정보를 추출
        NodeList itemList = doc.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            // 필요한 정보 추출
            String subwayStationId = item.getElementsByTagName("subwayStationId").item(0).getTextContent();

            // 추출한 정보로 Node와 Link 생성

            findOrCreateNode(subwayStationId, graph);

        }

        // totalCount 값을 추출
        /*
        NodeList totalCountList = doc.getElementsByTagName("totalCount");
        if (totalCountList.getLength() > 0) {
            Element totalCountElement = (Element) totalCountList.item(0);
            String totalCount = totalCountElement.getTextContent();
            System.out.println("Total Count: " + totalCount);
        } else {
            System.out.println("Total Count not found in the XML.");
        }

         */

    }

    private static void findOrCreateNode(String subwayStationId, Graph graph) {
        if (!graph.nodes.containsKey(subwayStationId)) {
            Node node = new Node(subwayStationId);
            graph.nodes.put(subwayStationId, node);
        }
    }

    private static void printGraph(Graph graph) {
        System.out.println("저장된 데이터 전체 목록");

        int totalCount = 0;

        Map<String, Node> a = graph.nodes;

        for (Map.Entry<String, Node> entry : a.entrySet()) {
            System.out.println(entry.getKey());
            totalCount++;
        }
        System.out.println("totalCount: " + totalCount);
    }
}
