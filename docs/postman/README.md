# Profile Update API Postman Tests

This directory contains Postman collections for exercising the Booking Homestay API flows.

## Using the Collection

1. Import the collection into Postman (`File` → `Import` → `File`).
2. Review the collection-level variables and adjust identifiers or sample payload values to match the data in your environment (user IDs, names, etc.).
3. Set `baseUrl` to the running API instance (defaults to `http://localhost:8080`).
4. Send each request. The bundled tests will assert HTTP status 200 and validate key fields in the JSON response payload.

If your API requires authentication, add the appropriate headers or tokens to each request or configure them at the collection level before running the tests.

## Collections

- `profile-update.postman_collection.json`: Exercises the profile update endpoints for customers, hosts, and admins.
- `customer-host-registration-flow.postman_collection.json`: Walks through the full registration flow for customer and host accounts, including user creation, email confirmation, profile registration, and retrieval checks.
