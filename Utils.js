/**
 * ============================================================
 * PRIMO ART GALLERY
 * Utils.gs
 * ============================================================
 * Common helper functions
 * ============================================================
 */


/**
 * Formats date & time.
 */
function formatDateTime_(date) {
  return Utilities.formatDate(
    date,
    CONFIG.TIMEZONE,
    'dd MMM yyyy, hh:mm a'
  );
}


/**
 * Converts any value into trimmed text.
 */
function cleanText_(value) {

  if (value === null || value === undefined) {
    return '';
  }

  return String(value).trim();

}


/**
 * Cleans sheet header names.
 */
function cleanHeader_(value) {

  return cleanText_(value)
    .replace(/\s+/g, ' ');

}


/**
 * Escapes HTML safely.
 */
function escapeHtml_(value) {

  return cleanText_(value)

    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');

}


/**
 * Returns readable error text.
 */
function getErrorMessage_(error) {

  if (error && error.message) {
    return String(error.message);
  }

  return String(error);

}


/**
 * Logs error.
 */
function logError_(functionName, error) {

  console.error(
    '[' +
    functionName +
    '] ' +
    getErrorMessage_(error)
  );

}


/**
 * Returns spreadsheet.
 */
function getSpreadsheet_() {

  return SpreadsheetApp.openById(
    CONFIG.SPREADSHEET_ID
  );

}


/**
 * Returns response sheet.
 */
function getResponseSheet_() {

  const sheet = getSpreadsheet_()
    .getSheetByName(CONFIG.SHEET_NAME);

  if (!sheet) {
    throw new Error(
      'Sheet not found: ' +
      CONFIG.SHEET_NAME
    );
  }

  return sheet;

}


/**
 * Builds header map.
 */
function getHeaderMap_(sheet) {

  const headers = sheet
    .getRange(
      1,
      1,
      1,
      sheet.getLastColumn()
    )
    .getDisplayValues()[0];

  const map = {};

  headers.forEach(function(header, index) {

    map[
      cleanHeader_(header)
    ] = index + 1;

  });

  return map;

}


/**
 * Returns required column.
 */
function getRequiredColumn_(headerMap, headerName) {

  const column =
    headerMap[
      cleanHeader_(headerName)
    ];

  if (!column) {

    throw new Error(
      'Missing column: ' +
      headerName
    );

  }

  return column;

}


/**
 * Checks all required headers.
 */
function validateHeaders_() {

  const sheet = getResponseSheet_();

  const headerMap =
    getHeaderMap_(sheet);

  Object.keys(CONFIG.HEADERS)
    .forEach(function(key) {

      const header =
        CONFIG.HEADERS[key];

      if (
        !headerMap[
          cleanHeader_(header)
        ]
      ) {

        throw new Error(
          'Missing header: ' +
          header
        );

      }

    });

}