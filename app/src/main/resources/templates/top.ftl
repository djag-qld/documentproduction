<#include "macros.ftl"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en-AU" lang="en-AU">
	<head>
		<!-- VERSION: @VERSION@ -->
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    	<meta charset="utf-8">
    	<meta http-equiv="X-UA-Compatible" content="IE=edge">
    	<meta name="viewport" content="width=device-width, initial-scale=1">

		<link rel="shortcut icon" href="https://static.qgov.net.au/assets/v2/images/skin/favicon.ico" />
		
		<link href="${themebase}/vendors/bootstrap/dist/css/bootstrap.min.css" rel="stylesheet">
	    <link href="${themebase}/vendors/font-awesome/css/font-awesome.min.css" rel="stylesheet">
	    <link href="${themebase}/vendors/iCheck/skins/flat/green.css" rel="stylesheet">
	    <link href="${themebase}/vendors/bootstrap-progressbar/css/bootstrap-progressbar-3.3.4.min.css" rel="stylesheet">
	    <link href="${themebase}/build/css/custom.min.css" rel="stylesheet">
		<link rel="stylesheet" href="${themebase}/vendors/select2/dist/css/select2.min.css">
		<link href="${fullUrl}/../public/css/documentproduction-2.css" rel="stylesheet">
		
		<#if dataTables?has_content && dataTables>
		<link href="${themebase}/vendors/datatables.net-bs/css/dataTables.bootstrap.min.css" rel="stylesheet" />
    	<link href="${themebase}/vendors/datatables.net-buttons-bs/css/buttons.bootstrap.min.css" rel="stylesheet" />
    	<link href="${themebase}/vendors/datatables.net-fixedheader-bs/css/fixedHeader.bootstrap.min.css" rel="stylesheet" />
    	<link href="${themebase}/vendors/datatables.net-responsive-bs/css/responsive.bootstrap.min.css" rel="stylesheet" />
    	<link href="${themebase}/vendors/datatables.net-scroller-bs/css/scroller.bootstrap.min.css" rel="stylesheet" />
    	</#if>
    	<#if calendarView?has_content && calendarView != "LIST">
	    <link href="${themebase}/vendors/fullcalendar/dist/fullcalendar.min.css" rel="stylesheet">
    	<link href="${themebase}/vendors/fullcalendar/dist/fullcalendar.print.css" rel="stylesheet" media="print">
    	</#if>
	    
	    <!-- jQuery -->
    	<script src="${themebase}/vendors/jquery/dist/jquery.min.js"></script>
    	<script type="text/javascript" src="/public/js/jquery.timepicker.min.js"></script>
    	<script type="text/javascript" src="/public/js/moment.min.js"></script>
    	<script src="/public/js/jquery-ui.min.js"></script>
    	<script type="text/javascript" src="/public/js/ui-multiple.js"></script>
		<title>Document Production</title>
	</head>
	<body class="nav-md footer_fixed">
	<script>
		if (localStorage.getItem('menu_toggle') == '0') {
			$("body").removeClass('nav-md');
			$("body").addClass('nav-sm');
		}
	</script>
    <div class="container body">
      <div class="main_container">
        <div class="col-md-3 left_col menu_fixed">
          <div class="left_col scroll-view">
            <div class="navbar nav_title" style="border: 0;">
              <a href="${casServiceLogin}" class="site_title"><span>Document Production</span></a>
            </div>

            <div class="clearfix"></div>

			<div class="modal fade" id="modal" tabindex="-1" role="dialog" aria-labelledby="modalLabel" aria-hidden="true">
			  <div class="modal-dialog modal-lg">
			    <div class="modal-content">
			      <div class="modal-body">
			      </div>
			      <div class="modal-footer">
			      </div>
			    </div>
			  </div>
			</div>

            <div id="sidebar-menu" class="main_menu_side hidden-print main_menu">
              <div class="menu_section active">
                <ul class="nav side-menu">
                    <li>
                    	<a href="${fullUrl}/document"><i class="fa fa-book"></i> Documents</a>
	                </li>
	                <li>
                    	<a href="${fullUrl}/signature"><i class="fa fa-pencil"></i> Signatures</a>
	                </li>
	                <li>
                    	<a href="${fullUrl}/template"><i class="fa fa-file-code-o"></i> Document templates</a>
	                </li>
	                <li>
                    	<a href="${fullUrl}/documentsignature"><i class="fa fa-edit"></i> Signature templates</a>
	                </li>
	                <li>
                    	<a href="${fullUrl}/signaturekey"><i class="fa fa-key"></i> Signing keys</a>
	                </li>
	                <li>
                    	<a href="${fullUrl}/apikey"><i class="fa fa-cogs"></i> API keys</a>
	                </li>
	                <li>
                    	<a href="${fullUrl}/audit"><i class="fa fa-database"></i> Audit</a>                    	
	                </li>
                </ul>
              </div>
            </div>

            <div class="sidebar-footer hidden-small">
            </div>
          </div>
        </div>
        
        <!-- top navigation -->
        <div class="top_nav">
          <div class="nav_menu">
            <nav class="" role="navigation">
              <div class="nav toggle">
                <a id="menu_toggle"><i class="fa fa-bars"></i></a>
              </div>				
              <ul class="nav navbar-nav navbar-right" style="margin-top:0.8em">
                <li class="logout">
                  	<form method="POST" action="/logout">
                  	   <span class="logged-in-as"><i class="fa fa-user"> </i> Logged in as ${username!'-'}</span>
					   <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
					   <input id="logout" type="submit" value="Log out" class="btn btn-info" />
					</form>
                </li>
              </ul>
            </nav>
          </div>
        </div>
        <!-- /top navigation -->

		<!-- page content -->
        <div class="right_col" role="main">
          <div class="">
            <div class="page-title">
              <div class="title_left">
                <h2>${pageTitle!'Document Production'}</h2>
              </div>
			  
            </div>
            <div class="clearfix"></div>
			
	
	

    
	