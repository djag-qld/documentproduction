<#assign pageTitle>Signatures</#assign>
<#include "top.ftl"/>
<div class="x_panel" id="documentsignature">        
    <div class="x_content">
		<table class="table table-striped">
			<thead>
				<tr>
					<th>Created</th>
					<th>Created by</th>
					<th>Alias</th>
					<th>Signatory template</th>
					<th>Reason template</th>
					<th>Location template</th>
					<th>Contact information template</th>
					<th>Signature key</th>
					<th>Version</th>
				</tr>
			</thead>
			<tbody>
				<#if items?has_content>
				<#list items as item>
				<tr>
					<td>${item.created?datetime?iso_local}</td>
					<td>${item.createdBy}</td>
					<td>${item.alias}</td>
					<td>${item.signatoryTemplate!''}</td>
					<td>${item.reasonTemplate!''}</td>
					<td>${item.locationTemplate!''}</td>
					<td>${item.contactInfoTemplate!''}</td>
					<td>${item.signatureKey.alias} (v:${item.signatureKey.version})</td>
					<td>${item.version}</td>
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
<div class="x_panel" id="documentsignatureadd">        
    <div class="x_title">
		<h2>Save Signature</h2>
		<div class="clearfix"></div>
	</div>
	<div class="x_content">
	<form method="POST" action="${fullUrl}/documentsignature/add" class="form-horizontal form-label-left">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        
        <div class="item form-group">
            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="alias">Alias <span class="required">*</span></label>
            <div class="col-md-6 col-sm-6">
            	<input class="form-control " type="text" value="" id="alias" name="alias" required="required"/>
            </div>
        </div>
        <div class="item form-group">
            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="alias">Signatory template <span class="required">*</span></label>
            <div class="col-md-6 col-sm-6">
            	<input class="form-control " type="text" value="" id="signatoryTemplate" name="signatoryTemplate" required="required"/>
            </div>
        </div>
        <div class="item form-group">
            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="reasonTemplate">Reason template <span class="required">*</span></label>
            <div class="col-md-6 col-sm-6">
            	<input class="form-control " type="text" value="" id="reasonTemplate" name="reasonTemplate" required="required"/>
            </div>
        </div>
        <div class="item form-group">
            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="locationTemplate">Location template</label>
            <div class="col-md-6 col-sm-6">
            	<input class="form-control " type="text" value="" id="locationTemplate" name="locationTemplate" />
            </div>
        </div>
        <div class="item form-group">
            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="contactInfoTemplate">Contact information template</label>
            <div class="col-md-6 col-sm-6">
            	<input class="form-control " type="text" value="" id="contactInfoTemplate" name="contactInfoTemplate"/>
            </div>
        </div>
        <div class="item form-group">
            <label class="col-form-label col-md-3 col-sm-3 label-align" for="alias">Signature key <span class="required">*</span></label>
            <div class="col-md-6 col-sm-6">
	            <select class="form-control" name="signatureKeyAlias" required="required">
	            	<#list signatureKeyAliases as signatureKeyAlias>
	            		<option value="${signatureKeyAlias.alias} v:${signatureKeyAlias.version}">${signatureKeyAlias.alias} (version: ${signatureKeyAlias.version})</option>
	            	</#list>
	            </select>
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
