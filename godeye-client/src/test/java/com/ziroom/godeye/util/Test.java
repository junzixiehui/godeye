package com.ziroom.godeye.util;

import org.apache.http.client.utils.URIUtils;

import java.net.URI;
import java.net.URLEncoder;

/**
 * Description:
 *
 * @author: by qlb
 * @date: 2017/4/24 23:20
 * @version: 1.0
 */
public class Test {

    @org.junit.Test
    public void testUri() throws Exception{
        String param = "param1=" + URLEncoder.encode("中国", "UTF-8") + "&param2=value2";
        URI uri = URIUtils.createURI("http", "localhost", 8080,
         "/sshsky/index.html", param, null);
       System.out.println(uri);
    }
}
