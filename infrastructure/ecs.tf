resource "aws_ecs_cluster" "cluster" {
  name = "my-ecs-cluster"

  setting {
    name  = "containerInsights"
    value = "enabled"
  }
}

module "places-service" {
  source = "./modules/ecs-service"

  component_name            = "places"
  task_definition_file_path = "${path.module}/files/places_task_definition.json"
  cpu                       = 512
  memory                    = 1024
  templatefile_arguments = {
    db_host     = "todo"
    db_port     = "todo"
    db_name     = "todo"
    db_user     = "todo"
    db_password = "todo"
  }
  cluster_arn       = aws_ecs_cluster.cluster.arn
  nlb_arn           = aws_lb.nlb.arn
  application_port  = 8980
  nlb_port          = 8980
  private_subnet_ids = [aws_subnet.private_subnet_a.id]
  security_group_id = aws_security_group.allow_all_traffic.id
  vpc_id            = aws_vpc.vpc.id
}


module "bff-service" {
  source = "./modules/ecs-service"

  component_name            = "bff"
  task_definition_file_path = "${path.module}/files/bff_task_definition.json"
  cpu                       = 512
  memory                    = 1024
  templatefile_arguments = {
    call_real_llm = "false"
    llm_url       = "todo"
    places_url    = "http://${aws_lb.nlb.dns_name}:8980"
  }
  cluster_arn       = aws_ecs_cluster.cluster.arn
  nlb_arn           = aws_lb.nlb.arn
  application_port  = 8981
  nlb_port          = 8981
  private_subnet_ids = [aws_subnet.private_subnet_a.id]
  security_group_id = aws_security_group.allow_all_traffic.id
  vpc_id            = aws_vpc.vpc.id
}