resource "aws_iam_role" "bedrock_fine_tuned" {
  name = "bedrock-fine-tuned-role"

  assume_role_policy = jsonencode(
    {
      "Version" : "2012-10-17",
      "Statement" : [
        {
          "Effect" : "Allow",
          "Principal" : {
            "Service" : "bedrock.amazonaws.com"
          },
          "Action" : "sts:AssumeRole",
          "Condition" : {
            "StringEquals" : {
              "aws:SourceAccount" : "366403523879"
            }
          }
        }
      ]
    })
}

resource "aws_iam_role_policy_attachment" "allow_all_policy" {
  role       = aws_iam_role.bedrock_fine_tuned.name
  policy_arn = "arn:aws:iam::aws:policy/AdministratorAccess"
}