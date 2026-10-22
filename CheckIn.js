/**
 * ============================================================
 * PRIMO ART GALLERY
 * VISITOR MANAGEMENT SYSTEM
 * CheckIn.gs
 * ============================================================
 */


/**
 * ============================================================
 * STAFF QR CHECK-IN
 * ============================================================
 *
 * This function:
 *
 * 1. Validates Visitor ID
 * 2. Finds visitor in Google Sheet
 * 3. Prevents duplicate check-in
 * 4. Uses LockService to prevent simultaneous check-ins
 * 5. Updates Check-in Status
 * 6. Updates Check-in Time
 * 7. Returns visitor information
 *
 */
function staffCheckInVisitor(visitorId) {


  // ==========================================================
  // ACQUIRE SCRIPT LOCK
  // ==========================================================

  const lock =
    LockService.getScriptLock();


  try {


    // --------------------------------------------------------
    // Wait for another check-in operation to finish
    // --------------------------------------------------------

    lock.waitLock(10000);


    // --------------------------------------------------------
    // Clean Visitor ID
    // --------------------------------------------------------

    visitorId =
      cleanText_(
        visitorId
      );


    // --------------------------------------------------------
    // Validate Visitor ID
    // --------------------------------------------------------

    if (!visitorId) {

      return {

        status:
          "invalid",

        message:
          "Invalid Visitor ID."

      };

    }


    // ========================================================
    // FIND VISITOR
    // ========================================================

    const visitor =
      findVisitorById_(
        visitorId
      );


    // --------------------------------------------------------
    // Visitor Not Found
    // --------------------------------------------------------

    if (!visitor) {

      return {

        status:
          "invalid",

        message:
          "Visitor ID not found."

      };

    }


    // ========================================================
    // CHECK DUPLICATE CHECK-IN
    // ========================================================

    const currentStatus =
      cleanText_(
        visitor.checkInStatus
      ).toLowerCase();


    if (
      currentStatus
      ===
      "checked in"
    ) {


      return {

        status:
          "already_checked",

        message:
          "Visitor has already checked in.",

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

      };

    }


    // ========================================================
    // CREATE CHECK-IN TIME
    // ========================================================

    const now =
      new Date();


    // ========================================================
    // UPDATE CHECK-IN STATUS
    // ========================================================

    updateVisitorField_(

      visitor,

      CONFIG.HEADERS.CHECKIN_STATUS,

      "Checked In"

    );


    // ========================================================
    // UPDATE CHECK-IN TIME
    // ========================================================

    updateVisitorField_(

      visitor,

      CONFIG.HEADERS.CHECKIN_TIME,

      now

    );


    // ========================================================
    // RETURN SUCCESS
    // ========================================================

    return {

      status:
        "success",

      message:
        "Visitor checked in successfully.",

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
          "Checked In",

        checkInTime:
          formatDateTime_(
            now
          )

      }

    };


  }


  // ==========================================================
  // ERROR HANDLING
  // ==========================================================

  catch(error) {


    // --------------------------------------------------------
    // Log Error
    // --------------------------------------------------------

    logError_(

      "staffCheckInVisitor",

      error

    );


    // --------------------------------------------------------
    // Return Error
    // --------------------------------------------------------

    return {

      status:
        "error",

      message:
        getErrorMessage_(
          error
        )

    };


  }


  // ==========================================================
  // RELEASE LOCK
  // ==========================================================

  finally {


    try {

      lock.releaseLock();

    }

    catch(lockError) {

      console.log(

        "Lock release error: "
        +
        lockError

      );

    }

  }

}





/**
 * ============================================================
 * QR VALIDATION ALIAS
 * ============================================================
 *
 * Used by existing scanner code.
 *
 */
function checkInVisitor(visitorId) {

  return staffCheckInVisitor(

    visitorId

  );

}





/**
 * ============================================================
 * RESET VISITOR CHECK-IN
 * ============================================================
 *
 * Changes:
 *
 * Checked In
 *      ↓
 * Pending
 *
 * Check-in time is cleared.
 *
 */
function resetCheckIn(visitorId) {


  // ----------------------------------------------------------
  // Clean Visitor ID
  // ----------------------------------------------------------

  visitorId =
    cleanText_(
      visitorId
    );


  // ----------------------------------------------------------
  // Find Visitor
  // ----------------------------------------------------------

  const visitor =
    findVisitorById_(
      visitorId
    );


  // ----------------------------------------------------------
  // Validate Visitor
  // ----------------------------------------------------------

  if (!visitor) {

    throw new Error(

      "Visitor not found."

    );

  }


  // ----------------------------------------------------------
  // Update Status
  // ----------------------------------------------------------

  updateVisitorField_(

    visitor,

    CONFIG.HEADERS.CHECKIN_STATUS,

    "Pending"

  );


  // ----------------------------------------------------------
  // Clear Check-in Time
  // ----------------------------------------------------------

  updateVisitorField_(

    visitor,

    CONFIG.HEADERS.CHECKIN_TIME,

    ""

  );


  // ----------------------------------------------------------
  // Return Result
  // ----------------------------------------------------------

  return {

    success:
      true,

    visitorId:
      visitor.visitorId

  };

}





/**
 * ============================================================
 * REGENERATE QR CODE
 * ============================================================
 */
function regenerateQr(visitorId) {


  // ----------------------------------------------------------
  // Clean Visitor ID
  // ----------------------------------------------------------

  visitorId =
    cleanText_(
      visitorId
    );


  // ----------------------------------------------------------
  // Find Visitor
  // ----------------------------------------------------------

  const visitor =
    findVisitorById_(
      visitorId
    );


  // ----------------------------------------------------------
  // Validate Visitor
  // ----------------------------------------------------------

  if (!visitor) {

    throw new Error(

      "Visitor not found."

    );

  }


  // ----------------------------------------------------------
  // Generate QR URL
  // ----------------------------------------------------------

  const qrUrl =
    generateQrUrl_(
      visitor.visitorId
    );


  // ----------------------------------------------------------
  // Save QR URL
  // ----------------------------------------------------------

  updateVisitorField_(

    visitor,

    CONFIG.HEADERS.QR_CODE,

    qrUrl

  );


  // ----------------------------------------------------------
  // Return Result
  // ----------------------------------------------------------

  return {

    success:
      true,

    qrUrl:
      qrUrl

  };

}





/**
 * ============================================================
 * CHECK-IN SUMMARY
 * ============================================================
 *
 * Returns:
 *
 * total
 * checkedIn
 * pending
 *
 */
function getCheckInSummary() {


  // ----------------------------------------------------------
  // Get Response Sheet
  // ----------------------------------------------------------

  const sheet =
    getResponseSheet_();


  // ----------------------------------------------------------
  // Get Header Map
  // ----------------------------------------------------------

  const headerMap =
    getHeaderMap_(
      sheet
    );


  // ----------------------------------------------------------
  // Get Check-in Status Column
  // ----------------------------------------------------------

  const statusCol =
    getRequiredColumn_(

      headerMap,

      CONFIG.HEADERS.CHECKIN_STATUS

    );


  // ----------------------------------------------------------
  // Calculate Total Visitors
  // ----------------------------------------------------------

  const total =
    Math.max(

      sheet.getLastRow()
      -
      1,

      0

    );


  // ----------------------------------------------------------
  // Counters
  // ----------------------------------------------------------

  let checkedIn =
    0;


  let pending =
    0;


  // ==========================================================
  // READ STATUS DATA
  // ==========================================================

  if (
    total > 0
  ) {


    const values =
      sheet

        .getRange(

          2,

          statusCol,

          total,

          1

        )

        .getDisplayValues();


    // --------------------------------------------------------
    // Count Status
    // --------------------------------------------------------

    values.forEach(

      function(row) {


        const status =
          cleanText_(
            row[0]
          ).toLowerCase();


        if (

          status
          ===
          "checked in"

        ) {


          checkedIn++;


        }


        else {


          pending++;


        }

      }

    );

  }


  // ==========================================================
  // RETURN SUMMARY
  // ==========================================================

  return {

    total:
      total,

    checkedIn:
      checkedIn,

    pending:
      pending

  };

}





/**
 * ============================================================
 * MANUAL CHECK-IN
 * ============================================================
 */
function manualCheckIn(visitorId) {


  return staffCheckInVisitor(

    visitorId

  );

}





/**
 * ============================================================
 * VISITOR STATUS
 * ============================================================
 */
function getVisitorStatus(visitorId) {


  // ----------------------------------------------------------
  // Clean Visitor ID
  // ----------------------------------------------------------

  visitorId =
    cleanText_(
      visitorId
    );


  // ----------------------------------------------------------
  // Find Visitor
  // ----------------------------------------------------------

  const visitor =
    findVisitorById_(
      visitorId
    );


  // ----------------------------------------------------------
  // Visitor Not Found
  // ----------------------------------------------------------

  if (!visitor) {

    return null;

  }


  // ----------------------------------------------------------
  // Return Status
  // ----------------------------------------------------------

  return {

    visitorId:
      visitor.visitorId,

    name:
      visitor.name,

    status:
      visitor.checkInStatus,

    time:
      visitor.checkInTime

  };

}