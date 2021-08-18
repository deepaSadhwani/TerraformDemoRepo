module "project-predefined-role" {
  source = "mahesh-madipally/db_Role_template/google"
  target_level         = "project"
  target_id            = "db-sample-314713"
  role_id              = "custom_role_123"
  title                = "custom_role_123"
  description          = "Custom Role Description"
  base_roles           = ["roles/iam.serviceAccountAdmin"]
  permissions          = ["iam.roles.list", "iam.roles.create", "iam.roles.delete"]
  excluded_permissions = ["iam.serviceAccounts.setIamPolicy"]
  
}

