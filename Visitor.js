/**
 * ============================================================
 * VISITOR.GS
 * Visitor & Sheet Management Functions
 * ============================================================
 */

/**
 * Validates that all configured headers exist.
 */
function validateHeaders_() {

  const sheet = getResponseSheet_();
  const headerMap = getHeaderMap_(sheet);

  Object.keys(CONFIG.HEADERS).forEach(function (key) {

    const header = CONFIG.HEADERS[key];

    if (!headerMap[cleanHeader_(header)]) {

      throw new Error(
        'Missing required header: ' + header
      );

    }

  });

  return true;

}


/**
 * Returns the configured response sheet.
 */
function getResponseSheet_() {

  const spreadsheet =
    SpreadsheetApp.openById(
      CONFIG.SPREADSHEET_ID
    );

  const sheet =
    spreadsheet.getSheetByName(
      CONFIG.SHEET_NAME
    );

  if (!sheet) {

    throw new Error(
      'Sheet not found: ' +
      CONFIG.SHEET_NAME
    );

  }

  return sheet;

}


/**
 * Builds a normalized header map.
 */
function getHeaderMap_(sheet) {

  if (!sheet) {

    throw new Error(
      'Sheet object is required.'
    );

  }

  const lastColumn =
    sheet.getLastColumn();

  if (lastColumn < 1) {

    throw new Error(
      'Sheet has no headers.'
    );

  }

  const headers =
    sheet
      .getRange(
        1,
        1,
        1,
        lastColumn
      )
      .getDisplayValues()[0];

  const map = {};

  headers.forEach(function (
    header,
    index
  ) {

    const key =
      cleanHeader_(header);

    if (key) {

      map[key] =
        index + 1;

    }

  });

  return map;

}


/**
 * Returns required column.
 */
function getRequiredColumn_(
  headerMap,
  headerName
) {

  const column =
    headerMap[
      cleanHeader_(headerName)
    ];

  if (!column) {

    throw new Error(
      'Required column not found: ' +
      headerName
    );

  }

  return column;

}


/**
 * Generates the next Visitor ID.
 */
function generateVisitorId_() {

  const lock =
    LockService.getScriptLock();

  lock.waitLock(30000);

  try {

    const sheet =
      getResponseSheet_();

    const headerMap =
      getHeaderMap_(sheet);

    const idColumn =
      getRequiredColumn_(
        headerMap,
        CONFIG.HEADERS.UNIQUE_ID
      );

    const lastRow =
      sheet.getLastRow();

    let max = 0;

    if (lastRow >= 2) {

      const values =
        sheet
          .getRange(
            2,
            idColumn,
            lastRow - 1,
            1
          )
          .getDisplayValues();

      values.forEach(function (row) {

        const id =
          cleanText_(row[0]);

        const match =
          id.match(/(\d+)$/);

        if (match) {

          const number =
            Number(match[1]);

          if (
            !isNaN(number) &&
            number > max
          ) {

            max = number;

          }

        }

      });

    }

    return (
      CONFIG.ID_PREFIX +
      String(max + 1)
        .padStart(4, '0')
    );

  }

  finally {

    lock.releaseLock();

  }

}/**
 * Creates a QR Code URL.
 * QR contains the Visitor Pass URL.
 */
function generateQrUrl_(visitorId) {

  const webAppUrl =
    cleanText_(CONFIG.WEB_APP_URL);

  if (
    !webAppUrl ||
    webAppUrl.indexOf('/exec') === -1
  ) {

    throw new Error(
      'WEB_APP_URL is not configured correctly.'
    );

  }

  const visitorPassUrl =
    webAppUrl +
    '?id=' +
    encodeURIComponent(visitorId);

  return (
    'https://quickchart.io/qr?text=' +
    encodeURIComponent(visitorPassUrl) +
    '&size=' +
    CONFIG.QR_SIZE +
    '&margin=2'
  );

}


/**
 * Reads one visitor row.
 */
function getVisitorByRow_(
  sheet,
  rowNumber,
  headerMap
) {

  const row =
    sheet
      .getRange(
        rowNumber,
        1,
        1,
        sheet.getLastColumn()
      )
      .getDisplayValues()[0];

  function getValue_(headerName) {

    const column =
      headerMap[
        cleanHeader_(headerName)
      ];

    if (!column) {
      return '';
    }

    return cleanText_(
      row[column - 1]
    );

  }

  return {

    rowNumber: rowNumber,

    timestamp:
      getValue_(
        CONFIG.HEADERS.TIMESTAMP
      ),

    name:
      getValue_(
        CONFIG.HEADERS.NAME
      ),

    phone:
      getValue_(
        CONFIG.HEADERS.PHONE
      ),

    email:
      getValue_(
        CONFIG.HEADERS.EMAIL
      ),

    city:
      getValue_(
        CONFIG.HEADERS.CITY
      ),

    source:
      getValue_(
        CONFIG.HEADERS.SOURCE
      ),

    consent:
      getValue_(
        CONFIG.HEADERS.CONSENT
      ),

    profession:
      getValue_(
        CONFIG.HEADERS.PROFESSION
      ),

    visitorId:
      getValue_(
        CONFIG.HEADERS.UNIQUE_ID
      ),

    qrCode:
      getValue_(
        CONFIG.HEADERS.QR_CODE
      ),

    emailStatus:
      getValue_(
        CONFIG.HEADERS.EMAIL_STATUS
      ),

    emailError:
      getValue_(
        CONFIG.HEADERS.EMAIL_ERROR
      ),

    checkInStatus:
      getValue_(
        CONFIG.HEADERS.CHECKIN_STATUS
      ),

    checkInTime:
      getValue_(
        CONFIG.HEADERS.CHECKIN_TIME
      )

  };

}


/**
 * Finds a visitor using Visitor ID.
 */
function findVisitorById_(visitorId) {

  const searchId =
    cleanText_(visitorId)
      .toUpperCase();

  if (!searchId) {

    return null;

  }

  const sheet =
    getResponseSheet_();

  const headerMap =
    getHeaderMap_(sheet);

  const idColumn =
    getRequiredColumn_(
      headerMap,
      CONFIG.HEADERS.UNIQUE_ID
    );

  const lastRow =
    sheet.getLastRow();

  if (lastRow < 2) {

    return null;

  }

  const values =
    sheet
      .getRange(
        2,
        idColumn,
        lastRow - 1,
        1
      )
      .getDisplayValues();

  for (
    let i = 0;
    i < values.length;
    i++
  ) {

    if (
      cleanText_(values[i][0])
        .toUpperCase() === searchId
    ) {

      return getVisitorByRow_(
        sheet,
        i + 2,
        headerMap
      );

    }

  }

  return null;

}/**
 * Updates a field for a visitor.
 */
function updateVisitorField_(visitor, fieldName, value) {

  if (!visitor || !visitor.rowNumber) {

    throw new Error(
      'Invalid visitor record.'
    );

  }

  const sheet =
    getResponseSheet_();

  const headerMap =
    getHeaderMap_(sheet);

  const column =
    getRequiredColumn_(
      headerMap,
      fieldName
    );

  sheet
    .getRange(
      visitor.rowNumber,
      column
    )
    .setValue(value);

}


/**
 * Returns all visitors.
 */
function getAllVisitors_() {

  const sheet =
    getResponseSheet_();

  const headerMap =
    getHeaderMap_(sheet);

  const lastRow =
    sheet.getLastRow();

  if (lastRow < 2) {

    return [];

  }

  const visitors = [];

  for (
    let row = 2;
    row <= lastRow;
    row++
  ) {

    visitors.push(

      getVisitorByRow_(
        sheet,
        row,
        headerMap
      )

    );

  }

  return visitors;

}


/**
 * Returns total visitors.
 */
function getVisitorCount_() {

  return getAllVisitors_().length;

}


/**
 * Returns checked-in visitors.
 */
function getCheckedInCount_() {

  return getAllVisitors_()

    .filter(function(visitor){

      return (
        cleanText_(
          visitor.checkInStatus
        ).toLowerCase()
        ===
        'checked in'
      );

    })

    .length;

}


/**
 * Returns pending visitors.
 */
function getPendingCount_() {

  return getAllVisitors_()

    .filter(function(visitor){

      return (
        cleanText_(
          visitor.checkInStatus
        ).toLowerCase()
        !==
        'checked in'
      );

    })

    .length;

}


/**
 * Searches visitors.
 */
function searchVisitors(keyword) {

  keyword =
    cleanText_(keyword)
      .toLowerCase();

  if (!keyword) {

    return getAllVisitors_();

  }

  return getAllVisitors_()

    .filter(function(visitor){

      return [

        visitor.name,
        visitor.email,
        visitor.phone,
        visitor.city,
        visitor.visitorId

      ]

      .join(' ')

      .toLowerCase()

      .includes(keyword);

    });

}/**
 * Creates visitor record after form submit
 */
function createVisitor_(rowNumber) {

  const sheet = getResponseSheet_();

  const headerMap = getHeaderMap_(sheet);


  const visitorId =
    generateVisitorId_();


  const qrCode =
    generateQrUrl_(visitorId);



  const visitorIdCol =
    getRequiredColumn_(
      headerMap,
      CONFIG.HEADERS.UNIQUE_ID
    );


  const qrCol =
    getRequiredColumn_(
      headerMap,
      CONFIG.HEADERS.QR_CODE
    );


  const emailStatusCol =
    getRequiredColumn_(
      headerMap,
      CONFIG.HEADERS.EMAIL_STATUS
    );


  const emailErrorCol =
    getRequiredColumn_(
      headerMap,
      CONFIG.HEADERS.EMAIL_ERROR
    );


  const checkStatusCol =
    getRequiredColumn_(
      headerMap,
      CONFIG.HEADERS.CHECKIN_STATUS
    );


  const checkTimeCol =
    getRequiredColumn_(
      headerMap,
      CONFIG.HEADERS.CHECKIN_TIME
    );



  sheet
    .getRange(rowNumber, visitorIdCol)
    .setValue(visitorId);



  sheet
    .getRange(rowNumber, qrCol)
    .setValue(qrCode);



  sheet
    .getRange(rowNumber, emailStatusCol)
    .setValue("Processing");



  sheet
    .getRange(rowNumber, emailErrorCol)
    .setValue("");



  sheet
    .getRange(rowNumber, checkStatusCol)
    .setValue("Pending");



  sheet
    .getRange(rowNumber, checkTimeCol)
    .setValue("");



  SpreadsheetApp.flush();



  const visitor =
    getVisitorByRow_(
      sheet,
      rowNumber,
      headerMap
    );


  return visitor;

}