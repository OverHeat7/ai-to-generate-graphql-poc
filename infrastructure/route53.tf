resource "aws_route53_zone" "private_zone" {
  name = "private.com"

  vpc {
    vpc_id = aws_vpc.vpc.id
  }
}

resource "aws_route53_record" "apps" {
  zone_id = aws_route53_zone.private_zone.zone_id
  name    = "apps.private.com"
  type    = "A"

  alias {
    name                   = aws_lb.nlb.dns_name
    zone_id                = aws_lb.nlb.zone_id
    evaluate_target_health = true
  }
}