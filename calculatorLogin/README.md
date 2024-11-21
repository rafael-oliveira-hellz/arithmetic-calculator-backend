# Calculator login - Serverless Microservice

## **Overview**

The **Calculator Login** microservice is a serverless application designed exclusively for **user login**. It operates on AWS Lambda, ensuring scalability, cost-efficiency, and seamless integration with AWS services. The microservice includes core functionaly such as log out users from Cognito.

---

## **Project Features**

- **Architecture**: The project follows a combination of the Domain-Driven Design (DDD) and the Layered Architecture approaches.
- **User Login**: Implements APIs for user login.
- **Serverless Framework**: Leverages AWS Lambda for event-driven architecture.
- **Global Exception Handling**: Centralized exception handling to manage and return meaningful error responses.

---

## **Project Structure**

```plaintext
src/main/java/org/exercise
├── config
│   ├── CognitoConfig.java          // AWS Cognito configuration for user authentication.
│   ├── CorsConfig.java             // CORS configuration for HTTP requests.
│
├── core
│   ├── dtos                        // Data Transfer Objects (DTOs) for request/response payloads.
│   ├── entities                    // JPA entities (e.g., User, Balance).
│   ├── exceptions                  // Custom exception handling classes.
│   ├── interfaces                  // Service interface definitions.
│   ├── services                    // Business logic implementations.
│
├── http
│   ├── controllers                 // REST controllers for handling HTTP endpoints.
│   ├── handlers                    // Global exception handler for REST APIs.
│
├── infrastructure
│   ├── lambda                      // AWS Lambda handler for integration.
│
├── persistence
│   ├── UserRepository.java         // JPA repository for user data persistence.
│
└── Application.java                // Main Spring Boot entry point.
```

## **Getting Started**

### **Prerequisites**

Ensure the following tools are installed:

- **Java** (11 or higher)
- **Maven**
- **Docker** (for AWS Lambda testing)
- **AWS CLI** (configured with your AWS account)
- **Serverless Framework** (optional for deployment)

### **Setup**

#### 1. Clone the Repository

```bash
git clone <repository-url>
cd calculatorLogin
```

## **2. Set Environment Variables**

The application relies on several environment variables defined in `application.properties`. Set these variables based on your operating system:

### **Linux/MacOS**

```bash
export COGNITO_USER_POOL_ID=your_user_pool_id
export COGNITO_CLIENT_ID=your_client_id
export COGNITO_CLIENT_SECRET=your_client_secret
export REGION=your_aws_region
export DB_HOST=your_database_host
export DB_PORT=your_database_port
export DB_NAME=your_database_name
export DB_USER=your_database_user
export DB_PASSWORD=your_database_password
export INITIAL_AMOUNT=1000
```

### **Windows (cmd)**

```bash
set COGNITO_USER_POOL_ID=your_user_pool_id
set COGNITO_CLIENT_ID=your_client_id
set COGNITO_CLIENT_SECRET=your_client_secret
set REGION=your_aws_region
set DB_HOST=your_database_host
set DB_PORT=your_database_port
set DB_NAME=your_database_name
set DB_USER=your_database_user
set DB_PASSWORD=your_database_password
set INITIAL_AMOUNT=1000
```

### **Windows (PowerShell)**

```bash
$env:COGNITO_USER_POOL_ID="your_user_pool_id"
$env:COGNITO_CLIENT_ID="your_client_id"
$env:COGNITO_CLIENT_SECRET="your_client_secret"
$env:REGION="your_aws_region"
$env:DB_HOST="your_database_host"
$env:DB_PORT="your_database_port"
$env:DB_NAME="your_database_name"
$env:DB_USER="your_database_user"
$env:DB_PASSWORD="your_database_password"
$env:INITIAL_AMOUNT=1000
```

## **3. Running the Application**

Run the application using Maven. Depending on your environment, use one of the following commands:

### **Using Maven Wrapper**

```bash
./mvnw spring-boot:run
```

### **Using Maven**

```bash
mvn spring-boot:run
```

### Alternatively, you can build and run the JAR file:

## Using Maven Wrapper

```bash
./mvnw clean package
java -jar target/calculatorLogout-0.0.1-SNAPSHOT.jar
```

## Using Maven

```bash
mvn clean package
java -jar target/calculatorLogout-0.0.1-SNAPSHOT.jar
```

## **4. Using Docker**

This project supports Docker for local testing and deployment, allowing you to simulate AWS Lambda behavior locally and package the application for deployment.

---

### **Step 4.1: Create the Dockerfile**

Create a `Dockerfile` in the root directory of your project with the following content:

```dockerfile
FROM public.ecr.aws/lambda/java:11

COPY target/calculatorLogout-0.0.1-SNAPSHOT.jar ${LAMBDA_TASK_ROOT}/app.jar

CMD ["org.exercise.infrastructure.lambda.StreamLambdaHandler"]
```

### **Step 4.2: Build the Docker Image**

Run the following command in the root of your project to build the Docker image:

```bash
docker build -t calculator-login .
```

**This command**:

- Uses the Dockerfile to build an image named calculator-login.
- Copies the compiled application JAR into the container.

### **Step 4.3: Run the Docker Container**

Use the following command to run the Docker container locally and expose it for testing:

```bash
docker run -p 9000:8080 calculator-login
```

**Explanation**:

- The `-p 9000:8080` flag maps port `9000` on your local machine to port `8080` in the container.

Once the container is running, it will simulate the AWS Lambda environment.

### **Step 4.4: Test the Docker Container**

Use `curl` or an HTTP client to send a POST request to test the Lambda function:

```bash
curl -X POST "http://localhost:9000/2015-03-31/functions/function/invocations" -d '{}'
```

If configured correctly, the container should process the request and return a response.

### **Step 4.5: Stop the Container**

To stop the running container, press `Ctrl+C` in the terminal where the container is running or find the container ID using:

```bash
docker ps
```

Then stop it with:

```bash
docker stop <container_id>
```

### **Optional: Test Changes**

If you make changes to the code, rebuild the JAR and Docker image:

- Rebuild the JAR
- Rebuild the Docker Image
- Run the Container Again

By following these steps, you can test your Lambda function locally using Docker and prepare it for deployment.

## **Step 5: Testing the Lambda Locally**

Follow these steps to test the Lambda function locally for user login using Docker.

### **Step 5.1: Start the Docker Container**

Ensure the container is running. Use the following command to start it:

```bash
docker run -p 9000:8080 calculator-login
```

**This command**:

Maps port `9000` on your local machine to port `8080` inside the container.
Simulates the AWS Lambda runtime environment.

### **Step 5.2: Create a Test Payload**

Create a JSON file with the test payload containing the user login data, in the project root, for instance.

```plaintext
project-root/
├── events/
│   └── event.json
├── Dockerfile
├── src/
└── target/
```

The `event.json` file will look like this:

```bash
{
    "username": "user",
    "password": "pass"
}
```

### **Step 5.3: Send a Request**

Use `curl` or any HTTP client to send the payload to the Lambda endpoint exposed by the container. The default endpoint for the simulated Lambda is:

```bash
http://localhost:9000/2015-03-31/functions/function/invocations
```

Run the following command to send the request:

```bash
curl -X POST \
    -H "Content-Type: application/json" \
    -d @/path/to/event.json \
    "http://localhost:9000/2015-03-31/functions/function/invocations"
```

### **Step 5.4: Validate the Response**

If everything is configured correctly:

- The container processes the payload.
- The `StreamLambdaHandler` class is invoked.
- The Lambda returns a response, typically in JSON format.

For example, a successful login might return:

```
{
    "id": "23ac8a9a-d0e1-70b8-d06e-8e1fd86269fd",
    "username": "test",
    "email": "test@example.com",
    "active": true,
    "balance": 100,
    "accessToken": "token"
}
```

## Tips for Debugging

### Check Container Logs

If the response is not as expected, check the container logs for more details:

```bash
docker logs <container_id>
```

To find the container ID, use:

```bash
docker ps
```

## **Step 6: Deploying to AWS Lambda**

## **Upload to Amazon ECR**

To deploy the Docker container as an AWS Lambda function, follow these steps:

### **Step 6.1: Authenticate Docker with ECR**

```bash
aws ecr get-login-password --region <region> | docker login --username AWS --password-stdin <account_id>.dkr.ecr.<region>.amazonaws.com
```

### **Step 6.2: Tag the Docker Image**

```bash
docker tag calculator-login:latest <account_id>.dkr.ecr.<region>.amazonaws.com/calculator-login:latest
```

### **Step 6.3: Push the Image to ECR**

```bash
docker push <account_id>.dkr.ecr.<region>.amazonaws.com/calculator-login:latest
```

### **Step 6.4: Configure Lambda to Use the Docker Image**

1. Go to the AWS Lambda Console.
2. Create a new Lambda function.
3. Select "Container Image" as the function source.
4. Specify the ECR image URI.

## **Technologies Used**

- Java 21
- Spring Boot
- AWS Lambda
- AWS RDS PostgreSQL
- AWS API Gateway

### **Contributing**

Feel free to open issues or submit pull requests to contribute to the project.

### **License**

This project is licensed under the MIT License.
