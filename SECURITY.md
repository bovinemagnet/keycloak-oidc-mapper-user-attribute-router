# Security Summary - Keycloak 15 Legacy Branch

## ⚠️ CRITICAL: Known Vulnerabilities

**This legacy branch is built for Keycloak 15.0.1, which contains 26+ known security vulnerabilities.**

### Overview

This is a **LEGACY COMPATIBILITY BRANCH ONLY**. Keycloak 15.0.1 has numerous critical security vulnerabilities that have been patched in later versions. This branch should only be used by organizations that:

1. Cannot immediately upgrade from Keycloak 15
2. Understand and accept the security risks
3. Have compensating security controls in place
4. Are actively planning migration to current Keycloak versions

### Known Vulnerabilities in Keycloak 15.0.1

The following vulnerabilities exist in Keycloak 15.0.1 (and affect all users of Keycloak 15):

#### Critical Vulnerabilities
- **mTLS Authentication Bypass** via Reverse Proxy TLS Termination (Patched: 26.0.6+)
- **Denial of Service** vulnerability (Patched: 24.0.0+)
- **Session Fixation** in Elytron SAML adapters (Patched: 22.0.12+)
- **Privilege Escalation** on Token Exchange feature (Patched: 18.0.0+)
- **User Impersonation** via stolen UUID code (Patched: 21.0.1+)
- **Admin API Privilege Escalation** (Patched: 24.0.5+)

#### High Severity Vulnerabilities
- **Hostname Verification** issues (Patched: 26.2.2+)
- **Inefficient Regular Expression Complexity** (Patched: 24.0.9+)
- **Open Redirect** vulnerability (Patched: 25.0.6+)
- **Path Traversal** in redirection validation (Patched: 22.0.10+)
- **Cross-Site Scripting (XSS)** when validating URI-schemes (Patched: 21.1.2+)
- **Improper Authorization** (Patched: 15.1.1+)
- **Improper Client Certificate Validation** (Patched: 21.1.2+)
- **Sensitive Information Exposure** in PAR (Patched: 24.0.5+)
- **Unvalidated Cross-Origin Messages** leading to DDoS (Patched: 22.0.10+)
- **Redirect URI Validation Bypass** (Patched: 23.0.3+)
- **WebAuthn Registration** vulnerability (Patched: 15.1.0+)

### Test Dependencies

#### AssertJ (Test Only - Not Shipped)
- **Version**: 3.23.1 (testImplementation only)
- **Known Vulnerability**: XML External Entity (XXE) when parsing untrusted XML
- **Impact**: LOW - This is a test-only dependency, not included in the deployed JAR
- **Note**: Cannot upgrade to 3.27.7 as it requires Java 11+

### Important Understanding

1. **Mapper vs Server**: These vulnerabilities are in the Keycloak server itself, not in this mapper plugin
2. **CompileOnly Dependencies**: The Keycloak dependencies are `compileOnly` and are NOT included in the plugin JAR
3. **Existing Risk**: If you're running Keycloak 15, you already have these vulnerabilities
4. **Plugin Impact**: This plugin does not introduce new vulnerabilities; it works within your existing Keycloak environment

### STRONGLY RECOMMENDED Actions

1. **Upgrade Keycloak**: Migrate to Keycloak 26.2.2 or later as soon as possible
2. **Use Main Branch**: Switch to the main branch of this project when you upgrade
3. **Security Controls**: Implement additional security controls while running Keycloak 15:
   - Network segmentation
   - Web Application Firewall (WAF)
   - Strict TLS/SSL configuration
   - Regular security monitoring
   - Limited internet exposure

### Why This Branch Exists

Organizations sometimes need time to plan and execute major upgrades. This branch provides a temporary solution for those actively planning migration from Keycloak 15 to current versions.

**This branch is NOT intended for:**
- New installations
- Production systems (unless no other option exists)
- Long-term use

### Security Scanning Results (Plugin Code Only)

- **CodeQL Scan**: ✅ No vulnerabilities in plugin code
- **Code Review**: ✅ No issues in plugin code
- **Plugin Dependencies**: ⚠️ Test dependencies have known issues but are not shipped

### Migration Path

For production security, please migrate to:
1. **Keycloak 26.2.2+** (all known vulnerabilities patched)
2. **Java 17+** (required for modern Keycloak)
3. **Main branch** of this project (actively maintained)
