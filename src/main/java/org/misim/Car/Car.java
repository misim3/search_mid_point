package org.misim.Car;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

class Node {
    long nodeId;
    double latitude;
    double longitude;

    List<Link> adj;

    public Node(long nodeId) {
        this.nodeId = nodeId;
        this.adj = new ArrayList<>();
    }

    public Node(long nodeId, double latitude, double longitude) {
        this.nodeId = nodeId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adj = new ArrayList<>();
    }
}

class Link {
    long targetNodeId;
    float time;

    public Link(long targetNodeId, float time) {
        this.targetNodeId = targetNodeId;
        this.time = time;
    }
}

class NodeQ implements Comparable<NodeQ> {
    long id;
    double time;

    public NodeQ(long id, double time) {
        this.id = id;
        this.time = time;
    }

    @Override
    public int compareTo(NodeQ other) {
        return Double.compare(this.time, other.time);
    }
}

class Graph {
    Map<Long, Node> nodes;

    public Graph() {
        this.nodes = new HashMap<>(530000);
    }
}

public class Car {
    public static void main(String[] args) {
        // 대한민국 위도, 경도 범위 33.12 ~ 38.58, 125.11 ~ 131.86
        // 해당 범위로 요청 시 전국 데이터 응답 가능
        // 위 범위보다 더 큰 값을 넣어서 요청한 응답 결과 527394개의 링크 데이터 확인 가능.

        // 추가적으로 dbf 파일을 읽어서 노드와 링크에 대한 정확한 데이터를 추출해보자.
        // dbf 파일은 microsoft access 에서 열기 가능하다.

        /*
        StringBuilder urlBuilder = new StringBuilder("https://openapi.its.go.kr:9443/trafficInfo"); //URL
        urlBuilder.append("?" + URLEncoder.encode("apiKey", "UTF-8") + "=" + URLEncoder.encode("dbe5ff4cb3ba4847aa120e9d12cd7dcb", "UTF-8")); //공개키
        urlBuilder.append("&" + URLEncoder.encode("type","UTF-8") + "=" + URLEncoder.encode("all", "UTF-8")); //도로유형
        urlBuilder.append("&" + URLEncoder.encode("routeNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); //노선번호
        urlBuilder.append("&" + URLEncoder.encode("drcType","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); //도로방향
        urlBuilder.append("&" + URLEncoder.encode("minX","UTF-8") + "=" + URLEncoder.encode("125.11", "UTF-8")); //최소경도영역
        urlBuilder.append("&" + URLEncoder.encode("maxX","UTF-8") + "=" + URLEncoder.encode("131.86", "UTF-8")); //최대경도영역
        urlBuilder.append("&" + URLEncoder.encode("minY","UTF-8") + "=" + URLEncoder.encode("33.12", "UTF-8")); //최소위도영역
        urlBuilder.append("&" + URLEncoder.encode("maxY","UTF-8") + "=" + URLEncoder.encode("38.58", "UTF-8")); //최대위도영역
        urlBuilder.append("&" + URLEncoder.encode("getType","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); //출력타입

        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "text/xml;charset=UTF-8");
        System.out.println("Response code: " + conn.getResponseCode());
        BufferedReader rd;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();
        System.out.println(sb.toString());

         */

        // 출발 노드 입력
        List<Node> startNodes = new ArrayList<>();
        startNodes.add(new Node(1640030400)); // 인천대정문
        startNodes.add(new Node(1640030300)); // 센트럴파크역
        startNodes.add(new Node(1660005700)); // 부평역
        startNodes.add(new Node(1320000101)); // 부산역


        // 그래프 초기화 (예시로 인접 리스트 사용)
        Graph graph = new Graph();

        try {
            // 프로그램 실행 시작 시간 측정
            long startTime = System.currentTimeMillis();
            // XML 응답을 가져오는 코드
            InputStream xmlStream = getXMLStream("https://openapi.its.go.kr:9443/trafficInfo?apiKey=dbe5ff4cb3ba4847aa120e9d12cd7dcb&type=all&drcType=all&minX=120&maxX=140&minY=30&maxY=40&getType=xml");

            // XML을 파싱하는 코드
            parseXML(xmlStream, graph);
            System.out.println("parseXMl done!");

            // 프로그램 실행 종료 시간 측정
            long endTime1 = System.currentTimeMillis();

            // 각 출발 노드에서 동일한 시간이 걸리는 노드들의 집합(4 출발 노드의 교집합)
            List<Long> equalTimeNodes = findMidNodes(startNodes, graph);

            // 프로그램 실행 종료 시간 측정
            long endTime2 = System.currentTimeMillis();

            printEqualTimeNodes(equalTimeNodes);

            long executionTime1 = endTime1 - startTime;
            long executionTime2 = endTime2 - endTime1;

            System.out.println("openapi 처리 시간: " + executionTime1 + " 밀리초");
            System.out.println("프로그램 실행 시간: " + executionTime2 + " 밀리초");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void printEqualTimeNodes(List<Long> equalTimeNodes) {
        System.out.println("도착 노드의 정보:");

        if (equalTimeNodes.isEmpty()) {
            System.out.println("중간 지점 노드가 없습니다.");
            return ;
        }

        for (Long node : equalTimeNodes) {
            System.out.println("Node ID: " + node);
            System.out.println("--------------");
        }

        System.out.println("Node size: " + equalTimeNodes.size());
    }


    // initializeGraph 함수 수정
    /*
    private static void initializeGraph(List<Node> nodes, List<Link> adjacencyList) {
        // 노드 및 링크 초기화 (예시)
        nodes.add(new Node(0, 37.7749, 122.4194));
        nodes.add(new Node(1, 34.0522, 118.2437));
        nodes.add(new Node(2, 40.7128, 74.0060));
        nodes.add(new Node(3, 41.8781, 87.6298));

        // 링크 초기화 (예시)
        adjacencyList[0] = new ArrayList<>(Arrays.asList(new Link(0, 0, 1, 5), new Link(1, 0, 2, 10)));
        adjacencyList[1] = new ArrayList<>(Collections.singletonList(new Link(2, 1, 3, 8)));
        adjacencyList[2] = new ArrayList<>(Collections.singletonList(new Link(3, 2, 3, 7)));
        adjacencyList[3] = new ArrayList<>();
    }

     */


    // 수정된 메서드
    // 수정할 부분 timeDistance는 입력받고, 리턴 값은 그냥 같은 timeDistance동안 도달할 수 있는 노드들로 하자.
    // 입력받은 timeDistance부터 timeDistance+10까지의 시간동안 도달할 수 있는 노드들을 리턴하는 것으로 하자.
    // 그러면 매번 이 함수가 호출될 때마다 새롭게 DP를 해야한다.
    // 그러니까 이 함수 내에서 timeDistance를 키우고, 출발 노드 교집합을 찾는 것도 함께 해야한다.
    // 그러니까 이 함수를 여러 함수로 분리해야한다.
    public static List<Long> findMidNodes(List<Node> startNodes, Graph graph) {
        Set[] endNodes = new Set[startNodes.size()];

        // 선언된 변수 초기화
        for (int j = 0; j < startNodes.size(); j++) {
            endNodes[j] = new HashSet<>();
        }

        // 이동시간 변수 추가 및 초기화
        double timeLimit = 0;

        List<Long> intersectionNodes = new ArrayList<>(); // 동일한 이동 시간을 갖는 노드들의 교집합

        // 반복문의 조건을 교집합의 존재 여부로 변경
        while (intersectionNodes.isEmpty()) {

            // 이동시간을 10씩 키우며 노드를 탐색
            timeLimit += 60 * 10;

            for (int j = 0; j < startNodes.size(); j++) {
                Node startNode = startNodes.get(j);

                // 도착할 수 있는 노드들을 찾는 함수 호출
                Set<Long> reachableNodes = findReachableNodes(startNode, graph, timeLimit);

                // 도착 가능한 노드들 추가
                endNodes[j].addAll(reachableNodes);
            }

            // 동일한 이동 시간을 갖는 노드들의 교집합 찾기
            intersectionNodes = findIntersection(endNodes);
        }

        System.out.println("timeLimit(분): " + timeLimit /60);

        return intersectionNodes;
    }

    // 도착할 수 있는 노드들을 찾는 함수
    private static Set<Long> findReachableNodes(Node startNode, Graph graph, double timeLimit) {
        double startTime;
        long currentNodeId;

        Set<Long> reachableNodes = new HashSet<>();
        PriorityQueue<NodeQ> currentQueue = new PriorityQueue<>();
        Set<Long> visitedNodes = new HashSet<>();

        Map<Long, Double> solution = new HashMap<>(graph.nodes.size());

        long startNodeId = startNode.nodeId;

        // 시작점의 정보를 입력
        solution.put(startNodeId, 0d);
        NodeQ ndq = new NodeQ(startNodeId, solution.get(startNodeId));
        currentQueue.add(ndq);
        visitedNodes.add(startNodeId);

        while (!currentQueue.isEmpty()) {
            NodeQ currentNodeQ = currentQueue.poll();
            currentNodeId = currentNodeQ.id;
            startTime = currentNodeQ.time;

            if (startTime >= timeLimit) {
                continue;
            }

            if (graph.nodes.get(currentNodeId) != null && graph.nodes.get(currentNodeId).adj != null) {
                for (Link currentLink : graph.nodes.get(currentNodeId).adj) {
                    double endTime = startTime + currentLink.time;
                    long nextNodeId = currentLink.targetNodeId;

                    if (endTime < timeLimit && !visitedNodes.contains(nextNodeId)) {
                        if (solution.containsKey(nextNodeId)) {
                            if (solution.get(nextNodeId) > endTime) {
                                solution.put(nextNodeId, endTime);
                            }
                        } else {
                            solution.put(nextNodeId, endTime);
                        }

                        // 도착할 수 있는 노드 추가
                        reachableNodes.add(nextNodeId);

                        ndq = new NodeQ(nextNodeId, solution.get(nextNodeId));
                        currentQueue.add(ndq);
                        visitedNodes.add(nextNodeId);
                    }
                }
            }
        }
        return reachableNodes;
    }

    // 리스트 배열의 중복된 노드들만 남기는 함수
    private static List<Long> findIntersection(Set[] lists) {
        Map<Long, Integer> nodeCount = new HashMap<>();

        // 각 노드가 나타난 횟수를 세기
        for (Set<Long> list : lists) {
            for (Long nodeId : list) {
                nodeCount.put(nodeId, nodeCount.getOrDefault(nodeId, 0) + 1);
                /*
                if (!nodeCount.containsKey(nodeId)) {
                    nodeCount.put(nodeId, 0);
                } else {
                    nodeCount.put(nodeId, nodeCount.get(nodeId) + 1);
                }

                 */
            }
        }

        List<Long> intersection = new ArrayList<>();

        // 중복된 노드만 선택
        for (Map.Entry<Long, Integer> entry : nodeCount.entrySet()) {
            if (entry.getValue() == lists.length) {
                intersection.add(entry.getKey());
            }
        }

        return intersection;
    }

    /*
    public static List<Integer> solveSSSPFromNodes(List<Node> startNodes, List<Node> nodes, List<Link> links) {
        // 수정할 때 선언할 변수: timeDistance, List<Node>[startNodes의 수] endNodes
        // List<Node>[startNodes의 수] endNodes의 교집합 존재하는지 확인하는 함수가 반복문의 조건식이다.
        // 그래서 교집합이 없으면, 반복문이 동작한다.
        // timeDistance를 60*10(10분)씩 키우고, List<Node>[startNodes의 수] endNodes에 도착할 수 있는 노드들 추가.(노드 추가는 따로 함수 구현)
        // 교집합이 존재하면 반복문 멈추고, 교집합 노드들 반환

        int numNodes = nodes.size();
        int i;
        float startTime, endTime;
        int currentNode, nextNodeId;
        int scanBegin, scanEnd;

        // 이동시간 변수 추가 및 초기화
        float timeDistance = 0;

        PriorityQueue<NodeQ> currentQueue = new PriorityQueue<>();
        List<Integer> equalTimeNodes = new ArrayList<>(); // 동일한 이동 시간을 갖는 노드들을 저장할 리스트

        // 시작점의 정보를 입력
        float[] solution = new float[numNodes];
        int[] predecessor = new int[numNodes];
        Arrays.fill(solution, Float.POSITIVE_INFINITY);
        Arrays.fill(predecessor, -1);
        solution[startNodes] = 0;
        NodeQ ndq = new NodeQ(startNodes, 0);
        currentQueue.add(ndq);

        while (!currentQueue.isEmpty()) {
            NodeQ currentNodeQ = currentQueue.poll();
            currentNode = currentNodeQ.id;
            startTime = currentNodeQ.time;

            scanBegin = 0;
            scanEnd = links.size();

            for (i = scanBegin; i < scanEnd; i++) {
                Link currentLink = links.get(i);
                endTime = startTime + currentLink.time;
                nextNodeId = currentLink.endNodeId;

                if (endTime < timeDistance) {
                    if (solution[nextNodeId] > endTime) {
                        solution[nextNodeId] = endTime;
                        predecessor[nextNodeId] = currentNode;

                        // 동일한 이동 시간을 갖는 노드를 리스트에 추가
                        if (equalTimeNodes.isEmpty() || equalTimeNodes.contains(nextNodeId)) {
                            equalTimeNodes.add(nextNodeId);
                        }
                        ndq = new NodeQ(nextNodeId, endTime);
                        currentQueue.add(ndq);
                    }
                }
            }

            // 이동시간을 10씩 키우며 노드를 탐색
            timeDistance += 10;

            // 모든 출발 노드에서 동일한 이동 시간을 갖는 노드가 있는지 확인
            if (equalTimeNodes.size() == numNodes) {
                break;
            }
        }

        return equalTimeNodes;
    }

     */

    // URL로부터 XML 응답을 가져오는 함수
    private static InputStream getXMLStream(String urlString) throws Exception {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        return connection.getInputStream();
    }

    // parseXML 함수에서 추출한 정보로 Node와 Link 생성
    private static void parseXML(InputStream xmlStream, Graph graph) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlStream);

        // 'item' 태그 아래의 정보를 추출
        NodeList itemList = doc.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            // 필요한 정보 추출
            String startNodeId = item.getElementsByTagName("startNodeId").item(0).getTextContent();
            String endNodeId = item.getElementsByTagName("endNodeId").item(0).getTextContent();
            String travelTime = item.getElementsByTagName("travelTime").item(0).getTextContent();

            // 추출한 정보로 Node와 Link 생성
            long startNodeIdLong = Long.parseLong(startNodeId);
            long endNodeIdLong = Long.parseLong(endNodeId);
            float travelTimeFloat = Float.parseFloat(travelTime);

            findOrCreateNode(startNodeIdLong, endNodeIdLong, travelTimeFloat, graph);

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

    //Node가 이미 추가되었는지 확인 후 추가 또는 가져오기
    private static void findOrCreateNode(long startNodeId, long endNodeId, float time, Graph graph) {
        if (!graph.nodes.containsKey(startNodeId)) {
            Node node = new Node(startNodeId);
            node.adj.add(new Link(endNodeId, time));
            graph.nodes.put(startNodeId, node);
        } else {
            Node node = graph.nodes.get(startNodeId);
            node.adj.add(new Link(endNodeId, time));
        }
    }
}

