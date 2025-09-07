output "instance_id" {
  value = aws_instance.extra_ec2.id
}

output "instance_public_ip" {
  value = aws_instance.extra_ec2.public_ip
}

output "public_ip" {
  description = "Public IP of the extra EC2 instance"
  value       = aws_instance.extra_ec2.public_ip
}

output "private_ip" {
  description = "Private IP of the extra EC2 instance"
  value       = aws_instance.extra_ec2.private_ip
}
