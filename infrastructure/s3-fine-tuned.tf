resource "aws_s3_bucket" "fine-tuned-bucket" {
  bucket = "llm-fine-tuned-source-bucket"
}

resource "aws_s3_object" "gpt_4_1_mini_dataset" {
  bucket = aws_s3_bucket.fine-tuned-bucket.id
  key    = "/train/gpt-4-1-mini/dataset.jsonl"
  source = "${path.module}/files/fine-tuned-datasets/gpt-4-1-mini/dataset.jsonl"
}

resource "aws_s3_object" "nova_pro_dataset" {
  bucket = aws_s3_bucket.fine-tuned-bucket.id
  key    = "/train/nova-pro/dataset.jsonl"
  source = "${path.module}/files/fine-tuned-datasets/nova-pro/dataset.jsonl"
}