package com.terraform.demo.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.bertramlabs.plugins.hcl4j.HCLParser;
import com.bertramlabs.plugins.hcl4j.HCLParserException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.terraform.demo.domain.ApplicationProperties;
import com.terraform.demo.literals.ApplicationConstants;

@Service
public class TerraformServiceImpl implements TerraformService {

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	ApplicationProperties appProperties;
	
	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public List<String> checkRequiredAttributesForProject(Map<String, Object> customRole, String roleType) throws IOException {
		
		List<String> templateFields= readTemplate(customRole);
		
		List<String> metadataFields=customRole.keySet().stream().collect(Collectors.toList());
		
		if(ApplicationConstants.PROJECT_PREDEFINED_ROLE_TYPE.equalsIgnoreCase(roleType)){
			removeExtraAttributes(metadataFields);
		}
		
		templateFields.removeAll(metadataFields);
		
		return templateFields;
	}

	/**
	 * This method will create the terraform final Script and return the byte[]
	 */

	@Override
	public byte[] createProjectRoleScript(Map<String, Object> customRole, String roleType) throws IOException {
		Path file=null;
		List<String> lines = new ArrayList<>();
		lines.add("module  \"" +  appProperties.getProjectCustomRoleModule()+"\"{");
		lines.add("source			= \"" + appProperties.getSource() +"\"");
		lines.add("target_level     = \"" + ApplicationConstants.PROJECT + "\"");
		lines.add("target_id        = \"" + customRole.get("project") + "\"");
		lines.add("role_id          = \"" + customRole.get("role_id") + "\"");
		lines.add("title            = \"" + customRole.get("title") + "\"");
		lines.add("description 		= \"" + customRole.get("description") + "\"");
		lines.add("permissions 		="
				+((List<String>)customRole.get("permissions")).stream().map(s -> String.format("\"%s\"", s)).collect(Collectors.toList()));
		
		if(ApplicationConstants.PROJECT_PREDEFINED_ROLE_TYPE.equalsIgnoreCase(roleType)){
			lines.add("base_roles       ="
					+((List<String>)customRole.get("base_roles")).stream().map(s -> String.format("\"%s\"", s)).collect(Collectors.toList()));
			
			if(null!=customRole.get("excluded_permissions")){
				lines.add("excluded_permissions =  		"
						+((List<String>)customRole.get("excluded_permissions")).stream().map(s -> String.format("\"%s\"", s)).collect(Collectors.toList())
						+ " \n }");
			}else{
				lines.add("\n }");
			}
			file = Paths.get(appProperties.getPredefinedRoleScriptName());
		}else if(ApplicationConstants.PROJECT_CUSTOM_ROLE_TYPE.equalsIgnoreCase(roleType)){
			lines.add("\n }");
			
			file = Paths.get(appProperties.getCustomRoleScriptName());
		}

		Files.write(file, lines, StandardCharsets.UTF_8);

		byte[] bytes = Files.readAllBytes(file);

		return bytes;
	}
	
	@Override
	public List<String> readTemplate(Map<String, Object> customRole) throws IOException {

		/**
		 * Read template from Github Repository
		 */

		HttpHeaders headers = new HttpHeaders();
		headers.set(ApplicationConstants.AUTH_HEADER, appProperties.getAuthToken());
		headers.set(ApplicationConstants.USER_AGENT_HEADER,appProperties.getGitUserID());
		HttpEntity<String> entity = new HttpEntity("parameters", headers);
		List<String> templateFields=new ArrayList<>();

		ResponseEntity<String> responseBody = restTemplate
				.exchange(URI.create(appProperties.getTemplateURL()), HttpMethod.GET, entity, String.class);

		if (responseBody.getStatusCode().equals(HttpStatus.OK)) {
			
			String encodedContent = objectMapper.readTree(responseBody.getBody()).get("content").asText();
			byte[] contents = DatatypeConverter.parseBase64Binary(encodedContent);
			
			
			HCLParser parser = new HCLParser();
			File outputFile = new File("main.tf");
			try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
			    outputStream.write(contents);
			}
			
			Map<String, Object> templateContents;
			try {
				templateContents = parser.parse(outputFile);
				if(null!=templateContents)
					templateContents=	(Map<String, Object>)templateContents.get("resource");
				if(null!=templateContents)
					templateContents=(Map<String, Object>)templateContents.get("google_project_iam_custom_role");
				if(null!=templateContents)
					templateContents=(Map<String, Object>)templateContents.get("project-custom-role");
				
				
				templateFields=templateContents.keySet().stream().filter(s->!s.equalsIgnoreCase("count")).collect(Collectors.toList());
				
			} catch (HCLParserException e) {
				e.printStackTrace();
			}
			
		}

		return templateFields;
	}

	private void removeExtraAttributes(List<String> metadataFields) {
		metadataFields.remove("base_roles");
		metadataFields.remove("excluded_permissions");
	}

	@Override
	public String getExistingScript(String existingScriptpath) {
		
		String sha="";
		HttpHeaders headers = new HttpHeaders();
		
		setHeaders(headers);
		
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
		
		try{
			ResponseEntity<String> response = restTemplate.exchange(
					URI.create(appProperties.getGitRepositoryURL() + existingScriptpath), HttpMethod.GET, entity, String.class);
			
			if(response.getStatusCode().equals(HttpStatus.OK)){
				sha = objectMapper.readTree(response.getBody()).get("sha").asText();
			}
		}catch (HttpClientErrorException e) {
			
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		
		return sha;
	}
	
	private void setHeaders(HttpHeaders headers) {
		headers.set(ApplicationConstants.AUTH_HEADER, appProperties.getAuthToken());
		headers.set(ApplicationConstants.USER_AGENT_HEADER, appProperties.getGitUserID());
		
	}

	@Override
	public ResponseEntity<String> commitScriptToGithubRepo(byte[] bytes, String sha, String message,String path) {
		HashMap<String, String> body = new HashMap<String, String>();
		HttpHeaders headers = new HttpHeaders();
		ResponseEntity<String> newResponse;
		String requestBody;
		
		setHeaders(headers);
		
		String encodedContent = java.util.Base64.getEncoder().encodeToString(bytes);
		
		body.put("content", encodedContent);
		body.put("branch", appProperties.getBranchName());
		
		if(!"".equals(sha)){
			body.put("sha", sha);
			body.put(ApplicationConstants.MSG,ApplicationConstants.UPDATE_SCRIPT_MESSAGE);
			message=ApplicationConstants.UPDATE_SCRIPT_MESSAGE;
		}else{
			body.put(ApplicationConstants.MSG,ApplicationConstants.CREATE_SCRIPT_MESSAGE);
			message=ApplicationConstants.CREATE_SCRIPT_MESSAGE;
		}

		try {
			requestBody = objectMapper.writeValueAsString(body);
			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity(requestBody, headers);

			newResponse = restTemplate.exchange(URI.create(appProperties.getGitRepositoryURL() + path),
					HttpMethod.PUT, requestEntity, String.class);
		} catch (JsonProcessingException e) {
			newResponse =ResponseEntity.internalServerError().body("Error :"+ e.getMessage());
			e.printStackTrace();
		}
		
		
		return newResponse;
	}

	@Override
	public String validateAttributes(Map<String, Object> customRole) {
		String errorMessage="";
		if(null!=customRole.get("role_id") && !"".equalsIgnoreCase(customRole.get("role_id").toString())){
			String roleId=customRole.get("role_id").toString();
			if(!roleId.matches("^[a-zA-Z0-9_\\.]{3,64}$")){
				errorMessage="role_id must match the pattern : ^[a-zA-Z0-9_\\.]{3,64}$";
			}
		}
		if(null!=customRole.get("title") && !"".equalsIgnoreCase(customRole.get("title").toString())){
			String title=customRole.get("title").toString();
			if(!title.matches("^[a-zA-Z0-9_\\.]{3,64}$")){
				errorMessage +="\ntitle must match the pattern : ^[a-zA-Z0-9_\\.]{3,64}$";
			}
		}
		
		return errorMessage;
	}


}
