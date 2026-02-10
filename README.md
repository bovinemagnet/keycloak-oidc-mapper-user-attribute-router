# keycloak-oidc-mapper-user-attribute-router

A Keycloak 17 OIDC protocol mapper that routes user attributes to different token claims based on a type attribute.

## Overview

This mapper allows you to store multiple ID types in Keycloak user attributes and route them to different token claims based on the ID type. This is useful when you have different types of identifiers (employee ID, student ID, contractor ID, etc.) and need to map them to different claims in the OIDC token.

## Use Case Example

Say you have:
- User attribute `id_value` = "e123"
- User attribute `id_type` = "empl"
- A mapper configured with `route_match` = "empl" and `token_claim` = "emp_id"

The mapper will:
1. Read the `id_type` attribute
2. Check if it matches the configured `route_match` value ("empl")
3. If it matches, add the `id_value` ("e123") to the token as the claim "emp_id"

This allows Keycloak to store what was sent in (both id_value and id_type), but selectively send out specific values via configured attributes like "staff_id", "empl_id", etc.

## Configuration

The mapper has three main configuration properties:

1. **Value Attribute** (default: "id_value") - The user attribute containing the value to route
2. **Type Attribute** (default: "id_type") - The user attribute containing the type/routing key
3. **Route Match Value** - The value of the type attribute that triggers this routing (e.g., "empl")

Plus the standard OIDC claim configuration:
- **Token Claim Name** - The name of the claim in the token (e.g., "emp_id")
- **Claim JSON Type** - The type of the claim value
- **Add to ID token** - Whether to include in ID token
- **Add to access token** - Whether to include in access token
- **Add to userinfo** - Whether to include in userinfo endpoint

## Build

```bash
./gradlew build
```

## Compile

```bash
./gradlew compileJava
```

## Test

```bash
./gradlew test
```

## Create JAR

```bash
./gradlew jar
```

The JAR file will be created in `build/libs/`.

## Install

1. Copy the JAR file to the Keycloak server's `providers` directory (Keycloak 17+) or `standalone/deployments` directory (older versions)
2. Restart Keycloak
3. The mapper will be available as "User Attribute Router" in the OIDC client mapper configuration

## Multiple Mappers

You can create multiple mapper instances for different ID types:

- Mapper 1: route_match="empl" → claim="emp_id"
- Mapper 2: route_match="student" → claim="student_id"
- Mapper 3: route_match="contractor" → claim="contractor_id"

Each mapper will only add its claim when the `id_type` matches its `route_match` value.
