variable "component_name" {
  description = "The name of the component, used for naming resources. Valid values are 'places' and 'bff'."
  type        = string
  validation {
    condition     = var.component_name == "places" || var.component_name == "bff"
    error_message = "The component_name variable must be either 'places' or 'bff'."
  }
}

variable "task_definition_file_path" {
  description = "The file path to the ECS task definition JSON template."
  type        = string
  validation {
    condition = can(file(var.task_definition_file_path))
    error_message = "The task_definition_file_path must point to a valid file."
  }
}

variable "cpu" {
  description = "The amount of CPU to allocate to the ECS task."
  type        = number
  default     = 256
  # Valid values are 256, 512, 1024, 2048, or 4096
  validation {
    condition = contains([256, 512, 1024, 2048, 4096], var.cpu)
    error_message = "The cpu variable must be one of the following values: 256, 512, 1024, 2048, or 4096."
  }
}

variable "memory" {
  description = "The amount of memory (in MiB) to allocate to the ECS task."
  type        = number
  default     = 512
  # Valid values are 512, 1024, 2048, 3072, 4096, 5120, 6144, 7168, or 8192
  validation {
    condition = contains([512, 1024, 2048, 3072, 4096, 5120, 6144, 7168, 8192], var.memory)
    error_message = "The memory variable must be one of the following values: 512, 1024, 2048, 3072, 4096, 5120, 6144, 7168, or 8192."
  }
}

variable "templatefile_arguments" {
  description = "A map of arguments to pass to the templatefile function for rendering templates."
  type = map(any)
  default = {}
}

variable "application_port" {
  description = "The port on which the application listens inside the container."
  type        = number
  default     = 8080
}

variable "nlb_port" {
  description = "The port on which the Network Load Balancer will listen."
  type        = number
  validation {
    condition     = var.nlb_port > 0 && var.nlb_port < 65536
    error_message = "The nlb_port variable must be a valid port number between 1 and 65535."
  }
}

variable "cluster_arn" {
  description = "The ARN of the ECS cluster where the service will be deployed."
  type        = string
  validation {
    condition = can(regex("^arn:aws:ecs:[a-z]{2}-[a-z]+-[1-9][0-9]*:[0-9]+:cluster/[a-zA-Z0-9-_]+$", var.cluster_arn))
    error_message = "The cluster_arn variable must be a valid ECS cluster ARN."
  }
}

variable "desired_count" {
  description = "The desired number of tasks to run in the ECS service."
  type        = number
  default     = 1
  validation {
    condition     = var.desired_count >= 0
    error_message = "The desired_count variable must be a non-negative integer."
  }
}

variable "private_subnet_ids" {
  description = "A list of private subnet IDs where the ECS service will run."
  type = list(string)
  validation {
    condition     = length(var.private_subnet_ids) > 0
    error_message = "The private_subnet_ids variable must contain at least one subnet ID."
  }
}

variable "security_group_id" {
  description = "The ID of the security group to associate with the ECS service."
  type        = string
  validation {
    condition = can(regex("^sg-[a-z0-9]+$", var.security_group_id))
    error_message = "The security_group_id variable must be a valid security group ID."
  }
}

variable "nlb_arn" {
  description = "The ARN of the Network Load Balancer to associate with the ECS service."
  type        = string
  validation {
    condition = can(regex("^arn:aws:elasticloadbalancing:[a-z]{2}-[a-z]+-[1-9][0-9]*:[0-9]+:loadbalancer/net/[a-zA-Z0-9-_]+/[a-zA-Z0-9-_]+$", var.nlb_arn))
    error_message = "The nlb_arn variable must be a valid Network Load Balancer ARN."
  }
}

variable "vpc_id" {
  description = "The ID of the VPC where the ECS service and resources will be created."
  type        = string
  validation {
    condition = can(regex("^vpc-[a-z0-9]+$", var.vpc_id))
    error_message = "The vpc_id variable must be a valid VPC ID."
  }
}