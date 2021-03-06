package com.tiger.dataProcess.service;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.tiger.utils.*;

@Service("dataProcessService")  
@Transactional
public class DataProcessService {
	private static Logger log = LoggerFactory.getLogger(DataProcessService.class);
	@Autowired
	private MultilDataSources multilDataSources;
	@Autowired
	private DBConfig dbConfig;
    private JdbcTemplate jdbcTemplate;
	@Autowired
    private Environment environment;
	@Autowired
	private JasyptUtils jasyptUtils;
	
	//获取分页列表数据的统一方法；
  	@SuppressWarnings("unchecked")
  	public JSONObject getListPaging(String userid,int start,int limit,String sort,String dir,String dtID, String qparams) {
  		final JSONObject infos = new JSONObject();
  		JSONObject pros = parseProname("pro_pglist",dtID);
  		if(pros!=null&&pros.containsKey("error")){
  			return pros;
  		}
  		String proName = pros.getString("proName");
  		
  		StringBuffer sql = new StringBuffer("{call ");
  		sql.append(proName).append("(?,?,?,?,?,?,?,?)}");
  		
  		final String fuserid = userid;
  		final int fstart = start;
  		final int flimit = limit;
  		final String fparams = qparams;
  		final String fsort = sort;
  		final String fdir = dir;
  		log.debug("v_operator:"+fuserid);
  		log.debug("v_params:"+fparams);
  		log.debug("v_sort:"+fsort);
  		log.debug("v_dir:"+fdir);
  		log.debug("v_start:"+fstart);
  		log.debug("v_limit:"+flimit);
  		final List rows = new ArrayList();
  		infos.put("totalCount", 0);
  		infos.put("rows", rows);
  		try{
  			@SuppressWarnings("unchecked")
  			Object execute = jdbcTemplate.execute(sql.toString(),new CallableStatementCallback() {
  				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
  					cs.setString(1,fuserid);
  					cs.setString(2,fparams);
  					cs.setString(3,fsort);
  					cs.setString(4,fdir);
  					cs.setInt(5,fstart);
  					cs.setInt(6,flimit);
  					cs.registerOutParameter(7, Types.NUMERIC);
  	                cs.registerOutParameter(8,oracle.jdbc.OracleTypes.CURSOR);  
  	                cs.execute();  
  	                int count = cs.getInt(7);
  	                infos.put("totalCount", count);
  	                ResultSet rs = (ResultSet) cs.getObject(8); 
  	                if(rs==null){
  	                	return rows;
  	                }
  	                ResultSetMetaData rsmd=rs.getMetaData();
  	        		//获取元信息
  	        		int colNum=rsmd.getColumnCount();
  	                while (rs.next()) {
  	                	Map row = new HashMap();
  	                	for(int i=1;i<=colNum;i++){
  	        				String sVal=rs.getString(i);
  	        				String colName = rsmd.getColumnLabel(i).toLowerCase();
  	        				row.put(colName, sVal);
  	        			}
  	                	rows.add(row);
  	                }
  	                infos.put("rows", rows);
  	                return rows;
  				} 
  			});
  		}catch(Throwable e){
  			infos.put("error", e.toString());
  			log.error(e.toString());
  		}
  		return infos;
  	}
  	
  	@SuppressWarnings("unchecked")
	public JSONObject getList(String userid,String sort,String dir,String dtID, String qparams) {
		final JSONObject infos = new JSONObject();
		JSONObject pros = parseProname("pro_list",dtID);
  		if(pros!=null&&pros.containsKey("error")){
  			return pros;
  		}
  		String proName = pros.getString("proName");
		
		StringBuffer sql = new StringBuffer("{call ");
		sql.append(proName).append("(?,?,?,?,?)}");
		final String fuserid = userid;
		final String fparams = qparams;
		final String fsort = sort;
		final String fdir = dir;
		log.debug("v_operator:"+fuserid);
		log.debug("v_params:"+fparams);
		log.debug("v_sort:"+fsort);
		log.debug("v_dir:"+fdir);
		try{
			Object data = (List)jdbcTemplate.execute(sql.toString(),new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					final List rows = new ArrayList();
					cs.setString(1,fuserid);
					cs.setString(2,fparams);
					cs.setString(3,fsort);
					cs.setString(4,fdir);
	                cs.registerOutParameter(5,oracle.jdbc.OracleTypes.CURSOR);  
	                cs.execute();  
	                ResultSet rs = (ResultSet) cs.getObject(5); 
	                if(rs==null){
	                	return rows;
	                }
	                ResultSetMetaData rsmd=rs.getMetaData();
	        		//获取元信息
	        		int colNum=rsmd.getColumnCount();
	                while (rs.next()) {
	                	Map row = new HashMap();
	                	for(int i=1;i<=colNum;i++){
	        				String sVal=rs.getString(i);
	        				String colName = rsmd.getColumnLabel(i).toLowerCase();
	        				row.put(colName, sVal);
	        			}
	                	rows.add(row);
	                }
	                infos.put("rows", rows);
	                return rows;
				} 
			});
		}catch(Throwable e){
			infos.put("error", e.toString());
			log.error(e.toString());
		}
		return infos;
	}
  	
  	@SuppressWarnings("rawtypes")
	public JSONObject getSingleRecord(String userid,String dtID, String params) {
		JSONObject infos = new JSONObject();
		JSONObject pros = parseProname("pro_get",dtID);
  		if(pros!=null&&pros.containsKey("error")){
  			return pros;
  		}
  		String proName = pros.getString("proName");
		
		StringBuffer sql = new StringBuffer("{call ");
		sql.append(proName).append("(?,?,?)}");
		final String fuserid = userid;
		final String fparams = params;
		log.debug("v_operator:"+fuserid);
		log.debug("v_params:"+fparams);
		try{
			@SuppressWarnings("unchecked")
			final JSONObject trow = (JSONObject)jdbcTemplate.execute(sql.toString(),new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					JSONObject row =new JSONObject();
					cs.setString(1,fuserid);
					cs.setString(2,fparams);
	                cs.registerOutParameter(3,oracle.jdbc.OracleTypes.CURSOR);  
	                cs.execute();  
	                ResultSet rs = (ResultSet) cs.getObject(3); 
	                if(rs==null){
	                	return row;
	                }
	                ResultSetMetaData rsmd=rs.getMetaData();
	        		//获取元信息
	        		int colNum=rsmd.getColumnCount();
	                while (rs.next()) {
	                	for(int i=1;i<=colNum;i++){
	        				String sVal=rs.getString(i);
	        				String colName = rsmd.getColumnName(i).toLowerCase();
	        				row.put(colName, sVal);
	        			}
	                }
	                return row;
				} 
			});
			infos = trow;
		}catch(Throwable e){
			infos.put("error", e.toString());
			log.error(e.toString());
		}
		return infos;
	}
  	
  	@SuppressWarnings("unchecked")
	public JSONObject saveData(String userid,String dtID, String params) {
		final JSONObject infos = new JSONObject();
		JSONObject pros = parseProname("pro_save",dtID);
  		if(pros!=null&&pros.containsKey("error")){
  			return pros;
  		}
  		String proName = pros.getString("proName");
		
		StringBuffer sql = new StringBuffer("{call ");
		sql.append(proName).append("(?,?,?,?)}");
		String flag = "1";
		final String[] results = new String[2];
		try{
			final String fUser = userid;
			final String fparams = params;
			log.debug("v_operator:"+fUser);
			log.debug("v_params:"+fparams);
			flag = (String)jdbcTemplate.execute(sql.toString(),new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					cs.setString(1,fUser);
					cs.setString(2,fparams);
	                cs.registerOutParameter(3,Types.VARCHAR);  
	                cs.registerOutParameter(4,Types.VARCHAR);  
	                cs.execute();  
	                String tmpflag = cs.getString(3);
	                String tmpInfo = cs.getString(4);
	                if(!"1".equals(tmpflag)){
	                	log.error(tmpInfo);
	                }
	                results[0] = tmpflag;
	                results[1] = tmpInfo;
	                infos.put("flag", tmpflag);
	                infos.put("info", tmpInfo);
	                return tmpflag;  
				} 
			});
		}catch(Throwable e){
			results[0] = "9";
			results[1] = e.toString();
			log.error(e.toString());
		}
		return infos;
	}

	@SuppressWarnings("unchecked")
	public Map deleteData(String userid,String dtID, String params) {
		final JSONObject infos = new JSONObject();
		JSONObject pros = parseProname("pro_delete",dtID);
  		if(pros!=null&&pros.containsKey("error")){
  			return pros;
  		}
  		String proName = pros.getString("proName");
		
		StringBuffer sql = new StringBuffer("{call ");
		sql.append(proName).append("(?,?,?,?)}");
		String flag = "1";
		try{
			final String fUser = userid;
			final String fparams = params;
			log.debug("v_operator:"+fUser);
			log.debug("v_params:"+fparams);
			flag = (String)jdbcTemplate.execute(sql.toString(),new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					cs.setString(1,fUser);
					cs.setString(2,fparams);
	                cs.registerOutParameter(3,Types.VARCHAR);  
	                cs.registerOutParameter(4,Types.VARCHAR);  
	                cs.execute();  
	                String tmpflag = cs.getString(3);
	                String tmpInfo = cs.getString(4);
	                if(!"1".equals(tmpflag)){
	                	log.error(tmpInfo);
	                }
	                infos.put("flag", tmpflag);
	                infos.put("info", tmpInfo);
	                return tmpflag;  
				} 
			});
		}catch(Throwable e){
			infos.put("flag", "9");
            infos.put("info", e.toString());
			log.error(e.toString());
		}
		return infos;
	}
	
	@SuppressWarnings("unchecked")
	public Map checkDuplicate(String userid, String dtID, String params) {
		final JSONObject infos = new JSONObject();
		JSONObject pros = parseProname("pro_checkDup",dtID);
  		if(pros!=null&&pros.containsKey("error")){
  			return pros;
  		}
  		String proName = pros.getString("proName");
		StringBuffer sql = new StringBuffer("{call ");
		sql.append(proName).append("(?,?,?,?,?)}");
		String flag = "1";
		try{
			final String fUser = userid;
			final String fparams = params;
			log.debug("v_operator:"+fUser);
			log.debug("v_params:"+fparams);
			flag = (String)jdbcTemplate.execute(sql.toString(),new CallableStatementCallback() {
				public Object doInCallableStatement(CallableStatement cs)throws SQLException, DataAccessException {
					cs.setString(1,fUser);
					cs.setString(2,fparams);
	                cs.registerOutParameter(3,Types.VARCHAR);  
	                cs.registerOutParameter(4,Types.VARCHAR);  
	                cs.registerOutParameter(5,Types.VARCHAR); 
	                cs.execute();  
	                String tmpflag = cs.getString(3);
	                String tmpDup = cs.getString(4);
	                String tmpInfo = cs.getString(5);
	                if(!"1".equals(tmpflag)){
	                	log.error(tmpInfo);
	                }
	                infos.put("flag", tmpflag);
	                infos.put("isDup", tmpDup);
	                infos.put("info", tmpInfo);
	                return tmpflag;  
				} 
			});
		}catch(Throwable e){
			infos.put("flag", "9");
			infos.put("isDup", "");
			infos.put("info", e.toString());
			log.error(e.toString());
		}
		return infos;
	}
	
	private JSONObject parseProname(String proType,String dtID){
		JSONObject infos = new JSONObject();
		String proKey=proType+"_"+dtID;
		String wholeProName = dbConfig.getWholeProName(proKey);
  		if(StringUtils.isEmpty(wholeProName)){
  			log.info("数据库中未找到存储过程："+proKey+"，尝试配置文件中查找。");
  			wholeProName = environment.getProperty(proKey);
  			if(StringUtils.isEmpty(wholeProName)){
  				infos.put("error", "未配置存储过程："+proKey);
  				return infos;
  			}
  		}
  		String[] dtPros = jasyptUtils.parseDataPros(wholeProName);
  		String dtName = dtPros[0];
  		String proName = dtPros[1];
  		jdbcTemplate= multilDataSources.getJdbcTemplate(dtName);
  		if(jdbcTemplate==null){
  			infos.put("error", "未配置相应的数据源："+dtName);
  			return infos;
  		}
  		if(proName==null||"".equals(proName)){
  			infos.put("error", "未配置要执行的存储过程名："+proKey);
  			return infos;
  		}
  		infos.put("dtName", dtName);
  		infos.put("proName", proName);
  		return infos;
	}
	
}
