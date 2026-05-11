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

# EKS Outputs
output "eks_cluster_name" {
  description = "EKS cluster name"
  value       = aws_eks_cluster.tracelink.name
}

output "eks_cluster_endpoint" {
  description = "EKS API server endpoint"
  value       = aws_eks_cluster.tracelink.endpoint
}

output "eks_cluster_version" {
  description = "Kubernetes version running on the cluster"
  value       = aws_eks_cluster.tracelink.version
}

output "eks_oidc_issuer" {
  description = "OIDC issuer URL — needed for IRSA (IAM Roles for Service Accounts)"
  value       = aws_eks_cluster.tracelink.identity[0].oidc[0].issuer
}

output "kubeconfig_update_command" {
  description = "Run this to update your local kubeconfig and start using kubectl"
  value       = "aws eks update-kubeconfig --region ${var.aws_region} --name ${aws_eks_cluster.tracelink.name}"
}
