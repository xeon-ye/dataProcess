package com.tiger.utils.bean;

import com.alibaba.fastjson.JSONObject;

public class UpdateDataJson {
	private String dataID;
	private String updateParams ;
	
	public String getUpdateParams() {
		return updateParams;
	}
	public void setUpdateParams(String updateParams) {
		this.updateParams = updateParams;
	}
	public JSONObject parseJUpdateParams(){
		return JSONObject.parseObject(this.updateParams);
	}
	public String getDataID() {
		return dataID;
	}
	public void setDataID(String dataID) {
		this.dataID = dataID;
	}
}
