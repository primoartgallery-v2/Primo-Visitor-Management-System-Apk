/**
 * ============================================================
 * REPORTS.GS
 * Reporting & Statistics
 * ============================================================
 */

/**
 * Returns dashboard statistics.
 */
function getDashboardStats() {

  const sheet = getResponseSheet_();
  const data = sheet.getDataRange().getValues();

  if (data.length <= 1) {
    return {
      total: 0,
      checkedIn: 0,
      pending: 0,
      emailsSent: 0,
      emailsFailed: 0
    };
  }

  const headers = data[0];

  const statusCol = headers.indexOf(CONFIG.HEADERS.CHECKIN_STATUS);
  const emailCol = headers.indexOf(CONFIG.HEADERS.EMAIL_STATUS);

  let total = 0;
  let checked = 0;
  let pending = 0;
  let sent = 0;
  let failed = 0;

  for (let i = 1; i < data.length; i++) {

    total++;

    if (statusCol > -1) {

      const status = String(data[i][statusCol]).trim().toLowerCase();

      if (status === "checked in") {
        checked++;
      } else {
        pending++;
      }

    }

    if (emailCol > -1) {

      const email = String(data[i][emailCol]).trim().toLowerCase();

      if (email === "sent") {
        sent++;
      }

      if (email === "failed") {
        failed++;
      }

    }

  }

  return {
    total: total,
    checkedIn: checked,
    pending: pending,
    emailsSent: sent,
    emailsFailed: failed
  };

}


/**
 * Returns complete visitor list.
 */
function getAllVisitors() {

  const sheet = getResponseSheet_();
  const values = sheet.getDataRange().getDisplayValues();

  if (values.length <= 1) return [];

  const headers = values.shift();

  return values.map(function(row) {

    let obj = {};

    headers.forEach(function(header,index){

      obj[header] = row[index];

    });

    return obj;

  });

}


/**
 * Returns today's registrations.
 */
function getTodayVisitors() {

  const sheet = getResponseSheet_();
  const values = sheet.getDataRange().getValues();

  const today = Utilities.formatDate(
    new Date(),
    CONFIG.TIMEZONE,
    "dd/MM/yyyy"
  );

  let result = [];

  for (let i = 1; i < values.length; i++) {

    let rowDate = values[i][0];

    if (rowDate instanceof Date) {

      let d = Utilities.formatDate(
        rowDate,
        CONFIG.TIMEZONE,
        "dd/MM/yyyy"
      );

      if (d === today) {
        result.push(values[i]);
      }

    }

  }

  return result;

}


/**
 * Count visitors by city.
 */
function getCityWiseReport() {

  const sheet = getResponseSheet_();
  const values = sheet.getDataRange().getValues();

  const headers = values[0];

  const cityCol = headers.indexOf(CONFIG.HEADERS.CITY);

  let report = {};

  for (let i = 1; i < values.length; i++) {

    let city = String(values[i][cityCol]).trim();

    if (!city) city = "Unknown";

    if (!report[city]) {
      report[city] = 0;
    }

    report[city]++;

  }

  return report;

}


/**
 * Export all data to CSV.
 */
function exportCSV() {

  const sheet = getResponseSheet_();

  const csv = sheet
    .getDataRange()
    .getDisplayValues()
    .map(r => r.join(","))
    .join("\n");

  return Utilities.base64Encode(csv);

}


/**
 * Returns latest registrations.
 */
function getLatestVisitors(limit) {

  limit = limit || 20;

  const sheet = getResponseSheet_();

  const data = sheet.getDataRange().getDisplayValues();

  if (data.length <= 1) return [];

  const headers = data.shift();

  data.reverse();

  const latest = data.slice(0, limit);

  return latest.map(function(row){

    let obj = {};

    headers.forEach(function(h,i){

      obj[h] = row[i];

    });

    return obj;

  });

}


/**
 * Searches visitors.
 */
function searchVisitors(keyword) {

  keyword = String(keyword).toLowerCase();

  const list = getAllVisitors();

  return list.filter(function(item){

    return JSON.stringify(item)
      .toLowerCase()
      .indexOf(keyword) > -1;

  });

}


/**
 * Returns complete report.
 */
function generateReport() {

  return {

    dashboard : getDashboardStats(),

    cityWise : getCityWiseReport(),

    latest : getLatestVisitors(10)

  };

}