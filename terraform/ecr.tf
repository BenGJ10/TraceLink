# ECR Repository
resource "aws_ecr_repository" "backend" {
  name                 = var.ecr_backend_repo_name
  image_tag_mutability = "MUTABLE" # Allows overwriting 'latest' tag

  # Scan images for vulnerabilities on push
  image_scanning_configuration {
    scan_on_push = true
  }

  # Encrypt images at rest using AWS-managed keys
  encryption_configuration {
    encryption_type = "AES256"
  }

  tags = {
    Name = var.ecr_backend_repo_name
  }
}

# ECR Lifecycle Policy
# Automatically prune old untagged images to keep costs low
resource "aws_ecr_lifecycle_policy" "backend" {
  repository = aws_ecr_repository.backend.name

  policy = jsonencode({
    rules = [
      {
        # Rule 1: Keep the most recent N tagged images
        rulePriority = 1
        description  = "Keep last ${var.ecr_image_retention_count} tagged images"
        selection = {
          tagStatus   = "tagged"
          tagPrefixList = ["v", "release", "latest"]
          countType   = "imageCountMoreThan"
          countNumber = var.ecr_image_retention_count
        }
        action = {
          type = "expire"
        }
      },
      {
        # Rule 2: Always remove untagged/dangling images after 1 day
        rulePriority = 2
        description  = "Remove untagged images after 1 day"
        selection = {
          tagStatus   = "untagged"
          countType   = "sinceImagePushed"
          countUnit   = "days"
          countNumber = 1
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

# ECR Repository Policy
# Allows the current AWS account to pull/push images
data "aws_caller_identity" "current" {}

resource "aws_ecr_repository_policy" "backend" {
  repository = aws_ecr_repository.backend.name

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "AllowAccountAccess"
        Effect = "Allow"
        Principal = {
          AWS = "arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"
        }
        Action = [
          "ecr:GetDownloadUrlForLayer",
          "ecr:BatchGetImage",
          "ecr:BatchCheckLayerAvailability",
          "ecr:PutImage",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:CompleteLayerUpload",
          "ecr:DescribeRepositories",
          "ecr:ListImages",
          "ecr:DescribeImages"
        ]
      }
    ]
  })
}
