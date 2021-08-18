package com.terraform.demo.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;

public interface TerraformService {
	
	public List<String> checkRequiredAttributesForProject(Map<String, Object> customRole, String roleType) throws IOException;

	public byte[] createProjectRoleScript(Map<String, Object> customRole, String roleType) throws IOException;
	
	public	List<String> readTemplate(Map<String, Object> customRole) throws IOException;
	
	public String getExistingScript(String scriptPath);

	public ResponseEntity<String> commitScriptToGithubRepo(byte[] bytes, String sha, String message,String path);

	public String validateAttributes(Map<String, Object> customRole);

}
