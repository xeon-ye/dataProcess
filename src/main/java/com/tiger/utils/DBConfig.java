package com.tiger.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.druid.util.StringUtils;

@Service("dbConfig")  
public class DBConfig {
	private static Logger log = LoggerFactory.getLogger(DBConfig.class);
	@Autowired
	private MultilDataSources multilDataSources;
    @SuppressWarnings("rawtypes")
	private List dtPros = null;
	@SuppressWarnings("rawtypes")
	private Map dtProMap = null;
	
	public String getWholeProName(String proKey){
		if(StringUtils.isEmpty(proKey)){
			return "";
		}
		if(dtProMap==null){
			loadPros();
		}
		if(dtProMap!=null&&dtProMap.containsKey(proKey)){
			Map pro = (Map)dtProMap.get(proKey);
			return (String)pro.get("IVALUE");
		}
		return "";
	}
	private void loadPros(){
		List jts = multilDataSources.getJdbcTemplates();
		if(jts!=null&&jts.size()>0){
			for(int i=0;i<jts.size();i++){
				JdbcTemplate jt = (JdbcTemplate)jts.get(i);
				try{
			  		Object[] params = null;
			  		StringBuffer sql = new StringBuffer("select item,iname,ivalue,remark from system_pro_set order by item");
			  		dtPros=jt.queryForList(sql.toString(),new Object[]{});
			  		if(dtPros!=null&&dtPros.size()>0){
			  			dtProMap = new HashMap();
			  	    	for(int j=0;j<dtPros.size();j++){
			  	    		Map ss = (Map)dtPros.get(j);
			  	    		dtProMap.put((String)ss.get("item"), ss);
			  	    	}
			  		}
		  		}catch(Exception e){
		  			log.error("加载存储过程配置时发生错误："+e.toString());
		  			log.error("数据源信息："+jt.getDataSource().toString());
			  	}
			}
		}
	}
	public boolean reloadPros(){
		loadPros();
		return true;
	}
}
