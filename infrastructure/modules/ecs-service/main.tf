data "aws_region" "current" {}

# Create ECR
resource "aws_ecr_repository" "ecr" {
  name                 = var.component_name
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = false
  }
}
# Create cloudwatch log group
resource "aws_cloudwatch_log_group" "log_group" {
  name              = "/ecs-service/${var.component_name}"
  retention_in_days = 7
}


# Create ECS Task Definition
resource "aws_ecs_task_definition" "task" {
  family       = var.component_name
  network_mode = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu          = var.cpu
  memory       = var.memory

  container_definitions = templatefile(var.task_definition_file_path, local.templatefile_arguments)
  execution_role_arn = aws_iam_role.ecs_execution_task_role.arn
  task_role_arn      = aws_iam_role.task_role.arn
}

# Create ECS Service
resource "aws_ecs_service" "ecs_service" {
  name                 = var.component_name
  cluster              = var.cluster_arn
  task_definition      = aws_ecs_task_definition.task.arn
  desired_count        = var.desired_count
  launch_type          = "FARGATE"
  force_new_deployment = false

  network_configuration {
    subnets          = var.private_subnet_ids
    assign_public_ip = false
    security_groups = [var.security_group_id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.service_nlb_tg.arn
    container_name   = var.component_name
    container_port   = var.application_port
  }

  lifecycle {
    ignore_changes = [
      desired_count
    ]
  }
}