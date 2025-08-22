resource "aws_vpc" "vpc" {
  cidr_block           = "10.0.0.0/16"
  instance_tenancy     = "default"
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "my-vpc"
  }
}

# Create internet gateway
resource "aws_internet_gateway" "igw" {
  vpc_id = aws_vpc.vpc.id

  tags = {
    Name = "my-internet-gateway"
  }
}

# Create public subnet A
resource "aws_subnet" "public_subnet_a" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.20.0/24"
  availability_zone       = "us-east-1a"
  map_public_ip_on_launch = "true"

  tags = {
    Name = "public-subnet-a"
  }
}
# Create public subnet B
resource "aws_subnet" "public_subnet_b" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.40.0/24"
  availability_zone       = "us-east-1b"
  map_public_ip_on_launch = "true"

  tags = {
    Name = "public-subnet-b"
  }
}
# Create public subnet C
resource "aws_subnet" "public_subnet_c" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.50.0/24"
  availability_zone       = "us-east-1c"
  map_public_ip_on_launch = "true"

  tags = {
    Name = "public-subnet-c"
  }
}

# Create NACL for public subnet
resource "aws_network_acl" "public_subnet_nacl" {
  vpc_id = aws_vpc.vpc.id
  subnet_ids = [aws_subnet.public_subnet_a.id, aws_subnet.public_subnet_b.id, aws_subnet.public_subnet_c.id]

  ingress {
    protocol   = -1
    rule_no    = 100
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }

  egress {
    protocol   = -1
    rule_no    = 100
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }
}

resource "aws_network_acl_association" "public_subnet_a_nacl_association" {
  network_acl_id = aws_network_acl.public_subnet_nacl.id
  subnet_id      = aws_subnet.public_subnet_a.id
}
resource "aws_network_acl_association" "public_subnet_b_nacl_association" {
  network_acl_id = aws_network_acl.public_subnet_nacl.id
  subnet_id      = aws_subnet.public_subnet_b.id
}
resource "aws_network_acl_association" "public_subnet_c_nacl_association" {
  network_acl_id = aws_network_acl.public_subnet_nacl.id
  subnet_id      = aws_subnet.public_subnet_c.id
}

# Create route table for public subnet
resource "aws_route_table" "public_subnet_rt" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.igw.id
  }
  tags = {
    Name = "public-subnet-rt"
  }
}

resource "aws_route_table_association" "public_subnet_a_association" {
  subnet_id      = aws_subnet.public_subnet_a.id
  route_table_id = aws_route_table.public_subnet_rt.id
}
resource "aws_route_table_association" "public_subnet_b_association" {
  subnet_id      = aws_subnet.public_subnet_b.id
  route_table_id = aws_route_table.public_subnet_rt.id
}
resource "aws_route_table_association" "public_subnet_c_association" {
  subnet_id      = aws_subnet.public_subnet_c.id
  route_table_id = aws_route_table.public_subnet_rt.id
}

# Create Elastic IP for NAT Gateway A
resource "aws_eip" "eip_for_nat_gw_a" {
  domain = "vpc"
  tags = {
    Name = "nat-gateway-eip-a"
  }
}
# Create Elastic IP for NAT Gateway B
resource "aws_eip" "eip_for_nat_gw_b" {
  domain = "vpc"
  tags = {
    Name = "nat-gateway-eip-b"
  }
}
# Create Elastic IP for NAT Gateway C
resource "aws_eip" "eip_for_nat_gw_c" {
  domain = "vpc"
  tags = {
    Name = "nat-gateway-eip-c"
  }
}

# Create NAT Gateway A
resource "aws_nat_gateway" "nat_gw_a" {
  subnet_id         = aws_subnet.public_subnet_a.id
  connectivity_type = "public"
  allocation_id     = aws_eip.eip_for_nat_gw_a.id
  tags = {
    Name = "my-nat-gateway-a"
  }
}
# Create NAT Gateway B
resource "aws_nat_gateway" "nat_gw_b" {
  subnet_id         = aws_subnet.public_subnet_b.id
  connectivity_type = "public"
  allocation_id     = aws_eip.eip_for_nat_gw_b.id
  tags = {
    Name = "my-nat-gateway-b"
  }
}
# Create NAT Gateway C
resource "aws_nat_gateway" "nat_gw_c" {
  subnet_id         = aws_subnet.public_subnet_c.id
  connectivity_type = "public"
  allocation_id     = aws_eip.eip_for_nat_gw_c.id
  tags = {
    Name = "my-nat-gateway-c"
  }
}

# Create private subnet A
resource "aws_subnet" "private_subnet_a" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.10.0/24"
  availability_zone       = "us-east-1a"
  map_public_ip_on_launch = "false"
  tags = {
    Name = "private-subnet-a"
  }
}

# Create private subnet B
resource "aws_subnet" "private_subnet_b" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.0.0/24"
  availability_zone       = "us-east-1b"
  map_public_ip_on_launch = "false"
  tags = {
    Name = "private-subnet-b"
  }
}

# Create private subnet C
resource "aws_subnet" "private_subnet_c" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.30.0/24"
  availability_zone       = "us-east-1c"
  map_public_ip_on_launch = "false"
  tags = {
    Name = "private-subnet-c"
  }
}


# Create private subnet D
resource "aws_subnet" "private_subnet_d" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.70.0/24"
  availability_zone       = "us-east-1d"
  map_public_ip_on_launch = "false"
  tags = {
    Name = "private-subnet-d"
  }
}


# Create private subnet E
resource "aws_subnet" "private_subnet_e" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.80.0/24"
  availability_zone       = "us-east-1e"
  map_public_ip_on_launch = "false"
  tags = {
    Name = "private-subnet-e"
  }
}

# Create private subnet F
resource "aws_subnet" "private_subnet_f" {
  vpc_id                  = aws_vpc.vpc.id
  cidr_block              = "10.0.60.0/24"
  availability_zone       = "us-east-1f"
  map_public_ip_on_launch = "false"
  tags = {
    Name = "private-subnet-f"
  }
}

# Create NACL for private subnet
resource "aws_network_acl" "private_subnet_nacl" {
  vpc_id = aws_vpc.vpc.id
  subnet_ids = [aws_subnet.private_subnet_a.id, aws_subnet.private_subnet_b.id, aws_subnet.private_subnet_c.id]

  ingress {
    protocol   = -1
    rule_no    = 100
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }

  egress {
    protocol   = -1
    rule_no    = 100
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 0
    to_port    = 0
  }
}

resource "aws_network_acl_association" "private_subnet_a_nacl_association" {
  network_acl_id = aws_network_acl.private_subnet_nacl.id
  subnet_id      = aws_subnet.private_subnet_a.id
}

resource "aws_network_acl_association" "private_subnet_b_nacl_association" {
  network_acl_id = aws_network_acl.private_subnet_nacl.id
  subnet_id      = aws_subnet.private_subnet_b.id
}
resource "aws_network_acl_association" "private_subnet_c_nacl_association" {
  network_acl_id = aws_network_acl.private_subnet_nacl.id
  subnet_id      = aws_subnet.private_subnet_c.id
}

# Create route table for private subnet A
resource "aws_route_table" "private_subnet_rt_a" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_nat_gateway.nat_gw_a.id
  }
  tags = {
    Name = "private-subnet-rt-a"
  }
}
# Create route table for private subnet B
resource "aws_route_table" "private_subnet_rt_b" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_nat_gateway.nat_gw_b.id
  }
  tags = {
    Name = "private-subnet-rt-b"
  }
}
# Create route table for private subnet C
resource "aws_route_table" "private_subnet_rt_c" {
  vpc_id = aws_vpc.vpc.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_nat_gateway.nat_gw_c.id
  }
  tags = {
    Name = "private-subnet-rt-c"
  }
}

resource "aws_route_table_association" "private_subnet_a_rt_association" {
  subnet_id      = aws_subnet.private_subnet_a.id
  route_table_id = aws_route_table.private_subnet_rt_a.id
}
resource "aws_route_table_association" "private_subnet_b_rt_association" {
  subnet_id      = aws_subnet.private_subnet_b.id
  route_table_id = aws_route_table.private_subnet_rt_b.id
}
resource "aws_route_table_association" "private_subnet_c_rt_association" {
  subnet_id      = aws_subnet.private_subnet_c.id
  route_table_id = aws_route_table.private_subnet_rt_c.id
}
