package com.terraform.demo.domain;

import java.util.List;

public class CustomRole {
	
	private String module;
	private String source;
	private String roleId;
	private String targetLevel;
	private String targetId;
	private String title;
	private String description;
	private List<String> permissions;
	
	
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getRoleId() {
		return roleId;
	}
	public void setRoleId(String roleId) {
		this.roleId = roleId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getPermissions() {
		return permissions;
	}
	public void setPermissions(List<String> permissions) {
		this.permissions = permissions;
	}
	public String getModule() {
		return module;
	}
	public void setModule(String module) {
		this.module = module;
	}
	public String getTargetId() {
		return targetId;
	}
	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}
	public String getTargetLevel() {
		return targetLevel;
	}
	public void setTargetLevel(String targetLevel) {
		this.targetLevel = targetLevel;
	}
	
	
	

}
