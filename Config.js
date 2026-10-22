/**
 * ============================================================
 * PRIMO ART GALLERY
 * VISITOR MANAGEMENT SYSTEM
 * Config.gs
 * ============================================================
 */

const CONFIG = Object.freeze({

  // ==========================================================
  // PROJECT
  // ==========================================================

  PROJECT_NAME: 'Primo Visitor Management System',

  ORGANIZER: 'Primo Art Gallery',

  EVENT_NAME: 'DRISHTI – A Legacy of Masters',

  VENUE:
    'Living Traditions Centre (LTC), Bikaner House, Pandara Road, New Delhi – 110003',

  EVENT_DATE: '19–23 December 2026',

  EVENT_TIME: '11:00 AM – 7:00 PM',

  WEBSITE: 'https://primoartgallery.com',

  EMAIL: 'primoartgallery9248@gmail.com',

  PHONE: '+91 8595507802',

  TIMEZONE: 'Asia/Kolkata',


  // ==========================================================
  // GOOGLE FORM / SHEET
  // ==========================================================

  SPREADSHEET_ID:
    '1p5dxwVx9EgdGK2KceEumuJRU1HMxe-CvltEJZZqBRfE',

  FORM_ID:
    '1_bNuOsHmGUANjbGhfJX54_vF-eyl6S1grp3UBnf3EAY',

  SHEET_NAME: 'Form responses 1',


  // ==========================================================
  // WEB APP
  // ==========================================================

  WEB_APP_URL:
    'https://script.google.com/macros/s/AKfycbxqdfRHhyFAm61gRTCoB83CQibYcgN_3N51ShvSwJaavBluXh6qZoVePLZoYPfm-ErX/exec',


  // ==========================================================
  // VISITOR ID
  // ==========================================================

  ID_PREFIX: 'PAG-2026-',

  QR_SIZE: 500,


  // ==========================================================
  // STAFF
  // ==========================================================

  STAFF_PIN: '2026',


  // ==========================================================
  // EMAIL
  // ==========================================================

  EMAIL_SUBJECT:
    'Primo Art Gallery | Visitor Pass',

  EMAIL_FROM_NAME:
    'Primo Art Gallery',


  // ==========================================================
  // STATUS
  // ==========================================================

  STATUS: Object.freeze({

    PENDING: 'Pending',

    CHECKED_IN: 'Checked In',

    SENT: 'Sent',

    FAILED: 'Failed',

    PROCESSING: 'Processing'

  }),


  // ==========================================================
  // SHEET HEADERS
  // ==========================================================

  HEADERS: Object.freeze({

    TIMESTAMP: 'Timestamp',

    NAME: 'Full Name',

    PHONE: 'Phone No.',

    EMAIL: 'E-Mail ID',

    CITY: 'City',

    SOURCE: 'How did you hear about us?',

    CONSENT: 'Consent',

    PROFESSION: 'Profession',

    UNIQUE_ID: 'Unique ID',

    QR_CODE: 'QR Code',

    EMAIL_STATUS: 'Email Status',

    EMAIL_ERROR: 'Email Error',

    CHECKIN_STATUS: 'Check-in Status',

    CHECKIN_TIME: 'Check-in Time'

  })


});