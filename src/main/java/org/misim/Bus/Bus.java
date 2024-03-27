package org.misim.Bus;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.*;
import java.util.List;

class City {
    int cityId;

    public City(int cityId) {
        this.cityId = cityId;
    }
}

class BusStop {

    String nodeId;

    int cityCode;

    double latitude;

    double longitude;

    Map<String, Route> adj;

    public BusStop(String nodeId, int cityCode, double latitude, double longitude) {
        this.nodeId = nodeId;
        this.cityCode = cityCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.adj = new HashMap<>();
    }
}

class Route {

    String nodeId;

    String nodeOrd;

    String routeId;

    public Route (String routeId) {
        this.routeId = routeId;
    }

    public Route(String nodeId, String routeId) {
        this.nodeId = nodeId;
        this.routeId = routeId;
    }

    public Route(String nodeId, String nodeOrd, String routeId) {
        this.nodeId = nodeId;
        this.nodeOrd = nodeOrd;
        this.routeId = routeId;
    }
}

class RouteList {

    int cityCode;
    String routeId;

    String routeNo;

    HashMap<String, Route> routes;

    public RouteList(int cityCode, String routeId, String routeNo) {
        this.cityCode = cityCode;
        this.routeId = routeId;
        this.routeNo = routeNo;
        this.routes = new HashMap<>();
    }
}

class Graph {

    Map<Integer, City> cities;

    Map<String, BusStop> busStops;

    Map<String, RouteList> routes;

    public Graph() {
        this.cities = new HashMap<>(150);
        this.busStops = new HashMap<>();
        this.routes = new HashMap<>();
    }
}

class NodeQ implements Comparable<NodeQ> {
    String id; // 현재 노드 ID
    double time; // 현재 노드에 도달하는 데 걸린 시간
    String routeId; // 이동에 사용된 노선 ID
    List<String> passedNodes; // 현재 노드에 도달하기까지 경유한 모든 노드의 ID 리스트

    public NodeQ(String id, double time, String routeId) {
        this.id = id;
        this.time = time;
        this.routeId = routeId;
    }

    public NodeQ(String id, double time, String routeId, List<String> passedNodes) {
        this.id = id;
        this.time = time;
        this.routeId = routeId;
        this.passedNodes = new ArrayList<>(passedNodes);
    }

    @Override
    public int compareTo(NodeQ other) {
        return Double.compare(this.time, other.time);
    }
}


public class Bus {
    public static void main(String[] args) {
        // 그래프 초기화 (예시로 인접 리스트 사용)
        Graph graph;

        // 출발 버스 정류장 ID 입력
        List<String> startNodes = new ArrayList<>();
        startNodes.add("ICB164000386"); // 인천대입구역
        startNodes.add("ICB164000395"); // 인천대정문

        try {
            // 프로그램 실행 시작 시간 측정
            long startTime = System.nanoTime();

            // 출발지 버스 정류장 ID에
            graph = makeGraph();

            // 프로그램 실행 종료 시간 측정
            long endTime1 = System.nanoTime();

            long executionTime1 = endTime1 - startTime;

            System.out.println("openapi 처리 시간: " + (double) executionTime1  + " 초");

            printGraph(graph);

            List<String> equalTimeBusStops = findMidBusStops(startNodes, graph);

            printBusStop(equalTimeBusStops);


        } catch (Exception e) {
            e.fillInStackTrace();
        }


    }

    private static void printBusStop(List<String> equalTimeBusStops) {
        equalTimeBusStops.forEach(System.out::println);
    }

    private static Graph makeGraph() throws Exception{

        Graph graph = new Graph();

        // 전체 도시 코드 추출
        //StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getCtyCodeList"); /*URL*/
        //urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=snY%2FE%2Fh1clc%2FQvfB6XfZVOMyJfyzGVBzOy%2Bs4F0UCeVuXqvBB1zu8Spjz2%2FF%2F%2BBSa8oxXfpYQ%2BQYvyDX1jwZ0w%3D%3D"); /*Service Key*/
        //urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*데이터 타입(xml, json)*/

        //InputStream xmlStream = getXMLStream(urlBuilder.toString());

        //parseXMLCityCode(xmlStream, graph);

        City city = new City(23);
        graph.cities.put(23, city);

        System.out.println("CityCode done!");

        // 도시 코드로 도시별 전체 버스 정류장 목록 정보 추출

        StringBuilder urlBuilder;
        InputStream xmlStream;

        Map<Integer, City> a = graph.cities;

        for (Map.Entry<Integer, City> entry : a.entrySet()) {
            urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnNoList"); /*URL*/
            urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=snY%2FE%2Fh1clc%2FQvfB6XfZVOMyJfyzGVBzOy%2Bs4F0UCeVuXqvBB1zu8Spjz2%2FF%2F%2BBSa8oxXfpYQ%2BQYvyDX1jwZ0w%3D%3D"); /*Service Key*/
            urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
            urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10000", "UTF-8")); /*한 페이지 결과 수*/
            urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*데이터 타입(xml, json)*/
            urlBuilder.append("&" + URLEncoder.encode("cityCode","UTF-8") + "=" + URLEncoder.encode(String.valueOf(entry.getKey()), "UTF-8")); /*도시코드*/
            // urlBuilder.append("&" + URLEncoder.encode("nodeNm","UTF-8") + "=" + URLEncoder.encode("전통시장", "UTF-8")); /*정류소명*/
            // urlBuilder.append("&" + URLEncoder.encode("nodeNo","UTF-8") + "=" + URLEncoder.encode("44810", "UTF-8")); /*정류소번호*/

            xmlStream = getXMLStream(urlBuilder.toString());

            parseXMLBusStop(xmlStream, entry.getKey(), graph);
        }

        System.out.println("BusStopList done!");

        // 버스 노선 목록 정보 추출

        for (Map.Entry<Integer, City> entry : a.entrySet()) {
            urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteNoList"); /*URL*/
            urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=snY%2FE%2Fh1clc%2FQvfB6XfZVOMyJfyzGVBzOy%2Bs4F0UCeVuXqvBB1zu8Spjz2%2FF%2F%2BBSa8oxXfpYQ%2BQYvyDX1jwZ0w%3D%3D"); /*Service Key*/
            urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
            urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10000", "UTF-8")); /*한 페이지 결과 수*/
            urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*데이터 타입(xml, json)*/
            urlBuilder.append("&" + URLEncoder.encode("cityCode","UTF-8") + "=" + URLEncoder.encode(String.valueOf(entry.getKey()), "UTF-8")); /*도시코드*/
            // urlBuilder.append("&" + URLEncoder.encode("nodeNm","UTF-8") + "=" + URLEncoder.encode("전통시장", "UTF-8")); /*정류소명*/
            // urlBuilder.append("&" + URLEncoder.encode("nodeNo","UTF-8") + "=" + URLEncoder.encode("44810", "UTF-8")); /*정류소번호*/

            xmlStream = getXMLStream(urlBuilder.toString());

            parseXMLBusRoute(xmlStream, entry.getKey(), graph);
        }

        System.out.println("RouteList done!");

        // 버스 정류장 정보에 인접한 정류장 정보 추가

        Map<String, BusStop> b = graph.busStops;

        for (Map.Entry<String, BusStop> entry : b.entrySet()) {
            urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusSttnInfoInqireService/getSttnThrghRouteList"); /*URL*/
            urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=snY%2FE%2Fh1clc%2FQvfB6XfZVOMyJfyzGVBzOy%2Bs4F0UCeVuXqvBB1zu8Spjz2%2FF%2F%2BBSa8oxXfpYQ%2BQYvyDX1jwZ0w%3D%3D"); /*Service Key*/
            urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
            urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10000", "UTF-8")); /*한 페이지 결과 수*/
            urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*데이터 타입(xml, json)*/
            urlBuilder.append("&" + URLEncoder.encode("cityCode","UTF-8") + "=" + URLEncoder.encode(String.valueOf(entry.getValue().cityCode), "UTF-8")); /*도시코드 [상세기능4. 도시코드 목록 조회]에서 조회 가능*/
            urlBuilder.append("&" + URLEncoder.encode("routeId","UTF-8") + "=" + URLEncoder.encode(String.valueOf(entry.getKey()), "UTF-8")); /*정류소ID*/

            xmlStream = getXMLStream(urlBuilder.toString());

            parseXMLBusStopRoute(xmlStream, entry.getKey(), graph);
        }

        System.out.println("BusStopRoute done!");

        // 버스 노선이 경유하는 버스 정류장 정리

        Map<String, RouteList> c = graph.routes;

        List<String> needRoutes = graph.busStops.get("ICB164000386").adj.keySet().stream().toList();
        needRoutes.addAll(graph.busStops.get("ICB164000395").adj.keySet().stream().toList());

        for (Map.Entry<String, RouteList> entry : c.entrySet()) {
            if (needRoutes.contains(entry.getKey())) {
                urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteAcctoThrghSttnList"); /*URL*/
                urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=snY%2FE%2Fh1clc%2FQvfB6XfZVOMyJfyzGVBzOy%2Bs4F0UCeVuXqvBB1zu8Spjz2%2FF%2F%2BBSa8oxXfpYQ%2BQYvyDX1jwZ0w%3D%3D"); /*Service Key*/
                urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
                urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10000", "UTF-8")); /*한 페이지 결과 수*/
                urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*데이터 타입(xml, json)*/
                urlBuilder.append("&" + URLEncoder.encode("cityCode","UTF-8") + "=" + URLEncoder.encode(String.valueOf(entry.getValue().cityCode), "UTF-8")); /*도시코드 [상세기능4. 도시코드 목록 조회]에서 조회 가능*/
                urlBuilder.append("&" + URLEncoder.encode("routeId","UTF-8") + "=" + URLEncoder.encode(String.valueOf(entry.getKey()), "UTF-8")); /*노선ID*/

                xmlStream = getXMLStream(urlBuilder.toString());

                parseXMLBusRouteStop(xmlStream, entry.getKey(), graph);
            }
        }

        System.out.println("BusRouteStop done!");


        return graph;
    }

    private static InputStream getXMLStream(String urlString) throws Exception {
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        return connection.getInputStream();
    }

    private static void parseXMLCityCode(InputStream xmlStream, Graph graph) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlStream);

        // 'item' 태그 아래의 정보를 추출
        NodeList itemList = doc.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            if (item != null) {
                // 필요한 정보 추출
                String cityCode = item.getElementsByTagName("citycode").item(0).getTextContent();

                // 추출한 정보로 Node와 Link 생성
                int cityCodeInt = Integer.parseInt(cityCode);

                findOrCreateCity(cityCodeInt, graph);
            }
        }
    }

    private static void parseXMLBusStop(InputStream xmlStream, int cityCode, Graph graph) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlStream);

        // 'item' 태그 아래의 정보를 추출
        NodeList itemList = doc.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            if (item != null) {
                // 필요한 정보 추출
                String nodeid = item.getElementsByTagName("nodeid").item(0).getTextContent();
                String gpslati = item.getElementsByTagName("gpslati").item(0).getTextContent();
                String gpslong = item.getElementsByTagName("gpslong").item(0).getTextContent();

                findOrCreateBusStop(nodeid, gpslati, gpslong, cityCode, graph);
            }
        }
    }

    private static void parseXMLBusRoute(InputStream xmlStream, int cityCode , Graph graph) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlStream);

        // 'item' 태그 아래의 정보를 추출
        NodeList itemList = doc.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            if (item != null) {

                // 필요한 정보 추출
                String routeid = item.getElementsByTagName("routeid").item(0).getTextContent();
                String routeno = item.getElementsByTagName("routeno").item(0).getTextContent();

                findOrCreateBusRoute(cityCode, routeid, routeno, graph);
            }
        }
    }

    private static void parseXMLBusStopRoute(InputStream xmlStream, String nodeId, Graph graph) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlStream);

        // 'item' 태그 아래의 정보를 추출
        NodeList itemList = doc.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            if (item != null) {
                // 필요한 정보 추출
                String routeid = item.getElementsByTagName("routeid").item(0).getTextContent();

                // findOrCreateBusStopRoute(nodeId, routeid, graph);

                Route route = new Route(nodeId, routeid);
                graph.busStops.get(nodeId).adj.put(routeid, route);
            }
        }
    }



    private static void parseXMLBusRouteStop(InputStream xmlStream, String routeid, Graph graph) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlStream);

        // 'item' 태그 아래의 정보를 추출
        NodeList itemList = doc.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            if (item != null) {
                // 필요한 정보 추출
                String nodeid = item.getElementsByTagName("nodeid").item(0).getTextContent();
                String nodeord = item.getElementsByTagName("nodeord").item(0).getTextContent();

                Route route = new Route(nodeid, nodeord, routeid);

                graph.routes.get(routeid).routes.put(nodeid, route);
            }
        }
    }

    private static void findOrCreateBusStopRoute(String nodeId, String routeid, Graph graph) {

        if (graph.routes.containsKey(routeid)) {

            Map<String, Route> a = graph.routes.get(routeid).routes;

            if (a != null) {

                Route b = a.get(nodeId);

                if (b != null) {
                    String index = b.nodeOrd;

                    if (index != null) {

                        Route route;

                        for (Map.Entry<String, Route> entry : a.entrySet()) {
                            if (Integer.parseInt(entry.getValue().nodeOrd) - 1 == Integer.parseInt(index)) {
                                route = new Route(entry.getValue().nodeId, routeid);
                                //graph.busStops.get(nodeId).adj.add(route);
                            }
                            if (Integer.parseInt(entry.getValue().nodeOrd) + 1 == Integer.parseInt(index)) {
                                route = new Route(entry.getValue().nodeId, routeid);
                                //graph.busStops.get(nodeId).adj.add(route);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void findOrCreateCity(int cityCode, Graph graph) {
        if (!graph.cities.containsKey(cityCode)) {
            City city = new City(cityCode);
            graph.cities.put(cityCode, city);
        }
    }

    private static void findOrCreateBusStop(String nodeid, String gpslati, String gpslong, int cityCode, Graph graph) {
        if (!graph.busStops.containsKey(nodeid)) {
            BusStop busStop = new BusStop(nodeid, cityCode, Double.parseDouble(gpslati), Double.parseDouble(gpslong));
            graph.busStops.put(nodeid, busStop);
        }
    }

    private static void findOrCreateBusRoute(int cityCode, String routeid, String routeno, Graph graph) {

        if (!graph.routes.containsKey(routeid)) {
            RouteList routes = new RouteList(cityCode, routeid, routeno);

            graph.routes.put(routeid, routes);
        }
    }

    private static List<String> findMidBusStops(List<String> startStops, Graph graph) {
        // 각 정류장에서 도달 가능한 정류장들의 집합
        Set[] endStops = new Set[startStops.size()];

        // 초기화
        for (int i = 0; i < startStops.size(); i++) {
            endStops[i] = new HashSet<>();
        }

        // 이동시간 변수 추가 및 초기화
        double timeLimit = 0;

        List<String> intersectionStops = new ArrayList<>(); // 동일한 이동 시간을 갖는 정류장들의 교집합

        // 교집합이 없을 때 반복
        while (intersectionStops.isEmpty()) {
            // 이동시간 증가
            timeLimit += 60 * 10; // 10분 단위로 증가 (단위: 분)


            // 각 정류장에 대해 이동 가능한 정류장 탐색
            for (int i = 0; i < startStops.size(); i++) {
                String startStopId = startStops.get(i);

                // 도착할 수 있는 정류장들을 찾음
                Set<String> reachableStops = findReachableBusStops(startStopId, graph, timeLimit);

                // 도착 가능한 정류장들 추가
                endStops[i].addAll(reachableStops);
            }

            // 동일한 이동 시간을 갖는 정류장들의 교집합 찾기
            intersectionStops = findIntersection(endStops);

            if (timeLimit >= 60 * 60) {
                break;
            }
        }

        System.out.println("이동 시간 제한: " + timeLimit + "분");

        return intersectionStops;
    }

    private static Set<String> findReachableBusStops(String startStop, Graph graph, double timeLimit) {
        double startTime;
        String currentNodeId;
        String previousRouteId;

        Set<String> reachableStops = new HashSet<>();
        PriorityQueue<NodeQ> currentQueue = new PriorityQueue<>();
        Set<String> visitedStops = new HashSet<>();

        Map<String, Double> solution = new HashMap<>(graph.busStops.size());

        // 시작점의 정보를 입력
        solution.put(startStop, 0d);
        NodeQ ndq = new NodeQ(startStop, solution.get(startStop), null);
        currentQueue.add(ndq);
        visitedStops.add(startStop);

        while (!currentQueue.isEmpty()) {
            NodeQ currentNodeQ = currentQueue.poll();
            currentNodeId = currentNodeQ.id;
            startTime = currentNodeQ.time;
            previousRouteId = currentNodeQ.routeId;

            if (startTime >= timeLimit) {
                continue;
            }

            if (graph.busStops.get(currentNodeId) != null && graph.busStops.get(currentNodeId).adj != null) {
                System.out.println("1단계");
                for (Route nextRoute : graph.busStops.get(currentNodeId).adj.values()) {
                    double endTime; // 버스 이동 속도에 따라 적절히 조정되어야 함
                    String nextRouteId = nextRoute.routeId;
                    String currentNodeOrder = graph.routes.get(nextRouteId).routes.get(currentNodeId).nodeOrd;
                    for (Route nextStopId : graph.routes.get(nextRouteId).routes.values()) {
                        if (Integer.parseInt(nextStopId.nodeOrd) + 1 == Integer.parseInt(currentNodeOrder) || Integer.parseInt(nextStopId.nodeOrd) - 1 == Integer.parseInt(currentNodeOrder)) {
                            if (previousRouteId != null) {
                                if (nextRouteId.equals(previousRouteId)) {
                                    endTime = startTime + (60);
                                } else {
                                    endTime = startTime + 60 * 4;
                                }
                            } else {
                                endTime = startTime + 60;
                            }

                            if (endTime < timeLimit && !visitedStops.contains(nextStopId.nodeId)) {
                                System.out.println("2단계");
                                if (solution.containsKey(nextStopId.nodeId)) {
                                    if (solution.get(nextStopId.nodeId) > endTime) {
                                        solution.put(nextStopId.nodeId, endTime);
                                    }
                                } else {
                                    solution.put(nextStopId.nodeId, endTime);
                                }

                                // 도착할 수 있는 정류장 추가
                                reachableStops.add(nextStopId.nodeId);

                                ndq = new NodeQ(nextStopId.nodeId, solution.get(nextStopId.nodeId), nextRoute.routeId);
                                currentQueue.add(ndq);
                                visitedStops.add(nextStopId.nodeId);
                            }
                        }
                    }
                }
            }
        }
        return reachableStops;
    }

    private static List<String> findIntersection(Set<String>[] lists) {
        Map<String, Integer> stopCount = new HashMap<>();

        // 각 정류장이 나타난 횟수 세기
        for (Set<String> list : lists) {
            for (String stopId : list) {
                stopCount.put(stopId, stopCount.getOrDefault(stopId, 0) + 1);
            }
        }

        List<String> intersectionStops = new ArrayList<>();

        // 중복된 정류장만 선택
        for (Map.Entry<String, Integer> entry : stopCount.entrySet()) {
            if (entry.getValue() == lists.length) {
                intersectionStops.add(entry.getKey()); // 실제 위도, 경도는 데이터에서 가져와야 함
            }
        }

        return intersectionStops;
    }



    private static void printGraph(Graph graph) {
        System.out.println("저장된 데이터 전체 목록");

        System.out.println("totalCityCount: " + graph.cities.size());
        System.out.println("totalBusRouteCount: " + graph.routes.size());
        System.out.println("totalBusStopCount: " + graph.busStops.size());
    }
}
