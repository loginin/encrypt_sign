package cn;

import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.crypto.symmetric.AES;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Date;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class EncryptMain {

    static String key = "1160ace19fa44bc8857a2f20c885fcd9";
    static String url = "http://hello.world/a.do";
    static HttpClient client = HttpClients.createDefault();

    public static void main(String[] args) throws IOException {
        JSONObject req = reqJson();
        sign(req);
        JSONObject resp = sendReq(req);
        if (validateSign(resp)) {
            String encryptData = resp.getString("data");
            String originData = new AES(key.getBytes()).decryptStr(encryptData);
            System.out.println("解析后的response.data: " + originData);
        } else {
            System.out.println("响应验签失败.");
        }
    }

    private static boolean validateSign(JSONObject json) {
        TreeMap<String, String> treeMap = new TreeMap<>();
        json.forEach((k, v) -> treeMap.put(k, String.valueOf(v)));
        //移除sign字段
        String sign = treeMap.remove("sign");
        //其余参数按照字典序升序排列, 最后追加秘钥key
        String s = treeMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining("&"));
        s += key;
        String newSign = DigestUtil.sha1Hex(s);
        return newSign.equals(sign);
    }

    private static JSONObject sendReq(JSONObject req) throws IOException {
        HttpPost post = new HttpPost(url);
        post.setEntity(new StringEntity(req.toJSONString()));
        post.setHeader("content-type", "application/json");
        HttpResponse response = client.execute(post);
        String resp = null;
        if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 200) {
            resp = EntityUtils.toString(response.getEntity());
        }
        JSONObject json = JSONObject.parseObject(resp);
        int code = json.getIntValue("code");
        if (code == 200) {//成功
            return json;
        } else {
            System.out.println("出现了错误. 响应内容为:" + resp);
        }
        return null;
    }

    //签名
    private static void sign(JSONObject json) {
        String ts = json.getString("timestamp");
        String v = json.getString("version");
        String dv = json.getJSONArray("data").toJSONString();
        //1. 将data先AES,再base64
        String edv = new AES(key.getBytes()).encryptBase64(dv);
        //2. 按照字典序升序排列参数, 最后追加秘钥key
        String s = "data=" + edv + "&timestamp=" + ts + "&version=" + v + key;
        //3. 签名
        String sign = DigestUtil.sha1Hex(s);
        //4. 覆盖data原值, 同时将sign字段到参数列表里
        json.put("data", edv);
        json.put("sign", sign);
    }

    //构造请求参数
    private static JSONObject reqJson() {
        JSONObject json = new JSONObject();
        json.put("version", "1");
        json.put("timestamp", new Date().getTime());
        JSONArray array = new JSONArray();
        {
            JSONObject imei = new JSONObject();
            imei.put("type", "imei");
            JSONArray arr = new JSONArray();
            arr.add("12348");
            arr.add("afalkd565j");
            imei.put("ids", arr);
            array.add(imei);
        }
        {
            JSONObject oaid = new JSONObject();
            oaid.put("type", "oaid");
            JSONArray arr = new JSONArray();
            arr.add("1234");
            arr.add("45443222222");
            oaid.put("ids", arr);
            array.add(oaid);
        }
        json.put("data", array);
        return json;
    }


}
