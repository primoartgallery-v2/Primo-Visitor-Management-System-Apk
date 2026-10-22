/**
 * ============================================================
 * PRIMO ART GALLERY
 * Security.gs
 * ============================================================
 */


/**
 * Change this PIN whenever required.
 */
const SECURITY = Object.freeze({

  STAFF_PIN: "2026",

  ADMIN_PIN: "9248"

});


/**
 * Verify Staff PIN
 */
function verifyStaffPin(pin) {

  pin = cleanText_(pin);

  return pin === SECURITY.STAFF_PIN;

}


/**
 * Verify Admin PIN
 */
function verifyAdminPin(pin) {

  pin = cleanText_(pin);

  return pin === SECURITY.ADMIN_PIN;

}


/**
 * Create Session
 */
function createSession(type) {

  const cache =
    CacheService.getUserCache();

  const token =
    Utilities.getUuid();

  cache.put(
    token,
    type,
    6 * 60 * 60
  );

  return token;

}


/**
 * Validate Session
 */
function validateSession(token) {

  if (!token) {
    return false;
  }

  const cache =
    CacheService.getUserCache();

  return cache.get(token) != null;

}


/**
 * Remove Session
 */
function destroySession(token) {

  if (!token) {
    return;
  }

  CacheService
    .getUserCache()
    .remove(token);

}


/**
 * Staff Login
 */
function staffLogin(pin) {

  if (!verifyStaffPin(pin)) {

    return {

      success: false,

      message: "Invalid Staff PIN"

    };

  }

  const token =
    createSession("staff");

  return {

    success: true,

    token: token

  };

}


/**
 * Admin Login
 */
function adminLogin(pin) {

  if (!verifyAdminPin(pin)) {

    return {

      success: false,

      message: "Invalid Admin PIN"

    };

  }

  const token =
    createSession("admin");

  return {

    success: true,

    token: token

  };

}


/**
 * Logout
 */
function logout(token) {

  destroySession(token);

  return {

    success: true

  };

}


/**
 * Generate One-Time Access Code
 */
function generateAccessCode() {

  return Math.floor(
    100000 +
    Math.random() * 900000
  ).toString();

}


/**
 * Verify Access Code
 */
function verifyAccessCode(input, actual) {

  return cleanText_(input) === cleanText_(actual);

}


/**
 * Check Project Access
 */
function isAuthorized(token) {

  return validateSession(token);

}


/**
 * Return Security Status
 */
function securityStatus() {

  return {

    project: CONFIG.PROJECT_NAME,

    security: "Enabled",

    sessionTimeout: "6 Hours",

    version: "1.0"

  };

}