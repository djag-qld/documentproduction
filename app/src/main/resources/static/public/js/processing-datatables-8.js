$(document).ready(function() {
	$("#records-table").DataTable( {
		 "pageLength": 25,
		 "searching": false
	});
	$("#relationships-table").DataTable( {
		 "pageLength": 10,
		 "ordering": false,
		 "searching": false,
		 "lengthChange": false
	});
	$("#audit-table").DataTable( {
		 "pageLength": 10,
		 "ordering": false,
		 "searching": true
	});
	$("#notes-table").DataTable( {
		 "pageLength": 5,
		 "ordering": false,
		 "searching": true,
		 "lengthMenu": [ 5, 10, 25, 50, 100 ]
	});	
	
	$("#tasks-table").DataTable( {
		 "pageLength": 5,
		 "ordering": false,
		 "searching": true,
		 "lengthMenu": [ 5, 10, 25, 50, 100 ]
	}).on('draw', function() { 
		$('.barcodePreview').click(function(e) {
			var url = $(this).attr('href');
			$('#modal').find('.modal-body').load(url, function(result) {
				$('#modal').modal('show');
				$('#modal').find('#modalTypeHeader').text($('#previewType').text());
			});
		});
	});
	
	$("#admin-table").DataTable( {
		 "pageLength": 25,
		 "searching": true
	});
});
