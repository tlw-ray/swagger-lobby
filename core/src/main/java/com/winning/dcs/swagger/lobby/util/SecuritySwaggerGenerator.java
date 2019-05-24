package com.winning.dcs.swagger.lobby.util;

import io.swagger.models.HttpMethod;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.models.Tag;
import io.swagger.parser.SwaggerParser;
import org.apache.commons.io.IOUtils;
import springfox.documentation.spring.web.json.JacksonModuleRegistrar;
import springfox.documentation.spring.web.json.Json;
import springfox.documentation.spring.web.json.JsonSerializer;
import springfox.documentation.swagger2.configuration.Swagger2JacksonModule;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class SecuritySwaggerGenerator {

    private URL url;
    private Map<String, Set<HttpMethod>> pathKeyHttpMethodMap = new HashMap();

    public SecuritySwaggerGenerator(URL url, Map<String, Set<HttpMethod>> pathKeyHttpMethodMap) {
        this.url = url;
        this.pathKeyHttpMethodMap = pathKeyHttpMethodMap;
    }

    public Map<String, Set<HttpMethod>> getPathKeyHttpMethodMap() {
        return pathKeyHttpMethodMap;
    }

    public URL getUrl() {
        return url;
    }

    public String filter() throws IOException {
        String swaggerJson = IOUtils.toString(url);
        SwaggerParser swaggerParser = new SwaggerParser();
        Swagger swagger = swaggerParser.parse(swaggerJson);

        if(pathKeyHttpMethodMap != null) {
            //1. 遍历Swagger的所有Path, 移除不符合上述条件的Operation, 并记录不可用的PathKey
            Set<String> unavailablePathKeys = new HashSet();
            for (Map.Entry<String, Path> entry : swagger.getPaths().entrySet()) {
                String pathKey = entry.getKey();
                Set<HttpMethod> httpMethods = pathKeyHttpMethodMap.get(pathKey);
                if (httpMethods != null) {
                    //该路径符合筛选条件
                    Path path = entry.getValue();
                    //去除该路径下不在可以访问的HttpMethod中的
                    Set<HttpMethod> unavailableHttpMethods = unavailableHttpMethod(path, httpMethods);
                    for (HttpMethod httpMethod : unavailableHttpMethods) {
                        //去除不在条件中的httpMethod
                        path.set(httpMethod.name().toLowerCase(), null);
                    }
                } else {
                    unavailablePathKeys.add(pathKey);
                }
            }

            //2. 移除不可用的的Path
            for (String pathKey : unavailablePathKeys) {
                swagger.getPaths().remove(pathKey);
            }

            //3. 去除Swagger中未使用的Tag
            Set<Tag> unavailableTags = unavailableTag(swagger, pathKeyHttpMethodMap);
            for (Tag tag : unavailableTags) {
                swagger.getTags().remove(tag);
            }

            //4. 获得Swagger中未使用的Model, 并移除
            ModelScanner modelScanner = new ModelScanner();
            Set<String> usedModelNames = modelScanner.process(swagger);
            Set<String> unusedModelNames = new HashSet();
            for (String modelName : swagger.getDefinitions().keySet()) {
                if (!usedModelNames.contains(modelName)) {
                    unusedModelNames.add(modelName);
                }
            }
            for (String unusedModelName : unusedModelNames) {
                swagger.getDefinitions().remove(unusedModelName);
            }
        }

        //4. 输出经过过滤的Swagger
        JacksonModuleRegistrar jacksonModuleRegistrar = new Swagger2JacksonModule();
        List<JacksonModuleRegistrar> jacksonModuleRegistrars = new ArrayList();
        jacksonModuleRegistrars.add(jacksonModuleRegistrar);
        JsonSerializer jsonSerializer = new JsonSerializer(jacksonModuleRegistrars);
        Json json = jsonSerializer.toJson(swagger);
        return json.value();
    }

    private Set<HttpMethod> unavailableHttpMethod(Path path, Set<HttpMethod> availableHttpMethods){
        Set<HttpMethod> httpMethods = path.getOperationMap().keySet();
        httpMethods.removeAll(availableHttpMethods);
        return httpMethods;
    }

    private Set<Tag> unavailableTag(Swagger swagger, Map<String, Set<HttpMethod>> tagMethodMap){
        Set<Tag> unavailableTags = new HashSet();
        for(Tag tag:swagger.getTags()){
            String tagName = tag.getName();
            if(!tagMethodMap.keySet().contains(tagName)){
                unavailableTags.add(tag);
            }
        }
        return unavailableTags;
    }

}
