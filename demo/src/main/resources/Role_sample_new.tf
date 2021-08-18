
module "project-custom-role" {
  source               = "mahesh-madipally/db_Role_template/google"
  target_level         = "project"
  target_id            = "db-sample-314713"
  role_id              = "sample"
  title                = "sample"
  description          = "Custom Role Description"
  permissions          = ["iam.roles.list", "iam.roles.create", "iam.roles.delete","container.clusters.get","container.clusters.list"]
}
