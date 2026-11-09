# Library frontend

Angular application for the books API. The browser sends requests to `/api/books`; nginx forwards `/api/` to Spring Boot in Docker Compose, avoiding cross-origin configuration.

## Run with Docker Compose

1. In the repository root, copy `.env.example` to `.env` and configure the required Oracle credentials. Do not commit `.env`.
2. Run `docker compose up --build` from the repository root. Oracle can take several minutes to initialize.
3. Open <http://localhost:4200>. The existing API is available at <http://localhost:8080/api/books> with the default configuration.

The host frontend port can be changed with `FRONTEND_PORT` in `.env`; the backend host port is configured with `PORT`. The frontend proxy uses the internal Compose address `springboot:8080`, independently of the host port. Do not remove the Oracle volume if you need to preserve its data.

## Architecture

The Angular CLI-generated standalone application is the project archetype. Components manage presentation and forms; `BookService` encapsulates HTTP requests (service/repository pattern); the `Book` and `BookInput` types describe the API contract. The backend retains controller, service and repository layers.

## Development and checks

With Node.js 20.19+ or 22.12+ and npm installed, run `npm ci`, `npm run build`, and `npm test -- --watch=false` inside `frontend`. Karma tests require a supported Chrome installation. For local development without Compose's nginx proxy, run `npm start -- --proxy-config proxy.conf.json` and start the API on localhost port 8080; change `proxy.conf.json` if the API runs elsewhere.
