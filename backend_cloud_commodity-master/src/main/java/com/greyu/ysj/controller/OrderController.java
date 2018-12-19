package com.esto.ilive.controller;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.esto.ilive.config.Configure;
import com.esto.ilive.config.WebLogAspect;
import com.esto.ilive.dao.OrderinformationMapper;
import com.esto.ilive.domain.*;
import com.esto.ilive.pojo.OrderDeviceKeyWithName;
import com.esto.ilive.service.*;
import com.esto.ilive.service.redisMessage.ActiveSendMsg;
import com.esto.ilive.service.redisMessage.ActiveService;
import com.esto.ilive.service.redisMessage.RedisService;
import com.esto.ilive.utils.JsonObjectResult;
import com.esto.ilive.utils.JsonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * 订单管理
 * @author: lixiao
 * @date: 2018/9/26
 */
@Api(value = "OrderController", description = "订单管理接口")
@RestController
public class OrderController {

    @Autowired
    private OrderinformationMapper orderinformationMapper;

    @Autowired
    private OrderDeviceService orderDeviceService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ActiveService activeService;

    @Autowired
    private ActiveSendMsg activeSendMsg;

    @Autowired
    private DeviceService deviceService;

    @ApiOperation(value = "创建订单")
    @PostMapping("/api/order/create")
    public JsonResult createOrder(@RequestBody Orderinformation orderinformation){
        JsonResult jsonResult = new JsonResult();
        try{
            if(orderService.insert(orderinformation) == 1){
                return Configure.setResult();
            }
        }catch(Exception e){
            e.printStackTrace();
            jsonResult.setCode(300);
            jsonResult.setMessage("订单号重复");
            return jsonResult;
        }finally{
            return  jsonResult;
        }
    }

    @ApiOperation(value = "删除订单")
    @PostMapping("/api/order/delete")
    public JsonResult batchDeleteOrder(@RequestBody Integer[] item){
        orderinformationMapper.deleteOrderList(item);
        return Configure.setResult();
    }

    @ApiOperation(value = "更新订单")
    @PostMapping("/api/order/update")
    public JsonResult updateOrder(@RequestBody Orderinformation orderinformation){
        JsonResult jsonResult = new JsonResult();
        try {
            if( orderService.updateByPrimaryKey(orderinformation) == 1){
                return Configure.setResult();
            }
        } catch (Exception e) {
            e.printStackTrace();
            jsonResult.setCode(300);
            jsonResult.setMessage("订单号重复");
            return jsonResult;
        }finally {
            return jsonResult;
        }
    }

    @ApiOperation(value = "查询订单")
    @ApiImplicitParams({@ApiImplicitParam(name = "id", value = "id号", required = false, dataType = "Integer"),
            @ApiImplicitParam(name = "ordernumber", value = "订单号", required = false, dataType = "String"),
            @ApiImplicitParam(name = "servicemanid", value = "维保人员", required = false, dataType = "String")
    })
    @GetMapping("/api/order/list")
    public JsonObjectResult findOneOrder(@RequestParam(value = "ordernumber", required = false) String ordernumber,
                                         @RequestParam(value = "servicemanid", required = false) String servicemanid,
                                         @RequestParam(value = "id", required=false) Integer id) {
        JsonObjectResult jsonObjectResult = new JsonObjectResult();
        OrderDeviceInfo order = new OrderDeviceInfo();
        order.setServicemanid(servicemanid);
        order.setOrdernumber(ordernumber);
        order.setId(id);
        jsonObjectResult.setObj(orderService.getOrderByIf(order));
        return jsonObjectResult;
    }

    @ApiOperation(value = "删除已经激活订单")
    @PostMapping("/api/order/device_bind_delete")
    public JsonResult deleteBindDevice(@RequestBody OrderDevicesKey orderDevicesKey){
        orderDeviceService.deleteByPrimaryKey(orderDevicesKey);
        Device device = deviceService.selectByMchineId(orderDevicesKey.getMachineid());
        device.setActivationtime(null);
        deviceService.updateByPrimaryKey(device);
        return Configure.setResult();
    }

    /**
     * 订单激活绑定
     * @param orderDeviceKeyWithName
     * @return
     */
    @ApiOperation(value = "订单激活绑定")
    @PostMapping("/api/order/device_bind")
    public JsonResult bindDevice(@RequestBody OrderDeviceKeyWithName orderDeviceKeyWithName) {
        JsonResult jsonResult = new JsonResult();
        String activePerson = orderDeviceKeyWithName.getFirstcommissioner();
        OrderDevicesKey orderDevicesKey = new OrderDevicesKey();
        orderDevicesKey.setMachineid(orderDeviceKeyWithName.getMachineid());
        orderDevicesKey.setOrdernumber(orderDeviceKeyWithName.getOrdernumber());
        try {
            OrderDevicesKey orderDevicesKey1 = orderDeviceService.isExistOrderDevice(orderDevicesKey);
            if(orderDevicesKey1 != null){
                jsonResult.setCode(300);
                String message = "整机ID：" + orderDevicesKey1.getMachineid() + "在订单：" + orderDevicesKey1.getOrdernumber() +"中激活";
                jsonResult.setMessage(message);
                return jsonResult;
            }else{
                //发送激活消息
                activeSendMsg.sendMsg(orderDevicesKey);
                //判断激活消息
                System.out.println("开始判断激活消息");
                String activeResult = activeService.activeCheck(orderDevicesKey);
                System.out.println("激活消息判断完成，activeResult="+activeResult);
                if (activeResult != null && activeResult.equals("succeed")) {
                    jsonResult.setMessage("ok");
                    jsonResult.setCode(200);
                    Device device = deviceService.selectByMchineId(orderDevicesKey.getMachineid());
                    if(device == null){
                        jsonResult.setCode(300);
                        jsonResult.setMessage("设备不存在");
                        return jsonResult;
                    }else{
                        int ret = orderDeviceService.insert(orderDevicesKey);
                        device.setActivationtime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( new Timestamp(System.currentTimeMillis())));
                        device.setFirstcommissioner(activePerson);
                        deviceService.updateByPrimaryKey(device);
                        return jsonResult;
                    }
                } else {
                    jsonResult.setCode(300);
                    jsonResult.setMessage("激活失败"+activeResult);
                    return jsonResult;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            jsonResult.setCode(300);
            jsonResult.setMessage("激活失败");
            return jsonResult;
        }
    }


    @ApiOperation(value = "订单表下载")
    @GetMapping("/api/order/download")
    public JsonResult downloadOrder(HttpServletRequest request, HttpServletResponse response) throws Exception {
        List<OrderDeviceInfo> orders = orderService.getOrderList();
        System.out.println(orders);
        Workbook workbook = ExcelExportUtil.exportExcel(new ExportParams(), OrderDeviceInfo.class, orders);
        File savefile = new File("D:/excel/");
        if (!savefile.exists()) {
            savefile.mkdirs();
        }
        String time = new SimpleDateFormat("yyyyMMddHHmmss").format( new Timestamp(System.currentTimeMillis()));
        String filename = time +".xls";
        String path = "D:/excel/order" + filename;
        FileOutputStream fos = new FileOutputStream(path);
        workbook.write(fos);
        fos.close();
        //workbook.write(response.getOutputStream());
        JsonResult jsonResult = new JsonResult();
        String path1 = "/download/order" + filename;
        jsonResult.setCode(200);
        jsonResult.setMessage(path1);
        return jsonResult;
    }



}


