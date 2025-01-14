# Slideshow Service

**Slideshow Service** is a reactive image and slideshow management service built with **Spring Boot**, leveraging **R2DBC** and **PostgreSQL**.

---

## **Features**
- Manage images (add, search, delete).
- Create and delete slideshows.
- Reactive programming support using **Spring WebFlux**.
- Data persistence with **PostgreSQL** using **R2DBC**.
- Transaction management with **R2DBC Transaction Manager**.

---

## **Running the Project Locally**
### Prerequisites:
- **Java 17+**
- **Docker**

---

### **Step 1: Launch PostgreSQL using Docker Compose**
Run the PostgreSQL container using the `docker-compose.yml` file located in the root directory:

```bash
docker-compose up -d
```

This will start a PostgreSQL instance with the necessary configurations for the project.

---

### **Step 2: Build the Project using the Dockerfile**
Before running the application, you need to build the Docker image using the `Dockerfile` located in the root directory:

```bash
docker build -t slideshow-service .
```

---

### **Step 3: Run the Application as a Docker Container**
Once the PostgreSQL container is running and the application is built, you can start the service:

```bash
docker run -p 8080:8080 --network="host" slideshow-service
```

---

### **Step 4: Database Configuration (`application.yml`)**
Ensure the database settings match your local PostgreSQL setup:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/postgres
    username: postgres
    password: postgres
```

---

### **Step 5: Verifying the Setup**
- **Check PostgreSQL:** Run `docker ps` to ensure the container is running.
- **Check the Application:** Once the app is running, test the endpoint:
   ```bash
   curl -X GET http://localhost:8080/api/v1/images/search
   ```

---

## **API Documentation**
### 1. **Add a New Image:**
```http
POST /api/v1/images
Content-Type: application/json

{
    "duration": 10,
    "url": "https://assets-prd.ignimgs.com/2022/11/01/startrekdiscoveryseason4homeentertainmenttrailer-ign-blogroll-1667332155012.jpg"
}
```
**Response:**
```json
{
  "id": 1,
  "url": "https://assets-prd.ignimgs.com/2022/11/01/startrekdiscoveryseason4homeentertainmenttrailer-ign-blogroll-1667332155012.jpg",
  "duration": 10
}
```

---

### 2. **Search Images:**
```http
GET /api/v1/images/search?keyword=discovery&duration=10
```
**Response:**
```json
[
  {
    "id": 1,
    "url": "https://assets-prd.ignimgs.com/2022/11/01/startrekdiscoveryseason4homeentertainmenttrailer-ign-blogroll-1667332155012.jpg",
    "duration": 10
  }
]
```

---

### 3. **Delete an Image:**
Method delete image with specified id and all references to it in existing slideshows
```http
DELETE /api/v1/images/{id}
```
 
---

## **Project Architecture**
- `ImagesController`: REST controller for image management.
- `ImageService`: Business logic for image operations.
- `ImageRepository`: Reactive repository for PostgreSQL.
- `SlideshowService`: Service for slideshow management.
- `ImagesValidationFacade` & `ImagesValidationFacade` : Implements validation logic for input data.

---

## **Testing**
- Unit and integration tests using `WebTestClient` and `Mockito`.
- Run tests using:
```bash
./gradlew test
```

---

## **Database Migrations (Liquibase)**
- Migrations are located in `src/main/resources/db/changelog`.
- Migrations are automatically applied during startup.

---

## **Transactions & Reactive Programming**
- Transaction handling via `R2dbcTransactionManager`.
- Fully reactive stack with `WebFlux`.

---

