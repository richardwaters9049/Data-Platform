# Data Platform ETL Service

## Overview

Backend service to ingest, validate, and store CSV data using Spring Boot and PostgreSQL.

## Tech Stack

- Java 17
- Spring Boot
- PostgreSQL (Docker)
- JPA/Hibernate
- Maven

## Architecture

Client -> REST API -> Service Layer -> JPA -> PostgreSQL

## Features (Planned)

- CSV Upload Endpoint
- Data Validation
- ETL Pipeline
- Redis Caching
- Vue Frontend

## Run Instructions

1. Start PostgreSQL via Docker (port 55432)
2. Run: mvn spring-boot:run
3. Access: <http://localhost:8080>
