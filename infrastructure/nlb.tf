resource "aws_lb" "nlb" {
  name               = "my-network-loadbalancer"
  load_balancer_type = "network"
  security_groups = [aws_security_group.allow_all_traffic.id]
  subnets = [aws_subnet.private_subnet_a.id]

  enable_deletion_protection = true
}