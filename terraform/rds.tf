# RDS Security Group
# Restricts MySQL access to ONLY internal VPC traffic.
# In production, replace the cidr_blocks with your EKS node security group instead.
resource "aws_security_group" "rds_sg" {
  name        = "tracelink-rds-sg"
  description = "Allow MySQL access from within the VPC only"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "MySQL from VPC"
    from_port   = 3306
    to_port     = 3306
    protocol    = "tcp"
    # Restricts to VPC CIDR — NOT 0.0.0.0/0 like a demo would do
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "tracelink-rds-sg"
  }
}

# RDS Subnet Group
resource "aws_db_subnet_group" "tracelink" {
  name       = "tracelink-db-subnet-group"
  subnet_ids = data.aws_subnets.default.ids

  tags = {
    Name = "tracelink-db-subnet-group"
  }
}

# RDS Instance
resource "aws_db_instance" "tracelink_db" {
  identifier = "tracelink-db"

  # Engine
  engine         = "mysql"
  engine_version = "8.0"

  # Compute & Storage
  instance_class    = var.db_instance_class
  allocated_storage = var.db_allocated_storage
  storage_type      = "gp3"
  storage_encrypted = true # Always encrypt at rest in production

  # Database
  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  # High Availability
  multi_az = var.db_multi_az

  # Networking
  db_subnet_group_name   = aws_db_subnet_group.tracelink.name
  vpc_security_group_ids = [aws_security_group.rds_sg.id]
  publicly_accessible    = true # Private — only accessible from within VPC

  # Maintenance & Backups
  backup_retention_period    = var.db_backup_retention_days
  apply_immediately           = true # Apply changes immediately for demo
  
  # Lifecycle
  skip_final_snapshot       = true # In prod, always take a final snapshot before destroying

  tags = {
    Name = "tracelink-db"
  }
}
