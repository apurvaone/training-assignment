<html>
   <head>
      <title>${layoutSettings.companyName}</title>
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


   </head>
   <body data-offset="125">

   </body>