# resource "aws_instance" "extra_ec2" {
#   ami           = var.ami_id
#   instance_type = var.instance_type
#   key_name      = var.key_name
#   subnet_id     = var.subnet_id

#   tags = {
#     Name = var.name
#   }
# }


# Get the VPC of the subnet you pass in
data "aws_subnet" "selected" {
  id = var.subnet_id
}

# Simple SG: SSH + HTTP in, all egress out
resource "aws_security_group" "ec2_sg" {
  name        = "${var.name}-sg"
  description = "Allow SSH and HTTP"
  vpc_id      = data.aws_subnet.selected.vpc_id

  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"] # tighten to your IP if you want
  }

  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = { Name = "${var.name}-sg" }
}

resource "aws_instance" "extra_ec2" {
  ami                         = var.ami_id
  instance_type               = var.instance_type
  key_name                    = var.key_name
  subnet_id                   = var.subnet_id
  vpc_security_group_ids      = [aws_security_group.ec2_sg.id]
  associate_public_ip_address = true

  tags = { Name = var.name }
}

# resource "aws_instance" "extra_ec2" {
#   ami                         = var.ami_id
#   instance_type               = var.instance_type
#   key_name                    = var.key_name
#   subnet_id                   = var.subnet_id
#   vpc_security_group_ids      = [aws_security_group.ec2_sg.id]

#   # ✅ This forces AWS to assign a public IP
#   associate_public_ip_address = true

#   tags = {
#     Name = var.name
#   }
# }
