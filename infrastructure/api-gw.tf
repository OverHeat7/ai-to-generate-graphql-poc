resource "aws_api_gateway_rest_api" "api_gateway" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  name        = "places-api-gateway"
  description = "API Gateway for Places Service"
  endpoint_configuration {
    types = ["REGIONAL"]
  }
}

resource "aws_api_gateway_vpc_link" "vpc_link" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  name        = "places-vpc-link"
  description = "VPC Link for Places Service"
  target_arns = [aws_lb.nlb[0].arn]
}

resource "aws_api_gateway_resource" "places" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  rest_api_id = aws_api_gateway_rest_api.api_gateway[0].id
  parent_id   = aws_api_gateway_rest_api.api_gateway[0].root_resource_id
  path_part   = "places"
}

resource "aws_api_gateway_resource" "places_v1" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  rest_api_id = aws_api_gateway_rest_api.api_gateway[0].id
  parent_id   = aws_api_gateway_resource.places[0].id
  path_part   = "v1"
}

resource "aws_api_gateway_resource" "places_v1_search" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  rest_api_id = aws_api_gateway_rest_api.api_gateway[0].id
  parent_id   = aws_api_gateway_resource.places_v1[0].id
  path_part   = "search"
}

resource "aws_api_gateway_resource" "places_v1_ai_search" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  rest_api_id = aws_api_gateway_rest_api.api_gateway[0].id
  parent_id   = aws_api_gateway_resource.places_v1[0].id
  path_part   = "ai-search"
}

resource "aws_api_gateway_method" "places_v1_search_post" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  rest_api_id = aws_api_gateway_rest_api.api_gateway[0].id
  resource_id = aws_api_gateway_resource.places_v1_search[0].id

  authorization    = "NONE"
  http_method      = "POST"
  api_key_required = true
}

resource "aws_api_gateway_method" "places_v1_ai_search_post" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  rest_api_id = aws_api_gateway_rest_api.api_gateway[0].id
  resource_id = aws_api_gateway_resource.places_v1_ai_search[0].id

  authorization    = "NONE"
  http_method      = "POST"
  api_key_required = true
}

resource "aws_api_gateway_integration" "places_v1_search_post" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  rest_api_id = aws_api_gateway_rest_api.api_gateway[0].id
  resource_id = aws_api_gateway_resource.places_v1_search[0].id
  http_method = aws_api_gateway_method.places_v1_search_post[0].http_method

  type                    = "HTTP_PROXY"
  integration_http_method = aws_api_gateway_method.places_v1_search_post[0].http_method
  uri                     = "http://apps.private.com:8980/v1/search"
  connection_type         = "VPC_LINK"
  connection_id           = aws_api_gateway_vpc_link.vpc_link[0].id

  tls_config {
    insecure_skip_verification = true
  }
}


resource "aws_api_gateway_integration" "places_v1_ai_search_post" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  rest_api_id = aws_api_gateway_rest_api.api_gateway[0].id
  resource_id = aws_api_gateway_resource.places_v1_ai_search[0].id
  http_method = aws_api_gateway_method.places_v1_ai_search_post[0].http_method

  type                    = "HTTP_PROXY"
  integration_http_method = aws_api_gateway_method.places_v1_ai_search_post[0].http_method
  uri                     = "http://apps.private.com:8981/v1"
  connection_type         = "VPC_LINK"
  connection_id           = aws_api_gateway_vpc_link.vpc_link[0].id

  tls_config {
    insecure_skip_verification = true
  }
}

resource "aws_api_gateway_deployment" "places_deployment" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  depends_on = [aws_api_gateway_integration.places_v1_search_post,
    aws_api_gateway_integration.places_v1_ai_search_post]
  rest_api_id = aws_api_gateway_rest_api.api_gateway[0].id

  triggers = {
    always = timestamp()
  }

  lifecycle {
    create_before_destroy = true
  }
}

resource "aws_api_gateway_stage" "places_stage" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  rest_api_id   = aws_api_gateway_rest_api.api_gateway[0].id
  stage_name    = "deploy"
  deployment_id = aws_api_gateway_deployment.places_deployment[0].id

  variables = {
    api_version = "v1"
  }
}


resource "aws_api_gateway_api_key" "places_api_key" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  name        = "places-api-key"
  description = "API Key for Places Service"
  enabled     = true
}

resource "aws_api_gateway_usage_plan" "usage_plan" {
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  name        = "places-usage-plan"
  description = "Usage plan for Places Service API"
  api_stages {
    api_id = aws_api_gateway_rest_api.api_gateway[0].id
    stage  = aws_api_gateway_stage.places_stage[0].stage_name
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
  count = local.deploy_places && local.deploy_bff ? 1 : 0
  key_id        = aws_api_gateway_api_key.places_api_key[0].id
  key_type      = "API_KEY"
  usage_plan_id = aws_api_gateway_usage_plan.usage_plan[0].id
}