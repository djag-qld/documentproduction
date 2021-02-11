<#assign pageTitle>Documents</#assign>
<#assign dataTables=true />
<#include "top.ftl"/>
<div class="x_panel" id="document">
    <div class="x_title">Documents produced</div>
    <div class="x_content">
    	<table class="table table-striped" id="dataListTable" style="width: 100%">
			<thead>
				<tr>
					<th>ID</th>
					<th>Created</th>
					<th>Created by</th>					
					<th>Template</th>
					<th>Signatures</th>
					<th>Counter</th>
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
			'url': 'document/list/data',
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
            	data: 'created',
            	render: function(data, type, row) {
            		return timeConverter(row.created);
            	}
            },
			{ 
            	data: 'createdBy'
			},
			{ 
            	data: 'template.alias'
			},
			{
				data: 'signatures',				
				render: function(data, type, row) {
					console.log(data);
					console.log(type);
					console.log(row);
					var joined = "";
					for (var i=0; i < data.length; i++) {
						joined = data[i].alias + "; ";
					}
					return joined;
				},
				"searchable": false
			},
			{ 
            	data: 'counter'
			}
        ],
        order: [[ 1, "desc" ]],
        serverSide: true,
		responsive: true
	});
});

</script>
<#include "bottom.ftl"/>