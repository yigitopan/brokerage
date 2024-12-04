# Brokerage System API

A robust backend API system for managing stock orders and customer transactions in a brokerage firm. The system handles order management, customer operations, and automated order matching.

## How to Run

The system requires a few prerequisites to run properly:

- Java 17 or higher
- PostgreSQL running locally
- Maven

You'll need a running PostgreSQL instance with a database called "brokerage". The application expects the following configuration in application.properties:

```
spring.datasource.url=jdbc:postgresql://localhost:5432/brokeragespring.datasource.username=postgresspring.datasource.password=postgres
```

To create the database, run:

```
CREATE DATABASE brokerage;
```

or, a quick terminal command to create it on docker:

```
docker run --name postgres-brokerage -e POSTGRES_DB=brokerage -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres
```


## API Documentation

Complete API documentation and examples can be found in our Postman collection:
[Postman Collection Link](https://lunar-astronaut-666046.postman.co/workspace/Brokerage-API~a3cce0b0-0ca6-4be5-8d97-ed9733b60335/collection/17012533-e1f6ad49-4e82-4dab-8383-6045686e0170?action=share&creator=17012533)<br>

## Order Matching Algorithm

The system implements an order matching algorithm that optimizes price execution for users. Here's a detailed example of how the matching works:

When User A creates a sell order for 10 DCoin at 50 TRY per coin, the order enters the system as PENDING. Later, when User B places a buy order for 6 DCoin at 52 TRY, the system identifies a matching opportunity.

The matching process executes as follows:

1. System detects User A's selling price (50 TRY) is lower than User B's buying price (52 TRY)
1. Order executes at User A's price of 50 TRY (best price for buyer)
1. User B's order for 6 DCoin is fully matched and marked as MATCHED
1. User A's original order is partially filled:

- Remaining size reduced to 4 DCoin
- Status stays PENDING
- Original price maintains at 50 TRY
- Can be matched with future buy orders

This mechanism ensures:

- Optimal and realistic matching of orders
- Partial order fulfillment

## Future Improvements
### Testing Coverage

Currently, the system includes CustomerIntegrationTest as an example implementation. Additional integration tests should be developed for:
- OrderController
- AssetController
- Authentication flows
- Order matching scenarios

### Documentation Enhancements

While a Postman collection provides API documentation, implementing Swagger would offer additional benefits.

### Alternative Design Considerations

The current system allows asset creation, but an alternative approach worth considering would be:

- Predefined system assets instead of dynamic creation
- Removal of add asset endpoint
- System-level asset management


The system currently implements comprehensive security measures including:

- JWT-based authentication
- Role-based access control (Customer/Admin)
- Endpoint protection
- Customer data isolation
