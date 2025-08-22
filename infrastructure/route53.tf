resource "aws_route53_zone" "private_zone" {
  count = local.deploy_places || local.deploy_bff ? 1 : 0
  name  = "private.com"

  vpc {
    vpc_id = aws_vpc.vpc.id
  }
}

resource "aws_route53_record" "apps" {
  count   = local.deploy_places || local.deploy_bff ? 1 : 0
  zone_id = aws_route53_zone.private_zone[0].zone_id
  name    = "apps.private.com"
  type    = "A"

  alias {
    name                   = aws_lb.nlb[0].dns_name
    zone_id                = aws_lb.nlb[0].zone_id
    evaluate_target_health = true
  }
}