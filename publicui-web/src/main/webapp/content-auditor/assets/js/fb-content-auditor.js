// fb-content-auditor.js

if(console) console.log('fb-content-auditor.js is initiated.');

jQuery(function () {
  $('[data-toggle="tooltip"]').tooltip();
  
  $(document)
  .on('focus','.search-form input',function(){
	$(this).parents('form, div').addClass('has-focus');
	  
  })
  .on('blur','.search-form input', function(){
	$(this).parents('form, div').removeClass('has-focus');
  })
  .on('click','.table-row-clickable tr td:not(.table-hide)', function(e){
	  if(!$(this).hasClass('fa-stack'))
	window.location =  $(this).find('.clickable-link').attr('href');
  })
  ;

});