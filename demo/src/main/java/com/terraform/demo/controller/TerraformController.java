package com.terraform.demo.controller;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.terraform.demo.domain.ApplicationProperties;
import com.terraform.demo.literals.ApplicationConstants;
import com.terraform.demo.service.TerraformService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController("/api")
public class TerraformController {

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	TerraformService tfService;
	@Autowired
	ApplicationProperties applicationProperties;
	
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@ApiOperation(value = "Create Terraform final Script", notes = "This API will create the terraform final script with the provided metadata and push it into the respective Github repository.")
	@PostMapping("/commit-terraform-script")
	
	public ResponseEntity<String> commitTemplatetoGithubRepo(@RequestBody  Map<String, Object> customRole) throws IOException {

		/**
		 * 1. Check role type in request json (customrole) 
		 * 2. Get template and check for particular module 
		 * 3. Compare all the parameters if present in the request json or not 
		 * 4. Create new file Role.tf and put values in the required format
		 * 5. Commit the file in the github repo
		 * 
		 */

		List<String> attributesNotPresent = new ArrayList<String>();
		String message="";
		String commitID="";
		String roleType="";
		if(null!=customRole.get("role_type")){
			roleType=customRole.get("role_type").toString();
			
			if (ApplicationConstants.PROJECT_CUSTOM_ROLE_TYPE.equalsIgnoreCase(roleType)) {
				
				attributesNotPresent=tfService.checkRequiredAttributesForProject(customRole, roleType);
				

				if (attributesNotPresent.isEmpty()) {
					
					String errorMessage=tfService.validateAttributes(customRole);
					
					if("".equals(errorMessage)){
						/**
						 * check first if the script already present in the repository,
						 * if yes then get the script and update 
						 * if not then create a new one
						 */
						
						String sha=tfService.getExistingScript(applicationProperties.getExistingCustomScriptPath());

						byte[] bytes = tfService.createProjectRoleScript(customRole, roleType);
						
						String path = applicationProperties.getCustomScriptPath()+ applicationProperties.getCustomRoleScriptName();
						
						ResponseEntity<String> newResponse=tfService.commitScriptToGithubRepo(bytes,sha,message,path);
						if(newResponse.getStatusCode().equals(HttpStatus.CREATED) || newResponse.getStatusCode().equals(HttpStatus.OK)){
							commitID=objectMapper.readTree(newResponse.getBody()).get("commit").get("url").asText();
							
							return  ResponseEntity.ok().body(message+" Commit ID: "+commitID );
						}else{
							return newResponse;
						}
					}else{
						return ResponseEntity.badRequest().body("Invalid attributes:\n"+errorMessage);
					}
					
				} else {
					return  ResponseEntity.badRequest().body("Mandatory attributes not present in request body: "+attributesNotPresent);
				}

			} else if (ApplicationConstants.ORG_CUSTOM_ROLE_TYPE.equalsIgnoreCase(roleType)) {
				// checkRequiredAttributesForOrg(customRole);
				return new ResponseEntity<String>(HttpStatus.OK);

			}  else if (ApplicationConstants.PROJECT_PREDEFINED_ROLE_TYPE.equalsIgnoreCase(roleType)) {
				
				attributesNotPresent=tfService.checkRequiredAttributesForProject(customRole,roleType);
				
				if(null == customRole.get("base_roles") ){
					attributesNotPresent.add("base_roles");
				}
				

				if (attributesNotPresent.isEmpty()) {
					
					String errorMessage=tfService.validateAttributes(customRole);

					if("".equals(errorMessage)){
						/**
						 * check first if the predefined role script already present in the repository,
						 * if yes then get the script and update 
						 * if not then create a new one
						 */
						
						String sha=tfService.getExistingScript(applicationProperties.getExistingPredefinedScriptPath());

						byte[] bytes = tfService.createProjectRoleScript(customRole, roleType);
						
						String path = applicationProperties.getPredefinedScriptPath()+ applicationProperties.getPredefinedRoleScriptName();
						
						ResponseEntity<String> newResponse=tfService.commitScriptToGithubRepo(bytes,sha,message, path);
						
						if(newResponse.getStatusCode().equals(HttpStatus.CREATED) || newResponse.getStatusCode().equals(HttpStatus.OK)){
							commitID=objectMapper.readTree(newResponse.getBody()).get("commit").get("url").asText();
							
							return  ResponseEntity.ok().body(message+" Commit ID: "+commitID );
						}else{
							return newResponse;
						}
					}else{
						return  ResponseEntity.badRequest().body("Invalid attributes:\n"+errorMessage);
					}
				} else {
					return  ResponseEntity.badRequest().body("Mandatory Attributes Not present in Request Body: "+attributesNotPresent);
				}

			}else {
				return  ResponseEntity.badRequest().body("Invalid role type");
			}
		}
		return ResponseEntity.badRequest().body("Role type not present in request body");

	}
	

}
