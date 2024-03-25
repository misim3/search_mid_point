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
import java.util.*;

// 노드가 아니라 노선으로 변경해야 한다.
// 그게 아니라 인천 1호선은 1호선 끼리만 인접하게 한다. 양쪽으로.
// 그렇게 데이터 저장한 후에 환승 가능역만 인접한 것을 연결해주는 방식으로 하자. 그래서 환승할 때 이동 시간이 소요되게 하자.
class Node {
    String nodeId;

    String subwayStationName;

    String subwayRouteName;

    double latitude;
    double longitude;

    List<Link> adj;

    public Node(String nodeId, String subwayStationName, String subwayRouteName) {
        this.nodeId = nodeId;
        this.subwayStationName = subwayStationName;
        this.subwayRouteName = subwayRouteName;
        this.adj = new ArrayList<>();
    }

    public Node(String nodeId, double latitude, double longitude) {
        this.nodeId = nodeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adj = new ArrayList<>();
    }
}


class Link {

    String targetNodeId;

    String subwayRouteName;

    float time;

    public Link(String targetNodeId, String subwayRouteName, float time) {
        this.targetNodeId = targetNodeId;
        this.subwayRouteName = subwayRouteName;
        this.time = time;
    }
}

class NodeQ implements Comparable<NodeQ> {
    String id;

    double time;

    String routeName;

    public NodeQ(String id, double time, String routeName) {
        this.id = id;
        this.time = time;
        this.routeName = routeName;
    }

    @Override
    public int compareTo(NodeQ other) {
        return Double.compare(this.time, other.time);
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

        List<String> startNodes = new ArrayList<>();
        startNodes.add("1");
        startNodes.add("2");

        try {
            // 프로그램 실행 시작 시간 측정
            long startTime = System.currentTimeMillis();

            makeSubwayGraph(graph);

            // 프로그램 실행 종료 시간 측정
            long endTime1 = System.currentTimeMillis();

            long executionTime1 = endTime1 - startTime;

            System.out.println("openapi 처리 시간: " + executionTime1 + " 밀리초");

            printGraph(graph);

            List<String> equalTimeNodes = findMidNodes(startNodes, graph);

            printSubway(equalTimeNodes);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printSubway(List<String> equalTimeNodes) {
        equalTimeNodes.forEach(System.out::println);
    }

    private static void makeSubwayGraph(Graph graph) throws Exception{

        // XML 응답을 가져오는 코드
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/SubwayInfoService/getKwrdFndSubwaySttnList"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=snY%2FE%2Fh1clc%2FQvfB6XfZVOMyJfyzGVBzOy%2Bs4F0UCeVuXqvBB1zu8Spjz2%2FF%2F%2BBSa8oxXfpYQ%2BQYvyDX1jwZ0w%3D%3D"); /*Service Key*/
        // urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        // urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("1100", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*데이터 타입(xml, json)*/
        // urlBuilder.append("&" + URLEncoder.encode("subwayStationName","UTF-8") + "=" + URLEncoder.encode("신도림", "UTF-8")); /*지하철역명*/

        InputStream xmlStream = getXMLStream(urlBuilder.toString());
        // XML을 파싱하는 코드
        parseXML(xmlStream, graph);
        System.out.println("parseXMl done!");
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
            String subwayStationName = item.getElementsByTagName("subwayStationName").item(0).getTextContent();
            String subwayRouteName = item.getElementsByTagName("subwayRouteName").item(0).getTextContent();

            // 추출한 정보로 Node와 Link 생성

            findOrCreateNode(subwayStationId, subwayStationName, subwayRouteName, graph);

        }
    }

    private static void findOrCreateNode(String subwayStationId, String subwayStationName, String subwayRouteName, Graph graph) {
        if (!graph.nodes.containsKey(subwayStationId)) {
            Node node = new Node(subwayStationId, subwayStationName, subwayRouteName);
            graph.nodes.put(subwayStationId, node);
        }
    }

    private static List<String> findMidNodes(List<String> startNodes, Graph graph) {

        Set[] endStops = new Set[startNodes.size()];

        for (int i = 0; i < startNodes.size(); i++) {
            endStops[i] = new HashSet<>();
        }

        double timeLimit = 0;

        List<String> intersectionStops = new ArrayList<>();

        while (intersectionStops.isEmpty()) {

            timeLimit += 60 * 10;

            System.out.println(timeLimit);

            for (int i = 0; i < startNodes.size(); i++) {
                String startNode = startNodes.get(i);

                Set<String> reachableNodes = findReachableNodes(startNode, graph, timeLimit);

                endStops[i].addAll(reachableNodes);
            }

            intersectionStops = findIntersection(endStops);
        }

        System.out.println("이동 시간 제한: " + timeLimit + "분");

        return intersectionStops;
    }

    private static Set<String> findReachableNodes(String startNode, Graph graph, Double timeLimit) {
        double startTime;
        String currentNodeId;
        String previousRouteName;

        Set<String> reachableNodes = new HashSet<>();
        PriorityQueue<NodeQ> currentQueue = new PriorityQueue<NodeQ>();
        Set<String> visitedStops = new HashSet<>();

        Map<String, Double> solution = new HashMap<>(graph.nodes.size());

        solution.put(startNode, 0d);
        NodeQ ndq = new NodeQ(startNode, solution.get(startNode), graph.nodes.get(startNode).subwayRouteName);
        currentQueue.add(ndq);
        visitedStops.add(startNode);

        while (!currentQueue.isEmpty()) {
            NodeQ currentNodeQ = currentQueue.poll();
            currentNodeId = currentNodeQ.id;
            startTime = currentNodeQ.time;
            previousRouteName = currentNodeQ.routeName;

            if (startTime >= timeLimit) {
                continue;
            }

            if (graph.nodes.get(currentNodeId) != null && graph.nodes.get(currentNodeId).adj != null) {
                for (Link currentRoute : graph.nodes.get(currentNodeId).adj) {
                    double endTime;
                    String nextNodeId = currentRoute.targetNodeId;
                    String nextRouteName = currentRoute.subwayRouteName;

                    if (previousRouteName != null) {
                        if (nextRouteName.equals(previousRouteName)) {
                            endTime = startTime + (60);
                        } else {
                            endTime = startTime + 60 * 4;
                        }
                    } else {
                        endTime = startTime + 60;
                    }

                    if (endTime < timeLimit && !visitedStops.contains(nextNodeId)) {
                        if (solution.containsKey(nextNodeId)) {
                            if (solution.get(nextNodeId) > endTime) {
                                solution.put(nextNodeId, endTime);
                            }
                        } else {
                            solution.put(nextNodeId, endTime);
                        }

                        // 도착할 수 있는 정류장 추가
                        reachableNodes.add(nextNodeId);

                        ndq = new NodeQ(nextNodeId, solution.get(nextNodeId), nextRouteName);
                        currentQueue.add(ndq);
                        visitedStops.add(nextNodeId);
                    }
                }
            }
        }
        return reachableNodes;
    }

    private static List<String> findIntersection(Set<String>[] lists) {
        Map<String, Integer> nodeCount = new HashMap<>();

        for (Set<String> list : lists) {
            for (String nodeId : list) {
                nodeCount.put(nodeId, nodeCount.getOrDefault(nodeId, 0) + 1);
            }
        }

        List<String> intersection = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : nodeCount.entrySet()) {
            if (entry.getValue() == lists.length) {
                intersection.add(entry.getKey());
            }
        }

        return intersection;
    }

    private static void printGraph(Graph graph) {
        System.out.println("저장된 데이터 전체 목록");

        System.out.println("totalCount: " + graph.nodes.size());
    }
}
