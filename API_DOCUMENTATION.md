# Real-Time Messaging Application Backend API Documentation

This document describes the RESTful endpoints for the backend of the real-time messaging application, built with Spring Boot.

---

## 1. AuthController

**Base URL:** `/api/auth`

Handles user authentication, including registration and login.

### 1.1. `POST /api/auth/register`

- **Description:** Registers a new user with a unique email and password.
- **Method:** `POST`
- **URL:** `/api/auth/register`
- **Request Body (`RegisterRequest`):**
  ```json
  {
    "username": "string", // User's chosen username
    "email": "string", // User's email, must be unique
    "password": "string" // User's password
  }
  ```
- **Response Body (`AuthResponse` - 200 OK):**
  ```json
  {
    "token": "string", // JWT token for authentication
    "user": {
      "id": "long",
      "username": "string",
      "email": "string"
    }
  }
  ```
- **Error Responses:**
  - `400 Bad Request`: If email is already taken or request body is invalid.

### 1.2. `POST /api/auth/login`

- **Description:** Authenticates an existing user with their email and password, returning a JWT token.
- **Method:** `POST`
- **URL:** `/api/auth/login`
- **Request Body (`LoginRequest`):**
  ```json
  {
    "email": "string", // User's registered email
    "password": "string" // User's password
  }
  ```
- **Response Body (`AuthResponse` - 200 OK):**
  ```json
  {
    "token": "string", // JWT token for authentication
    "user": {
      "id": "long",
      "username": "string",
      "email": "string"
    }
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If email or password is incorrect.
  - `400 Bad Request`: If request body is invalid.

---

## 2. UserController

**Base URL:** `/api/users`

Manages user profiles and search functionalities. All endpoints require authentication (JWT).

### 2.1. `GET /api/users/me`

- **Description:** Retrieves the profile information of the currently authenticated user.
- **Method:** `GET`
- **URL:** `/api/users/me`
- **Authentication:** Required (JWT in `Authorization` header)
- **Response Body (`UserResponse` - 200 OK):**
  ```json
  {
    "id": "long",
    "username": "string",
    "email": "string",
    "profilePictureUrl": "string" // Optional
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token is provided.
  - `404 Not Found`: If the authenticated user is not found (should not happen with valid token).

### 2.2. `GET /api/users/{id}`

- **Description:** Retrieves the profile information for a specific user by their ID.
- **Method:** `GET`
- **URL:** `/api/users/{id}`
- **Authentication:** Required (JWT in `Authorization` header)
- **Path Variables:**
  - `id` (Long): The ID of the user to retrieve.
- **Response Body (`UserResponse` - 200 OK):**
  ```json
  {
    "id": "long",
    "username": "string",
    "email": "string",
    "profilePictureUrl": "string" // Optional
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token is provided.
  - `404 Not Found`: If no user with the given ID exists.

### 2.3. `GET /api/users/search`

- **Description:** Searches for users by username or email. At least one query parameter must be provided.
- **Method:** `GET`
- **URL:** `/api/users/search`
- **Authentication:** Required (JWT in `Authorization` header)
- **Query Parameters:**
  - `username` (String, optional): Part of the username to search for.
  - `email` (String, optional): Part of the email to search for.
- **Response Body (`List<UserResponse>` - 200 OK):**
  ```json
  [
    {
      "id": "long",
      "username": "string",
      "email": "string",
      "profilePictureUrl": "string"
    }
  ]
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token is provided.
  - `400 Bad Request`: If neither `username` nor `email` parameters are provided.

### 2.4. `PUT /api/users/me`

- **Description:** Updates the profile information of the currently authenticated user.
- **Method:** `PUT`
- **URL:** `/api/users/me`
- **Authentication:** Required (JWT in `Authorization` header)
- **Request Body (`UpdateUserRequest` - nested record):**
  ```json
  {
    "username": "string" // New username for the authenticated user
    // Add other fields like "profilePictureUrl" here if needed
  }
  ```
- **Response Body (`UserResponse` - 200 OK):**
  ```json
  {
    "id": "long",
    "username": "string",
    "email": "string",
    "profilePictureUrl": "string" // Updated value
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token is provided.
  - `400 Bad Request`: If the request body is invalid or username is already taken.
  - `404 Not Found`: If the authenticated user is not found (should not happen with valid token).

### 2.5. `DELETE /api/users/me`

- **Description:** Deletes the account of the currently authenticated user. This action is irreversible.
- **Method:** `DELETE`
- **URL:** `/api/users/me`
- **Authentication:** Required (JWT in `Authorization` header)
- **Response Body (204 No Content):**
  - No content is returned upon successful deletion.
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token is provided.
  - `404 Not Found`: If the authenticated user is not found (should not happen with valid token).

---

## 3. ChatController

**Base URL:** `/api/chats`

Manages chat creation, listing, details, message history, and read statuses. All endpoints require authentication (JWT).

### 3.1. `POST /api/chats`

- **Description:** Creates a new chat (either single or group chat).
- **Method:** `POST`
- **URL:** `/api/chats`
- **Authentication:** Required (JWT in `Authorization` header)
- **Request Body (`ChatCreationRequest`):**
  ```json
  {
    "participantIds": [long, long, ...], // List of user IDs to include in the chat
    "chatName": "string",               // Optional, name for group chats
    "chatType": "SINGLE" | "GROUP"      // Type of chat
  }
  ```
- **Response Body (`ChatResponse` - 201 Created):**
  ```json
  {
    "id": "long",
    "name": "string",
    "type": "SINGLE" | "GROUP",
    "participants": [
      {"id": "long", "username": "string", "profilePictureUrl": "string"},
      ...
    ],
    "lastMessage": { /* ChatMessageResponse or null */ },
    "createdAt": "timestamp"
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token.
  - `400 Bad Request`: If `participantIds` is empty, contains invalid IDs, or `chatType` is invalid.

### 3.2. `GET /api/chats`

- **Description:** Lists all chats associated with the authenticated user, including the last message for each chat.
- **Method:** `GET`
- **URL:** `/api/chats`
- **Authentication:** Required (JWT in `Authorization` header)
- **Response Body (`List<ChatResponse>` - 200 OK):**
  ```json
  [
    {
      "id": "long",
      "name": "string",
      "type": "SINGLE" | "GROUP",
      "participants": [ ... ],
      "lastMessage": { /* ChatMessageResponse or null */ },
      "createdAt": "timestamp"
    },
    ...
  ]
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token.
  - `400 Bad Request`: If there's an issue with fetching chats for the user.

### 3.3. `GET /api/chats/{chatId}`

- **Description:** Retrieves detailed information about a specific chat.
- **Method:** `GET`
- **URL:** `/api/chats/{chatId}`
- **Authentication:** Required (JWT in `Authorization` header)
- **Path Variables:**
  - `chatId` (Long): The ID of the chat to retrieve.
- **Response Body (`ChatResponse` - 200 OK):**
  ```json
  {
    "id": "long",
    "name": "string",
    "type": "SINGLE" | "GROUP",
    "participants": [
      {"id": "long", "username": "string", "profilePictureUrl": "string"},
      ...
    ],
    "lastMessage": { /* ChatMessageResponse or null */ },
    "createdAt": "timestamp"
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token.
  - `403 Forbidden`: If the authenticated user is not a participant in the chat.
  - `404 Not Found`: If no chat with the given ID exists.

### 3.4. `GET /api/chats/{chatId}/messages`

- **Description:** Retrieves a paginated list of messages for a specific chat.
- **Method:** `GET`
- **URL:** `/api/chats/{chatId}/messages`
- **Authentication:** Required (JWT in `Authorization` header)
- **Path Variables:**
  - `chatId` (Long): The ID of the chat.
- **Query Parameters:**
  - `page` (int, optional, default: 0): The page number to retrieve.
  - `size` (int, optional, default: 50): The number of messages per page.
- **Response Body (`MessageHistoryResponse` - 200 OK):**
  ```json
  {
    "chatId": "long",
    "messages": [
      {
        "id": "long",
        "chatId": "long",
        "senderId": "long",
        "senderUsername": "string",
        "content": "string",
        "timestamp": "timestamp"
      },
      ...
    ],
    "currentPage": "integer",
    "pageSize": "integer",
    "totalPages": "integer",
    "totalMessages": "long",
    "hasMore": "boolean"
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token.
  - `403 Forbidden`: If the authenticated user is not a participant in the chat.
  - `404 Not Found`: If no chat with the given ID exists.

### 3.5. `PUT /api/chats/{chatId}/read`

- **Description:** Marks all unread messages in a specific chat for the authenticated user as read.
- **Method:** `PUT`
- **URL:** `/api/chats/{chatId}/read`
- **Authentication:** Required (JWT in `Authorization` header)
- **Path Variables:**
  - `chatId` (Long): The ID of the chat.
- **Response Body (`ReadStatusResponse` - 200 OK):**
  ```json
  {
    "chatId": "long",
    "messagesMarkedAsRead": "integer" // Number of messages newly marked as read
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token.
  - `400 Bad Request`: If there's an issue with the request (e.g., chat not found or user not in chat).

### 3.6. `GET /api/chats/{chatId}/unread-count`

- **Description:** Retrieves the count of unread messages for the authenticated user within a specific chat.
- **Method:** `GET`
- **URL:** `/api/chats/{chatId}/unread-count`
- **Authentication:** Required (JWT in `Authorization` header)
- **Path Variables:**
  - `chatId` (Long): The ID of the chat.
- **Response Body (`UnreadCountResponse` - 200 OK):**
  ```json
  {
    "chatId": "long",
    "unreadCount": "long" // Count of unread messages for the user in this chat
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token.
  - `403 Forbidden`: If the authenticated user is not a participant in the chat.
  - `404 Not Found`: If no chat with the given ID exists.

---

## 4. MessageController

**Base URL:** `/api/chats`

Provides HTTP endpoints for managing individual messages within a chat. These serve as a fallback for WebSocket and also for operations like message editing/deletion. All endpoints require authentication (JWT).

### 4.1. `POST /api/chats/{chatId}/messages`

- **Description:** Sends a new message to a specific chat via HTTP. Primarily intended as a fallback if WebSocket is unavailable, or for specific use cases.
- **Method:** `POST`
- **URL:** `/api/chats/{chatId}/messages`
- **Authentication:** Required (JWT in `Authorization` header)
- **Path Variables:**
  - `chatId` (Long): The ID of the chat where the message will be sent.
- **Request Body (`ChatMessageRequest`):**
  ```json
  {
    "chatId": "long", // Must match the path variable {chatId}
    "content": "string" // The text content of the message
  }
  ```
- **Response Body (`ChatMessageResponse` - 201 Created):**
  ```json
  {
    "id": "long",
    "chatId": "long",
    "senderId": "long",
    "senderUsername": "string",
    "content": "string",
    "timestamp": "timestamp"
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token.
  - `403 Forbidden`: If the authenticated user is not a participant in the chat.
  - `400 Bad Request`: If `chatId` in request body does not match path variable, or if content is empty.

### 4.2. `GET /api/chats/{chatId}/messages/{messageId}`

- **Description:** Retrieves details of a specific message within a given chat.
- **Method:** `GET`
- **URL:** `/api/chats/{chatId}/messages/{messageId}`
- **Authentication:** Required (JWT in `Authorization` header)
- **Path Variables:**
  - `chatId` (Long): The ID of the chat.
  - `messageId` (Long): The ID of the message.
- **Response Body (`ChatMessageResponse` - 200 OK):**
  ```json
  {
    "id": "long",
    "chatId": "long",
    "senderId": "long",
    "senderUsername": "string",
    "content": "string",
    "timestamp": "timestamp"
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token.
  - `403 Forbidden`: If the authenticated user is not a participant in the chat.
  - `404 Not Found`: If the chat or message does not exist, or if the message does not belong to the chat.

### 4.3. `DELETE /api/chats/{chatId}/messages/{messageId}`

- **Description:** Deletes a specific message from a chat. The message can typically only be deleted by its sender.
- **Method:** `DELETE`
- **URL:** `/api/chats/{chatId}/messages/{messageId}`
- **Authentication:** Required (JWT in `Authorization` header)
- **Path Variables:**
  - `chatId` (Long): The ID of the chat.
  - `messageId` (Long): The ID of the message to delete.
- **Response Body (`DeleteMessageResponse` - 200 OK):**
  ```json
  {
    "messageId": "long",
    "chatId": "long",
    "deleted": "boolean" // True if deleted successfully, false otherwise
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token.
  - `403 Forbidden`: If the authenticated user is not a participant in the chat, or is not the sender of the message.
  - `404 Not Found`: If the chat or message does not exist.

### 4.4. `PATCH /api/chats/{chatId}/messages/{messageId}`

- **Description:** Updates an existing message (e.g., to edit its content). The message can typically only be updated by its sender.
- **Method:** `PATCH`
- **URL:** `/api/chats/{chatId}/messages/{messageId}`
- **Authentication:** Required (JWT in `Authorization` header)
- **Path Variables:**
  - `chatId` (Long): The ID of the chat.
  - `messageId` (Long): The ID of the message to update.
- **Request Body (`UpdateMessageRequest` - nested static class):**
  ```json
  {
    "content": "string" // The new text content for the message
  }
  ```
- **Response Body (`ChatMessageResponse` - 200 OK):**
  ```json
  {
    "id": "long",
    "chatId": "long",
    "senderId": "long",
    "senderUsername": "string",
    "content": "string", // Updated content
    "timestamp": "timestamp" // Potentially updated timestamp for last edit
  }
  ```
- **Error Responses:**
  - `401 Unauthorized`: If no valid JWT token.
  - `403 Forbidden`: If the authenticated user is not a participant in the chat, or is not the sender of the message.
  - `404 Not Found`: If the chat or message does not exist.
  - `400 Bad Request`: If the new `content` is empty.

---

## 5. HealthController

**Base URL:** `/health`

Provides a simple endpoint to check the application's health status.

### 5.1. `GET /health`

- **Description:** Checks the health status of the backend application.
- **Method:** `GET`
- **URL:** `/health`
- **Authentication:** None required.
- **Response Body (200 OK):**
  ```
  OK
  ```
- **Error Responses:**
  - Returns an error if the application is not running or encounters an internal server error.
