# ECS Execution Role
resource "aws_iam_role" "ecs_execution_task_role" {
  name = "${var.component_name}-exec-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Sid    = ""
        Principal = {
          "Service" : "ecs-tasks.amazonaws.com"
        }
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "ecs_execution_managed_policy_attachment" {
  role       = aws_iam_role.ecs_execution_task_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# ECS Task Role
resource "aws_iam_role" "task_role" {
  name = "${var.component_name}-task-role"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Sid    = ""
        Principal = {
          "Service" : "ecs-tasks.amazonaws.com"
        }
      },
    ]
  })
  description = "Role for ECS task full access"
}

resource "aws_iam_role_policy" "extra_policies" {
  name = "${var.component_name}-extra-policies"
  role = aws_iam_role.task_role.*.id[0]
  policy = jsonencode({
    "Version" : "2012-10-17",
    "Statement" : [
      {
        "Action" : [
          "cloudwatch:*",
          "aurora:*",
          "ecs:*",
          "ecr:*"
        ],
        "Effect" : "Allow",
        "Resource" : "*"
      }
    ]
  })
}