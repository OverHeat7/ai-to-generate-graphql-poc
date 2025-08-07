resource "aws_api_gateway_rest_api" "api_gateway" {
  name        = "places-api-gateway"
  description = "API Gateway for Places Service"
  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_vpc_link" "vpc_link" {
  name        = "places-vpc-link"
  description = "VPC Link for Places Service"
  target_arns = [aws_lb.nlb.arn]
}

resource "aws_api_gateway_resource" "places" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  parent_id   = aws_api_gateway_rest_api.api_gateway.root_resource_id
  path_part   = "places"
}

resource "aws_api_gateway_resource" "places_v1" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  parent_id   = aws_api_gateway_resource.places.id
  path_part   = "v1"
}

resource "aws_api_gateway_resource" "places_v1_search" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  parent_id   = aws_api_gateway_resource.places_v1.id
  path_part   = "search"
}

resource "aws_api_gateway_resource" "places_v1_ai_search" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  parent_id   = aws_api_gateway_resource.places_v1.id
  path_part   = "ai-search"
}

resource "aws_api_gateway_method" "places_v1_search_post" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  resource_id = aws_api_gateway_resource.places_v1_search.id

  authorization    = "NONE"
  http_method      = "POST"
  api_key_required = true
}

resource "aws_api_gateway_method" "places_v1_ai_search_post" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  resource_id = aws_api_gateway_resource.places_v1_ai_search.id

  authorization    = "NONE"
  http_method      = "POST"
  api_key_required = true
}

resource "aws_api_gateway_integration" "places_v1_search_post" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  resource_id = aws_api_gateway_resource.places_v1_search.id
  http_method = aws_api_gateway_method.places_v1_search_post.http_method

  type                    = "HTTP_PROXY"
  integration_http_method = aws_api_gateway_method.places_v1_search_post.http_method
  uri                     = "http://apps.private.com:8980/v1/search"
  connection_type         = "VPC_LINK"
  connection_id           = aws_api_gateway_vpc_link.vpc_link.id

  tls_config {
    insecure_skip_verification = true
  }
}


resource "aws_api_gateway_integration" "places_v1_ai_search_post" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id
  resource_id = aws_api_gateway_resource.places_v1_ai_search.id
  http_method = aws_api_gateway_method.places_v1_ai_search_post.http_method

  type                    = "HTTP_PROXY"
  integration_http_method = aws_api_gateway_method.places_v1_ai_search_post.http_method
  uri                     = "http://apps.private.com:8981/v1"
  connection_type         = "VPC_LINK"
  connection_id           = aws_api_gateway_vpc_link.vpc_link.id

  tls_config {
    insecure_skip_verification = true
  }
}

resource "aws_api_gateway_deployment" "places_deployment" {
  rest_api_id = aws_api_gateway_rest_api.api_gateway.id

  triggers = {
    always = timestamp()
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_stage" "places_stage" {
  rest_api_id   = aws_api_gateway_rest_api.api_gateway.id
  stage_name    = "deploy"
  deployment_id = aws_api_gateway_deployment.places_deployment.id

  variables = {
    api_version = "v1"
  }
}


resource "aws_api_gateway_api_key" "places_api_key" {
  name        = "places-api-key"
  description = "API Key for Places Service"
  enabled     = true
}

resource "aws_api_gateway_usage_plan" "usage_plan" {
  name        = "places-usage-plan"
  description = "Usage plan for Places Service API"
  api_stages {
    api_id = aws_api_gateway_rest_api.api_gateway.id
    stage  = aws_api_gateway_stage.places_stage.stage_name
  }
  throttle_settings {
    burst_limit = 100
    rate_limit  = 50
  }
  quota_settings {
    limit  = 1000000
    period = "MONTH"
  }
}

resource "aws_api_gateway_usage_plan_key" "usage_plan_key" {
  key_id        = aws_api_gateway_api_key.places_api_key.id
  key_type      = "API_KEY"
  usage_plan_id = aws_api_gateway_usage_plan.usage_plan.id
}