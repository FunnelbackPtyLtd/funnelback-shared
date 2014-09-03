//NOTE: parameters set on page: contentOptimiser

$(document).ready(function() {
	
			$(document) //START document daisy chain
			
			.on('click','.link-collections', function(){
				var catchTarget = $('#catch-collection-list');
				
				// prevent loading when not needed to
				if (!catchTarget.hasClass('loaded')){
	
					$.get('?',function(data){
						var list = $(data).find('.fb-list').html();
						
						list = list.replace(/collection=/g,'query=' + contentOptimiser.target_url + '&collection=');
						
						catchTarget.html(list).addClass('loaded');
					});
	
				} 
 				//return false;
				
			})
			
			// Click on Query and URL Fields in the Content Summary 
			.on('click','#co-cs-form',function(){
	
				var a = $(this); 
				var b = 'disguise';
				
				//add classes to show the form via CSS
				a.addClass('active');	
				if(a.hasClass(b)){
					a.removeClass(b);
					$('#co-cs-summary').hide();
					
					// Might be needed if not working as expected 
					//setTimeout(function(){$('#form_query').focus();},200);
					
				// focus in on the form on conditions met
				var targetFormQuery = $(this).find('#form_query');
				var targetFormURL = $(this).find('#form_url');
				
				if (!$(targetFormQuery).is(':focus') && !$(targetFormURL).is(':focus')){
					$(targetFormQuery).focus();
					}
				}
				
	
			})
			
			// Unfocus on Query and URL Fields in the Content Summary
			.on('focusout','#co-cs-form',function(){
				var a = $(this); 
				var b = 'disguise';
				
				setTimeout(function(){
					if(a.hasClass('active')){}
					else
					{
					a.addClass(b);
					$('#co-cs-summary').show();	
					}
				},500);
				
				if(!a.hasClass(b)){
				a.removeClass('active');
				}
				
			})
						
				
			; // End 'document' daisy chain 

		
			
}); // End 'document' ready