## Test comands:

curl -X GET http://localhost:8080/api/users/me \
 -H "Authorization: Bearer YOUR_JWT_TOKEN"

## Get user by token

curl -X GET http://localhost:8080/api/users/1 \
 -H "Authorization: Bearer YOUR_JWT_TOKEN"

## Login return token

curl -X POST http://localhost:8080/api/auth/login \
 -H "Content-Type: application/json" \
 -d '{
"email": "test@example.com",
"password": "password123"
}'

## Register return token

curl -X POST http://localhost:8080/api/auth/register \
 -H "Content-Type: application/json" \
 -d '{
"email": "test@example.com",
"password": "password123"
}'
