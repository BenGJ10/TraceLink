# RDS Outputs
output "rds_endpoint" {
  description = "Full RDS endpoint (host:port) — use in DB_URL"
  value       = aws_db_instance.tracelink_db.endpoint
}

output "rds_host" {
  description = "Hostname-only RDS endpoint (without port)"
  value       = aws_db_instance.tracelink_db.address
}

output "rds_port" {
  description = "RDS port"
  value       = aws_db_instance.tracelink_db.port
}

output "rds_db_name" {
  description = "Name of the MySQL database"
  value       = aws_db_instance.tracelink_db.db_name
}

output "rds_username" {
  description = "Master username"
  value       = aws_db_instance.tracelink_db.username
  sensitive   = true
}

output "spring_db_url" {
  description = "Ready-to-use Spring Boot JDBC URL for DB_URL env variable"
  value       = "jdbc:mysql://${aws_db_instance.tracelink_db.endpoint}/${var.db_name}?useSSL=true&requireSSL=false&serverTimezone=UTC"
}

# ECR Outputs
output "ecr_repository_url" {
  description = "Full ECR repository URL — used in docker tag and docker push commands"
  value       = aws_ecr_repository.backend.repository_url
}

output "ecr_repository_name" {
  description = "ECR repository name"
  value       = aws_ecr_repository.backend.name
}

output "ecr_registry_id" {
  description = "AWS account ID (ECR registry ID)"
  value       = aws_ecr_repository.backend.registry_id
}

output "docker_login_command" {
  description = "Command to authenticate Docker with ECR"
  value       = "aws ecr get-login-password --region ${var.aws_region} | docker login --username AWS --password-stdin ${aws_ecr_repository.backend.repository_url}"
}
