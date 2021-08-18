module  "project-custom-role"{
source			= "mahesh-madipally/db_Role_template/google"
target_level     = "project"
target_id        = "db-sample-314713"
role_id          = "base_role_devlop"
title            = "base_role_devlop"
description 		= "Predefined Role Description"
permissions 		=["iam.roles.list", "iam.roles.create"]
base_roles       =["roles/iam.serviceAccountAdmin"]

 }
