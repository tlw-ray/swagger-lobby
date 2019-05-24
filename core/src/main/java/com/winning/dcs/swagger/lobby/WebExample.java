package com.winning.dcs.swagger.lobby;

import com.winning.dcs.swagger.lobby.util.SecuritySwaggerGenerator;
import io.swagger.models.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URL;
import java.util.*;

//带有页面的鉴权生成Swagger演示
@SpringBootApplication
@RestController
public class WebExample {
    public static void main(String[] args){
        //1. 以SpringBoot形式启动项目，依赖项需要spring-boot-starter-web和swagger-ui
        //2. 会用到src/main/resources/static下面加入静态页面swagger.html
        //3. 启动该WebExample后访问http://127.0.0.1:8080/swagger.html?userID=1, 可用来传递用户ID参数, 并获得对应的Swagger
        SpringApplication.run(WebExample.class, args);
    }

    @RequestMapping("/json")
    public String generateSwaggerJson(@RequestParam int userID) throws IOException {
        //1. 访问控制规则描述: 根据userID参数定义哪些RESTful路径下的哪些HttpMethod可以被看到。
        Map<String, Set<HttpMethod>> pathKeyMethodMap = null;
       if(userID == 1){
           pathKeyMethodMap = new HashMap();
           Set<HttpMethod> httpMethods2 = new HashSet();
           httpMethods2.add(HttpMethod.GET);
           pathKeyMethodMap.put("/pet/{petId}", httpMethods2);
       }else if(userID == 2){
           pathKeyMethodMap = new HashMap();
           Set<HttpMethod> httpMethods1 = new HashSet();
           httpMethods1.add(HttpMethod.POST);
           pathKeyMethodMap.put("/user", httpMethods1);
       }else if(userID == 3){
           pathKeyMethodMap = new HashMap();
           Set<HttpMethod> httpMethods1 = new HashSet();
           httpMethods1.add(HttpMethod.PUT);
           httpMethods1.add(HttpMethod.POST);
           pathKeyMethodMap.put("/pet", httpMethods1);
           Set<HttpMethod> httpMethods2 = new HashSet();
           httpMethods2.add(HttpMethod.GET);
           pathKeyMethodMap.put("/pet/findByStatus", httpMethods2);
       }

        //2. 对swagger来源URL根据上面描述的条件进行过滤产生新的Swagger
        URL url = new URL("http://127.0.0.1:8080/pet.json");
        SecuritySwaggerGenerator securitySwaggerGenerator = new SecuritySwaggerGenerator(url, pathKeyMethodMap);
        return securitySwaggerGenerator.filter();
    }
}
