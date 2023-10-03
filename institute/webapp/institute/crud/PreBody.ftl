<html>
   <head>
      <title>Acropolis</title>
      <meta name="viewport" content="width=device-width, user-scalable=no"/>
      <#if webSiteFaviconContent?has_content>
      <link rel="shortcut icon" href="">
      </#if>
      <#list layoutSettings.styleSheets as styleSheet>
      <link rel="stylesheet" href="${StringUtil.wrapString(styleSheet)}" type="text/css"/>
      </#list>
      <#list layoutSettings.javaScripts as javaScript>
      <script type="text/javascript" src="${StringUtil.wrapString(javaScript)}"></script>
      </#list>
      <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.5.2/css/bootstrap.min.css">
      <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css">
      <style>

         .custom-box {
         font-size: 12px;
         padding: 5px;
         overflow-y: auto;
         }

         /* Custom CSS for smaller h2 size */
         h2 {
         font-size: 1.5rem;
         }

         /* Custom CSS for smaller h3 size */
         h3 {
         font-size: 1.25rem;
         }

         h4 {
         font-size: 1.00rem;
         }

         /* Custom CSS for smaller p size */
         p {
         font-size: 16px;
         }

         .edit-button {
         font-size: 0.8rem;
         background-color: #1282A2;
         color: #fff;
         padding: 5px 10px;
         border: none;
         border-radius: 5px;
         cursor: pointer;
         }

         .delete-icon-container i   {
         display: flex;
         justify-content: center;
         align-items: center;
         height: 100%;
         color:#1282A2;!important;
         }

         .edit-button:hover {
         background-color: #0056b3;
         }

      .view-details-link {
           text-decoration: none;
           color:#1282A2;
           font-weight: 600;
           display: block;
           text-align: center;
         }

         .custom-add-student-button {
           background-color: #1282A2;
           border-color: #1282A2;
           color: #fff;
           font-weight: bold;
         }

         .heading {
         font-size: 1.75rem;
         font-weight: bold;
         text-align: left;
         margin-bottom: 20px;}

      </style>

        <script src="https://code.jquery.com/jquery-3.6.3.min.js"></script>

   </head>
   <body data-offset="125">
      <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
         <a class="navbar-brand" href="<@ofbizUrl>ShowParties</@ofbizUrl>" style="margin-left: 20px;">Acropolis Group of Institutions
         </a>
         <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent" aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
         <span class="navbar-toggler-icon"></span>
         </button>
         <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav mr-auto">
               <li class="nav-item active">
                  <a class="nav-link" href="<@ofbizUrl>ShowContact</@ofbizUrl>">Contact <span class="sr-only"></span></a>
               </li>
            </ul>
         </div>
      </nav>
   </body>