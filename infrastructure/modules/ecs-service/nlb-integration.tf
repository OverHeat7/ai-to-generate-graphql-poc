resource "aws_lb_listener" "nlb_listener" {
  load_balancer_arn = var.nlb_arn
  port              = var.nlb_port
  protocol          = "TCP"

  default_action {
    target_group_arn = aws_lb_target_group.service_nlb_tg.arn
    type             = "forward"
  }

  depends_on = [
    aws_lb_target_group.service_nlb_tg
  ]
}

resource "aws_lb_target_group" "service_nlb_tg" {
  name = "${var.component_name}-nlb-tg"

  port                 = var.application_port
  protocol             = "TCP"
  vpc_id               = var.vpc_id
  target_type          = "ip"
  deregistration_delay = 60

  # health_check {
  #   healthy_threshold   = var.healthy_threshold
  #   unhealthy_threshold = var.unhealthy_threshold
  #   path                = var.healthcheck_path
  #   port                = var.healthcheck_port
  #   protocol            = var.healthcheck_protocol
  #   interval            = var.healthcheck_interval
  # }
}