resource "aws_lb" "nlb" {
  count = local.deploy_places || local.deploy_bff ? 1 : 0
  name               = "my-network-loadbalancer"
  load_balancer_type = "network"
  security_groups = [aws_security_group.allow_all_traffic.id]
  subnets = [aws_subnet.private_subnet_a.id]

  enable_deletion_protection       = false
  enable_cross_zone_load_balancing = true
  internal                         = true
}