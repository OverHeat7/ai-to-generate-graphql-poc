resource "aws_ecs_cluster" "cluster" {
  count = local.deploy_bff || local.deploy_places ? 1 : 0
  name = "my-ecs-cluster"

  setting {
    name  = "containerInsights"
    value = "disabled"
  }
}

module "places-service" {
  count = local.deploy_places ? 1 : 0
  source = "./modules/ecs-service"
  depends_on = [aws_db_instance.postgres]

  component_name            = "places"
  task_definition_file_path = "${path.module}/files/places_task_definition.json"
  cpu                       = 512
  memory                    = 1024
  templatefile_arguments = {
    db_host     = aws_db_instance.postgres[0].address
    db_port     = aws_db_instance.postgres[0].port
    db_name     = "postgis"
    db_user     = "postgis"
    db_password = "password"
  }
  cluster_arn       = aws_ecs_cluster.cluster[0].arn
  nlb_arn           = aws_lb.nlb[0].arn
  application_port  = 8980
  nlb_port          = 8980
  private_subnet_ids = [aws_subnet.private_subnet_a.id]
  security_group_id = aws_security_group.allow_all_traffic.id
  vpc_id            = aws_vpc.vpc.id
}


module "bff-service" {
  count = local.deploy_bff ? 1 : 0
  source = "./modules/ecs-service"

  component_name            = "bff"
  task_definition_file_path = "${path.module}/files/bff_task_definition.json"
  cpu                       = 512
  memory                    = 1024
  templatefile_arguments = {
    call_real_llm = "true"
    places_url    = "http://apps.private.com:8980/graphql"
  }
  cluster_arn       = aws_ecs_cluster.cluster[0].arn
  nlb_arn           = aws_lb.nlb[0].arn
  application_port  = 8981
  nlb_port          = 8981
  private_subnet_ids = [aws_subnet.private_subnet_a.id]
  security_group_id = aws_security_group.allow_all_traffic.id
  vpc_id            = aws_vpc.vpc.id
}