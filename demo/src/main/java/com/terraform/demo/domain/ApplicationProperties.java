package com.terraform.demo.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ApplicationProperties {
	
	@Value("${template.url}")
	private String templateURL;
	
	@Value("${git.repo.url}")
	private String gitRepositoryURL;
	
	@Value("${git.userid}")
	private String gitUserID;
	
	@Value("${branch.name}")
	private String branchName;
	
	@Value("${project.custom.role.module}")
	private String projectCustomRoleModule;
	
	@Value("${project.predefined.role.module}")
	private String projectPredefinedRoleModule;
	
	@Value("${org.custom.module}")
	private String orgCustomModule;
	
	@Value("${source}")
	private String source;
	
	@Value("${auth.token}")
	private String authToken;
	
	@Value("${custom.role.script.name}")
	private String customRoleScriptName;
	
	@Value("${existing.custom.script.path}")
	private String existingCustomScriptPath;
	
	@Value("${custom.script.path}")
	private String customScriptPath;
	
	@Value("${predefined.script.path}")
	private String predefinedScriptPath;
	
	@Value("${existing.predefined.script.path}")
	private String existingPredefinedScriptPath;
	
	@Value("${predefined.role.script.name}")
	private String predefinedRoleScriptName;
	

	public String getTemplateURL() {
		return templateURL;
	}

	public String getBranchName() {
		return branchName;
	}

	public String getProjectCustomRoleModule() {
		return projectCustomRoleModule;
	}

	public String getProjectPredefinedRoleModule() {
		return projectPredefinedRoleModule;
	}

	public String getOrgCustomModule() {
		return orgCustomModule;
	}

	public String getSource() {
		return source;
	}

	public String getAuthToken() {
		return authToken;
	}

	public String getCustomRoleScriptName() {
		return customRoleScriptName;
	}

	public String getExistingCustomScriptPath() {
		return existingCustomScriptPath;
	}

	public String getExistingPredefinedScriptPath() {
		return existingPredefinedScriptPath;
	}

	public String getPredefinedRoleScriptName() {
		return predefinedRoleScriptName;
	}

	public void setPredefinedRoleScriptName(String predefinedRoleScriptName) {
		this.predefinedRoleScriptName = predefinedRoleScriptName;
	}

	public String getGitRepositoryURL() {
		return gitRepositoryURL;
	}

	public String getGitUserID() {
		return gitUserID;
	}

	public String getCustomScriptPath() {
		return customScriptPath;
	}

	public String getPredefinedScriptPath() {
		return predefinedScriptPath;
	}



}
