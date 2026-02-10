# Usage Examples

This document provides examples of how to configure and use the User Attribute Router mapper.

## Basic Setup

### 1. User Attributes

First, ensure your Keycloak users have the necessary attributes set:

- `id_value`: The actual identifier value (e.g., "e123", "s456")
- `id_type`: The type of identifier (e.g., "empl", "student", "contractor")

You can set these attributes in the Keycloak Admin Console under Users → [Select User] → Attributes.

Example user attributes:
```
id_value: e123
id_type: empl
```

### 2. Mapper Configuration

In your OIDC client configuration, add a new Protocol Mapper:

1. Navigate to Clients → [Your Client] → Mappers
2. Click "Create"
3. Select "User Attribute Router" from the Mapper Type dropdown

### 3. Mapper Settings

Configure the mapper with the following settings:

- **Name**: Employee ID Router (or any descriptive name)
- **Value Attribute**: `id_value` (the attribute containing the value)
- **Type Attribute**: `id_type` (the attribute containing the type)
- **Route Match Value**: `empl` (the type value that triggers this mapper)
- **Token Claim Name**: `emp_id` (the claim name in the token)
- **Claim JSON Type**: String
- **Add to ID token**: ON
- **Add to access token**: ON
- **Add to userinfo**: ON

## Multiple ID Type Example

If you need to support multiple ID types, create multiple mappers:

### Mapper 1: Employee ID
- Route Match Value: `empl`
- Token Claim Name: `emp_id`

### Mapper 2: Student ID
- Route Match Value: `student`
- Token Claim Name: `student_id`

### Mapper 3: Contractor ID
- Route Match Value: `contractor`
- Token Claim Name: `contractor_id`

## Result

Given a user with:
```
id_value: e123
id_type: empl
```

With the Employee ID mapper configured, the resulting token will include:
```json
{
  "emp_id": "e123",
  ...
}
```

If the same user had `id_type: student`, the Student ID mapper would activate instead:
```json
{
  "student_id": "e123",
  ...
}
```

## Advanced Usage

### Using Custom Attribute Names

You can use different attribute names by changing the "Value Attribute" and "Type Attribute" settings:

- **Value Attribute**: `custom_id_value`
- **Type Attribute**: `custom_id_type`

Just ensure your users have these custom attributes set.

### Multiple Values

If you have users with multiple ID types, you can set multiple mappers and let the routing handle which claims to include based on the current `id_type` value.

## Troubleshooting

### Claim Not Appearing

If your claim doesn't appear in the token:

1. Check that the user has both `id_value` and `id_type` attributes set
2. Verify that `id_type` matches the "Route Match Value" exactly (case-sensitive)
3. Ensure the mapper is enabled for the correct token types (ID token, Access token, UserInfo)

### Wrong Value

If the wrong value appears:

1. Check that the "Value Attribute" setting points to the correct user attribute
2. Verify the user's attribute values in the Keycloak Admin Console
