package com.tiger.utils.bean;

import com.alibaba.fastjson.JSONObject;

public class DeleteDataJson {
	private String dataID;
	private String delParams ;
	public String getDataID() {
		return dataID;
	}
	public void setDataID(String dataID) {
		this.dataID = dataID;
	}
	public String getDelParams() {
		return delParams;
	}
	public void setDelParams(String delParams) {
		this.delParams = delParams;
	}
	public JSONObject parseJDelParams(){
		return JSONObject.parseObject(this.delParams);
	}
}
