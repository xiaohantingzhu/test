package com.esto.ilive.controller;

import com.esto.ilive.config.Configure;
import com.esto.ilive.domain.Page;
import com.esto.ilive.service.PageService;
import com.esto.ilive.utils.JsonObjectResult;
import com.esto.ilive.utils.JsonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 页面管理
 * @author: lixiao
 * @date: 2018/9/21
 */
@Api(value = "PageController", description = "页面管理接口")
@RestController
public class PageController {

    @Autowired
    private PageService pageService;

    @ApiOperation(value = "创建页面")
    @RequestMapping(value = "/api/page/create",method = RequestMethod.POST)
    public JsonResult createPage(@RequestBody Page page){
        pageService.insert(page);
        return Configure.setResult();
    }

    @ApiOperation(value = "删除页面")
    @RequestMapping(value ="/api/page/delete",method = RequestMethod.POST)
    public JsonResult batchDeletePage(@RequestBody Integer[] item){
        pageService.delectPageList(item);
        return Configure.setResult();
    }

    @ApiOperation(value = "更新页面")
    @PostMapping("/api/page/update")
    public JsonResult updatePage(@RequestBody Page page){
        pageService.updateByPrimaryKey(page);
        return Configure.setResult();
    }

    int i=0;
    @ApiOperation(value = "查询所有页面")
    @GetMapping("/api/page/list")
    public JsonObjectResult findAllPage() {
        JsonObjectResult jsonObjectResult = new JsonObjectResult();
        jsonObjectResult.setObj(pageService.getPageList());
        System.out.println(i++);
        return jsonObjectResult;
    }

    @ApiOperation(value = "根据角色ID查页面")
    @GetMapping("/api/page/listone")
    public JsonObjectResult findOnePage(@RequestParam(value = "roleid", required = true) Integer roleid) {
        JsonObjectResult jsonObjectResult = new JsonObjectResult();
        jsonObjectResult.setObj(pageService.getPageByRoleId(roleid));
        return jsonObjectResult;
    }

}
