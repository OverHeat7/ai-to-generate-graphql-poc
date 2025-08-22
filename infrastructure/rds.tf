resource "aws_db_subnet_group" "db_subnet" {
  count       = local.deploy_places ? 1 : 0
  name        = "private-subnet-group"
  subnet_ids = [aws_subnet.private_subnet_a.id, aws_subnet.private_subnet_b.id, aws_subnet.private_subnet_c.id]
  description = "Subnet group for RDS instances in private subnets"
  tags = {
    Name = "private-subnet-group"
  }
}

resource "aws_db_instance" "postgres" {
  count                   = local.deploy_places ? 1 : 0
  identifier              = "places-pois-db"
  engine                  = "postgres"
  engine_version          = "17.4"
  instance_class          = "db.t4g.micro"
  db_name                 = "postgis"
  username                = "postgis"
  password                = "password"
  skip_final_snapshot     = true
  vpc_security_group_ids = [aws_security_group.allow_all_traffic.id]
  db_subnet_group_name    = aws_db_subnet_group.db_subnet[0].id
  allocated_storage       = 15
  backup_retention_period = 1
  storage_type            = "gp2"
  apply_immediately       = true
  publicly_accessible     = false
}
