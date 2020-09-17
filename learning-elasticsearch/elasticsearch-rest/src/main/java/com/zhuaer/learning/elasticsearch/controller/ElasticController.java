package com.zhuaer.learning.elasticsearch.controller;

import com.zhuaer.learning.elasticsearch.service.impl.BaseElasticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


/**
 * @ClassName ElasticController
 * @Description TODO
 * @Author zhua
 * @Date 2020/9/10 17:05
 * @Version 1.0
 */
@Slf4j
@RestController
public class ElasticController {

    @Autowired
    public BaseElasticService baseElasticService;



    /**
     * 判断索引是否存在；存在-TRUE，否则-FALSE
     * @param index
     * @return
     */
    @RequestMapping(value = "/exist/{index}")
    public void indexExist(@PathVariable(value = "index") String index){

        try {
            if(!baseElasticService.isExistsIndex(index)){
                log.error("index={},不存在",index);

            } else {
                log.info(" 索引已经存在, " + index);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
