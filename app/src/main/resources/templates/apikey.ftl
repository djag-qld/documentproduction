<#assign pageTitle>API keys</#assign>
<#include "top.ftl"/>
<div class="x_panel" id="apikey">        
    <div class="x_content">
		<table class="table table-striped">
			<thead>
				<tr>
					<th>Created</th>
					<th>Created by</th>
					<th>Last modified</th>
					<th>Last modified by</th>
					<th>API Key ID</th>
					<th>Last used</th>
					<th>Status</th>
					<th>Actions</th>
				</tr>
			</thead>
			<tbody>
				<#if items?has_content>
				<#list items as item>
				<tr>
					<td>${item.created?datetime?iso_local}</td>
					<td>${item.createdBy}</td>
					<td>${item.lastModified?datetime?iso_local}</td>
					<td>${item.lastModifiedBy}</td>
					<td>${item.apiKeyId}</td>
					<td><#if item.lastUsed?has_content>${item.lastUsed?datetime?iso_local}<#else>-</#if></td>
					<td><#if item.enabled>Enabled<#else>Disabled</#if></td>
					<td>
						<form method="POST" action="${fullUrl}/apikey/toggle">
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
							<input type="hidden" name="apiKeyId" value="${item.apiKeyId}"/>
							<input class="btn btn-<#if item.enabled!false>warning<#else>success</#if>" type="submit" value="<#if item.enabled!false>Disable<#else>Enable</#if>" />
						</form>
					</td>
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
<div class="x_panel" id="apikeyadd">        
    <div class="x_title">
		<h2>Create new API key</h2>
		<div class="clearfix"></div>
	</div>
	<div class="x_content">
	<form method="POST" action="${fullUrl}/apikey/add">
        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
        
        <div class="form-group">
            <label for="name">Generated API Key</label>
            <p id="newApiKey">${apiKey!''}</p>
            <input type="hidden" value="${apiKey!''}" id="apiKey" name="apiKey"/>
        </div>
        <div class="ln_solid"></div>
        <div class="form-group">
        	<input class="btn btn-primary " name="action" type="submit" value="Create" id="create"/>
        </div>
    </form>
	</div>
</div>
<#include "bottom.ftl"/>
