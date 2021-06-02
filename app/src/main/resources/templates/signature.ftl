<#assign pageTitle>Signatures</#assign>
<#assign dataTables=true />
<#include "top.ftl"/>
<div class="x_panel" id="document">
    <div class="x_title">Signatures produced</div>
    <div class="x_content">
    	<table class="table table-striped" id="dataListTable" style="width: 100%">
			<thead>
				<tr>
					<th>ID</th>
					<th>Created</th>
					<th>Signature</th>
					<th>Signature digest</th>
					<th>Signature algorithm</th>
					<th>KMS ID</th>					
					<th>Status</th>
				</tr>
			</thead>
			<tbody>
			</tbody>
		</table>	
    </div>
</div>

<script>

$(document).ready(function() {
	$('#dataListTable').dataTable({
		'ajax': {
			'contentType': 'application/json', 
			'url': 'signature/list/data',
			'type': 'POST',
			'beforeSend': function (request) {
     			request.setRequestHeader("${_csrf.headerName}", "${_csrf.token}");
    		},
			'data': function(d) {
				return JSON.stringify(d);
			}
		}, columns: [
			{
				data: 'id',
			},
			{ 
            	data: 'createdAt',
            	render: function(data, type, row) {            		
            		return timeConverter(row.createdAt);
            	}
            },
			{
				data: 'signatureHex',
			},
			{
				data: 'signatureHexAlgorithm',
			},
			{
				data: 'signatureAlgorithm',
			},
			{
				data: 'keyId',
			},
			{ 
            	data: 'status'
			}
        ],
        order: [[ 1, "desc" ]],
        serverSide: true,
		responsive: true
	});
});

</script>
<#include "bottom.ftl"/>