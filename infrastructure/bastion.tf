resource "aws_instance" "Bastion" {
  count             = local.enable_bastion ? 1 : 0
  ami               = "ami-0233214e13e500f77"
  instance_type     = "t2.micro"
  availability_zone = "eu-central-1a"
  key_name          = "bastion-key"
  tenancy           = "default"
  subnet_id         = aws_subnet.public_subnet_a.id
  vpc_security_group_ids = [aws_security_group.allow_all_traffic.id]

  # Attach the instance profile here
  iam_instance_profile = aws_iam_instance_profile.ssm_instance_profile.name

  tags = {
    Name = "bastion-host"
  }
}

# Create IAM Role for EC2 to use SSM
resource "aws_iam_role" "ssm_role" {
  name = "ec2-ssm-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17",
    Statement = [{
      Action = "sts:AssumeRole",
      Principal = {
        Service = "ec2.amazonaws.com"
      },
      Effect = "Allow",
      Sid    = ""
    }]
  })
}

# Attach the AWS-managed SSM policy
resource "aws_iam_role_policy_attachment" "ssm_attach" {
  role       = aws_iam_role.ssm_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Create instance profile for EC2
resource "aws_iam_instance_profile" "ssm_instance_profile" {
  name = "ec2-ssm-instance-profile"
  role = aws_iam_role.ssm_role.name
}