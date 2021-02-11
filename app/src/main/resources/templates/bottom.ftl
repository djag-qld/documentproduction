          </div>
        </div>
        <!-- /page content -->

        <!-- footer content -->
        <footer>        	          
          <div class="pull-right">          
          Document Production – ${appVersion!'-'} – Page generated – ${.now?datetime}
          </div>
          
        </footer>
        <!-- /footer content -->
      </div>
    </div>
    
    <script>
    function timeConverter(timestamp) {
		if (!timestamp) {
			return '-';
		}
		var a = new Date(timestamp);
		var year = a.getFullYear();
		var month = a.getMonth() + 1;
		if (month < 10) {
			month = '0' + month;
		}
			  
		var date = a.getDate();
		if (date < 10) {
			date = '0' + date;
		}
		var hour = a.getHours();
		if (hour < 10) {
			hour = '0' + hour;
		}
		var min = a.getMinutes();
		if (min < 10) {
			min = '0' + min;
		}
		
		var sec = a.getSeconds();
		if (sec < 10) {
			sec = '0' + sec;
		}
		
		return year + '/' + month + '/' + date + ' ' + hour + ':' + min + ':' + sec ;
	}
	</script>

    
    <!-- Bootstrap -->
    <script src="${themebase}/vendors/bootstrap/dist/js/bootstrap.min.js"></script>
    <!-- FastClick -->
    <script src="${themebase}/vendors/fastclick/lib/fastclick.js"></script>
    <!-- NProgress -->
    <script src="${themebase}/vendors/nprogress/nprogress.js"></script>
    
    <!-- bootstrap-progressbar -->
    <script src="${themebase}/vendors/bootstrap-progressbar/bootstrap-progressbar.min.js"></script>

	<script src="${themebase}/vendors/iCheck/icheck.min.js"></script>

	
    <!-- Custom Theme Scripts -->
    <script src="${themebase}/build/js/custom.min.js"></script>
    <script type="text/javascript" src="${themebase}/vendors/select2/dist/js/select2.min.js"></script>
    <#if calendarView?has_content && calendarView != "LIST">
    <script src="${themebase}/production/js/moment/moment.min.js"></script>
	<script src="${themebase}/vendors/fullcalendar/dist/fullcalendar.min.js"></script>
	</#if>
	
	<#if dataTables?has_content && dataTables>
    <script src="${themebase}/vendors/datatables.net/js/jquery.dataTables.min.js"></script>
    <script src="${themebase}/vendors/datatables.net-bs/js/dataTables.bootstrap.min.js"></script>
    <script src="${themebase}/vendors/datatables.net-buttons/js/dataTables.buttons.min.js"></script>
    <script src="${themebase}/vendors/datatables.net-buttons-bs/js/buttons.bootstrap.min.js"></script>
    <script src="${themebase}/vendors/datatables.net-buttons/js/buttons.flash.min.js"></script>
    <script src="${themebase}/vendors/datatables.net-buttons/js/buttons.html5.min.js"></script>
    <script src="${themebase}/vendors/datatables.net-buttons/js/buttons.print.min.js"></script>
    <script src="${themebase}/vendors/datatables.net-fixedheader/js/dataTables.fixedHeader.min.js"></script>
    <script src="${themebase}/vendors/datatables.net-keytable/js/dataTables.keyTable.min.js"></script>
    <script src="${themebase}/vendors/datatables.net-responsive/js/dataTables.responsive.min.js"></script>
    <script src="${themebase}/vendors/datatables.net-responsive-bs/js/responsive.bootstrap.js"></script>
    <script src="${themebase}/vendors/datatables.net-scroller/js/dataTables.scroller.min.js"></script>
	</#if>

    </body>
</html>