provider "aws" {
  region = "eu-central-1"
}

terraform {
  required_version = ">= 1.6"
  backend "s3" {
    region = "eu-central-1"
    bucket = "ai-to-generate-graphql-poc-terraform-state"
    dynamodb_table = "ai-to-generate-graphql-poc-terraform-state"
    key = "terraform.tfstate"
  }
}

locals {
  enable_bastion = true
}