package com.sup.keycloak.oidc.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.FullNameMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.AccessToken;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class UserAttributeRouterMapperTest {

	static final String CLAIM_NAME = "emp_id";
	static final String TEST_ID_VALUE = "id_value";
	static final String TEST_ID_TYPE = "id_type";
	static final String TEST_TYPE_EMPL = "empl";
	static final String TEST_TYPE_STUDENT = "student";
	static final String TEST_TYPE_CONTRACTOR = "contractor";

	@Test
	public void shouldTokenMapperDisplayCategory() {
		final String tokenMapperDisplayCategory = new FullNameMapper().getDisplayCategory();
		assertThat(new UserAttributeRouterMapper().getDisplayCategory()).isEqualTo(tokenMapperDisplayCategory);
	}

	@Test
	public void shouldHaveDisplayType() {
		assertThat(new UserAttributeRouterMapper().getDisplayType()).isNotBlank();
	}

	@Test
	public void shouldHaveHelpText() {
		assertThat(new UserAttributeRouterMapper().getHelpText()).isNotBlank();
	}

	@Test
	public void shouldHaveId() {
		assertThat(new UserAttributeRouterMapper().getId()).isNotBlank();
	}

	@Test
	@DisplayName("shouldHavePropertiesInConfiguration")
	public void shouldHaveProperties() {
		final List<String> configPropertyNames = new UserAttributeRouterMapper().getConfigProperties().stream()
				.map(ProviderConfigProperty::getName)
				.collect(Collectors.toList());
		assertThat(configPropertyNames).contains(
				OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME,
				OIDCAttributeMapperHelper.INCLUDE_IN_USERINFO,
				UserAttributeRouterMapper.VALUE_ATTRIBUTE,
				UserAttributeRouterMapper.TYPE_ATTRIBUTE,
				UserAttributeRouterMapper.ROUTE_MATCH);
	}

	@Test
	@DisplayName("Should add claim when type matches")
	public void shouldAddClaimWhenTypeMatches() {
		final UserSessionModel session = givenUserSession("e123", TEST_TYPE_EMPL);
		final AccessToken accessToken = transformAccessToken(session, TEST_TYPE_EMPL);

		assertThat(accessToken.getOtherClaims().get(CLAIM_NAME)).isEqualTo("e123");
	}

	@Test
	@DisplayName("Should not add claim when type does not match")
	public void shouldNotAddClaimWhenTypeDoesNotMatch() {
		final UserSessionModel session = givenUserSession("s456", TEST_TYPE_STUDENT);
		final AccessToken accessToken = transformAccessToken(session, TEST_TYPE_EMPL);

		assertThat(accessToken.getOtherClaims().get(CLAIM_NAME)).isNull();
	}

	@Test
	@DisplayName("Should not add claim when type attribute is missing")
	public void shouldNotAddClaimWhenTypeAttributeMissing() {
		final UserSessionModel session = givenUserSessionWithMissingType("e123");
		final AccessToken accessToken = transformAccessToken(session, TEST_TYPE_EMPL);

		assertThat(accessToken.getOtherClaims().get(CLAIM_NAME)).isNull();
	}

	@Test
	@DisplayName("Should not add claim when value attribute is missing")
	public void shouldNotAddClaimWhenValueAttributeMissing() {
		final UserSessionModel session = givenUserSessionWithMissingValue(TEST_TYPE_EMPL);
		final AccessToken accessToken = transformAccessToken(session, TEST_TYPE_EMPL);

		assertThat(accessToken.getOtherClaims().get(CLAIM_NAME)).isNull();
	}

	@ParameterizedTest
	@DisplayName("Should route different ID types correctly")
	@CsvSource({
			"e123,empl,empl,e123",
			"s456,student,student,s456",
			"c789,contractor,contractor,c789",
			"e123,empl,student,",  // type mismatch, should be null
			"s456,student,empl,"   // type mismatch, should be null
	})
	public void shouldRouteDifferentIdTypes(String idValue, String idType, String routeMatch, String expectedClaim) {
		final UserSessionModel session = givenUserSession(idValue, idType);
		final AccessToken accessToken = transformAccessToken(session, routeMatch);

		if (expectedClaim == null || expectedClaim.isEmpty()) {
			assertThat(accessToken.getOtherClaims().get(CLAIM_NAME)).isNull();
		} else {
			assertThat(accessToken.getOtherClaims().get(CLAIM_NAME)).isEqualTo(expectedClaim);
		}
	}

	@Test
	@DisplayName("Should create claim mapper with correct configuration")
	public void shouldCreateClaimMapper() {
		ProtocolMapperModel mapper = UserAttributeRouterMapper.createClaimMapper(
				"Test Router",
				TEST_ID_VALUE,
				TEST_ID_TYPE,
				TEST_TYPE_EMPL,
				CLAIM_NAME,
				"String",
				true,
				true);

		assertThat(mapper.getName()).isEqualTo("Test Router");
		assertThat(mapper.getProtocolMapper()).isEqualTo(UserAttributeRouterMapper.PROVIDER_ID);
		assertThat(mapper.getConfig().get(UserAttributeRouterMapper.VALUE_ATTRIBUTE)).isEqualTo(TEST_ID_VALUE);
		assertThat(mapper.getConfig().get(UserAttributeRouterMapper.TYPE_ATTRIBUTE)).isEqualTo(TEST_ID_TYPE);
		assertThat(mapper.getConfig().get(UserAttributeRouterMapper.ROUTE_MATCH)).isEqualTo(TEST_TYPE_EMPL);
		assertThat(mapper.getConfig().get(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME)).isEqualTo(CLAIM_NAME);
	}

	@SuppressWarnings("PMD.CloseResource")
	private UserSessionModel givenUserSession(String idValue, String idType) {
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		UserModel user = Mockito.mock(UserModel.class);
		org.keycloak.models.KeycloakSession keycloakSession = Mockito.mock(org.keycloak.models.KeycloakSession.class);
		when(userSession.getUser()).thenReturn(user);
		when(user.getAttributeStream(TEST_ID_VALUE)).thenReturn(Arrays.asList(idValue).stream());
		when(user.getAttributeStream(TEST_ID_TYPE)).thenReturn(Arrays.asList(idType).stream());
		return userSession;
	}

	private UserSessionModel givenUserSessionWithMissingType(String idValue) {
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		UserModel user = Mockito.mock(UserModel.class);
		when(userSession.getUser()).thenReturn(user);
		when(user.getAttributeStream(TEST_ID_VALUE)).thenReturn(Arrays.asList(idValue).stream());
		when(user.getAttributeStream(TEST_ID_TYPE)).thenReturn(Arrays.<String>asList().stream());
		return userSession;
	}

	private UserSessionModel givenUserSessionWithMissingValue(String idType) {
		UserSessionModel userSession = Mockito.mock(UserSessionModel.class);
		UserModel user = Mockito.mock(UserModel.class);
		when(userSession.getUser()).thenReturn(user);
		when(user.getAttributeStream(TEST_ID_VALUE)).thenReturn(Arrays.<String>asList().stream());
		when(user.getAttributeStream(TEST_ID_TYPE)).thenReturn(Arrays.asList(idType).stream());
		return userSession;
	}

	private AccessToken transformAccessToken(UserSessionModel userSessionModel, String routeMatch) {
		final ProtocolMapperModel mappingModel = new ProtocolMapperModel();
		mappingModel.setConfig(createConfig(routeMatch));
		org.keycloak.models.KeycloakSession keycloakSession = Mockito.mock(org.keycloak.models.KeycloakSession.class);
		org.keycloak.models.KeycloakContext keycloakContext = Mockito.mock(org.keycloak.models.KeycloakContext.class);
		org.keycloak.models.ClientModel clientModel = Mockito.mock(org.keycloak.models.ClientModel.class);
		
		when(keycloakSession.getContext()).thenReturn(keycloakContext);
		when(keycloakContext.getClient()).thenReturn(clientModel);
		
		return new UserAttributeRouterMapper().transformAccessToken(new AccessToken(), mappingModel, keycloakSession,
				userSessionModel,
				null);
	}

	private Map<String, String> createConfig(String routeMatch) {
		final Map<String, String> result = new HashMap<>();
		result.put(OIDCAttributeMapperHelper.INCLUDE_IN_ACCESS_TOKEN, "true");
		result.put(OIDCAttributeMapperHelper.TOKEN_CLAIM_NAME, CLAIM_NAME);
		result.put(UserAttributeRouterMapper.VALUE_ATTRIBUTE, TEST_ID_VALUE);
		result.put(UserAttributeRouterMapper.TYPE_ATTRIBUTE, TEST_ID_TYPE);
		result.put(UserAttributeRouterMapper.ROUTE_MATCH, routeMatch);
		result.put(OIDCAttributeMapperHelper.JSON_TYPE, "String");

		return result;
	}
}
