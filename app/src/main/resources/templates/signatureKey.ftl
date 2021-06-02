<#assign pageTitle>Signing keys</#assign>
<#include "top.ftl"/>
<div class="row">
	<div class="col-md-12 col-sm-12">
		<div class="x_panel" id="signaturekey">
			<a href="signaturekey/toggleLatest" id="toggleLatest"><#if hideInactive>Show inactive<#else>Hide inactive</#if></a>
		    <div class="x_content">
				<table class="table table-striped">
					<thead>
						<tr>
							<th>Created</th>
							<th>Created by</th>
							<th>Alias</th>
							<th>KMS ID</th>
							<th>Timestamp endpoint</th>
							<th>Version</th>
							<th>Certificate</th>					
						</tr>
					</thead>
					<tbody>
						<#if items?has_content>
						<#list items as item>
						<tr>
							<td>${item.created?datetime?iso_local}</td>
							<td>${item.createdBy}</td>
							<td>${item.alias}</td>
							<td>${item.kmsId}</td>
							<td>${item.timestampEndpoint!'-'}</td>
							<td>${item.version}</td>
							<td><#if item.certificate?has_content><a href="${fullUrl}/signaturekey/${item.alias}/certificate/${item.version}">Certificate</a></#if></td>
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
	</div>
</div>
<div class="row">
	<div class="col-md-6 col-sm-6">
		<div class="x_panel" id="signaturekeyadd">        
		    <div class="x_title">
				<h2>Save signing key</h2>
				<div class="clearfix"></div>
			</div>
			<div class="x_content">
				<form method="POST" action="${fullUrl}/signaturekey/add" class="form-label-left">
			        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
			        
			        <div class="form-group row">
			            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="alias">Alias <span class="required">*</span></label>
			            <div class="col-md-9 col-sm-9 ">
			            	<input class="form-control " type="text" value="" id="alias" name="alias" required="required"/>
			            </div>
			        </div>
			        <div class="form-group row">
			            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="alias">KMS ID <span class="required">*</span></label>
			            <div class="col-md-9 col-sm-9 ">
			            	<input class="form-control " type="text" value="" id="kmsId" name="kmsId" required="required"/>
			            </div>
			        </div>
			        <div class="form-group row">
			            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="alias">Timestamp endpoint URL </label>
			            <div class="col-md-9 col-sm-9 ">
			            	<input class="form-control " type="text" value="" id="timestampEndpoint" name="timestampEndpoint" />
			            </div>
			        </div>
			        <div class="form-group row">
			            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="alias">Certificate response </label>
			            <div class="col-md-9 col-sm-9 ">
			            	<textarea class="form-control " id="certificate" name="certificate"></textarea>
			            </div>
			        </div>
			        <div class="ln_solid"></div>
    				<div class="form-group">
		        		<input class="btn btn-primary " name="action" type="submit" value="Save" id="create"/>
		        	</div>
			    </form>
			</div>
		</div>
	</div>
	<div class="col-md-6 col-sm-6">
		<div class="x_panel" id="signaturekeyadd">        
		    <div class="x_title">
				<h2>Generate CSR</h2>
				<div class="clearfix"></div>
			</div>
			<div class="x_content">
				<form method="POST" action="${fullUrl}/signaturekey/csr" class="form-label-left">
			        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
			        
			        <div class="form-group row">
			            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="alias">Alias <span class="required">*</span></label>
			            <div class="col-md-9 col-sm-9 ">
				            <select class="form-control" id="csralias" name="alias">
			            		<#list items as item>
			            			<option value="${item.alias} v:${item.version}">${item.alias} (version: ${item.version})</option>
			            		</#list>
			            	</select>            	
			            </div>
			        </div>
			        <div class="form-group row">
			            <label class="col-form-label col-md-3 col-sm-3 label-align"  for="alias">Subject DN <span class="required">*</span></label>
			            <div class="col-md-9 col-sm-9 ">
			            	<input class="form-control " type="text" value="${defaultSubjectdn}" id="kmsId" name="subjectdn" required="required"/>
			            </div>
			        </div>
			        <div class="ln_solid"></div>
    				<div class="form-group">
		        		<input class="btn btn-primary " name="action" type="submit" value="Create" id="create"/>
		        	</div>
			    </form>
			</div>
		</div>
	</div>
</div>
<#include "bottom.ftl"/>