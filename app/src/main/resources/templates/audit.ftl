<#assign dataTables=true />
<#assign pageTitle>Audit events</#assign>
<#include "top.ftl"/>
<div class="x_panel" id="audit">
        
    <div class="x_content">
		<table class="table table-striped" id="dataListTable" style="width: 100%">
			<thead>
				<tr>
					<th>Created</th>
					<th>Created by</th>
					<th>Event</th>					
					<th>Target</th>
					<th>Target type</th>								
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
			'url': 'audit/list/data',
			'type': 'POST',
			'beforeSend': function (request) {
     			request.setRequestHeader("${_csrf.headerName}", "${_csrf.token}");
    		},
			'data': function(d) {
				return JSON.stringify(d);
			}
		}, columns: [
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
            	data: 'event'
			},
			{ 
            	data: 'target'
			},
			{ 
            	data: 'targetType'
			}
        ],
        order: [[ 0, "desc" ]],
        serverSide: true,
		responsive: true
	});
});

</script>
<#include "bottom.ftl"/>
