FROM oven/bun:1.3.1-alpine

WORKDIR /app

COPY package.json bun.lock ./
RUN bun install --frozen-lockfile

COPY index.html vite.config.js ./
COPY src ./src

EXPOSE 5173

CMD ["bun", "run", "dev"]
