package com.winning.dcs.swagger.lobby.util;

import io.swagger.models.*;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.properties.*;

import java.util.*;

//获得某个Model中所有用到的其它Model的名称
//可用于去除所有未用到的Model
public class ModelScanner {

    Set<String> simpleRefSet = new HashSet();
    Swagger swagger;

    public Set<String> process(Swagger swagger){
        this.swagger = swagger;
        //遍历Swagger.getPaths()中被用到的Model
        for(Path path:swagger.getPaths().values()){
            for(Map.Entry<HttpMethod, Operation> httpMethodOperationEntry:path.getOperationMap().entrySet()) {
                Operation operation = httpMethodOperationEntry.getValue();
                //遍历所有参数
                for (Parameter parameter : operation.getParameters()) {
                    if (parameter instanceof BodyParameter) {
                        BodyParameter bodyParameter = (BodyParameter) parameter;
                        Model model = bodyParameter.getSchema();
                        process(model);
                    }
                }

                //当返回200时用到的模型
                for (Map.Entry<String, Response> entry1 : operation.getResponses().entrySet()) {
                    Response response = entry1.getValue();
                    Model responseMode = response.getResponseSchema();
                    process(responseMode);
                }
            }
        }

        //遍历这些被用到的模型，找到被嵌套调用的模型
        Map<String, Model> refMadelMap = swagger.getDefinitions();
        Set<String> pathUsedRefSet = new HashSet(simpleRefSet);
        for(String simpleRef:pathUsedRefSet){
            Model model = refMadelMap.get(simpleRef);
            if(model != null){
                process(model);
            }
        }
        return simpleRefSet;
    }

    private void process(Model model){
        if(model instanceof RefModel){
            RefModel refModel = (RefModel)model;
            String simpleRef = refModel.getSimpleRef();
            process(simpleRef);
        }else if(model instanceof ModelImpl){
            ModelImpl modelImpl = (ModelImpl)model;
            Map<String, Property> propertyMap = modelImpl.getProperties();
            if(propertyMap != null) {
                for (Property property : modelImpl.getProperties().values()) {
                    process(property);
                }
            }
            Property property = modelImpl.getAdditionalProperties();
            process(property);

            //出现在Path段中的ModelImpl都是无名的无需注册
        }else if(model instanceof ArrayModel){
            ArrayModel arrayModel = (ArrayModel)model;
            Property property = arrayModel.getItems();
            if(property != null) {
                process(property);
            }
            Map<String, Property> propertyMap = arrayModel.getProperties();
            if(propertyMap != null) {
                for (Property property1 : arrayModel.getProperties().values()) {
                    process(property1);
                }
            }
        }else if(model instanceof ComposedModel){
            ComposedModel composedModel = (ComposedModel) model;
            Model child = composedModel.getChild();
            process(child);
            for(Model model1 : composedModel.getAllOf()){
                process(model1);
            }
            for(RefModel refModel:composedModel.getInterfaces()){
                process(refModel);
            }
        }
    }

    private void process(Property property){
        if(property != null){
            if(property instanceof RefProperty) {
                RefProperty refProperty = (RefProperty) property;
                String simpleRef = refProperty.getSimpleRef();
                process(simpleRef);
            }else if(property instanceof ArrayProperty){
                ArrayProperty arrayProperty = (ArrayProperty) property;
                Property property1 = arrayProperty.getItems();
                process(property1);
            }else if(property instanceof MapProperty){
                MapProperty mapProperty = (MapProperty)property;
                Property property1 = mapProperty.getAdditionalProperties();
                process(property1);
            }else if(property instanceof ObjectProperty){
                ObjectProperty objectProperty = (ObjectProperty)property;
                Map<String, Property> propertyMap = objectProperty.getProperties();
                if(propertyMap != null) {
                    for (Property property1 : propertyMap.values()) {
                        process(property1);
                    }
                }
            }else if(property instanceof ComposedProperty){
                ComposedProperty composedProperty = (ComposedProperty)property;
                List<Property> properties = composedProperty.getAllOf();
                if(properties != null){
                    for(Property property1:properties){
                        process(property1);
                    }
                }
            }else{
                //非复合类型
            }
        }
    }

    private void process(String simpleRef){
        if(!simpleRefSet.contains(simpleRef)){
            simpleRefSet.add(simpleRef);
            Model model = swagger.getDefinitions().get(simpleRef);
            process(model);
        }
    }
}
