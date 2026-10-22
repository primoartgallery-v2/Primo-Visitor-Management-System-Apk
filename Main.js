/**
 * ============================================================
 * PRIMO ART GALLERY
 * VISITOR MANAGEMENT SYSTEM
 * Main.gs
 * ============================================================
 */


/**
 * ============================================================
 * FIREBASE HOSTED SCANNER API
 * ============================================================
 *
 * Firebase Hosting par chalne wale Staff Scanner se
 * Google Apps Script backend ko request receive karega.
 *
 * Supported actions:
 *
 * 1. checkIn
 * 2. lookup
 *
 * Example request:
 *
 * {
 *   "action": "checkIn",
 *   "visitorId": "PAG-2026-XXXX"
 * }
 *
 */


/**
 * ============================================================
 * POST API
 * ============================================================
 */
function doPost(e) {

  try {

    // --------------------------------------------------------
    // Validate Request
    // --------------------------------------------------------

    if (
      !e ||
      !e.postData ||
      !e.postData.contents
    ) {

      return jsonResponse_({

        status: 'error',

        message:
          'Invalid API request.'

      });

    }


    // --------------------------------------------------------
    // Parse JSON
    // --------------------------------------------------------

    const data =
      JSON.parse(
        e.postData.contents
      );


    // --------------------------------------------------------
    // Read Action
    // --------------------------------------------------------

    const action =
      cleanText_(
        data.action
      );


    // --------------------------------------------------------
    // Read Visitor ID
    // --------------------------------------------------------

    const visitorId =
      cleanText_(
        data.visitorId
      );


    // ========================================================
    // ACTION 1
    // CHECK-IN VISITOR
    // ========================================================

    if (
      action === 'checkIn'
    ) {


      // ------------------------------------------------------
      // Validate Visitor ID
      // ------------------------------------------------------

      if (!visitorId) {

        return jsonResponse_({

          status:
            'invalid',

          message:
            'Visitor ID is required.'

        });

      }


      // ------------------------------------------------------
      // Perform Check-In
      // ------------------------------------------------------

      const result =
        staffCheckInVisitor(
          visitorId
        );


      // ------------------------------------------------------
      // Return Result
      // ------------------------------------------------------

      return jsonResponse_(
        result
      );

    }


    // ========================================================
    // ACTION 2
    // VISITOR LOOKUP
    // ========================================================

    if (
      action === 'lookup'
    ) {


      // ------------------------------------------------------
      // Validate Visitor ID
      // ------------------------------------------------------

      if (!visitorId) {

        return jsonResponse_({

          status:
            'invalid',

          message:
            'Visitor ID is required.'

        });

      }


      // ------------------------------------------------------
      // Find Visitor
      // ------------------------------------------------------

      const visitor =
        findVisitorById_(
          visitorId
        );


      // ------------------------------------------------------
      // Visitor Not Found
      // ------------------------------------------------------

      if (!visitor) {

        return jsonResponse_({

          status:
            'invalid',

          message:
            'Visitor ID not found.'

        });

      }


      // ------------------------------------------------------
      // Return Visitor Information
      // ------------------------------------------------------

      return jsonResponse_({

        status:
          'success',


        visitor: {

          visitorId:
            visitor.visitorId,

          name:
            visitor.name,

          email:
            visitor.email,

          phone:
            visitor.phone,

          city:
            visitor.city,

          checkInStatus:
            visitor.checkInStatus,

          checkInTime:
            visitor.checkInTime

        }

      });

    }


    // ========================================================
    // UNKNOWN ACTION
    // ========================================================

    return jsonResponse_({

      status:
        'error',

      message:
        'Unknown API action.'

    });


  } catch (error) {


    // --------------------------------------------------------
    // Log Error
    // --------------------------------------------------------

    logError_(
      'doPost API',
      error
    );


    // --------------------------------------------------------
    // Return Error
    // --------------------------------------------------------

    return jsonResponse_({

      status:
        'error',

      message:
        getErrorMessage_(
          error
        )

    });

  }

}


/**
 * ============================================================
 * JSON RESPONSE HELPER
 * ============================================================
 */
function jsonResponse_(data) {

  return ContentService

    .createTextOutput(

      JSON.stringify(
        data
      )

    )

    .setMimeType(

      ContentService.MimeType.JSON

    );

}





/**
 * ============================================================
 * GOOGLE FORM SUBMISSION
 * ============================================================
 *
 * Runs whenever a Google Form response is submitted.
 *
 */
function onFormSubmit(e) {

  try {


    // --------------------------------------------------------
    // Validate Event
    // --------------------------------------------------------

    if (
      !e ||
      !e.range
    ) {

      throw new Error(

        'Please create an Installable On Form Submit Trigger.'

      );

    }


    // --------------------------------------------------------
    // Get Sheet
    // --------------------------------------------------------

    const sheet =
      e.range.getSheet();


    // --------------------------------------------------------
    // Verify Sheet
    // --------------------------------------------------------

    if (

      sheet.getName()
      !==
      CONFIG.SHEET_NAME

    ) {


      console.log(

        'Skipped sheet: '
        +
        sheet.getName()

      );


      return;

    }


    // --------------------------------------------------------
    // Validate Headers
    // --------------------------------------------------------

    validateHeaders_();


    // --------------------------------------------------------
    // Get Submitted Row
    // --------------------------------------------------------

    const row =
      e.range.getRow();


    // --------------------------------------------------------
    // Create Visitor
    // --------------------------------------------------------

    const visitor =
      createVisitor_(
        row
      );


    // --------------------------------------------------------
    // Send Visitor Pass Email
    // --------------------------------------------------------

    const emailResult =
      sendVisitorPassEmail_(
        visitor
      );


    // --------------------------------------------------------
    // Log Email Result
    // --------------------------------------------------------

    console.log(

      JSON.stringify(
        emailResult
      )

    );


  } catch (error) {


    // --------------------------------------------------------
    // Log Error
    // --------------------------------------------------------

    logError_(
      'onFormSubmit',
      error
    );

  }

}





/**
 * ============================================================
 * WEB APP ENTRY POINT
 * ============================================================
 *
 * Existing Google Apps Script Web App system.
 *
 * Supported URLs:
 *
 * Staff Scanner:
 * ?page=staff
 *
 * Visitor Badge:
 * ?id=PAG-2026-XXXX
 *
 * Dashboard:
 * ?page=dashboard
 *
 */
function doGet(e) {


  // ----------------------------------------------------------
  // Get Page Parameter
  // ----------------------------------------------------------

  const page =
    cleanText_(

      e &&
      e.parameter

        ? e.parameter.page

        : ''

    );


  // ----------------------------------------------------------
  // Get Visitor ID
  // ----------------------------------------------------------

  const visitorId =
    cleanText_(

      e &&
      e.parameter

        ? e.parameter.id

        : ''

    );





  // ==========================================================
  // STAFF SCANNER
  // ==========================================================

  if (

    page.toLowerCase()
    ===
    'staff'

  ) {


    return HtmlService

      .createTemplateFromFile(

        'StaffScanner'

      )

      .evaluate()

      .setTitle(

        CONFIG.PROJECT_NAME

      )

      .setXFrameOptionsMode(

        HtmlService
          .XFrameOptionsMode
          .ALLOWALL

      );

  }





  // ==========================================================
  // VISITOR BADGE
  // ==========================================================

  if (
    visitorId
  ) {


    // --------------------------------------------------------
    // Find Visitor
    // --------------------------------------------------------

    const visitor =
      findVisitorById_(
        visitorId
      );


    // --------------------------------------------------------
    // Invalid Visitor
    // --------------------------------------------------------

    if (!visitor) {


      return HtmlService

        .createHtmlOutput(

          '<h2 style="font-family:Arial;text-align:center;margin-top:80px;">' +

          'Invalid Visitor Pass' +

          '</h2>'

        );

    }





    // --------------------------------------------------------
    // Load Badge Template
    // --------------------------------------------------------

    const template =
      HtmlService

        .createTemplateFromFile(

          'BadgeTemplate'

        );


    // --------------------------------------------------------
    // Pass Visitor Data
    // --------------------------------------------------------

    template.visitor =
      visitor;


    template.config =
      CONFIG;


    // --------------------------------------------------------
    // Return Badge
    // --------------------------------------------------------

    return template

      .evaluate()

      .setTitle(

        'Visitor Pass'

      );

  }





  // ==========================================================
  // DASHBOARD
  // ==========================================================

  if (

    page.toLowerCase()
    ===
    'dashboard'

  ) {


    // --------------------------------------------------------
    // Load Dashboard Template
    // --------------------------------------------------------

    const template =
      HtmlService

        .createTemplateFromFile(

          'Dashboard'

        );


    // --------------------------------------------------------
    // Pass Config
    // --------------------------------------------------------

    template.config =
      CONFIG;


    // --------------------------------------------------------
    // Return Dashboard
    // --------------------------------------------------------

    return template

      .evaluate()

      .setTitle(

        CONFIG.PROJECT_NAME

      );

  }





  // ==========================================================
  // DEFAULT PAGE
  // ==========================================================

  return HtmlService

    .createHtmlOutput(

      '<h2 style="font-family:Arial;text-align:center;margin-top:80px;">'

      +

      CONFIG.PROJECT_NAME

      +

      '</h2>'

    );

}





/**
 * ============================================================
 * PROJECT SETUP
 * ============================================================
 */
function setupProject() {


  // ----------------------------------------------------------
  // Setup Started
  // ----------------------------------------------------------

  console.log(

    'Setup Started'

  );



  // ----------------------------------------------------------
  // Validate Headers
  // ----------------------------------------------------------

  validateHeaders_();



  console.log(

    'Headers Verified'

  );



  // ----------------------------------------------------------
  // Connect Spreadsheet
  // ----------------------------------------------------------

  const spreadsheet =
    SpreadsheetApp.openById(

      CONFIG.SPREADSHEET_ID

    );



  console.log(

    'Spreadsheet Connected'

  );



  // ----------------------------------------------------------
  // Get Existing Triggers
  // ----------------------------------------------------------

  const triggers =
    ScriptApp.getProjectTriggers();



  // ----------------------------------------------------------
  // Check Form Trigger
  // ----------------------------------------------------------

  const exists =
    triggers.some(

      function(trigger) {


        return (

          trigger.getHandlerFunction()
          ===
          'onFormSubmit'

          &&

          trigger.getEventType()
          ===
          ScriptApp.EventType
            .ON_FORM_SUBMIT

        );

      }

    );





  // ----------------------------------------------------------
  // Create Trigger If Missing
  // ----------------------------------------------------------

  if (!exists) {


    ScriptApp

      .newTrigger(

        'onFormSubmit'

      )

      .forSpreadsheet(

        spreadsheet

      )

      .onFormSubmit()

      .create();



    console.log(

      'Trigger Created'

    );


  } else {


    console.log(

      'Trigger Already Exists'

    );

  }





  // ----------------------------------------------------------
  // Setup Complete
  // ----------------------------------------------------------

  console.log(

    'Primo Visitor Management System Ready'

  );

}





/**
 * ============================================================
 * OPEN DASHBOARD
 * ============================================================
 */
function openDashboard() {


  const url =

    CONFIG.WEB_APP_URL

    +

    '?page=dashboard';



  const html =

    HtmlService

      .createHtmlOutput(

        '<script>' +

        'window.open("' +

        url +

        '","_blank");' +

        'google.script.host.close();' +

        '</script>'

      );



  SpreadsheetApp

    .getUi()

    .showModalDialog(

      html,

      'Opening Dashboard'

    );

}





/**
 * ============================================================
 * OPEN OLD STAFF SCANNER
 * ============================================================
 *
 * NOTE:
 * Firebase Hosted Scanner ab primary scanner hoga.
 *
 * Ye function existing Apps Script scanner ko
 * preserve karne ke liye rakha gaya hai.
 *
 */
function openScanner() {


  const url =

    CONFIG.WEB_APP_URL

    +

    '?page=staff';



  const html =

    HtmlService

      .createHtmlOutput(

        '<script>' +

        'window.open("' +

        url +

        '","_blank");' +

        'google.script.host.close();' +

        '</script>'

      );



  SpreadsheetApp

    .getUi()

    .showModalDialog(

      html,

      'Opening Scanner'

    );

}