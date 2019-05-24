package com.winning.dcs.swagger.lobby;

import com.winning.dcs.swagger.lobby.util.SecuritySwaggerGenerator;
import io.swagger.models.HttpMethod;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

//基于Main方法的示例
public class MainExample {
    public static void main(String[] args) throws IOException {
        //1. 访问控制规则描述: 定义哪些RESTful路径下的哪些HttpMethod可以被看到。
        Map<String, Set<HttpMethod>> pathKeyMethodMap = new HashMap();
        Set<HttpMethod> httpMethods2 = new HashSet();
        httpMethods2.add(HttpMethod.GET);
        pathKeyMethodMap.put("/rest/odsPersonRoleInfoController", httpMethods2);

        //2. 对swagger来源URL根据上面描述的条件进行过滤产生新的Swagger
        URL url = new URL("http://172.16.6.161:8080/ODS/rest/v2/api-docs");
        SecuritySwaggerGenerator securitySwaggerGenerator = new SecuritySwaggerGenerator(url, pathKeyMethodMap);
        //输出过滤后的Swagger
        System.out.println(securitySwaggerGenerator.filter());
    }
}
