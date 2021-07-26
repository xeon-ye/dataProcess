package com.tiger.dataProcess.controller;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;

import com.alibaba.fastjson.JSONObject;
import com.tiger.utils.bean.QuerySingleRdJson;
import com.tiger.utils.bean.*;
import com.tiger.dataProcess.service.DataProcessService;
import com.tiger.utils.DBConfig;
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
    private Environment environment;
	@Autowired
	private JasyptUtils jasyptUtils;
	@Autowired
    private ContextRefresher contextRefresher;
	@Autowired
	private DBConfig dbConfig;
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
	
	@RequestMapping(value="/getTree",method = RequestMethod.POST)
	@ResponseBody
	public JResponse getBmCodesTree(@RequestBody GetDataJson gd){
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
		
		JSONObject jqps = jparams.containsKey("qParams")? jparams.getJSONObject("qParams"):new JSONObject();
		String sLoadAll = jqps.containsKey("loadAll")?jqps.getString("loadAll"):"0";
		boolean loadAll = "1".equals(sLoadAll)||"true".equals(sLoadAll);
		jqps.put("loadAll", sLoadAll);
		
		String qps = jqps.toJSONString();
		String tparams = StringUtils.substringBetween(qps, "{", "}");
		tparams = StringUtils.replace(tparams, "\"", "");
		String proName = "bmCodesTree";
		JSONObject jucfg = dataProcessService.getList(userid,sort,dir,dtID,tparams);
		if(jucfg!=null&&jucfg.containsKey("error")){
			jr.setRetCode("9");
			jr.setRetMsg(jucfg.getString("error"));
			jr.setRetData(null);
		}else{
			if(loadAll){//非懒加载时，构造children结构
				List allRows = jucfg.getJSONArray("rows");
				if(allRows!=null&&allRows.size()>0){
					List sortedRows = new ArrayList();
					for(int i=0;i<allRows.size();i++){
						Map rRow = (Map)allRows.get(i);
						String rootPid = (String)rRow.get("pid");
						//取出第一层（pid为空的）的节点，进行子节点构造
						if(StringUtils.isEmpty(rootPid)){
							String rootId = (String)rRow.get("id");
							List children = new ArrayList();
							buildChildren(allRows,rRow,i+1,children);
							rRow.put("children", children);
							sortedRows.add(rRow);
						}
					}
					jucfg.put("rows", sortedRows);
				}
			}
			jr.setRetCode("0");
			jr.setRetMsg("");
			jr.setRetData(jucfg);
		}
		return jr;
	}
	private void buildChildren(List allRows,Map preRow,int idx,List children){
		while(idx<allRows.size()){
			Map jrow = (Map)allRows.get(idx);
			String cPid = (String)jrow.get("pid");
			String cLeaf = (String)jrow.get("leaf");
			String pNode = (String)preRow.get("id");
			if(pNode.equals(cPid)){
				if(!"1".equals(cLeaf)){
					List newChildren = new ArrayList();
					buildChildren(allRows,jrow,idx+1,newChildren);
					jrow.put("children", newChildren);
					children.add(jrow);
				}else{
					children.add(jrow);
				}
			}else{
				preRow.put("children", children);
			}
			idx++;
		}
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
		//外部配置：需要在调用存储过程（传递到数据库）保存之前就加密指定字段的dataID,可配置多个方法。
		String encryptDataIDs = environment.getProperty("dataIds2EncryptBeforSave", "user");
		String[] dtids = encryptDataIDs==null ? new String[]{""} : encryptDataIDs.split(",");
		for(int i=0;i<dtids.length;i++){
			//若当前处理的保存方法属该类方法（提前加密字段）
			if(dtids[i].equals(dtId)){
				//循环处理指定需要加密的字段（可多个）
				String params2Encrypt= environment.getProperty("params2Encrypt", "pswd");
				String[] enParams = params2Encrypt==null?new String[]{""}:params2Encrypt.split(",");
				for(int j=0;j<enParams.length;j++){
					String pname = enParams[j];
					if(!StringUtils.isEmpty(pname)&&params.containsKey(pname)&&!StringUtils.isEmpty(params.getString(pname))){
						String pswd = params.getString(pname);
						byte[] decode = Base64.getDecoder().decode(pswd);
						String dcPswd = new String(decode);
						log.info("原文："+pswd);
						log.info("Base64.getDecoder().decode之后："+dcPswd);
						String hashed = BCrypt.hashpw(dcPswd, BCrypt.gensalt());
						params.put(pname, hashed);
					}
				}
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
	
	@RequestMapping(value="/checkDuplicate",method = RequestMethod.POST)
	@ResponseBody
	public JResponse checkDuplicate(@RequestBody QuerySingleRdJson qb){
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
		log.debug("delete(dataID:"+dtId+",userid:"+userid);
		Map result = dataProcessService.checkDuplicate(userid,dtId,tparams);
		String flag = (String)result.get("flag");
		if("1".equals(flag)){
			jr.setRetCode("0");
			jr.setRetMsg("");
			JSONObject dup = new JSONObject();
			dup.put("isDup", (String)result.get("isDup"));
			dup.put("info", (String)result.get("info"));
			jr.setRetData(dup);
		}else{
			jr.setRetCode("9");
			jr.setRetMsg((String)result.get("info"));
			jr.setRetData(null);
		}
		return jr;
	}
	
	@RequestMapping(value = "/refreshConfig", method = RequestMethod.GET)
	@ResponseBody
    public JResponse refreshConfig() throws Exception {
        contextRefresher.refresh();
        dbConfig.reloadPros();
        JResponse jr = new JResponse();
		jr.setRetCode("0");
		jr.setRetMsg("配置已刷新！");
		return jr;
    }
	
	@RequestMapping(value = "/encryptStr")
    public String encrypt(@RequestParam String str,@RequestParam(required=false) String password){
        String encryptStr = jasyptUtils.encypt(str,password);
        return encryptStr;
    }
	@RequestMapping(value = "/decryptStr")
    public String decryptStr(@RequestParam String str,@RequestParam(required=false) String password){
        String decryptStr = jasyptUtils.decypt(str,password);
        return decryptStr;
    }
}
