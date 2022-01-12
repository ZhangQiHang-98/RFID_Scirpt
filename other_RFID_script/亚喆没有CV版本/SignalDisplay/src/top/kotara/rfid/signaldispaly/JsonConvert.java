package top.kotara.rfid.signaldispaly;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class JsonConvert {
    private static final String colon="=";
    private static final String quotation_marks="[-o-]";
    public static String JsonToFilename(JSONObject jo){
        String jsonstr=jo.toJSONString();
        String pattern1="([\"][ ]*):([ ]*[\"])";
        String njson1=jsonstr.replaceAll(pattern1,"$1"+colon+"$2");
        String pattern2="([^\\\\])([\"])";
        String njson2=njson1.replaceAll(pattern2,"$1"+quotation_marks).replace("\n","");
        return njson2;
    }
    public static JSONObject FilenameToJson(String fakejsonstr){
        String jsonstr=fakejsonstr.replaceAll(quotation_marks,"\"").replaceAll("([\"][ ]*)=([ ]*[\"])","$1:$2");
        JSONObject jo = JSON.parseObject(jsonstr);
        return jo;
    }
}
