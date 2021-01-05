package top.xb.imgspace.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONUtil {
    // JSON字符串转List
    public static List<Map<String, Object>> jsonObject2List(JSONObject jsonObject, String keyName) {

        List<Map<String, Object>> list = new ArrayList<>();
        if(null != jsonObject){
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(keyName);

                for (Object jsonObject2:jsonArray) {
                    Map<String, Object> map = new HashMap<>();
                    for(Map.Entry<String,Object> entry:((JSONObject)jsonObject2).entrySet()){
                        map.put(entry.getKey(),entry.getValue());
                    }
                    list.add(map);
                }
                return list;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }


    //return msg转String
    public static JSONObject returnmsg(int returnvalue,String msg){
        return JSON.parseObject("{\"return\":" + returnvalue + ",\"returnmsg\":\"" + msg + "\"}");
    }


}
