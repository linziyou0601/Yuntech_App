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

        if(status.get("status").toString().equals("fail")){
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

            String redirectUrl = "";
            Pattern pattern = Pattern.compile("var redirectUrl = '(.*)';");
            Matcher matcher = pattern.matcher(document.select("script").first().html());
            if(matcher.find()) redirectUrl = matcher.group(1);

            //GET取得課表頁面
            con = Jsoup.connect(redirectUrl)
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
                for(int i = 0; i < 3; i++)
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
            result = dom.select("tr[class$=\"Row\"]");

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
            Document document = setCookieCAS("https://webapp.yuntech.edu.tw/WebNewCAS/StudentFile/");

            String redirectUrl = "";
            Pattern pattern = Pattern.compile("var redirectUrl = '(.*)';");
            Matcher matcher = pattern.matcher(document.select("script").first().html());
            if(matcher.find()) redirectUrl = matcher.group(1);

            //GET取得個人資料頁面
            con = Jsoup.connect(redirectUrl)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.120 Safari/537.36")
                    .cookies(cookiesCAS);

            document = con.get();

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
                data.put("profile_mainteacher", tr.get(1).select("#ctl00_ContentPlaceHolder1_oStudInfo_ClassTeacher").first().text() + "\n" +
                                                tr.get(1).select("#ctl00_ContentPlaceHolder1_oStudInfo_ClassTeacher a").first().attr("href").replace("mailto:", ""));
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
                data.put("title", card.select("div > div > div").first().text());
                result.add(data);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

}
