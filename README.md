# Build
mvn clean package -DskipTests

# Run locally (set AWS creds or use local DynamoDB)
export AWS_REGION=us-east-1
export AWS_ACCESS_KEY_ID=...
export AWS_SECRET_ACCESS_KEY=...
# also set table env names if you used different
java -jar target/fondos-0.0.1-SNAPSHOT.jar

# Docker build
docker build -t fondosapi:latest .

# Run Docker (example)
docker run -e AWS_REGION=us-east-1 -p 8080:8080 fondosapi:latest
