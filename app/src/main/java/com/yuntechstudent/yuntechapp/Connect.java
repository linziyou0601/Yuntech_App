package com.yuntechstudent.yuntechapp;

import android.util.Base64;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Connect {
    public static Connection con;
    public static Map<String, String> cookies;
    public static Map<String, String> cookiesCAS;
    public static Map<String, String> queryCourseNext;

    //--------------------登入--------------------//
    public Map login(String username, String password) {
        Map<String, Map> result = new HashMap<>();
        Map<String, String> status = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        try {
            //----------建立連線資料----------//
            con = Jsoup.connect("https://webapp.yuntech.edu.tw/YunTechSSO/Account/Login")
                       .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");

            //----------登入表單資料----------//
            //GET取得頁面
            Connection.Response response = con.execute();
            Document document = Jsoup.parse(response.body());
            //填入資料
            Map<String, String> datas = new HashMap<>();
            datas.put("__RequestVerificationToken", document.select("input[name=\"__RequestVerificationToken\"]").first().attr("value"));
            datas.put("pRememberMe", "true");
            datas.put("redirectUrl", "");
            datas.put("pLoginName", username);
            datas.put("pLoginPassword", password);
            datas.put("BDC_VCID_YuntechSSO", document.select("#BDC_VCID_YuntechSSO").first().attr("value"));
            datas.put("BDC_BackWorkaround_YuntechSSO", document.select("#BDC_BackWorkaround_YuntechSSO").first().attr("value"));
            datas.put("BDC_Hs_YuntechSSO", document.select("#BDC_Hs_YuntechSSO").first().attr("value"));
            datas.put("BDC_SP_YuntechSSO", document.select("#BDC_SP_YuntechSSO").first().attr("value"));
            datas.put("pSecretString", "");

            //----------登入----------//
            //POST登入表單
            con = Jsoup.connect("https://webapp.yuntech.edu.tw/YunTechSSO/Account/Login")
                       .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                       .method(Connection.Method.POST)
                       .cookies(response.cookies())
                       .data(datas);
            response = con.execute();
            document = Jsoup.parse(response.body());

            //----------驗證登入狀態----------//
            Elements check = document.select("div.profile-element");
            if(check.size() == 0){
                cookies = null;
                status.put("status", "fail");
            }
            else{
                cookies = response.cookies();
                //儲存使用者名稱
                Elements profileDom = document.select("div.profile-element > a#NavHeaderNameLink > span");
                status.put("status", "success");
                data.put("student_name", profileDom.get(1).text());
                data.put("student_major", profileDom.get(0).text());
                //儲存使用者圖片
                byte[] imgBytes = Jsoup.connect("https://webapp.yuntech.edu.tw/YunTechSSO/Photo").cookies(cookies).ignoreContentType(true).execute().bodyAsBytes();
                String imgString = Base64.encodeToString(imgBytes, Base64.NO_WRAP);
                data.put("student_image", imgString);

            }
        } catch (IOException e) {
            status.put("status", "fail");
            e.printStackTrace();
        }

        if(status.get("status").equals("fail")){
            data.put("student_name", "未登入");
            data.put("student_major", "請先登入");
            data.put("student_image", "");
        }

        result.put("status", status);
        result.put("data", data);
        return result;
    }

    //--------------------取得教務系統Cookie--------------------//
    public Document setCookieCAS(String url) {
        Document result = new Document("");
        try {
            //----------建立連線資料----------//
            con = Jsoup.connect(url)
                       .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                       .cookies(cookies);

            //----------取得教務系統連線資料----------//
            //GET取得轉跳頁面
            Connection.Response response = con.execute();
            result = Jsoup.parse(response.body());
            cookiesCAS = response.cookies();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    //--------------------取得學期資料--------------------//
    public Map getSemester(String url) {
        Map<String, Map> result = new HashMap<>();
        Map<String, String> status = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        Map<String, Document> dom = new HashMap<>();
        try {
            //----------建立連線資料----------//
            Document document = setCookieCAS(url);

            /*String redirectUrl = "";
            Pattern pattern = Pattern.compile("var redirectUrl = '(.*)';");
            Matcher matcher = pattern.matcher(document.select("script").first().html());
            if(matcher.find()) redirectUrl = matcher.group(1);*/

            //GET取得課表頁面
            con = Jsoup.connect(url)
                       .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                       .cookies(cookiesCAS);

            document = con.get();

            //----------驗證課表頁面狀態----------//
            Elements seme = document.select("#ctl00_ContentPlaceHolder1_AcadSeme > option");
            if(seme.size() == 0){
                status.put("status", "fail");
            }
            else{
                status.put("status", "success");
                dom.put("document", document);
                for(int i = 0; i < seme.size(); i++)
                    data.put(seme.get(i).attr("value"), seme.get(i).text());
            }
        } catch (IOException e) {
            status.put("status", "fail");
            e.printStackTrace();
        }

        result.put("status", status);
        result.put("data", data);
        result.put("dom", dom);
        return result;
    }

    //--------------------取得課表--------------------//
    public Elements courseData(String seme, Document document) {
        Elements result = new Elements();
        try {
            //----------學期課表資料----------//
            //填入資料
            Map<String, String> datas = new HashMap<>();
            datas.put("ctl00_ToolkitScriptManager1_HiddenField", document.select("#ctl00_ToolkitScriptManager1_HiddenField").first().attr("value"));
            datas.put("__EVENTTARGET", "ctl00$ContentPlaceHolder1$AcadSeme");
            datas.put("__EVENTARGUMENT", "");
            datas.put("__LASTFOCUS", document.select("#__LASTFOCUS").first().attr("value"));
            datas.put("__VIEWSTATE", document.select("#__VIEWSTATE").first().attr("value"));
            datas.put("__VIEWSTATEGENERATOR", document.select("#__VIEWSTATEGENERATOR").first().attr("value"));
            datas.put("__EVENTVALIDATION", document.select("#__EVENTVALIDATION").first().attr("value"));
            datas.put("ctl00$ContentPlaceHolder1$AcadSeme", seme);

            //GET取得課表頁面
            con = Jsoup.connect("https://webapp.yuntech.edu.tw/WebNewCAS/StudentFile/Course/")
                       .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                       .method(Connection.Method.POST)
                       .cookies(cookiesCAS)
                       .data(datas);

            Connection.Response response = con.execute();
            Document dom = Jsoup.parse(response.body());
            Element table = dom.select("#ctl00_ContentPlaceHolder1_StudCour_GridView").first();
            result = table.select("tr[class$=\"Row\"]");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    //--------------------取得個人資料--------------------//
    public Map studentProfile() {
        Map<String, Map> result = new HashMap<>();
        Map<String, String> status = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        try {
            //----------建立連線資料----------//
            //GET取得個人資料頁面
            con = Jsoup.connect("https://webapp.yuntech.edu.tw/WebNewCAS/StudentFile/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                    .cookies(cookies);

            Document document = con.get();

            //----------驗證個人資料頁面狀態----------//
            Elements tr = document.select("#ctl00_ContentPlaceHolder1_oStudInfo_StudInfoTable tr");
            if(tr.size() == 0){
                status.put("status", "fail");
            }
            else{
                status.put("status", "success");
                data.put("profile_class", tr.get(1).select("#ctl00_ContentPlaceHolder1_oStudInfo_EduSys").first().text() + "／" +
                                          tr.get(1).select("#ctl00_ContentPlaceHolder1_oStudInfo_StudClass").first().text());
                data.put("profile_double_major", tr.get(3).select("td").get(3).text());
                if(tr.get(1).select("#ctl00_ContentPlaceHolder1_oStudInfo_ClassTeacher a").size() > 0){
                    data.put("profile_mainteacher", tr.get(1).select("#ctl00_ContentPlaceHolder1_oStudInfo_ClassTeacher").first().text() + "\n" +
                                                    tr.get(1).select("#ctl00_ContentPlaceHolder1_oStudInfo_ClassTeacher a").first().attr("href").replace("mailto:", ""));
                }else{
                    data.put("profile_mainteacher", "");
                }
                data.put("profile_graduation", tr.get(0).select("td").get(1).text().replaceAll("(學年度第)", "-").replaceAll("(學期)", ""));
                data.put("profile_entrance", tr.get(0).select("td").get(3).text());
                data.put("profile_academic_status", tr.get(3).select("td").get(1).text());
            }
        } catch (IOException e) {
            status.put("status", "fail");
            e.printStackTrace();
        }

        result.put("status", status);
        result.put("data", data);
        return result;
    }

    //--------------------取得成績--------------------//
    public Elements scoreData(String seme, Document document) {
        Elements result = new Elements();
        try {
            //----------學期成績資料----------//
            //填入資料
            Map<String, String> datas = new HashMap<>();
            datas.put("ctl00$ContentPlaceHolder1$ToolkitScriptManager1", "ctl00$ContentPlaceHolder1$UpdatePanel1|ctl00$ContentPlaceHolder1$AcadSeme");
            datas.put("ctl00_ContentPlaceHolder1_ToolkitScriptManager1_HiddenField", document.select("#ctl00_ContentPlaceHolder1_ToolkitScriptManager1_HiddenField").first().attr("value"));
            datas.put("ctl00_ContentPlaceHolder1_TabContainer1_ClientState", document.select("#ctl00_ContentPlaceHolder1_TabContainer1_ClientState").first().attr("value"));
            datas.put("ctl00$ContentPlaceHolder1$TabContainer1$TabPanel2$ReportKind", "1");
            datas.put("__ASYNCPOST", "true");
            datas.put("__EVENTTARGET", "ctl00$ContentPlaceHolder1$AcadSeme");
            datas.put("__EVENTARGUMENT", "");
            datas.put("__LASTFOCUS", document.select("#__LASTFOCUS").first().attr("value"));
            datas.put("__VIEWSTATE", document.select("#__VIEWSTATE").first().attr("value"));
            datas.put("__VIEWSTATEGENERATOR", document.select("#__VIEWSTATEGENERATOR").first().attr("value"));
            datas.put("__EVENTVALIDATION", document.select("#__EVENTVALIDATION").first().attr("value"));
            datas.put("ctl00$ContentPlaceHolder1$AcadSeme", seme);

            //GET取得課表頁面
            con = Jsoup.connect("https://webapp.yuntech.edu.tw/WebNewCAS/StudentFile/Score/")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                    .method(Connection.Method.POST)
                    .cookies(cookiesCAS)
                    .data(datas);

            Connection.Response response = con.execute();
            Document dom = Jsoup.parse(response.body());

            result = dom.select("#ctl00_ContentPlaceHolder1_TabContainer1_TabPanel1 table");

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    //--------------------取得應修未修畢業學分資料--------------------//
    public Map studentGraduate() {
        Map<String, Map> result = new HashMap<>();
        Map<String, String> status = new HashMap<>();
        Map<String, String> data = new HashMap<>();
        try {
            //----------建立連線資料----------//
            //GET取得應修未修畢業學分頁面
            con = Jsoup.connect("https://webapp.yuntech.edu.tw/WebNewCAS/Graduation/Score/StudGradCour.aspx")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                    .cookies(cookies);

            Document document = con.get();

            //----------驗證應修未修畢業學分頁面狀態----------//
            String str = "#ctl00_ContentPlaceHolder1_oStudGradInfo_";
            Element table = document.select("#ctl00_ContentPlaceHolder1_StudentData_Panel").first();
            if(table == null){
                status.put("status", "fail");
            }
            else{
                status.put("status", "success");
                //-----
                data.put("stuName", table.select(str + "StudName").first().text());
                data.put("stuNum", table.select(str + "StudNo").first().text());
                //-----
                data.put("masterDept", table.select(str + "MasterDept").first().text());
                data.put("doubDept", table.select(str + "DoubDept").first().text());
                data.put("minorDept", table.select(str + "MinorDept").first().text());
                data.put("degree", table.select(str + "MasterDegree").first().text() +
                                         table.select(str + "DoubDegree").first().text() +
                                         table.select(str + "MinorDegree").first().text() );
                //-----
                data.put("dateEnter", table.select(str + "arvdate").first().text());
                data.put("dateGrad", table.select(str + "graddate").first().text());
                data.put("totalCredits", table.select(str + "totalCredits").first().text());
                data.put("gradScore", table.select(str + "GrdScore").first().text());
                data.put("gradAvg", table.select(str + "GrdAvg").first().text());
                data.put("testScore", table.select(str + "TestScore").first().text());
                data.put("topicCHN", table.select(str + "chntopic").first().text());
                data.put("topicENG", table.select(str + "engtopic").first().text());
                //-----
                data.put("grdMP", table.select(str + "Grd_MP").first().text());
                data.put("getMP", table.select(str + "Get_MP").first().text());
                data.put("nsMP", table.select(str + "NS_MP").first().text());
                data.put("outMP", table.select(str + "WithOut_MP").first().text());
                //-----
                data.put("grdIJ", table.select(str + "Grd_I").first().text() + "/" + table.select(str + "Grd_J").first().text());
                data.put("getIJ", table.select(str + "Get_I").first().text() + "/" + table.select(str + "Get_J").first().text());
                data.put("nsIJ", table.select(str + "NS_I").first().text() + "/" + table.select(str + "NS_J").first().text());
                data.put("outIJ", table.select(str + "WithOut_I").first().text() + "/" + table.select(str + "WithOut_J").first().text());
                //-----
                data.put("grdCom", table.select(str + "Grd_Com").first().text());
                data.put("getCom", table.select(str + "Get_Com").first().text());
                data.put("nsCom", table.select(str + "NS_Com").first().text());
                data.put("outCom", table.select(str + "WithOut_Com").first().text());
                //-----
                data.put("grdMOpt", table.select(str + "Grd_MOpt").first().text());
                data.put("getMOpt1", table.select(str + "Get_M1").first().text());
                data.put("getMOpt2", table.select(str + "Get_M2").first().text());
                data.put("nsMOpt", table.select(str + "NS_MOpt").first().text());
                data.put("outMOpt", table.select(str + "WithOut_MOpt").first().text());
                //-----
                data.put("title_Grd_EOSpe", table.select(str + "Grd_EOSpe").first().text());
                data.put("grdOSpe", table.select(str + "Grd_OSpe").first().text());
                data.put("getOSpe1", table.select(str + "Get_O1").first().text());
                data.put("getOSpe2", table.select(str + "Get_O2").first().text());
                data.put("getOSpe3", table.select(str + "Get_EOSpe").first().text());
                data.put("nsOSpe1", table.select(str + "NS_OSpe").first().text());
                data.put("nsOSpe2", table.select(str + "NS_EOSpe").first().text());
                data.put("outOSpe", table.select(str + "WithOut_OSpe").first().text());
                //-----
                data.put("grdTotal", table.select(str + "Grd_Total").first().text());
                data.put("getTotal", table.select(str + "Get_Total").first().text());
                data.put("nsTotal", table.select(str + "NS_Total").first().text());
                data.put("outTotal", table.select(str + "WithOut_Total").first().text());
                //-----
                data.put("grdServ", table.select(str + "Grd_Serv").first().text());
                data.put("getServ", table.select(str + "Get_Serv").first().text());
                data.put("nsServ", table.select(str + "NS_Serv").first().text());
                data.put("outServ", table.select(str + "WithOut_Serv").first().text());
                //-----
                data.put("grdDB", table.select(str + "StudCreditTable tr").get(2).select("td").get(8).text());
                data.put("getDB", table.select(str + "Get_DB").first().text());
                data.put("nsDB", table.select(str + "StudCreditTable tr").get(4).select("td").get(9).text());
                data.put("outDB", table.select(str + "StudCreditTable tr").get(5).select("td").get(8).text());
                //-----
                data.put("grdUHT", table.select(str + "StudCreditTable tr").get(2).select("td").get(9).text());
                data.put("getUHT", table.select(str + "Get_UHT").first().text());
                data.put("nsUHT", table.select(str + "StudCreditTable tr").get(4).select("td").get(10).text());
                data.put("outUHT", table.select(str + "StudCreditTable tr").get(5).select("td").get(9).text());
                //-----
                data.put("passEng", table.select(str + "EngPass").first().text());
                data.put("passIntern", table.select(str + "InternshipPass").first().text());
                data.put("passEthics", table.select(str + "EthicsPass").first().text());
                //-----
                data.put("distanceCredits", table.select(str + "Distance_Credits").first().text());
                data.put("notExistsCourse", table.select(str + "NotExistsFlowChart").first().text().replaceAll("、", "\n"));
            }
        } catch (IOException e) {
            status.put("status", "fail");
            e.printStackTrace();
        }

        result.put("status", status);
        result.put("data", data);
        return result;
    }

    //--------------------取得最新消息--------------------//
    public ArrayList<Map<String, String>> newsData(int start) {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        try {
            //----------最新消息資料----------//
            //GET取得最新消息
            con = Jsoup.connect("https://www.yuntech.edu.tw/index.php/2019-07-25-11-32-12/2019-04-10-08-05-51/itemlist?start=" + start)
                       .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
            Document document = con.get();
            Element el = document.select("#k2Container").first();

            for(Element card: el.select(".cardblock")) {
                Map<String, String> data = new HashMap<>();
                data.put("date", card.select(".cardheader .carddate").first().text());
                data.put("unit", card.select(".cardheader .cardunit a").first().text());
                data.put("title", card.select("a.cardtxtclick").first().text());
                data.put("link", "https://www.yuntech.edu.tw" + card.select("a.cardtxtclick").first().attr("href"));
                result.add(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    //--------------------取得焦點消息--------------------//
    public ArrayList<Map<String, String>> spotlightData() {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        try {
            //----------焦點消息資料----------//
            //GET取得焦點消息
            con = Jsoup.connect("https://www.yuntech.edu.tw/index.php/2019-04-10-08-04-08")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
            Document document = con.get();
            Element el = document.select("#sp_list").first();

            for(Element card: el.select(".col-md-3")) {
                Map<String, String> data = new HashMap<>();
                data.put("title", card.select("span").first().text());
                data.put("link", "https://www.yuntech.edu.tw" + card.select("a").first().attr("href"));
                result.add(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    //--------------------往火車站時刻表--------------------//
    public ArrayList<Map<String, String>> toStationData() {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        try {
            //----------往火車站----------//
            //GET取得時刻表
            con = Jsoup.connect("https://ags.yuntech.edu.tw/index.php?option=com_content&task=view&id=1396&Itemid=547&from=News")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
            Document document = con.get();

            //存metadata
            Element el = document.select(".MsoNormalTable").first().select("thead").first();
            Map<String, String> data = new HashMap<>();
            data.put("madeTime", el.select("tr").get(0).select("td > p > span").get(0).text() + el.select("tr").get(0).select("td > p > span").get(1).text());
            data.put("originSpot", "校門口");
            result.add(data);

            //存schedule
            el = document.select(".MsoNormalTable").first().select("tbody").first();
            for(Element tr: el.select("tr")) {
                data = new HashMap<>();
                data.put("depart", tr.select("td").get(1).text());
                data.put("company", tr.select("td").get(4).text());
                data.put("number", tr.select("td").get(5).text());
                data.put("origin", tr.select("td").get(6).text().split("→")[0]);
                data.put("dest", tr.select("td").get(6).text().split("→")[1]);
                data.put("remark", tr.select("td").get(7).text());
                result.add(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    //--------------------往雲科大時刻表--------------------//
    public ArrayList<Map<String, String>> toYuntechData() {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        try {
            //----------往雲科大----------//
            //GET取得時刻表
            con = Jsoup.connect("https://ags.yuntech.edu.tw/index.php?option=com_content&task=view&id=1396&Itemid=547&from=News")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36");
            Document document = con.get();

            //存metadata
            Element el = document.select(".MsoNormalTable").get(1).select("thead").first();
            Map<String, String> data = new HashMap<>();
            data.put("madeTime", el.select("tr").get(0).select("td > p > span").get(1).text() + el.select("tr").get(0).select("td > p > span").get(2).text());
            data.put("originSpot", "後火車站");
            result.add(data);

            //存schedule
            el = document.select(".MsoNormalTable").get(1).select("tbody").first();
            for(Element tr: el.select("tr")) {
                data = new HashMap<>();
                data.put("depart", tr.select("td").get(2).text());
                data.put("company", tr.select("td").get(3).text());
                data.put("number", tr.select("td").get(4).text());
                data.put("origin", tr.select("td").get(5).text().split("→")[0]);
                data.put("dest", tr.select("td").get(5).text().split("→")[1]);
                data.put("remark", tr.select("td").get(6).text());
                result.add(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    //--------------------選擇學院--------------------//
    public Map getQueryCourseCollege(Map<String, String> datas) {
        Map<String, Map> result = new HashMap<>();
        Map<String, String> status = new HashMap<>();
        Map<String, Document> dom = new HashMap<>();
        try {
            //----------建立連線資料----------//
            //GET取得查詢頁面
            con = Jsoup.connect("https://webapp.yuntech.edu.tw/WebNewCAS/Course/QueryCour.aspx")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                    .method(Connection.Method.POST)
                    .data(datas);
            Connection.Response response = con.execute();
            Document document = Jsoup.parse(response.body());
            dom.put("document", document);
            status.put("status", "success");
        } catch (IOException e) {
            status.put("status", "fail");
            e.printStackTrace();
        }
        result.put("dom", dom);
        return result;
    }

    //--------------------取得所有課程--------------------//
    public ArrayList<Map<String, String>> queryCourseData(Map<String, String> datas, int page) {
        ArrayList<Map<String, String>> result = new ArrayList<>();
        try {
            //----------取得課程資料----------//
            //GET取得課程頁面
            con = Jsoup.connect("https://webapp.yuntech.edu.tw/WebNewCAS/Course/QueryCour.aspx")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                    .method(Connection.Method.POST)
                    .data((page>1)? queryCourseNext: datas);
            Connection.Response response = con.execute();
            Document document = Jsoup.parse(response.body());
            Element el = document.select("#ctl00_ContentPlaceHolder1_Course_GridView").first();

            //----------資料存入List----------//
            if(el.select("[class$='Row']").size() > 0){
                for (Element row: el.select("[class$='Row']")) {
                    Map<String, String> item = new HashMap<>();
                    item.put("Serial_No", row.select("td").get(0).text());
                    item.put("Curriculum_No", row.select("td").get(1).text());
                    item.put("Course_Name_CHT", row.select("td").get(2).select("a").first().text());
                    item.put("Course_Name_ENG", row.select("td").get(2).select("span").first().text());
                    item.put("Class", row.select("td").get(3).text());
                    item.put("Team", row.select("td").get(4).text());
                    item.put("Required_Elective", row.select("td").get(5).select("span").first().text());
                    item.put("Credits", row.select("td").get(6).text());
                    item.put("Schedule_Location", row.select("td").get(7).text());
                    item.put("Instructor", row.select("td").get(8).text());
                    item.put("Sel", row.select("td").get(9).text());
                    item.put("Max", row.select("td").get(10).text());
                    item.put("Remarks", row.select("td").get(11).text());
                    Element link = row.select("td").get(2).select("a").first();
                    if(link.hasAttr("href")){
                        String linkUrl = "";
                        Pattern pattern = Pattern.compile("javascript:openwindow\\('(.*)'\\)");
                        Matcher matcher = pattern.matcher(link.attr("href").replaceAll("&amp;", "&"));
                        if(matcher.find()) linkUrl = matcher.group(1);
                        item.put("Link", "https://webapp.yuntech.edu.tw" + linkUrl);
                    }else{
                        item.put("Link", "");
                    }

                    result.add(item);
                }
            }

            //----------儲存下次頁面隱藏值----------//
            Map<String, String> item = new HashMap<>();
            if(document.select("#ctl00_ContentPlaceHolder1_PageControl1_NextPage").first().hasAttr("href")){
                queryCourseNext = new HashMap<>();
                queryCourseNext.put("ctl00$ToolkitScriptManager1", "ctl00$ContentPlaceHolder1$UpdatePanel2|ctl00$ContentPlaceHolder1$PageControl1$NextPage");
                queryCourseNext.put("ctl00_ToolkitScriptManager1_HiddenField", ";;AjaxControlToolkit, Version=4.1.60919.0, Culture=neutral, PublicKeyToken=28f01b0e84b6d53e:zh-TW:ab75ae50-1505-49da-acca-8b96b908cb1a:f2c8e708:de1feab2:720a52bf:f9cec9bc:589eaa30:a67c2700:ab09e3fe:87104b7c:8613aea7:3202a5a2:be6fb298");
                queryCourseNext.put("__LASTFOCUS", document.select("#__LASTFOCUS").first().attr("value"));
                queryCourseNext.put("__EVENTTARGET", "ctl00$ContentPlaceHolder1$PageControl1$NextPage");
                queryCourseNext.put("__EVENTARGUMENT", document.select("#__EVENTARGUMENT").first().attr("value"));
                queryCourseNext.put("__LASTFOCUS", document.select("#__LASTFOCUS").first().attr("value"));
                queryCourseNext.put("__VIEWSTATE", document.select("#__VIEWSTATE").first().attr("value"));
                queryCourseNext.put("__VIEWSTATEGENERATOR", document.select("#__VIEWSTATEGENERATOR").first().attr("value"));
                queryCourseNext.put("__EVENTVALIDATION", document.select("#__EVENTVALIDATION").first().attr("value"));
                queryCourseNext.put("ctl00$ContentPlaceHolder1$PageControl1$Pages", String.valueOf(page));
                queryCourseNext.put("ctl00$ContentPlaceHolder1$PageControl1$PageSize", "20");
                queryCourseNext.put("ctl00$ContentPlaceHolder1$Cour_Remark", "");
                item.put("next", "true");
            }else{
                item.put("next", "false");
            }
            result.add(item);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
