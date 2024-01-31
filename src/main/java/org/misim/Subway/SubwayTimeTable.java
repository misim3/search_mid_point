package org.misim.Subway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class SubwayTimeTable {
    public static void main(String[] args) throws IOException {
        StringBuilder urlBuilder = new StringBuilder("http://apis.data.go.kr/1613000/SubwayInfoService/getSubwaySttnAcctoSchdulList"); /*URL*/
        urlBuilder.append("?" + URLEncoder.encode("serviceKey","UTF-8") + "=snY%2FE%2Fh1clc%2FQvfB6XfZVOMyJfyzGVBzOy%2Bs4F0UCeVuXqvBB1zu8Spjz2%2FF%2F%2BBSa8oxXfpYQ%2BQYvyDX1jwZ0w%3D%3D"); /*Service Key*/
        // urlBuilder.append("&" + URLEncoder.encode("pageNo","UTF-8") + "=" + URLEncoder.encode("1", "UTF-8")); /*페이지번호*/
        // urlBuilder.append("&" + URLEncoder.encode("numOfRows","UTF-8") + "=" + URLEncoder.encode("130", "UTF-8")); /*한 페이지 결과 수*/
        urlBuilder.append("&" + URLEncoder.encode("_type","UTF-8") + "=" + URLEncoder.encode("xml", "UTF-8")); /*데이터 타입(xml, json)*/
        urlBuilder.append("&" + URLEncoder.encode("subwayStationId","UTF-8") + "=" + URLEncoder.encode("MTRARA1A01", "UTF-8")); /*지하철역ID [상세기능1. 지하철역 목록조회]에서 조회 가능*/
        urlBuilder.append("&" + URLEncoder.encode("dailyTypeCode","UTF-8") + "=" + URLEncoder.encode("01", "UTF-8")); /*요일구분코드(01:평일, 02:토요일, 03:일요일)*/
        urlBuilder.append("&" + URLEncoder.encode("upDownTypeCode","UTF-8") + "=" + URLEncoder.encode("D", "UTF-8")); /*상하행구분코드(U:상행, D:하행)*/
        URL url = new URL(urlBuilder.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");
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
        // arrTime과 depTime은 HH MM SS 형식이다.
        // arrTime 열차가 해당 역에 도착하는 시간, depTime은 열차가 해당 역을 떠나는 시간.
        // 인접한 지하철역 정보 저장 후에 상행, 하행의 depTime 차이를 기준으로 소요 시간 계산.
        // @ 여기선 문제가 완행, 급행 구별에 대한 방법.
    }
}