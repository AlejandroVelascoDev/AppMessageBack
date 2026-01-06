src
└── main
└── java
└── com
└── mobile
└── backend
├── BackendApplication.java // <-- Tu clase principal con @SpringBootApplication
|
├── config // Configuraciones generales de la app
│ ├── CorsConfig.java
│ ├── PasswordConfig.java
│ ├── SecurityConfig.java
│ └── WebSocketConfig.java
|
├── controller // Controladores REST para HTTP
│ ├── AuthController.java // /api/auth/{login,register}
│ ├── ChatController.java // /api/chats, /api/chats/{id}
│ ├── HealthController.java // /api/health
│ ├── MessageController.java // /api/chats/{chatId}/messages
│ └── UserController.java // /api/users/{id}, /api/users/me, /api/users/search
|
├── dto // Data Transfer Objects para requests y responses
│ ├── auth // DTOs específicos de autenticación
│ │ ├── LoginRequest.java
│ │ ├── RegisterRequest.java
│ │ └── AuthResponse.java
│ ├── chat // DTOs de chats
│ │ ├── ChatCreationRequest.java
│ │ └── ChatResponse.java
│ ├── message // DTOs de mensajes
│ │ ├── MessageRequest.java
│ │ └── MessageResponse.java
│ └── user // DTOs de usuario
│ ├── UserProfileUpdateRequest.java
│ └── UserResponse.java
|
├── entity // Modelos de base de datos
│ ├── User.java
│ ├── Chat.java
│ └── Message.java
|
├── repository // Interfaces para acceso a datos (JPA Repositories)
│ ├── UserRepository.java
│ ├── ChatRepository.java
│ └── MessageRepository.java
|
├── security // Lógica de seguridad (JWT, filtros, etc.)
│ ├── JwtService.java
│ ├── JwtAuthenticationFilter.java // Filtro para validar JWT en cada request
│ └── CustomUserDetailsService.java // Para cargar detalles de usuario para Spring Security
|
├── service // Lógica de negocio principal
│ ├── AuthService.java // Lógica de registro/login
│ ├── UserService.java // Lógica de negocio de usuarios
│ ├── ChatService.java // Lógica de negocio de chats
│ └── MessageService.java // Lógica de negocio de mensajes
|
└── websocket // Lógica específica de WebSockets (Handlers, interceptores)
├── WebSocketEventListener.java // Para eventos de conexión/desconexión
└── ChatWebSocketHandler.java // Si usas WebSockets sin STOMP directamente
