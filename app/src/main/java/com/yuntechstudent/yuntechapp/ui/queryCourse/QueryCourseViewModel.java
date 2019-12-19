package com.yuntechstudent.yuntechapp.ui.queryCourse;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Integer.parseInt;

public class QueryCourseViewModel extends ViewModel {

    public ArrayList<String> semestersList;
    public ArrayList<String> collegesList;
    public Map<Integer, ArrayList<String>> deptsMap;
    public Map<Integer, ArrayList<String>> deptsEngMap;

    public QueryCourseViewModel() {
        semestersList = new ArrayList<>();
        collegesList = new ArrayList<>();
        collegesList.addAll(Arrays.asList(new String[]{"請選擇查詢學院", "工程學院", "管理學院", "設計學院", "人文與科學學院", "未來學院"}));
        deptsMap = new HashMap<>();
        deptsEngMap = new HashMap<>();
        /*None*/
        deptsMap.put(0, new ArrayList<>(Arrays.asList(new String[]{"請選擇查詢系所"})));
        deptsEngMap.put(0, new ArrayList<>(Arrays.asList(new String[]{""})));
        /*工程*/
        deptsMap.put(1, new ArrayList<>(Arrays.asList(new String[]{"請選擇查詢系所", "工程科技菁英班", "工程學院", "工程科技研究所", "機械工程系", "電機工程系", "電子工程系", "環境與安全衛生工程系", "化學工程與材料工程系", "營建工程系", "資訊工程系"})));
        deptsEngMap.put(1, new ArrayList<>(Arrays.asList(new String[]{"", "UEX", "BEX", "GEX", "UEM", "UEE", "UEL", "UES", "UEC", "UEB", "UEF"})));
        /*管理*/
        deptsMap.put(2, new ArrayList<>(Arrays.asList(new String[]{"請選擇查詢系所", "管理學院", "高階管理碩士學位學程", "工商管理學士學位學程", "工業工程與管理系", "企業管理系", "資訊管理系", "財務金融系", "會計系", "國際管理學士學位學程", "創業管理碩士學位學程", "產業經營專業博士學位學程"})));
        deptsEngMap.put(2, new ArrayList<>(Arrays.asList(new String[]{"", "BMX", "GMD", "UME", "UMT", "UMB", "UMI", "UMF", "UMA", "UMP", "GMJ", "DMK"})));
        /*設計*/
        deptsMap.put(3, new ArrayList<>(Arrays.asList(new String[]{"請選擇查詢系所", "設計學院", "設計學研究所", "工業設計系", "視覺傳達設計系", "建築與室內設計系", "數位媒體設計系", "創意生活設計系"})));
        deptsEngMap.put(3, new ArrayList<>(Arrays.asList(new String[]{"", "BDX", "GDX", "UDT", "UDC", "UDS", "UDD", "UDO"})));
        /*人科*/
        deptsMap.put(4, new ArrayList<>(Arrays.asList(new String[]{"請選擇查詢系所", "人文與科學學院", "應用外語系", "文化資產維護系", "技術及職業教育研究所", "漢學應用研究所", "休閒運動研究所", "科技法律研究所", "材料科技研究所", "英語菁英學程", "師資培育中心"})));
        deptsEngMap.put(4, new ArrayList<>(Arrays.asList(new String[]{"", "BHX", "UHE", "UHA", "GHT", "GHC", "GHL", "GHW", "GHM", "UHN", "UHT"})));
        /*未來*/
        deptsMap.put(5, new ArrayList<>(Arrays.asList(new String[]{"請選擇查詢系所", "產業專案學士學位學程", "前瞻學士學位學程", "未來學院", "通識教育中心"})));
        deptsEngMap.put(5, new ArrayList<>(Arrays.asList(new String[]{"", "UFI", "UHI", "BFX", "UHX"})));
    }

    //--------------------處理學期--------------------//
    public int parseSemeToInt(String s){
        return parseInt(s.replaceAll("(學年第)|(學期)", ""));
    }
    public void setSemestersList(Map semesters){
        semestersList.clear();
        for(Object k: semesters.keySet())
            semestersList.add(semesters.get(k).toString());
        Comparator<String> compareBySemesters = (String o1, String o2) -> (parseSemeToInt(o1) >= parseSemeToInt(o2))? 1: -1;
        Collections.sort(semestersList, Collections.reverseOrder(compareBySemesters));
    }
    public ArrayList getSemestersList(){
        return semestersList;
    }
    //--------------------處理學院--------------------//
    public ArrayList getCollegesList(){
        return collegesList;
    }
    public String getCollegesCode(String s){
        if(collegesList.indexOf(s)==0)
            return "";
        return String.valueOf(collegesList.indexOf(s));
    }
    //--------------------處理學系--------------------//
    public ArrayList getDeptsList(String s){
        String college = getCollegesCode(s);
        if(college.equals("")) college = "0";
        return deptsMap.get(parseInt(college));
    }
    public String getDeptsCode(String college, String s){
        if(college.equals("")) college="0";
        ArrayList<String> deptsList = deptsMap.get(parseInt(college));
        ArrayList<String> deptsEngList = deptsEngMap.get(parseInt(college));
        return deptsEngList.get(deptsList.indexOf(s));
    }
}