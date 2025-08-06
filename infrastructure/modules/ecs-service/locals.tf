locals {
  templatefile_arguments = merge({
    image          = aws_ecr_repository.ecr.repository_url
    container_name = var.component_name
    app_port       = var.application_port
    aws_log_group  = aws_cloudwatch_log_group.log_group.name
    aws_region     = data.aws_region.current.region
  }, var.templatefile_arguments)
}