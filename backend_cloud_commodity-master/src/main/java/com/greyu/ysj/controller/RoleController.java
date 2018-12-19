package com.esto.ilive.controller;

import ch.qos.logback.core.util.FileUtil;
import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.esto.ilive.config.Configure;
import com.esto.ilive.domain.OrderDeviceInfo;
import com.esto.ilive.domain.PageAllotion;
import com.esto.ilive.domain.Role;
import com.esto.ilive.domain.RolePagesKey;
import com.esto.ilive.service.PageAllocationService;
import com.esto.ilive.service.RoleService;
import com.esto.ilive.utils.ExcelUtiles;
import com.esto.ilive.utils.JsonObjectResult;
import com.esto.ilive.utils.JsonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

/**
 * 角色管理
 * @author: lixiao
 * @date: 2018/9/21
 */
@Api(value = "RoleController", description = "角色管理接口")
@RestController
public class RoleController {

    @Autowired
    private RoleService roleService;

    @Autowired
    private PageAllocationService pageAllocationService;

    @ApiOperation(value = "创建角色")
    @RequestMapping(value = "/api/role/create",method = RequestMethod.POST)
    public JsonResult createRole(@RequestBody Role role){
        roleService.insert(role);
        return Configure.setResult();
    }

    @ApiOperation(value = "删除角色")
    @RequestMapping(value ="/api/role/delete",method = RequestMethod.POST)
    public JsonResult batchDeleteRole(@RequestBody Integer[] item){
        roleService.delectRoleList(item);
        return Configure.setResult();
    }

    @ApiOperation(value = "更新角色")
    @RequestMapping(value = "/api/role/update",method = RequestMethod.POST)
    public JsonResult updateRole(@RequestBody Role role){
        roleService.updateByPrimaryKey(role);
        return Configure.setResult();
    }

    @ApiOperation(value = "查询所有角色")
    @RequestMapping(value = "/api/role/list",method = RequestMethod.GET)
    public JsonObjectResult findAllRole() {
        JsonObjectResult jsonObjectResult = new JsonObjectResult();
        System.out.println(roleService.getRoleList().size());
        jsonObjectResult.setObj(roleService.getRoleList());
        return jsonObjectResult;
    }

    @ApiOperation(value = "根据ID查角色")
    @RequestMapping(value = "/api/role/{id}",method = RequestMethod.GET)
    public JsonObjectResult findOneRole(@PathVariable("id") Integer id) {
        JsonObjectResult jsonObjectResult = new JsonObjectResult();
        jsonObjectResult.setObj(roleService.selectByPrimaryKey(id));
        return jsonObjectResult;
    }

    @ApiOperation(value = "角色页面分配接口")
    @PostMapping("/api/role/page_allocation")
    public JsonResult pageAllocation(@RequestBody PageAllotion pageAllotion){
        pageAllocationService.pageAllocation(pageAllotion);
        return Configure.setResult();
    }


}
