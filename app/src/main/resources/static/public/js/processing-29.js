/* Adds an input group for a TaskInputDefinitionField that increments the ids and names so they can be mapped in the admin service. */
function addTaskInputDefinitionGroup(type, nameVal, categoryVal, categoryOrderVal, displayRowVal, displayColumnVal, defaultValueVal, maxLengthVal, minLengthVal, readonlyVal, fieldTypeVal, searchableTypeVal, searchableSubTypeVal) {
	var inc = $('.' + type).length;
	var cloned = $('.' + type + ':first').clone();
	
	cloned.find('input').each(function() {
		$(this).attr('id', type + '-' + inc + '-' + $(this).attr('fieldName'));
		$(this).attr('name', type + '-' + inc + '-' + $(this).attr('fieldName'));	
	});
	
	cloned.find('select').each(function() {
		$(this).attr('id', type + '-' + inc + '-' + $(this).attr('fieldName'));
		$(this).attr('name', type + '-' + inc + '-' + $(this).attr('fieldName'));
	});
	cloned.find('option').prop('selected', false);
	setTaskInputFields(cloned, nameVal, categoryVal, categoryOrderVal, displayRowVal, displayColumnVal, defaultValueVal, maxLengthVal, minLengthVal, readonlyVal, fieldTypeVal, searchableTypeVal, searchableSubTypeVal);
	cloned.removeClass('hidden');
	cloned.appendTo('.' + type + 'Wrapper');
}

function setTaskInputFields(cloned, nameVal, categoryVal, categoryOrderVal, displayRowVal, displayColumnVal, defaultValueVal, maxLengthVal, minLengthVal, readonlyVal, fieldTypeVal, searchableTypeVal, searchableSubTypeVal) {
	cloned.find('[fieldName="name"]').val(nameVal);
	cloned.find('[fieldName="category"]').val(categoryVal);
	cloned.find('[fieldName="categoryOrder"]').val(categoryOrderVal);
	cloned.find('[fieldName="searchableType"]').val(searchableTypeVal);
	cloned.find('[fieldName="searchableSubType"]').val(searchableSubTypeVal);
	cloned.find('[fieldName="displayColumn"]').val(displayColumnVal);
	cloned.find('[fieldName="displayRow"]').val(displayRowVal);
	cloned.find('[fieldName="defaultValue"]').val(defaultValueVal);
	cloned.find('[fieldName="maxLength"]').val(maxLengthVal);
	cloned.find('[fieldName="minLength"]').val(minLengthVal);
	if (readonlyVal) {
		cloned.find('[fieldName="readonly"][value="true"]').attr("checked", "checked");
		cloned.find('[fieldName="readonly"][value="false"]').removeAttr("checked");
	}
	cloned.find('[fieldName="fieldType"]').find('option[value="' + fieldTypeVal + '"]').attr('selected', 'selected');
}

function clearInputs(empty) {
	while ($('.attributes').length > (empty ? 0 : 1)) {
		$('.attributes:last').remove();
	}
}

function searchByBarcode(barcode) {
	window.location = "/services/registrations/processing/recordsBarcode/" + barcode;
}

processingPortal = {};
processingPortal.titleCasedFlag = {};
processingPortal.toTitleCaseName = function(str) {
    return str.toLowerCase().replace( /\b((m)(a?c))?(\w)/g, function($1, $2, $3, $4, $5) { 
        if ($2) {
            return $3.toUpperCase() + $4 + $5.toUpperCase();
        } 
        return $1.toUpperCase(); 
    });
}
processingPortal.toTitleCase = function(str) {
	return str.replace(/\w\S*/g, function(txt) {
		return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase();
	});
}
processingPortal.titleCaseNameOnFirstBlur = function(selector) {
	$(selector).blur(function() {
		id = $(this).attr('id');
		if (!processingPortal.titleCasedFlag[id]){
			obj = $("input[name='" + id + "']");
			obj.val(processingPortal.toTitleCaseName(obj.val()));
		}
		processingPortal.titleCasedFlag[id] = true;
	});
}
processingPortal.titleCaseOnFirstBlur = function(selector) {
	$(selector).blur(function() {
	    id = $(this).attr('id');
		if (!processingPortal.titleCasedFlag[id]){
			obj = $("input[name='" + id + "']");
			obj.val(processingPortal.toTitleCase(obj.val()));
		}
		processingPortal.titleCasedFlag[id] = true;
	});
}


var autoCompleteAddresses = {};

function initPlaces() {
	// Australia sometimes recognises different names to Google
    var countryNameRenames = {"Bosnia & Herzegovina":"Bosnia and Herzegovina",
            "Brunei":"Brunei Darussalam",
            "Cape Verde":"Cabo Verde",
            "Caribbean Netherlands":"Bonaire, Sint Eustatius and Saba",
            "Ceuta & Melilla":"Spanish North Africa",
            "Congo - Brazzaville":"Republic of Congo",
            "Congo - Kinshasa":"Democratic Republic of Congo",
            "Falkland Islands (Islas Malvinas)":"Falkland Islands",
            "Macedonia (FYROM)":"North Macedonia",
            "Micronesia":"Federated States of Micronesia",
            "Myanmar (Burma)":"Myanmar",
            "North Korea":"Democratic People's Republic of Korea",
            "Russia":"Russian Federation",
            "SÃ£o TomÃ© & PrÃ­ncipe":"Sao Tome and Principe",
            "South Korea":"Republic of Korea",
            "St. Kitts & Nevis":"St Kitts and Nevis",
            "St. Pierre & Miquelon":"St Pierre and Miquelon",
            "St. Vincent & Grenadines":"St Vincent and the Grenadines",
            "Swaziland":"Eswatini",
            "Trinidad & Tobago":"Trinidad and Tobago",
            "Turks & Caicos Islands":"Turks and Caicos Islands",
            "U.S. Virgin Islands":"United States Virgin Island",
            "Wallis & Futuna":"Wallis and Futuna",
            "Western Sahara":"Western Sahara",
            "United States":"United States of America"};
    
	var placeSearch, autocomplete;
	var componentForm = {
		subpremise: 'short_name',
		street_number: 'short_name',
		route: 'long_name',
		locality: 'long_name',
		administrative_area_level_1: 'short_name',
		country: 'long_name',
		postal_code: 'short_name',
		postal_town: 'long_name',
		sublocality_level_1: 'long_name'
	};

	initGooglePlacesAPI = function() {
		if (window.location.href.indexOf('http://localhost') == 0) {
			console.log('google places disabled for local dev');
			return;
		}
		
		var defaultBounds = new google.maps.LatLngBounds(
			new google.maps.LatLng(-27.917574, 153.702789)
		);
		var localizedToBrisbaneArea = {
			bounds: defaultBounds
		};
	
		$('.autoCompleteAddress').each(function() {
			var id = $(this).attr('id');
		  	// save these for later use by the listener
		  	autoAddress = new google.maps.places.Autocomplete(
				/** @type {HTMLInputElement} */ (document.getElementById(id)), localizedToBrisbaneArea);
		  	autoAddress.setFields(['address_component']);
			autoCompleteAddresses[id] = autoAddress;
			google.maps.event.addListener(autoAddress, 'place_changed', function () {
				fillInAddress(id);
			});
		});
	}
	
	// [START region_fillform]
	fillInAddress = function(address) {
		// registrationsProcessing.setupFieldDepends() should add the autoCompleteAddress class to appropriate address line1 fields
		$('.autoCompleteAddress').each(function() {
			var id = $(this).attr('id');
			if (address == id && autoCompleteAddresses[id]) {
				var userEnteredStreet = $(this).val();
				var place = autoCompleteAddresses[id].getPlace();
				var route = getAddressPart('route', place, 'long_name');
				var streetNumber = getAddressPart('street_number', place, 'short_name') + ' ' + getAddressPart('route', place, 'long_name');
				var firstWordOfStreet = route.split(' ',1)[0];
				if (firstWordOfStreet == 'Saint' && userEnteredStreet.includes("St ")) { // Google stores St as Saint and regex match won't work hence replaced with St
					firstWordOfStreet = 'St';
				}
			 
				var regex = RegExp('^(.*?)'+firstWordOfStreet); // get all the user entered values before a match with the first word from the Google result
				result = regex.exec(userEnteredStreet);
				if (Array.isArray(result)) {
					streetNumber = result[1] + '' + route; // add the street name to the user-entered unit & street number
				}
				$('#' + id.replace('.', '\\.')).val(streetNumber);
						
		        var stateAddressPart = getAddressPart('administrative_area_level_1', place, 'short_name');
		        var postalCodeAddressPart = getAddressPart('postal_code', place, 'short_name');
				var countryAddressPart = getAddressPart('country', place, 'long_name');
		        var suburbAddressPart = getAddressPart('locality', place, 'long_name');
		        var postalTownAddressPart = getAddressPart('postal_town', place, 'long_name');
		        var sublocalityLevel1AddressPart = getAddressPart('sublocality_level_1', place, 'long_name');
		        
		        if (suburbAddressPart.length == 0) {
		        	if (postalTownAddressPart.length > 1) {
		        		suburbAddressPart = postalTownAddressPart;
		        	} else {
		        		suburbAddressPart = sublocalityLevel1AddressPart;
		        	}
		        }
		        $('#' + id.replace('Line1', 'Suburb').replace('.', '\\.')).val(suburbAddressPart);
		        
		        for (var key in countryNameRenames) {
		            if (!countryNameRenames.hasOwnProperty(key)) {
		                continue;
		            }
		            countryAddressPart = countryAddressPart.replace(key, countryNameRenames[key]);
		        }
		        
		        if (postalCodeAddressPart.match(/^(BT)/)) {
		          countryAddressPart = "Northern Ireland";
		          stateAddressPart = "";
		        } else if (postalCodeAddressPart.match(/^(LL)|^(NP)|^(CF)|^(SA)|^(CH)|^(SY)|^(HR)|^(LD)/)) {
		          countryAddressPart = "Wales";
		          stateAddressPart = "";
		        } else if (postalCodeAddressPart.match(/^(AB)|^(DD)|^(FK)|^(PA)|^(DG)|^(ZE)|^(KA)|^(G)|^(EH)|^(IV)|^(KW)|^(PH)|^(KA)|^(TD)|^(ML)|^(HS)/)) {
		          countryAddressPart = "Scotland";
		          stateAddressPart = "";
		        } else if (countryAddressPart == "IE") {
		          countryAddressPart = "Republic of Ireland";
		          stateAddressPart = "";
		        } else if (countryAddressPart == "GB") {
		          countryAddressPart = "England";
		          stateAddressPart = "";
		        } else if (countryAddressPart == "United Kingdom") {
		          countryAddressPart = "England";
		          stateAddressPart = "";
		        } else if (countryAddressPart == "New Zealand") {
		          stateAddressPart = "";
		        }
		        
		        $('#' + id.replace('Line1', 'State').replace('.', '\\.')).val(stateAddressPart);
		        $('#' + id.replace('Line1', 'Postcode').replace('.', '\\.')).val(postalCodeAddressPart);
				$('#' + id.replace('Line1', 'Country').replace('.', '\\.')).val(countryAddressPart);
			}
		});
	}
  
	// Get each component of the address from the place details
	// and fill the corresponding field on the form.
	getAddressPart = function(part, place, typeItem) {
		for (var i = 0; place.address_components && i < place.address_components.length; i++) {
			var addressType = place.address_components[i].types[0];
			if (part == addressType) {
				return place.address_components[i][typeItem];
			}
	  	}
		return '';
	}
}

initPlaces();

reloadNotes = function(response) {
	let dataTable = $("#notes-table").DataTable();
	dataTable.clear();
	for (var i=0; i < response.noteViews.length; i++) {
		let createdAt = moment(response.noteViews[i].added);
		dataTable.row.add([
			createdAt.format('DD MMMM YYYY – h:mm:ss A'),
			response.noteViews[i].author.remoteEmail,
			response.noteViews[i].content
		]);
	}
	dataTable.draw();
}

var delay = (function() {
  var timer = 0;
  return function(callback, ms){
    clearTimeout (timer);
    timer = setTimeout(callback, ms);
  };
})();
var activeField = undefined;

$(document).ready(function() {
	
	$("#addNoteForm").submit(function(event){
		event.preventDefault();
		var post_url = $(this).attr("action");
		var request_method = $(this).attr("method"); 
		var form_data = $(this).serialize();
		$.ajax({
			url : post_url,
			type: request_method,
			data : form_data
		}).done(function(response) {
			if (response.noteViews) {
				$("#addNoteForm #noteContent").val("");
				reloadNotes(response);
			}
		}).error(function(err) {
			console.log(err);
		});
	});
	
	$("#saveForLaterFormButton").click(function() {
		$("#attributes\\.saveForLater\\.yes").val("true");
		$(this).closest("form").find("input").removeAttr("required");
		$(this).closest("form").find("select").removeAttr("required");
		$(this).closest("form").find("textarea").removeAttr("required");
		$("#submitFormButton").click();
		return false;
	});
	
	$('.barcodePreview').click(function(e) {
		var url = $(this).attr('href');
		$('#modal').find('.modal-body').load(url, function(result) {
			$('#modal').modal('show');
			$('#modal').find('#modalTypeHeader').text($('#previewType').text());
		});
	});
	
	$('.searchPreview').click(function(e) {
		var url = $(this).attr('href');
		activeField = $(this).parent().find("input");
		$('#modal').find('.modal-body').load(url, function(result) {
			$('#modal').modal('show');
			$('#modal').find('#modalTypeHeader').text($('#previewType').text());			
		});
	});
	
	if (typeof loadCalendarEvents === "function" && $.isFunction(jQuery.fn.fullCalendar)) {
		loadCalendarEvents();
  	}
		
	$(".delete").click(function() {
		return confirm("Are you sure?");
	});
	
	$(".barcode-search").find("button").click(function() {
		searchByBarcode($(".barcode-search").find("input[type='text']").val());
	});
	$(".barcode-search").find("input[type='text']").keypress(function(e) {
		if (e.which == 13) { //enter
			searchByBarcode($(".barcode-search").find("input[type='text']").val());
		}
	});
	
	//basic select2 select lists for ability to search
	$("select").not('.fieldType').select2();
	
	var viewer = {};
	if (window.location.href.indexOf("/admin/processes/list") > 0) {
		(function(BpmnModeler) {
			viewer = new BpmnModeler({ container: '#canvas', width: '100%', height: '900px' });
			var val = $("#workflowxml").val();
			viewer.importXML(val, function(err) { });
		})(window.BpmnJS);
		
		viewer.clear();
		
		$("#updateProcessXml").click(function(evt) {
			evt.preventDefault();
			viewer.saveXML({ format: true }, function(err, xml) {
				if (err) {
					
				} else {
					$("#workflowxml").val(xml);	
				}
			});
			
			$("#workflowxml").scrollTop(0);
			$('html, body').animate({
                scrollTop: $("#workflowXmlGroup").offset().top
            }, 0);
		});
	}
	
	$(".admin-edit").click(function() {
		var id = $(this).attr("rowid");
		$("#dataColValues-" + id).find("textarea").each(function() {
			var val = $(this).val();
			
			if (val && ($(this).attr("dataCol") === "attributes")) {
				$('.' + $(this).attr("dataCol") + ':first').addClass('hidden');
				var obj = JSON.parse(val);
				for (var i=0; i < obj.fields.length; i++) {
					addTaskInputDefinitionGroup($(this).attr("dataCol"), obj.fields[i].name, obj.fields[i].category, obj.fields[i].categoryOrder,  
						obj.fields[i].displayRow, obj.fields[i].displayColumn, obj.fields[i].defaultValue, 
						obj.fields[i].maxLength, obj.fields[i].minLength, obj.fields[i].readonly, obj.fields[i].type, 
						obj.fields[i].searchableType, obj.fields[i].searchableSubType);
				}
				
				return;
			}
			
			var inputField = $("#" + $(this).attr("dataCol"));
			if (inputField.is("textarea")) {
				if (inputField.attr("id") === "workflowxml") {
					viewer.clear();
					viewer.importXML(val, function(err) {
				  	});
				}
				inputField.text(val);
			} else {
				// input field likely a radio button set if this isnt found.
				if (inputField.size() === 0) {
					$("#" + $(this).attr("dataCol") + "-" + val).attr("checked", "checked");
				} else {
					inputField.val(val);
				}
			}
		});
		$("#deleteId").val(id);	
		$("#update-tab").click();
		$("select").not('.fieldType').select2();
	});
	
	
	$("#addattributes").click(function() {
		addTaskInputDefinitionGroup('attributes', '', 'all', '99999', '1', '1', '', 255, 0, false, 'STRING', '', '');
	});
	
	$("#removeattributes").click(function() {
		if ($('.attributes').length > 1) {
			$('.attributes:last').remove();
       	}
	});
	
	//Process start buttons
	$(".processStart").click(function() {
		$("#" + $(this).attr("formId")).submit();
	});

	$(".date-input").datepicker({ dateFormat: 'dd/mm/yy'});
	
	$("#previous").click(function() {
		$("#page").val(parseInt($("#page").val()) - 1);
		$("#searchForm").submit();
	});	
	$("#next").click(function() {
		$("#page").val(parseInt($("#page").val()) + 1);
		$("#searchForm").submit();
	});
	
	$("#menu_toggle").click(function() {
		if (localStorage.getItem('menu_toggle') == undefined || localStorage.getItem('menu_toggle') == '0') {
			  localStorage.setItem('menu_toggle', '1');
		  } else {
			  localStorage.setItem('menu_toggle', '0'); 
		  }
	});


  // prevent tabs in textareas - useful for template editing
  // ref: https://stackoverflow.com/questions/6140632/how-to-handle-tab-in-textarea
	$("textarea").keydown(function(e) {
	    if(e.keyCode === 9) { // tab was pressed
	        // get caret position/selection
	        var start = this.selectionStart;
	        var end = this.selectionEnd;
	
	        var $this = $(this);
	        var value = $this.val();
	
	        // set textarea value to: text before caret + tab + text after caret
	        $this.val(value.substring(0, start)
	                    + "\t"
	                    + value.substring(end));
	
	        // put caret at right position again (add one for the tab)
	        this.selectionStart = this.selectionEnd = start + 1;
	
	        // prevent the focus lose
	        e.preventDefault();
	    }
	});
	
	$(".searchQPreview").keyup(
	    function () {
	        delay(function () {
	            var keyword = $(".searchQPreview").val();
	            var URL = encodeURI("${fullUrl}/previewRecords/" + $(".searchQPreview").attr("recordType") + "/" + $(".searchQPreview").attr("recordSubType") + "?q=" + keyword);
	            $.ajax({
	                url: URL,
	                cache: false,
	                type: "GET",
	                success: function(response) {
	                	var contents = jQuery.parseHTML(response).find("#searchResults").html();
	                    $("#searchResults").html(contents);
	                }
	            });
	        }, 500);
	    }
	);
	
	if ($(".uploads") && $(".uploads").length > 0) {
		$(".uploads").dmUploader({
			maxFileSize: 20000000,
			url: 'upload',
			dnd: true,
			auto: true,
			method: 'POST',
			multiple: false,
			extraData: function() {
				taskId = $(this).find("[attr='taskId']").val();
				field = $(this).find("[attr='field']").val();
				csrf = $("input[name='_csrf']").val();
				return {
					"taskId": taskId,
					"field": field,
					"_csrf": csrf
				};
			},
			onNewFile: function(id, file) {
				var alphanumeric = /[^a-zA-Z0-9-_. ]/g;
				if (file.name.match(alphanumeric)) {
					$(this).parent().find('.files').html('File name contains invalid characters');
					taskId = $(this).find("[attr='taskId']").val();
					field = $(this).find("[attr='field']").val();
					$("#" + taskId + "-" + field + "-fileId").val("");
					return false;
				}
				$(this).parent().find('.files').html('');
				ui_multi_add_file($(this).parent(), id, file);
			},
			onBeforeUpload: function(id) {
				$(this).parent().find('.files').html('Uploading...');
			},
			onUploadSuccess: function(id, data) {
				if (data === 'error') {
					$(this).parent().find('.files').html('Upload failed');
					return;
				}
				
				$(this).parent().find('.files').html('Upload successful');
				$(this).parent().find('.uploadFileType').attr('title', 'Upload successful');
				taskId = $(this).find("[attr='taskId']").val();
				field = $(this).find("[attr='field']").val();
				inputFileId = taskId + "-" + field + "-fileId";
				$("#" + inputFileId).val(data);
			},
			onUploadError: function(id, xhr, status, message) {
				$(this).parent().find('.files').html(message);
			},
			onFileSizeError: function(file) {
				$(this).parent().find('.files').html('File too large');
			},
			onFileTypeError: function(file) {
				$(this).parent().find('.files').html('Not a supported file type');
			}
		});
	}
	
	
  
	registrationsProcessing.setupFieldDepends();
	initGooglePlacesAPI();
});



	
