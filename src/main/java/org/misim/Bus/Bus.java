package org.misim.Bus;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class City {
    int cityId;
    String cityName;

    public City(int cityId) {
        this.cityId = cityId;
    }
}

class BusStop {

    String nodeId;

    double latitude;

    double longitude;

    public BusStop(String nodeId, double latitude, double longitude) {
        this.nodeId = nodeId;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}

class Route {

    String routeId;

    String routeNo;

    String routeType;

    int cityCode;

    List<String> busStops;

    public Route(String routeId, String routeNo, String routeType, int cityCode) {
        this.routeId = routeId;
        this.routeNo = routeNo;
        this.routeType = routeType;
        this.cityCode = cityCode;
        this.busStops = new ArrayList<String>();
    }
}

class Graph {

    Map<Integer, City> cities;
    Map<String, Route> buses;

    Map<String, BusStop> busStops;

    public Graph() {
        this.cities = new HashMap<>(150);
        this.buses = new HashMap<>();
        this.busStops = new HashMap<>();
    }
}

// 01.02 버스 노선별 정보, 전체 버스 정류소 정보 저장

public class Bus {
    public static void main(String[] args) {
        // 그래프 초기화 (예시로 인접 리스트 사용)
        Graph graph = new Graph();

        try {
            // 프로그램 실행 시작 시간 측정
            long startTime = System.nanoTime();

            // 전체 도시 코드 추출
            StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getCtyCodeList"); /*URL*/
            urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=snY%2FE%2Fh1clc%2FQvfB6XfZVOMyJfyzGVBzOy%2Bs4F0UCeVuXqvBB1zu8Spjz2%2FF%2F%2BBSa8oxXfpYQ%2BQYvyDX1jwZ0w%3D%3D"); /*Service Key*/
            urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*데이터 타입(xml, json)*/

            InputStream xmlStream = getXMLStream(urlBuilder.toString());

            parseXMLCityCode(xmlStream, graph);
            System.out.println("CityCode done!");

            // 도시 코드로 도시별 전체 버스 경로 정보 추출

            Map<Integer, City> a = graph.cities;

            for (Map.Entry<Integer, City> entry : a.entrySet()) {
                urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteNoList"); /*URL*/
                urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=snY%2FE%2Fh1clc%2FQvfB6XfZVOMyJfyzGVBzOy%2Bs4F0UCeVuXqvBB1zu8Spjz2%2FF%2F%2BBSa8oxXfpYQ%2BQYvyDX1jwZ0w%3D%3D"); /*Service Key*/
                // urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
                // urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
                urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*데이터 타입(xml, json)*/
                urlBuilder.append("&" + URLEncoder.encode("cityCode","UTF-8") + "=" + URLEncoder.encode(String.valueOf(entry.getKey()), "UTF-8")); /*도시코드*/
                // urlBuilder.append("&" + URLEncoder.encode("nodeNm","UTF-8") + "=" + URLEncoder.encode("전통시장", "UTF-8")); /*정류소명*/
                // urlBuilder.append("&" + URLEncoder.encode("nodeNo","UTF-8") + "=" + URLEncoder.encode("44810", "UTF-8")); /*정류소번호*/

                xmlStream = getXMLStream(urlBuilder.toString());

                parseXMLBusRouteList(xmlStream, entry.getKey(), graph);
            }

            System.out.println("Route done!");

            // 버스 번호로 버스 경유 정류소 정보 추출 -> 버스별 경유 정류소 정보 저장 및 전체 버스 정류소 정보 저장

            Map<String, Route> b = graph.buses;

            for (Map.Entry<String, Route> entry : b.entrySet()) {
                urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/BusRouteInfoInqireService/getRouteAcctoThrghSttnList"); /*URL*/
                urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=snY%2FE%2Fh1clc%2FQvfB6XfZVOMyJfyzGVBzOy%2Bs4F0UCeVuXqvBB1zu8Spjz2%2FF%2F%2BBSa8oxXfpYQ%2BQYvyDX1jwZ0w%3D%3D"); /*Service Key*/
                // urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
                // urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("10", "UTF-8")); /*한 페이지 결과 수*/
                urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*데이터 타입(xml, json)*/
                urlBuilder.append("&" + URLEncoder.encode("cityCode","UTF-8") + "=" + URLEncoder.encode(String.valueOf(entry.getValue().cityCode), "UTF-8")); /*도시코드 [상세기능4. 도시코드 목록 조회]에서 조회 가능*/
                urlBuilder.append("&" + URLEncoder.encode("routeId","UTF-8") + "=" + URLEncoder.encode(String.valueOf(entry.getKey()), "UTF-8")); /*노선ID [상세기능1. 노선번호목록 조회]에서 조회 가능*/

                xmlStream = getXMLStream(urlBuilder.toString());

                parseXMLBusRouteStop(xmlStream, entry.getKey(), graph);
            }

            System.out.println("BusStop done!");

            // 프로그램 실행 종료 시간 측정
            long endTime1 = System.nanoTime();

            long executionTime1 = endTime1 - startTime;

            System.out.println("openapi 처리 시간: " + (double) executionTime1  + " 초");

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

    private static void parseXMLCityCode(InputStream xmlStream, Graph graph) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlStream);

        // 'item' 태그 아래의 정보를 추출
        NodeList itemList = doc.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            // 필요한 정보 추출
            String cityCode = item.getElementsByTagName("citycode").item(0).getTextContent();

            // 추출한 정보로 Node와 Link 생성
            int cityCodeInt = Integer.parseInt(cityCode);

            findOrCreateCity(cityCodeInt, graph);

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

    private static void parseXMLBusRouteList(InputStream xmlStream, int cityCode, Graph graph) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlStream);

        // 'item' 태그 아래의 정보를 추출
        NodeList itemList = doc.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            // 필요한 정보 추출
            String routeid = item.getElementsByTagName("routeid").item(0).getTextContent();
            String routeno = item.getElementsByTagName("routeno").item(0).getTextContent();
            String routetp = item.getElementsByTagName("routetp").item(0).getTextContent();

            findOrCreateBus(routeid, routeno, routetp, cityCode, graph);

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

    private static void parseXMLBusRouteStop(InputStream xmlStream, String routeId, Graph graph) throws Exception {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(xmlStream);

        // 'item' 태그 아래의 정보를 추출
        NodeList itemList = doc.getElementsByTagName("item");

        for (int i = 0; i < itemList.getLength(); i++) {
            Element item = (Element) itemList.item(i);

            // 필요한 정보 추출
            String nodeid = item.getElementsByTagName("nodeid").item(0).getTextContent();
            String gpslati = item.getElementsByTagName("gpslati").item(0).getTextContent();
            String gpslong = item.getElementsByTagName("gpslong").item(0).getTextContent();

            findOrCreateBusStop(routeId, nodeid, gpslati, gpslong, graph);

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

    private static void findOrCreateCity(int cityCode, Graph graph) {
        if (!graph.cities.containsKey(cityCode)) {
            City city = new City(cityCode);
            graph.cities.put(cityCode, city);
        }
    }

    private static void findOrCreateBus(String routeid, String routeno, String routetp, int cityCode, Graph graph) {
        if (!graph.buses.containsKey(routeid)) {
            Route route = new Route(routeid, routeno, routetp, cityCode);
            graph.buses.put(routeid, route);
        }
    }

    private static void findOrCreateBusStop(String routeid, String nodeid, String latitude, String longitude, Graph graph) {
        // 버스 노선에 버스 정류소 정보 추가
        if(!graph.buses.containsKey(routeid)) {
            graph.buses.get(routeid).busStops.add(nodeid);
        }

        // graph의 busStops(전체 버스 정류소 정보)에 버스 정류소 정보 추가
        if (!graph.busStops.containsKey(nodeid)) {
            BusStop busStop = new BusStop(nodeid, Double.valueOf(latitude), Double.valueOf(longitude));
            graph.busStops.put(nodeid, busStop);
        }
    }

    private static void printGraph(Graph graph) {
        System.out.println("저장된 데이터 전체 목록");

        System.out.println("totalCityCount: " + graph.cities.size());
        System.out.println("totalRouteCount: " + graph.buses.size());
        System.out.println("totalBusStopCount: " + graph.busStops.size());
    }
}
