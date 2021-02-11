// Creates a new file and add it to our list
function ui_multi_add_file(origin, id, file)
{
  var template = $('#files-template').text();
  template = template.replace('%%filename%%', file.name);

  template = $(template);
  template.prop('id', 'uploaderFile' + id);
  template.data('file-id', id);

  origin.find('.files').find('li.empty').fadeOut(); // remove the 'no files yet'
  origin.find('.files').prepend(template);
}

// Changes the status messages on our list
function ui_multi_update_file_status(origin, id, status, message)
{
  origin.find('#uploaderFile' + id).find('span').html(message).prop('class', 'text-' + status);
}

// Updates a file progress, depending on the parameters it may animate it or change the color.
function ui_multi_update_file_progress(origin, id, percent, color, active)
{
  color = (typeof color === 'undefined' ? false : color);
  active = (typeof active === 'undefined' ? true : active);

  var bar = origin.find('#uploaderFile' + id).find('div.progress-bar');

  bar.width(percent + '%').attr('aria-valuenow', percent);
  bar.toggleClass('progress-bar-striped progress-bar-animated', active);

  if (percent === 0){
    bar.html('');
  } else {
    bar.html(percent + '%');
  }

  if (color !== false){
    bar.removeClass('bg-success bg-info bg-warning bg-danger');
    bar.addClass('bg-' + color);
  }
}

// Toggles the disabled status of Star/Cancel buttons on one particular file
function ui_multi_update_file_controls(origin, id, start, cancel, wasError)
{
  wasError = (typeof wasError === 'undefined' ? false : wasError);

  origin.find('#uploaderFile' + id).find('button.start').prop('disabled', !start);
  origin.find('#uploaderFile' + id).find('button.cancel').prop('disabled', !cancel);

  if (!start && !cancel) {
    origin.find('#uploaderFile' + id).find('.controls').fadeOut();
  } else {
    origin.find('#uploaderFile' + id).find('.controls').fadeIn();
  }

  if (wasError) {
    origin.find('#uploaderFile' + id).find('button.start').html('Retry');
  }
}
