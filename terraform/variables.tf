# General
variable "aws_region" {
  description = "AWS region to deploy all resources"
  type        = string
  default     = "ap-south-1"
}

variable "environment" {
  description = "Deployment environment (dev / staging / prod)"
  type        = string
  default     = "prod"
}

# RDS
variable "db_name" {
  description = "Name of the MySQL database to create inside RDS"
  type        = string
  default     = "tracelink_db"
}

variable "db_username" {
  description = "Master username for the RDS instance"
  type        = string
}

variable "db_password" {
  description = "Master password for the RDS instance (use a strong password)"
  type        = string
  sensitive   = true
}

variable "db_instance_class" {
  description = "RDS instance type"
  type        = string
  default     = "db.t3.small"
}

variable "db_allocated_storage" {
  description = "Allocated storage for RDS in GB"
  type        = number
  default     = 20
}

variable "db_multi_az" {
  description = "Enable Multi-AZ for high availability (recommended for production)"
  type        = bool
  default     = false # Set to true for actual production
}

variable "db_backup_retention_days" {
  description = "Number of days to retain automated backups (0 = disabled)"
  type        = number
  default     = 0
}

# ECR
variable "ecr_backend_repo_name" {
  description = "Name of the ECR repository for the backend Docker image"
  type        = string
  default     = "tracelink-backend"
}

variable "ecr_image_retention_count" {
  description = "Number of Docker images to retain in ECR before pruning old ones"
  type        = number
  default     = 10
}
