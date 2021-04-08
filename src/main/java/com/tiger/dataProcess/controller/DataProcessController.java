package com.tiger.dataProcess.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.tiger.utils.bean.*;
import com.tiger.dataProcess.service.DataProcessService;
import com.tiger.utils.JResponse;
import com.tiger.utils.JasyptUtils;
import com.tiger.utils.bean.GetDataJson;


@CrossOrigin
@RestController 
public class DataProcessController {
	private static Logger log = LoggerFactory.getLogger(DataProcessController.class);
	@Autowired
	private DataProcessService dataProcessService;
	@Autowired
	private JasyptUtils jasyptUtils;
	
	/**
	 * 获取分页列表
	 * @param gd
	 * @return
	 */
	@RequestMapping(value="/queryListPaging",method = RequestMethod.POST)
	@ResponseBody
	public JResponse queryListPaging(@RequestBody GetDataJson gd){
		JResponse jr = new JResponse();
		String dtID=gd.getDataID();
		JSONObject jparams = gd.parseJQueryParams();
		String sort="",dir="",qparams = "",userid="";
		if(StringUtils.isEmpty(userid)){
			try{
				userid = jparams.getString("operator");
			}catch(Exception e){
			}
		}
		int start=jparams.containsKey("start")?jparams.getIntValue("start"):0;
		int limit=jparams.containsKey("limit")?jparams.getIntValue("limit"):30;
		sort = jparams.containsKey("sort")?jparams.getString("sort"):"";
		dir =  jparams.containsKey("dir")?jparams.getString("dir"):"";
		String qps = jparams.containsKey("qParams")? jparams.getString("qParams"):"";
		String tparams = StringUtils.substringBetween(qps, "{", "}");
		tparams = StringUtils.replace(tparams, "\"", "");
		log.debug("queryListPaging(dataID:"+dtID+",userid:"+userid+")");
		JSONObject jucfg = dataProcessService.getListPaging(userid,start,limit,sort,dir,dtID,tparams);
		if(jucfg!=null&&jucfg.containsKey("error")){
			jr.setRetCode("9");
			jr.setRetMsg(jucfg.getString("error"));
			jr.setRetData(null);
		}else{
			jr.setRetCode("0");
			jr.setRetMsg("");
			jr.setRetData(jucfg);
		}
		return jr;
	}
	/**
	 * 获取不分页列表
	 * @param gd
	 * @return
	 */
	@RequestMapping(value="/queryList",method = RequestMethod.POST)
	@ResponseBody
	public JResponse queryList(@RequestBody GetDataJson gd){
		JResponse jr = new JResponse();
		String dtID=gd.getDataID();
		JSONObject jparams = gd.parseJQueryParams();
		String sort="",dir="",qparams = "",userid="";
		if(StringUtils.isEmpty(userid)){
			try{
				userid = jparams.getString("operator");
			}catch(Exception e){
			}
		}
		sort = jparams.containsKey("sort")?jparams.getString("sort"):"";
		dir =  jparams.containsKey("dir")?jparams.getString("dir"):"";
		String qps = jparams.containsKey("qParams")? jparams.getString("qParams"):"";
		String tparams = StringUtils.substringBetween(qps, "{", "}");
		tparams = StringUtils.replace(tparams, "\"", "");
		log.debug("queryList(dataID:"+dtID+",userid:"+userid+")");
		JSONObject jucfg = dataProcessService.getList(userid,sort,dir,dtID,tparams);
		if(jucfg!=null&&jucfg.containsKey("error")){
			jr.setRetCode("9");
			jr.setRetMsg(jucfg.getString("error"));
			jr.setRetData(null);
		}else{
			jr.setRetCode("0");
			jr.setRetMsg("");
			jr.setRetData(jucfg);
		}
		return jr;
	}
	
	/**
	 * 获取单条记录
	 * @param ud
	 * @return
	 */
	@RequestMapping(value="/getSingleRecord",method = RequestMethod.POST)
	@ResponseBody
	public JResponse getSingleRecord(@RequestBody QuerySingleRdJson qb){
		JResponse jr = new JResponse();
		String dtId = qb.getDataID();
		JSONObject params = qb.parseJKeyParams();
		String userid="";
		if(StringUtils.isEmpty(userid)){
			try{
				userid = params.getString("operator");
			}catch(Exception e){
			}
		}
		String sps = params.toJSONString();
		String tparams = StringUtils.substringBetween(sps, "{", "}");
		tparams = StringUtils.replace(tparams, "\"", "");
		log.debug("getSingleRecord(dataID:"+dtId+",userid:"+userid);
		Map result = dataProcessService.getSingleRecord(userid,dtId,tparams);
		if(result!=null&&result.containsKey("error")){
			jr.setRetCode("9");
			jr.setRetMsg((String)result.get("error"));
			jr.setRetData(null);
		}else{
			jr.setRetCode("0");
			jr.setRetMsg("");
			jr.setRetData(result);
		}
		return jr;
	}
	
	@RequestMapping(value="/save",method = RequestMethod.POST)
	@ResponseBody
	public JResponse saveData(@RequestBody UpdateDataJson ud){
		JResponse jr = new JResponse();
		String dtId = ud.getDataID();
		JSONObject params = ud.parseJUpdateParams();
		String userid="";
		if(StringUtils.isEmpty(userid)){
			try{
				userid = params.getString("operator");
			}catch(Exception e){
			}
		}
		String sps = params.toJSONString();
		String tparams = StringUtils.substringBetween(sps, "{", "}");
		tparams = StringUtils.replace(tparams, "\"", "");
		log.debug("save(dataID:"+dtId+",userid:"+userid);
		Map result = dataProcessService.saveData(userid,dtId,tparams);
		String flag = (String)result.get("flag");
		if("1".equals(flag)){
			jr.setRetCode("0");
			jr.setRetMsg("");
			JSONObject oj = new JSONObject();
			oj.put("info", (String)result.get("info"));
			jr.setRetData(oj);
		}else{
			jr.setRetCode("9");
			jr.setRetMsg((String)result.get("info"));
			jr.setRetData(null);
		}
		return jr;
	}
	
	/**
	 * 删除信息
	 * @param ud
	 * @return
	 */
	@RequestMapping(value="/delete",method = RequestMethod.POST)
	@ResponseBody
	public JResponse deleteData(@RequestBody DeleteDataJson dd){
		JResponse jr = new JResponse();
		String dtId = dd.getDataID();
		JSONObject params = dd.parseJDelParams();
		String userid="";
		if(StringUtils.isEmpty(userid)){
			try{
				userid = params.getString("operator");
			}catch(Exception e){
			}
		}
		String sps = params.toJSONString();
		String tparams = StringUtils.substringBetween(sps, "{", "}");
		tparams = StringUtils.replace(tparams, "\"", "");
		log.debug("delete(dataID:"+dtId+",userid:"+userid);
		Map result = dataProcessService.deleteData(userid,dtId,tparams);
		String flag = (String)result.get("flag");
		if("1".equals(flag)){
			jr.setRetCode("0");
			jr.setRetMsg("");
			JSONObject oj = new JSONObject();
			oj.put("info", (String)result.get("info"));
			jr.setRetData(oj);
		}else{
			jr.setRetCode("9");
			jr.setRetMsg((String)result.get("info"));
			jr.setRetData(null);
		}
		return jr;
	}
	
	
	
	@RequestMapping(value = "/getUsers",method = RequestMethod.GET)
    public  List findAllUsers(@RequestParam(required=false) String dtName){
        List users = dataProcessService.getUsers(dtName);
        return users;
    }
	
	@RequestMapping(value = "/encryptStr",method = RequestMethod.GET)
    public String encrypt(@RequestParam String str,@RequestParam(required=false) String password){
        String encryptStr = jasyptUtils.encypt(str,password);
        return encryptStr;
    }
	@RequestMapping(value = "/decryptStr",method = RequestMethod.GET)
    public String decryptStr(@RequestParam String str,@RequestParam(required=false) String password){
        String decryptStr = jasyptUtils.decypt(str,password);
        return decryptStr;
    }
}
