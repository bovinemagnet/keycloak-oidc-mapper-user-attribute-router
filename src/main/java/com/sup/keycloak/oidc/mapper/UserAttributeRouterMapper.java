package com.sup.keycloak.oidc.mapper;

import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.ProtocolMapperUtils;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.logging.Logger;
import org.keycloak.protocol.oidc.mappers.*;

/**
 * UserAttributeRouterMapper - Routes user attributes to different token claims based on a type attribute.
 * 
 * This mapper reads two user attributes:
 * 1. id_value - the actual value to be routed
 * 2. id_type - the type that determines which claim to use
 * 
 * Example: If id_value="e123" and id_type="empl", and the mapper is configured with
 * route_match="empl", then the value "e123" will be added to the configured token claim.
 */
public class UserAttributeRouterMapper extends AbstractOIDCProtocolMapper
		implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

	private static final Logger LOGGER = Logger.getLogger(UserAttributeRouterMapper.class);

	public static final String PROVIDER_ID = "oidc-usermodel-router-attribute-mapper";
	public static final String DISPLAY_TYPE = "User Attribute Router";
	public static final String HELP_TEXT = "Routes a user attribute to a token claim based on a type attribute";

	// Value Attribute
	public static final String VALUE_ATTRIBUTE = "value-attribute";
	public static final String VALUE_ATTRIBUTE_LABEL = "Value Attribute";
	public static final String VALUE_ATTRIBUTE_HELP_TEXT = "The user attribute that contains the value to route (e.g., id_value)";

	// Type Attribute
	public static final String TYPE_ATTRIBUTE = "type-attribute";
	public static final String TYPE_ATTRIBUTE_LABEL = "Type Attribute";
	public static final String TYPE_ATTRIBUTE_HELP_TEXT = "The user attribute that contains the type/routing key (e.g., id_type)";

	// Route Match
	public static final String ROUTE_MATCH = "route-match";
	public static final String ROUTE_MATCH_LABEL = "Route Match Value";
	public static final String ROUTE_MATCH_HELP_TEXT = "The value of the type attribute that triggers this routing (e.g., 'empl' to match id_type='empl')";

	private static final List<ProviderConfigProperty> configProperties = new ArrayList<ProviderConfigProperty>();
	static {
		ProviderConfigProperty property;
		
		// Value Attribute
		property = new ProviderConfigProperty();
		property.setName(VALUE_ATTRIBUTE);
		property.setLabel(VALUE_ATTRIBUTE_LABEL);
		property.setHelpText(VALUE_ATTRIBUTE_HELP_TEXT);
		property.setType(ProviderConfigProperty.STRING_TYPE);
		property.setDefaultValue("id_value");
		configProperties.add(property);

		// Type Attribute
		property = new ProviderConfigProperty();
		property.setName(TYPE_ATTRIBUTE);
		property.setLabel(TYPE_ATTRIBUTE_LABEL);
		property.setHelpText(TYPE_ATTRIBUTE_HELP_TEXT);
		property.setType(ProviderConfigProperty.STRING_TYPE);
		property.setDefaultValue("id_type");
		configProperties.add(property);

		// Route Match
		property = new ProviderConfigProperty();
		property.setName(ROUTE_MATCH);
		property.setLabel(ROUTE_MATCH_LABEL);
		property.setHelpText(ROUTE_MATCH_HELP_TEXT);
		property.setType(ProviderConfigProperty.STRING_TYPE);
		configProperties.add(property);

		// Add standard OIDC attribute mapper configuration
		OIDCAttributeMapperHelper.addAttributeConfig(configProperties, UserAttributeMapper.class);
	}

	public List<ProviderConfigProperty> getConfigProperties() {
		return configProperties;
	}

	@Override
	public String getId() {
		return PROVIDER_ID;
	}

	@Override
	public String getDisplayType() {
		return DISPLAY_TYPE;
	}

	@Override
	public String getDisplayCategory() {
		return TOKEN_MAPPER_CATEGORY;
	}

	@Override
	public String getHelpText() {
		return HELP_TEXT;
	}

	@Override
	protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession) {
		UserModel user = userSession.getUser();
		
		// Get configuration
		final String valueAttributeName = mappingModel.getConfig().get(VALUE_ATTRIBUTE);
		final String typeAttributeName = mappingModel.getConfig().get(TYPE_ATTRIBUTE);
		final String routeMatch = mappingModel.getConfig().get(ROUTE_MATCH);
		
		// Validate configuration
		if (valueAttributeName == null || valueAttributeName.isEmpty()) {
			LOGGER.warn("[UserAttributeRouterMapper] Value attribute name is not configured");
			return;
		}
		
		if (typeAttributeName == null || typeAttributeName.isEmpty()) {
			LOGGER.warn("[UserAttributeRouterMapper] Type attribute name is not configured");
			return;
		}
		
		if (routeMatch == null || routeMatch.isEmpty()) {
			LOGGER.warn("[UserAttributeRouterMapper] Route match value is not configured");
			return;
		}
		
		// Get the type attribute value
		final boolean aggregateAttrs = Boolean.valueOf(mappingModel.getConfig().get(ProtocolMapperUtils.AGGREGATE_ATTRS));
		Collection<String> typeAttributeValue = KeycloakModelUtils.resolveAttribute(user, typeAttributeName, aggregateAttrs);
		
		if (typeAttributeValue == null || typeAttributeValue.isEmpty()) {
			LOGGER.debug("[UserAttributeRouterMapper] Type attribute '" + typeAttributeName + "' not found or empty for user");
			return;
		}
		
		// Check if the type matches our route
		String typeValue = typeAttributeValue.iterator().next();
		if (!routeMatch.equals(typeValue)) {
			LOGGER.debug("[UserAttributeRouterMapper] Type attribute value '" + typeValue + "' does not match route match '" + routeMatch + "'");
			return;
		}
		
		// Get the value attribute
		Collection<String> valueAttributeValue = KeycloakModelUtils.resolveAttribute(user, valueAttributeName, aggregateAttrs);
		
		if (valueAttributeValue == null || valueAttributeValue.isEmpty()) {
			LOGGER.debug("[UserAttributeRouterMapper] Value attribute '" + valueAttributeName + "' not found or empty for user");
			return;
		}
		
		// Map the claim
		LOGGER.debug("[UserAttributeRouterMapper] Routing value from '" + valueAttributeName + "' to claim based on type '" + typeValue + "'");
		OIDCAttributeMapperHelper.mapClaim(token, mappingModel, valueAttributeValue);
	}

	public static ProtocolMapperModel createClaimMapper(String name,
			String valueAttribute,
			String typeAttribute,
			String routeMatch,
			String tokenClaimName, 
			String claimType,
			boolean accessToken, 
			boolean idToken) {
		return createClaimMapper(name, valueAttribute, typeAttribute, routeMatch, 
				tokenClaimName, claimType, accessToken, idToken, false);
	}

	public static ProtocolMapperModel createClaimMapper(String name,
			String valueAttribute,
			String typeAttribute,
			String routeMatch,
			String tokenClaimName, 
			String claimType,
			boolean accessToken, 
			boolean idToken,
			boolean aggregateAttrs) {
		ProtocolMapperModel mapper = new ProtocolMapperModel();
		mapper.setName(name);
		mapper.setProtocolMapper(PROVIDER_ID);
		mapper.setProtocol("openid-connect");
		
		java.util.Map<String, String> config = new java.util.HashMap<>();
		config.put(VALUE_ATTRIBUTE, valueAttribute);
		config.put(TYPE_ATTRIBUTE, typeAttribute);
		config.put(ROUTE_MATCH, routeMatch);
		config.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, tokenClaimName);
		config.put(OIDCAttributeMapperHelper.JSON_TYPE, claimType);
		config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, Boolean.toString(accessToken));
		config.put(OIDCAttributeMapperHelper.INCLUDE_IN_ID_TOKEN, Boolean.toString(idToken));
		
		if (aggregateAttrs) {
			config.put(ProtocolMapperUtils.AGGREGATE_ATTRS, "true");
		}
		
		mapper.setConfig(config);

		return mapper;
	}
}
