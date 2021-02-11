<#assign pageTitle>Templates</#assign>
<#include "top.ftl"/>
<div class="x_panel" id="template">        
    <div class="x_content">
		<table class="table table-striped">
			<thead>
				<tr>
					<th>Created</th>
					<th>Created by</th>
					<th>Alias</th>
					<th>Version</th>
					<th>Actions</th>
				</tr>
			</thead>
			<tbody>
				<#if items?has_content>
				<#list items as item>
				<tr>
					<td>${item.created?datetime?iso_local}</td>
					<td>${item.createdBy}</td>
					<td>${item.alias}</td>
					<td>${item.version}</td>
					<td><a href="${fullUrl}/template/view/${item.alias}/${item.version}">Download</a> <br/>
					<a href="${fullUrl}/template/preview/${item.alias}/${item.version}">Print preview</a></td>
				</tr>
				</#list>
				<#else>
				<tr>
					<td colspan="999">No data available in this table</td>
				</tr>
				</#if>
			</tbody>
		</table>
	</div>
</div>
<div class="x_panel" id="templateadd">        
    <div class="x_title">
		<h2>Save Template</h2>
		<div class="clearfix"></div>
	</div>
	<div class="x_content">
	<form method="POST" action="${fullUrl}/template/add" class="">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        
        <div class="form-group row">
            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="alias">Alias <span class="required">*</span></label>
            <div class="col-md-6 col-sm-6">
            	<input class="form-control " type="text" value="" id="alias" name="alias" required="required"/>
            </div>
        </div>
        <div class="form-group row">
            <label class="col-form-label col-md-3 col-sm-3 label-align" for="content">Content <span class="required">*</span></label>
            <div class="col-md-6 col-sm-6">
            	<textarea style="height: 30em" class="form-control " type="text" value="" id="content" name="content" required="required">&lt;html&gt;Enter Freemarker HTML template here&lt;/html&gt;</textarea>
            </div>
        </div>
        <div class="ln_solid"></div>
		<div class="form-group">
        	<input class="btn btn-primary " name="action" type="submit" value="Save" id="create"/>
        </div>
    </form>
	</div>
</div>
<#include "bottom.ftl"/>