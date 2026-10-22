/**
 * ============================================================
 * PRIMO ART GALLERY
 * Email.gs
 * ============================================================
 */


/**
 * Sends Visitor Pass Email
 */
function sendVisitorPassEmail_(visitor) {


  const email =
    cleanText_(visitor.email);



  if(
    !email ||
    email.indexOf("@") === -1
  ){


    updateVisitorField_(
      visitor,
      CONFIG.HEADERS.EMAIL_STATUS,
      "Failed"
    );


    updateVisitorField_(
      visitor,
      CONFIG.HEADERS.EMAIL_ERROR,
      "Invalid Email Address"
    );


    return {
      success:false,
      error:"Invalid Email Address"
    };

  }



  try{


    const template =
      HtmlService
      .createTemplateFromFile(
        "EmailTemplate"
      );



    template.visitor = visitor;

    template.config = CONFIG;



    const htmlBody =
      template
      .evaluate()
      .getContent();




    const subject =
      CONFIG.EVENT_NAME +
      " | Visitor Pass | " +
      visitor.visitorId;





    MailApp.sendEmail({


      to:email,


      subject:subject,


      htmlBody:htmlBody,


      body:

      "Dear "+
      visitor.name+
      ",\n\n"+
      "Your visitor pass for "+
      CONFIG.EVENT_NAME+
      " is ready.\n\n"+
      "Visitor ID: "+
      visitor.visitorId+
      "\n\n"+
      "Please carry this pass during the exhibition."


    });





    updateVisitorField_(

      visitor,

      CONFIG.HEADERS.EMAIL_STATUS,

      "Sent"

    );



    updateVisitorField_(

      visitor,

      CONFIG.HEADERS.EMAIL_ERROR,

      ""

    );





    return {

      success:true,

      email:email

    };



  }



  catch(error){



    logError_(

      "sendVisitorPassEmail_",

      error

    );



    updateVisitorField_(

      visitor,

      CONFIG.HEADERS.EMAIL_STATUS,

      "Failed"

    );



    updateVisitorField_(

      visitor,

      CONFIG.HEADERS.EMAIL_ERROR,

      getErrorMessage_(error)

    );




    return {

      success:false,

      error:getErrorMessage_(error)

    };


  }


}





/**
 * Resend Visitor Pass
 */
function resendVisitorEmail(visitorId){


  const visitor =
    findVisitorById_(visitorId);



  if(!visitor){

    throw new Error(
      "Visitor not found."
    );

  }



  return sendVisitorPassEmail_(
    visitor
  );


}





/**
 * Resend all pending emails
 */
function resendPendingEmails(){



  const sheet =
    getResponseSheet_();



  const headerMap =
    getHeaderMap_(sheet);



  const lastRow =
    sheet.getLastRow();



  if(lastRow < 2){

    return {
      success:false,
      message:"No visitors found."
    };

  }





  let sent = 0;





  for(
    let row=2;
    row<=lastRow;
    row++
  ){



    const visitor =
      getVisitorByRow_(
        sheet,
        row,
        headerMap
      );





    if(

      visitor.email &&

      visitor.emailStatus !== "Sent"

    ){



      const result =
        sendVisitorPassEmail_(
          visitor
        );



      if(result.success){

        sent++;

      }



      Utilities.sleep(500);



    }



  }




  return {

    success:true,

    emailsSent:sent

  };


}





/**
 * Test Email
 */
function sendTestEmail(){



  MailApp.sendEmail({


    to:CONFIG.EMAIL,


    subject:
    "Primo Visitor System Test",


    body:
    "Email service is working successfully."


  });



  return {

    success:true,

    message:"Test email sent."

  };


}